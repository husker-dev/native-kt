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
        append("${indent}\tprivate val handle")
        append(function.name.capitalized())
        append($$" = ForeignUtils.lookup(\"${prefix}")
        append(function.name)
        append("\", ")
        append(function.isCritical())
        append(", ")

        val args = arrayListOf(function.type.toForeignType())
        args += function.args.map {
            it.type.toForeignType()
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

        val useArena = !function.isCritical() && (function.args.any { it.type.isString() } || function.args.any { it.isDealloc() })

        if(useArena)
            append("ForeignArena().use { arena ->\n\t\t")
        else append("\n${indent}\t\t")

        val type = function.type.toKotlinForeignType()

        val args = function.args.joinToString { castToNative(it.type, it.name, function.isCritical(), it.isDealloc(), useArena) }
        val call = "(handle${function.name.capitalized()}.invokeExact($args) as $type)"
        append(castFromNative(function.type, call, function.isDealloc(), useArena))

        if(useArena)
            append("\n\t}")
        append("\n")
    }

    private fun printCallbackInvoke(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val args = listOf("callback: MemorySegment") +
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
            append("(ForeignUtils.callbacks[callback.address()] as (")
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
                else -> content
            }
            is ResolvedIdlCallbackFunction ->
                if(useArena) "arena.asCallback<${type.declaration.name}>($content, $dealloc)"
                else "ForeignUtils.asCallback<${type.declaration.name}>($content, $dealloc)"
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
                else -> content
            }
            is ResolvedIdlCallbackFunction ->
                if(dealloc) "arena.callback($content.wrap${type.declaration.name}())"
                else "$content.wrap${type.declaration.name}()"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }


    fun ResolvedIdlType.toForeignType(): String = when(this) {
        is ResolvedIdlType.Union -> throw UnsupportedOperationException("Union type are not unsupported")
        is ResolvedIdlType.Void -> "null"
        is ResolvedIdlType.Default -> buildString {
            append(when(declaration) {
                is BuiltinIdlDeclaration -> when(val a = (declaration as BuiltinIdlDeclaration).kind) {
                    WebIDLBuiltinKind.CHAR -> "ForeignUtils.C_CHAR"
                    WebIDLBuiltinKind.BOOLEAN -> "ForeignUtils.C_BOOLEAN"
                    WebIDLBuiltinKind.BYTE,
                    WebIDLBuiltinKind.UNSIGNED_BYTE -> "ForeignUtils.C_BYTE"
                    WebIDLBuiltinKind.SHORT,
                    WebIDLBuiltinKind.UNSIGNED_SHORT -> "ForeignUtils.C_SHORT"
                    WebIDLBuiltinKind.INT,
                    WebIDLBuiltinKind.UNSIGNED_INT -> "ForeignUtils.C_INT"
                    WebIDLBuiltinKind.LONG,
                    WebIDLBuiltinKind.UNSIGNED_LONG -> "ForeignUtils.C_LONG"
                    WebIDLBuiltinKind.FLOAT,
                    WebIDLBuiltinKind.UNRESTRICTED_FLOAT -> "ForeignUtils.C_FLOAT"
                    WebIDLBuiltinKind.DOUBLE,
                    WebIDLBuiltinKind.UNRESTRICTED_DOUBLE -> "ForeignUtils.C_DOUBLE"
                    WebIDLBuiltinKind.STRING -> "ForeignUtils.C_ADDRESS"
                    else -> throw UnsupportedOperationException(a.toString())
                }
                else -> "ForeignUtils.C_ADDRESS"
            })
            if(parameters.isNotEmpty())
                throw UnsupportedOperationException("Parameters are not null")
        }
    }
}