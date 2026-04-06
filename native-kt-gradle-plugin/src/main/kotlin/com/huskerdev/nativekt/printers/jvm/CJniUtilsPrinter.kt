package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.allFields
import com.huskerdev.nativekt.utils.isDealloc
import com.huskerdev.nativekt.utils.printLabel
import com.huskerdev.nativekt.utils.toCDefType
import com.huskerdev.nativekt.utils.toJavaDesc
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized
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
            
        """.trimIndent())

        if(idl.dictionaries.isNotEmpty()) {
            printLabel(builder, "Structs")
            printStructs(builder)
        }

        printLabel(builder, "String")
        builder.append("""
            
            jclass stringClass;
            jmethodID stringConstructor;
            typedef struct KString KString;
            
            jstring JNI_toJvmString(JNIEnv *env, KString str, bool dealloc) {
                int32_t length = str.length;
            
                jbyteArray bytes = (*env)->NewByteArray(env, length);
                (*env)->SetByteArrayRegion(env, bytes, 0, length, (jbyte*)str.data);
            
                jstring result = (jstring)(*env)->NewObject(env, stringClass, stringConstructor, bytes);
                (*env)->DeleteLocalRef(env, bytes);
                
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
                (*env)->Release##Name##ArrayElements(env, arr, tmp, JNI_ABORT);                     \
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

        printLabel(builder, "Object array")
        builder.append("""
                
            jobjectArray JNI_toJvmArray(
                JNIEnv *env, 
                KArray src, 
                jobject (*converter)(JNIEnv*, void*),
                jclass clazz,
                bool dealloc
            ) {
                jobjectArray result = (*env)->NewObjectArray(env, src.size, clazz, NULL);
                void** elements = (void**)src.elements;
                for(int i = 0; i < src.size; i++)
                    (*env)->SetObjectArrayElement(env, result, i, converter(env, elements[i]));
                if(dealloc) free((void*) src.elements);
                return result;
            }

            KArray JNI_toNativeArray(
                JNIEnv *env, 
                jobjectArray src,
                void* (*converter)(JNIEnv*, jobject)
            ) {
                int length = (*env)->GetArrayLength(env, src);
                void** elements = malloc(length * sizeof(void*));
                for(int i = 0; i < length; i++)
                    elements[i] = converter(env, (*env)->GetObjectArrayElement(env, src, i));
                return (KArray){ (const void**) elements, length };
            }
            
        """.trimIndent())

        if(idl.callbacks.isNotEmpty()) {
            printLabel(builder, "Callbacks")
            builder.append("\n")
            idl.callbacks.values
                .map { "callback${it.name}" }
                .chunked(4)
                .joinTo(builder, prefix = "jmethodID ", separator = ",\n\t", postfix = ";\n") { it.joinToString() }

            builder.append("\n")
            builder.append("""
                
                typedef struct JNI_Callback {
                    void *m;
                    void (*invoke)();
                    void (*free)(struct JNI_Callback*);
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
                    jobject result = (jobject)callback->m;
                    if(dealloc) JNI_CALLBACK_free(callback);
                    return result;
                }
                
                JNI_Callback* JNI_toNativeCallback(JNIEnv *env, jobject obj, void (*invoke)()) {
                	JNI_Callback* callback = (JNI_Callback*)malloc(sizeof(JNI_Callback));
                	callback->invoke = invoke;
                	callback->free = JNI_CALLBACK_free;
                	callback->m = (void*)(*env)->NewGlobalRef(env, obj);
                	return callback;
                }
                
            """.trimIndent())
            idl.callbacks.values.forEach { callback ->
                printCallbackInvoke(builder, callback)
            }
        }

        printRegisterFunction(builder)

        builder.append("\n\n#endif // KOTLIN_NATIVE_JNI_UTILS_H")

        target.writeText(builder.toString())
    }

    private fun printStructs(builder: StringBuilder) = builder.apply {
        append("\n")
        idl.dictionaries.values
            .map { "struct${it.name}Class" }
            .chunked(3)
            .joinTo(builder, prefix = "jclass ", separator = ",\n\t", postfix = ";\n") { it.joinToString() }

        idl.dictionaries.values
            .map { "struct${it.name}Constructor" }
            .chunked(3)
            .joinTo(builder, prefix = "jmethodID ", separator = ",\n\t", postfix = ";\n") { it.joinToString() }

        idl.dictionaries.values
            .flatMap { dict ->
                dict.allFields().map {
                    "struct${dict.name}Field${it.name.capitalized()}"
                }
            }
            .chunked(3)
            .joinTo(builder, prefix = "jmethodID ", separator = ",\n\t", postfix = ";\n") { it.joinToString() }

        idl.dictionaries.values.forEach { struct ->
            append("\n// ${struct.name}\n")

            // to JVM
            append("\n")
            append("jobject JNI_STRUCT_toJvm")
            append(struct.name)
            append("(JNIEnv *env, ")
            append(struct.name)
            append("* src) {\n\t")
            append("return (*env)->CallStaticObjectMethod(env, ")
            append("struct")
            append(struct.name)
            append("Class, struct")
            append(struct.name)
            append("Constructor, \n\t\t")
            struct.allFields().joinTo(builder, separator = ",\n\t\t") {
                castJniToJava(it.type, "src->${it.name}", dealloc = false, useArena = false)
            }
            append("\n\t);\n}\n")

            // to Native
            append("\n")
            append(struct.name)
            append("* JNI_STRUCT_toNative")
            append(struct.name)
            append("(JNIEnv *env, jobject src) {\n\t")
            append(struct.name)
            append("* result = malloc(sizeof(").append(struct.name).append("));\n\t")
            append("*result = (").append(struct.name).append(") {\n\t\t")
            struct.allFields().joinTo(builder, separator = ",\n\t\t") { field ->
                val fieldVariable = "struct${struct.name}Field${field.name.capitalized()}"
                val getter = when(field.type) {
                    is ResolvedIdlType.Default -> when(val decl = (field.type as ResolvedIdlType.Default).declaration) {
                        is BuiltinIdlDeclaration -> when(decl.kind) {
                            WebIDLBuiltinKind.BOOLEAN -> "CallBooleanMethod"
                            WebIDLBuiltinKind.BYTE -> "CallByteMethod"
                            WebIDLBuiltinKind.CHAR -> "CallCharMethod"
                            WebIDLBuiltinKind.SHORT -> "CallShortMethod"
                            WebIDLBuiltinKind.INT -> "CallIntMethod"
                            WebIDLBuiltinKind.LONG -> "CallLongMethod"
                            WebIDLBuiltinKind.FLOAT -> "CallFloatMethod"
                            WebIDLBuiltinKind.DOUBLE -> "CallDoubleMethod"
                            else -> "CallObjectMethod"
                        }
                        else -> "CallObjectMethod"
                    }
                    else -> throw UnsupportedOperationException(field.type.toString())
                }
                castJavaToJNI(field.type, "(*env)->$getter(env, src, $fieldVariable)", dealloc = false, useArena = false)
            }
            append("\n\t};")
            append("\n\treturn result;\n}\n")
        }
    }

    private fun printCallbackInvoke(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val args = listOf("${callback.name}* _callback") +
                callback.args.map { "${it.type.toCDefType()} ${it.name}" }

        val jvmArgs = listOf("(jobject)_callback->m") +
                callback.args.map { castJniToJava(it.type, it.name, it.isDealloc(), false) }

        append("""
            
            ${callback.type.toCDefType()} JNI_CALLBACK_INVOKE_${callback.name}(${args.joinToString()}) {
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
                is ResolvedIdlDictionary -> "CallStaticObjectMethod"
                else -> throw UnsupportedOperationException(callback.type.toString())
            }
            else -> throw UnsupportedOperationException(callback.type.toString())
        }

        val call = "(*env)->$funcName(env, jniClass, callback${callback.name}, ${jvmArgs.joinToString()})"

        if(callback.type !is ResolvedIdlType.Void) {
            append(callback.type.toCDefType())
            append(" __result = ")
            append(castJavaToJNI(callback.type, call, dealloc = false, useArena = false))
        } else
            append(call)

        append(";\n")

        append("\tJVM_detach(__status);\n")

        if(callback.type !is ResolvedIdlType.Void)
            append("\treturn __result;\n")
        append("}\n")
    }

    private fun printRegisterFunction(builder: StringBuilder) = builder.apply {
        printLabel(builder, "Init function")
        append("""
            
            jint JNI_Init(JavaVM *vm, JNINativeMethod *methods, jint count) {
                jvm = vm;
                
                JNIEnv *env;
                (*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_6);
                
                jniClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "${(classPath.split(".") + name).joinToString(separator = "/")}"));
                (*env)->RegisterNatives(env, jniClass, methods, count);
                
                // String
                stringClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "java/lang/String"));
                stringConstructor = (*env)->GetMethodID(env, stringClass, "<init>", "([B)V");
                
        """.replaceIndent())

        if(idl.dictionaries.isNotEmpty()) {
            append("\n\t")
            append("// Struct")
            idl.dictionaries.values.forEach { struct ->
                val structClassPath = "${classPath.replace(".", "/")}/${struct.name}"
                val classFieldName = "struct${struct.name}Class"
                val constructorFieldName = "struct${struct.name}Constructor"

                append("\n\t")
                append(classFieldName)
                append(" = (*env)->NewGlobalRef(env, (*env)->FindClass(env, \"")
                append(structClassPath)
                append("\"));\n\t")

                append(constructorFieldName)
                append(" = (*env)->GetStaticMethodID(env, ")
                append(classFieldName)
                append(", \"of\", \"(")
                struct.allFields().joinTo(builder, separator = "") { d -> d.type.toJavaDesc() }
                append(")L")
                append(structClassPath)
                append(";\");\n\t")

                struct.allFields().joinTo(builder, separator = "\n\t") { field ->
                    val fieldVariableName = "struct${struct.name}Field${field.name.capitalized()}"
                    "$fieldVariableName = (*env)->GetMethodID(env, $classFieldName, \"get${field.name.capitalized()}\", \"()${field.type.toJavaDesc()}\");"
                }
                append("\n")
            }
        }

        // Lookup callback functions
        if(idl.callbacks.isNotEmpty()) {
            append("\n\t")
            append("// Callbacks\n\t")
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

        append("\n\treturn JNI_VERSION_1_8;\n")
        append("}")
    }
}