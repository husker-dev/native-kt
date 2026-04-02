package com.huskerdev.nativekt.printers.js

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized
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
            @file:Suppress("unused", "ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT")
            
            package $classPath
            
            import com.huskerdev.nativekt.web.*
            import kotlin.js.*
            
        """.trimIndent())

        if(useCoroutines)
            builder.append("""
                import kotlinx.coroutines.suspendCancellableCoroutine
                import kotlin.coroutines.resume
                
            """.trimIndent())

        builder.append("""
            
            @JsModule("$fileName")
            private external val _lib: JsAny
            
            private lateinit var _module: Module
            
        """.trimIndent())

        val initCallbacks = if(idl.callbacks.isNotEmpty())
            "initCallbacks()" else ""

        builder.append("""
            
            fun wrapCallback(block: (JsNumber) -> Unit): JsAny = js("block")
            
            private var isLib${moduleName.capitalized()}Loaded_: Boolean = false
            ${actual}val isLib${moduleName.capitalized()}Loaded: Boolean
                get() = isLib${moduleName.capitalized()}Loaded_
            
            ${actual}fun ${syncFunctionName(moduleName)}(): Unit = 
                throw UnsupportedOperationException("Synchronous library loading is not supported in JS")
                
            ${actual}fun ${asyncFunctionName(moduleName)}(onReady: () -> Unit) {
                if(isLib${moduleName.capitalized()}Loaded) 
                    return
                
                loadLib<Module>(_lib).then {
                    _module = it
                    $initCallbacks
                    isLib${moduleName.capitalized()}Loaded_ = true
                    onReady()
                    _lib // does nothing, but required
                }
            }
            
        """.trimIndent())

        if(useCoroutines) {
            builder.append("""
                
                ${actual}suspend fun ${asyncFunctionName(moduleName)}() {
                    if(isLibTestLoaded)
                        return
                    suspendCancellableCoroutine { continuation ->
                        ${asyncFunctionName(moduleName)} {
                            continuation.resume(Unit)
                        }
                    }
                }
                
            """.trimIndent())
        }

        // Callbacks loading
        if(idl.callbacks.isNotEmpty()) {
            builder.append("\nprivate var _freeCallback: Int = 0\n")
            idl.callbacks.values.joinTo(builder, separator = "\n") {
                "private var _invoke${it.name} = 0"
            }

            builder.append("\n")
            idl.callbacks.values.forEachIndexed { index, callback ->
                builder.append("\nprivate fun _callback")
                builder.append(index)
                builder.append("Js(block: (")
                (arrayListOf("Int") + callback.args.map { toSimpleJsType(it.type) })
                    .joinTo(builder)
                builder.append(") -> ")
                builder.append(toSimpleJsType(callback.type))
                builder.append("): JsAny = js(\"block\")")
            }

            builder.append("""
                
                
                private fun initCallbacks() {
                    _freeCallback = createCallbackFreeFunction(_module)
                
            """.trimIndent())
            idl.callbacks.values.forEachIndexed { index, callback ->
                printCallbackInvoke(builder, callback, index)
            }
            builder.append("}\n")

            // wrap
            idl.callbacks.values.forEach { callback -> printCallbackWrap(builder, callback) }
        }

        idl.globalOperators().forEach { printFunction(builder, it) }

        printTypes(builder, idl.globalOperators())

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

    private fun printCallbackInvoke(
        builder: StringBuilder,
        callback: ResolvedIdlCallbackFunction,
        index: Int
    ) = builder.apply {
        val args = listOf("_c: Int") + callback.args.map { "${it.name}: ${toSimpleJsType(it.type)}" }
        val castedArgs = callback.args.map { castToJS(it.type, it.name, it.isDealloc(), false) }

        // header
        append("\n\t// ").append(callback.name)
        append("\n\t_invoke")
        append(callback.name)
        append(" = _module._setCallback(${index}, _callback${index}Js { ")
        args.joinTo(builder)
        append(" ->\n\t\t")

        // body
        val call = "unwrapCallback<${callback.name}>(_module, _c, false)(${castedArgs.joinToString()})"
        append(castToNative(callback.type, call, dealloc = false, useArena = false))
        append("\n\t")

        // footer
        append("})\n")
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function, isActual = expectActual, forcePrintVoid = true)
        append(" = ")

        val useArena = function.args.any { it.type.isString() || it.type.isArray() || it.isDealloc() }

        if(useArena)
            append("EmArena.use(_module) { arena ->")
        append("\n\t")

        val args = function.args.joinToString {
            castToNative(it.type, it.name, it.isDealloc(), useArena)
        }
        val func = "_module.${function.name}"
        append(castToJS(function.type, "$func($args)", function.isDealloc(), useArena))

        if(useArena)
            append("\n}")

        append("\n")
    }

    private fun printTypes(buffer: StringBuilder, functions: List<ResolvedIdlOperation>) = buffer.apply {
        append("""
            
            private external interface Lib: JsAny {
            	fun default(): Promise<Module>
            }

            private external interface Module: EmModule {
            	fun _setCallback(index: Int, callback: JsAny): Int

        """.trimIndent())

        functions.forEach { function ->
            append("\tfun ")
            append(function.name)
            append("(")
            function.args.joinTo(buffer) { "${it.name}: ${toSimpleJsType(it.type)}" }
            append(")")
            if(function.type !is ResolvedIdlType.Void) {
                append(": ")
                append(toSimpleJsType(function.type))
            }
            append("\n")
        }
        append("}\n")
    }

    private fun castToNative(type: ResolvedIdlType, content: String, dealloc: Boolean, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.CHAR -> "${content}.code"
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "arena.allocCStr($content)"
                    else "allocCStr(_module, $content)"
                WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                    when (declaration) {
                        is BuiltinIdlDeclaration -> {
                            val name = declaration.kind.simpleName()
                            if (useArena) "arena.toNative${name}Array($content)"
                            else "toNative${name}Array(_module, $content)"
                        }
                        is ResolvedIdlEnum ->
                            "toNativeEnumArray(_module, $content)"
                        is ResolvedIdlDictionary -> content
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlEnum -> "${content}.ordinal"
            is ResolvedIdlCallbackFunction ->
                if(dealloc) "arena.callback($content.wrap${decl.name}())"
                else "$content.wrap${decl.name}()"
            is ResolvedIdlDictionary -> content
            else -> throw UnsupportedOperationException(type.toString())
        }
        is ResolvedIdlType.Union -> throw UnsupportedOperationException(type.toString())
    }

    private fun castToJS(type: ResolvedIdlType, content: String, dealloc: Boolean, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.FLOAT -> "$content.truncF32()"
                WebIDLBuiltinKind.CHAR -> "$content.toChar()"
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "arena.unwrapCStr($content, $dealloc)"
                    else "unwrapCStr(_module, $content, $dealloc)"
                WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                    when (declaration) {
                        is BuiltinIdlDeclaration -> {
                            val name = declaration.kind.simpleName()
                            if (useArena) "arena.toKotlin${name}Array($content, $dealloc)"
                            else "toKotlin${name}Array(_module, $content, $dealloc)"
                        }
                        is ResolvedIdlEnum ->
                            "toKotlinEnumArray(_module, $content, $dealloc, ${declaration.name}.entries::get)"
                        is ResolvedIdlDictionary ->
                            content
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlEnum -> "${decl.name}.entries[$content]"
            is ResolvedIdlCallbackFunction ->
                if(useArena) "arena.unwrapCallback<${decl.name}>($content, $dealloc)"
                else "unwrapCallback<${decl.name}>(_module, $content, $dealloc)"
            is ResolvedIdlDictionary -> content
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }

    private fun toSimpleJsType(type: ResolvedIdlType): String = when(type) {
        is ResolvedIdlType.Void -> "Unit"
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.BOOLEAN -> "Boolean"
                WebIDLBuiltinKind.FLOAT -> "Float"
                WebIDLBuiltinKind.CHAR -> "Int"
                WebIDLBuiltinKind.INT -> "Int"
                WebIDLBuiltinKind.DOUBLE -> "Double"
                WebIDLBuiltinKind.BYTE -> "Byte"
                WebIDLBuiltinKind.SHORT -> "Short"
                WebIDLBuiltinKind.LONG -> "Long"
                WebIDLBuiltinKind.STRING -> "EmString"
                WebIDLBuiltinKind.LIST -> "EmArray"
                else -> "JsNumber"
            }
            is ResolvedIdlEnum -> "Int"
            is ResolvedIdlCallbackFunction -> "Int"
            else -> "JsNumber"
        }
        else -> throw UnsupportedOperationException(type.toString())
    }
}