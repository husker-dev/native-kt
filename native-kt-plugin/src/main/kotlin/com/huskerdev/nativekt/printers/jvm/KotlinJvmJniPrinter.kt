package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.webidl.resolver.IdlResolver

class KotlinJvmJniPrinter(
    idl: IdlResolver,
    builder: StringBuilder,
    name: String = "JNI",
    parentClass: String? = null,
    instanceMethods: Boolean = false,
    isPrivate: Boolean = true,
    indent: String = ""
) {
    init {
        builder.append(indent)
        if(isPrivate)
            builder.append("private ")

        builder.append("class ")
        builder.append(name)
        if(parentClass != null)
            builder.append(": $parentClass")
        builder.append(" {\n")

        // Static functions
        builder.append("${indent}\tcompanion object {\n")
        builder.append("${indent}\t\t@JvmStatic external fun register()\n")

        idl.globalOperators().forEach { function ->
            builder.append("${indent}\t\t@JvmStatic ")
            printFunctionHeader(
                builder, function,
                isExternal = true
            )
            builder.append("\n")
        }
        builder.append("${indent}\t}\n")

        builder.append("""
            
            init {
                register()
            }
            
        """.replaceIndent(indent + "\t"))

        // Instance methods
        if(instanceMethods) {
            idl.globalOperators().forEach { function ->
                builder.append("\n${indent}\t")
                printFunctionHeader(
                    builder, function,
                    isExternal = false,
                    isOverride = parentClass != null,
                    name = "_${function.name}",
                    forcePrintVoid = true
                )
                builder.append(" = \n${indent}\t\t")
                builder.append(function.name)
                builder.append("(")
                function.args.joinTo(builder) { it.name }
                builder.append(")\n")
            }
        }
        builder.append("${indent}}")
    }
}