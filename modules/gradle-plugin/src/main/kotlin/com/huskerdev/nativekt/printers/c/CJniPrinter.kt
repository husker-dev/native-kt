package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.utils.*
import com.huskerdev.nativekt.utils.arrayType
import com.huskerdev.webidl.resolver.*
import java.io.File

class CJniPrinter(
    val idl: IdlResolver,
    target: File,
    val classPath: String,
    val moduleName: String,
    val name: String = "JNI",
    val isAndroid: Boolean,
    val isAndroidCriticalEnabled: Boolean
) {
    init {
        val builder = StringBuilder()
        builder.append("""
            #include "jni_utils.h"
            
        """.trimIndent())

        listOf(
            *idl.globalOperators().toTypedArray(),
            *idl.interfaces.values
                .flatMap { it.toOperations() }.toTypedArray()
        ).forEach {
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

        val operations = listOf(
            *idl.globalOperators().toTypedArray(),
            *idl.interfaces.values
                .flatMap { it.toOperations() }.toTypedArray()
        )

        append("""
            
            static char** JNI_read_mangled_methods(JNIEnv *env, jobject mangled_methods) {
                jsize length = (*env)->GetArrayLength(env, mangled_methods);
            
                char** result = malloc(sizeof(void*) * length);
                for (jsize i = 0; i < length; i++) {
                    jstring str = (jstring) (*env)->GetObjectArrayElement(env, mangled_methods, i);
                    jsize size = (*env)->GetStringLength(env, str);
            
                    char* strData = malloc(size + 1);
                    (*env)->GetStringUTFRegion(env, str, 0, size, strData);
                    strData[size] = 0;
                    result[i] = strData;
                }
                return result;
            }
            
            static void JNI_free_mangled_methods(JNIEnv *env, jobject mangled_methods, char** mangled_names) {
                for (jsize i = 0; i < (*env)->GetArrayLength(env, mangled_methods); i++)
                    free(mangled_names[i]);
                free(mangled_names);
            }
            
            JNIEXPORT void JNICALL Java_${classPath.replace(".", "_")}_${name}_nJNILoad(JNIEnv *env, jclass clazz, jobject mangled_methods$isCritical) {
                char** mangled_names = JNI_read_mangled_methods(env, mangled_methods);
                JNINativeMethod* methods = malloc(sizeof(JNINativeMethod) * ${operations.size});
        """.trimIndent())

        operations.forEachIndexed { index, function ->
            val funcName = function.jniName()
            val funcDesc = function.toJavaDesc(classPath)
            val nl = "\n\t\t"

            append("\n\tmethods[$index] = (JNINativeMethod) {$nl")
            if(isAndroid && isAndroidCriticalEnabled && function.isCritical() && function.isAndroidCriticalCapable()) {
                val criticalFuncName = funcName + "_"
                val criticalFuncDesc = function.toJavaDesc(classPath, isCritical = true)

                append("mangled_names[$index],$nl")
                append("critical ? \"$criticalFuncDesc\" : \"!${funcDesc}\",$nl")
                append("critical ? (void*)&$criticalFuncName : (void*)&$funcName")
            } else
                append("mangled_names[$index],$nl\"$funcDesc\",$nl(void*)&$funcName")

            append("\n\t};")
        }

        append("""
            
                JNI_Init(env, methods, ${operations.size});
                free(methods);
                JNI_free_mangled_methods(env, mangled_methods, mangled_names);
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
                "${it.type.toJNIType()} __jvm_${it.name.snakeCase()}"
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
                append("\n\t${it.type.toCType().padEnd(typeMaxLength)} __native_${it.name.snakeCase()} = ")
                if(it.type.isNullable && !it.type.isPrimitive() && !it.type.isEnum())
                    append("__jvm_${it.name.snakeCase()} == 0 ? NULL : ")
                append(castKotlinToJNI(
                    it.type,
                    "__jvm_${it.name.snakeCase()}",
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
                "__jvm_${it.name.snakeCase()}"
            else "__native_${it.name.snakeCase()}"
        }
        val call = "${mangle(classPath, moduleName, function.name)}($args)"

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
                classPath, moduleName,
                function.type,
                "result_native"
            )?.apply { append("\n\t$this;") }
        }

        // ==================
        //    Release args
        // ==================

        function.args.forEach { arg ->
            val type = arg.type
            val name = "__native_${arg.name.snakeCase()}"
            when {
                type.isString() -> "JNI_release_kstring_on_stack(env, $name)"
                type.isArray() -> type.arrayType { type ->
                    when {
                        type.isPrimitive() -> "JNI_release_${type.toCType(ignoreUnsigned = true).lowercase()}array_on_stack(env, ${castToSignedC(arg.type, name)})"
                        type.isEnum() -> "JNI_release_enum_array_on_stack($name)"
                        else -> "JNI_release_karray_on_stack($name, (void*) ${freeFuncFor(classPath, moduleName, type, "")!!.dropLast(2)})"
                    }
                }
                type.isDictionary() -> forceFreeFuncFor(classPath, moduleName, type, name)
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
            builder, classPath, moduleName,
            name = "${function.jniName()}_",
            function
        )
    }

    private fun ResolvedIdlOperation.jniName() =
        "JNI__${name.snakeCase()}"
}

internal fun castJniToKotlin(
    type: ResolvedIdlType,
    content: String
): String = when {
    type.isUnsigned() -> castJniToKotlin(type.toSignedType(), castToSignedC(type, content))
    type.isEnum() -> "JNI_to_kotlin_enum(env, $content, class_${type.toCType().lowercase()}, values_${type.toCType().lowercase()})"
    type.isDictionary() -> "JNI_to_kotlin_${type.toCType(ptr = false).lowercase()}(env, $content)"
    type.isString() -> "JNI_to_kotlin_kstring(env, $content)"
    type.isCallback() -> "JNI_to_kotlin_callback(env, (_AbstractCallback*) $content)"
    type.isInterface() -> "JNI_to_kotlin_${type.declaration.name.lowercase()}(env, $content)"
    type.isArray() -> type.arrayType { type ->
        when {
            type.isPrimitive() -> "JNI_to_kotlin_${type.toCType(ptr = false).lowercase()}array(env, $content)"
            type.isEnum() -> "JNI_to_kotlin_enum_array(env, $content, class_${type.toCType().lowercase()}, values_${type.toCType().lowercase()})"
            else -> {
                val cast = castJniToKotlin(type, "").split("(")[0]
                "JNI_to_kotlin_karray(env, $content, (jobject(*)(JNIEnv*, void*)) $cast, class_${type.toKotlinType(printNullable = false).lowercase()})"
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
    type.isEnum() -> "JNI_to_native_enum(env, $content)"
    type.isDictionary() -> "JNI_to_native_${type.toCType(ptr = false).lowercase()}(env, $content, $flags)"
    type.isString() ->
        if(onStack) "JNI_to_native_kstring_on_stack(env, $content, alloca(JNI_STRING_STACK_SIZE))"
        else "JNI_to_native_kstring(env, $content, $flags)"
    type.isCallback() -> {
        val name = type.toCType(ptr = false)
        if (onStack) "($name*) JNI_to_native_callback_on_stack(env, $content, (void(*)()) JNI_CALLBACK_INVOKE_${name.lowercase()}, alloca(JNI_ABSTRACT_CALLBACK_SIZE))"
        else "($name*) JNI_to_native_callback(env, $content, (void(*)()) JNI_CALLBACK_INVOKE_${name.lowercase()}, $flags)"
    }
    type.isInterface() -> "JNI_to_native__interface(env, $content)"
    type.isArray() -> type.arrayType { type ->
        when {
            type.isPrimitive() ->
                if(onStack) "JNI_to_native_${type.toCType(ptr = false).lowercase()}array_on_stack(env, $content, alloca(JNI_ARRAY_STACK_SIZE))"
                else "JNI_to_native_${type.toCType(ptr = false).lowercase()}array(env, $content, $flags)"
            type.isEnum() ->
                if (onStack) "JNI_to_native_enum_array_on_stack(env, $content, alloca(JNI_ARRAY_STACK_SIZE))"
                else "JNI_to_native_enum_array(env, $content, $flags)"
            else -> {
                val castFunc = castKotlinToJNI(type, "", false, "").split("(")[0]
                if(onStack) "JNI_to_native_karray_on_stack(env, $content, (void*(*)(JNIEnv*, jobject, char)) $castFunc, alloca(JNI_ARRAY_STACK_SIZE))"
                else "JNI_to_native_karray(env, $content, (void*(*)(JNIEnv*, jobject, char)) $castFunc, $flags)"
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