package com.huskerdev.nativekt.printers.cpp

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*

internal val ResolvedIdlDeclaration.cppName: String
    get() = when (this) {
        is ResolvedIdlEnum -> cppName
        is ResolvedIdlDictionary -> cppName
        is ResolvedIdlCallbackFunction -> cppName
        is ResolvedIdlInterface -> cppName
        else -> throw UnsupportedOperationException("${(this as BuiltinIdlDeclaration).kind}")
    }

internal val ResolvedIdlOperation.cppName: String
    get() = when {
        isInterfaceOperationConstructor() -> interfaceConstructorNameOrNull()?.snakeCase() ?: "_create"
        isInterfaceOperationFn() -> interfaceFunctionName().snakeCase()
        else -> name.snakeCase()
    }

internal val ResolvedIdlDictionary.cppName: String
    get() = name.upperCamelCase()

internal val ResolvedIdlField.cppName: String
    get() = name.snakeCase()

internal val ResolvedIdlEnum.cppName: String
    get() = name.upperCamelCase()

internal val ResolvedIdlCallbackFunction.cppName: String
    get() = name.upperCamelCase()

internal val ResolvedIdlInterface.cppName: String
    get() = "I${name.upperCamelCase()}"

fun ResolvedIdlType.toCppType(
    enumAsInt: Boolean = false,
    printOption: Boolean = true,
    ptr: Boolean = false
): String {
    val p = if(ptr) "*" else ""
    val result = when {
        isVoid() -> "void"
        isChar() -> "uint16_t"
        isBoolean() -> "bool"
        isByte() -> "int8_t"
        isUByte() -> "uint8_t"
        isShort() -> "int16_t"
        isUShort() -> "uint16_t"
        isInt() -> "int32_t"
        isUInt() -> "uint32_t"
        isLong() -> "int64_t"
        isULong() -> "uint64_t"
        isFloat() -> "float"
        isDouble() -> "double"
        isEnum() -> if(enumAsInt) "int32_t" else declaration.cppName
        isString() -> "KString$p"
        isDictionary() -> "${declaration.cppName}$p"
        isInterface() || isCallback() -> "std::shared_ptr<${declaration.cppName}>$p"
        isArray() -> "KArray<${arrayTypeOrNull()!!.toCppType(enumAsInt, printOption = true, ptr = false)}>$p"
        else -> "UNKNOWN"
    }
    return if(isNullable && printOption)
        "KOptional<$result>"
    else result
}