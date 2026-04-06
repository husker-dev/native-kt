package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.*
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
                callbackAsAny = true,
                enumAsInt = true,
                dictionaryAsAny = true
            )
            builder.append("\n")
        }

        idl.callbacks.values.forEach { callback ->
            val args = listOf("_obj: Any") +
                    callback.args.map { "${it.name}: ${it.type.toKotlinType(callbackAsAny = true, enumAsInt = true, dictionaryAsAny = true)}" }

            builder.append("\n\t\t@Suppress(\"unchecked_cast\")\n")
            builder.append("\t\t@JvmStatic fun callback")
            builder.append(callback.name)
            builder.append("(${args.joinToString()}): ")
            builder.append(callback.type.toKotlinType(callbackAsAny = true, enumAsInt = true, dictionaryAsAny = true))
            builder.append(" =\n\t\t\t")

            val callArgs = callback.args.joinToString { jniCastToJvm(it.type, it.name) }
            val modelArgs = callback.args.joinToString { it.type.toKotlinType() }
            val model = "($modelArgs) -> ${callback.type.toKotlinType()}"

            val call = "(_obj as $model)($callArgs)"

            builder.append(jniCastToNative(callback.type, call))
            builder.append("\n")
        }
        builder.append("${indent}\t}\n")

        // Instance methods
        if(instanceMethods) {
            idl.globalOperators().forEach { function ->
                builder.append("\n${indent}\t")
                printFunctionHeader(
                    builder, function,
                    isExternal = false, isOverride = parentClass != null,
                    name = "_${function.name}",
                    forcePrintVoid = true
                )
                builder.append(" = \n${indent}\t\t")

                val args = function.args.joinToString { jniCastToNative(it.type, it.name) }
                val call = "${function.name}(${args})"

                builder.append(jniCastToJvm(function.type, call))
                builder.append("\n")
            }
        }
        builder.append("${indent}}")
    }
}

internal fun jniCastToNative(type: ResolvedIdlType, content: String) = when {
    type.isEnum() -> "$content.ordinal"
    type.isEnumArray() -> "$content.run { IntArray(size) { get(it).ordinal } }"
    type.isDictionaryArray() -> "$content as Array<Any>"
    else -> content
}

internal fun jniCastToJvm(type: ResolvedIdlType, content: String) = when(type) {
    is ResolvedIdlType.Default -> when {
        type.isDictionary() || type.isCallback() -> "$content as ${type.declaration.name}"
        type.isDictionaryArray() -> "$content as Array<${(type.parameters[0] as ResolvedIdlType.Default).declaration.name}>"
        type.isEnum() -> "${type.declaration.name}.entries[$content]"
        type.isEnumArray() -> {
            val param = type.parameters[0] as ResolvedIdlType.Default
            "$content.run { Array(size) { ${param.declaration.name}.entries[get(it)] } }"
        }
        else -> content
    }
    else -> content
}