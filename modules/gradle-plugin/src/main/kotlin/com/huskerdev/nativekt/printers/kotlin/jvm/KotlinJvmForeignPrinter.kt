package com.huskerdev.nativekt.printers.kotlin.jvm

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized

class KotlinJvmForeignPrinter(
    idl: IdlResolver,
    builder: StringBuilder,
    val classPath: String,
    val moduleName: String,
    val name: String = "Foreign",
    parentClass: String? = null,
    val indent: String = ""
) {
    init {
        builder.append("${indent}private class $name(libraryPath: String)")
        if(parentClass != null)
            builder.append(": $parentClass")
        builder.append(" {\n\n")

        if(idl.callbacks.isNotEmpty()) {
            builder.append("\tcompanion object {\n")

            idl.dictionaries.values.forEach { printDictionaryLayout(builder, it) }
            idl.dictionaries.values.forEach { printDictionaryCasts(builder, it) }
            idl.callbacks.values.forEach { printCallbackInvoke(builder, it) }

            builder.append("\n$indent\t\tprivate val lookup = MethodHandles.lookup()\n")
            idl.callbacks.values.forEach { printCallbackUpcall(builder, it) }

            builder.append("\t}\n\n")
        }

        builder.append($$"""
            private val handle = SymbolLookup.libraryLookup(java.nio.file.Paths.get(libraryPath), Arena.global())
            
            private val addressKArrayFree = address(handle, "$${mangle("karray_free")}")
            private val handleKArrayFree = handle(addressKArrayFree, false, null, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            
        """.replaceIndent("\t"))

        buildList {
            addAll(listOf(
                "KString",
                "KCharArray", "KBooleanArray",
                "KByteArray", "KUByteArray",
                "KShortArray", "KUShortArray",
                "KIntArray", "KUIntArray",
                "KLongArray", "KULongArray",
                "KFloatArray", "KDoubleArray"
            ))
            idl.dictionaries.values.mapTo(this) { it.name }
        }.forEach {
            builder.append("\n\tprivate val address${it.capitalized()}Free = address(handle, \"${mangle("${it}_free")}\")")
            builder.append("\n\tprivate val handle${it.capitalized()}Free = handle(address${it.capitalized()}Free, false, null, ValueLayout.ADDRESS)\n")
        }
        builder.append("\n")

        idl.globalOperators().forEach {
            printFunctionHandle(builder, it)
        }

        builder.append("""
            
            override fun _address(name: String): Long =
                address(handle, name).address()
            
        """.replaceIndent("$indent\t"))

        idl.globalOperators().forEach {
            printFunctionCall(builder, it)
        }
        builder.append("${indent}}")
    }

    fun mangle(name: String) =
        mangle(classPath, moduleName, name)

    private fun printDictionaryLayout(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        append("$indent\t\tprivate val layout${dictionary.name.upperCamelCase()} = CStructLayout(")
        buildList {
            dictionary.allFields().mapTo(this) { it.type.toForeignType() }
            add("ValueLayout.JAVA_BYTE")
        }.joinTo(builder)
        append(")\n")
    }

    private fun printDictionaryCasts(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val name = dictionary.name.upperCamelCase()
        val layout = "layout$name"
        val fields = dictionary.allFields()

        // to native (heap)
        append("\n\t\tprivate fun toNativeDictionary$name(of: $name?) = ")
        append("of?.run { malloc($layout.size).apply {")

        fields.forEachIndexed { i, it ->
            val value = castToNative(it.type, "of.${it.name.camelCase()}", useArena = false)
            val set = "set(${it.type.toForeignType()}, $layout[$i], $value)"
            append("\n\t\t\t$set")
        }
        append("\n\t\t\tset(ValueLayout.JAVA_BYTE, $layout[${fields.size}], FLAG_RELEASABLE)")

        append("\n\t\t} } ?: MemorySegment.NULL\n")

        // to native (arena)
        append("\n\t\tprivate fun toNativeDictionary${name}OnArena(arena: Arena, of: $name?) = ")
        append("of?.run { arena.allocate($layout.size).apply {")

        fields.forEachIndexed { i, it ->
            val value = castToNative(it.type, "of.${it.name.camelCase()}", useArena = true)
            val set = "set(${it.type.toForeignType()}, $layout[$i], $value)"
            append("\n\t\t\t$set")
        }
        append("\n\t\t\tset(ValueLayout.JAVA_BYTE, $layout[${fields.size}], 0)")

        append("\n\t\t} } ?: MemorySegment.NULL\n")

        // to kotlin
        append("\n\t\tprivate fun toKotlinDictionary$name(of: MemorySegment)")
        append(" = if(of.address() != 0L)")
        append("\n\t\t\tof.reinterpret($layout.size).run { $name(")

        fields.forEachIndexed { i, it ->
            val get = "get(${it.type.toForeignType()}, $layout[$i])"
            val value = castFromNative(it.type, get)
            append("\n\t\t\t\t$value,")
        }
        append("\n\t\t\t) } else null\n")
    }

    private fun printFunctionHandle(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        val isCriticalAlt = function.isCritical() && function.isCriticalCapable() && (function.hasString() || function.hasArray())

        append("${indent}\tprivate val handle")
        append(function.name.upperCamelCase())
        append($$" = lookup(handle, \"$${mangle(function.name)}")
        if(isCriticalAlt)
            append("_")
        append("\", ${function.isCritical()}, ")

        val args = arrayListOf(function.type.toForeignType())
        args += function.args.flatMap {
            when {
                isCriticalAlt && it.type.isString() -> listOf("ValueLayout.ADDRESS", "ValueLayout.JAVA_INT", "ValueLayout.JAVA_INT")
                isCriticalAlt && it.type.isArray() -> listOf("ValueLayout.ADDRESS", "ValueLayout.JAVA_INT")
                it.type.isUByte() || it.type.isUShort() -> listOf("ValueLayout.JAVA_INT")
                else -> listOf(it.type.toForeignType())
            }
        }
        args.joinTo(builder)
        append(")\n")
    }

    private fun printFunctionCall(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {

        val useArena = function.args.any {
            (it.type.isString() && !function.isCritical()) ||
            (it.type.isArray() && !function.isCritical()) ||
            it.type.isDictionary() ||
            it.type.isCallback()
        }

        val transforms = function.args.mapNotNull {
            val nullable = if(it.type.isNullable) "?" else ""
            when {
                function.isCritical() && it.type.isString() ->
                    "\n${indent}\t\tval _bytes_${it.name.camelCase()} = ${it.name.camelCase()}$nullable.toByteArray()"
                else -> null
            }
        }

        val args = function.args.flatMap {
            val name = it.name.camelCase()
            val nullable = if(it.type.isNullable) "?" else ""
            val elseNum = if(it.type.isNullable) " ?: -1" else ""
            when {
                function.isCritical() && it.type.isString() -> listOf(
                    "toNativeKByteArrayDirect(_bytes_$name)",
                    "$name$nullable.length$elseNum",
                    "_bytes_$name$nullable.size$elseNum"
                )
                function.isCritical() && it.type.isEnumArray() -> listOf(
                    "toNativeEnumArrayDirect($name)",
                    "$name$nullable.size$elseNum"
                )
                function.isCritical() && it.type.isArray() -> listOf(
                    "toNative${it.type.toCType(ptr = false, ignoreUnsigned = true)}Direct(${castToSigned(it.type, name)})",
                    "$name$nullable.size$elseNum"
                )
                else -> listOf(castToNative(it.type, name, useArena = useArena, smallTypesAsInt = true))
            }
        }.joinToString()

        val deallocFunc = if(function.type.isReleasable())
            freeFuncFor(function.type, "_result_native")
        else null

        val call = "(handle${function.name.upperCamelCase()}.invokeExact($args) as ${function.type.toKotlinForeignType()})"

        // === Print ===

        append("\n${indent}\t")
        printFunctionHeader(builder, function, isOverride = true)

        when {
            useArena -> append(" = Arena.ofConfined().use { arena ->")
            deallocFunc != null || transforms.isNotEmpty() -> append(" {")
            else -> append(" = ")
        }

        transforms.joinTo(builder, separator = "")
        append("\n$indent\t\t")

        if(deallocFunc != null) {
            append("val _result_native = $call")
            append("\n$indent\t\t")
            append("val _result_jvm = ${castFromNative(function.type, "_result_native")}")
            append("\n$indent\t\t")
            append(deallocFunc)

            if(function.type !is ResolvedIdlType.Void) {
                append("\n$indent\t\t")
                if(useArena) append("_result_jvm")
                else append("return _result_jvm")
            }
        } else {
            if(!useArena && transforms.isNotEmpty())
                append("return ")
            append(castFromNative(function.type, call))
        }

        if(useArena || deallocFunc != null || transforms.isNotEmpty())
            append("\n$indent\t}")
        append("\n")
    }

    private fun printCallbackInvoke(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val args = buildList {
            add("_callback: MemorySegment")
            callback.args.mapTo(this) { "${it.name.camelCase()}: ${it.type.toKotlinForeignType()}" }
        }.joinToString()

        val lambdaArgs = callback.args.joinToString { castFromNative(it.type, it.name.camelCase()) }
        val type = callback.type.toKotlinForeignType(smallUnsignedTypesAsInt = true)

        append("\n\t\t@JvmStatic fun invoke${callback.name.upperCamelCase()}($args): $type =\n\t\t\t")

        val call = "toKotlinCallback<${callback.name.upperCamelCase()}>(_callback)!!($lambdaArgs)"

        append(castToNative(callback.type, call, useArena = false, smallTypesAsInt = true))
        append("\n")
    }

    private fun printCallbackUpcall(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        append("\n\t\tprivate val upcall${callback.name.upperCamelCase()} = lookup.upcall(\n\t\t\t")

        // lookup
        append("\"invoke${callback.name.upperCamelCase()}\",\n\t\t\t")
        append("MethodType.methodType(")

        val returnType = if(callback.type is ResolvedIdlType.Void)
            "Void::class.javaPrimitiveType"
        else "${callback.type.toKotlinForeignType(smallUnsignedTypesAsInt = true)}::class.java"

        val argClasses = buildList {
            add(returnType)
            add("MemorySegment::class.java")
            callback.args.mapTo(this) { "${it.type.toKotlinForeignType()}::class.java" }
        }

        argClasses.joinTo(builder)
        append("),\n\t\t\t")

        // desc

        if(callback.type is ResolvedIdlType.Void)
            append("FunctionDescriptor.ofVoid(")
        else
            append("FunctionDescriptor.of(").append(callback.type.toForeignType(smallTypesAsInt = true)).append(", ")

        buildList {
            add("ValueLayout.ADDRESS")
            callback.args.mapTo(this) { it.type.toForeignType() }
        }.joinTo(builder)
        append(")\n\t\t)\n")
    }

    private fun freeFuncFor(
        type: ResolvedIdlType,
        content: String
    ) = when {
        type.isString() -> "handleKStringFree.invoke($content)"
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() -> "handle${type.toCType()}ArrayFree.invoke($content)"
                type.isEnum() ->
                    "handleKIntArrayFree.invoke($content)"
                else -> "handleKArrayFree.invoke($content, address${type.toCType(ptr = false)}Free)"
            }
        }
        type.isCallback() -> "ForeignUtils.callbackFree($content)"
        type.isDictionary() || type.isString() -> "handle${type.toCType(ptr = false)}Free.invoke($content)"
        else -> null
    }

    private fun castFromNative(type: ResolvedIdlType, content: String): String {
        val nullAssert = if(type.isNullable) "" else "!!"
        return when {
            type.isUnsigned() -> castToUnsigned(type, castFromNative(type.toSignedType(), content))
            type.isString() -> "toKotlinKString($content)$nullAssert"
            type.isCallback() -> "toKotlinCallback<${type.toKotlinType()}>($content)$nullAssert"
            type.isEnum() -> "${type.declaration.name}.entries[$content]"
            type.isDictionary() -> "toKotlinDictionary${type.declaration.name}($content)$nullAssert"
            type.isArray() -> type.arrayType { type ->
                when {
                    type.isPrimitive() -> "toKotlinK${type.toKotlinType(ignoreUnsigned = true)}Array($content)$nullAssert"
                    type.isEnum() -> "toKotlinEnumArray($content, ${type.toKotlinType(printNullable = false)}::class.java)$nullAssert"
                    else -> {
                        val fn = castFromNative(type, "").split("(")[0]
                        "toKotlinKArray<${type.toKotlinType()}>($content, ::$fn, ${type.toKotlinType(printNullable = false)}::class.java)$nullAssert"
                    }
                }
            }
            else -> content
        }
    }

    private fun castToNative(
        type: ResolvedIdlType,
        content: String,
        useArena: Boolean,
        smallTypesAsInt: Boolean = false
    ): String = when {
        type.isUnsigned() -> castToNative(type.toSignedType(), castToSigned(type, content, smallTypesAsInt), useArena)
        type.isEnum() -> "$content.ordinal"
        type.isString() ->
            if (useArena) "toNativeKStringOnArena(arena, $content)"
            else "toNativeKString($content)"
        type.isCallback() ->
            if (useArena) "createCallbackOnArena(arena, $content, upcall${type.declaration.name})"
            else "createCallback($content, upcall${type.declaration.name})"
        type.isDictionary() ->
            if (useArena) "toNativeDictionary${type.declaration.name}OnArena(arena, $content)"
            else "toNativeDictionary${type.declaration.name}($content)"
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() ->
                    if (useArena) "toNativeK${type.toKotlinType(ignoreUnsigned = true)}ArrayOnArena(arena, $content)"
                    else "toNativeK${type.toKotlinType(ignoreUnsigned = true)}Array($content)"
                type.isEnum() ->
                    if (useArena) "toNativeEnumArrayOnArena(arena, $content)"
                    else "toNativeEnumArray($content)"
                else -> {
                    val fn = castToNative(type, "", useArena).split("(")[0]
                    if (useArena) "toNativeKArrayOnArena(arena, $content, ::$fn)"
                    else "toNativeKArray($content, ::$fn)"
                }
            }
        }
        else -> content
    }

    private fun ResolvedIdlType.toForeignType(
        smallTypesAsInt: Boolean = false
    ): String = when {
        isVoid() -> "null"
        isChar() -> "ValueLayout.JAVA_CHAR"
        isBoolean() -> "ValueLayout.JAVA_BOOLEAN"
        isByte() -> "ValueLayout.JAVA_BYTE"
        isUByte() -> if(smallTypesAsInt) "ValueLayout.JAVA_INT" else "ValueLayout.JAVA_BYTE"
        isShort() -> "ValueLayout.JAVA_SHORT"
        isUShort() -> if(smallTypesAsInt) "ValueLayout.JAVA_INT" else "ValueLayout.JAVA_SHORT"
        isInt() || isUInt() -> "ValueLayout.JAVA_INT"
        isLong() || isULong() -> "ValueLayout.JAVA_LONG"
        isFloat() -> "ValueLayout.JAVA_FLOAT"
        isDouble() -> "ValueLayout.JAVA_DOUBLE"
        isEnum() -> "ValueLayout.JAVA_INT"
        else -> "ValueLayout.ADDRESS"
    }

    private fun ResolvedIdlType.toKotlinForeignType(
        smallUnsignedTypesAsInt: Boolean = false
    ): String {
        return if(isCallback() || isString() || isArray() || isDictionary())
            "MemorySegment"
        else toKotlinType(
            enumAsInt = true,
            ignoreUnsigned = true,
            smallUnsignedTypesAsInt = smallUnsignedTypesAsInt
        )
    }
}