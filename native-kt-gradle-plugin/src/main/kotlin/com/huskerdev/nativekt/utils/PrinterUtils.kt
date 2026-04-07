package com.huskerdev.nativekt.utils

import com.huskerdev.webidl.parser.IdlAttributedHolder
import com.huskerdev.webidl.parser.IdlExtendedAttribute
import com.huskerdev.webidl.resolver.BuiltinIdlDeclaration
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlCallbackFunction
import com.huskerdev.webidl.resolver.ResolvedIdlDeclaration
import com.huskerdev.webidl.resolver.ResolvedIdlDictionary
import com.huskerdev.webidl.resolver.ResolvedIdlEnum
import com.huskerdev.webidl.resolver.ResolvedIdlField
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind
import org.gradle.internal.extensions.stdlib.capitalized
import kotlin.math.ceil
import kotlin.math.max

fun asyncFunctionName(moduleName: String) =
    "loadLib${moduleName.capitalized()}"

fun syncFunctionName(moduleName: String) =
    "loadLib${moduleName.capitalized()}Sync"

fun ResolvedIdlType.toKotlinForeignType(): String {
    return if(isCallback() || isString() || isArray() || isDictionary())
        "MemorySegment"
    else toKotlinType(enumAsInt = true)
}

fun <T> ResolvedIdlType.Default.firstParam(block: (type: ResolvedIdlType.Default, declaration: ResolvedIdlDeclaration) -> T): T {
    val param = parameters.firstOrNull()
        ?: throw UnsupportedOperationException("Array without type")
    val type = param as? ResolvedIdlType.Default
        ?: throw UnsupportedOperationException("Unsupported array type: $param")
    return block(type, type.declaration)
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
    callbackAsAny: Boolean = false,
    enumAsInt: Boolean = false,
    dictionaryAsAny: Boolean = false
): String = when(this) {
    is ResolvedIdlType.Void -> "Unit"
    is ResolvedIdlType.Default -> when(declaration) {
        is BuiltinIdlDeclaration -> when((declaration as BuiltinIdlDeclaration).kind) {
            WebIDLBuiltinKind.CHAR -> "Char"
            WebIDLBuiltinKind.BOOLEAN -> "Boolean"
            WebIDLBuiltinKind.BYTE -> "Byte"
            WebIDLBuiltinKind.SHORT -> "Short"
            WebIDLBuiltinKind.INT -> "Int"
            WebIDLBuiltinKind.LONG -> "Long"
            WebIDLBuiltinKind.FLOAT -> "Float"
            WebIDLBuiltinKind.DOUBLE -> "Double"
            WebIDLBuiltinKind.STRING -> if(stringAsBytes) "ByteArray" else "String"
            WebIDLBuiltinKind.LIST -> firstParam { type, declaration ->
                when (declaration) {
                    is BuiltinIdlDeclaration -> when (declaration.kind) {
                        WebIDLBuiltinKind.CHAR -> "CharArray"
                        WebIDLBuiltinKind.BOOLEAN -> "BooleanArray"
                        WebIDLBuiltinKind.BYTE -> "ByteArray"
                        WebIDLBuiltinKind.SHORT -> "ShortArray"
                        WebIDLBuiltinKind.INT -> "IntArray"
                        WebIDLBuiltinKind.LONG -> "LongArray"
                        WebIDLBuiltinKind.FLOAT -> "FloatArray"
                        WebIDLBuiltinKind.DOUBLE -> "DoubleArray"
                        else -> "Array<${type.toKotlinType(stringAsBytes, callbackAsAny, enumAsInt, dictionaryAsAny)}>"
                    }
                    is ResolvedIdlEnum -> if (enumAsInt)
                        "IntArray" else "Array<${declaration.name}>"
                    else -> "Array<${type.toKotlinType(stringAsBytes, callbackAsAny, enumAsInt, dictionaryAsAny)}>"
                }
            }
            else -> throw UnsupportedOperationException(toString())
        }
        is ResolvedIdlEnum -> if(enumAsInt) "Int" else declaration.name
        is ResolvedIdlCallbackFunction -> if(callbackAsAny) "Any" else declaration.name
        is ResolvedIdlDictionary -> if(dictionaryAsAny) "Any" else declaration.name
        else -> declaration.name
    }
    else -> throw UnsupportedOperationException(toString())
}

fun ResolvedIdlType.toCType(
    longPtr: Boolean = false,
    constChar: Boolean = true,
    callbackAsPtr: Boolean = false,
    dictionaryAsPtr: Boolean = false
): String = when(this) {
    is ResolvedIdlType.Void -> "void"
    is ResolvedIdlType.Default -> when(declaration) {
        is BuiltinIdlDeclaration -> when((declaration as BuiltinIdlDeclaration).kind) {
            WebIDLBuiltinKind.CHAR -> "uint16_t"
            WebIDLBuiltinKind.BOOLEAN -> "bool"
            WebIDLBuiltinKind.BYTE -> "int8_t"
            WebIDLBuiltinKind.SHORT -> "int16_t"
            WebIDLBuiltinKind.INT -> "int32_t"
            WebIDLBuiltinKind.LONG -> "int64_t${if(longPtr) "*" else ""}"
            WebIDLBuiltinKind.FLOAT -> "float"
            WebIDLBuiltinKind.DOUBLE -> "double"
            WebIDLBuiltinKind.STRING -> "${if(constChar) "const " else ""}char*"
            WebIDLBuiltinKind.LIST -> firstParam { type, _ ->
                "${type.toCType()}*"
            }
            else -> throw UnsupportedOperationException(toString())
        }
        is ResolvedIdlEnum -> "${declaration.name}*"
        is ResolvedIdlCallbackFunction ->
            if(callbackAsPtr) "intptr_t"
            else "${declaration.name}*"
        is ResolvedIdlDictionary ->
            if(dictionaryAsPtr) "intptr_t"
            else "${declaration.name}*"
        else -> "${declaration.name}*"
    }
    else -> throw UnsupportedOperationException(toString())
}

fun ResolvedIdlType.toCDefType(
    longPtr: Boolean = false,
    enumAsInt: Boolean = false
): String = when(this) {
    is ResolvedIdlType.Void -> "void"
    is ResolvedIdlType.Default -> when(declaration) {
        is BuiltinIdlDeclaration -> when((declaration as BuiltinIdlDeclaration).kind) {
            WebIDLBuiltinKind.CHAR -> "KChar"
            WebIDLBuiltinKind.BOOLEAN -> "KBoolean"
            WebIDLBuiltinKind.BYTE -> "KByte"
            WebIDLBuiltinKind.SHORT -> "KShort"
            WebIDLBuiltinKind.INT -> "KInt"
            WebIDLBuiltinKind.LONG -> "KLong${if (longPtr) "*" else ""}"
            WebIDLBuiltinKind.FLOAT -> "KFloat"
            WebIDLBuiltinKind.DOUBLE -> "KDouble"
            WebIDLBuiltinKind.STRING -> "KString${if (longPtr) "*" else ""}"
            WebIDLBuiltinKind.LIST -> firstParam { _, declaration ->
                when (declaration) {
                    is BuiltinIdlDeclaration -> when (declaration.kind) {
                        WebIDLBuiltinKind.CHAR -> "KCharArray"
                        WebIDLBuiltinKind.BOOLEAN -> "KBooleanArray"
                        WebIDLBuiltinKind.BYTE -> "KByteArray"
                        WebIDLBuiltinKind.SHORT -> "KShortArray"
                        WebIDLBuiltinKind.INT -> "KIntArray"
                        WebIDLBuiltinKind.LONG -> "KLongArray"
                        WebIDLBuiltinKind.FLOAT -> "KFloatArray"
                        WebIDLBuiltinKind.DOUBLE -> "KDoubleArray"
                        WebIDLBuiltinKind.STRING -> "KStringArray"
                        else -> throw UnsupportedOperationException()
                    }
                    is ResolvedIdlEnum -> "KIntArray"
                    is ResolvedIdlDictionary -> "KArray"
                    else -> throw UnsupportedOperationException(declaration.name)
                }
            }
            else -> throw UnsupportedOperationException(toString())
        }
        is ResolvedIdlEnum -> if(enumAsInt) "KInt" else declaration.name
        is ResolvedIdlCallbackFunction -> "${declaration.name}*"
        is ResolvedIdlDictionary -> "${declaration.name}*"
        else -> throw UnsupportedOperationException(declaration.name)
    }
    else -> throw UnsupportedOperationException(toString())
}


fun ResolvedIdlType.toJNIType(): String = when(this) {
    is ResolvedIdlType.Void -> "void"
    is ResolvedIdlType.Default -> when(declaration) {
        is BuiltinIdlDeclaration -> when((declaration as BuiltinIdlDeclaration).kind) {
            WebIDLBuiltinKind.CHAR -> "jchar"
            WebIDLBuiltinKind.BOOLEAN -> "jboolean"
            WebIDLBuiltinKind.BYTE -> "jbyte"
            WebIDLBuiltinKind.SHORT -> "jshort"
            WebIDLBuiltinKind.INT -> "jint"
            WebIDLBuiltinKind.LONG -> "jlong"
            WebIDLBuiltinKind.FLOAT -> "jfloat"
            WebIDLBuiltinKind.DOUBLE -> "jdouble"
            WebIDLBuiltinKind.STRING -> "jstring"
            WebIDLBuiltinKind.LIST -> firstParam { _, declaration ->
                if(declaration is BuiltinIdlDeclaration) when(declaration.kind) {
                    WebIDLBuiltinKind.CHAR -> "jcharArray"
                    WebIDLBuiltinKind.BOOLEAN -> "jbooleanArray"
                    WebIDLBuiltinKind.BYTE -> "jbyteArray"
                    WebIDLBuiltinKind.SHORT -> "jshortArray"
                    WebIDLBuiltinKind.INT -> "jintArray"
                    WebIDLBuiltinKind.LONG -> "jlongArray"
                    WebIDLBuiltinKind.FLOAT -> "jfloatArray"
                    WebIDLBuiltinKind.DOUBLE -> "jdoubleArray"
                    else -> "jobjectArray"
                } else "jobjectArray"
            }
            else -> throw UnsupportedOperationException(toString())
        }
        is ResolvedIdlEnum -> "jint"
        else -> "jobject"
    }
    else -> throw UnsupportedOperationException(toString())
}

fun ResolvedIdlType.toJavaDesc(): String = when(this) {
    is ResolvedIdlType.Void -> "V"
    is ResolvedIdlType.Default -> when(declaration) {
        is BuiltinIdlDeclaration -> when((declaration as BuiltinIdlDeclaration).kind) {
            WebIDLBuiltinKind.CHAR -> "C"
            WebIDLBuiltinKind.BOOLEAN -> "Z"
            WebIDLBuiltinKind.BYTE -> "B"
            WebIDLBuiltinKind.SHORT -> "S"
            WebIDLBuiltinKind.INT -> "I"
            WebIDLBuiltinKind.LONG -> "J"
            WebIDLBuiltinKind.FLOAT -> "F"
            WebIDLBuiltinKind.DOUBLE -> "D"
            WebIDLBuiltinKind.STRING -> "Ljava/lang/String;"
            WebIDLBuiltinKind.LIST -> firstParam { type, _ ->
                "[${type.toJavaDesc()}"
            }
            else -> throw UnsupportedOperationException(toString())
        }
        is ResolvedIdlEnum -> "I"
        else -> "Ljava/lang/Object;"
    }
    else -> throw UnsupportedOperationException(toString())
}

fun ResolvedIdlDictionary.calcMem(): Pair<Int, Int> {
    var sum = 0.0
    var max = 0.0
    allFields().forEach {
        val cur = it.type.getAlignment().toDouble()
        sum = (cur * ceil(sum / cur)) + cur
        max = max(max, cur)
    }
    return Pair(sum.toInt(), max.toInt())
}

fun ResolvedIdlType.getAlignment(): Int = when(this) {
    is ResolvedIdlType.Union,
    is ResolvedIdlType.Void -> throw UnsupportedOperationException()
    is ResolvedIdlType.Default -> when(declaration) {
        is BuiltinIdlDeclaration -> when(val a = (declaration as BuiltinIdlDeclaration).kind) {
            WebIDLBuiltinKind.CHAR -> 2
            WebIDLBuiltinKind.BOOLEAN -> 1
            WebIDLBuiltinKind.BYTE -> 1
            WebIDLBuiltinKind.SHORT -> 2
            WebIDLBuiltinKind.INT -> 4
            WebIDLBuiltinKind.LONG -> 8
            WebIDLBuiltinKind.FLOAT -> 4
            WebIDLBuiltinKind.DOUBLE -> 8
            WebIDLBuiltinKind.STRING -> 8
            WebIDLBuiltinKind.LIST -> 8
            else -> throw UnsupportedOperationException(a.toString())
        }
        is ResolvedIdlEnum -> 4
        else -> 8
    }
}



fun ResolvedIdlType.isString(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    return (declaration as BuiltinIdlDeclaration).kind == WebIDLBuiltinKind.STRING
}

fun ResolvedIdlType.isLong(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    return (declaration as BuiltinIdlDeclaration).kind == WebIDLBuiltinKind.LONG
}

fun ResolvedIdlType.isArray(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    return (declaration as BuiltinIdlDeclaration).kind == WebIDLBuiltinKind.LIST
}

fun ResolvedIdlType.isStringArray(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    if((declaration as BuiltinIdlDeclaration).kind != WebIDLBuiltinKind.LIST)
        return false
    return firstParam { type, _ -> type.isString() }
}

fun ResolvedIdlType.isCallback(): Boolean =
    this is ResolvedIdlType.Default && declaration is ResolvedIdlCallbackFunction

fun ResolvedIdlType.isEnum(): Boolean =
    this is ResolvedIdlType.Default && declaration is ResolvedIdlEnum

fun ResolvedIdlType.isEnumArray(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    if((declaration as BuiltinIdlDeclaration).kind != WebIDLBuiltinKind.LIST)
        return false
    return firstParam { type, _ -> type.isEnum() }
}

fun ResolvedIdlType.isDictionary(): Boolean =
    this is ResolvedIdlType.Default && declaration is ResolvedIdlDictionary

fun ResolvedIdlType.isDictionaryArray(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    if((declaration as BuiltinIdlDeclaration).kind != WebIDLBuiltinKind.LIST)
        return false
    return firstParam { type, _ -> type.isDictionary() }
}

fun ResolvedIdlOperation.isCritical(): Boolean =
    this.attributes.any {
        it is IdlExtendedAttribute.NoArgs && it.name == "Critical"
    }

fun IdlAttributedHolder.isDealloc(): Boolean =
    this.attributes.any {
        it is IdlExtendedAttribute.NoArgs && it.name == "Dealloc"
    }

fun IdlAttributedHolder.isDeallocContent(): Boolean =
    this.attributes.any {
        it is IdlExtendedAttribute.NoArgs && it.name == "DeallocContent"
    }

fun ResolvedIdlOperation.isCriticalCapable(): Boolean =
    !type.isArray() && !type.isString() && !type.isDictionary() &&
            !args.any { it.type.isStringArray() || it.type.isDictionaryArray() }

fun ResolvedIdlOperation.hasString(): Boolean =
    args.any { it.type.isString() }

fun ResolvedIdlOperation.hasArray(): Boolean =
    args.any { it.type.isArray() }

fun IdlResolver.globalOperators() =
    namespaces.values.flatMap { it.operations }

fun ResolvedIdlDictionary.allFields(): List<ResolvedIdlField.Declaration> {
    val result = arrayListOf<ResolvedIdlField.Declaration>()
    var curDict: ResolvedIdlDictionary? = this
    while(curDict != null) {
        result.addAll(0, curDict.fields)
        curDict = curDict.implements
    }
    return result
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
    callbackAsAny: Boolean = false,
    enumAsInt: Boolean = false,
    dictionaryAsAny: Boolean = false,
    arraysLen: Boolean = false,
) = builder.apply {
    if(isActual) append("actual ")
    if(isExpect) append("expect ")
    if(isExternal) append("external ")
    if(isOverride) append("override ")

    append("fun ")
    append(name)
    append("(")

    function.args.forEachIndexed { index, arg ->
        append(arg.name)
        append(": ")
        append(arg.type.toKotlinType(stringAsBytes, callbackAsAny, enumAsInt, dictionaryAsAny))

        if((stringAsBytes && arg.type.isString()) || (arraysLen && arg.type.isArray())) {
            append(", __len_")
            append(arg.name)
            append(": Int")
        }

        if(index != function.args.lastIndex)
            append(", ")
    }
    append(")")
    if(forcePrintVoid || function.type !is ResolvedIdlType.Void) {
        append(": ")
        append(function.type.toKotlinType(stringAsBytes, callbackAsAny, enumAsInt, dictionaryAsAny))
    }
}

fun printLabel(builder: StringBuilder, text: String) = builder.apply {
    val indent = 5

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