package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.utils.*
import com.huskerdev.nativekt.utils.arrayType
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
            
            static char** readMangledMethods(JNIEnv *env, jobject mangledMethods) {
                jsize length = (*env)->GetArrayLength(env, mangledMethods);
            
                char** result = malloc(sizeof(void*) * length);
                for (jsize i = 0; i < length; i++) {
                    jstring str = (jstring) (*env)->GetObjectArrayElement(env, mangledMethods, i);
                    jsize size = (*env)->GetStringLength(env, str);
            
                    char* strData = malloc(size + 1);
                    (*env)->GetStringUTFRegion(env, str, 0, size, strData);
                    strData[size] = 0;
                    result[i] = strData;
                }
                return result;
            }
            
            static void freeMangledMethods(JNIEnv *env, jobject mangledMethods, char** mangledNames) {
                for (jsize i = 0; i < (*env)->GetArrayLength(env, mangledMethods); i++)
                    free(mangledNames[i]);
                free(mangledNames);
            }
            
            JNIEXPORT void JNICALL Java_${classPath.replace(".", "_")}_${name}_nJNILoad(JNIEnv *env, jclass clazz, jobject mangledMethods$isCritical) {
                char** mangledNames = readMangledMethods(env, mangledMethods);
                JNINativeMethod* methods = malloc(sizeof(JNINativeMethod) * ${idl.globalOperators().size});
        """.trimIndent())

        val operators = idl.globalOperators()
        operators.forEachIndexed { index, function ->
            val name = function.name
            val funcName = function.jniName()
            val funcDesc = function.toJavaDesc(classPath)
            val nl = "\n\t\t"

            append("\n\tmethods[$index] = (JNINativeMethod) {$nl")
            if(isAndroid && isAndroidCriticalEnabled && function.isCritical() && function.isAndroidCriticalCapable()) {
                val criticalFuncName = funcName + "_"
                val criticalFuncDesc = function.toJavaDesc(classPath, isCritical = true)

                append("mangledNames[$index],$nl")
                append("critical ? \"$criticalFuncDesc\" : \"!${funcDesc}\",$nl")
                append("critical ? (void*)&$criticalFuncName : (void*)&$funcName")
            } else
                append("mangledNames[$index],$nl\"$funcDesc\",$nl(void*)&$funcName")

            append("\n\t};")
        }

        append("""
            
                JNI_Init(env, methods, ${idl.globalOperators().size});
                free(methods);
                freeMangledMethods(env, mangledMethods, mangledNames);
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
            add("jclass cls")
            addAll(function.args.map {
                "${it.type.toJNIType()} __jvm_${it.name}"
            })
        }.joinTo(builder, prefix = "(", postfix = ") {")

        val returns = function.type !is ResolvedIdlType.Void
        val needReleases = function.args.any { it.type.isReleasable() } || function.type.isReleasable()

        // ===========
        //    Cast
        // ===========

        val argsToCast = function.args.filter { !it.type.isPrimitive() }
        val typeMaxLength = argsToCast.maxOfOrNull { it.type.toCType().length } ?: 0

        // write args
        argsToCast.joinTo(this, separator = "") {
            buildString {
                append("\n\t${it.type.toCType().padEnd(typeMaxLength)} __native_${it.name} = ")
                if(it.type.isNullable && !it.type.isPrimitive() && !it.type.isEnum())
                    append("__jvm_${it.name} == 0 ? NULL : ")
                append(castKotlinToJNI(
                    it.type,
                    "__jvm_${it.name}",
                    onStack = true,
                    flags = "0"
                ))
                append(";")
            }
        }

        // ===========
        //    Call
        // ===========

        val args = function.args.joinToString {
            if(it.type.isPrimitive())
                "__jvm_${it.name}"
            else "__native_${it.name}"
        }
        val call = "${function.name}($args)"

        if(returns) {
            if(needReleases) {
                if(!function.type.isPrimitive()) {
                    append("\n\t${function.type.toCType()} result_native = $call;")
                    append("\n\t${function.type.toJNIType()} result_jvm = ${castJniToKotlin(function.type, "result_native")};")
                } else
                    append("\n\t${function.type.toJNIType()} result_jvm = ${castJniToKotlin(function.type, call)};")
            } else
                append("\n\treturn ${castJniToKotlin(function.type, call)};")
        } else append("\n\t$call;")

        // ==================
        //   Dealloc result
        // ==================

        if(function.type.isReleasable()) {
            freeFuncFor(
                function.type,
                "result_native"
            )?.apply { append("\n\t$this;") }
        }

        // ==================
        //    Release args
        // ==================

        function.args.forEach { arg ->
            val type = arg.type
            val name = "__native_${arg.name}"
            when {
                type.isString() -> "JNI_releaseKStringOnStack(env, $name)"
                type.isArray() -> type.arrayType { type ->
                    when {
                        type.isPrimitive() -> "JNI_release${type.toCType(ignoreUnsigned = true)}ArrayOnStack(env, ${castToSignedC(arg.type, name)})"
                        type.isEnum() -> "JNI_releaseEnumArrayOnStack($name)"
                        else -> "JNI_releaseKArrayOnStack($name, (void*) ${freeFuncFor(type, "")!!.dropLast(2)})"
                    }
                }
                type.isDictionary() -> forceFreeFuncFor(type, name)
                else -> return@forEach
            }.apply { append("\n\t$this;") }
        }

        // ==================
        //      Return
        // ==================

        if(returns && needReleases)
            append("\n\treturn result_jvm;")
        append("\n}\n")
    }

    private fun printCriticalFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\nstatic ")
        printCriticalNativeFunctionContent(
            builder,
            name = "${function.jniName()}_",
            function
        )
    }

    private fun ResolvedIdlOperation.jniName() =
        "Java_${classPath.replace(".", "_")}_${this@CJniPrinter.name}_$name"
}

internal fun castJniToKotlin(
    type: ResolvedIdlType,
    content: String
): String = when {
    type.isUnsigned() -> castJniToKotlin(type.toSignedType(), castToSignedC(type, content))
    type.isEnum() -> "JNI_toKotlinEnum(env, $content, class${type.toCType()}, values${type.toCType()})"
    type.isDictionary() -> "JNI_toKotlin${type.toCType(ptr = false)}(env, $content)"
    type.isString() -> "JNI_toKotlinKString(env, $content)"
    type.isCallback() -> "JNI_toKotlinCallback(env, (_AbstractCallback*) $content)"
    type.isArray() -> type.arrayType { type ->
        when {
            type.isPrimitive() -> "JNI_toKotlin${type.toCType(ptr = false)}Array(env, $content)"
            type.isEnum() -> "JNI_toKotlinEnumArray(env, $content, class${type.toCType()}, values${type.toCType()})"
            else -> {
                val cast = castJniToKotlin(type, "").split("(")[0]
                "JNI_toKotlinKArray(env, $content, (jobject(*)(JNIEnv*, void*)) $cast, class${type.toKotlinType(printNullable = false)})"
            }
        }
    }
    else -> content
}

internal fun castKotlinToJNI(
    type: ResolvedIdlType,
    content: String,
    onStack: Boolean,
    flags: String
): String = when {
    type.isUnsigned() -> castToUnsignedC(type, castKotlinToJNI(type.toSignedType(), content, onStack, flags))
    type.isEnum() -> "JNI_toNativeEnum(env, $content)"
    type.isDictionary() -> "JNI_toNative${type.toCType(ptr = false)}(env, $content, $flags)"
    type.isString() ->
        if(onStack) "JNI_toNativeKStringOnStack(env, $content, alloca(JNI_StringStackSize))"
        else "JNI_toNativeKString(env, $content, $flags)"
    type.isCallback() -> {
        val name = type.toCType(ptr = false)
        if (onStack) "($name*) JNI_toNativeCallbackOnStack(env, $content, (void(*)())JNI_CALLBACK_INVOKE_$name, alloca(_AbstractCallbackSize))"
        else "($name*) JNI_toNativeCallback(env, $content, (void(*)())JNI_CALLBACK_INVOKE_$name, $flags)"
    }
    type.isArray() -> type.arrayType { type ->
        when {
            type.isPrimitive() ->
                if(onStack) "JNI_toNative${type.toCType(ptr = false)}ArrayOnStack(env, $content, alloca(JNI_ArrayStackSize))"
                else "JNI_toNative${type.toCType(ptr = false)}Array(env, $content, $flags)"
            type.isEnum() ->
                if (onStack) "JNI_toNativeEnumArrayOnStack(env, $content, alloca(JNI_ArrayStackSize))"
                else "JNI_toNativeEnumArray(env, $content, $flags)"
            else -> {
                val castFunc = castKotlinToJNI(type, "", false, "").split("(")[0]
                if(onStack) "JNI_toNativeKArrayOnStack(env, $content, (void*(*)(JNIEnv*, jobject, char)) $castFunc, alloca(JNI_ArrayStackSize))"
                else "JNI_toNativeKArray(env, $content, (void*(*)(JNIEnv*, jobject, char)) $castFunc, $flags)"
            }
        }
    }
    else -> content
}

fun ResolvedIdlType.toJNIType(): String = when {
    isVoid() -> "void"
    isChar() -> "jchar"
    isBoolean() -> "jboolean"
    isByte() || isUByte() -> "jbyte"
    isShort() || isUShort() -> "jshort"
    isInt() || isUInt() -> "jint"
    isLong() || isULong() -> "jlong"
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

internal fun ResolvedIdlType.toJavaDesc(
    classpath: String,
    isCritical: Boolean = false
): String = when {
    isVoid() -> "V"
    isChar() -> "C"
    isBoolean() -> "Z"
    isByte() || isUByte() -> "B"
    isShort() || isUShort() -> "S"
    isInt() || isUInt() -> "I"
    isLong() || isULong() -> "J"
    isFloat() -> "F"
    isDouble() -> "D"
    isString() -> if(isCritical) "[B" else "Ljava/lang/String;"
    isEnum() && isCritical -> "I"
    isArray() -> arrayType { type ->
        "[${type.toJavaDesc(classpath, isCritical)}"
    }
    else -> "L${classpath.replace(".", "/")}/${(this as ResolvedIdlType.Default).declaration.name};"
}

internal fun ResolvedIdlOperation.toJavaDesc(
    classpath: String,
    isCritical: Boolean = false
): String = buildString {
    args.joinTo(this, "", prefix = "(", postfix = ")") {
        it.type.toJavaDesc(classpath, isCritical)
    }
    append(type.toJavaDesc(classpath, isCritical))
}