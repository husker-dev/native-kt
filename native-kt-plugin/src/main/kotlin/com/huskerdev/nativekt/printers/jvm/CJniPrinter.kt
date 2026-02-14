package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.isCritical
import com.huskerdev.nativekt.utils.isDealloc
import com.huskerdev.nativekt.utils.isString
import com.huskerdev.nativekt.utils.toJNIType
import com.huskerdev.webidl.resolver.BuiltinIdlDeclaration
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind
import java.io.File

class CJniPrinter(
    val idl: IdlResolver,
    target: File,
    val classPath: String,
    val name: String = "JNI"
) {
    init {
        val builder = StringBuilder()
        builder.append("""
            #include <jni.h>
            #include "api.h"
            #include "jni_arena.h"
            
        """.trimIndent())


        idl.globalOperators().forEach { printFunction(builder, it) }

        builder.append("\n")
        printRegisterFunction(builder)


        target.writeText(builder.toString())
    }

    private fun printRegisterFunction(builder: StringBuilder) = builder.apply {
        append("JNIEXPORT void JNICALL Java_")
        append(classPath.replace(".", "_"))
        append("_")
        append(name)
        append("_register")
        append("(JNIEnv *env, jclass cls) {\n\t")
        append("JNINativeMethod methods[] = {\n")

        // {"run", "()V", (void *)&Java_natives_glfwBindings_GlfwBindingsJNI_glfwInit},
        val operators = idl.globalOperators()
        operators.forEachIndexed { index, function ->
            append("\t\t{\"")
            append(function.name)
            append("\", \"(")
            function.args.joinTo(builder, "") { it.type.toJavaDesc() }
            append(")")
            append(function.type.toJavaDesc())
            append("\", (void*)&Java_")
            append(classPath.replace(".", "_"))
            append("_")
            append(name)
            append("_")
            append(function.name)
            append("}")
            if(index != operators.lastIndex)
                append(",")
            append("\n")
        }

        append("\t};\n\t")
        append("(*env)->RegisterNatives(env, cls, methods, sizeof(methods)/sizeof(methods[0]));\n")
        append("}")
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        // void Java_nativelib_AwesomeLib_hello(JNIEnv *env, jobject

        append("\n")
        append(function.type.toJNIType())
        append(" Java_")
        append(classPath.replace(".", "_"))
        append("_")
        append(name)
        append("_")
        append(function.name)
        append("(JNIEnv *env, jclass __cls")

        if(function.args.isNotEmpty())
            append(", ")

        function.args.joinTo(this) {
            "${it.type.toJNIType()} ${it.name}"
        }

        append(") {\n")

        val useArena = function.args.any { it.type.isString() } || function.type.isString()

        if(useArena)
            append("\tArena* arena = Arena__new(env);\n")

        append("\t")
        if(function.type !is ResolvedIdlType.Void) {
            if(useArena) {
                append(function.type.toJNIType())
                append(" __result = ")
            } else append("return ")
        }

        // == Function call ==
        val args = function.args.joinToString { castFromJava(it.type, it.name, function.isCritical()) }
        val call = "${function.name}($args)"
        append(castToJava(function.type, call, function.isDealloc()))
        append(";\n")

        if(useArena) {
            append("\tArena__free(arena);\n")
            if(function.type !is ResolvedIdlType.Void)
                append("\treturn __result;\n")
        }

        append("}\n")
    }

    fun castToJava(type: ResolvedIdlType, content: String, dealloc: Boolean): String {
        return if(type.isString())
            "Arena__wrapString(arena, $content, $dealloc)"
        else content
    }

    fun castFromJava(type: ResolvedIdlType, content: String, critical: Boolean): String {
        return if(type.isString()) {
            if(critical)
                "Arena__unwrapStringCritical(arena, $content)"
            else
                "Arena__unwrapString(arena, $content)"
        } else content
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

}