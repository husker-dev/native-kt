package com.huskerdev.nativekt.printers

import com.huskerdev.webidl.resolver.BuiltinIdlDeclaration
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind

fun ResolvedIdlType.toKotlinType(): String = when(this) {
    is ResolvedIdlType.Union -> throw UnsupportedOperationException("Union type are not unsupported")
    is ResolvedIdlType.Void -> "Unit"
    is ResolvedIdlType.Default -> buildString {
        append(when(declaration) {
            is BuiltinIdlDeclaration -> when((declaration as BuiltinIdlDeclaration).kind) {
                WebIDLBuiltinKind.BOOLEAN -> "Boolean"
                WebIDLBuiltinKind.BYTE -> "Byte"
                WebIDLBuiltinKind.UNSIGNED_BYTE -> "UByte"
                WebIDLBuiltinKind.SHORT -> "Short"
                WebIDLBuiltinKind.UNSIGNED_SHORT -> "UShort"
                WebIDLBuiltinKind.INT -> "Int"
                WebIDLBuiltinKind.UNSIGNED_INT -> "UInt"
                WebIDLBuiltinKind.LONG -> "Long"
                WebIDLBuiltinKind.UNSIGNED_LONG -> "ULong"
                WebIDLBuiltinKind.UNRESTRICTED_FLOAT -> "Float"
                WebIDLBuiltinKind.UNRESTRICTED_DOUBLE -> "Double"
                WebIDLBuiltinKind.STRING -> "String"
                else -> throw UnsupportedOperationException()
            }
            else -> declaration.name
        })
        if(parameters.isNotEmpty())
            throw UnsupportedOperationException("Parameters are not null")
    }
}

fun ResolvedIdlType.toCType(): String = when(this) {
    is ResolvedIdlType.Union -> throw UnsupportedOperationException("Union type are not unsupported")
    is ResolvedIdlType.Void -> "void"
    is ResolvedIdlType.Default -> buildString {
        append(when(declaration) {
            is BuiltinIdlDeclaration -> when((declaration as BuiltinIdlDeclaration).kind) {
                WebIDLBuiltinKind.BOOLEAN -> "bool"
                WebIDLBuiltinKind.BYTE -> "int8_t"
                WebIDLBuiltinKind.UNSIGNED_BYTE -> "uint8_t"
                WebIDLBuiltinKind.SHORT -> "int16_t"
                WebIDLBuiltinKind.UNSIGNED_SHORT -> "uint16_t"
                WebIDLBuiltinKind.INT -> "int32_t"
                WebIDLBuiltinKind.UNSIGNED_INT -> "uint32_t"
                WebIDLBuiltinKind.LONG -> "int64_t"
                WebIDLBuiltinKind.UNSIGNED_LONG -> "uint32_t"
                WebIDLBuiltinKind.UNRESTRICTED_FLOAT -> "float"
                WebIDLBuiltinKind.UNRESTRICTED_DOUBLE -> "double"
                WebIDLBuiltinKind.STRING -> "char*"
                else -> throw UnsupportedOperationException()
            }
            else -> declaration.name
        })
        if(parameters.isNotEmpty())
            throw UnsupportedOperationException("Parameters are not null")
    }
}

fun ResolvedIdlType.isString(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    return (declaration as BuiltinIdlDeclaration).kind == WebIDLBuiltinKind.STRING
}

fun IdlResolver.globalOperators() =
    namespaces.values.flatMap { it.operations }

fun functionHeader(
    function: ResolvedIdlOperation,
    isOverride: Boolean = false,
    isActual: Boolean = false,
    isExternal: Boolean = false
) = StringBuilder().apply {
    printFunctionHeader(this, function, isOverride, isActual, isExternal)
}.toString()

fun printFunctionHeader(
    builder: StringBuilder,
    function: ResolvedIdlOperation,
    isOverride: Boolean = false,
    isActual: Boolean = false,
    isExternal: Boolean = false
) = builder.apply {
    if(isActual) append("actual ")
    if(isExternal) append("external ")
    if(isOverride) append("override ")

    append("fun ")
    append(function.name)
    append("(")

    function.args.forEachIndexed { index, arg ->
        append(arg.name)
        append(": ")
        append(arg.type.toKotlinType())

        if(index != function.args.lastIndex)
            append(", ")
    }
    append(")")
    if(function.type !is ResolvedIdlType.Void) {
        append(": ")
        append(function.type.toKotlinType())
    }
}