package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized

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

            idl.callbacks.values.forEach { printCallbackInvoke(builder, it) }
            idl.callbacks.values.forEach { printCallbackMethodHandle(builder, it) }
            idl.callbacks.values.forEach { printCallbackDesc(builder, it) }
            builder.append("\n")
            idl.callbacks.values.forEach { printCallbackWrap(builder, it) }

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
                        function.args.any { it.type.isString() || it.type.isArray() || it.isDealloc() })

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
        append(castFromNative(function.type, call, function.isDealloc(), useArena))

        if(useArena)
            append("\n\t}")
        append("\n")
    }

    private fun printCallbackInvoke(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val args = listOf("_callback: MemorySegment") +
                callback.args.map { "${it.name}: ${it.type.toKotlinForeignType()}" }

        val lambdaArgTypes = callback.args.map { it.type.toKotlinType() }
        val lambdaArgs = callback.args.map { castFromNative(it.type, it.name, it.isDealloc(), false) }

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

    private fun printCallbackWrap(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        append("\n\t\tfun ")
        append(callback.name)
        append(".wrap")
        append(callback.name)
        append("(): MemorySegment =\n\t\t\t")
        append("ForeignUtils.createCallback(this, methodHandle")
        append(callback.name)
        append(", methodDesc")
        append(callback.name)
        append(")\n")
    }

    private fun castFromNative(type: ResolvedIdlType, content: String, dealloc: Boolean, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(type.declaration) {
            is BuiltinIdlDeclaration -> when((type.declaration as BuiltinIdlDeclaration).kind) {
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "arena.asString($content, $dealloc)"
                    else "ForeignUtils.asString($content, $dealloc)"
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
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlCallbackFunction ->
                if(useArena) "arena.asCallback<${type.declaration.name}>($content, $dealloc)"
                else "ForeignUtils.asCallback<${type.declaration.name}>($content, $dealloc)"
            is ResolvedIdlEnum -> "${type.declaration.name}.entries[$content]"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }

    private fun castToNative(type: ResolvedIdlType, content: String, critical: Boolean, dealloc: Boolean, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(type.declaration) {
            is BuiltinIdlDeclaration -> when((type.declaration as BuiltinIdlDeclaration).kind) {
                WebIDLBuiltinKind.STRING ->
                    if(critical) "ForeignUtils.heapStr($content)"
                    else if(useArena) "arena.cstr($content)"
                    else "ForeignUtils.cstr($content)"
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
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlCallbackFunction ->
                if(dealloc) "arena.callback($content.wrap${type.declaration.name}())"
                else "$content.wrap${type.declaration.name}()"
            is ResolvedIdlEnum -> "$content.ordinal"
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
}