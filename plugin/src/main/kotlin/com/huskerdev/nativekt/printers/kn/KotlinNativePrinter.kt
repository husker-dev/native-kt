package com.huskerdev.nativekt.printers.kn

import com.huskerdev.nativekt.printers.asyncFunctionName
import com.huskerdev.nativekt.printers.globalOperators
import com.huskerdev.nativekt.printers.isString
import com.huskerdev.nativekt.printers.printFunctionHeader
import com.huskerdev.nativekt.printers.syncFunctionName
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
import java.io.File

class KotlinNativePrinter(
    idl: IdlResolver,
    target: File,
    classPath: String,
    moduleName: String,
    useCoroutines: Boolean,
    val isX32: Boolean
) {
    val cinteropPath = "cinterop.$classPath"

    init {
        val builder = StringBuilder()
        builder.append("@file:OptIn(ExperimentalForeignApi::class)\n\n")
        builder.append("package $classPath\n\n")
        builder.append("import kotlinx.cinterop.*\n")
        builder.append("\n@Throws(UnsupportedOperationException::class)\n")
        builder.append("actual fun ${syncFunctionName(moduleName)}() = Unit\n")
        builder.append("actual fun ${asyncFunctionName(moduleName)}(onReady: () -> Unit) = onReady()\n")
        if(useCoroutines)
            builder.append("actual suspend fun ${asyncFunctionName(moduleName)}() = Unit\n")

        idl.globalOperators().forEach { printFunction(builder, it) }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function, isActual = true)
        append(" = \n\t")

        val func = "$cinteropPath.${function.name}"
        append(castFromNative(function.type, "$func(${castArgs(function.args)})"))

        append("\n")
    }

    private fun castArgs(args: List<ResolvedIdlField.Argument>): String {
        return args.flatMap { arg ->
            if(arg.type.isString())
                listOf(castToNative(arg.type, arg.name), if(isX32) "${arg.name}.length.toUInt()" else "${arg.name}.length.toULong()")
            else
                listOf(castToNative(arg.type, arg.name))
        }.joinToString()
    }

    private fun castFromNative(type: ResolvedIdlType, content: String): String = when(type) {
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
                WebIDLBuiltinKind.LONG,
                WebIDLBuiltinKind.UNSIGNED_LONG,
                WebIDLBuiltinKind.INT -> content

                WebIDLBuiltinKind.STRING -> "$content.toKString()"
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
                WebIDLBuiltinKind.LONG,
                WebIDLBuiltinKind.UNSIGNED_LONG,
                WebIDLBuiltinKind.INT,
                WebIDLBuiltinKind.STRING -> content
            }
        }
    }
}