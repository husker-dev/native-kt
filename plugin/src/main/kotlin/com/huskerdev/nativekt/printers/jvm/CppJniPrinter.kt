package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.printers.globalOperators
import com.huskerdev.nativekt.printers.isString
import com.huskerdev.nativekt.printers.toJNIType
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlField
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import java.io.File

class CppJniPrinter(
    idl: IdlResolver,
    target: File,
    val classPath: String
) {
    init {
        val builder = StringBuilder()
        builder.append("""
            #include <jni.h>
            #include "api.h"
            
        """.trimIndent())

        idl.globalOperators().forEach { printFunction(builder, it) }

        target.writeText(builder.toString())
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        // JNIEXPORT void JNICALL Java_nativelib_AwesomeLib_hello(JNIEnv *env, jobject

        append("\nJNIEXPORT ")
        append(function.type.toJNIType())
        append(" Java_")
        append(classPath.replace(".", "_"))
        append("_JNI_")
        append(function.name)
        append("(JNIEnv *env, jobject __this")

        if(function.args.isNotEmpty())
            append(", ")

        function.args.joinTo(this) {
            val underscore = if(it.type.isString()) "_" else ""

            "${it.type.toJNIType()} $underscore${it.name}"
        }

        append(") {\n")

        // Import strings
        function.args
            .filter { it.type.isString() }
            .joinTo(this, separator = "") {
                "\tconst char* ${it.name} = (*env)->GetStringUTFChars(env, _${it.name}, NULL);\n"
            }

        append('\t')

        // Store in variable if returns
        if(function.type !is ResolvedIdlType.Void) {
            append(function.type.toJNIType())
            append(" __result = ")
        }

        // == Function call ==
        append(function.name)
        append("(")
        append(castArgs(function.args))
        append(");\n\t")

        // Release strings
        function.args
            .filter { it.type.isString() }
            .joinTo(this, separator = "") {
                "(*env)->ReleaseStringUTFChars(env, _${it.name}, ${it.name});\n"
            }

        // Actual return
        if(function.type !is ResolvedIdlType.Void)
            append("\treturn __result;\n")
        append("}\n")
    }

    private fun castArgs(args: List<ResolvedIdlField.Argument>): String {
        return args.flatMap { arg ->
            if(arg.type.isString())
                listOf(arg.name, "(*env)->GetStringUTFLength(env, _${arg.name})")
            else
                listOf(arg.name)
        }.joinToString()
    }

}