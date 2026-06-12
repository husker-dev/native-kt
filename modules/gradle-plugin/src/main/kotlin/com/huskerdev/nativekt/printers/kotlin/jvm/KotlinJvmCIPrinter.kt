package com.huskerdev.nativekt.printers.kotlin.jvm

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
                    private class $name(libraryPath: String): $parentClass {
                    
                """.trimIndent())
            } else {
                append("""
                    private class $name(
                        libraryPath: String,
                        parent: $parentClass
                    ): $parentClass by parent {
                    
                """.trimIndent())
            }

            append("""
                private val emptyCharArray = charArrayOf()
                private val emptyBooleanArray = booleanArrayOf()
                private val emptyByteArray = byteArrayOf()
                private val emptyShortArray = shortArrayOf()
                private val emptyIntArray = intArrayOf()
                private val emptyLongArray = longArrayOf()
                private val emptyFloatArray = floatArrayOf()
                private val emptyDoubleArray = doubleArrayOf()
                
            """.replaceIndent("\t"))
            append("\n")

            if(operators.isNotEmpty()) {
                append("\tcompanion object {")

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

                if(implementFields)
                    append("\n\t\tSystem.load(libraryPath)")

                operators.forEach {
                    printFunctionBinding(builder, it)
                }

                append("\n\t}\n")

                append($$"""
                    
                    private fun linkFunction(name: String, alt: Boolean, vararg types: Class<*>) {
                        JVMCIUtils.linkNativeCall(
                            $$name::class.java.getDeclaredMethod("_$name", *types),
                            _address("EXPORTED_$${classPath.replace(".", "_")}_$name${if (alt) "_" else ""}")
                        )
                    }
                    
                """.replaceIndent("\t"))

                if (implementFields) {
                    append("""
                        
                        override fun _address(name: String): Long =
                            NativeKtUtils.findAddress(name)
                        
                    """.replaceIndent("\t"))

                    val nonCritical = idl.globalOperators()
                        .filter { !it.isCritical() }

                    if (nonCritical.isNotEmpty()) {
                        val list = nonCritical.joinToString(separator = "") { "\n\t- ${it.name}" }
                        throw UnsupportedOperationException("JVMCI can not operate with non-critical operations: $list")
                    }
                }

                operators.forEach {
                    printFunctionCall(builder, it)
                }
            }
            append("\n}\n")
        }
    }

    private fun printFunctionBinding(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        val args = listOf("\"${function.name}\"", function.hasString() || function.hasArray()) +
                function.args.flatMap {
                    val clazz = "${it.type.toKotlinType(stringAsBytes = true, enumAsInt = true, printNullable = false)}::class.java"
                    when {
                        it.type.isString() -> listOf(clazz, "Int::class.java", "Int::class.java")
                        it.type.isArray() -> listOf(clazz, "Int::class.java")
                        else -> listOf(clazz)
                    }
                }

        append("\n\t\tlinkFunction(${args.joinToString()})")
    }

    private fun printFunctionCall(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\n\t")
        printFunctionHeader(builder, function,
            isOverride = true
        )
        append(" {")

        function.args.forEach {
            val nullable = if(it.type.isNullable) "?" else ""
            if(it.type.isString()) {
                append("\n\t\tval _bytes_${it.name} = ${it.name}$nullable.toByteArray()")
                if(it.type.isNullable) append(" ?: emptyByteArray")
            }
            if(it.type.isEnumArray()) {
                append("\n\t\tval _ints_${it.name} = ${it.name}$nullable.run { IntArray(size) { this[it].ordinal } }")
                if(it.type.isNullable) append(" ?: emptyIntArray")
            }
        }

        append("\n\t\t")
        if(function.type !is ResolvedIdlType.Void)
            append("return ")

        val args = function.args.joinToString {
            toNativeCriticalType(it.type, it.name)
        }
        val call = "_${function.name}(${args})"
        append(toKotlinCriticalType(function.type, call))
        append("\n\t}\n")
    }
}

internal fun toNativeCriticalType(type: ResolvedIdlType, name: String): String {
    val nullable = if(type.isNullable) "?" else ""
    val elseNum = if(type.isNullable) " ?: -1" else ""
    return when {
        type.isString() -> "_bytes_$name, $name$nullable.length$elseNum, _bytes_$name.size"
        type.isEnum() -> "$name.ordinal"
        type.isEnumArray() -> "_ints_$name, $name$nullable.size$elseNum"
        type.isArray() -> {
            if(type.isNullable)
                "$name ?: empty${type.toKotlinType(printNullable = false)}, $name?.size ?: -1"
            else "$name, $name.size"
        }
        else -> name
    }
}

internal fun toKotlinCriticalType(type: ResolvedIdlType, name: String) = when(type) {
    is ResolvedIdlType.Default -> when (val decl = type.declaration){
        is ResolvedIdlEnum -> "${decl.name}.entries[${name}]"
        else -> name
    }
    else -> name
}