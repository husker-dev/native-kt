@file:OptIn(ExperimentalContracts::class)

package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.webidl.parser.IdlExtendedAttribute
import com.huskerdev.webidl.resolver.*
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun asyncLoadFunctionName(context: NativeModuleContext) =
    "loadLib${context.moduleName.upperCamelCase()}"

fun syncLoadFunctionName(context: NativeModuleContext) =
    "loadLib${context.moduleName.upperCamelCase()}Sync"

fun loadFieldName(context: NativeModuleContext) =
    "isLib${context.moduleName.upperCamelCase()}Loaded"

fun String.snakeCase(): String = buildString {
    this@snakeCase.forEachIndexed { index, c ->
        if(c.isUpperCase() &&
            index > 1 &&
            index < this@snakeCase.length &&
            c.isLowerCase() != this@snakeCase[index-1].isLowerCase()
        ) append('_')

        append(c.lowercase())
    }
}

fun String.camelCase(): String {
    return split("_")
        .joinToString("") { it.uppercaseFirstChar() }
        .replaceFirstChar { it.lowercase() }
}

fun String.upperCamelCase(): String =
    camelCase().uppercaseFirstChar()

fun mangle(
    classPath: String,
    moduleName: String,
    content: String
) = "nativekt" +
        "_${classPath.split(".").joinToString("_") { it.lowercase() }}" +
        "_${moduleName.snakeCase()}" +
        "_$content"

fun <T> Iterable<T>.joinListToString(
    separator: String = ",",
    prefix: String = "",
    postfix: String = "",
    baseIndent: String = "",
    transform: ((T) -> CharSequence) = { it.toString() }
) = buildString {
    joinListTo(this, separator, prefix, postfix, baseIndent, transform)
}

fun <T, A : Appendable> Iterable<T>.joinListTo(
    buffer: A,
    separator: String = ",",
    prefix: String = "",
    postfix: String = "",
    baseIndent: String = "",
    transform: ((T) -> CharSequence) = { it.toString() }
) {
    buffer.append(prefix)
    val iterator = iterator()
    while(iterator.hasNext()) {
        buffer.append('\n').append(baseIndent).append('\t').append(transform(iterator.next()))
        if(iterator.hasNext())
            buffer.append(separator)
        else buffer.append('\n').append(baseIndent)
    }
    buffer.append(postfix)
}

// Names

fun ResolvedIdlOperation.cnameMangled(
    context: NativeModuleContext
) = mangle(context.classPath, context.moduleName, cname)

val ResolvedIdlDeclaration.kname: String
    get() = when (this) {
        is ResolvedIdlEnum -> kname
        is ResolvedIdlDictionary -> kname
        is ResolvedIdlCallbackFunction -> kname
        is ResolvedIdlInterface -> kname
        else -> throw UnsupportedOperationException()
    }

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

val ResolvedIdlOperation.kname: String
    get() = when {
        isInterfaceOperationFn() -> "_interface${interfaceName().upperCamelCase()}Fn${interfaceFunctionName().upperCamelCase()}"
        isInterfaceOperationConstructor() -> "_interface${interfaceName().upperCamelCase()}New${interfaceConstructorIndex()}"
        isInterfaceOperationFree() -> "_interface${interfaceName().upperCamelCase()}Free"
        isInterfaceOperationClone() -> "_interface${interfaceName().upperCamelCase()}Clone"
        isInterfaceOperationAddress() -> "_interface${interfaceName().upperCamelCase()}Address"
        else -> name.camelCase()
    }

val ResolvedIdlField.cname: String
    get() = name.snakeCase()

val ResolvedIdlField.kname: String
    get() = name.camelCase()

val ResolvedIdlDictionary.cname: String
    get() = name.upperCamelCase()

val ResolvedIdlDictionary.kname: String
    get() = name.upperCamelCase()

fun ResolvedIdlDictionary.subCFunc(
    context: NativeModuleContext,
    func: String
): String = mangle(context.classPath, context.moduleName, "_${name.camelCase().lowercase()}_$func")

fun ResolvedIdlDictionary.subFieldCFunc(
    context: NativeModuleContext,
    field: ResolvedIdlField.Declaration
): String = mangle(context.classPath, context.moduleName, "_${name.camelCase().lowercase()}__${field.cname}")

val ResolvedIdlEnum.cname: String
    get() = name.upperCamelCase()

val ResolvedIdlEnum.kname: String
    get() = name.upperCamelCase()

val ResolvedIdlCallbackFunction.cname: String
    get() = name.upperCamelCase()

val ResolvedIdlCallbackFunction.kname: String
    get() = name.upperCamelCase()

val ResolvedIdlInterface.cname: String
    get() = name.upperCamelCase()

val ResolvedIdlInterface.kname: String
    get() = name.upperCamelCase()


// Types

fun <T> ResolvedIdlType.Default.arrayType(block: (type: ResolvedIdlType.Default) -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val param = parameters.firstOrNull()
        ?: throw UnsupportedOperationException("Array without type")
    val type = param as? ResolvedIdlType.Default
        ?: throw UnsupportedOperationException("Unsupported array type: $param")
    return block(type)
}

fun ResolvedIdlType.arrayTypeOrNull(): ResolvedIdlType.Default? {
    contract {
        returnsNotNull() implies(this@arrayTypeOrNull is ResolvedIdlType.Default)
    }
    if(!isArray()) return null
    return parameters.firstOrNull() as? ResolvedIdlType.Default
}

fun ResolvedIdlType.builtinOrNull(): BuiltinIdlDeclaration? {
    contract {
        returnsNotNull() implies(this@builtinOrNull is ResolvedIdlType.Default)
    }
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return null
    return declaration as BuiltinIdlDeclaration
}

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
    return when {
        isVoid() -> "void"
        isChar() -> "uint16_t"
        isBoolean() -> "bool"
        isByte() -> "int8_t"
        isUByte() -> if(ignoreUnsigned) "int8_t" else "uint8_t"
        isShort() -> "int16_t"
        isUShort() -> if(ignoreUnsigned) "int16_t" else "uint16_t"
        isInt() -> "int32_t"
        isUInt() -> if(ignoreUnsigned) "int32_t" else "uint32_t"
        isLong() -> "int64_t"
        isULong() -> if(ignoreUnsigned) "int64_t" else "uint64_t"
        isFloat() -> "float"
        isDouble() -> "double"
        isEnum() -> if(enumAsInt) "int32_t" else declaration.name
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
    return when {
        isVoid() -> "void"
        isChar() -> "uint16_t"
        isBoolean() -> "bool"
        isByte() -> "int8_t"
        isUByte() -> if(ignoreUnsigned) "int8_t" else "uint8_t"
        isShort() -> "int16_t"
        isUShort() -> if(ignoreUnsigned) "int16_t" else "uint16_t"
        isInt() -> "int32_t"
        isUInt() -> if(ignoreUnsigned) "int32_t" else "uint32_t"
        isLong() -> "int64_t"
        isULong() -> if(ignoreUnsigned) "int64_t" else "uint64_t"
        isFloat() -> "float"
        isDouble() -> "double"
        isEnum() -> "int32_t"
        else -> "void*$nullable"
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

// ===== Simple types ======

fun ResolvedIdlType.isPrimitive(): Boolean {
    contract {
        returns(true) implies(this@isPrimitive is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind in setOf(
        WebIDLBuiltinKind.CHAR,
        WebIDLBuiltinKind.BOOLEAN,
        WebIDLBuiltinKind.BYTE,
        WebIDLBuiltinKind.UNSIGNED_BYTE,
        WebIDLBuiltinKind.SHORT,
        WebIDLBuiltinKind.UNSIGNED_SHORT,
        WebIDLBuiltinKind.INT,
        WebIDLBuiltinKind.UNSIGNED_INT,
        WebIDLBuiltinKind.LONG,
        WebIDLBuiltinKind.UNSIGNED_LONG,
        WebIDLBuiltinKind.FLOAT,
        WebIDLBuiltinKind.DOUBLE,
    )
}

fun ResolvedIdlType.isUnsigned(): Boolean {
    contract {
        returns(true) implies(this@isUnsigned is ResolvedIdlType.Default)
    }
    val set = setOf(
        WebIDLBuiltinKind.UNSIGNED_BYTE,
        WebIDLBuiltinKind.UNSIGNED_SHORT,
        WebIDLBuiltinKind.UNSIGNED_INT,
        WebIDLBuiltinKind.UNSIGNED_LONG,
    )
    return builtinOrNull()?.kind in set || arrayTypeOrNull()?.builtinOrNull()?.kind in set
}

fun ResolvedIdlType.toSignedType(): ResolvedIdlType {
    contract {
        returns(true) implies(this@toSignedType is ResolvedIdlType.Default)
    }
    if(!isUnsigned())
        return this
    if(isArray()) {
        val arrType = arrayTypeOrNull()!!.toSignedType()
        return ResolvedIdlType.Default(BuiltinIdlDeclaration(declaration.name, WebIDLBuiltinKind.LIST), listOf(arrType), isNullable)
    }
    val kind = when {
        isUByte() -> WebIDLBuiltinKind.BYTE
        isUShort() -> WebIDLBuiltinKind.SHORT
        isUInt() -> WebIDLBuiltinKind.INT
        isULong() -> WebIDLBuiltinKind.LONG
        else -> throw UnsupportedOperationException()
    }
    return ResolvedIdlType.Default(BuiltinIdlDeclaration(declaration.name, kind), emptyList(), isNullable)
}

fun ResolvedIdlType.isVoid(): Boolean {
    contract {
        returns(true) implies(this@isVoid is ResolvedIdlType.Void)
    }
    return this is ResolvedIdlType.Void
}

fun ResolvedIdlType.isString(): Boolean {
    contract {
        returns(true) implies(this@isString is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.STRING
}

fun ResolvedIdlType.isLong(): Boolean {
    contract {
        returns(true) implies(this@isLong is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.LONG
}

fun ResolvedIdlType.isULong(): Boolean {
    contract {
        returns(true) implies(this@isULong is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.UNSIGNED_LONG
}

fun ResolvedIdlType.isInt(): Boolean {
    contract {
        returns(true) implies(this@isInt is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.INT
}

fun ResolvedIdlType.isUInt(): Boolean {
    contract {
        returns(true) implies(this@isUInt is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.UNSIGNED_INT
}

fun ResolvedIdlType.isDouble(): Boolean {
    contract {
        returns(true) implies(this@isDouble is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.DOUBLE
}

fun ResolvedIdlType.isFloat(): Boolean {
    contract {
        returns(true) implies(this@isFloat is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.FLOAT
}

fun ResolvedIdlType.isBoolean(): Boolean {
    contract {
        returns(true) implies(this@isBoolean is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.BOOLEAN
}

fun ResolvedIdlType.isShort(): Boolean {
    contract {
        returns(true) implies(this@isShort is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.SHORT
}

fun ResolvedIdlType.isUShort(): Boolean {
    contract {
        returns(true) implies(this@isUShort is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.UNSIGNED_SHORT
}

fun ResolvedIdlType.isByte(): Boolean {
    contract {
        returns(true) implies(this@isByte is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.BYTE
}

fun ResolvedIdlType.isUByte(): Boolean {
    contract {
        returns(true) implies(this@isUByte is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.UNSIGNED_BYTE
}

fun ResolvedIdlType.isChar(): Boolean {
    contract {
        returns(true) implies(this@isChar is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.CHAR
}

fun ResolvedIdlType.isArray(): Boolean {
    contract {
        returns(true) implies(this@isArray is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.LIST
}

fun ResolvedIdlType.isCallback(): Boolean {
    contract {
        returns(true) implies(this@isCallback is ResolvedIdlType.Default)
    }
    return this is ResolvedIdlType.Default && declaration is ResolvedIdlCallbackFunction
}

fun ResolvedIdlType.isEnum(): Boolean {
    contract {
        returns(true) implies(this@isEnum is ResolvedIdlType.Default)
    }
    return this is ResolvedIdlType.Default && declaration is ResolvedIdlEnum
}

fun ResolvedIdlType.isInterface(): Boolean {
    contract {
        returns(true) implies(this@isInterface is ResolvedIdlType.Default)
    }
    return this is ResolvedIdlType.Default && declaration is ResolvedIdlInterface
}

fun ResolvedIdlType.isRawInterface(): Boolean {
    contract {
        returns(true) implies(this@isRawInterface is ResolvedIdlType.Default)
    }
    return isInterface() && parameters.isNotEmpty()
}

fun ResolvedIdlType.isDictionary(): Boolean {
    contract {
        returns(true) implies(this@isDictionary is ResolvedIdlType.Default)
    }
    return this is ResolvedIdlType.Default && declaration is ResolvedIdlDictionary
}

fun ResolvedIdlType.isReleasable(): Boolean =
    isArray() || isString() || isDictionary() || isCallback() || isInterface()

// ==== Arrays =====

fun ResolvedIdlType.isStringArray(): Boolean {
    contract { returns(true) implies(this@isStringArray is ResolvedIdlType.Default) }
    return arrayTypeOrNull()?.isString() ?: false
}

fun ResolvedIdlType.isCharArray(): Boolean {
    contract { returns(true) implies(this@isCharArray is ResolvedIdlType.Default) }
    return arrayTypeOrNull()?.isChar() ?: false
}

fun ResolvedIdlType.isBooleanArray(): Boolean {
    contract { returns(true) implies(this@isBooleanArray is ResolvedIdlType.Default) }
    return arrayTypeOrNull()?.isBoolean() ?: false
}

fun ResolvedIdlType.isByteArray(): Boolean {
    contract { returns(true) implies(this@isByteArray is ResolvedIdlType.Default) }
    return arrayTypeOrNull()?.isByte() ?: false
}

fun ResolvedIdlType.isUByteArray(): Boolean {
    contract { returns(true) implies(this@isUByteArray is ResolvedIdlType.Default) }
    return arrayTypeOrNull()?.isUByte() ?: false
}

fun ResolvedIdlType.isShortArray(): Boolean {
    contract { returns(true) implies(this@isShortArray is ResolvedIdlType.Default) }
    return arrayTypeOrNull()?.isShort() ?: false
}

fun ResolvedIdlType.isUShortArray(): Boolean {
    contract { returns(true) implies(this@isUShortArray is ResolvedIdlType.Default) }
    return arrayTypeOrNull()?.isUShort() ?: false
}

fun ResolvedIdlType.isIntArray(): Boolean {
    contract { returns(true) implies(this@isIntArray is ResolvedIdlType.Default) }
    return arrayTypeOrNull()?.isInt() ?: false
}

fun ResolvedIdlType.isUIntArray(): Boolean {
    contract { returns(true) implies(this@isUIntArray is ResolvedIdlType.Default) }
    return arrayTypeOrNull()?.isUInt() ?: false
}

fun ResolvedIdlType.isLongArray(): Boolean {
    contract { returns(true) implies(this@isLongArray is ResolvedIdlType.Default) }
    return arrayTypeOrNull()?.isLong() ?: false
}

fun ResolvedIdlType.isULongArray(): Boolean {
    contract { returns(true) implies(this@isULongArray is ResolvedIdlType.Default) }
    return arrayTypeOrNull()?.isULong() ?: false
}

fun ResolvedIdlType.isFloatArray(): Boolean {
    contract { returns(true) implies(this@isFloatArray is ResolvedIdlType.Default) }
    return arrayTypeOrNull()?.isFloat() ?: false
}

fun ResolvedIdlType.isDoubleArray(): Boolean {
    contract { returns(true) implies(this@isDoubleArray is ResolvedIdlType.Default) }
    return arrayTypeOrNull()?.isDouble() ?: false
}

fun ResolvedIdlType.isPrimitiveArray(): Boolean {
    contract { returns(true) implies(this@isPrimitiveArray is ResolvedIdlType.Default) }
    return arrayTypeOrNull()?.isPrimitive() ?: false
}

fun ResolvedIdlType.isEnumArray(): Boolean {
    contract {
        returns(true) implies(this@isEnumArray is ResolvedIdlType.Default)
    }
    return arrayTypeOrNull()?.isEnum() ?: false
}

fun ResolvedIdlType.isDictionaryArray(): Boolean {
    contract {
        returns(true) implies(this@isDictionaryArray is ResolvedIdlType.Default)
    }
    return arrayTypeOrNull()?.isDictionary() ?: false
}

fun ResolvedIdlType.isInterfaceArray(): Boolean {
    contract {
        returns(true) implies(this@isInterfaceArray is ResolvedIdlType.Default)
    }
    return arrayTypeOrNull()?.isInterface() ?: false
}


private fun ResolvedIdlOperation.hasAttribute(name: String): Boolean =
    attributes.any { it.name.lowercase() == name }


fun ResolvedIdlOperation.getInterface(context: NativeModuleContext) =
    context.interfaces.firstOrNull { it.name == interfaceName() }

fun ResolvedIdlOperation.isInterfaceOperation() = hasAttribute("__interface")

fun ResolvedIdlOperation.interfaceName(): String = attributes
    .filterIsInstance<IdlExtendedAttribute.StringValue>()
    .first { it.name.lowercase() == "__interface" }
    .value

fun ResolvedIdlOperation.interfaceFunctionName(): String = attributes
    .filterIsInstance<IdlExtendedAttribute.StringValue>()
    .first { it.name.lowercase() == "__interface_fn" }
    .value

fun ResolvedIdlOperation.interfaceConstructorNameOrNull(): String? = attributes
    .filterIsInstance<IdlExtendedAttribute.IdentifierValue>()
    .firstOrNull { it.name.lowercase() == "name" }
    ?.identifier

fun ResolvedIdlOperation.interfaceConstructorIndex(): Int = attributes
    .filterIsInstance<IdlExtendedAttribute.IntegerValue>()
    .first { it.name.lowercase() == "__interface_new" }
    .value

fun ResolvedIdlOperation.isInterfaceOperationConstructor() = hasAttribute("__interface_new")
fun ResolvedIdlOperation.isInterfaceOperationFn() = hasAttribute("__interface_fn")
fun ResolvedIdlOperation.isInterfaceOperationFree() = hasAttribute("__interface_free")
fun ResolvedIdlOperation.isInterfaceOperationClone() = hasAttribute("__interface_clone")
fun ResolvedIdlOperation.isInterfaceOperationAddress() = hasAttribute("__interface_address")

fun ResolvedIdlOperation.isCritical(): Boolean = hasAttribute("critical")

fun ResolvedIdlOperation.isCriticalCapable(): Boolean =
    (type.isVoid() || type.isPrimitive() || type.isEnum() || type.isInterface()) &&
            args.all {
                it.type.isPrimitive() || it.type.isEnum()
                    || it.type.isString() || it.type.isInterface()
                    || it.type.isPrimitiveArray() || it.type.isEnumArray()
            }

// Same as default critical, but without array and string args
fun ResolvedIdlOperation.isAndroidCriticalCapable(): Boolean =
    !type.isArray() && !type.isString() && !type.isDictionary() &&
            args.all { !it.type.isArray() && !it.type.isString() }

// ========

fun IdlResolver.allOperations() = buildList {
    addAll(globalOperators())
    addAll(interfaceOperators())
}

fun IdlResolver.globalOperators() =
    namespaces.values.flatMap { it.operations }

fun IdlResolver.interfaceOperators() =
    interfaces.values.flatMap { it.toOperations() }

fun ResolvedIdlDictionary.allFields() = buildList {
    var cur: ResolvedIdlDictionary? = this@allFields
    while(cur != null) {
        addAll(0, cur.fields)
        cur = cur.implements
    }
}

fun ResolvedIdlInterface.toOperations() = buildList {
    val interfaceTagAttribute = IdlExtendedAttribute.StringValue("__interface", name)
    val criticalAttribute = IdlExtendedAttribute.NoArgs("critical")

    val longType = ResolvedIdlType.Default(
        BuiltinIdlDeclaration("long", WebIDLBuiltinKind.LONG),
        emptyList(),
        false
    )
    val rawInterfaceType = ResolvedIdlType.Default(
        this@toOperations,
        listOf(longType),
        false
    )
    val rawInterfaceArg = ResolvedIdlField.Argument(
        "_self", rawInterfaceType, null,
        isOptional = false, isVariadic = false, attributes = listOf(interfaceTagAttribute)
    )

    constructors.forEachIndexed { index, constructor ->
        add(ResolvedIdlOperation(
            name = "INTERFACE_CONSTRUCTOR",
            type = rawInterfaceType,
            args = constructor.args,
            isStatic = false,
            attributes = buildList {
                add(interfaceTagAttribute)
                add(IdlExtendedAttribute.IntegerValue("__interface_new", index))
                addAll(constructor.attributes)
            }
        ))
    }
    operations.forEach { operation ->
        add(ResolvedIdlOperation(
            name = "INTERFACE_FUNCTION",
            type = operation.type,
            args = buildList {
                add(rawInterfaceArg)
                addAll(operation.args)
            },
            isStatic = false,
            attributes = buildList {
                add(interfaceTagAttribute)
                add(IdlExtendedAttribute.StringValue("__interface_fn", operation.name))
                addAll(operation.attributes)
            }
        ))
    }

    // free
    add(ResolvedIdlOperation(
        name = "INTERFACE_FREE",
        type = ResolvedIdlType.Void("void"),
        args = listOf(rawInterfaceArg),
        isStatic = false,
        attributes = listOf(interfaceTagAttribute, IdlExtendedAttribute.NoArgs("__interface_free"))
    ))

    // clone
    add(ResolvedIdlOperation(
        name = "INTERFACE_CLONE",
        type = rawInterfaceType,
        args = listOf(rawInterfaceArg),
        isStatic = false,
        attributes = listOf(interfaceTagAttribute, criticalAttribute, IdlExtendedAttribute.NoArgs("__interface_clone"))
    ))

    // address
    add(ResolvedIdlOperation(
        name = "INTERFACE_ADDRESS",
        type = longType,
        args = listOf(rawInterfaceArg),
        isStatic = false,
        attributes = listOf(interfaceTagAttribute, criticalAttribute, IdlExtendedAttribute.NoArgs("__interface_address"))
    ))
}


fun printFunctionHeader(
    builder: StringBuilder,
    function: ResolvedIdlOperation,
    isOverride: Boolean = false,
    isPrivate: Boolean = false,
    isActual: Boolean = false,
    isExternal: Boolean = false,
    isExpect: Boolean = false,
    name: String = function.kname,
    printType: Boolean = true,
    forcePrintVoid: Boolean = false,
    stringAsBytes: Boolean = false,
    enumAsInt: Boolean = false,
    printNullable: Boolean = true,
    ignoreUnsigned: Boolean = false,
    interfaceAsLong: Boolean = false,
    arraysLen: Boolean = false,
) = builder.apply {
    if(isActual) append("actual ")
    if(isExpect) append("expect ")
    if(isExternal) append("external ")
    if(isPrivate) append("private ")
    if(isOverride) append("override ")

    val args = function.args.flatMap { arg ->
        val name = arg.kname
        val result = "$name: ${arg.type.toKotlinType(
            stringAsBytes, 
            enumAsInt,
            printNullable,
            ignoreUnsigned,
            interfaceAsLong = interfaceAsLong,
        )}"
        when {
            stringAsBytes && arg.type.isString() ->
                listOf(result, "__len_$name: Int", "__size_$name: Int")
            arraysLen && arg.type.isArray() ->
                listOf(result, "__len_$name: Int")
            else -> listOf(result)
        }
    }.joinToString()

    append("fun $name($args)")

    if(printType && (forcePrintVoid || function.type !is ResolvedIdlType.Void))
        append(": ${function.type.toKotlinType(
            stringAsBytes, 
            enumAsInt,
            printNullable,
            ignoreUnsigned,
            interfaceAsLong = interfaceAsLong
        )}")
}

fun StringBuilder.printLabel(text: String, padding: Int = 5, indent: String = "") {
    // line 1
    append("\n")
    append(indent)
    append("// ╔")
    append("═".repeat(text.length + padding*2))
    append("╗\n")

    // line 2
    append(indent)
    append("// ║")
    append(" ".repeat(padding))
    append(text)
    append(" ".repeat(padding))
    append("║\n")

    // line 3
    append(indent)
    append("// ╚")
    append("═".repeat(text.length + padding*2))
    append("╝\n")
}