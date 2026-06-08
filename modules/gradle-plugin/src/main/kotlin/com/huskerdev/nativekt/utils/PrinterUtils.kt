package com.huskerdev.nativekt.utils

import com.huskerdev.webidl.parser.IdlAttributedHolder
import com.huskerdev.webidl.parser.IdlExtendedAttribute
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized

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
    enumAsInt: Boolean = false,
    ptr: Boolean = true
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
            WebIDLBuiltinKind.STRING -> "KString${if(ptr) "*" else ""}"
            WebIDLBuiltinKind.LIST -> firstParam { _, declaration ->
                when (declaration) {
                    is BuiltinIdlDeclaration -> when (declaration.kind) {
                        WebIDLBuiltinKind.CHAR -> "KCharArray${if(ptr) "*" else ""}"
                        WebIDLBuiltinKind.BOOLEAN -> "KBooleanArray${if(ptr) "*" else ""}"
                        WebIDLBuiltinKind.BYTE -> "KByteArray${if(ptr) "*" else ""}"
                        WebIDLBuiltinKind.SHORT -> "KShortArray${if(ptr) "*" else ""}"
                        WebIDLBuiltinKind.INT -> "KIntArray${if(ptr) "*" else ""}"
                        WebIDLBuiltinKind.LONG -> "KLongArray${if(ptr) "*" else ""}"
                        WebIDLBuiltinKind.FLOAT -> "KFloatArray${if(ptr) "*" else ""}"
                        WebIDLBuiltinKind.DOUBLE -> "KDoubleArray${if(ptr) "*" else ""}"
                        WebIDLBuiltinKind.STRING -> "KStringArray${if(ptr) "*" else ""}"
                        else -> throw UnsupportedOperationException()
                    }
                    is ResolvedIdlEnum -> "KIntArray${if(ptr) "*" else ""}"
                    is ResolvedIdlDictionary -> "KArray${if(ptr) "*" else ""}"
                    else -> throw UnsupportedOperationException(declaration.name)
                }
            }
            else -> throw UnsupportedOperationException(toString())
        }
        is ResolvedIdlEnum -> if(enumAsInt) "KInt" else declaration.name
        is ResolvedIdlCallbackFunction -> "${declaration.name}${if(ptr) "*" else ""}"
        is ResolvedIdlDictionary -> "${declaration.name}${if(ptr) "*" else ""}"
        else -> throw UnsupportedOperationException(declaration.name)
    }
    else -> throw UnsupportedOperationException(toString())
}


fun ResolvedIdlType.toJNIType(
    isCritical: Boolean = false
): String = when(this) {
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
            WebIDLBuiltinKind.STRING -> if(isCritical) "jbyte*" else "jstring"
            WebIDLBuiltinKind.LIST -> firstParam { _, declaration ->
                when (declaration) {
                    is BuiltinIdlDeclaration -> when (declaration.kind) {
                        WebIDLBuiltinKind.CHAR -> if (isCritical) "char*" else "jcharArray"
                        WebIDLBuiltinKind.BOOLEAN -> if (isCritical) "jboolean*" else "jbooleanArray"
                        WebIDLBuiltinKind.BYTE -> if (isCritical) "jbyte*" else "jbyteArray"
                        WebIDLBuiltinKind.SHORT -> if (isCritical) "jshort*" else "jshortArray"
                        WebIDLBuiltinKind.INT -> if (isCritical) "jint*" else "jintArray"
                        WebIDLBuiltinKind.LONG -> if (isCritical) "jlong*" else "jlongArray"
                        WebIDLBuiltinKind.FLOAT -> if (isCritical) "jfloat*" else "jfloatArray"
                        WebIDLBuiltinKind.DOUBLE -> if (isCritical) "jdouble*" else "jdoubleArray"
                        else -> "jobjectArray"
                    }
                    is ResolvedIdlEnum if(isCritical) -> "jint*"
                    else -> "jobjectArray"
                }
            }
            else -> throw UnsupportedOperationException(toString())
        }
        is ResolvedIdlEnum -> if(isCritical) "jint" else "jobject"
        else -> "jobject"
    }
    else -> throw UnsupportedOperationException(toString())
}

fun ResolvedIdlOperation.toJavaDesc(
    classpath: String,
    isCritical: Boolean = false
): String = "(${args.joinToString("") { 
    it.type.toJavaDesc(classpath, isCritical) 
}})${type.toJavaDesc(classpath, isCritical)}"

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
            WebIDLBuiltinKind.LIST -> firstParam { type, _ ->
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

fun ResolvedIdlType.getAlignment(
    x86: Boolean = false
): Int = when(this) {
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
            WebIDLBuiltinKind.STRING -> if(x86) 4 else 8
            WebIDLBuiltinKind.LIST -> if(x86) 4 else 8
            else -> throw UnsupportedOperationException(a.toString())
        }
        is ResolvedIdlEnum -> 4
        else -> if(x86) 4 else 8
    }
}


fun ResolvedIdlType.isPrimitive(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    return (declaration as BuiltinIdlDeclaration).kind in setOf(
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

fun ResolvedIdlType.isInt(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    return (declaration as BuiltinIdlDeclaration).kind == WebIDLBuiltinKind.INT
}

fun ResolvedIdlType.isDouble(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    return (declaration as BuiltinIdlDeclaration).kind == WebIDLBuiltinKind.DOUBLE
}

fun ResolvedIdlType.isFloat(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    return (declaration as BuiltinIdlDeclaration).kind == WebIDLBuiltinKind.FLOAT
}

fun ResolvedIdlType.isBoolean(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    return (declaration as BuiltinIdlDeclaration).kind == WebIDLBuiltinKind.BOOLEAN
}

fun ResolvedIdlType.isShort(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    return (declaration as BuiltinIdlDeclaration).kind == WebIDLBuiltinKind.SHORT
}

fun ResolvedIdlType.isByte(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    return (declaration as BuiltinIdlDeclaration).kind == WebIDLBuiltinKind.BYTE
}

fun ResolvedIdlType.isChar(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    return (declaration as BuiltinIdlDeclaration).kind == WebIDLBuiltinKind.CHAR
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

fun ResolvedIdlType.isBooleanArray(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    if((declaration as BuiltinIdlDeclaration).kind != WebIDLBuiltinKind.LIST)
        return false
    return firstParam { type, _ -> type.isBoolean() }
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

        if(stringAsBytes && arg.type.isString())
            append(", __len_${arg.name}: Int, __size_${arg.name}: Int")

        if(arraysLen && arg.type.isArray())
            append(", __len_${arg.name}: Int")

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