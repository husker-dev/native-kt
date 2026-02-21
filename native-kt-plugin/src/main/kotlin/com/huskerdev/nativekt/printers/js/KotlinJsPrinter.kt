package com.huskerdev.nativekt.printers.js

import com.huskerdev.nativekt.utils.asyncFunctionName
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.isDealloc
import com.huskerdev.nativekt.utils.isString
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.syncFunctionName
import com.huskerdev.webidl.resolver.BuiltinIdlDeclaration
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlCallbackFunction
import com.huskerdev.webidl.resolver.ResolvedIdlDictionary
import com.huskerdev.webidl.resolver.ResolvedIdlEnum
import com.huskerdev.webidl.resolver.ResolvedIdlField
import com.huskerdev.webidl.resolver.ResolvedIdlInterface
import com.huskerdev.webidl.resolver.ResolvedIdlNamespace
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import com.huskerdev.webidl.resolver.ResolvedIdlTypeDef
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind
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
            package $classPath
            
            import kotlin.js.*
            ${
                if(useCoroutines) "import kotlinx.coroutines.await"
                else ""
            }
            
            @JsModule("$fileName")
            private external val _lib: dynamic
            
            private var _module: dynamic = null
            
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

        val arenaName = "${moduleName.capitalized()}Arena"

        builder.append("""
            
            class $arenaName: AutoCloseable {
                companion object {
                    fun <T> use(block: $arenaName.() -> T) = 
                        $arenaName().use { block(it) }
                }
                
                private val allocated = hashSetOf<Any>()
                
                fun malloc(size: Int): Any =
                    (_module._malloc(size) as Any).also { allocated += this }
                
                fun String.cstr(): Any {
                    val len = _module.lengthBytesUTF8(this) + 1
                    val mem = malloc(len)
                    _module.stringToUTF8(this, mem, len)
                    return mem
                }
                
                fun asString(ptr: Any, dealloc: Boolean): String {
                    val result = _module.UTF8ToString(ptr)
                    if(dealloc && ptr !in allocated)
                        _module._free(ptr)
                    return result
                }
            
                override fun close() = allocated.forEach {
                    _module._free(it)
                }
            }
            
            private fun _${moduleName}UnwrapString(ptr: dynamic, dealloc: Boolean): String {
            	val result = _module.UTF8ToString(ptr)
            	if(dealloc)
            		_module._free(ptr)
            	return result
            }
            
            private fun _${moduleName}UnwrapLong(value: dynamic): Long {
            	val ptr = (value as JsNumber).toInt() shr 2

            	val low = (_module.HEAP32[ptr] as JsNumber).toLong()
            	val high = (_module.HEAP32[ptr + 1] as JsNumber).toLong()
            	_module._free(ptr)

            	return high shl 32 or (low and 0xffffffff)
            }
            
            private fun _${moduleName}WrapLong(value: Long): Int {
            	val ptr = (_module._malloc(8) as JsNumber).toInt()
                _module.HEAP32[ptr shr 2] = (value and 0xffffffff).toInt()
                _module.HEAP32[(ptr shr 2) + 1] = (value shr 32).toInt()
                return ptr
            }
            
        """.trimIndent())

        idl.globalOperators().forEach { printFunction(builder, it) }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function, isActual = expectActual, forcePrintVoid = true)
        append(" = ")

        val useArena = function.args.any { it.type.isString() } || function.type.isString()

        if(useArena)
            append("${moduleName.capitalized()}Arena.use {")
        append("\n\t")

        val func = "_module.__${function.name}"
        append(castFromJS(function.type, "$func(${castArgs(function.args)})", function.isDealloc()))

        if(useArena)
            append("\n}")

        append("\n")
    }

    private fun castArgs(args: List<ResolvedIdlField.Argument>): String {
        return args.joinToString { arg ->
            castToJS(arg.type, arg.name)
        }
    }

    private fun castFromJS(type: ResolvedIdlType, content: String, dealloc: Boolean): String = when(type) {
        is ResolvedIdlType.Union -> throw UnsupportedOperationException()
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is ResolvedIdlCallbackFunction -> TODO()
            is ResolvedIdlDictionary -> TODO()
            is ResolvedIdlEnum -> TODO()
            is ResolvedIdlInterface -> TODO()
            is ResolvedIdlNamespace -> TODO()
            is ResolvedIdlTypeDef -> TODO()
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.ANY -> TODO()
                WebIDLBuiltinKind.OBJECT -> TODO()
                WebIDLBuiltinKind.VOID -> TODO()
                WebIDLBuiltinKind.LIST -> TODO()
                WebIDLBuiltinKind.MUTABLE_LIST -> TODO()
                WebIDLBuiltinKind.MAP -> TODO()
                WebIDLBuiltinKind.PROMISE -> TODO()
                WebIDLBuiltinKind.USV_STRING -> TODO()
                WebIDLBuiltinKind.BIG_INT -> TODO()
                WebIDLBuiltinKind.BYTE_SEQUENCE -> TODO()

                WebIDLBuiltinKind.BOOLEAN -> "$content == 1"

                WebIDLBuiltinKind.CHAR,
                WebIDLBuiltinKind.UNSIGNED_INT,
                WebIDLBuiltinKind.FLOAT,
                WebIDLBuiltinKind.UNRESTRICTED_FLOAT,
                WebIDLBuiltinKind.DOUBLE,
                WebIDLBuiltinKind.UNRESTRICTED_DOUBLE,
                WebIDLBuiltinKind.BYTE,
                WebIDLBuiltinKind.UNSIGNED_BYTE,
                WebIDLBuiltinKind.SHORT,
                WebIDLBuiltinKind.UNSIGNED_SHORT,
                WebIDLBuiltinKind.INT -> content

                WebIDLBuiltinKind.LONG,
                WebIDLBuiltinKind.UNSIGNED_LONG -> "_${moduleName}UnwrapLong($content)"

                WebIDLBuiltinKind.STRING -> "asString($content, $dealloc)"
            }
        }
    }

    private fun castToJS(type: ResolvedIdlType, content: String): String = when(type) {
        is ResolvedIdlType.Union -> throw UnsupportedOperationException()
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is ResolvedIdlCallbackFunction -> content
            is ResolvedIdlDictionary -> TODO()
            is ResolvedIdlEnum -> TODO()
            is ResolvedIdlInterface -> TODO()
            is ResolvedIdlNamespace -> TODO()
            is ResolvedIdlTypeDef -> TODO()
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.ANY -> TODO()
                WebIDLBuiltinKind.OBJECT -> TODO()
                WebIDLBuiltinKind.VOID -> TODO()
                WebIDLBuiltinKind.LIST -> TODO()
                WebIDLBuiltinKind.MUTABLE_LIST -> TODO()
                WebIDLBuiltinKind.MAP -> TODO()
                WebIDLBuiltinKind.PROMISE -> TODO()
                WebIDLBuiltinKind.USV_STRING -> TODO()
                WebIDLBuiltinKind.BIG_INT -> TODO()
                WebIDLBuiltinKind.BYTE_SEQUENCE -> TODO()

                WebIDLBuiltinKind.BOOLEAN,
                WebIDLBuiltinKind.CHAR,
                WebIDLBuiltinKind.UNSIGNED_INT,
                WebIDLBuiltinKind.FLOAT,
                WebIDLBuiltinKind.UNRESTRICTED_FLOAT,
                WebIDLBuiltinKind.DOUBLE,
                WebIDLBuiltinKind.UNRESTRICTED_DOUBLE,
                WebIDLBuiltinKind.BYTE,
                WebIDLBuiltinKind.UNSIGNED_BYTE,
                WebIDLBuiltinKind.SHORT,
                WebIDLBuiltinKind.UNSIGNED_SHORT,
                WebIDLBuiltinKind.INT -> content

                WebIDLBuiltinKind.LONG,
                WebIDLBuiltinKind.UNSIGNED_LONG -> "_${moduleName}WrapLong($content)"

                WebIDLBuiltinKind.STRING -> "$content.cstr()"
            }
        }
    }
}