package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized

class KotlinJvmForeignPrinter(
    idl: IdlResolver,
    builder: StringBuilder,
    val classPath: String,
    name: String = "Foreign",
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

        val useArena = !function.isCritical() && (function.args.any { it.type.isString() } || function.type.isString())

        if(useArena)
            append("CustomForeignArena().use { arena ->\n\t\t")
        else append("\n${indent}\t\t")

        val type = if(function.type.isString())
            "java.lang.foreign.MemorySegment"
        else function.type.toKotlinType()

        val args = function.args.joinToString { castToNative(it.type, it.name, function.isCritical()) }
        val call = "(handle${function.name.capitalized()}.invokeExact($args) as $type)"
        append(castFromNative(function.type, call, function.isDealloc()))

        if(useArena)
            append("\n\t}")
        append("\n")
    }

    private fun castFromNative(type: ResolvedIdlType, content: String, dealloc: Boolean): String {
        return if(type.isString())
            "arena.asString($content, $dealloc)"
        else content
    }

    private fun castToNative(type: ResolvedIdlType, content: String, critical: Boolean): String {
        return if(type.isString()) {
            if(critical)
                "ForeignUtils.heapStr($content)"
            else "arena.cstr($content)"
        } else content
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
                else -> declaration.name
            })
            if(parameters.isNotEmpty())
                throw UnsupportedOperationException("Parameters are not null")
        }
    }
}