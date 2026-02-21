package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.*
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
        append("""
            
            /* =================== *\
                      Load
            \* =================== */
            
            JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
                JNINativeMethod methods[] = {
            
        """.trimIndent())

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

        // Get env
        append("""
            
                return JNI_Init(vm, methods, ${idl.globalOperators().size});
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

        val useArena = function.args.any { it.type.isString() || it.isDealloc() }

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
        val args = function.args.joinToString { castJavaToJNI(it.type, "__arg_${it.name}", function.isCritical(), it.isDealloc(), useArena) }
        val call = "${function.name}($args)"
        append(castJniToJava(function.type, call, function.isDealloc(), useArena))
        append(";\n")

        if(useArena) {
            append("\tArena__free(&arena);\n")
            if(function.type !is ResolvedIdlType.Void)
                append("\treturn __result;\n")
        }

        append("}\n")
    }



}