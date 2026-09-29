package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.kotlin.toKotlinType
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*

fun ResolvedIdlOperation.cnameMangled(
    context: NativeModuleContext
) = context.mangle(cname, isInternal = false)

fun ResolvedIdlDictionary.subCFunc(
    context: NativeModuleContext,
    func: String
): String = context.mangle("${name.camelCase().lowercase()}_$func")

fun ResolvedIdlDictionary.subFieldCFunc(
    context: NativeModuleContext,
    field: ResolvedIdlField.Declaration
): String = context.mangle("${name.camelCase().lowercase()}__${field.cname}")

val ResolvedIdlDeclaration.cname: String
    get() = when (this) {
        is ResolvedIdlEnum -> cname
        is ResolvedIdlDictionary -> cname
        is ResolvedIdlCallbackFunction -> cname
        is ResolvedIdlInterface -> cname
        else -> throw UnsupportedOperationException()
    }

val ResolvedIdlOperation.cname: String
    get() = when {
        isInterfaceOperationFn() -> "_interface_${interfaceName().lowercase()}_fn_${interfaceFunctionName().snakeCase()}"
        isInterfaceOperationConstructor() -> "_interface_${interfaceName().lowercase()}_new_${interfaceConstructorIndex()}"
        isInterfaceOperationFree() -> "_interface_${interfaceName().lowercase()}_free"
        isInterfaceOperationClone() -> "_interface_${interfaceName().lowercase()}_clone"
        isInterfaceOperationAddress() -> "_interface_${interfaceName().lowercase()}_address"
        else -> name.snakeCase()
    }

val ResolvedIdlField.cname: String
    get() = name.snakeCase()

val ResolvedIdlDictionary.cname: String
    get() = name.upperCamelCase()

val ResolvedIdlCallbackFunction.cname: String
    get() = name.upperCamelCase()

val ResolvedIdlInterface.cname: String
    get() = name.upperCamelCase()

val ResolvedIdlEnum.cname: String
    get() = name.upperCamelCase()

fun ResolvedIdlType.toCType(
    enumAsInt: Boolean = false,
    ptr: Boolean = true,
    printNullable: Boolean = false,
    ignoreUnsigned: Boolean = false,
    rcAsVoid: Boolean = false
): String {
    val nullable = if(ptr && printNullable) {
        if (isNullable) " _Nullable" else " _Nonnull"
    } else ""
    val ptr = if(ptr) "*" else ""
    val primitiveNullable = if((isPrimitive() || isEnum()) && isNullable) "*$nullable" else ""
    return when {
        isVoid() -> "void"
        isChar() -> "uint16_t$primitiveNullable"
        isBoolean() -> "bool$primitiveNullable"
        isByte() -> "int8_t$primitiveNullable"
        isUByte() -> if(ignoreUnsigned) "int8_t$primitiveNullable" else "uint8_t$primitiveNullable"
        isShort() -> "int16_t$primitiveNullable"
        isUShort() -> if(ignoreUnsigned) "int16_t$primitiveNullable" else "uint16_t$primitiveNullable"
        isInt() -> "int32_t$primitiveNullable"
        isUInt() -> if(ignoreUnsigned) "int32_t$primitiveNullable" else "uint32_t$primitiveNullable"
        isLong() -> "int64_t$primitiveNullable"
        isULong() -> if(ignoreUnsigned) "int64_t$primitiveNullable" else "uint64_t$primitiveNullable"
        isFloat() -> "float$primitiveNullable"
        isDouble() -> "double$primitiveNullable"
        isEnum() -> if(enumAsInt) "int32_t$primitiveNullable" else "${declaration.name}$primitiveNullable"
        isString() -> "KString$ptr$nullable"
        isArray() -> arrayType { type ->
            when {
                type.isPrimitive() -> "K${type.toKotlinType(ignoreUnsigned = ignoreUnsigned)}Array$ptr$nullable"
                type.isEnum() -> "KIntArray$ptr$nullable"
                else -> "KArray$ptr$nullable"
            }
        }
        isInterface() || isCallback() ->
            if(rcAsVoid && isRawInterface()) "void*$nullable"
            else "RC_${declaration.cname}*$nullable"
        else -> "${(this as ResolvedIdlType.Default).declaration.name.upperCamelCase()}$ptr$nullable"
    }
}

fun ResolvedIdlType.toCommonNativeType(
    printNullable: Boolean = false,
    ignoreUnsigned: Boolean = false,
): String {
    val nullable = if(printNullable) {
        if (isNullable) " _Nullable" else " _Nonnull"
    } else ""
    val primitiveNullable = if((isPrimitive() || isEnum()) && isNullable) "*$nullable" else ""
    return when {
        isVoid() -> "void"
        isChar() -> "uint16_t$primitiveNullable"
        isBoolean() -> "bool$primitiveNullable"
        isByte() -> "int8_t$primitiveNullable"
        isUByte() -> if(ignoreUnsigned) "int8_t$primitiveNullable" else "uint8_t$primitiveNullable"
        isShort() -> "int16_t$primitiveNullable"
        isUShort() -> if(ignoreUnsigned) "int16_t$primitiveNullable" else "uint16_t$primitiveNullable"
        isInt() -> "int32_t$primitiveNullable"
        isUInt() -> if(ignoreUnsigned) "int32_t$primitiveNullable" else "uint32_t$primitiveNullable"
        isLong() -> "int64_t$primitiveNullable"
        isULong() -> if(ignoreUnsigned) "int64_t$primitiveNullable" else "uint64_t$primitiveNullable"
        isFloat() -> "float$primitiveNullable"
        isDouble() -> "double$primitiveNullable"
        isEnum() -> "int32_t$primitiveNullable"
        else -> "void*$nullable"
    }
}