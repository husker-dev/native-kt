package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*

class KotlinJvmCIPrinter(
    idl: IdlResolver,
    builder: StringBuilder,
    val classPath: String,
    name: String = "JVMCI",
    parentClass: String
) {
    init {
        builder.apply {
            val operators = idl.globalOperators()
                .filter { it.isCritical() }

            append($$"""
                private class $$name(
                	fileName: String,
                	parent: $$parentClass
                ): $$parentClass by parent {
                    companion object {
                        @JvmStatic external fun getFunctionAddress(libName: String, funcName: String): Long
                        
                        private fun linkFunction(lib: String, name: String, alt: Boolean, vararg types: Class<*>) {
                            JVMCIUtils.linkNativeCall(
                                $$name::class.java.getDeclaredMethod(name, *types),
                                getFunctionAddress(lib, "EXPORTED_$${classPath.replace(".", "_")}_$name${if (alt) "_" else ""}")
                            )
                        }
                        
            """.trimIndent())

            operators.forEach {
                append("\n\t\t@JvmStatic ")
                printFunctionHeader(builder, it, isExternal = true, stringAsBytes = true, arraysLen = true)
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

            append("\n}\n")
        }
    }

    private fun printFunctionBinding(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        val args = listOf("\"${function.name}\"", function.hasString() || function.hasArray()) +
                function.args.flatMap {
                    val clazz = "${it.type.toKotlinType(stringAsBytes = true)}::class.java"

                    if(it.type.isString() || it.type.isArray())
                        listOf(clazz, "Int::class.java")
                    else listOf(clazz)
                }

        append("\n\t\tlinkFunction(fileName, ${args.joinToString()})")
    }

    private fun printFunctionCall(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\n\t")
        printFunctionHeader(builder, function,
            isOverride = true,
            name = "_${function.name}",
            forcePrintVoid = true
        )
        append(" =\n\t\t")

        val args = function.args.joinToString {
            if(it.type.isString())
                "${it.name}.toByteArray(), ${it.name}.length"
            else if(it.type.isArray())
                "${it.name}, ${it.name}.size"
            else it.name
        }
        append("${function.name}(${args})")
        append("\n")
    }

}