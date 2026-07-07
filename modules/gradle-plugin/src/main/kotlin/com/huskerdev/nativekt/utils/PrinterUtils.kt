@file:OptIn(ExperimentalContracts::class)

package com.huskerdev.nativekt.utils

import com.huskerdev.webidl.parser.IdlExtendedAttribute
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun asyncLoadFunctionName(moduleName: String) =
    "loadLib${moduleName.capitalized()}"

fun syncLoadFunctionName(moduleName: String) =
    "loadLib${moduleName.capitalized()}Sync"

fun mangle(
    classPath: String,
    moduleName: String,
    content: String
) = "nativekt" +
        "_${classPath.split(".").joinToString("_") { it.lowercase() }}" +
        "_${moduleName.snakeCase()}" +
        "_${content.snakeCase()}"

fun interfaceOperationCName(
    classPath: String,
    moduleName: String,
    inter: ResolvedIdlInterface,
    operation: ResolvedIdlOperation
) = mangle(classPath, moduleName,
    content = "_${inter.name.lowercase()}_fn_${operation.name.snakeCase()}"
)

fun interfaceConstructorCName(
    classPath: String,
    moduleName: String,
    inter: ResolvedIdlInterface,
    constructor: ResolvedIdlConstructor
) = mangle(classPath, moduleName,
    content = "_${inter.name.lowercase()}_new_${inter.constructors.indexOf(constructor)}"
)

fun interfaceFreeCName(
    classPath: String,
    moduleName: String,
    inter: ResolvedIdlInterface
) = mangle(classPath, moduleName,
    content = "_${inter.name.lowercase()}_free"
)

fun ResolvedIdlInterface.toOperations(
    classPath: String,
    moduleName: String
) = buildList {
    val interfaceType = ResolvedIdlType.Default(this@toOperations, emptyList(), false)
    val interfaceArg = ResolvedIdlField.Argument(
        "ptr", interfaceType, null,
        isOptional = false, isVariadic = false, attributes = emptyList()
    )

    // free
    add(ResolvedIdlOperation(
        name = interfaceFreeCName(classPath, moduleName, this@toOperations),
        type = ResolvedIdlType.Void("void"),
        args = listOf(interfaceArg),
        isStatic = false,
        attributes = emptyList()
    ))
    constructors.forEach { constructor ->
        add(ResolvedIdlOperation(
            name = interfaceConstructorCName(classPath, moduleName, this@toOperations, constructor),
            type = interfaceType,
            args = constructor.args,
            isStatic = false,
            attributes = constructor.attributes
        ))
    }
    operations.forEach { operation ->
        add(ResolvedIdlOperation(
            name = interfaceOperationCName(classPath, moduleName, this@toOperations, operation),
            type = operation.type,
            args = buildList {
                add(interfaceArg)
                addAll(operation.args)
            },
            isStatic = false,
            attributes = operation.attributes
        ))
    }
}

// Names

fun String.snakeCase(): String = buildString {
    this@snakeCase.forEachIndexed { index, c ->

        // for: aaAAAaaA -> aa_aaa_aa_a
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


// Types

fun <T> ResolvedIdlType.Default.arrayType(block: (type: ResolvedIdlType.Default) -> T): T {
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
    return (this as? ResolvedIdlType.Default)?.parameters?.firstOrNull() as? ResolvedIdlType.Default
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
    smallUnsignedTypesAsInt: Boolean = false
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
        isEnum() -> if(enumAsInt) "Int" else declaration.name
        isArray() -> arrayType { type ->
            when {
                type.isPrimitive() -> "${type.toKotlinType(ignoreUnsigned = ignoreUnsigned)}Array$nullable"
                type.isEnum() && enumAsInt -> "IntArray$nullable"
                else -> "Array<${type.toKotlinType(stringAsBytes, enumAsInt)}>$nullable"
            }
        }
        else -> "${(this as ResolvedIdlType.Default).declaration.name.upperCamelCase()}$nullable"
    }
}

fun ResolvedIdlType.toCType(
    enumAsInt: Boolean = false,
    ptr: Boolean = true,
    printNullable: Boolean = false,
    ignoreUnsigned: Boolean = false,
): String {
    val ptr = if(ptr) "*" else ""
    val nullable = if(printNullable) {
        if (isNullable) " _Nullable" else " _Nonnull"
    } else ""
    return when {
        isVoid() -> "void"
        isChar() -> "KChar"
        isBoolean() -> "KBoolean"
        isByte() -> "KByte"
        isUByte() -> if(ignoreUnsigned) "KByte" else "KUByte"
        isShort() -> "KShort"
        isUShort() -> if(ignoreUnsigned) "KShort" else "KUShort"
        isInt() -> "KInt"
        isUInt() -> if(ignoreUnsigned) "KInt" else "KUInt"
        isLong() -> "KLong"
        isULong() -> if(ignoreUnsigned) "KLong" else "KULong"
        isFloat() -> "KFloat"
        isDouble() -> "KDouble"
        isEnum() -> if(enumAsInt) "KInt" else declaration.name
        isString() -> "KString$ptr$nullable"
        isArray() -> arrayType { type ->
            when {
                type.isPrimitive() -> "${type.toCType(ignoreUnsigned = ignoreUnsigned)}Array$ptr$nullable"
                type.isEnum() -> "KIntArray$ptr$nullable"
                else -> "KArray$ptr$nullable"
            }
        }
        isInterface() -> "void*$nullable"
        else -> "${(this as ResolvedIdlType.Default).declaration.name.upperCamelCase()}$ptr$nullable"
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

internal fun castToSignedC(type: ResolvedIdlType, content: String): String = when {
    type.isUByte() -> "(KByte) $content"
    type.isUShort() -> "(KShort) $content"
    type.isUInt() -> "(KInt) $content"
    type.isULong() -> "(KLong) $content"
    type.isArray() -> type.arrayType { type ->
        when {
            type.isUByte() -> "(KByteArray*) $content"
            type.isUShort() -> "(KShortArray*) $content"
            type.isUInt() -> "(KIntArray*) $content"
            type.isULong() -> "(KLongArray*) $content"
            else -> content
        }
    }
    else -> content
}

internal fun castToUnsignedC(type: ResolvedIdlType, content: String): String = when {
    type.isUByte() -> "(KUByte) $content"
    type.isUShort() -> "(KUShort) $content"
    type.isUInt() -> "(KUInt) $content"
    type.isULong() -> "(KULong) $content"
    type.isArray() -> type.arrayType { type ->
        when {
            type.isUByte() -> "(KUByteArray*) $content"
            type.isUShort() -> "(KUShortArray*) $content"
            type.isUInt() -> "(KUIntArray*) $content"
            type.isULong() -> "(KULongArray*) $content"
            else -> content
        }
    }
    else -> content
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

fun ResolvedIdlType.toUnsignedType(): ResolvedIdlType {
    contract {
        returns(true) implies(this@toUnsignedType is ResolvedIdlType.Default)
    }
    if(isUnsigned())
        return this
    if(isArray()) {
        val arrType = arrayTypeOrNull()!!.toUnsignedType()
        return ResolvedIdlType.Default(BuiltinIdlDeclaration(declaration.name, WebIDLBuiltinKind.LIST), listOf(arrType), isNullable)
    }
    val kind = when {
        isByte() -> WebIDLBuiltinKind.UNSIGNED_BYTE
        isShort() -> WebIDLBuiltinKind.UNSIGNED_SHORT
        isInt() -> WebIDLBuiltinKind.UNSIGNED_INT
        isLong() -> WebIDLBuiltinKind.UNSIGNED_LONG
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

fun ResolvedIdlType.isDictionary(): Boolean {
    contract {
        returns(true) implies(this@isDictionary is ResolvedIdlType.Default)
    }
    return this is ResolvedIdlType.Default && declaration is ResolvedIdlDictionary
}

fun ResolvedIdlType.isReleasable(): Boolean =
    isArray() || isString() || isDictionary() || isCallback()

// ==== Arrays =====

fun ResolvedIdlType.isStringArray(): Boolean {
    contract {
        returns(true) implies(this@isStringArray is ResolvedIdlType.Default)
    }
    return arrayTypeOrNull()?.isString() ?: false
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

fun ResolvedIdlType.isLongArray(): Boolean {
    contract {
        returns(true) implies(this@isLongArray is ResolvedIdlType.Default)
    }
    return arrayTypeOrNull()?.isLong() ?: false
}

internal fun ResolvedIdlType.isAnyLongType(): Boolean {
    contract {
        returns(true) implies(this@isAnyLongType is ResolvedIdlType.Default)
    }
    return isLong() || isULong() || isLongArray()
}

internal fun IdlResolver.isUsingLong(): Boolean {
    // operators
    if(globalOperators().any { op ->
        op.type.isAnyLongType() || op.args.any { it.type.isAnyLongType() }
    }) return true

    // callbacks
    if(callbacks.values.any { cb ->
        cb.type.isAnyLongType() || cb.args.any { it.type.isAnyLongType() }
    }) return true

    // dictionaries
    if(dictionaries.values.any { cb ->
        cb.fields.any { it.type.isAnyLongType() }
    }) return true

    return false
}

fun ResolvedIdlOperation.isCritical(): Boolean =
    this.attributes.any {
        it is IdlExtendedAttribute.NoArgs && it.name.lowercase() == "critical"
    }

fun ResolvedIdlOperation.isCriticalCapable(): Boolean =
    !type.isArray() && !type.isString() && !type.isDictionary() &&
            args.all { !it.type.isStringArray() && !it.type.isDictionaryArray() }

// Same as default critical, but without array and string args
fun ResolvedIdlOperation.isAndroidCriticalCapable(): Boolean =
    !type.isArray() && !type.isString() && !type.isDictionary() &&
            args.all { !it.type.isArray() && !it.type.isString() }

fun ResolvedIdlOperation.hasString(): Boolean =
    args.any { it.type.isString() }

fun ResolvedIdlOperation.hasArray(): Boolean =
    args.any { it.type.isArray() }

fun IdlResolver.globalOperators() =
    namespaces.values.flatMap { it.operations }

fun ResolvedIdlDictionary.allFields() = buildList {
    var cur: ResolvedIdlDictionary? = this@allFields
    while(cur != null) {
        addAll(0, cur.fields)
        cur = cur.implements
    }
}

fun functionHeader(
    function: ResolvedIdlOperation,
    isOverride: Boolean = false,
    isPrivate: Boolean = false,
    isActual: Boolean = false,
    isExternal: Boolean = false,
    isExpect: Boolean = false,
    name: String = function.name,
    forceVoid: Boolean = false,
    stringAsBytes: Boolean = false,
    callbackAsAny: Boolean = false
) = StringBuilder().apply {
    printFunctionHeader(this, function, isOverride, isPrivate, isActual, isExternal, isExpect, name, forceVoid, stringAsBytes, callbackAsAny)
}.toString()

fun printFunctionHeader(
    builder: StringBuilder,
    function: ResolvedIdlOperation,
    isOverride: Boolean = false,
    isPrivate: Boolean = false,
    isActual: Boolean = false,
    isExternal: Boolean = false,
    isExpect: Boolean = false,
    name: String = function.name.camelCase(),
    forcePrintVoid: Boolean = false,
    stringAsBytes: Boolean = false,
    enumAsInt: Boolean = false,
    ignoreUnsigned: Boolean = false,
    arraysLen: Boolean = false,
) = builder.apply {
    if(isActual) append("actual ")
    if(isExpect) append("expect ")
    if(isExternal) append("external ")
    if(isPrivate) append("private ")
    if(isOverride) append("override ")

    val args = function.args.flatMap { arg ->
        val name = arg.name.camelCase()
        val result = "$name: ${arg.type.toKotlinType(stringAsBytes, enumAsInt, ignoreUnsigned = ignoreUnsigned)}"
        when {
            stringAsBytes && arg.type.isString() ->
                listOf(result, "__len_$name: Int", "__size_$name: Int")
            arraysLen && arg.type.isArray() ->
                listOf(result, "__len_$name: Int")
            else -> listOf(result)
        }
    }.joinToString()

    append("fun $name($args)")

    if(forcePrintVoid || function.type !is ResolvedIdlType.Void)
        append(": ${function.type.toKotlinType(stringAsBytes, enumAsInt, ignoreUnsigned = ignoreUnsigned)}")
}

fun printLabel(builder: StringBuilder, text: String, indent: Int = 5) = builder.apply {
    // line 1
    append("\n// ╔")
    append("═".repeat(text.length + indent*2))
    append("╗\n")

    // line 2
    append("// ║")
    append(" ".repeat(indent))
    append(text)
    append(" ".repeat(indent))
    append("║\n")

    // line 3
    append("// ╚")
    append("═".repeat(text.length + indent*2))
    append("╝\n")
}