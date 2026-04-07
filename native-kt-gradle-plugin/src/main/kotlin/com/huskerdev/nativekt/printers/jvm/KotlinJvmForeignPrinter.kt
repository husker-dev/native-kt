package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized
import kotlin.math.ceil
import kotlin.math.max

class KotlinJvmForeignPrinter(
    idl: IdlResolver,
    builder: StringBuilder,
    val classPath: String,
    val name: String = "Foreign",
    parentClass: String? = null,
    val indent: String = ""
) {
    init {
        builder.append("${indent}private class ")
        builder.append(name)
        builder.append("(prefix: String = \"EXPORTED_")
        builder.append(classPath.replace(".", "_"))
        builder.append("_\")")
        if(parentClass != null)
            builder.append(": $parentClass")
        builder.append(" {\n\n")

        if(idl.callbacks.isNotEmpty()) {
            builder.append("\tcompanion object {\n")

            idl.dictionaries.values.forEach { printDictionaryDesc(builder, it) }
            idl.dictionaries.values.forEach { printDictionaryCasts(builder, it) }

            idl.callbacks.values.forEach { printCallbackInvoke(builder, it) }
            idl.callbacks.values.forEach { printCallbackMethodHandle(builder, it) }
            idl.callbacks.values.forEach { printCallbackDesc(builder, it) }
            builder.append("\n")
            idl.callbacks.values.forEach { printCallbackToNative(builder, it) }

            builder.append("\t}\n\n")
        }

        idl.globalOperators().forEach {
            printFunctionHandle(builder, it)
        }
        idl.globalOperators().forEach {
            printFunctionCall(builder, it)
        }
        builder.append("${indent}}")
    }

    private fun ResolvedIdlDictionary.calcMem(): Pair<Int, Int> {
        var sum = 0.0
        var max = 0.0
        allFields().forEach {
            val cur = it.type.getAlignment().toDouble()
            sum = (cur * ceil(sum / cur)) + cur
            max = max(max, cur)
        }
        return Pair(sum.toInt(), max.toInt())
    }

    private fun printDictionaryDesc(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val (sum, max) = dictionary.calcMem()
        val padding = sum % max

        val structName = "struct${dictionary.name.capitalized()}"

        append("\n\t\tprivate val ")
        append(structName)
        append(" = MemoryLayout.structLayout(\n\t\t\t")
        dictionary.allFields().joinTo(builder, separator = ",\n\t\t\t") {
            "${it.type.toForeignType()}.withName(\"${it.name}\")"
        }
        if(padding != 0)
            append(",\n\t\t\tMemoryLayout.paddingLayout(${padding})")
        append("\n\t\t)\n\t\t")

        dictionary.allFields().joinTo(builder, separator = "\n\t\t") {
            val fieldName = "${structName}Field${it.name.capitalized()}"
            "private val $fieldName = $structName.varHandle(MemoryLayout.PathElement.groupElement(\"${it.name}\"))"
        }
        append("\n")
    }

    private fun printDictionaryCasts(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val (sum, max) = dictionary.calcMem()
        val mem = (max * ceil(sum.toDouble() / max)).toInt()

        val structName = "struct${dictionary.name.capitalized()}"

        // to native
        append("\n\t\tprivate fun toNativeDictionary")
        append(dictionary.name.capitalized())
        append("(of: ")
        append(dictionary.name)
        append(") = Arena.global().allocate(")
        append(structName)
        append(").apply {\n\t\t\t")
        dictionary.allFields().joinTo(builder, separator = "\n\t\t\t") {
            val fieldName = "${structName}Field${it.name.capitalized()}"
            "$fieldName.set(this, 0L, of.${it.name})"
        }
        append("\n\t\t}\n")

        // to jvm
        append("\n\t\tprivate fun toJvmDictionary")
        append(dictionary.name.capitalized())
        append("(of: MemorySegment, dealloc: Boolean) = of.reinterpret(${mem}).run {\n\t\t\t")
        append(dictionary.name)
        append("(\n\t\t\t\t")
        dictionary.allFields().joinTo(builder, separator = ",\n\t\t\t\t") {
            val fieldName = "${structName}Field${it.name.capitalized()}"
            "$fieldName.get(this, 0L) as ${it.type.toKotlinForeignType()}"
        }
        append("\n\t\t\t)\n\t\t}.also { if (dealloc) ForeignUtils.freeHandle.invoke(of) }\n")
    }


    private fun printFunctionHandle(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        val isCriticalAlt = function.isCritical() && function.isCriticalCapable() && (function.hasString() || function.hasArray())

        append("${indent}\tprivate val handle")
        append(function.name.capitalized())
        append($$" = ForeignUtils.lookup(\"${prefix}")
        append(function.name)
        if(isCriticalAlt)
            append("_")
        append("\", ")
        append(function.isCritical())
        append(", ")

        val args = arrayListOf(function.type.toForeignType())
        args += function.args.flatMap {
            if(isCriticalAlt && it.type.isString())
                listOf("ForeignUtils.C_ADDRESS", "ForeignUtils.C_INT")
            else listOf(it.type.toForeignType())
        }
        args.joinTo(builder)
        append(")\n")
    }

    private fun printFunctionCall(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\n${indent}\t")
        printFunctionHeader(builder, function,
            isOverride = true,
            name = "_${function.name}",
            forcePrintVoid = true
        )
        append(" = ")

        val useArena = !function.isCritical() && (
                function.type.isString() || function.type.isArray() ||
                        function.args.any { !it.type.isDictionary() && !it.type.isDictionaryArray() && (it.type.isString() || it.type.isArray() || it.isDealloc()) })

        if(useArena)
            append("ForeignArena().use { arena ->\n\t\t")
        else append("\n${indent}\t\t")

        val type = function.type.toKotlinForeignType()

        val args = arrayListOf<String>()
        if(function.type.isString() || function.type.isArray())
            args += "arena.heap as SegmentAllocator"

        args += function.args.flatMap {
            val casted = castToNative(it.type, it.name, function.isCritical(), it.isDealloc(), useArena)

            if(it.type.isString() && function.isCritical())
                listOf(casted, "${it.name}.length")
            else listOf(casted)
        }

        val call = "(handle${function.name.capitalized()}.invokeExact(${args.joinToString()}) as $type)"
        append(castFromNative(function.type, call, function.isDealloc(), function.isDeallocContent(), useArena))

        if(useArena)
            append("\n\t}")
        append("\n")
    }

    private fun printCallbackInvoke(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val args = listOf("_callback: MemorySegment") +
                callback.args.map { "${it.name}: ${it.type.toKotlinForeignType()}" }

        val lambdaArgTypes = callback.args.map { it.type.toKotlinType() }
        val lambdaArgs = callback.args.map { castFromNative(it.type, it.name, it.isDealloc(), it.isDeallocContent(), false) }

        val type = callback.type.toKotlinForeignType()

        append("\n\t\t@JvmStatic fun invoke")
        append(callback.name)
        append("(")
        args.joinTo(builder)
        append("): ")
        append(type)
        append(" =\n\t\t\t")

        // body
        val call = StringBuilder().apply {
            append("(ForeignUtils.callbacks[_callback.address()] as (")
            lambdaArgTypes.joinTo(this)
            append(") -> ")
            append(callback.type.toKotlinType())
            append(")(")
            lambdaArgs.joinTo(this)
            append(")")
        }

        append(castToNative(callback.type, call.toString(), critical = false, dealloc = false, useArena = false))
        append("\n")
    }

    private fun printCallbackMethodHandle(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        append("\n\t\tprivate val methodHandle")
        append(callback.name)
        append(" = MethodHandles.lookup().findStatic(\n\t\t\t")
        append(name)
        append("::class.java,\n\t\t\t")
        append("\"invoke")
        append(callback.name)
        append("\",\n\t\t\t")
        append("MethodType.methodType(")

        val returnType = if(callback.type is ResolvedIdlType.Void)
            "Void::class.javaPrimitiveType"
        else "${callback.type.toKotlinForeignType()}::class.java"

        val argClasses = listOf(returnType, "MemorySegment::class.java") +
                callback.args.map { "${it.type.toKotlinForeignType()}::class.java" }

        argClasses.joinTo(builder)
        append(")\n\t\t)\n")
    }

    private fun printCallbackDesc(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        append("\n\t\tprivate val methodDesc")
        append(callback.name)
        if(callback.type is ResolvedIdlType.Void)
            append(" = FunctionDescriptor.ofVoid(")
        else
            append(" = FunctionDescriptor.of(").append(callback.type.toForeignType()).append(", ")

        val args = listOf("ValueLayout.ADDRESS") +
                callback.args.map { it.type.toForeignType() }

        args.joinTo(builder)
        append(")")
    }

    private fun printCallbackToNative(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        append("\n\t\tfun ")
        append("toNativeCallback")
        append(callback.name)
        append("(c: ")
        append(callback.name)
        append("): MemorySegment =\n\t\t\t")
        append("ForeignUtils.createCallback(c, methodHandle")
        append(callback.name)
        append(", methodDesc")
        append(callback.name)
        append(")\n")
    }

    private fun castFromNative(type: ResolvedIdlType, content: String, dealloc: Boolean, deallocContent: Boolean, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(type.declaration) {
            is BuiltinIdlDeclaration -> when((type.declaration as BuiltinIdlDeclaration).kind) {
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "arena.toJvmString($content, $dealloc)"
                    else "ForeignUtils.toJvmString($content, $dealloc)"
                WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                    when (declaration) {
                        is BuiltinIdlDeclaration -> {
                            val name = declaration.kind.simpleName()
                            if (useArena) "arena.toJvm${name}Array($content, $dealloc)"
                            else "ForeignUtils.toJvm${name}Array($content, $dealloc)"
                        }
                        is ResolvedIdlEnum -> {
                            if (useArena) "arena.toJvmEnumArray($content, $dealloc, ${declaration.name}::class.java)"
                            else "ForeignUtils.toJvmEnumArray($content, $dealloc, ${declaration.name}::class.java)"
                        }
                        is ResolvedIdlDictionary -> "ForeignUtils.toJvmArray($content, ::toJvmDictionary${declaration.name}, ${declaration.name}::class.java, $dealloc, $deallocContent)"
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlCallbackFunction ->
                if(useArena) "arena.toJvmCallback<${type.declaration.name}>($content, $dealloc)"
                else "ForeignUtils.toJvmCallback<${type.declaration.name}>($content, $dealloc)"
            is ResolvedIdlEnum -> "${type.declaration.name}.entries[$content]"
            is ResolvedIdlDictionary -> "toJvmDictionary${type.declaration.name}(${content}, $dealloc)"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }

    private fun castToNative(type: ResolvedIdlType, content: String, critical: Boolean, dealloc: Boolean, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(type.declaration) {
            is BuiltinIdlDeclaration -> when((type.declaration as BuiltinIdlDeclaration).kind) {
                WebIDLBuiltinKind.STRING ->
                    if(critical) "ForeignUtils.toNativeHeapString($content)"
                    else if(useArena) "arena.toNativeString($content)"
                    else "ForeignUtils.toNativeString($content)"
                WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                    when (declaration) {
                        is BuiltinIdlDeclaration -> {
                            val name = declaration.kind.simpleName()
                            if (useArena) "arena.toNative${name}Array($content)"
                            else "ForeignUtils.toNative${name}Array($content)"
                        }
                        is ResolvedIdlEnum -> {
                            if (useArena) "arena.toNativeEnumArray($content)"
                            else "ForeignUtils.toNativeEnumArray($content)"
                        }
                        is ResolvedIdlDictionary -> "ForeignUtils.toNativeArray($content, ::toNativeDictionary${declaration.name})"
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlCallbackFunction ->
                if(dealloc) "arena.callback(toNativeCallback${type.declaration.name}($content))"
                else "toNativeCallback${type.declaration.name}($content)"
            is ResolvedIdlEnum -> "$content.ordinal"
            is ResolvedIdlDictionary -> "toNativeDictionary${type.declaration.name}(${content})"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }

    fun ResolvedIdlType.toForeignType(): String = when(this) {
        is ResolvedIdlType.Union -> throw UnsupportedOperationException("Union type are not unsupported")
        is ResolvedIdlType.Void -> "null"
        is ResolvedIdlType.Default -> when(declaration) {
            is BuiltinIdlDeclaration -> when(val a = (declaration as BuiltinIdlDeclaration).kind) {
                WebIDLBuiltinKind.CHAR -> "ForeignUtils.C_CHAR"
                WebIDLBuiltinKind.BOOLEAN -> "ForeignUtils.C_BOOLEAN"
                WebIDLBuiltinKind.BYTE -> "ForeignUtils.C_BYTE"
                WebIDLBuiltinKind.SHORT -> "ForeignUtils.C_SHORT"
                WebIDLBuiltinKind.INT -> "ForeignUtils.C_INT"
                WebIDLBuiltinKind.LONG -> "ForeignUtils.C_LONG"
                WebIDLBuiltinKind.FLOAT -> "ForeignUtils.C_FLOAT"
                WebIDLBuiltinKind.DOUBLE -> "ForeignUtils.C_DOUBLE"
                WebIDLBuiltinKind.STRING -> "ForeignUtils.STRING_STRUCT"
                WebIDLBuiltinKind.LIST -> "ForeignUtils.ARRAY_STRUCT"
                else -> throw UnsupportedOperationException(a.toString())
            }
            is ResolvedIdlEnum -> "ForeignUtils.C_INT"
            else -> "ForeignUtils.C_ADDRESS"
        }
    }

    fun ResolvedIdlType.getAlignment(): Int = when(this) {
        is ResolvedIdlType.Union,
        is ResolvedIdlType.Void -> throw UnsupportedOperationException()
        is ResolvedIdlType.Default -> when(declaration) {
            is BuiltinIdlDeclaration -> when(val a = (declaration as BuiltinIdlDeclaration).kind) {
                WebIDLBuiltinKind.CHAR -> 2
                WebIDLBuiltinKind.BOOLEAN -> 1
                WebIDLBuiltinKind.BYTE -> 1
                WebIDLBuiltinKind.SHORT -> 2
                WebIDLBuiltinKind.INT -> 4
                WebIDLBuiltinKind.LONG -> 8
                WebIDLBuiltinKind.FLOAT -> 4
                WebIDLBuiltinKind.DOUBLE -> 8
                WebIDLBuiltinKind.STRING -> 8
                WebIDLBuiltinKind.LIST -> 8
                else -> throw UnsupportedOperationException(a.toString())
            }
            is ResolvedIdlEnum -> 4
            else -> 8
        }
    }
}