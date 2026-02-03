package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.webidl.resolver.IdlResolver

class KotlinJvmJniPrinter(
    idl: IdlResolver,
    builder: StringBuilder,
    parentClass: String? = null,
    instanceMethods: Boolean = false
) {
    init {
        builder.append("private class JNI")
        if(parentClass != null)
            builder.append(": $parentClass")
        builder.append(" {\n")

        // Static functions
        builder.append("\tcompanion object {\n")
        idl.globalOperators().forEach { function ->
            builder.append("\t\t@JvmStatic ")
            printFunctionHeader(
                builder, function,
                isExternal = true
            )
            builder.append("\n")
        }
        builder.append("\t}\n")

        // Instance methods
        if(instanceMethods) {
            builder.append("\n")
            idl.globalOperators().forEach { function ->
                builder.append("\t")
                printFunctionHeader(
                    builder, function,
                    isExternal = false,
                    isOverride = parentClass != null,
                    name = "_${function.name}"
                )
                builder.append(" = \n\t\t")
                builder.append(function.name)
                builder.append("(")
                function.args.joinTo(builder) { it.name }
                builder.append(")\n")
            }
        }
        builder.append("}")
    }
}