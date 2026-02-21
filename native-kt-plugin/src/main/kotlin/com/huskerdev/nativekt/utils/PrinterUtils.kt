package com.huskerdev.nativekt.utils

import com.huskerdev.webidl.parser.IdlAttributedHolder
import com.huskerdev.webidl.parser.IdlExtendedAttribute
import com.huskerdev.webidl.resolver.BuiltinIdlDeclaration
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlCallbackFunction
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind
import org.gradle.internal.extensions.stdlib.capitalized

fun asyncFunctionName(moduleName: String) =
    "loadLib${moduleName.capitalized()}"

fun syncFunctionName(moduleName: String) =
    "loadLib${moduleName.capitalized()}Sync"

fun ResolvedIdlType.toKotlinForeignType(): String {
    return if(isCallback() || isString())
        "MemorySegment"
    else toKotlinType()
}

fun ResolvedIdlType.toKotlinType(
    stringAsBytes: Boolean = false,
    callbackAsAny: Boolean = false
): String = when(this) {
    is ResolvedIdlType.Union -> throw UnsupportedOperationException("Union type are not unsupported")
    is ResolvedIdlType.Void -> "Unit"
    is ResolvedIdlType.Default -> buildString {
        append(when(declaration) {
            is BuiltinIdlDeclaration -> when(val a = (declaration as BuiltinIdlDeclaration).kind) {
                WebIDLBuiltinKind.CHAR -> "Char"
                WebIDLBuiltinKind.BOOLEAN -> "Boolean"
                WebIDLBuiltinKind.BYTE,
                WebIDLBuiltinKind.UNSIGNED_BYTE -> "Byte"
                WebIDLBuiltinKind.SHORT,
                WebIDLBuiltinKind.UNSIGNED_SHORT -> "Short"
                WebIDLBuiltinKind.INT,
                WebIDLBuiltinKind.UNSIGNED_INT -> "Int"
                WebIDLBuiltinKind.LONG,
                WebIDLBuiltinKind.UNSIGNED_LONG -> "Long"
                WebIDLBuiltinKind.FLOAT,
                WebIDLBuiltinKind.UNRESTRICTED_FLOAT -> "Float"
                WebIDLBuiltinKind.DOUBLE,
                WebIDLBuiltinKind.UNRESTRICTED_DOUBLE -> "Double"
                WebIDLBuiltinKind.STRING -> if(stringAsBytes) "ByteArray" else "String"
                else -> throw UnsupportedOperationException(a.toString())
            }
            is ResolvedIdlCallbackFunction -> if(callbackAsAny) "Any" else declaration.name
            else -> declaration.name
        })
        if(parameters.isNotEmpty())
            throw UnsupportedOperationException("Parameters are not null")
    }
}

fun ResolvedIdlType.toCType(
    longPtr: Boolean = false,
    constChar: Boolean = true
): String = when(this) {
    is ResolvedIdlType.Union -> throw UnsupportedOperationException("Union type are not unsupported")
    is ResolvedIdlType.Void -> "void"
    is ResolvedIdlType.Default -> buildString {
        append(when(declaration) {
            is BuiltinIdlDeclaration -> when((declaration as BuiltinIdlDeclaration).kind) {
                WebIDLBuiltinKind.CHAR -> "uint16_t"
                WebIDLBuiltinKind.BOOLEAN -> "bool"
                WebIDLBuiltinKind.BYTE,
                WebIDLBuiltinKind.UNSIGNED_BYTE -> "int8_t"
                WebIDLBuiltinKind.SHORT,
                WebIDLBuiltinKind.UNSIGNED_SHORT -> "int16_t"
                WebIDLBuiltinKind.INT,
                WebIDLBuiltinKind.UNSIGNED_INT -> "int32_t"
                WebIDLBuiltinKind.LONG,
                WebIDLBuiltinKind.UNSIGNED_LONG -> "int64_t${if(longPtr) "*" else ""}"
                WebIDLBuiltinKind.FLOAT,
                WebIDLBuiltinKind.UNRESTRICTED_FLOAT -> "float"
                WebIDLBuiltinKind.DOUBLE,
                WebIDLBuiltinKind.UNRESTRICTED_DOUBLE -> "double"
                WebIDLBuiltinKind.STRING -> "${if(constChar) "const " else ""}char*"
                else -> throw UnsupportedOperationException()
            }
            is ResolvedIdlCallbackFunction -> "${declaration.name}*"
            else -> declaration.name
        })
        if(parameters.isNotEmpty())
            throw UnsupportedOperationException("Parameters are not null")
    }
}


fun ResolvedIdlType.toJNIType(): String = when(this) {
    is ResolvedIdlType.Union -> throw UnsupportedOperationException("Union type are not unsupported")
    is ResolvedIdlType.Void -> "void"
    is ResolvedIdlType.Default -> buildString {
        append(when(declaration) {
            is BuiltinIdlDeclaration -> when(val a = (declaration as BuiltinIdlDeclaration).kind) {
                WebIDLBuiltinKind.CHAR -> "jchar"
                WebIDLBuiltinKind.BOOLEAN -> "jboolean"
                WebIDLBuiltinKind.BYTE,
                WebIDLBuiltinKind.UNSIGNED_BYTE -> "jbyte"
                WebIDLBuiltinKind.SHORT,
                WebIDLBuiltinKind.UNSIGNED_SHORT -> "jshort"
                WebIDLBuiltinKind.INT,
                WebIDLBuiltinKind.UNSIGNED_INT -> "jint"
                WebIDLBuiltinKind.LONG,
                WebIDLBuiltinKind.UNSIGNED_LONG -> "jlong"
                WebIDLBuiltinKind.FLOAT,
                WebIDLBuiltinKind.UNRESTRICTED_FLOAT -> "jfloat"
                WebIDLBuiltinKind.DOUBLE,
                WebIDLBuiltinKind.UNRESTRICTED_DOUBLE -> "jdouble"
                WebIDLBuiltinKind.STRING -> "jobject"
                else -> throw UnsupportedOperationException(a.toString())
            }
            else -> "jobject"
        })
        if(parameters.isNotEmpty())
            throw UnsupportedOperationException("Parameters are not null")
    }
}

fun ResolvedIdlType.toJavaDesc(): String = when(this) {
    is ResolvedIdlType.Union -> throw UnsupportedOperationException("Union type are not unsupported")
    is ResolvedIdlType.Void -> "V"
    is ResolvedIdlType.Default -> buildString {
        append(when(declaration) {
            is BuiltinIdlDeclaration -> when(val a = (declaration as BuiltinIdlDeclaration).kind) {
                WebIDLBuiltinKind.CHAR -> "C"
                WebIDLBuiltinKind.BOOLEAN -> "Z"
                WebIDLBuiltinKind.BYTE,
                WebIDLBuiltinKind.UNSIGNED_BYTE -> "B"
                WebIDLBuiltinKind.SHORT,
                WebIDLBuiltinKind.UNSIGNED_SHORT -> "S"
                WebIDLBuiltinKind.INT,
                WebIDLBuiltinKind.UNSIGNED_INT -> "I"
                WebIDLBuiltinKind.LONG,
                WebIDLBuiltinKind.UNSIGNED_LONG -> "J"
                WebIDLBuiltinKind.FLOAT,
                WebIDLBuiltinKind.UNRESTRICTED_FLOAT -> "F"
                WebIDLBuiltinKind.DOUBLE,
                WebIDLBuiltinKind.UNRESTRICTED_DOUBLE -> "D"
                WebIDLBuiltinKind.STRING -> "Ljava/lang/String;"
                else -> throw UnsupportedOperationException(a.toString())
            }
            else -> "Ljava/lang/Object;"
        })
        if(parameters.isNotEmpty())
            throw UnsupportedOperationException("Parameters are not null")
    }
}

fun castJniToJava(type: ResolvedIdlType, content: String, dealloc: Boolean, useArena: Boolean): String {
    return when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "Arena__wrapString(&arena, $content, $dealloc)"
                    else "JNI_toJvmString(env, $content, $dealloc)"
                else -> content
            }
            is ResolvedIdlCallbackFunction -> "JNI_toJvmCallback(env, (JNI_Callback*)$content, $dealloc)"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }
}

fun castJavaToJNI(type: ResolvedIdlType, content: String, critical: Boolean, dealloc: Boolean, useArena: Boolean): String {
    return when(type) {
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "Arena__unwrapString${if(critical) "Critical" else ""}(&arena, $content)"
                    else "JNI_toNativeString(env, $content)"
                else -> content
            }
            is ResolvedIdlCallbackFunction ->
                if(dealloc) "(${decl.name}*)Arena__callback(&arena, (JNI_Callback*)JNI_wrap${decl.name}(env, $content))"
                else "JNI_wrap${decl.name}(env, $content)"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }
}

fun ResolvedIdlType.isString(): Boolean {
    if (this !is ResolvedIdlType.Default ||
        declaration !is BuiltinIdlDeclaration) return false
    return (declaration as BuiltinIdlDeclaration).kind == WebIDLBuiltinKind.STRING
}

fun ResolvedIdlType.isCallback(): Boolean =
    this is ResolvedIdlType.Default && declaration is ResolvedIdlCallbackFunction

fun ResolvedIdlOperation.isCritical(): Boolean =
    this.attributes.any {
        it is IdlExtendedAttribute.NoArgs && it.name == "Critical"
    }

fun IdlAttributedHolder.isDealloc(): Boolean =
    this.attributes.any {
        it is IdlExtendedAttribute.NoArgs && it.name == "Dealloc"
    }

fun IdlResolver.globalOperators() =
    namespaces.values.flatMap { it.operations }

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
    callbackAsAny: Boolean = false
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
        append(arg.type.toKotlinType(stringAsBytes, callbackAsAny))

        if(index != function.args.lastIndex)
            append(", ")
    }
    append(")")
    if(forcePrintVoid || function.type !is ResolvedIdlType.Void) {
        append(": ")
        append(function.type.toKotlinType(stringAsBytes, callbackAsAny))
    }
}