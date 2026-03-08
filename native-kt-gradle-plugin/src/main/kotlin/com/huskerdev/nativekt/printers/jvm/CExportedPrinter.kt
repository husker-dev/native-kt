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
            #include <jni.h>
            
        """.trimIndent())

        idl.globalOperators().forEach {
            printFunction(builder, it)
            if(it.isCriticalCapable() && it.hasString())
                printFunctionCritical(builder, it)
        }

        target.writeText(builder.toString())
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\nJNIEXPORT ")
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
        append("\nJNIEXPORT ")
        append(function.type.toCDefType())
        append(" EXPORTED_")
        append(classPath.replace(".", "_"))
        append("_")
        append(function.name)
        append("_(")
        function.args.flatMap {
            if(it.type.isString())
                listOf("const char* __arg_${it.name}", "int32_t __length_${it.name}")
            else
                listOf("${it.type.toCDefType()} __arg_${it.name}")
        }.joinTo(this)
        append(") {\n")

        // == Function call ==
        append("\t")
        if(function.type !is ResolvedIdlType.Void)
            append("return ")

        val args = function.args.joinToString { castToKType(it.type, it.name) }
        val call = "${function.name}($args)"
        append(castFromKType(function.type, call))
        append(";\n}\n")
    }

    internal fun castToKType(type: ResolvedIdlType, name: String): String {
        return when(type) {
            is ResolvedIdlType.Void -> name
            is ResolvedIdlType.Default -> when(val decl = type.declaration) {
                is BuiltinIdlDeclaration -> when(decl.kind) {
                    WebIDLBuiltinKind.STRING -> "makeKString(__arg_${name}, __length_${name})"
                    else -> "__arg_$name"
                }
                else -> "__arg_$name"
            }
            else -> throw UnsupportedOperationException(type.toString())
        }
    }

    internal fun castFromKType(type: ResolvedIdlType, content: String): String {
        return when(type) {
            is ResolvedIdlType.Void -> content
            is ResolvedIdlType.Default -> when(val decl = type.declaration) {
                is BuiltinIdlDeclaration -> when(decl.kind) {
                    WebIDLBuiltinKind.STRING -> "$content.data"
                    else -> content
                }
                else -> content
            }
            else -> throw UnsupportedOperationException(type.toString())
        }
    }

}