package com.huskerdev.nativekt.printers

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

class KotlinNativePrinter(
    idl: IdlResolver,
    target: File,
    classPath: String,
    moduleName: String
) {
    val cinteropPath = "cinterop.$classPath"

    init {
        val builder = StringBuilder()
        builder.append("@file:OptIn(ExperimentalForeignApi::class)\n\n")
        builder.append("package $classPath\n\n")
        builder.append("import kotlinx.cinterop.*\n")
        builder.append("\n")
        builder.append("actual fun loadLib${moduleName.capitalized()}() = Unit\n")
        builder.append("actual suspend fun loadLib${moduleName.capitalized()}Async() = Unit\n")

        idl.namespaces.values.forEach { printNamespace(builder, it) }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printNamespace(builder: StringBuilder, namespace: ResolvedIdlNamespace){
        namespace.operations.forEach {
            printFunction(builder, it)
        }
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\nactual fun ")
        append(function.name)
        append("(")

        function.args.forEachIndexed { index, arg ->
            append(arg.name)
            append(": ")
            append(arg.type.toKotlinType())

            if(index != function.args.lastIndex)
                append(", ")
        }
        append(")")

        if(function.type !is ResolvedIdlType.Void) {
            append(": ")
            append(function.type.toKotlinType())
        }
        append(" = ")

        val func = "$cinteropPath.${function.name}"
        append(castFromNative(function.type, "$func(${castArgs(function.args)})"))

        append("\n")
    }

    private fun castArgs(args: List<ResolvedIdlField.Argument>): String {
        return args.flatMap { arg ->
            if(arg.type.isString()) {
                listOf(castToNative(arg.type, arg.name), "${arg.name}.length.toULong()")
            } else
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
                WebIDLBuiltinKind.INT -> content

                WebIDLBuiltinKind.STRING -> "$content.cstr"
            }
        }
    }
}