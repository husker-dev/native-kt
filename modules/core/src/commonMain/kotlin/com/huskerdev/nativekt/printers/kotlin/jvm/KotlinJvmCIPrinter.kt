package com.huskerdev.nativekt.printers.kotlin.jvm

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.printers.kotlin.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*


class KotlinJvmCIPrinter(
    private val context: NativeModuleContext,
    builder: StringBuilder,
    private val name: String = "JVMCI",
    private val parentClass: String,
    private val standalone: Boolean,
    private val indent: String = ""
) {
    private val indent1 = indent + "\t"
    private val indent2 = indent1 + "\t"

    init {
        builder.apply {
            printHeader()
            printFunctionHeaders()
            printFunctionLinking()
            printInterfaceCasts()
            printFunctions()
            append("${indent}}")
        }
    }

    private fun StringBuilder.printHeader() {
        if(standalone) {
            appendLine("""
                private class $name(libraryPath: String): $parentClass {
            """.replaceIndent(indent))
        } else {
            appendLine("""
                private class $name(parent: $parentClass): $parentClass by parent {
            """.replaceIndent(indent))
        }
    }

    private fun StringBuilder.printFunctionHeaders() {
        if(context.criticalOperations.isEmpty())
            return

        append("${indent1}companion object {")

        fun ResolvedIdlType.toCriticalKotlinType() = toKotlinType(
            stringAsBytes = true,
            enumAsInt = true,
            printNullable = false,
            ignoreUnsigned = true,
            interfaceAsLong = true,
        )

        context.criticalOperations.forEach {
            val type = it.type.toCriticalKotlinType()
            val args = it.args.joinToString { arg ->
                val name = arg.kname
                val result = "$name: ${arg.type.toCriticalKotlinType()}"
                when {
                    arg.type.isString() -> "$result, __size_$name: Int"
                    arg.type.isArray()  -> "$result, __len_$name: Int"
                    else -> result
                }
            }
            append("\n${indent2}@JvmStatic external fun _${it.kname}($args): $type")
        }
        append("\n${indent1}}\n")
    }

    private fun StringBuilder.printFunctionLinking() {
        if(context.criticalOperations.isEmpty())
            return

        append("${indent1}init {")

        if(standalone)
            append("\n${indent2}System.load(libraryPath)")

        context.criticalOperations.forEach {
            printFunctionLinking(it)
        }

        append($$"""
            
            }
            private fun _link(kName: String, cName: String, vararg types: Class<*>) {
                JVMCIUtils.linkNativeCall(
                    $$name::class.java.getDeclaredMethod(kName, *types),
                    _address(cName)
                )
            }
        """.replaceIndent(indent1))

        if (standalone) {
            append("""
                
                override fun _address(name: String): Long =
                    NativeKtUtils.findAddress(name)
            """.replaceIndent(indent1))

            val nonCritical = context.allOperations
                .filter { !it.isCritical() }

            if (nonCritical.isNotEmpty()) {
                val list = nonCritical.joinToString(separator = "") { "\n\t- ${it.name}" }
                throw UnsupportedOperationException("JVMCI can not operate with non-critical functions: $list")
            }
        }
    }

    private fun StringBuilder.printFunctionLinking(function: ResolvedIdlOperation) {
        val args = buildList {
            add("\"_${function.kname}\"")
            add("\"${function.cnameMangled(context)}\"")
            addAll(function.args.flatMap {
                val clazz = "${it.type.toKotlinType(
                    stringAsBytes = true,
                    enumAsInt = true,
                    printNullable = false,
                    ignoreUnsigned = true,
                    interfaceAsLong = true
                )}::class.java"
                when {
                    it.type.isString() -> listOf(clazz, "Int::class.java")
                    it.type.isArray() -> listOf(clazz, "Int::class.java")
                    else -> listOf(clazz)
                }
            })
        }

        append("\n\t\t_link(${args.joinToString()})")
    }

    private fun StringBuilder.printInterfaceCasts() {
        if(!context.hasInterfaceCast)
            return

        context.castedInterfaces.forEach { inter ->
            val name = inter.kname

            if(inter in context.toNativeDeclarationCasts) appendLine("""
                
                private fun toNative$name(self: $name?): Long {
                    if(self == null) return 0
                    return __interface${name}Clone(self.rcPtr)
                }
            """.replaceIndent(indent1))
            if(inter in context.toKotlinDeclarationCasts) appendLine("""
                
                private fun toKotlin$name(ptr: Long): $name? {
                    if(ptr == 0L) return null
                    return $name(Unit, __interface${name}Clone(ptr))
                        .also { _interface${name}Free(ptr) }
                }
            """.replaceIndent(indent1))
        }
    }

    private fun StringBuilder.printFunctions() {
        context.criticalOperations.forEach { function ->

            val args = function.args.joinToString {
                "${it.kname}: ${it.type.toKotlinType()}"
            }

            val castedArgs = function.args.joinToString {
                toNativeCriticalType(it.type, it.kname)
            }

            val casts = function.args.mapNotNull {
                val name = it.kname
                val nullable = if (it.type.isNullable) "?" else ""
                when {
                    it.type.isString() -> {
                        "val _bytes_$name = $name$nullable.toByteArray()" +
                            if (it.type.isNullable) " ?: JVMCIUtils.emptyByteArray" else ""
                    }
                    it.type.isEnumArray() -> {
                        "val _ints_$name = $name$nullable.run { IntArray(size) { this[it].ordinal } }" +
                            if (it.type.isNullable) " ?: JVMCIUtils.emptyIntArray" else ""
                    }
                    else -> null
                }
            }

            val call = toKotlinCriticalType(function.type, "_${function.kname}(${castedArgs})")


            append("\n${indent1}override fun ${function.kname}($args)")

            if(casts.isNotEmpty()) {
                val type = when {
                    function.type.isVoid() -> ""
                    else -> ": ${function.type.toKotlinType()}"
                }
                append("$type {")
                casts.forEach { append("\n$indent2$it") }

                append("\n$indent2")
                if(!function.type.isVoid())
                    append("return ")
                append(call)
            } else
                append(" = $call")

            if(casts.isNotEmpty())
                append("\n$indent1}")
        }
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
        type.isString() ->
            if(type.isNullable) "_bytes_$name, if($name == null) -1 else _bytes_$name.size"
            else "_bytes_$name, _bytes_$name.size"
        type.isEnum() -> "$name.ordinal"
        type.isEnumArray() -> "_ints_$name, $name$nullable.size$elseNum"
        type.isRawInterface() -> name
        type.isInterface() -> "toNative${type.declaration.kname}($name)"
        type.isArray() -> type.arrayType { arrType ->
            val casted = if(!ignoreUnsigned && arrType.isUnsigned())
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
    type.isRawInterface() -> name
    type.isInterface() -> {
        val nullAssert = if(type.isNullable) "" else "!!"
        "toKotlin${type.declaration.kname}($name)$nullAssert"
    }
    !ignoreUnsigned && type.isPrimitive() && type.isUnsigned() -> castToUnsigned(type, name)
    else -> name
}
