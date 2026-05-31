package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.utils.*
import com.huskerdev.nativekt.utils.firstParam
import com.huskerdev.webidl.resolver.*
import java.io.File

class CJniPrinter(
    val idl: IdlResolver,
    target: File,
    val classPath: String,
    val name: String = "JNI",
    val isAndroid: Boolean,
    val isAndroidCriticalEnabled: Boolean
) {
    init {
        val builder = StringBuilder()
        builder.append("""
            #include "jni_utils.h"
            
        """.trimIndent())

        idl.globalOperators().forEach {
            printFunction(builder, it)

            if(isAndroid && isAndroidCriticalEnabled && it.isCritical() && it.isAndroidCriticalCapable())
                printCriticalFunction(builder, it)
        }

        printRegisterFunction(builder)

        target.writeText(builder.toString())
    }

    private fun printRegisterFunction(builder: StringBuilder) = builder.apply {
        printLabel(builder, "Load")

        val isCritical = if(isAndroid && isAndroidCriticalEnabled)
            ", jboolean critical" else ""

        append("""
            
            JNIEXPORT void JNICALL Java_${classPath.replace(".", "_")}_${name}_JNILoad(JNIEnv *env, jclass clazz$isCritical) {
                JNINativeMethod methods[] = {
                    
        """.trimIndent())

        val operators = idl.globalOperators()
        operators.forEachIndexed { index, function ->
            val name = function.name
            val funcName = function.jniName()
            val funcDesc = function.toJavaDesc(classPath)
            val nl = "\n\t\t\t"

            append("{$nl")
            if(isAndroid && isAndroidCriticalEnabled && function.isCritical() && function.isAndroidCriticalCapable()) {
                val criticalFuncName = funcName + "_"
                val criticalFuncDesc = function.toJavaDesc(classPath, isCritical = true)

                append("critical ? \"${name}_\" : \"$name\",$nl")
                append("critical ? \"$criticalFuncDesc\" : \"!${funcDesc}\",$nl")
                append("critical ? (void*)&$criticalFuncName : (void*)&$funcName")
            } else
                append("\"$name\",$nl\"$funcDesc\",$nl(void*)&$funcName")

            append("\n\t\t}")
            if(index != operators.lastIndex)
                append(", ")
        }

        append("""
            
                };
                return JNI_Init(env, methods, ${idl.globalOperators().size});
            }
        """.trimIndent())
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\nstatic ")
        append(function.type.toJNIType())
        append(" ")
        append(function.jniName())

        buildList {
            add("JNIEnv *env")
            add("jclass __cls")
            addAll(function.args.map {
                "${it.type.toJNIType()} __arg_jvm_${it.name}"
            })
        }.joinTo(builder, prefix = "(", postfix = ") {")

        val returns = function.type !is ResolvedIdlType.Void
        val needReleases = function.args.any { !it.type.isPrimitive() } || function.isDealloc()

        // ===========
        //    Cast
        // ===========

        val argsToCast = function.args.filter { !it.type.isPrimitive() }
        val typeMaxLength = argsToCast.maxOfOrNull { it.type.toCDefType().length } ?: 0

        // write args
        argsToCast.joinTo(this, separator = "") {
            "\n\t${it.type.toCDefType().padEnd(typeMaxLength)} __arg_native_${it.name} = ${castJavaToJNI(
                it.type, 
                "__arg_jvm_${it.name}",
                onStack = true,
                flags = "0"
            )};"
        }

        // ===========
        //    Call
        // ===========

        val args = function.args.joinToString {
            if(it.type.isPrimitive())
                "__arg_jvm_${it.name}"
            else "__arg_native_${it.name}"
        }
        val call = "${function.name}($args)"

        if(returns) {
            if(needReleases) {
                if(!function.type.isPrimitive()) {
                    append("\n\t${function.type.toCDefType()} __result_native = $call;")
                    append("\n\t${function.type.toJNIType()} __result_jvm = ${castJniToJava(function.type, "__result_native")};")
                } else
                    append("\n\t${function.type.toJNIType()} __result_jvm = ${castJniToJava(function.type, call)};")
            } else
                append("\n\treturn ${castJniToJava(function.type, call)};")
        } else append("\n\t$call;")

        // ==================
        //   Dealloc result
        // ==================

        if(function.isDealloc()) {
            freeFuncFor(
                function.type,
                "__result_native"
            )?.apply { append("\n\t$this;") }
        }

        // ==================
        //    Release args
        // ==================

        function.args.forEach { arg ->
            when {
                arg.type.isString() -> "JNI_releaseStringOnStack(env, __arg_native_${arg.name})"
                arg.type.isArray() -> {
                    (arg.type as ResolvedIdlType.Default).firstParam { _, declaration ->
                        when (declaration) {
                            is BuiltinIdlDeclaration -> {
                                val name = declaration.kind.simpleName()
                                "JNI_release${name}ArrayOnStack(env, __arg_native_${arg.name})"
                            }
                            is ResolvedIdlEnum -> "free((void*)__arg_native_${arg.name}->elements)"
                            is ResolvedIdlDictionary -> {
                                "JNI_forceFreeKArray(__arg_native_${arg.name}, (void*) JNI_forceFree${declaration.name})"
                            }
                            else -> throw UnsupportedOperationException(arg.type.toString())
                        }
                    }
                }
                arg.type.isDictionary() -> "JNI_forceFree${(arg.type as ResolvedIdlType.Default).declaration.name}(__arg_native_${arg.name})"
                else -> return@forEach
            }.apply { append("\n\t$this;") }
        }

        // ==================
        //      Return
        // ==================

        if(returns && needReleases)
            append("\n\treturn __result_jvm;")
        append("\n}\n")
    }

    private fun printCriticalFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\nstatic ")
        append(function.type.toJNIType(isCritical = true))
        append(" ")
        append(function.jniName())
        append("_")

        function.args.flatMap {
            val arg = "${it.type.toJNIType(isCritical = true)} __arg_${it.name}"

            if(it.type.isArray() || it.type.isString())
                listOf(arg, "jint __length_${it.name}")
            else listOf(arg)

        }.joinTo(builder, prefix = "(", postfix = ")")

        append(" {\n\t")

        if(function.type !is ResolvedIdlType.Void)
            append("return ")

        // == Function call ==
        val args = function.args.joinToString {
            castToKTypeFromCritical(it.type, it.name)
        }
        append("${function.name}($args);\n}\n")
    }

    private fun ResolvedIdlOperation.jniName() =
        "Java_${classPath.replace(".", "_")}_${this@CJniPrinter.name}_$name"
}

internal fun freeFuncFor(
    type: ResolvedIdlType,
    content: String
) = when {
    type.isString() -> "KString_free($content)"
    type.isArray() -> (type as ResolvedIdlType.Default).firstParam { _, declaration ->
        when (declaration) {
            is BuiltinIdlDeclaration -> "K${declaration.kind.simpleName()}Array_free($content)"
            is ResolvedIdlEnum -> "KIntArray_free($content)"
            is ResolvedIdlDictionary -> "KArray_free($content, (void*) ${declaration.name}_free)"
            else -> throw UnsupportedOperationException(type.toString())
        }
    }
    type.isCallback() -> "$content->free($content)"
    type.isDictionary() -> "${(type as ResolvedIdlType.Default).declaration.name}_free(${content})"
    else -> null
}

internal fun forceFreeFuncFor(
    type: ResolvedIdlType,
    content: String
) = when {
    type.isString() -> "JNI_forceFreeKString($content)"
    type.isArray() -> (type as ResolvedIdlType.Default).firstParam { _, declaration ->
        when (declaration) {
            is BuiltinIdlDeclaration -> "JNI_forceFreeK${declaration.kind.simpleName()}Array($content)"
            is ResolvedIdlEnum -> "JNI_forceFreeKIntArray($content)"
            is ResolvedIdlDictionary -> "JNI_forceFreeKArray($content, (void*) JNI_forceFree${declaration.name})"
            else -> throw UnsupportedOperationException(type.toString())
        }
    }
    type.isCallback() || type.isDictionary() ->
        "JNI_forceFree${(type as ResolvedIdlType.Default).declaration.name}($content)"
    else -> null
}


internal fun castJniToJava(type: ResolvedIdlType, content: String): String {
    return when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.STRING ->
                    "JNI_toKotlinString(env, $content)"
                WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                    when (declaration) {
                        is BuiltinIdlDeclaration ->
                            "JNI_toKotlin${declaration.kind.simpleName()}Array(env, $content)"
                        is ResolvedIdlEnum -> "JNI_toKotlinEnumArray(env, $content, enum${declaration.name}Class, enum${declaration.name}Values)"
                        is ResolvedIdlDictionary -> "JNI_toKotlinArray(env, $content, (jobject(*)(JNIEnv*, void*)) JNI_toKotlinDictionary${declaration.name}, struct${declaration.name}Class)"
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlCallbackFunction -> "JNI_toKotlinCallback(env, (JNI_Callback*)$content)"
            is ResolvedIdlEnum -> "JNI_toKotlinEnum(env, $content, enum${decl.name}Class, enum${decl.name}Values)"
            is ResolvedIdlDictionary -> "JNI_toKotlinDictionary${decl.name}(env, $content)"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }
}

internal fun castJavaToJNI(
    type: ResolvedIdlType,
    content: String,
    onStack: Boolean,
    flags: String
) = when(type) {
    is ResolvedIdlType.Default -> when(val decl = type.declaration) {
        is BuiltinIdlDeclaration -> when(decl.kind) {
            WebIDLBuiltinKind.STRING ->
                if(onStack) "JNI_toNativeStringOnStack(env, $content, alloca(JNI_StringStackSize))"
                else "JNI_toNativeString(env, $content, /* flags */ $flags)"
            WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                when (declaration) {
                    is BuiltinIdlDeclaration -> {
                        val name = declaration.kind.simpleName()
                        if(onStack) "JNI_toNative${name}ArrayOnStack(env, $content, alloca(JNI_ArrayStackSize))"
                        else "JNI_toNative${name}Array(env, $content, /* flags */ $flags)"
                    }
                    is ResolvedIdlEnum -> {
                        if(onStack) "JNI_toNativeEnumArrayOnStack(env, $content, alloca(JNI_ArrayStackSize))"
                        else "JNI_toNativeEnumArray(env, $content, /* flags */ $flags)"
                    }
                    is ResolvedIdlDictionary -> {
                        if(onStack) "JNI_toNativeArrayOnStack(env, $content, (void*(*)(JNIEnv*, jobject, char)) JNI_toNativeDictionary${declaration.name}, alloca(JNI_ArrayStackSize))"
                        else "JNI_toNativeArray(env, $content, (void*(*)(JNIEnv*, jobject, char)) JNI_toNativeDictionary${declaration.name}, /* flags */ $flags)"
                    }
                    else -> throw UnsupportedOperationException(type.toString())
                }
            }
            else -> content
        }
        is ResolvedIdlCallbackFunction -> {
            if (onStack) "(${decl.name}*) JNI_toNativeCallbackOnStack(env, $content, (void(*)())JNI_CALLBACK_INVOKE_${decl.name}, alloca(JNI_CallbackSize))"
            else "(${decl.name}*) JNI_toNativeCallback(env, $content, (void(*)())JNI_CALLBACK_INVOKE_${decl.name}, /* flags */ $flags)"
        }
        is ResolvedIdlEnum -> "JNI_toNativeEnum(env, $content)"
        is ResolvedIdlDictionary -> "JNI_toNativeDictionary${decl.name}(env, $content, /* flags */ $flags)"
        else -> throw UnsupportedOperationException(type.toString())
    }
    else -> throw UnsupportedOperationException(type.toString())
}