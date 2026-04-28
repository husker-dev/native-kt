package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import java.io.File

class CExportedPrinter(
    idl: IdlResolver,
    target: File,
    val classPath: String,
) {
    init {
        val builder = StringBuilder()
        builder.append("""
            #include "api.h"
            
            #if defined(_WIN32) || defined(__CYGWIN__)
              #define NATIVEKT_EXPORT __declspec(dllexport)
            #else
              #define NATIVEKT_EXPORT __attribute__((visibility("default")))
            #endif
            
        """.trimIndent())

        idl.globalOperators().forEach {
            printFunction(builder, it)
            if(it.isCriticalCapable() && (it.hasString() || it.hasArray()))
                printFunctionCritical(builder, it)
        }

        target.writeText(builder.toString())
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\nNATIVEKT_EXPORT ")
        append(function.type.toCDefType())
        append(" EXPORTED_")
        append(classPath.replace(".", "_"))
        append("_")
        append(function.name)
        append("(")
        function.args.joinTo(this) {
            "${it.type.toCDefType()} __arg_${it.name}"
        }
        append(") {\n")

        // == Function call ==
        append("\t")
        if(function.type !is ResolvedIdlType.Void)
            append("return ")

        val args = function.args.joinToString { "__arg_${it.name}" }
        append("${function.name}($args);\n}\n")
    }


    private fun printFunctionCritical(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\nNATIVEKT_EXPORT ")
        append(function.type.toCDefType(enumAsInt = true))
        append(" EXPORTED_")
        append(classPath.replace(".", "_"))
        append("_")
        append(function.name)
        append("_(")
        function.args.flatMap {
            if(it.type.isString())
                listOf("const char* __arg_${it.name}", "int32_t __length_${it.name}")
            else if(it.type.isArray()) {
                val type = (it.type as ResolvedIdlType.Default).firstParam { type, _ -> type.toCDefType(enumAsInt = true) }
                listOf("$type* __arg_${it.name}", "int32_t __length_${it.name}")
            } else
                listOf("${it.type.toCDefType(enumAsInt = true)} __arg_${it.name}")
        }.joinTo(this)
        append(") {\n")

        // == Function call ==
        append("\t")
        if(function.type !is ResolvedIdlType.Void)
            append("return ")

        val args = function.args.joinToString { castToKTypeFromCritical(it.type, it.name) }
        val call = "${function.name}($args)"
        append(call)
        append(";\n}\n")
    }
}

internal fun castToKTypeFromCritical(type: ResolvedIdlType, name: String): String {
    return when(type) {
        is ResolvedIdlType.Void -> name
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.LIST -> "${type.toCDefType()}_new(__arg_${name}, __length_${name})"
                WebIDLBuiltinKind.STRING -> "KString_new(__arg_${name}, __length_${name})"
                else -> "__arg_$name"
            }
            else -> "__arg_$name"
        }
        else -> throw UnsupportedOperationException(type.toString())
    }
}