package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.utils.allFields
import com.huskerdev.nativekt.utils.printLabel
import com.huskerdev.nativekt.utils.toCType
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
            
            #define K_FLAG_ON_STACK 8
            
            #define K_OBJECT_IS_ON_STACK(flags) ((flags) & K_FLAG_ON_STACK)
            
            JavaVM *jvm;
            jclass jniClass;
            
            jmethodID objectEquals, objectHashCode;
            
        """.trimIndent())

        printLabel(builder, "String")
        builder.append("""
            
            jclass classString;
            jmethodID stringConstructor, stringGetBytes;
            jstring stringUTF8Const;
            typedef struct KString KString;
            
            const size_t JNI_StringStackSize = sizeof(KString) + sizeof(intptr_t) * 2;

            jstring JNI_toKotlinKString(JNIEnv *env, KString* str) {
                if(str == NULL) 
                    return NULL;
                if(K_OBJECT_IS_ON_STACK(str->__flags))
                    return (jstring)((intptr_t*)((char*)str + sizeof(KString)))[0];
                
                int32_t size = str->size;
                
                jbyteArray bytes = (*env)->NewByteArray(env, size);
                (*env)->SetByteArrayRegion(env, bytes, 0, size, (jbyte*)str->data);
            
                jstring result = (jstring)(*env)->NewObject(env, classString, stringConstructor, bytes, stringUTF8Const);
                (*env)->DeleteLocalRef(env, bytes);
                return result;
            }
            
            KString* JNI_toNativeKStringOnStack(JNIEnv *env, jstring obj, void* mem) {
                jbyteArray bytes = (jbyteArray) (*env)->CallObjectMethod(env, obj, stringGetBytes, stringUTF8Const);
                ((size_t*)((char*)mem + sizeof(KString)))[0] = (size_t) obj;
                ((size_t*)((char*)mem + sizeof(KString)))[1] = (size_t) bytes;
                KString* result = (KString*)mem;
                *result = (KString) {
                    (const char*)(*env)->GetByteArrayElements(env, bytes, JNI_FALSE),
                    (*env)->GetArrayLength(env, bytes),
                    (*env)->GetStringLength(env, obj),
                    K_FLAG_ON_STACK
                };
                return result;
            }
            
            void JNI_releaseKStringOnStack(JNIEnv *env, KString* str) {
                if(str == NULL)
                    return;
                jbyteArray bytes = (jbyteArray)((size_t*)((char*)str + sizeof(KString)))[1];
                (*env)->ReleaseByteArrayElements(env, bytes, (jbyte*) str->data, JNI_ABORT);
            }
            
            KString* JNI_toNativeKString(JNIEnv *env, jstring obj, char flags) {
                if(obj == NULL)
                    return NULL;
                jbyteArray bytes = (jbyteArray) (*env)->CallObjectMethod(env, obj, stringGetBytes, stringUTF8Const);
                jsize length = (*env)->GetStringLength(env, obj);
                jsize size = (*env)->GetArrayLength(env, bytes);
            
                jbyte* str = (*env)->GetByteArrayElements(env, bytes, JNI_FALSE);
                void* strCopy = malloc(size);
                memcpy(strCopy, (void*)str, size);
            
                (*env)->ReleaseByteArrayElements(env, bytes, str, JNI_ABORT);
                (*env)->DeleteLocalRef(env, bytes);
                
                KString* result = (KString*) malloc(sizeof(KString));
                *result = (KString) {
                    (const char*) strCopy,
                    size,
                    length,
                    flags
                };
                return result;
            }
            
        """.trimIndent())

        printLabel(builder, "Primitive Arrays")
        builder.append("""

            const size_t JNI_ArrayStackSize = sizeof(KArray) + sizeof(size_t);

            #define KArrayCast(Name, JType)                                                              \
            K##Name##Array* JNI_toNativeK##Name##Array(JNIEnv *env, JType##Array arr, char flags) {      \
                if(arr == NULL)                                                                          \
                    return NULL;                                                                         \
                KInt length = (*env)->GetArrayLength(env, arr);                                          \
                size_t size = length * sizeof(JType);                                                    \
                                                                                                         \
                JType* elements = (*env)->Get##Name##ArrayElements(env, arr, JNI_FALSE);                 \
                void* elementsCopy = malloc(size);                                                       \
                memcpy(elementsCopy, (void*)elements, size);                                             \
                (*env)->Release##Name##ArrayElements(env, arr, elements, JNI_ABORT);                     \
                                                                                                         \
                K##Name##Array* result = (K##Name##Array*) malloc(sizeof(K##Name##Array));               \
                *result = (K##Name##Array) {                                                             \
                    (K##Name*)elementsCopy,                                                              \
                    size,                                                                                \
                    length,                                                                              \
                    flags                                                                                \
                };                                                                                       \
                return result;                                                                           \
            }                                                                                            \
                                                                                                         \
            K##Name##Array* JNI_toNativeK##Name##ArrayOnStack(JNIEnv *env, JType##Array arr, void* mem) {\
                ((size_t*)((char*)mem + sizeof(K##Name##Array)))[0] = (size_t) arr;                      \
                KInt length = (*env)->GetArrayLength(env, arr);                                          \
                K##Name##Array* result = (K##Name##Array*)mem;                                           \
                *result = (K##Name##Array) {                                                             \
                    (K##Name*)(*env)->Get##Name##ArrayElements(env, arr, JNI_FALSE),                     \
                    length * sizeof(JType),                                                              \
                    length,                                                                              \
                    K_FLAG_ON_STACK                                                                      \
                };                                                                                       \
                return result;                                                                           \
            }                                                                                            \
                                                                                                         \
            void JNI_releaseK##Name##ArrayOnStack(JNIEnv *env, K##Name##Array* arr) {                    \
                if(arr == NULL)                                                                          \
                    return;                                                                              \
                JType##Array elements = (JType##Array)*((size_t*)((char*)arr + sizeof(K##Name##Array))); \
                (*env)->Release##Name##ArrayElements(env, elements, (JType*)arr->elements, JNI_ABORT);   \
            }                                                                                            \
                                                                                                         \
            JType##Array JNI_toKotlinK##Name##Array(JNIEnv *env, K##Name##Array* arr) {                  \
                if(arr == NULL)                                                                          \
                    return NULL;                                                                         \
                if(K_OBJECT_IS_ON_STACK(arr->__flags))                                                   \
                    return (JType##Array)*((size_t*)((char*)arr + sizeof(K##Name##Array)));              \
                JType##Array result = (*env)->New##Name##Array(env, arr->length);                        \
                (*env)->Set##Name##ArrayRegion(env, result, 0, arr->length, (JType*)arr->elements);      \
                return result;                                                                           \
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
                
            KArray* JNI_toNativeKArray(
                JNIEnv *env, 
                jobjectArray src,
                void* (*converter)(JNIEnv*, jobject, char),
                char flags
            ) {
                if(src == NULL)
                    return NULL;
                int length = (*env)->GetArrayLength(env, src);
                void** elements = malloc(length * sizeof(void*));
                for(int i = 0; i < length; i++) {
                    jobject obj = (*env)->GetObjectArrayElement(env, src, i);
                    elements[i] = converter(env, obj, flags);
                    (*env)->DeleteLocalRef(env, obj);
                }
                
                KArray* result = (KArray*) malloc(sizeof(KArray));
                *result = (KArray) {
                    (const void**) elements,
                    length * sizeof(void*),
                    length,
                    flags,
                };
                return result;
            }
            
            KArray* JNI_toNativeKArrayOnStack(
                JNIEnv *env,
                jobjectArray src,
                void* (*converter)(JNIEnv*, jobject, char),
                void* mem
            ) {
                ((size_t*)((char*)mem + sizeof(KArray)))[0] = (size_t) src;
                KInt length = (*env)->GetArrayLength(env, src);
                
                void** elements = malloc(length * sizeof(void*));
                for(int i = 0; i < length; i++) {
                    jobject obj = (*env)->GetObjectArrayElement(env, src, i);
                    elements[i] = converter(env, obj, 0);
                    (*env)->DeleteLocalRef(env, obj);
                }
                
                KArray* result = (KArray*)mem;
                *result = (KArray) {
                    (const void**) elements,
                    length * sizeof(void*),
                    length,
                    K_FLAG_ON_STACK
                };
                return result;
            }
            
            void JNI_releaseKArrayOnStack(
            	KArray* self,
                void (*free_op)(void*)
            ) {
                if(self == NULL)
                    return;
                self->__flags |= K_FLAG_DATA_OWNER;
            	KArray_free(self, free_op);
            }
            
            jobjectArray JNI_toKotlinKArray(
                JNIEnv *env, 
                KArray* src, 
                jobject (*converter)(JNIEnv*, void*),
                jclass clazz
            ) {
                if(src == NULL)
                    return NULL;
                if(K_OBJECT_IS_ON_STACK(src->__flags))
                    return (jobjectArray)*((size_t*)((char*)src + sizeof(KArray)));
                
                jobjectArray result = (*env)->NewObjectArray(env, src->length, clazz, NULL);
                void** elements = (void**)src->elements;
                for(int i = 0; i < src->length; i++)
                    (*env)->SetObjectArrayElement(env, result, i, converter(env, elements[i]));
                return result;
            }
            
        """.trimIndent())

        if(idl.enums.isNotEmpty()) {
            printLabel(builder, "Enum casts")
            builder.append("\njmethodID enumOrdinal;")

            idl.enums.values.joinTo(builder, prefix = "\njclass ", postfix = ";") {
                "class${it.name}"
            }
            idl.enums.values.joinTo(builder, prefix = "\njmethodID ", postfix = ";") {
                "values${it.name}"
            }
            builder.append("""
                

                KInt JNI_toNativeEnum(JNIEnv* env, jobject of) {
                    return (*env)->CallIntMethod(env, of, enumOrdinal);
                }
                
                jobject JNI_toKotlinEnum(
                    JNIEnv* env, 
                    KInt of, 
                    jclass clazz, 
                    jmethodID valuesMethod
                ) {
                    jobjectArray values = (jobjectArray) (*env)->CallStaticObjectMethod(env, clazz, valuesMethod);
                    jobject result = (*env)->GetObjectArrayElement(env, values, of);
                    (*env)->DeleteLocalRef(env, values);
                    return result;
                }
                
                KIntArray* JNI_toNativeEnumArray(
                    JNIEnv *env,
                    jobjectArray src, 
                    char flags
                ) {
                    if(src == NULL)
                        return NULL;
                    int length = (*env)->GetArrayLength(env, src);
                    KInt* elements = malloc(length * sizeof(KInt));
                    for(int i = 0; i < length; i++) {
                        jobject obj = (*env)->GetObjectArrayElement(env, src, i);
                        elements[i] = JNI_toNativeEnum(env, (*env)->GetObjectArrayElement(env, src, i));
                        (*env)->DeleteLocalRef(env, obj);
                    }
                    
                    KIntArray* result = (KIntArray*) malloc(sizeof(KIntArray));
                    *result = (KIntArray) {
                        (const KInt*) elements,
                        length * sizeof(KInt),
                        length,
                        flags
                    };
                    return result;
                }
                
                KIntArray* JNI_toNativeEnumArrayOnStack(
                    JNIEnv *env,
                    jobjectArray src,
                    void* mem
                ) {
                    ((size_t*)((char*)mem + sizeof(KArray)))[0] = (size_t) src;
                    
                    int length = (*env)->GetArrayLength(env, src);
                    KInt* elements = malloc(length * sizeof(KInt));
                    for(int i = 0; i < length; i++) {
                        jobject obj = (*env)->GetObjectArrayElement(env, src, i);
                        elements[i] = JNI_toNativeEnum(env, (*env)->GetObjectArrayElement(env, src, i));
                        (*env)->DeleteLocalRef(env, obj);
                    }
                    
                    KIntArray* result = (KIntArray*)mem;
                    *result = (KIntArray) {
                        (const KInt*) elements,
                        length * sizeof(KInt),
                        length,
                        K_FLAG_ON_STACK
                    };
                    return result;
                }
                
                void JNI_releaseEnumArrayOnStack(KIntArray* self) {
                	if(self == NULL)
                		return;
                	self->__flags |= K_FLAG_DATA_OWNER;
                	KIntArray_free(self);
                }
                
                jobjectArray JNI_toKotlinEnumArray(
                    JNIEnv *env,
                    KIntArray* src,
                    jclass clazz,
                    jmethodID valuesMethod
                ) {
                    if(src == NULL)
                        return NULL;
                    if(K_OBJECT_IS_ON_STACK(src->__flags))
                        return (jobjectArray)*((size_t*)((char*)src + sizeof(KArray)));
                    
                    jobjectArray result = (*env)->NewObjectArray(env, src->length, clazz, NULL);
                    const KInt* elements = src->elements;
                    for(int i = 0; i < src->length; i++)
                        (*env)->SetObjectArrayElement(env, result, i, JNI_toKotlinEnum(env, elements[i], clazz, valuesMethod));
                    return result;
                }
                
            """.trimIndent())
        }

        if(idl.callbacks.isNotEmpty()) {
            printLabel(builder, "Callback casts")
            builder.append("""
                
                const size_t _AbstractCallbackSize = sizeof(_AbstractCallback) + sizeof(size_t)*2;
                
                static jint JVM_attach(JNIEnv **env) {
                    jint status = (*jvm)->GetEnv(jvm, (void**)env, JNI_VERSION_1_6);
                    if (status == JNI_EDETACHED)
                        (*jvm)->AttachCurrentThread(jvm, ${if(!isAndroid) "(void**) " else "" }env, NULL);
                    return status;
                }
                
                static inline void JVM_detach(jint status) {
                    if (status == JNI_EDETACHED)
                        (*jvm)->DetachCurrentThread(jvm);
                }
                
                static inline KBoolean JNI_CALLBACK_equals(_AbstractCallback* ref, _AbstractCallback* with) {
                	jobject obj = (jobject)((size_t*)((char*)ref + sizeof(_AbstractCallback)))[0];
                	jobject obj2 = (jobject)((size_t*)((char*)with + sizeof(_AbstractCallback)))[0];
                    JNIEnv *env = (JNIEnv*)((size_t*)((char*)ref + sizeof(_AbstractCallback)))[1];
                	
                	jint status = env == NULL ? JVM_attach(&env) : JNI_OK;
                	KBoolean result = (*env)->CallBooleanMethod(env, obj, objectEquals, obj2);
                	if(status == JNI_EDETACHED) JVM_detach(status);
                	return result;
                }

                static inline KInt JNI_CALLBACK_hashCode(_AbstractCallback* ref) {
                	jobject obj = (jobject)((size_t*)((char*)ref + sizeof(_AbstractCallback)))[0];
                	JNIEnv *env = (JNIEnv*)((size_t*)((char*)ref + sizeof(_AbstractCallback)))[1];
                	
                	jint status = env == NULL ? JVM_attach(&env) : JNI_OK;
                	KInt result = (*env)->CallIntMethod(env, obj, objectHashCode);
                	if(status == JNI_EDETACHED) JVM_detach(status);
                	return result;
                }
                
                static void JNI_CALLBACK_free(_AbstractCallback* callback) {
                    if (callback == NULL || !K_OBJECT_IS_RELEASABLE(callback->__flags))
                        return;
                
                    JNIEnv *env;
                    jint status = JVM_attach(&env);
                    
                    jobject obj = (jobject)((size_t*)((char*)callback + sizeof(_AbstractCallback)))[0];
                    (*env)->DeleteGlobalRef(env, obj);
                    free((void*)callback);
                    
                    JVM_detach(status);
                }
                
                static _AbstractCallback* JNI_CALLBACK_clone(_AbstractCallback* ref) {
                    if(ref == NULL)
                        return NULL;
                    jobject obj = (jobject)((size_t*)((char*)ref + sizeof(_AbstractCallback)))[0];
                	JNIEnv *env = (JNIEnv*)((size_t*)((char*)ref + sizeof(_AbstractCallback)))[1];
                    jint status = env == NULL ? JVM_attach(&env) : JNI_OK;
                    
                    _AbstractCallback* callback = (_AbstractCallback*) malloc(_AbstractCallbackSize);
                    memcpy(callback, ref, sizeof(_AbstractCallback));
                    callback->__flags = K_FLAG_RELEASABLE;
                    
                    ((size_t*)((char*)callback + sizeof(_AbstractCallback)))[0] = (size_t)(*env)->NewGlobalRef(env, obj);
                    ((size_t*)((char*)callback + sizeof(_AbstractCallback)))[1] = (size_t) NULL;
                    
                    if(status == JNI_EDETACHED) JVM_detach(status);
                    return callback;
                }
                
                jobject JNI_toKotlinCallback(JNIEnv *env, _AbstractCallback* callback) {
                    if(callback == NULL)
                        return NULL;
                    jobject ref = (jobject)((size_t*)((char*)callback + sizeof(_AbstractCallback)))[0];
                    return (*env)->NewLocalRef(env, ref);
                }
                
                _AbstractCallback* JNI_toNativeCallbackOnStack(JNIEnv *env, jobject obj, void (*invoke)(), void* mem) {
                    ((size_t*)((char*)mem + sizeof(_AbstractCallback)))[0] = (size_t) obj;
                    ((size_t*)((char*)mem + sizeof(_AbstractCallback)))[1] = (size_t) env;
                    _AbstractCallback* callback = (_AbstractCallback*) mem;
                    *callback = (_AbstractCallback) {
                        0,
                        invoke,
                        JNI_CALLBACK_clone,
                        JNI_CALLBACK_equals,
                        JNI_CALLBACK_hashCode,
                        JNI_CALLBACK_free
                    };
                    return callback;
                }
                
                _AbstractCallback* JNI_toNativeCallback(JNIEnv *env, jobject obj, void (*invoke)(), char flags) {
                    if(obj == NULL)
                        return NULL;
                    void* mem = malloc(_AbstractCallbackSize);
                    ((size_t*)((char*)mem + sizeof(_AbstractCallback)))[0] = (size_t) (*env)->NewGlobalRef(env, obj);
                    ((size_t*)((char*)mem + sizeof(_AbstractCallback)))[1] = (size_t) NULL;
                    _AbstractCallback* callback = (_AbstractCallback*) mem;
                    *callback = (_AbstractCallback) {
                        flags,
                        invoke,
                        JNI_CALLBACK_clone,
                        JNI_CALLBACK_equals,
                        JNI_CALLBACK_hashCode,
                        JNI_CALLBACK_free
                    };
                    return callback;
                }
                
                
            """.trimIndent())
            idl.callbacks.values.forEach {
                printCallbackInvokeDef(builder, it)
            }
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
            .map { "class${it.name}" }
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
            append("\njobject JNI_toKotlin${struct.name}(JNIEnv *env, ${struct.name}* src) {\n\t")
            append("if(src == NULL) return NULL;\n\t")
            append("return (*env)->CallObjectMethod(env, struct${struct.name}Companion, struct${struct.name}Constructor, \n\t\t")

            struct.allFields().joinTo(builder, separator = ",\n\t\t") {
                castJniToJava(it.type, "src->${it.name}")
            }
            append("\n\t);\n}\n")

            // to Native
            append("\n${struct.name}* JNI_toNative${struct.name}(JNIEnv *env, jobject src, char flags) {\n\t")
            append("if(src == NULL) return NULL;\n\t")
            append("${struct.name}* result = malloc(sizeof(${struct.name}));\n\t")
            append("*result = (${struct.name}) {\n\t\t")

            buildList {
                struct.allFields().mapTo(this) { field ->
                    val fieldVariable = "struct${struct.name}Field${field.name.capitalized()}"
                    val getter = field.type.toMethodCall()
                    castJavaToJNI(field.type, "(*env)->$getter(env, src, $fieldVariable)", onStack = false, flags = "flags")
                }
                add("flags")
            }.joinTo(builder, separator = ",\n\t\t")
            append("\n\t};")
            append("\n\treturn result;\n}\n")
        }
    }

    private fun printCallbackInvokeDef(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val args = listOf("${callback.name}* _callback") +
                callback.args.map { "${it.type.toCType()} ${it.name}" }

        append("${callback.type.toCType()} JNI_CALLBACK_INVOKE_${callback.name}(${args.joinToString()});\n")
    }

    private fun printCallbackInvoke(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val args = buildList {
            add("${callback.name}* callback")
            callback.args.mapTo(this) {
                "${it.type.toCType()} __${it.name}"
            }
        }.joinToString()

        val jvmArgs = buildList {
            add("callback${callback.name}Invoke")
            callback.args.mapTo(this) {
                castJniToJava(it.type, "__${it.name}")
            }
        }.joinToString()

        append("""
            
            ${callback.type.toCType()} JNI_CALLBACK_INVOKE_${callback.name}(${args}) {
                jobject obj = (jobject)((size_t*)((char*)callback + sizeof(_AbstractCallback)))[0];
                JNIEnv *env = (JNIEnv*)((size_t*)((char*)callback + sizeof(_AbstractCallback)))[1];
                jint status = env == NULL ? JVM_attach(&env) : JNI_OK;
                
        """.trimIndent())

        val call = "(*env)->${callback.type.toMethodCall()}(env, obj, $jvmArgs)"

        if(callback.type !is ResolvedIdlType.Void) {
            append(callback.type.toCType())
            append(" result = ")
            append(castJavaToJNI(callback.type, call, onStack = false, flags = "K_FLAG_RELEASABLE"))
        } else
            append(call)

        append(";\n\tif(status == JNI_EDETACHED) JVM_detach(status);\n")

        if(callback.type !is ResolvedIdlType.Void)
            append("\treturn result;\n")
        append("}\n")
    }

    private fun printRegisterFunction(builder: StringBuilder) = builder.apply {
        printLabel(builder, "Init function")
        append("""
            
            static void JNI_Init(JNIEnv *env, JNINativeMethod *methods, jint count) {
                (*env)->GetJavaVM(env, &jvm);
                
                jniClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "${(classPath.split(".") + name).joinToString(separator = "/")}"));
                (*env)->RegisterNatives(env, jniClass, methods, count);
                
                // Object
                jclass objectClass = (*env)->FindClass(env, "java/lang/Object");
                objectEquals = (*env)->GetMethodID(env, objectClass, "equals", "(Ljava/lang/Object;)Z");
                objectHashCode = (*env)->GetMethodID(env, objectClass, "hashCode", "()I");
                
                // String
                classString = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "java/lang/String"));
                stringConstructor = (*env)->GetMethodID(env, classString, "<init>", "([BLjava/lang/String;)V");
                stringGetBytes = (*env)->GetMethodID(env, classString, "getBytes", "(Ljava/lang/String;)[B");
                stringUTF8Const = (*env)->NewGlobalRef(env, (*env)->NewStringUTF(env, "UTF-8"));
                
        """.replaceIndent())

        if(idl.enums.isNotEmpty()) {
            append("\n\t")
            append("// Enums")
            append("\n\tenumOrdinal = (*env)->GetMethodID(env, (*env)->FindClass(env, \"java/lang/Enum\"), \"ordinal\", \"()I\");\n")
            idl.enums.values.forEach { enum ->
                val classPath = "${classPath.replace(".", "/")}/${enum.name}"
                val classFieldName = "class${enum.name}"
                val valuesFieldName = "values${enum.name}"

                append("\n\t$classFieldName = (*env)->NewGlobalRef(env, (*env)->FindClass(env, \"$classPath\"));")
                append("\n\t$valuesFieldName = (*env)->GetStaticMethodID(env, $classFieldName, \"values\", \"()[L$classPath;\");\n")
            }
        }

        if(idl.dictionaries.isNotEmpty()) {
            append("\n\t")
            append("// Struct")
            idl.dictionaries.values.forEach { struct ->
                val structClassPath = "${classPath.replace(".", "/")}/${struct.name}"
                val classFieldName = "class${struct.name}"
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