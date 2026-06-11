@file:OptIn(ExperimentalContracts::class)

package com.huskerdev.nativekt.utils

import com.huskerdev.webidl.parser.IdlAttributedHolder
import com.huskerdev.webidl.parser.IdlExtendedAttribute
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun asyncLoadFunctionName(moduleName: String) =
    "loadLib${moduleName.capitalized()}"

fun syncLoadFunctionName(moduleName: String) =
    "loadLib${moduleName.capitalized()}Sync"

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
    enumAsInt: Boolean = false
): String = when {
    isVoid() -> "Unit"
    isChar() -> "Char"
    isBoolean() -> "Boolean"
    isByte() -> "Byte"
    isShort() -> "Short"
    isInt() -> "Int"
    isLong() -> "Long"
    isFloat() -> "Float"
    isDouble() -> "Double"
    isString() -> if(stringAsBytes) "ByteArray" else "String"
    isEnum() -> if(enumAsInt) "Int" else declaration.name
    isArray() -> arrayType { type ->
        when {
            type.isPrimitive() -> "${type.toKotlinType()}Array"
            type.isEnum() && enumAsInt -> "IntArray"
            else -> "Array<${type.toKotlinType(stringAsBytes, enumAsInt)}>"
        }
    }
    else -> (this as ResolvedIdlType.Default).declaration.name
}

fun ResolvedIdlType.toCType(
    enumAsInt: Boolean = false,
    ptr: Boolean = true
): String {
    val ptr = if(ptr) "*" else ""
    return when {
        isVoid() -> "void"
        isChar() -> "KChar"
        isBoolean() -> "KBoolean"
        isByte() -> "KByte"
        isShort() -> "KShort"
        isInt() -> "KInt"
        isLong() -> "KLong"
        isFloat() -> "KFloat"
        isDouble() -> "KDouble"
        isEnum() -> if(enumAsInt) "KInt" else declaration.name
        isString() -> "KString$ptr"
        isArray() -> arrayType { type ->
            when {
                type.isPrimitive() -> "${type.toCType()}Array$ptr"
                type.isEnum() -> "KIntArray$ptr"
                else -> "KArray$ptr"
            }
        }
        else -> "${(this as ResolvedIdlType.Default).declaration.name}$ptr"
    }
}

fun ResolvedIdlType.toJNIType(): String = when {
    isVoid() -> "void"
    isChar() -> "jchar"
    isBoolean() -> "jboolean"
    isByte() -> "jbyte"
    isShort() -> "jshort"
    isInt() -> "jint"
    isLong() -> "jlong"
    isFloat() -> "jfloat"
    isDouble() -> "jdouble"
    isString() -> "jstring"
    isArray() -> arrayType { type ->
        when {
            isPrimitive() -> "${type.toJNIType()}Array"
            else -> "jobjectArray"
        }
    }
    else -> "jobject"
}

fun ResolvedIdlOperation.toJavaDesc(
    classpath: String,
    isCritical: Boolean = false
): String = buildString {
    args.joinTo(this, "", prefix = "(", postfix = ")") {
        it.type.toJavaDesc(classpath, isCritical)
    }
    append(type.toJavaDesc(classpath, isCritical))
}

fun ResolvedIdlType.toJavaDesc(
    classpath: String,
    isCritical: Boolean = false
): String = when {
    isVoid() -> "V"
    isChar() -> "C"
    isBoolean() -> "Z"
    isByte() -> "B"
    isShort() -> "S"
    isInt() -> "I"
    isLong() -> "J"
    isFloat() -> "F"
    isDouble() -> "D"
    isString() -> if(isCritical) "[B" else "Ljava/lang/String;"
    isEnum() && isCritical -> "I"
    isArray() -> arrayType { type ->
        "[${type.toJavaDesc(classpath, isCritical)}"
    }
    else -> "L${classpath.replace(".", "/")}/${(this as ResolvedIdlType.Default).declaration.name};"
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
        WebIDLBuiltinKind.SHORT,
        WebIDLBuiltinKind.INT,
        WebIDLBuiltinKind.LONG,
        WebIDLBuiltinKind.FLOAT,
        WebIDLBuiltinKind.DOUBLE,
    )
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

fun ResolvedIdlType.isInt(): Boolean {
    contract {
        returns(true) implies(this@isInt is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.INT
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

fun ResolvedIdlType.isByte(): Boolean {
    contract {
        returns(true) implies(this@isByte is ResolvedIdlType.Default)
    }
    return builtinOrNull()?.kind == WebIDLBuiltinKind.BYTE
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

fun ResolvedIdlType.isDictionary(): Boolean {
    contract {
        returns(true) implies(this@isDictionary is ResolvedIdlType.Default)
    }
    return this is ResolvedIdlType.Default && declaration is ResolvedIdlDictionary
}

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

fun ResolvedIdlType.isBooleanArray(): Boolean {
    contract {
        returns(true) implies(this@isBooleanArray is ResolvedIdlType.Default)
    }
    return arrayTypeOrNull()?.isBoolean() ?: false
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
    return isLong() || isLongArray()
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

    return false
}

fun ResolvedIdlOperation.isCritical(): Boolean =
    this.attributes.any {
        it is IdlExtendedAttribute.NoArgs && it.name == "Critical"
    }

fun IdlAttributedHolder.isDealloc(): Boolean =
    this.attributes.any {
        it is IdlExtendedAttribute.NoArgs && it.name == "Dealloc"
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
    isActual: Boolean = false,
    isExternal: Boolean = false,
    isExpect: Boolean = false,
    name: String = function.name,
    forceVoid: Boolean = false,
    stringAsBytes: Boolean = false,
    callbackAsAny: Boolean = false
) = StringBuilder().apply {
    printFunctionHeader(this, function, isOverride, isActual, isExternal, isExpect, name, forceVoid, stringAsBytes, callbackAsAny)
}.toString()

fun printFunctionHeader(
    builder: StringBuilder,
    function: ResolvedIdlOperation,
    isOverride: Boolean = false,
    isActual: Boolean = false,
    isExternal: Boolean = false,
    isExpect: Boolean = false,
    name: String = function.name,
    forcePrintVoid: Boolean = false,
    stringAsBytes: Boolean = false,
    enumAsInt: Boolean = false,
    arraysLen: Boolean = false,
) = builder.apply {
    if(isActual) append("actual ")
    if(isExpect) append("expect ")
    if(isExternal) append("external ")
    if(isOverride) append("override ")

    val args = function.args.flatMap { arg ->
        val result = "${arg.name}: ${arg.type.toKotlinType(stringAsBytes, enumAsInt)}"
        when {
            stringAsBytes && arg.type.isString() ->
                listOf(result, "__len_${arg.name}: Int", "__size_${arg.name}: Int")
            arraysLen && arg.type.isArray() ->
                listOf(result, "__len_${arg.name}: Int")
            else -> listOf(result)
        }
    }.joinToString()

    append("fun $name($args)")

    if(forcePrintVoid || function.type !is ResolvedIdlType.Void)
        append(": ${function.type.toKotlinType(stringAsBytes, enumAsInt)}")
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