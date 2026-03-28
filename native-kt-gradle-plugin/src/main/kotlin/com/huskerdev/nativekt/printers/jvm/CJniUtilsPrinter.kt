package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.isDealloc
import com.huskerdev.nativekt.utils.printLabel
import com.huskerdev.nativekt.utils.toCDefType
import com.huskerdev.nativekt.utils.toJavaDesc
import com.huskerdev.webidl.resolver.*
import java.io.File

class CJniUtilsPrinter(
    val idl: IdlResolver,
    target: File,
    val classPath: String,
    val name: String,
    val isAndroid: Boolean
) {
    init {
        val builder = StringBuilder()
        builder.append("""
            #ifndef KOTLIN_NATIVE_JNI_UTILS_H
            #define KOTLIN_NATIVE_JNI_UTILS_H
            
            #include <jni.h>
            #include <stdlib.h>
            #include <string.h>
            #include "api.h"
            
            JavaVM *jvm;
            jclass jniClass;
            jclass stringClass;
            jmethodID stringConstructor;
            
            typedef struct KString KString;
            
            jstring JNI_createJString(JNIEnv *env, KString str) {
                int32_t length = str.length;

                jbyteArray bytes = (*env)->NewByteArray(env, length);
                (*env)->SetByteArrayRegion(env, bytes, 0, length, (jbyte*)str.data);
                
                jstring result = (jstring)(*env)->NewObject(env, stringClass, stringConstructor, bytes);
                (*env)->DeleteLocalRef(env, bytes);
                return result;
            }
            
        """.trimIndent())

        printLabel(builder, "String")
        builder.append("""

            jstring JNI_toJvmString(JNIEnv *env, KString str, bool dealloc) {
                jstring result = JNI_createJString(env, str);
                if(dealloc) free((void*)str.data);
                return result;
            }
            
            KString JNI_toNativeString(JNIEnv *env, jstring obj) {
                jsize length = (*env)->GetStringLength(env, obj);
                const char* temp = (*env)->GetStringUTFChars(env, obj, NULL);
                const char* copy = strdup(temp);
                (*env)->ReleaseStringUTFChars(env, obj, temp);
                return (KString) { copy, length };
            }
            
        """.trimIndent())

        printLabel(builder, "Primitive Arrays")
        builder.append("""

            #define KArrayCast(Name, JType)                                                         \
            K##Name##Array JNI_toNative##Name##Array(JNIEnv *env, JType##Array arr) {               \
                jsize size = (*env)->GetArrayLength(env, arr);                                      \
                JType* tmp = (*env)->Get##Name##ArrayElements(env, arr, NULL);                      \
                const JType* copy = (JType*)malloc(size * sizeof(JType));                           \
                memcpy((void*)copy, (void*)tmp, size * sizeof(JType));                              \
                (*env)->Release##Name##ArrayElements(env, arr, tmp, 0);                             \
                return (K##Name##Array) { (K##Name*)copy, size };                                   \
            }                                                                                       \
                                                                                                    \
            JType##Array JNI_toJvm##Name##Array(JNIEnv *env, K##Name##Array arr, bool dealloc) {    \
                JType##Array result = (*env)->New##Name##Array(env, arr.size);                      \
                (*env)->Set##Name##ArrayRegion(env, result, 0, arr.size, (JType*)arr.elements);     \
                if(dealloc) free((void*)arr.elements);                                              \
                return result;                                                                      \
            }

            KArrayCast(Char,    jchar)
            KArrayCast(Boolean, jboolean)
            KArrayCast(Byte,    jbyte)
            KArrayCast(Short,   jshort)
            KArrayCast(Int,     jint)
            KArrayCast(Long,    jlong)
            KArrayCast(Float,   jfloat)
            KArrayCast(Double,  jdouble)

            #undef KArrayCast
            
        """.trimIndent())
        printLabel(builder, "Arrays")
        builder.append("""
            
            KArray JNI_toNativeEnumArray(JNIEnv *env, jintArray arr) {
                jsize size = (*env)->GetArrayLength(env, arr);
                jint* tmp = (*env)->GetIntArrayElements(env, arr, NULL);
                const jint* copy = (jint*)malloc(size * sizeof(jint));
                memcpy((void*)copy, (void*)tmp, size * sizeof(jint));
                (*env)->ReleaseIntArrayElements(env, arr, tmp, 0);
                return (KArray) { (void*)copy, size };
            }

            jintArray JNI_toJvmEnumArray(JNIEnv *env, KArray arr, bool dealloc) {
                jintArray result = (*env)->NewIntArray(env, arr.size);
                (*env)->SetIntArrayRegion(env, result, 0, arr.size, (jint*)arr.elements);
                if(dealloc) free((void*)arr.elements);
                return result;
            }
            
        """.trimIndent())

        if(idl.callbacks.isNotEmpty()) {

            printLabel(builder, "Callbacks")
            builder.append("\n")
            idl.callbacks.values.joinTo(builder, separator = "\n") {
                "jmethodID callback${it.name};"
            }

            builder.append("\n")
            builder.append("""
                
                typedef struct JNI_Callback {
                    void *m;
                } JNI_Callback;
                
                static jint JVM_attach(JNIEnv **env) {
                    jint status = (*jvm)->GetEnv(jvm, (void**)env, JNI_VERSION_1_6);
                    if (status == JNI_EDETACHED)
                        (*jvm)->AttachCurrentThread(jvm, (${if(isAndroid) "JNIEnv**" else "void**"})env, NULL);
                    return status;
                }

                static inline void JVM_detach(jint status) {
                    if (status == JNI_EDETACHED)
                        (*jvm)->DetachCurrentThread(jvm);
                }
                
                void JNI_CALLBACK_free(JNI_Callback* callback) {
                    JNIEnv *env;
                    jint status = JVM_attach(&env);
                
                    (*env)->DeleteGlobalRef(env, (jobject)callback->m);
                    free((void*)callback);
                
                    JVM_detach(status);
                }
                
                jobject JNI_toJvmCallback(JNIEnv *env, JNI_Callback* callback, bool dealloc) {
                    jobject result = (*env)->NewLocalRef(env, (jobject)callback->m);
                    if(dealloc) JNI_CALLBACK_free(callback);
                    return result;
                }
                
            """.trimIndent())
            idl.callbacks.values.forEach { callback ->
                builder.append("""
                    
                    /*------ ${callback.name} ------*/
                    
                """.trimIndent())
                printCallbackInvoke(builder, callback)
                printCallbackCreate(builder, callback)
            }
        }

        printRegisterFunction(builder)

        builder.append("\n\n#endif // KOTLIN_NATIVE_JNI_UTILS_H")

        target.writeText(builder.toString())
    }

    private fun printCallbackInvoke(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val args = listOf("${callback.name}* _callback") +
                callback.args.map { "${it.type.toCDefType()} ${it.name}" }

        val jvmArgs = listOf("(jobject)_callback->m") +
                callback.args.map { castJniToJava(it.type, it.name, it.isDealloc(), false) }

        append("""
            
            ${callback.type.toCDefType()} JNI_CALLBACK_${callback.name}_invoke(${args.joinToString()}) {
                JNIEnv *env;
                jint __status = JVM_attach(&env);
                
        """.trimIndent())

        val funcName = when(callback.type) {
            is ResolvedIdlType.Void -> "CallStaticVoidMethod"
            is ResolvedIdlType.Default -> when(val decl = (callback.type as ResolvedIdlType.Default).declaration) {
                is BuiltinIdlDeclaration -> when(decl.kind) {
                    WebIDLBuiltinKind.BOOLEAN -> "CallStaticBooleanMethod"
                    WebIDLBuiltinKind.BYTE -> "CallStaticByteMethod"
                    WebIDLBuiltinKind.CHAR -> "CallStaticCharMethod"
                    WebIDLBuiltinKind.SHORT -> "CallStaticShortMethod"
                    WebIDLBuiltinKind.INT -> "CallStaticIntMethod"
                    WebIDLBuiltinKind.LONG -> "CallStaticLongMethod"
                    WebIDLBuiltinKind.FLOAT -> "CallStaticFloatMethod"
                    WebIDLBuiltinKind.DOUBLE -> "CallStaticDoubleMethod"
                    else -> "CallStaticObjectMethod"
                }
                is ResolvedIdlCallbackFunction -> "CallStaticObjectMethod"
                is ResolvedIdlEnum -> "CallStaticIntMethod"
                else -> throw UnsupportedOperationException(callback.type.toString())
            }
            else -> throw UnsupportedOperationException(callback.type.toString())
        }

        val call = "(*env)->$funcName(env, jniClass, callback${callback.name}, ${jvmArgs.joinToString()})"

        if(callback.type !is ResolvedIdlType.Void) {
            append(callback.type.toCDefType())
            append(" __result = ")
            append(castJavaToJNI(callback.type, call, critical = false, dealloc = false, useArena = false))
        } else
            append(call)

        append(";\n")

        append("\tJVM_detach(__status);\n")

        if(callback.type !is ResolvedIdlType.Void)
            append("\treturn __result;\n")
        append("}\n")
    }

    private fun printCallbackCreate(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        append("""
                
            ${callback.name}* JNI_wrap${callback.name}(JNIEnv *env, jobject obj) {
                ${callback.name}* callback = (${callback.name}*)malloc(sizeof(${callback.name}));
                callback->invoke = JNI_CALLBACK_${callback.name}_invoke;
                callback->free = (void (*)(${callback.name}*))JNI_CALLBACK_free;
                callback->m = (void*)(*env)->NewGlobalRef(env, obj);
                return callback;
            }                    
            
        """.trimIndent())
    }

    private fun printRegisterFunction(builder: StringBuilder) = builder.apply {
        printLabel(builder, "Init function")
        append("""
            
            jint JNI_Init(JavaVM *vm, JNINativeMethod *methods, jint count) {
                jvm = vm;
                
                JNIEnv *env;
                (*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6);
                
                jniClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "${(classPath.split(".") + name).joinToString(separator = "/")}"));
                stringClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "java/lang/String"));
                stringConstructor = (*env)->GetMethodID(env, stringClass, "<init>", "([B)V");
                
                (*env)->RegisterNatives(env, jniClass, methods, count);
                
        """.replaceIndent())

        // Lookup callback functions
        if(idl.callbacks.isNotEmpty()) {
            append("\n\t")
            idl.callbacks.values.forEach {
                append("callback")
                append(it.name)
                append(" = (*env)->GetStaticMethodID(env, jniClass, \"callback")
                append(it.name)
                append("\", \"(Ljava/lang/Object;")
                it.args.joinTo(builder, separator = "") { d -> d.type.toJavaDesc() }
                append(")")
                append(it.type.toJavaDesc())
                append("\");\n\t")
            }
        }

        append("\n\treturn JNI_VERSION_1_6;\n")
        append("}")
    }
}