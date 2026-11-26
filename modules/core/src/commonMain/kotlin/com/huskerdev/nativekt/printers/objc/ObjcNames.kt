package com.huskerdev.nativekt.printers.objc

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*

internal val ResolvedIdlDeclaration.objcName: String
    get() = name.upperCamelCase()

internal val ResolvedIdlField.objcName: String
    get() = name.camelCase().let {
        if (it in setOf("self", "super", "id", "Class", "new", "copy", "description", "hash")) "${it}_" else it
    }

internal val ResolvedIdlOperation.objcName: String
    get() = when {
        isInterfaceOperationConstructor() ->
            interfaceConstructorNameOrNull()?.camelCase() ?: "create${interfaceConstructorIndex()}"
        isInterfaceOperationFn() -> interfaceFunctionName().camelCase()
        else -> name.camelCase()
    }

internal fun ResolvedIdlType.isObjcObject() = isNullable || isReleasable()

fun ResolvedIdlType.toObjcType(printNullable: Boolean = true): String {
    val objcType = when {
        isVoid() -> "void"
        (isPrimitive() || isEnum()) && isNullable -> "NSNumber*"
        isBoolean() -> "BOOL"
        isChar() -> "unichar"
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
        isEnum() -> declaration.objcName
        isString() -> "KTString*"
        isArray() -> {
            val element = arrayTypeOrNull()!!
            when {
                !element.isNullable && (element.isPrimitive() || element.isEnum()) ->
                    "${element.objcArrayClassName}*"
                element.isNullable -> "KTArray*"
                else -> "KTArray<${element.toObjcType(printNullable = false)}>*"
            }
        }
        isDictionary() || isInterface() || isCallback() -> "${declaration.objcName}*"
        else -> throw UnsupportedOperationException("Unsupported Objective-C type: $this")
    }
    if (!printNullable || !isObjcObject()) return objcType

    val nullability = if (isNullable) "_Nullable" else "_Nonnull"
    return "$objcType $nullability"
}

internal val ResolvedIdlType.objcArrayClassName: String
    get() = when {
        isChar() -> "KTCharArray"
        isBoolean() -> "KTBooleanArray"
        isByte() || isUByte() -> "KTByteArray"
        isShort() || isUShort() -> "KTShortArray"
        isInt() || isUInt() || isEnum() -> "KTIntArray"
        isLong() || isULong() -> "KTLongArray"
        isFloat() -> "KTFloatArray"
        isDouble() -> "KTDoubleArray"
        else -> throw UnsupportedOperationException("Not a primitive array element: $this")
    }

internal data class ObjcPrimitiveType(
    val name: String,
    val type: String,
    val accessor: String,
    val toNative: Boolean,
    val toKotlin: Boolean
)

internal fun NativeModuleContext.objcPrimitiveArrayTypes() = listOf(
    ObjcPrimitiveType("Char", "uint16_t", "unsignedShortValue",
        hasCharArrayToNative, hasCharArrayToKotlin),
    ObjcPrimitiveType("Boolean", "bool", "boolValue",
        hasBooleanArrayToNative, hasBooleanArrayToKotlin),
    ObjcPrimitiveType("Byte", "int8_t", "charValue",
        hasByteArrayToNative || hasUByteArrayToNative,
        hasByteArrayToKotlin || hasUByteArrayToKotlin),
    ObjcPrimitiveType("Short", "int16_t", "shortValue",
        hasShortArrayToNative || hasUShortArrayToNative,
        hasShortArrayToKotlin || hasUShortArrayToKotlin),
    ObjcPrimitiveType("Int", "int32_t", "intValue",
        hasIntArrayToNative || hasUIntArrayToNative || hasEnumArrayToNative,
        hasIntArrayToKotlin || hasUIntArrayToKotlin || hasEnumArrayToKotlin),
    ObjcPrimitiveType("Long", "int64_t", "longLongValue",
        hasLongArrayToNative || hasULongArrayToNative,
        hasLongArrayToKotlin || hasULongArrayToKotlin),
    ObjcPrimitiveType("Float", "float", "floatValue",
        hasFloatArrayToNative, hasFloatArrayToKotlin),
    ObjcPrimitiveType("Double", "double", "doubleValue",
        hasDoubleArrayToNative, hasDoubleArrayToKotlin)
).filter { it.toNative || it.toKotlin }

internal fun NativeModuleContext.objcBoxedPrimitiveTypes() = listOf(
    ObjcPrimitiveType("Char", "uint16_t", "unsignedShortValue",
        hasCharNullableToNative, hasCharNullableToKotlin),
    ObjcPrimitiveType("Boolean", "bool", "boolValue",
        hasBooleanNullableToNative, hasBooleanNullableToKotlin),
    ObjcPrimitiveType("Byte", "int8_t", "charValue",
        hasByteNullableToNative || hasUByteNullableToNative,
        hasByteNullableToKotlin || hasUByteNullableToKotlin),
    ObjcPrimitiveType("Short", "int16_t", "shortValue",
        hasShortNullableToNative || hasUShortNullableToNative,
        hasShortNullableToKotlin || hasUShortNullableToKotlin),
    ObjcPrimitiveType("Int", "int32_t", "intValue",
        hasIntNullableToNative || hasUIntNullableToNative || hasEnumNullableToNative,
        hasIntNullableToKotlin || hasUIntNullableToKotlin || hasEnumNullableToKotlin),
    ObjcPrimitiveType("Long", "int64_t", "longLongValue",
        hasLongNullableToNative || hasULongNullableToNative,
        hasLongNullableToKotlin || hasULongNullableToKotlin),
    ObjcPrimitiveType("Float", "float", "floatValue",
        hasFloatNullableToNative, hasFloatNullableToKotlin),
    ObjcPrimitiveType("Double", "double", "doubleValue",
        hasDoubleNullableToNative, hasDoubleNullableToKotlin)
).filter { it.toNative || it.toKotlin }

internal fun objcMethod(name: String, args: List<Pair<String, String>>): String =
    if (args.isNotEmpty()) {
        args.mapIndexed { index, (arg, type) ->
            "${if (index == 0) name else arg}:($type)$arg"
        }.joinToString(" ")
    } else name

internal fun objcCall(receiver: String, name: String, args: List<Pair<String, String>>): String =
    "[$receiver " + if (args.isEmpty()) "$name]" else args.mapIndexed { index, (arg, value) ->
        "${if (index == 0) name else arg}:$value"
    }.joinToString(" ") + "]"

internal fun objcRuntimeName(context: NativeModuleContext, name: String) =
    "__attribute__((objc_runtime_name(\"${context.moduleName.upperCamelCase()}_$name\")))"
