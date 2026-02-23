package com.huskerdev.nativekt.printers.js

import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.toCType
import com.huskerdev.webidl.resolver.*
import java.io.File

class CEmscriptenPrinter(
    val idl: IdlResolver,
    target: File
) {
    init {
        val builder = StringBuilder()
        builder.append("""
            #include "api.h"
            #include <stdlib.h>
            
            int64_t* __emWrapLong(int64_t value) {
                int64_t* ptr = (int64_t*)malloc(sizeof(int64_t));
                *ptr = value;
                return ptr;
            }
            
            int64_t __emUnwrapLong(int64_t* value) {
                int64_t result = *value;
                free(value);
                return result;
            }
            
        """.trimIndent())

        idl.globalOperators().forEach { printFunction(builder, it) }

        target.writeText(builder.toString())
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\n")
        append(function.type.toCType(longPtr = true))
        append(" _")
        append(function.name)
        append("(")
        function.args.joinTo(this) {
            "${it.type.toCType(longPtr = true)} ${it.name}"
        }
        append(") {\n\t")

        if(function.type !is ResolvedIdlType.Void)
            append("return ")

        // == Function call ==
        val call = "${function.name}(${function.args.joinToString { castToNative(it.type, it.name) }})"
        append(castToJS(function.type, call))
        append(";\n}\n")
    }

    private fun castToNative(type: ResolvedIdlType, content: String): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.LONG,
                WebIDLBuiltinKind.UNSIGNED_LONG -> "__emUnwrapLong($content)"
                else -> content
            }
            is ResolvedIdlCallbackFunction -> content
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }

    private fun castToJS(type: ResolvedIdlType, content: String): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.LONG,
                WebIDLBuiltinKind.UNSIGNED_LONG -> "__emWrapLong($content)"
                else -> content
            }
            is ResolvedIdlCallbackFunction -> content
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }
}