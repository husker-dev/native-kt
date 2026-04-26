package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.*
import com.huskerdev.nativekt.utils.firstParam
import com.huskerdev.webidl.resolver.*
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
            #include "jni_arena.h"
            
        """.trimIndent())

        idl.globalOperators().forEach { printFunction(builder, it) }

        printRegisterFunction(builder)

        target.writeText(builder.toString())
    }

    private fun printRegisterFunction(builder: StringBuilder) = builder.apply {
        printLabel(builder, "Load")
        append("""
            
            JNIEXPORT void JNICALL Java_${classPath.replace(".", "_")}_${name}_JNILoad(JNIEnv *env, jclass clazz) {
                JNINativeMethod methods[] = {
            
        """.trimIndent())

        // {"run", "()V", (void *)&Java_natives_glfwBindings_GlfwBindingsJNI_glfwInit},
        val operators = idl.globalOperators()
        operators.forEachIndexed { index, function ->
            append("\t\t{\"")
            append(function.name)
            append("\", \"(")
            function.args.joinTo(builder, "") { it.type.toJavaDesc(classPath) }
            append(")")
            append(function.type.toJavaDesc(classPath))
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

        // Get env
        append("""
            
                return JNI_Init(env, methods, ${idl.globalOperators().size});
            }
        """.trimIndent())
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\nstatic ")
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
            "${it.type.toJNIType()} __arg_${it.name}"
        }

        append(") {\n")

        val useArena = function.args.any { it.type.isString() || it.type.isArray() || it.isDealloc() }

        if(useArena) {
            append("\tArena arena;\n")
            append("\tArena__init(&arena, env);\n")
        }

        append("\t")
        if(function.type !is ResolvedIdlType.Void) {
            if(useArena) {
                append(function.type.toJNIType())
                append(" __result = ")
            } else append("return ")
        }

        // == Function call ==
        val args = function.args.joinToString { castJavaToJNI(it.type, "__arg_${it.name}", it.isDealloc(), useArena) }
        val call = "${function.name}($args)"
        append(castJniToJava(function.type, call, function.isDealloc(), function.isDeallocContent(), useArena))
        append(";\n")

        if(useArena) {
            append("\tArena__free(&arena);\n")
            if(function.type !is ResolvedIdlType.Void)
                append("\treturn __result;\n")
        }

        append("}\n")
    }
}

internal fun castJniToJava(type: ResolvedIdlType, content: String, dealloc: Boolean, deallocContent: Boolean, useArena: Boolean): String {
    return when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "Arena__toKotlinString(&arena, $content, $dealloc)"
                    else "JNI_toKotlinString(env, $content, $dealloc)"
                WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                    when (declaration) {
                        is BuiltinIdlDeclaration -> {
                            val name = declaration.kind.simpleName()
                            if (useArena) "Arena__toKotlin${name}Array(&arena, $content, $dealloc)"
                            else "JNI_toKotlin${name}Array(env, $content, $dealloc)"
                        }
                        is ResolvedIdlEnum -> "JNI_toKotlinEnumArray(env, $content, enum${declaration.name}Class, enum${declaration.name}Values, $dealloc)"
                        is ResolvedIdlDictionary -> "JNI_toKotlinArray(env, $content, (jobject(*)(JNIEnv*, void*, bool))JNI_toKotlinDictionary${declaration.name}, struct${declaration.name}Class, $dealloc, $deallocContent)"
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlCallbackFunction -> "JNI_toKotlinCallback(env, (JNI_Callback*)$content, $dealloc)"
            is ResolvedIdlEnum -> "JNI_toKotlinEnum(env, $content, enum${decl.name}Class, enum${decl.name}Values)"
            is ResolvedIdlDictionary -> "JNI_toKotlinDictionary${decl.name}(env, $content, $dealloc)"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }
}

internal fun castJavaToJNI(type: ResolvedIdlType, content: String, dealloc: Boolean, useArena: Boolean): String {
    return when(type) {
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "Arena__toNativeString(&arena, $content)"
                    else "JNI_toNativeString(env, $content)"
                WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                    when (declaration) {
                        is BuiltinIdlDeclaration -> {
                            val name = declaration.kind.simpleName()
                            if (useArena) "Arena__toNative${name}Array(&arena, $content)"
                            else "JNI_toNative${name}Array(env, $content)"
                        }
                        is ResolvedIdlEnum -> "JNI_toNativeEnumArray(env, $content)"
                        is ResolvedIdlDictionary -> "JNI_toNativeArray(env, $content, (void*(*)(JNIEnv*, jobject))JNI_toNativeDictionary${declaration.name})"
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlCallbackFunction -> {
                val call = "JNI_toNativeCallback(env, $content, (void(*)())JNI_CALLBACK_INVOKE_${decl.name})"
                if (dealloc) "(${decl.name}*)Arena__callback(&arena, $call)"
                else "(${decl.name}*)$call"
            }
            is ResolvedIdlEnum -> "JNI_toNativeEnum(env, $content)"
            is ResolvedIdlDictionary -> "JNI_toNativeDictionary${decl.name}(env, $content)"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }
}