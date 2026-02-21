package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.castJavaToJNI
import com.huskerdev.nativekt.utils.castJniToJava
import com.huskerdev.nativekt.utils.isDealloc
import com.huskerdev.nativekt.utils.toCType
import com.huskerdev.nativekt.utils.toJavaDesc
import com.huskerdev.webidl.resolver.BuiltinIdlDeclaration
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlCallbackFunction
import com.huskerdev.webidl.resolver.ResolvedIdlType
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind
import java.io.File

class CJniUtilsPrinter(
    val idl: IdlResolver,
    target: File,
    val classPath: String,
    val name: String = "JNI"
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
            
        """.trimIndent())

        builder.append("""
            
            /* =================== *\
                      Casts
            \* =================== */
            
            jobject JNI_toJvmString(JNIEnv *env, const char* ptr, bool dealloc) {
                jobject result = (*env)->NewStringUTF(env, ptr);
                if(dealloc) free((void*)ptr);
                return result;
            }
            
            const char* JNI_toNativeString(JNIEnv *env, jobject obj) {
                const char* temp = (*env)->GetStringUTFChars(env, obj, NULL);
                const char* copy = strdup(temp);
                (*env)->ReleaseStringUTFChars(env, obj, temp);
                return copy;
            }
            
        """.trimIndent())

        if(idl.callbacks.isNotEmpty()) {

            builder.append("""
                
                /* =================== *\
                        Callbacks
                \* =================== */
                
            """.trimIndent())
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
                        (*jvm)->AttachCurrentThread(jvm, (void**)env, NULL);
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
        val args = listOf("${callback.name}* callback") +
                callback.args.map { "${it.type.toCType()} ${it.name}" }

        val jvmArgs = listOf("(jobject)callback->m") +
                callback.args.map { castJniToJava(it.type, it.name, it.isDealloc(), false) }

        append("""
            
            ${callback.type.toCType()} JNI_CALLBACK_${callback.name}_invoke(${args.joinToString()}) {
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
                    WebIDLBuiltinKind.UNRESTRICTED_FLOAT,
                    WebIDLBuiltinKind.FLOAT -> "CallStaticFloatMethod"
                    WebIDLBuiltinKind.UNRESTRICTED_DOUBLE,
                    WebIDLBuiltinKind.DOUBLE -> "CallStaticDoubleMethod"
                    else -> "CallStaticObjectMethod"
                }
                else -> throw UnsupportedOperationException()
            }
            else -> throw UnsupportedOperationException()
        }

        val call = "(*env)->$funcName(env, jniClass, callback${callback.name}, ${jvmArgs.joinToString()})"

        if(callback.type !is ResolvedIdlType.Void) {
            append(callback.type.toCType())
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
        append("""
            
            /* =================== *\
                  Init function
            \* =================== */
            
            jint JNI_Init(JavaVM *vm, JNINativeMethod *methods, jint count) {
                jvm = vm;
                
                JNIEnv *env;
                (*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6);
                
                jniClass = (*env)->FindClass(env, "${(classPath.split(".") + name).joinToString(separator = "/")}");
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