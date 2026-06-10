package com.huskerdev.nativekt.utils

import com.huskerdev.webidl.parser.IdlAttributedHolder
import com.huskerdev.webidl.parser.IdlExtendedAttribute
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized
import kotlin.contracts.ExperimentalContracts

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
    if (this !is ResolvedIdlType.Default)
        return null
    val param = parameters.firstOrNull()
        ?: return null
    val type = param as? ResolvedIdlType.Default
        ?: return null
    return type
}

@OptIn(ExperimentalContracts::class)
fun ResolvedIdlType.builtinOrNull(): BuiltinIdlDeclaration? {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return null
    return declaration as BuiltinIdlDeclaration
}

internal fun WebIDLBuiltinKind.simpleName() = when(this) {
    WebIDLBuiltinKind.CHAR -> "Char"
    WebIDLBuiltinKind.BOOLEAN -> "Boolean"
    WebIDLBuiltinKind.BYTE -> "Byte"
    WebIDLBuiltinKind.SHORT -> "Short"
    WebIDLBuiltinKind.INT -> "Int"
    WebIDLBuiltinKind.LONG -> "Long"
    WebIDLBuiltinKind.FLOAT -> "Float"
    WebIDLBuiltinKind.DOUBLE -> "Double"
    else -> throw UnsupportedOperationException(toString())
}

fun ResolvedIdlType.toKotlinType(
    stringAsBytes: Boolean = false,
    enumAsInt: Boolean = false
): String = when(this) {
    is ResolvedIdlType.Void -> "Unit"
    is ResolvedIdlType.Default -> when(val declaration = declaration) {
        is BuiltinIdlDeclaration -> when(declaration.kind) {
            WebIDLBuiltinKind.CHAR -> "Char"
            WebIDLBuiltinKind.BOOLEAN -> "Boolean"
            WebIDLBuiltinKind.BYTE -> "Byte"
            WebIDLBuiltinKind.SHORT -> "Short"
            WebIDLBuiltinKind.INT -> "Int"
            WebIDLBuiltinKind.LONG -> "Long"
            WebIDLBuiltinKind.FLOAT -> "Float"
            WebIDLBuiltinKind.DOUBLE -> "Double"
            WebIDLBuiltinKind.STRING -> if(stringAsBytes) "ByteArray" else "String"
            WebIDLBuiltinKind.LIST -> arrayType { type ->
                when (val declaration = type.declaration) {
                    is BuiltinIdlDeclaration -> when (declaration.kind) {
                        WebIDLBuiltinKind.CHAR -> "CharArray"
                        WebIDLBuiltinKind.BOOLEAN -> "BooleanArray"
                        WebIDLBuiltinKind.BYTE -> "ByteArray"
                        WebIDLBuiltinKind.SHORT -> "ShortArray"
                        WebIDLBuiltinKind.INT -> "IntArray"
                        WebIDLBuiltinKind.LONG -> "LongArray"
                        WebIDLBuiltinKind.FLOAT -> "FloatArray"
                        WebIDLBuiltinKind.DOUBLE -> "DoubleArray"
                        else -> "Array<${type.toKotlinType(stringAsBytes, enumAsInt)}>"
                    }
                    is ResolvedIdlEnum -> if (enumAsInt) "IntArray" else "Array<${declaration.name}>"
                    else -> "Array<${type.toKotlinType(stringAsBytes, enumAsInt)}>"
                }
            }
            else -> throw UnsupportedOperationException(toString())
        }
        is ResolvedIdlEnum -> if(enumAsInt) "Int" else declaration.name
        is ResolvedIdlCallbackFunction,
        is ResolvedIdlDictionary -> declaration.name
        else -> declaration.name
    }
    else -> throw UnsupportedOperationException(toString())
}

fun ResolvedIdlType.toCDefType(
    enumAsInt: Boolean = false,
    ptr: Boolean = true
): String {
    val ptr = if(ptr) "*" else ""
    return when(this) {
        is ResolvedIdlType.Void -> "void"
        is ResolvedIdlType.Default -> when(val declaration = declaration) {
            is BuiltinIdlDeclaration -> when(declaration.kind) {
                WebIDLBuiltinKind.CHAR -> "KChar"
                WebIDLBuiltinKind.BOOLEAN -> "KBoolean"
                WebIDLBuiltinKind.BYTE -> "KByte"
                WebIDLBuiltinKind.SHORT -> "KShort"
                WebIDLBuiltinKind.INT -> "KInt"
                WebIDLBuiltinKind.LONG -> "KLong"
                WebIDLBuiltinKind.FLOAT -> "KFloat"
                WebIDLBuiltinKind.DOUBLE -> "KDouble"
                WebIDLBuiltinKind.STRING -> "KString$ptr"
                WebIDLBuiltinKind.LIST -> arrayType { type ->
                    when (val declaration = type.declaration) {
                        is BuiltinIdlDeclaration -> when (declaration.kind) {
                            WebIDLBuiltinKind.CHAR -> "KCharArray$ptr"
                            WebIDLBuiltinKind.BOOLEAN -> "KBooleanArray$ptr"
                            WebIDLBuiltinKind.BYTE -> "KByteArray$ptr"
                            WebIDLBuiltinKind.SHORT -> "KShortArray$ptr"
                            WebIDLBuiltinKind.INT -> "KIntArray$ptr"
                            WebIDLBuiltinKind.LONG -> "KLongArray$ptr"
                            WebIDLBuiltinKind.FLOAT -> "KFloatArray$ptr"
                            WebIDLBuiltinKind.DOUBLE -> "KDoubleArray$ptr"
                            WebIDLBuiltinKind.STRING -> "KStringArray$ptr"
                            else -> throw UnsupportedOperationException()
                        }
                        is ResolvedIdlEnum -> "KIntArray$ptr"
                        is ResolvedIdlDictionary -> "KArray$ptr"
                        else -> throw UnsupportedOperationException(declaration.name)
                    }
                }
                else -> throw UnsupportedOperationException(toString())
            }
            is ResolvedIdlEnum -> if(enumAsInt) "KInt" else declaration.name
            is ResolvedIdlCallbackFunction,
            is ResolvedIdlDictionary -> "${declaration.name}$ptr"
            else -> throw UnsupportedOperationException(declaration.name)
        }
        else -> throw UnsupportedOperationException(toString())
    }
}

fun ResolvedIdlType.toJNIType(): String = when(this) {
    is ResolvedIdlType.Void -> "void"
    is ResolvedIdlType.Default -> when(val declaration = declaration) {
        is BuiltinIdlDeclaration -> when(declaration.kind) {
            WebIDLBuiltinKind.CHAR -> "jchar"
            WebIDLBuiltinKind.BOOLEAN -> "jboolean"
            WebIDLBuiltinKind.BYTE -> "jbyte"
            WebIDLBuiltinKind.SHORT -> "jshort"
            WebIDLBuiltinKind.INT -> "jint"
            WebIDLBuiltinKind.LONG -> "jlong"
            WebIDLBuiltinKind.FLOAT -> "jfloat"
            WebIDLBuiltinKind.DOUBLE -> "jdouble"
            WebIDLBuiltinKind.STRING -> "jstring"
            WebIDLBuiltinKind.LIST -> arrayType { type ->
                when (val declaration = type.declaration) {
                    is BuiltinIdlDeclaration -> when (declaration.kind) {
                        WebIDLBuiltinKind.CHAR -> "jcharArray"
                        WebIDLBuiltinKind.BOOLEAN -> "jbooleanArray"
                        WebIDLBuiltinKind.BYTE -> "jbyteArray"
                        WebIDLBuiltinKind.SHORT -> "jshortArray"
                        WebIDLBuiltinKind.INT -> "jintArray"
                        WebIDLBuiltinKind.LONG -> "jlongArray"
                        WebIDLBuiltinKind.FLOAT -> "jfloatArray"
                        WebIDLBuiltinKind.DOUBLE -> "jdoubleArray"
                        else -> "jobjectArray"
                    }
                    else -> "jobjectArray"
                }
            }
            else -> throw UnsupportedOperationException(toString())
        }
        else -> "jobject"
    }
    else -> throw UnsupportedOperationException(toString())
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
): String = when(this) {
    is ResolvedIdlType.Void -> "V"
    is ResolvedIdlType.Default -> when(val declaration = declaration) {
        is BuiltinIdlDeclaration -> when(declaration.kind) {
            WebIDLBuiltinKind.CHAR -> "C"
            WebIDLBuiltinKind.BOOLEAN -> "Z"
            WebIDLBuiltinKind.BYTE -> "B"
            WebIDLBuiltinKind.SHORT -> "S"
            WebIDLBuiltinKind.INT -> "I"
            WebIDLBuiltinKind.LONG -> "J"
            WebIDLBuiltinKind.FLOAT -> "F"
            WebIDLBuiltinKind.DOUBLE -> "D"
            WebIDLBuiltinKind.STRING -> if(isCritical) "[B" else "Ljava/lang/String;"
            WebIDLBuiltinKind.LIST -> arrayType { type ->
                "[${type.toJavaDesc(classpath, isCritical)}"
            }
            else -> throw UnsupportedOperationException(toString())
        }
        is ResolvedIdlEnum if (isCritical) -> "I"
        is ResolvedIdlEnum,
        is ResolvedIdlDictionary,
        is ResolvedIdlCallbackFunction -> "L${classpath.replace(".", "/")}/${declaration.name};"
        else -> "Ljava/lang/Object;"
    }
    else -> throw UnsupportedOperationException(toString())
}

// ===== Simple types ======

fun ResolvedIdlType.isPrimitive(): Boolean =
    builtinOrNull()?.kind in setOf(
        WebIDLBuiltinKind.CHAR,
        WebIDLBuiltinKind.BOOLEAN,
        WebIDLBuiltinKind.BYTE,
        WebIDLBuiltinKind.SHORT,
        WebIDLBuiltinKind.INT,
        WebIDLBuiltinKind.LONG,
        WebIDLBuiltinKind.FLOAT,
        WebIDLBuiltinKind.DOUBLE,
    )

fun ResolvedIdlType.isString(): Boolean =
    builtinOrNull()?.kind == WebIDLBuiltinKind.STRING

fun ResolvedIdlType.isLong(): Boolean =
    builtinOrNull()?.kind == WebIDLBuiltinKind.LONG

fun ResolvedIdlType.isInt(): Boolean =
    builtinOrNull()?.kind == WebIDLBuiltinKind.INT

fun ResolvedIdlType.isDouble(): Boolean =
    builtinOrNull()?.kind == WebIDLBuiltinKind.DOUBLE

fun ResolvedIdlType.isFloat(): Boolean =
    builtinOrNull()?.kind == WebIDLBuiltinKind.FLOAT

fun ResolvedIdlType.isBoolean(): Boolean =
    builtinOrNull()?.kind == WebIDLBuiltinKind.BOOLEAN

fun ResolvedIdlType.isShort(): Boolean =
    builtinOrNull()?.kind == WebIDLBuiltinKind.SHORT

fun ResolvedIdlType.isByte(): Boolean =
    builtinOrNull()?.kind == WebIDLBuiltinKind.BYTE

fun ResolvedIdlType.isChar(): Boolean =
    builtinOrNull()?.kind == WebIDLBuiltinKind.CHAR

fun ResolvedIdlType.isArray(): Boolean =
    builtinOrNull()?.kind == WebIDLBuiltinKind.LIST

fun ResolvedIdlType.isCallback(): Boolean =
    this is ResolvedIdlType.Default && declaration is ResolvedIdlCallbackFunction

fun ResolvedIdlType.isEnum(): Boolean =
    this is ResolvedIdlType.Default && declaration is ResolvedIdlEnum

fun ResolvedIdlType.isDictionary(): Boolean =
    this is ResolvedIdlType.Default && declaration is ResolvedIdlDictionary

// ==== Arrays =====

fun ResolvedIdlType.isStringArray(): Boolean =
    arrayTypeOrNull()?.isString() ?: false

fun ResolvedIdlType.isEnumArray(): Boolean =
    arrayTypeOrNull()?.isEnum() ?: false

fun ResolvedIdlType.isDictionaryArray(): Boolean =
    arrayTypeOrNull()?.isDictionary() ?: false

fun ResolvedIdlType.isBooleanArray(): Boolean =
    arrayTypeOrNull()?.isBoolean() ?: false

fun ResolvedIdlType.isLongArray(): Boolean =
    arrayTypeOrNull()?.isLong() ?: false

internal fun ResolvedIdlType.isAnyLongType(): Boolean =
    isLong() || isLongArray ()

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