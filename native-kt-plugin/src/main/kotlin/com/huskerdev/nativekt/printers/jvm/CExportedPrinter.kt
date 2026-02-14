package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.toCType
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
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

        idl.globalOperators().forEach { printFunction(builder, it) }

        target.writeText(builder.toString())
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\nJNIEXPORT ")
        append(function.type.toCType())
        append(" EXPORTED_")
        append(classPath.replace(".", "_"))
        append("_")
        append(function.name)
        append("(")
        function.args.joinTo(this) {
            "${it.type.toCType()} ${it.name}"
        }
        append(") {\n")

        // == Function call ==
        append("\t")
        if(function.type !is ResolvedIdlType.Void)
            append("return ")

        append(function.name)
        append("(")
        function.args.joinTo(builder) { it.name }
        append(");\n}\n")
    }


}