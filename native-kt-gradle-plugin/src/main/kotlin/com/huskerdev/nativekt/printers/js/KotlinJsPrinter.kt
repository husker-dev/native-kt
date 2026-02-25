package com.huskerdev.nativekt.printers.js

import com.huskerdev.nativekt.utils.asyncFunctionName
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.isDealloc
import com.huskerdev.nativekt.utils.isString
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.syncFunctionName
import com.huskerdev.nativekt.utils.toEmscriptenDesc
import com.huskerdev.webidl.resolver.BuiltinIdlDeclaration
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlCallbackFunction
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind
import java.io.File

class KotlinJsPrinter(
    idl: IdlResolver,
    target: File,
    classPath: String,
    val moduleName: String,
    useCoroutines: Boolean,
    val expectActual: Boolean
) {
    private val fileName = "./lib${moduleName}.js"

    init {
        val actual = if(expectActual) "actual " else ""

        val builder = StringBuilder()
        builder.append("""
            @file:OptIn(ExperimentalWasmJsInterop::class)
            package $classPath
            
            import com.huskerdev.nativekt.web.*
            import kotlin.js.*
            ${
                if(useCoroutines) "import kotlinx.coroutines.await"
                else ""
            }
            
            @JsModule("$fileName")
            private external val _lib: dynamic
            
            private var _module: dynamic = null
            
        """.trimIndent())

        if(idl.callbacks.isNotEmpty())
            builder.append("""
                set(value) {
                    field = value
                    initCallbacks()
                }
                
            """.replaceIndent("\t"))

        builder.append("""
            
            ${actual}val isLibTestLoaded: Boolean
                get() = _module != null
            
            ${actual}fun ${syncFunctionName(moduleName)}(): Unit = 
                throw UnsupportedOperationException("Synchronous library loading is not supported in JS")
                
            ${actual}fun ${asyncFunctionName(moduleName)}(onReady: () -> Unit) {
                if(_module != null) 
                    return
                    
                (_lib.default() as Promise<dynamic>).then { it: dynamic ->
                    _module = it
                    onReady()
                }
            }
            
        """.trimIndent())

        if(useCoroutines) {
            builder.append("""
                
                ${actual}suspend fun ${asyncFunctionName(moduleName)}() {
                    if(_module == null)
                        _module = (_lib.default() as Promise<dynamic>).await()
                }
                
            """.trimIndent())
        }

        // Callbacks loading
        if(idl.callbacks.isNotEmpty()) {
            builder.append("\nprivate var _freeCallback: dynamic = null\n")
            idl.callbacks.values.joinTo(builder, separator = "\n") {
                "private var _invoke${it.name}: dynamic = null"
            }

            builder.append("""
                
                
                private fun initCallbacks() {
                    _freeCallback = createCallbackFreeFunction(_module)
                    
            """.trimIndent())
            idl.callbacks.values.forEach { printCallbackInvoke(builder, it) }
            builder.append("}\n")

            // wrap
            idl.callbacks.values.forEach { callback -> printCallbackWrap(builder, callback) }
        }

        idl.globalOperators().forEach { printFunction(builder, it) }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printCallbackWrap(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        append("\nprivate fun ")
        append(callback.name)
        append(".wrap")
        append(callback.name)
        append("() =\n\t")
        append("mallocCallback(_module, this, _invoke")
        append(callback.name)
        append(", _freeCallback)\n")
    }

    private fun printCallbackInvoke(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val args = listOf("_c") + callback.args.map { it.name }
        val castedArgs = callback.args.map { castToJS(it.type, it.name, it.isDealloc(), false) }
        val argTypes = listOf(callback.type.toEmscriptenDesc(), "p") +
                callback.args.map { it.type.toEmscriptenDesc() }

        // header
        append("\n\t_invoke")
        append(callback.name)
        append(" = _module.addFunction({ ")
        args.joinTo(builder)
        append(" ->\n\t\t")

        // body
        val call = "unwrapCallback<${callback.name}>(_module, _c, false)(${castedArgs.joinToString()})"
        append(castToNative(callback.type, call, dealloc = false, useArena = false))
        append("\n\t")

        // footer
        append("}, \"")
        argTypes.joinTo(builder, separator = "")
        append("\")\n")
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function, isActual = expectActual, forcePrintVoid = true)
        append(" = ")

        val useArena = function.args.any { it.type.isString() || it.isDealloc() }

        if(useArena)
            append("EmArena.use(_module) { arena ->")
        append("\n\t")

        val args = function.args.joinToString {
            castToNative(it.type, it.name, it.isDealloc(), useArena)
        }
        val func = "_module.__${function.name}"
        append(castToJS(function.type, "$func($args)", function.isDealloc(), useArena))

        if(useArena)
            append("\n}")

        append("\n")
    }

    private fun castToNative(type: ResolvedIdlType, content: String, dealloc: Boolean, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.LONG,
                WebIDLBuiltinKind.UNSIGNED_LONG -> "wrapLong(_module, $content)"
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "arena.allocCStr($content)"
                    else "allocCStr(_module, $content)"
                else -> content
            }
            is ResolvedIdlCallbackFunction ->
                if(dealloc) "arena.callback($content.wrap${decl.name}())"
                else "$content.wrap${decl.name}()"
            else -> throw UnsupportedOperationException(type.toString())
        }
        is ResolvedIdlType.Union -> throw UnsupportedOperationException(type.toString())
    }

    private fun castToJS(type: ResolvedIdlType, content: String, dealloc: Boolean, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.BOOLEAN -> "$content == 1"
                WebIDLBuiltinKind.LONG,
                WebIDLBuiltinKind.UNSIGNED_LONG -> "unwrapLong(_module, $content)"
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "arena.unwrapCStr($content, $dealloc)"
                    else "unwrapCStr(_module, $content, $dealloc)"
                else -> content
            }
            is ResolvedIdlCallbackFunction ->
                if(useArena) "arena.unwrapCallback<${decl.name}>($content, $dealloc)"
                else "unwrapCallback<${decl.name}>(_module, $content, $dealloc)"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }
}