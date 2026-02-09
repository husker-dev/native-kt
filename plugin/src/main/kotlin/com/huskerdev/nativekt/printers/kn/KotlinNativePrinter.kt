package com.huskerdev.nativekt.printers.kn

import com.huskerdev.nativekt.utils.asyncFunctionName
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.isDealloc
import com.huskerdev.nativekt.utils.isString
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.syncFunctionName
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File

class KotlinNativePrinter(
    idl: IdlResolver,
    target: File,
    classPath: String,
    val moduleName: String,
    useCoroutines: Boolean,
    val expectActual: Boolean
) {
    val cinteropPath = "cinterop.$classPath"

    init {
        val actual = if(expectActual) "actual " else ""

        val builder = StringBuilder()
        builder.append("""
            @file:OptIn(ExperimentalForeignApi::class)
            
            package $classPath
            
            import kotlinx.cinterop.*
            import platform.posix.free
            
            ${actual}val isLibTestLoaded: Boolean = true
            
            @Throws(UnsupportedOperationException::class)
            ${actual}fun ${syncFunctionName(moduleName)}() = Unit
            ${actual}fun ${asyncFunctionName(moduleName)}(onReady: () -> Unit) = onReady()
            
        """.trimIndent())
        if(useCoroutines)
            builder.append("${actual}suspend fun ${asyncFunctionName(moduleName)}() = Unit\n")

        printArena(builder)

        idl.globalOperators().forEach { printFunction(builder, it) }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printArena(builder: StringBuilder){
        val arenaName = "${moduleName.capitalized()}Arena"
        builder.append("""
            
            private class $arenaName(
            	val memScope: MemScope
            ) {
            	companion object {
            		fun <T> use(block: $arenaName.() -> T) = memScoped {
            			$arenaName(this).run { block(this) }
            		}
            	}
            	private val allocated = hashSetOf<Long>()

            	fun String.wrap() =
            		cstr.getPointer(memScope).also { allocated += it.rawValue.toLong() }

            	fun CPointer<ByteVar>.unwrap(dealloc: Boolean): String {
            		val result = toKString()
            		if(dealloc && rawValue.toLong() !in allocated)
            			free(this)
            		return result
            	}
            }
            
        """.trimIndent())
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function, isActual = expectActual)
        append(" = ")

        val useArena = function.args.any { it.type.isString() } || function.type.isString()

        if(useArena)
            append("${moduleName.capitalized()}Arena.use {")
        append("\n\t")

        val func = "$cinteropPath.${function.name}"
        append(castFromNative(function.type, "$func(${castArgs(function.args)})", function.isDealloc()))

        if(useArena)
            append("\n}")

        append("\n")
    }

    private fun castArgs(args: List<ResolvedIdlField.Argument>): String {
        return args.joinToString { arg ->
            castToNative(arg.type, arg.name)
        }
    }

    private fun castFromNative(type: ResolvedIdlType, content: String, dealloc: Boolean): String = when(type) {
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

                WebIDLBuiltinKind.BOOLEAN,
                WebIDLBuiltinKind.UNSIGNED_INT,
                WebIDLBuiltinKind.FLOAT,
                WebIDLBuiltinKind.UNRESTRICTED_FLOAT,
                WebIDLBuiltinKind.DOUBLE,
                WebIDLBuiltinKind.UNRESTRICTED_DOUBLE,
                WebIDLBuiltinKind.BYTE,
                WebIDLBuiltinKind.UNSIGNED_BYTE,
                WebIDLBuiltinKind.SHORT,
                WebIDLBuiltinKind.UNSIGNED_SHORT,
                WebIDLBuiltinKind.LONG,
                WebIDLBuiltinKind.UNSIGNED_LONG,
                WebIDLBuiltinKind.INT -> content

                WebIDLBuiltinKind.CHAR -> "$content.toInt().toChar()"
                WebIDLBuiltinKind.STRING -> "$content!!.unwrap($dealloc)"
            }
        }
    }


    private fun castToNative(type: ResolvedIdlType, content: String): String = when(type) {
        is ResolvedIdlType.Union -> throw UnsupportedOperationException()
        is ResolvedIdlType.Void -> "Unit"
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

                WebIDLBuiltinKind.BOOLEAN,
                WebIDLBuiltinKind.UNSIGNED_INT,
                WebIDLBuiltinKind.FLOAT,
                WebIDLBuiltinKind.UNRESTRICTED_FLOAT,
                WebIDLBuiltinKind.DOUBLE,
                WebIDLBuiltinKind.UNRESTRICTED_DOUBLE,
                WebIDLBuiltinKind.BYTE,
                WebIDLBuiltinKind.UNSIGNED_BYTE,
                WebIDLBuiltinKind.SHORT,
                WebIDLBuiltinKind.UNSIGNED_SHORT,
                WebIDLBuiltinKind.LONG,
                WebIDLBuiltinKind.UNSIGNED_LONG,
                WebIDLBuiltinKind.INT -> content

                WebIDLBuiltinKind.STRING -> "$content.wrap()"

                WebIDLBuiltinKind.CHAR -> "$content.code.toUShort()"
            }
        }
    }
}