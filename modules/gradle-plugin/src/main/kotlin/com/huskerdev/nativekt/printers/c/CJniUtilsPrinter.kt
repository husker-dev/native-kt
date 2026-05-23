package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.utils.allFields
import com.huskerdev.nativekt.utils.isDealloc
import com.huskerdev.nativekt.utils.isDeallocContent
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

        printLabel(builder, "String")
        builder.append("""
            
            jclass stringClass;
            jmethodID stringConstructor;
            typedef struct KString KString;
            
            jstring JNI_toKotlinString(JNIEnv *env, KString str, bool dealloc) {
                int32_t length = str.length;
            
                jbyteArray bytes = (*env)->NewByteArray(env, length);
                (*env)->SetByteArrayRegion(env, bytes, 0, length, (jbyte*)str.data);
            
                jstring result = (jstring)(*env)->NewObject(env, stringClass, stringConstructor, bytes);
                (*env)->DeleteLocalRef(env, bytes);
                
                if(dealloc) free((void*)str.data);
                return result;
            }
            
            KString JNI_toNativeString(JNIEnv *env, jstring obj, bool releasable) {
                jsize length = (*env)->GetStringLength(env, obj);
                const char* temp = (*env)->GetStringUTFChars(env, obj, NULL);
                const char* copy = strdup(temp);
                (*env)->ReleaseStringUTFChars(env, obj, temp);
                return (KString) { copy, length, releasable, false };
            }
            
        """.trimIndent())

        printLabel(builder, "Primitive Arrays")
        builder.append("""

            #define KArrayCast(Name, JType)                                                           \
            K##Name##Array JNI_toNative##Name##Array(JNIEnv *env, JType##Array arr, bool releasable) {\
                jsize size = (*env)->GetArrayLength(env, arr);                                        \
                JType* tmp = (*env)->Get##Name##ArrayElements(env, arr, NULL);                        \
                const JType* copy = (JType*)malloc(size * sizeof(JType));                             \
                memcpy((void*)copy, (void*)tmp, size * sizeof(JType));                                \
                (*env)->Release##Name##ArrayElements(env, arr, tmp, JNI_ABORT);                       \
                return (K##Name##Array) { (K##Name*)copy, size, releasable, false };                  \
            }                                                                                         \
                                                                                                      \
            JType##Array JNI_toKotlin##Name##Array(JNIEnv *env, K##Name##Array arr, bool dealloc) {   \
                JType##Array result = (*env)->New##Name##Array(env, arr.size);                        \
                (*env)->Set##Name##ArrayRegion(env, result, 0, arr.size, (JType*)arr.elements);       \
                if(dealloc) free((void*)arr.elements);                                                \
                return result;                                                                        \
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
                
            jobjectArray JNI_toKotlinArray(
                JNIEnv *env, 
                KArray src, 
                jobject (*converter)(JNIEnv*, void*, bool),
                jclass clazz,
                bool dealloc,
                bool deallocContent
            ) {
                jobjectArray result = (*env)->NewObjectArray(env, src.size, clazz, NULL);
                void** elements = (void**)src.elements;
                for(int i = 0; i < src.size; i++)
                    (*env)->SetObjectArrayElement(env, result, i, converter(env, elements[i], deallocContent));
                if(dealloc) free((void*) src.elements);
                return result;
            }

            KArray JNI_toNativeArray(
                JNIEnv *env, 
                jobjectArray src,
                void* (*converter)(JNIEnv*, jobject, bool),
                bool releasable
            ) {
                int length = (*env)->GetArrayLength(env, src);
                void** elements = malloc(length * sizeof(void*));
                for(int i = 0; i < length; i++)
                    elements[i] = converter(env, (*env)->GetObjectArrayElement(env, src, i), releasable);
                return (KArray){ (const void**) elements, length, releasable, false };
            }
            
        """.trimIndent())

        if(idl.enums.isNotEmpty()) {
            printLabel(builder, "Enum casts")
            builder.append("\njmethodID enumOrdinal;")

            idl.enums.values.joinTo(builder, prefix = "\njclass ", postfix = ";") {
                "enum${it.name}Class"
            }
            idl.enums.values.joinTo(builder, prefix = "\njmethodID ", postfix = ";") {
                "enum${it.name}Values"
            }
            builder.append("""
                
                
                KInt JNI_toNativeEnum(JNIEnv* env, jobject of) {
                	return (*env)->CallIntMethod(env, of, enumOrdinal);
                }
                
                jobject JNI_toKotlinEnum(JNIEnv* env, KInt of, jclass clazz, jmethodID valuesMethod) {
                	jobjectArray values = (jobjectArray) (*env)->CallStaticObjectMethod(env, clazz, valuesMethod);
                	jobject result = (*env)->GetObjectArrayElement(env, values, of);
                	(*env)->DeleteLocalRef(env, values);
                	return result;
                }

                KIntArray JNI_toNativeEnumArray(
                	JNIEnv *env,
                	jobjectArray src,
                    bool releasable
                ) {
                	int length = (*env)->GetArrayLength(env, src);
                	KInt* elements = malloc(length * sizeof(KInt));
                	for(int i = 0; i < length; i++)
                		elements[i] = JNI_toNativeEnum(env, (*env)->GetObjectArrayElement(env, src, i));
                	return (KIntArray){ elements, length, releasable, false };
                }
                
                jobjectArray JNI_toKotlinEnumArray(
                    JNIEnv *env,
                    KIntArray src,
                    jclass clazz,
                    jmethodID valuesMethod,
                    bool dealloc
                ) {
                    jobjectArray result = (*env)->NewObjectArray(env, src.size, clazz, NULL);
                    const KInt* elements = src.elements;
                    for(int i = 0; i < src.size; i++)
                        (*env)->SetObjectArrayElement(env, result, i, JNI_toKotlinEnum(env, elements[i], clazz, valuesMethod));
                    if(dealloc) free((void*) src.elements);
                    return result;
                }
                
            """.trimIndent())
        }

        if(idl.callbacks.isNotEmpty()) {
            printLabel(builder, "Callback casts")
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
                
                void JNI_freeCallback(JNI_Callback* callback) {
                    JNIEnv *env;
                    jint status = JVM_attach(&env);
                
                    (*env)->DeleteGlobalRef(env, (jobject)callback->m);
                    free((void*)callback);
                
                    JVM_detach(status);
                }
                
                jobject JNI_toKotlinCallback(JNIEnv *env, JNI_Callback* callback, bool dealloc) {
                    jobject result = (jobject)callback->m;
                    if(dealloc) JNI_freeCallback(callback);
                    return result;
                }
                
                JNI_Callback* JNI_toNativeCallback(JNIEnv *env, jobject obj, void (*invoke)()) {
                	JNI_Callback* callback = (JNI_Callback*)malloc(sizeof(JNI_Callback));
                	callback->invoke = invoke;
                	callback->free = JNI_freeCallback;
                	callback->m = (void*)(*env)->NewGlobalRef(env, obj);
                	return callback;
                }
                
                
            """.trimIndent())
            idl.callbacks.values.forEach { printCallbackInvokeDef(builder, it) }
        }

        if(idl.dictionaries.isNotEmpty()) {
            printLabel(builder, "Structs")
            printStructs(builder)
        }

        if(idl.callbacks.isNotEmpty()) {
            printLabel(builder, "Callback invokes")
            builder.append("\n")
            idl.callbacks.values
                .map { "callback${it.name}Invoke" }
                .chunked(4)
                .joinTo(builder, prefix = "jmethodID ", separator = ",\n\t", postfix = ";\n") { it.joinToString() }

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
            .map { "struct${it.name}Companion" }
            .chunked(3)
            .joinTo(builder, prefix = "jobject ", separator = ",\n\t", postfix = ";\n") { it.joinToString() }

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
            append("jobject JNI_toKotlinDictionary")
            append(struct.name)
            append("(JNIEnv *env, ")
            append(struct.name)
            append("* src, bool dealloc) {\n\t")
            append("jobject result = (*env)->CallObjectMethod(env, ")
            append("struct")
            append(struct.name)
            append("Companion, struct")
            append(struct.name)
            append("Constructor, \n\t\t")
            struct.allFields().joinTo(builder, separator = ",\n\t\t") {
                castJniToJava(it.type, "src->${it.name}", dealloc = false, deallocContent = false, useArena = false)
            }
            append("\n\t);\n\t")
            append("if (dealloc) free((void*) src);\n\t")
            append("return result;\n")
            append("}\n")

            // to Native
            append("\n")
            append(struct.name)
            append("* JNI_toNativeDictionary")
            append(struct.name)
            append("(JNIEnv *env, jobject src, bool releasable) {\n\t")
            append(struct.name)
            append("* result = malloc(sizeof(").append(struct.name).append("));\n\t")
            append("*result = (").append(struct.name).append(") {\n\t\t")
            struct.allFields().joinTo(builder, separator = ",\n\t\t") { field ->
                val fieldVariable = "struct${struct.name}Field${field.name.capitalized()}"
                val getter = field.type.toMethodCall()
                castJavaToJNI(field.type, "(*env)->$getter(env, src, $fieldVariable)", dealloc = false, useArena = false, releasable = "releasable")
            }
            append("\n\t};")
            append("\n\treturn result;\n}\n")
        }
    }

    private fun printCallbackInvokeDef(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val args = listOf("${callback.name}* _callback") +
                callback.args.map { "${it.type.toCDefType()} ${it.name}" }

        append("${callback.type.toCDefType()} JNI_CALLBACK_INVOKE_${callback.name}(${args.joinToString()});\n")
    }

    private fun printCallbackInvoke(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val args = listOf("${callback.name}* _callback") +
                callback.args.map { "${it.type.toCDefType()} ${it.name}" }

        var jvmArgs = callback.args.joinToString {
            castJniToJava(it.type, it.name, it.isDealloc(), it.isDeallocContent(), false)
        }
        if(jvmArgs.isNotEmpty())
            jvmArgs = ", $jvmArgs"

        append("""
            
            ${callback.type.toCDefType()} JNI_CALLBACK_INVOKE_${callback.name}(${args.joinToString()}) {
                JNIEnv *env;
                jint __status = JVM_attach(&env);
                
        """.trimIndent())

        val funcName = callback.type.toMethodCall()
        val call = "(*env)->$funcName(env, (jobject)_callback->m, callback${callback.name}Invoke$jvmArgs)"

        if(callback.type !is ResolvedIdlType.Void) {
            append(callback.type.toCDefType())
            append(" __result = ")
            append(castJavaToJNI(callback.type, call, dealloc = false, useArena = false, releasable = "true"))
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
            
            void JNI_Init(JNIEnv *env, JNINativeMethod *methods, jint count) {
                (*env)->GetJavaVM(env, &jvm);
                
                jniClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "${(classPath.split(".") + name).joinToString(separator = "/")}"));
                (*env)->RegisterNatives(env, jniClass, methods, count);
                
                // String
                stringClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "java/lang/String"));
                stringConstructor = (*env)->GetMethodID(env, stringClass, "<init>", "([B)V");
                
        """.replaceIndent())

        if(idl.enums.isNotEmpty()) {
            append("\n\t")
            append("// Enums")
            append("\n\tenumOrdinal = (*env)->GetMethodID(env, (*env)->FindClass(env, \"java/lang/Enum\"), \"ordinal\", \"()I\");\n")
            idl.enums.values.forEach { enum ->
                val classPath = "${classPath.replace(".", "/")}/${enum.name}"
                val classFieldName = "enum${enum.name}Class"
                val valuesFieldName = "enum${enum.name}Values"

                append("\n\t$classFieldName = (*env)->NewGlobalRef(env, (*env)->FindClass(env, \"$classPath\"));")
                append("\n\t$valuesFieldName = (*env)->GetStaticMethodID(env, $classFieldName, \"values\", \"()[L$classPath;\");\n")
            }
        }

        if(idl.dictionaries.isNotEmpty()) {
            append("\n\t")
            append("// Struct")
            idl.dictionaries.values.forEach { struct ->
                val structClassPath = "${classPath.replace(".", "/")}/${struct.name}"
                val classFieldName = "struct${struct.name}Class"
                val companionFieldName = "struct${struct.name}Companion"
                val constructorFieldName = "struct${struct.name}Constructor"

                append("\n\t")

                append(classFieldName)
                append(" = (*env)->NewGlobalRef(env, (*env)->FindClass(env, \"")
                append(structClassPath)
                append("\"));\n\t")


                append(companionFieldName)
                append(" = (*env)->NewGlobalRef(env, (*env)->GetStaticObjectField(env, ")
                append(classFieldName)
                append(", (*env)->GetStaticFieldID(env, ")
                append(classFieldName)
                append(", \"Companion\", \"L")
                append(structClassPath)
                append($$"$Companion;\")));\n\t")

                append(constructorFieldName)
                append(" = (*env)->GetMethodID(env, (*env)->FindClass(env, \"")
                append(structClassPath)
                append($$"$Companion\"), \"of\", \"(")
                struct.allFields().joinTo(builder, separator = "") { d -> d.type.toJavaDesc(classPath) }
                append(")L")
                append(structClassPath)
                append(";\");\n\t")

                struct.allFields().joinTo(builder, separator = "\n\t") { field ->
                    val fieldVariableName = "struct${struct.name}Field${field.name.capitalized()}"
                    "$fieldVariableName = (*env)->GetMethodID(env, $classFieldName, \"get${field.name.capitalized()}\", \"()${field.type.toJavaDesc(classPath)}\");"
                }
                append("\n")
            }
        }

        // Lookup callback functions
        if(idl.callbacks.isNotEmpty()) {
            append("\n\t")
            append("// Callbacks\n\t")
            idl.callbacks.values.forEach {
                val args = it.args.joinToString(separator = "") { d -> d.type.toJavaDesc(classPath) }
                val ret = it.type.toJavaDesc(classPath)
                val path = classPath.replace(".", "/") + "/" + it.name

                append("callback${it.name}Invoke = (*env)->GetMethodID(env, (*env)->FindClass(env, \"$path\"), \"invoke\", \"($args)$ret\");\n\t")
            }
        }

        append("\n}")
    }
}

private fun ResolvedIdlType.toMethodCall() = when(this) {
    is ResolvedIdlType.Void -> "CallVoidMethod"
    is ResolvedIdlType.Default -> when(val decl = declaration) {
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
        is ResolvedIdlCallbackFunction -> "CallObjectMethod"
        is ResolvedIdlEnum -> "CallObjectMethod"
        is ResolvedIdlDictionary -> "CallObjectMethod"
        else -> "CallObjectMethod"
    }
    else -> throw UnsupportedOperationException(toString())
}