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
                        @JvmStatic external fun getLibraryHandle(name: String): Long
                        @JvmStatic external fun getFunctionAddress(libHandle: Long, funcName: String): Long
                        @JvmStatic external fun freeLibHandle(libHandle: Long)
                        
                        private fun linkFunction(lib: Long, name: String, vararg types: Class<*>) {
                            JVMCIUtils.linkNativeCall(
                                $$name::class.java.getDeclaredMethod(name, *types),
                                getFunctionAddress(lib, "EXPORTED_$${classPath.replace(".", "_")}_$name")
                            )
                        }
                        
            """.trimIndent())

            operators.forEach {
                append("\n\t\t@JvmStatic ")
                printFunctionHeader(builder, it, isExternal = true, stringAsBytes = true)
            }
            append("\n\t}\n\n")
            append($$"""
                init {
                    val lib = getLibraryHandle(fileName)
            """.replaceIndent("\t"))

            operators.forEach {
                printFunctionBinding(builder, it)
            }

            append("\n\t\tfreeLibHandle(lib)\n\t}\n")

            operators.forEach {
                printFunctionCall(builder, it)
            }

            append("\n}\n")
        }
    }

    private fun printFunctionBinding(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        val args = listOf("\"${function.name}\"") +
                function.args.map {
                    "${it.type.toKotlinType(stringAsBytes = true)}::class.java"
                }

        append("\n\t\tlinkFunction(lib, ${args.joinToString()})")
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
                "${it.name}.toByteArray()"
            else it.name
        }
        append("${function.name}(${args})")
        append("\n")
    }

}