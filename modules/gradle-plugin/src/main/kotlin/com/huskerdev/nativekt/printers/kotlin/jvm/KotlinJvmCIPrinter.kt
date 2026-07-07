package com.huskerdev.nativekt.printers.kotlin.jvm

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*

class KotlinJvmCIPrinter(
    idl: IdlResolver,
    builder: StringBuilder,
    val implementFields: Boolean,
    val classPath: String,
    val moduleName: String,
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

            if(operators.isNotEmpty()) {
                append("\tcompanion object {")

                operators.forEach {
                    append("\n\t\t@JvmStatic ")
                    printFunctionHeader(
                        builder, it,
                        name = "_${it.name.camelCase()}",
                        isExternal = true,
                        stringAsBytes = true,
                        arraysLen = true,
                        enumAsInt = true,
                        ignoreUnsigned = true
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
                    
                    private fun _linkFunction(kName: String, cName: String, alt: Boolean, vararg types: Class<*>) {
                        JVMCIUtils.linkNativeCall(
                            $$name::class.java.getDeclaredMethod(kName, *types),
                            _address(cName + if (alt) "_" else "")
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
        val args = buildList {
            add("\"_${function.name.camelCase()}\"")
            add("\"${mangle(classPath, moduleName, function.name)}\"")
            add(function.hasString() || function.hasArray())
            addAll(function.args.flatMap {
                val clazz = "${it.type.toKotlinType(
                    stringAsBytes = true,
                    enumAsInt = true,
                    printNullable = false,
                    ignoreUnsigned = true
                )}::class.java"
                when {
                    it.type.isString() -> listOf(clazz, "Int::class.java", "Int::class.java")
                    it.type.isArray() -> listOf(clazz, "Int::class.java")
                    else -> listOf(clazz)
                }
            })
        }

        append("\n\t\t_linkFunction(${args.joinToString()})")
    }

    private fun printFunctionCall(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\n\t")
        printFunctionHeader(builder, function,
            isOverride = true
        )

        val casts = buildString {
            function.args.forEach {
                val name = it.name.camelCase()
                val nullable = if (it.type.isNullable) "?" else ""
                if (it.type.isString()) {
                    append("\n\t\tval _bytes_$name = $name$nullable.toByteArray()")
                    if (it.type.isNullable) append(" ?: JVMCIUtils.emptyByteArray")
                }
                if (it.type.isEnumArray()) {
                    append("\n\t\tval _ints_$name = $name$nullable.run { IntArray(size) { this[it].ordinal } }")
                    if (it.type.isNullable) append(" ?: JVMCIUtils.emptyIntArray")
                }
            }
        }

        if(casts.isEmpty())
            append(" =")
        else append(" {")

        function.args.forEach {
            val name = it.name.camelCase()
            val nullable = if(it.type.isNullable) "?" else ""
            if(it.type.isString()) {
                append("\n\t\tval _bytes_$name = $name$nullable.toByteArray()")
                if(it.type.isNullable) append(" ?: JVMCIUtils.emptyByteArray")
            }
            if(it.type.isEnumArray()) {
                append("\n\t\tval _ints_$name = $name$nullable.run { IntArray(size) { this[it].ordinal } }")
                if(it.type.isNullable) append(" ?: JVMCIUtils.emptyIntArray")
            }
        }

        append("\n\t\t")
        if(casts.isNotEmpty() && function.type !is ResolvedIdlType.Void)
            append("return ")

        val args = function.args.joinToString {
            toNativeCriticalType(it.type, it.name.camelCase())
        }
        val call = "_${function.name.camelCase()}(${args})"
        append(toKotlinCriticalType(function.type, call))

        if(casts.isNotEmpty())
            append("\n\t}")
        append("\n")
    }
}

internal fun toNativeCriticalType(
    type: ResolvedIdlType,
    name: String,
    ignoreUnsigned: Boolean = false
): String {
    val nullable = if(type.isNullable) "?" else ""
    val elseNum = if(type.isNullable) " ?: -1" else ""
    return when {
        type.isString() -> "_bytes_$name, $name$nullable.length$elseNum, _bytes_$name.size"
        type.isEnum() -> "$name.ordinal"
        type.isEnumArray() -> "_ints_$name, $name$nullable.size$elseNum"
        type.isArray() -> {
            val casted = if(!ignoreUnsigned && type.isUnsigned())
                castToSigned(type, name)
            else name

            if(type.isNullable)
                "$casted ?: JVMCIUtils.empty${type.toKotlinType(printNullable = false, ignoreUnsigned = true)}, $name?.size ?: -1"
            else "$casted, $name.size"
        }
        !ignoreUnsigned && type.isUnsigned() -> castToSigned(type, name)
        else -> name
    }
}

internal fun toKotlinCriticalType(
    type: ResolvedIdlType,
    name: String,
    ignoreUnsigned: Boolean = false
) = when {
    type.isEnum() -> "${type.declaration.name}.entries[${name}]"
    !ignoreUnsigned && type.isUnsigned() -> castToUnsigned(type, name)
    else -> name
}