package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*

val ResolvedIdlDeclaration.kname: String
    get() = when (this) {
        is ResolvedIdlEnum -> kname
        is ResolvedIdlDictionary -> kname
        is ResolvedIdlCallbackFunction -> kname
        is ResolvedIdlInterface -> kname
        else -> throw UnsupportedOperationException()
    }

val ResolvedIdlOperation.kname: String
    get() = when {
        isInterfaceOperationFn() -> "_interface${interfaceName().upperCamelCase()}Fn${interfaceFunctionName().upperCamelCase()}"
        isInterfaceOperationConstructor() -> "_interface${interfaceName().upperCamelCase()}New${interfaceConstructorIndex()}"
        isInterfaceOperationFree() -> "_interface${interfaceName().upperCamelCase()}Free"
        isInterfaceOperationClone() -> "_interface${interfaceName().upperCamelCase()}Clone"
        isInterfaceOperationAddress() -> "_interface${interfaceName().upperCamelCase()}Address"
        else -> name.camelCase()
    }

val ResolvedIdlField.kname: String
    get() = name.camelCase()

val ResolvedIdlDictionary.kname: String
    get() = name.upperCamelCase()

val ResolvedIdlEnum.kname: String
    get() = name.upperCamelCase()

val ResolvedIdlCallbackFunction.kname: String
    get() = name.upperCamelCase()

val ResolvedIdlInterface.kname: String
    get() = name.upperCamelCase()

internal fun ResolvedIdlType.toKotlinType(
    stringAsBytes: Boolean = false,
    enumAsInt: Boolean = false,
    printNullable: Boolean = true,
    ignoreUnsigned: Boolean = false,
    smallUnsignedTypesAsInt: Boolean = false,
    interfaceAsLong: Boolean = false,
    rawInterfaceAsInt: Boolean = false
): String {
    val nullable = if(isNullable && printNullable) "?" else ""
    return when {
        isVoid() -> "Unit"
        isChar() -> "Char"
        isBoolean() -> "Boolean"
        isByte() -> "Byte"
        isUByte() -> if(smallUnsignedTypesAsInt) "Int" else if(ignoreUnsigned) "Byte" else "UByte"
        isShort() -> "Short"
        isUShort() -> if(smallUnsignedTypesAsInt) "Int" else if(ignoreUnsigned) "Short" else "UShort"
        isInt() -> "Int"
        isUInt() -> if(ignoreUnsigned) "Int" else "UInt"
        isLong() -> "Long"
        isULong() -> if(ignoreUnsigned) "Long" else "ULong"
        isFloat() -> "Float"
        isDouble() -> "Double"
        isString() -> if(stringAsBytes) "ByteArray$nullable" else "String$nullable"
        isEnum() -> if(enumAsInt) "Int" else declaration.kname
        isRawInterface() -> if(rawInterfaceAsInt) "Int" else "Long"
        isInterface() -> if(interfaceAsLong) "Long" else "${declaration.kname}$nullable"
        isArray() -> arrayType { type ->
            when {
                type.isPrimitive() -> "${type.toKotlinType(ignoreUnsigned = ignoreUnsigned)}Array$nullable"
                type.isEnum() && enumAsInt -> "IntArray$nullable"
                type.isInterface() && interfaceAsLong -> "LongArray"
                else -> "Array<${type.toKotlinType(stringAsBytes, enumAsInt, printNullable, interfaceAsLong = interfaceAsLong)}>$nullable"
            }
        }
        else -> "${(this as ResolvedIdlType.Default).declaration.kname}$nullable"
    }
}

internal fun castToSigned(
    type: ResolvedIdlType,
    content: String,
    smallTypesAsInt: Boolean = false
): String {
    val nullable = if(type.isNullable) "?" else ""
    return when {
        type.isUByte() -> if(smallTypesAsInt) "$content.toInt() and 0x000000ff" else "$content.toByte()"
        type.isUShort() -> if(smallTypesAsInt) "$content.toInt() and 0x0000ffff" else "$content.toShort()"
        type.isUInt() -> "$content.toInt()"
        type.isULong() -> "$content.toLong()"
        type.isArray() -> type.arrayType { type ->
            when {
                type.isUByte() -> "$content$nullable.asByteArray()"
                type.isUShort() -> "$content$nullable.asShortArray()"
                type.isUInt() -> "$content$nullable.asIntArray()"
                type.isULong() -> "$content$nullable.asLongArray()"
                else -> content
            }
        }
        else -> content
    }
}

internal fun castToUnsigned(type: ResolvedIdlType, content: String): String {
    val nullable = if(type.isNullable) "?" else ""
    return when {
        type.isUByte() -> "$content$nullable.toUByte()"
        type.isUShort() -> "$content$nullable.toUShort()"
        type.isUInt() -> "$content$nullable.toUInt()"
        type.isULong() -> "$content$nullable.toULong()"
        type.isArray() -> type.arrayType { type ->
            when {
                type.isUByte() -> "$content$nullable.asUByteArray()"
                type.isUShort() -> "$content$nullable.asUShortArray()"
                type.isUInt() -> "$content$nullable.asUIntArray()"
                type.isULong() -> "$content$nullable.asULongArray()"
                else -> content
            }
        }
        else -> content
    }
}
