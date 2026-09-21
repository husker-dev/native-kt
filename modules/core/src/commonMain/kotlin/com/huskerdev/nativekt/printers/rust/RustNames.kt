package com.huskerdev.nativekt.printers.rust

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*

internal val ResolvedIdlDeclaration.rustName: String
    get() = when (this) {
        is ResolvedIdlEnum -> rustName
        is ResolvedIdlDictionary -> rustName
        is ResolvedIdlCallbackFunction -> rustName
        is ResolvedIdlInterface -> rustName
        else -> throw UnsupportedOperationException("${(this as BuiltinIdlDeclaration).kind}")
    }

internal val ResolvedIdlOperation.rustName: String
    get() = when {
        isInterfaceOperationConstructor() -> interfaceConstructorNameOrNull()?.snakeCase() ?: "new"
        isInterfaceOperationFn() -> interfaceFunctionName().snakeCase()
        else -> name.snakeCase()
    }

internal val ResolvedIdlDictionary.rustName: String
    get() = name.upperCamelCase()

internal val ResolvedIdlField.rustName: String
    get() = name.snakeCase()

internal val ResolvedIdlEnum.rustName: String
    get() = name.upperCamelCase()

internal val ResolvedIdlCallbackFunction.rustName: String
    get() = name.upperCamelCase()

internal val ResolvedIdlInterface.rustName: String
    get() = "crate::${name.upperCamelCase()}"

internal fun ResolvedIdlType.toNativeRustType(ptrType: String = "mut"): String = when {
    isReleasable() -> "*$ptrType ${toRustType(printOption = false)}"
    isRawInterface() -> "usize"
    else -> toRustType()
}

internal fun ResolvedIdlType.toRustType(
    printOption: Boolean = true
): String {
    val result = when {
        isVoid() -> "()"
        isChar() -> "u16"
        isBoolean() -> "bool"
        isByte() -> "i8"
        isUByte() -> "u8"
        isShort() -> "i16"
        isUShort() -> "u16"
        isInt() -> "i32"
        isUInt() -> "u32"
        isLong() -> "i64"
        isULong() -> "u64"
        isFloat() -> "f32"
        isDouble() -> "f64"
        isEnum() -> declaration.rustName
        isString() -> "String"
        isDictionary() -> declaration.rustName
        isInterface() || isCallback() -> "Arc<${declaration.rustName}>"
        isArray() -> "Vec<${arrayTypeOrNull()!!.toRustType()}>"
        else -> "UNKNOWN"
    }
    return if(isNullable && printOption)
        "Option<$result>"
    else result
}