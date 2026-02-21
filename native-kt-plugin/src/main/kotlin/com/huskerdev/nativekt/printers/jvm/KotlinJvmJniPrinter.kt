package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.isCallback
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.toKotlinType
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlType

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

        idl.globalOperators().forEach { function ->
            builder.append("${indent}\t\t@JvmStatic ")
            printFunctionHeader(
                builder, function,
                isExternal = true,
                callbackAsAny = true
            )
            builder.append("\n")
        }

        idl.callbacks.values.forEach { callback ->
            val args = listOf("obj: Any") +
                    callback.args.map { "${it.name}: ${it.type.toKotlinType()}" }

            builder.append("\n\t\t@Suppress(\"unchecked_cast\")\n")
            builder.append("\t\t@JvmStatic fun callback")
            builder.append(callback.name)
            builder.append("(${args.joinToString()}) = \n")
            builder.append("\t\t\t(obj as (")
            callback.args.joinTo(builder) { it.type.toKotlinType() }
            builder.append(") -> ")
            builder.append(callback.type.toKotlinType())
            builder.append(")(")
            callback.args.joinTo(builder) { it.name }
            builder.append(")\n")
        }
        builder.append("${indent}\t}\n")

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
                builder.append(")")
                if(function.type.isCallback()) {
                    builder.append(" as ")
                    builder.append((function.type as ResolvedIdlType.Default).declaration.name)
                }
                builder.append("\n")
            }
        }
        builder.append("${indent}}")
    }
}