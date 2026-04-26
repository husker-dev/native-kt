package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.webidl.resolver.IdlResolver

class KotlinJvmJniPrinter(
    idl: IdlResolver,
    builder: StringBuilder,
    name: String = "JNI",
    parentClass: String? = null,
    forAndroid: Boolean,
    indent: String = ""
) {
    init {
        builder.append(indent)
        if(!forAndroid)
            builder.append("private ")

        builder.append("class ")
        builder.append(name)
        if(parentClass != null)
            builder.append(": $parentClass")
        builder.append(" {\n")

        if(forAndroid) {
            // Static functions
            builder.append("${indent}\tcompanion object {\n")
            builder.append("""
                @JvmStatic external fun JNILoad()
                init {
                    JNILoad()
                }
            """.replaceIndent("$indent\t\t"))
            builder.append("\n")

            idl.globalOperators().forEach { function ->
                builder.append("${indent}\t\t@JvmStatic ")
                printFunctionHeader(
                    builder, function,
                    isExternal = true
                )
                builder.append("\n")
            }
            builder.append("${indent}\t}\n")
        } else {
            // Instance methods
            builder.append("""
                companion object {
                    @JvmStatic external fun JNILoad()
                    init {
                        JNILoad()
                    }
                }
            """.replaceIndent("$indent\t"))
            builder.append("\n")

            idl.globalOperators().forEach { function ->
                builder.append("${indent}\t")
                printFunctionHeader(
                    builder, function,
                    isExternal = true,
                    isOverride = parentClass != null
                )
                builder.append("\n")
            }
        }
        builder.append("${indent}}")
    }
}