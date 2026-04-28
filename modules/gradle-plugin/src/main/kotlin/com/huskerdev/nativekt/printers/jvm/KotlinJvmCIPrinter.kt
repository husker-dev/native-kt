package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*

class KotlinJvmCIPrinter(
    idl: IdlResolver,
    builder: StringBuilder,
    val implementFields: Boolean,
    val classPath: String,
    name: String = "JVMCI",
    parentClass: String
) {
    init {
        builder.apply {
            val operators = idl.globalOperators()
                .filter { it.isCritical() }

            if(implementFields) {
                append("""
                    private class $name: $parentClass {
                    
                """.trimIndent())
            } else {
                append("""
                    private class $name(
                        parent: $parentClass
                    ): $parentClass by parent {
                    
                """.trimIndent())
            }

            if(operators.isNotEmpty()) {
                append($$"""
                    companion object {
                        private fun linkFunction(name: String, alt: Boolean, vararg types: Class<*>) {
                            JVMCIUtils.linkNativeCall(
                                $$name::class.java.getDeclaredMethod("_$name", *types),
                                NativeKtUtils.findAddress("EXPORTED_$${classPath.replace(".", "_")}_$name${if (alt) "_" else ""}")
                            )
                        }
                        
                """.replaceIndent("\t"))

                operators.forEach {
                    append("\n\t\t@JvmStatic ")
                    printFunctionHeader(
                        builder, it,
                        name = "_${it.name}",
                        isExternal = true,
                        stringAsBytes = true,
                        arraysLen = true,
                        enumAsInt = true
                    )
                }
                append("\n\t}\n\n")
                append("\tinit {")

                operators.forEach {
                    printFunctionBinding(builder, it)
                }

                append("\n\t}\n")

                operators.forEach {
                    printFunctionCall(builder, it)
                }

                if (implementFields) {
                    val nonCritical = idl.globalOperators()
                        .filter { !it.isCritical() }

                    if (nonCritical.isNotEmpty()) {
                        val list = nonCritical.joinToString(separator = "") { "\n\t- ${it.name}" }
                        throw UnsupportedOperationException("JVMCI can not operate with non-critical operations: $list")
                    }
                }
            }
            append("\n}\n")
        }
    }

    private fun printFunctionBinding(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        val args = listOf("\"${function.name}\"", function.hasString() || function.hasArray()) +
                function.args.flatMap {
                    val clazz = "${it.type.toKotlinType(stringAsBytes = true, enumAsInt = true)}::class.java"

                    if(it.type.isString() || it.type.isArray())
                        listOf(clazz, "Int::class.java")
                    else listOf(clazz)
                }

        append("\n\t\tlinkFunction(${args.joinToString()})")
    }

    private fun printFunctionCall(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\n\t")
        printFunctionHeader(builder, function,
            isOverride = true,
            forcePrintVoid = true
        )
        append(" =\n\t\t")

        val args = function.args.joinToString {
            toNativeCriticalType(it.type, it.name)
        }
        append("_${function.name}(${args})")
        append("\n")
    }

}

internal fun toNativeCriticalType(type: ResolvedIdlType, name: String) = when {
    type.isString() -> "${name}.toByteArray(), ${name}.length"
    type.isEnum() -> "${name}.ordinal"
    type.isEnumArray() -> "IntArray(${name}.size) { ${name}[it].ordinal }, ${name}.size"
    type.isArray() -> "${name}, ${name}.size"
    else -> name
}