package com.huskerdev.nativekt.printers.swift

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*

val ResolvedIdlDeclaration.swiftName: String
    get() = when (this) {
        is ResolvedIdlEnum -> swiftName
        is ResolvedIdlDictionary -> swiftName
        is ResolvedIdlCallbackFunction -> swiftName
        is ResolvedIdlInterface -> swiftName
        else -> throw UnsupportedOperationException(toString())
    }

val ResolvedIdlOperation.swiftName: String
    get() = when {
        isInterfaceOperationFn() -> interfaceFunctionName().camelCase()
        isInterfaceOperationConstructor() -> "_interface${interfaceName().upperCamelCase()}New${interfaceConstructorIndex()}"
        isInterfaceOperationFree() -> "_interface${interfaceName().upperCamelCase()}Free"
        isInterfaceOperationClone() -> "_interface${interfaceName().upperCamelCase()}Clone"
        isInterfaceOperationAddress() -> "_interface${interfaceName().upperCamelCase()}Address"
        else -> name.camelCase()
    }

val ResolvedIdlField.swiftName: String
    get() = if(name.startsWith("_")) name else name.camelCase()

val ResolvedIdlDictionary.swiftName: String
    get() = name.upperCamelCase()

val ResolvedIdlCallbackFunction.swiftName: String
    get() = name.upperCamelCase()

val ResolvedIdlInterface.swiftName: String
    get() = name.upperCamelCase()

val ResolvedIdlEnum.swiftName: String
    get() = name.upperCamelCase()

fun ResolvedIdlType.toSwiftType(
    printNullable: Boolean = false
): String = when {
    isVoid() -> "Void"
    isChar() -> "UInt16"
    isBoolean() -> "Bool"
    isByte() -> "Int8"
    isUByte() -> "UInt8"
    isShort() -> "Int16"
    isUShort() -> "UInt16"
    isInt() -> "Int32"
    isUInt() -> "UInt32"
    isLong() -> "Int64"
    isULong() -> "UInt64"
    isFloat() -> "Float"
    isDouble() -> "Double"
    isString() -> "String"
    isArray() -> "[${arrayTypeOrNull()!!.toSwiftType(printNullable = true)}]"
    else -> (this as ResolvedIdlType.Default).declaration.swiftName
}.run { if(printNullable && isNullable) "$this?" else this }

fun ResolvedIdlType.toNativeSwiftType(): String = when {
    isNullable -> "UnsafeMutableRawPointer"
    isVoid() -> "Void"
    isChar() -> "UInt16"
    isBoolean() -> "Bool"
    isByte() -> "Int8"
    isUByte() -> "UInt8"
    isShort() -> "Int16"
    isUShort() -> "UInt16"
    isInt() -> "Int32"
    isUInt() -> "UInt32"
    isLong() -> "Int64"
    isULong() -> "UInt64"
    isFloat() -> "Float"
    isDouble() -> "Double"
    isEnum() -> "Int32"
    else -> "UnsafeMutableRawPointer"
}.run { if(isNullable) "$this?" else this }
