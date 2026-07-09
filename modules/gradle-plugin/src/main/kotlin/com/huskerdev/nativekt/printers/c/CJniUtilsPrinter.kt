package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.utils.allFields
import com.huskerdev.nativekt.utils.isUnsigned
import com.huskerdev.nativekt.utils.printLabel
import com.huskerdev.nativekt.utils.snakeCase
import com.huskerdev.nativekt.utils.toCType
import com.huskerdev.nativekt.utils.upperCamelCase
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File

class CJniUtilsPrinter(
    val idl: IdlResolver,
    target: File,
    val classPath: String,
    val moduleName: String,
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
            jclass jni_class;
            
            jmethodID object_equals, object_hash_code;
            
        """.trimIndent())

        printLabel(builder, "String")
        builder.append("""
            
            jclass class_string;
            jmethodID string_constructor, string_get_bytes;
            jstring string_utf8_const;
            typedef struct KString KString;
            
            const size_t JNI_STRING_STACK_SIZE = sizeof(KString) + sizeof(intptr_t) * 2;

            jstring JNI_to_kotlin_kstring(JNIEnv *env, KString* str) {
                if(str == NULL) 
                    return NULL;
                if(K_OBJECT_IS_ON_STACK(str->__flags))
                    return (jstring)((intptr_t*)((char*)str + sizeof(KString)))[0];
                
                int32_t size = str->size;
                
                jbyteArray bytes = (*env)->NewByteArray(env, size);
                (*env)->SetByteArrayRegion(env, bytes, 0, size, (jbyte*)str->data);
            
                jstring result = (jstring)(*env)->NewObject(env, class_string, string_constructor, bytes, string_utf8_const);
                (*env)->DeleteLocalRef(env, bytes);
                return result;
            }
            
            KString* JNI_to_native_kstring_on_stack(JNIEnv *env, jstring obj, void* mem) {
                jbyteArray bytes = (jbyteArray) (*env)->CallObjectMethod(env, obj, string_get_bytes, string_utf8_const);
                ((size_t*)((char*)mem + sizeof(KString)))[0] = (size_t) obj;
                ((size_t*)((char*)mem + sizeof(KString)))[1] = (size_t) bytes;
                KString* result = (KString*) mem;
                *result = (KString) {
                    (const char*) (*env)->GetByteArrayElements(env, bytes, JNI_FALSE),
                    (*env)->GetArrayLength(env, bytes),
                    (*env)->GetStringLength(env, obj),
                    K_FLAG_ON_STACK
                };
                return result;
            }
            
            void JNI_release_kstring_on_stack(JNIEnv *env, KString* str) {
                if(str == NULL)
                    return;
                jbyteArray bytes = (jbyteArray)((size_t*)((char*)str + sizeof(KString)))[1];
                (*env)->ReleaseByteArrayElements(env, bytes, (jbyte*) str->data, JNI_ABORT);
            }
            
            KString* JNI_to_native_kstring(JNIEnv *env, jstring obj, char flags) {
                if(obj == NULL)
                    return NULL;
                jbyteArray bytes = (jbyteArray) (*env)->CallObjectMethod(env, obj, string_get_bytes, string_utf8_const);
                jsize length = (*env)->GetStringLength(env, obj);
                jsize size = (*env)->GetArrayLength(env, bytes);
            
                jbyte* str = (*env)->GetByteArrayElements(env, bytes, JNI_FALSE);
                void* str_copy = malloc(size);
                memcpy(str_copy, (void*) str, size);
            
                (*env)->ReleaseByteArrayElements(env, bytes, str, JNI_ABORT);
                (*env)->DeleteLocalRef(env, bytes);
                
                KString* result = (KString*) malloc(sizeof(KString));
                *result = (KString) {
                    (const char*) str_copy,
                    size,
                    length,
                    flags
                };
                return result;
            }
            
        """.trimIndent())

        printLabel(builder, "Primitive Arrays")
        builder.append("""

            const size_t JNI_ARRAY_STACK_SIZE = sizeof(KArray) + sizeof(size_t);

            #define KArrayCast(Name, DName, JType)                                                       \
            K##Name##Array* JNI_to_native_k##DName##array(                                               \
                JNIEnv *env,                                                                             \
                JType##Array arr,                                                                        \
                char flags                                                                               \
            ) {                                                                                          \
                if(arr == NULL)                                                                          \
                    return NULL;                                                                         \
                KInt length = (*env)->GetArrayLength(env, arr);                                          \
                size_t size = length * sizeof(JType);                                                    \
                                                                                                         \
                JType* elements = (*env)->Get##Name##ArrayElements(env, arr, JNI_FALSE);                 \
                void* elements_copy = malloc(size);                                                      \
                memcpy(elements_copy, (void*) elements, size);                                           \
                (*env)->Release##Name##ArrayElements(env, arr, elements, JNI_ABORT);                     \
                                                                                                         \
                K##Name##Array* result = (K##Name##Array*) malloc(sizeof(K##Name##Array));               \
                *result = (K##Name##Array) {                                                             \
                    (K##Name*) elements_copy,                                                            \
                    size,                                                                                \
                    length,                                                                              \
                    flags                                                                                \
                };                                                                                       \
                return result;                                                                           \
            }                                                                                            \
                                                                                                         \
            K##Name##Array* JNI_to_native_k##DName##array_on_stack(                                      \
                JNIEnv *env,                                                                             \
                JType##Array arr,                                                                        \
                void* mem                                                                                \
            ) {                                                                                          \
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
            void JNI_release_k##DName##array_on_stack(JNIEnv *env, K##Name##Array* arr) {                \
                if(arr == NULL)                                                                          \
                    return;                                                                              \
                JType##Array elements = (JType##Array)*((size_t*)((char*)arr + sizeof(K##Name##Array))); \
                (*env)->Release##Name##ArrayElements(env, elements, (JType*)arr->elements, JNI_ABORT);   \
            }                                                                                            \
                                                                                                         \
            JType##Array JNI_to_kotlin_k##DName##array(JNIEnv *env, K##Name##Array* arr) {               \
                if(arr == NULL)                                                                          \
                    return NULL;                                                                         \
                if(K_OBJECT_IS_ON_STACK(arr->__flags))                                                   \
                    return (JType##Array)*((size_t*)((char*)arr + sizeof(K##Name##Array)));              \
                JType##Array result = (*env)->New##Name##Array(env, arr->length);                        \
                (*env)->Set##Name##ArrayRegion(env, result, 0, arr->length, (JType*)arr->elements);      \
                return result;                                                                           \
            }

            KArrayCast(Char,    char,    jchar)
            KArrayCast(Boolean, boolean, jboolean)
            KArrayCast(Byte,    byte,    jbyte)
            KArrayCast(Short,   short,   jshort)
            KArrayCast(Int,     int,     jint)
            KArrayCast(Long,    long,    jlong)
            KArrayCast(Float,   float,   jfloat)
            KArrayCast(Double,  double,  jdouble)

            #undef KArrayCast
            
        """.trimIndent())

        printLabel(builder, "Object array")
        builder.append("""
                
            KArray* JNI_to_native_karray(
                JNIEnv *env, 
                jobjectArray src,
                void* (*converter)(JNIEnv*, jobject, char),
                char flags
            ) {
                if(src == NULL)
                    return NULL;
                jsize length = (*env)->GetArrayLength(env, src);
                void** elements = malloc(length * sizeof(void*));
                for(jsize i = 0; i < length; i++) {
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
            
            KArray* JNI_to_native_karray_on_stack(
                JNIEnv *env,
                jobjectArray src,
                void* (*converter)(JNIEnv*, jobject, char),
                void* mem
            ) {
                ((size_t*)((char*)mem + sizeof(KArray)))[0] = (size_t) src;
                jsize length = (*env)->GetArrayLength(env, src);
                
                void** elements = malloc(length * sizeof(void*));
                for(jsize i = 0; i < length; i++) {
                    jobject obj = (*env)->GetObjectArrayElement(env, src, i);
                    elements[i] = converter(env, obj, 0);
                    (*env)->DeleteLocalRef(env, obj);
                }
                
                KArray* result = (KArray*) mem;
                *result = (KArray) {
                    (const void**) elements,
                    length * sizeof(void*),
                    length,
                    K_FLAG_ON_STACK
                };
                return result;
            }
            
            void JNI_release_karray_on_stack(
            	KArray* self,
                void (*free_op)(void*)
            ) {
                if(self == NULL)
                    return;
                self->__flags |= K_FLAG_DATA_OWNER;
            	${mangle("KArray_free")}(self, free_op);
            }
            
            jobjectArray JNI_to_kotlin_karray(
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
                void** elements = (void**) src->elements;
                for(int i = 0; i < src->length; i++)
                    (*env)->SetObjectArrayElement(env, result, i, converter(env, elements[i]));
                return result;
            }
            
        """.trimIndent())

        if(idl.enums.isNotEmpty()) {
            printLabel(builder, "Enum casts")
            builder.append("\njmethodID enum_ordinal;")

            idl.enums.values.joinTo(builder, prefix = "\njclass ", postfix = ";") {
                "class_${it.name.lowercase()}"
            }
            idl.enums.values.joinTo(builder, prefix = "\njmethodID ", postfix = ";") {
                "values_${it.name.lowercase()}"
            }
            builder.append("""
                

                KInt JNI_to_native_enum(JNIEnv* env, jobject of) {
                    return (*env)->CallIntMethod(env, of, enum_ordinal);
                }
                
                jobject JNI_to_kotlin_enum(
                    JNIEnv* env, 
                    KInt of, 
                    jclass clazz, 
                    jmethodID values_method
                ) {
                    jobjectArray values = (jobjectArray) (*env)->CallStaticObjectMethod(env, clazz, values_method);
                    jobject result = (*env)->GetObjectArrayElement(env, values, of);
                    (*env)->DeleteLocalRef(env, values);
                    return result;
                }
                
                KIntArray* JNI_to_native_enum_array(
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
                        elements[i] = JNI_to_native_enum(env, (*env)->GetObjectArrayElement(env, src, i));
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
                
                KIntArray* JNI_to_native_enum_array_on_stack(
                    JNIEnv *env,
                    jobjectArray src,
                    void* mem
                ) {
                    ((size_t*)((char*)mem + sizeof(KArray)))[0] = (size_t) src;
                    
                    int length = (*env)->GetArrayLength(env, src);
                    KInt* elements = malloc(length * sizeof(KInt));
                    for(int i = 0; i < length; i++) {
                        jobject obj = (*env)->GetObjectArrayElement(env, src, i);
                        elements[i] = JNI_to_native_enum(env, (*env)->GetObjectArrayElement(env, src, i));
                        (*env)->DeleteLocalRef(env, obj);
                    }
                    
                    KIntArray* result = (KIntArray*) mem;
                    *result = (KIntArray) {
                        (const KInt*) elements,
                        length * sizeof(KInt),
                        length,
                        K_FLAG_ON_STACK
                    };
                    return result;
                }
                
                void JNI_release_enum_array_on_stack(KIntArray* self) {
                	if(self == NULL)
                		return;
                	self->__flags |= K_FLAG_DATA_OWNER;
                	${mangle("KIntArray_free")}(self);
                }
                
                jobjectArray JNI_to_kotlin_enum_array(
                    JNIEnv *env,
                    KIntArray* src,
                    jclass clazz,
                    jmethodID values_method
                ) {
                    if(src == NULL)
                        return NULL;
                    if(K_OBJECT_IS_ON_STACK(src->__flags))
                        return (jobjectArray)*((size_t*)((char*)src + sizeof(KArray)));
                    
                    jobjectArray result = (*env)->NewObjectArray(env, src->length, clazz, NULL);
                    const KInt* elements = src->elements;
                    for(int i = 0; i < src->length; i++)
                        (*env)->SetObjectArrayElement(env, result, i, JNI_to_kotlin_enum(env, elements[i], clazz, values_method));
                    return result;
                }
                
            """.trimIndent())
        }

        if(idl.callbacks.isNotEmpty()) {
            printLabel(builder, "Callback casts")
            builder.append("""
                
                const size_t JNI_ABSTRACT_CALLBACK_SIZE = sizeof(_AbstractCallback) + sizeof(size_t) * 2;
                
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
                	KBoolean result = (*env)->CallBooleanMethod(env, obj, object_equals, obj2);
                	if(status == JNI_EDETACHED) JVM_detach(status);
                	return result;
                }

                static inline KInt JNI_CALLBACK_hashCode(_AbstractCallback* ref) {
                	jobject obj = (jobject)((size_t*)((char*)ref + sizeof(_AbstractCallback)))[0];
                	JNIEnv *env = (JNIEnv*)((size_t*)((char*)ref + sizeof(_AbstractCallback)))[1];
                	
                	jint status = env == NULL ? JVM_attach(&env) : JNI_OK;
                	KInt result = (*env)->CallIntMethod(env, obj, object_hash_code);
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
                    free((void*) callback);
                    
                    JVM_detach(status);
                }
                
                static _AbstractCallback* JNI_CALLBACK_clone(_AbstractCallback* ref) {
                    if(ref == NULL)
                        return NULL;
                    jobject obj = (jobject)((size_t*)((char*)ref + sizeof(_AbstractCallback)))[0];
                	JNIEnv *env = (JNIEnv*)((size_t*)((char*)ref + sizeof(_AbstractCallback)))[1];
                    jint status = env == NULL ? JVM_attach(&env) : JNI_OK;
                    
                    _AbstractCallback* callback = (_AbstractCallback*) malloc(JNI_ABSTRACT_CALLBACK_SIZE);
                    memcpy(callback, ref, sizeof(_AbstractCallback));
                    callback->__flags = K_FLAG_RELEASABLE;
                    
                    ((size_t*)((char*)callback + sizeof(_AbstractCallback)))[0] = (size_t)(*env)->NewGlobalRef(env, obj);
                    ((size_t*)((char*)callback + sizeof(_AbstractCallback)))[1] = (size_t) NULL;
                    
                    if(status == JNI_EDETACHED) JVM_detach(status);
                    return callback;
                }
                
                jobject JNI_to_kotlin_callback(JNIEnv *env, _AbstractCallback* callback) {
                    if(callback == NULL)
                        return NULL;
                    jobject ref = (jobject)((size_t*)((char*)callback + sizeof(_AbstractCallback)))[0];
                    return (*env)->NewLocalRef(env, ref);
                }
                
                _AbstractCallback* JNI_to_native_callback_on_stack(JNIEnv *env, jobject obj, void (*invoke)(), void* mem) {
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
                
                _AbstractCallback* JNI_to_native_callback(JNIEnv *env, jobject obj, void (*invoke)(), char flags) {
                    if(obj == NULL)
                        return NULL;
                    void* mem = malloc(JNI_ABSTRACT_CALLBACK_SIZE);
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

        if(idl.interfaces.isNotEmpty()) {
            printLabel(builder, "Interfaces")
            printInterfaces(builder)
        }

        if(idl.callbacks.isNotEmpty()) {
            printLabel(builder, "Callback invokes")
            builder.append("\n")
            idl.callbacks.values
                .map { "callback_${it.name.lowercase()}_invoke" }
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

    fun mangle(name: String) =
        com.huskerdev.nativekt.utils.mangle(classPath, moduleName, name)

    private fun printStructs(builder: StringBuilder) = builder.apply {
        append("\n")
        idl.dictionaries.values
            .map { "class_${it.name.lowercase()}" }
            .chunked(3)
            .joinTo(builder, prefix = "jclass ", separator = ",\n\t", postfix = ";\n") { it.joinToString() }

        idl.dictionaries.values
            .map { "struct_${it.name.lowercase()}_companion" }
            .chunked(3)
            .joinTo(builder, prefix = "jobject ", separator = ",\n\t", postfix = ";\n") { it.joinToString() }

        idl.dictionaries.values
            .map { "struct_${it.name.lowercase()}_constructor" }
            .chunked(3)
            .joinTo(builder, prefix = "jmethodID ", separator = ",\n\t", postfix = ";\n") { it.joinToString() }

        idl.dictionaries.values
            .flatMap { dict ->
                dict.allFields().map {
                    "struct_${dict.name.lowercase()}_field_${it.name.snakeCase()}"
                }
            }
            .chunked(3)
            .joinTo(builder, prefix = "jmethodID ", separator = ",\n\t", postfix = ";\n") { it.joinToString() }

        idl.dictionaries.values.forEach { struct ->
            val name = struct.name.upperCamelCase()
            append("\n// $name\n")

            // to JVM
            append("\njobject JNI_to_kotlin_${struct.name.lowercase()}(JNIEnv *env, $name* src) {\n\t")
            append("if(src == NULL) return NULL;\n\t")
            append("return (*env)->CallObjectMethod(env, struct_${struct.name.lowercase()}_companion, struct_${struct.name.lowercase()}_constructor, \n\t\t")

            struct.allFields().joinTo(builder, separator = ",\n\t\t") {
                castJniToKotlin(it.type, "src->${it.name.snakeCase()}")
            }
            append("\n\t);\n}\n")

            // to Native
            append("\n$name* JNI_to_native_${struct.name.lowercase()}(JNIEnv *env, jobject src, char flags) {\n\t")
            append("if(src == NULL) return NULL;\n\t")
            append("$name* result = malloc(sizeof($name));\n\t")
            append("*result = ($name) {\n\t\t")

            buildList {
                struct.allFields().mapTo(this) { field ->
                    val fieldVariable = "struct_${struct.name.lowercase()}_field_${field.name.snakeCase()}"
                    val getter = field.type.toMethodCall()
                    castKotlinToJNI(field.type, "(*env)->$getter(env, src, $fieldVariable)", onStack = false, flags = "flags")
                }
                add("flags")
            }.joinTo(builder, separator = ",\n\t\t")
            append("\n\t};")
            append("\n\treturn result;\n}\n")
        }
    }

    private fun printInterfaces(builder: StringBuilder) = builder.apply {
        append("\njmethodID interface_ptr;\n")
        idl.interfaces.values
            .map { "class_${it.name.lowercase()}" }
            .chunked(3)
            .joinTo(builder, prefix = "jclass ", separator = ",\n\t", postfix = ";\n") { it.joinToString() }

        idl.interfaces.values
            .map { "interface_${it.name.lowercase()}_constructor" }
            .chunked(3)
            .joinTo(builder, prefix = "jmethodID ", separator = ",\n\t", postfix = ";\n") { it.joinToString() }

        idl.interfaces.values.forEach { inter ->
            val name = inter.name.upperCamelCase()
            append("\n// $name\n")

            // to JVM
            append("""
                
                jobject JNI_to_kotlin_${inter.name.lowercase()}(JNIEnv *env, void* src) {
                    if(src == NULL) return NULL;
                    return (*env)->CallStaticObjectMethod(env, class_${inter.name.lowercase()}, interface_${inter.name.lowercase()}_constructor, (jlong) src);
                }
                
            """.trimIndent())
        }
        append("""
            
            void* JNI_to_native__interface(JNIEnv *env, jobject src) {
                if(src == NULL) return NULL;
                return (void*) (*env)->CallLongMethod(env, src, interface_ptr);
            }
            
        """.trimIndent())
    }

    private fun printCallbackInvokeDef(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val args = listOf("${callback.name}* _callback") +
                callback.args.map { "${it.type.toCType()} ${it.name.snakeCase()}" }

        append("${callback.type.toCType()} JNI_CALLBACK_INVOKE_${callback.name.lowercase()}(${args.joinToString()});\n")
    }

    private fun printCallbackInvoke(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val args = buildList {
            add("${callback.name}* callback")
            callback.args.mapTo(this) {
                "${it.type.toCType()} __${it.name.snakeCase()}"
            }
        }.joinToString()

        val jvmArgs = buildList {
            add("callback_${callback.name.lowercase()}_invoke")
            callback.args.mapTo(this) {
                castJniToKotlin(it.type, "__${it.name.snakeCase()}")
            }
        }.joinToString()

        append("""
            
            ${callback.type.toCType()} JNI_CALLBACK_INVOKE_${callback.name.lowercase()}(${args}) {
                jobject obj = (jobject)((size_t*)((char*)callback + sizeof(_AbstractCallback)))[0];
                JNIEnv *env = (JNIEnv*)((size_t*)((char*)callback + sizeof(_AbstractCallback)))[1];
                jint status = env == NULL ? JVM_attach(&env) : JNI_OK;
                
        """.trimIndent())

        val call = "(*env)->${callback.type.toMethodCall()}(env, obj, $jvmArgs)"

        if(callback.type !is ResolvedIdlType.Void) {
            append(callback.type.toCType())
            append(" result = ")
            append(castKotlinToJNI(callback.type, call, onStack = false, flags = "K_FLAG_RELEASABLE"))
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
            
            static jmethodID JNI_get_unmangled_method_id(JNIEnv *env, jclass clazz, const char *name, const char *sig) {
                jsize target_name_length = strlen(name);
                
                jclass class_class = (*env)->FindClass(env, "java/lang/Class");
                jmethodID get_declared_methods = (*env)->GetMethodID(
                    env, class_class,
                    "getDeclaredMethods",
                    "()[Ljava/lang/reflect/Method;"
                );
            
                jobjectArray methods = (*env)->CallObjectMethod(env, clazz, get_declared_methods);
                jsize length = (*env)->GetArrayLength(env, methods);
            
                jclass class_method = (*env)->FindClass(env, "java/lang/reflect/Method");
                jmethodID get_name = (*env)->GetMethodID(env, class_method, "getName", "()Ljava/lang/String;");
            
                for (jsize i = 0; i < length; i++) {
                    jobject method = (*env)->GetObjectArrayElement(env, methods, i);
                    jstring method_name = (*env)->CallObjectMethod(env, method, get_name);
            
                    jsize name_length = (*env)->GetStringLength(env, method_name);
                    if (name_length >= target_name_length) {
                        char* str = malloc(name_length);
                        (*env)->GetStringUTFRegion(env, method_name, 0, name_length, str);
            
                        if (strncmp(name, str, target_name_length) == 0 &&
                            (name_length == target_name_length || (name_length > target_name_length && str[target_name_length] == '-'))
                        ) {
                            jmethodID result = (*env)->FromReflectedMethod(env, method);
                            (*env)->DeleteLocalRef(env, method);
                            (*env)->DeleteLocalRef(env, method_name);
                            free(str);
                            return result;
                        }
                        free(str);
                    }
            
                    (*env)->DeleteLocalRef(env, method);
                    (*env)->DeleteLocalRef(env, method_name);
                }
                return (*env)->GetMethodID(env, clazz, name, sig);
            }
            
            static void JNI_Init(JNIEnv *env, JNINativeMethod *methods, jint count) {
                (*env)->GetJavaVM(env, &jvm);
                
                jni_class = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "${(classPath.split(".") + name).joinToString(separator = "/")}"));
                (*env)->RegisterNatives(env, jni_class, methods, count);
                
                // Object
                jclass object_class = (*env)->FindClass(env, "java/lang/Object");
                object_equals = (*env)->GetMethodID(env, object_class, "equals", "(Ljava/lang/Object;)Z");
                object_hash_code = (*env)->GetMethodID(env, object_class, "hashCode", "()I");
                
                // String
                class_string = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "java/lang/String"));
                string_constructor = (*env)->GetMethodID(env, class_string, "<init>", "([BLjava/lang/String;)V");
                string_get_bytes = (*env)->GetMethodID(env, class_string, "getBytes", "(Ljava/lang/String;)[B");
                string_utf8_const = (*env)->NewGlobalRef(env, (*env)->NewStringUTF(env, "UTF-8"));
                
        """.replaceIndent())

        if(idl.enums.isNotEmpty()) {
            append("\n\t")
            append("// Enums")
            append("\n\tenum_ordinal = (*env)->GetMethodID(env, (*env)->FindClass(env, \"java/lang/Enum\"), \"ordinal\", \"()I\");\n")
            idl.enums.values.forEach { enum ->
                val classPath = "${classPath.replace(".", "/")}/${enum.name}"
                val classFieldName = "class_${enum.name.lowercase()}"
                val valuesFieldName = "values_${enum.name.lowercase()}"

                append("\n\t$classFieldName = (*env)->NewGlobalRef(env, (*env)->FindClass(env, \"$classPath\"));")
                append("\n\t$valuesFieldName = (*env)->GetStaticMethodID(env, $classFieldName, \"values\", \"()[L$classPath;\");\n")
            }
        }

        if(idl.dictionaries.isNotEmpty()) {
            append("\n\t// Struct")
            idl.dictionaries.values.forEach { struct ->
                val structClassPath = "${classPath.replace(".", "/")}/${struct.name}"
                val classFieldName = "class_${struct.name.lowercase()}"
                val companionFieldName = "struct_${struct.name.lowercase()}_companion"
                val constructorFieldName = "struct_${struct.name.lowercase()}_constructor"

                val argsDesc = struct.allFields().joinToString(separator = "") {
                    it.type.toJavaDesc(classPath)
                }

                append($$"""
                    
                    $$classFieldName = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "$$structClassPath"));
                    $$companionFieldName = (*env)->NewGlobalRef(env, (*env)->GetStaticObjectField(env, $$classFieldName, (*env)->GetStaticFieldID(env, $$classFieldName, "Companion", "L$$structClassPath$Companion;")));
                    $$constructorFieldName = (*env)->GetMethodID(env, (*env)->FindClass(env, "$$structClassPath$Companion"), "of", "($$argsDesc)L$$structClassPath;");
                
                """.replaceIndent("\t"))

                struct.allFields().joinTo(builder, separator = "\n\t") { field ->
                    val fieldVariableName = "struct_${struct.name.lowercase()}_field_${field.name.snakeCase()}"

                    val getMethodId = if(field.type.isUnsigned())
                        "JNI_get_unmangled_method_id"
                    else "(*env)->GetMethodID"

                    "$fieldVariableName = $getMethodId(env, $classFieldName, \"get${field.name.capitalized()}\", \"()${field.type.toJavaDesc(classPath)}\");"
                }
                append("\n")
            }
        }

        if(idl.interfaces.isNotEmpty()) {
            append("\n\t// Interfaces")
            append("\n\tinterface_ptr = (*env)->GetMethodID(env, (*env)->FindClass(env, \"com/huskerdev/nativekt/jvm/NativeKtResourceJvm\"), \"get_ptr\", \"()J\");")
            idl.interfaces.values.forEach { inter ->
                val interfaceClassPath = "${classPath.replace(".", "/")}/${inter.name}"
                val classFieldName = "class_${inter.name.lowercase()}"
                val constructorFieldName = "interface_${inter.name.lowercase()}_constructor"

                append($$"""
                    
                    $$classFieldName = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "$$interfaceClassPath"));
                    $$constructorFieldName = (*env)->GetStaticMethodID(env, $$classFieldName, "_wrap", "(J)L$$interfaceClassPath;");
                    
                """.replaceIndent("\t"))
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

                val getMethodId = if(it.type.isUnsigned() || it.args.any { arg -> arg.type.isUnsigned() })
                    "JNI_get_unmangled_method_id"
                else "(*env)->GetMethodID"

                append("callback_${it.name.lowercase()}_invoke = $getMethodId(env, (*env)->FindClass(env, \"$path\"), \"invoke\", \"($args)$ret\");\n\t")
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
            WebIDLBuiltinKind.CHAR -> "CallCharMethod"
            WebIDLBuiltinKind.BYTE, WebIDLBuiltinKind.UNSIGNED_BYTE -> "CallByteMethod"
            WebIDLBuiltinKind.SHORT, WebIDLBuiltinKind.UNSIGNED_SHORT -> "CallShortMethod"
            WebIDLBuiltinKind.INT, WebIDLBuiltinKind.UNSIGNED_INT -> "CallIntMethod"
            WebIDLBuiltinKind.LONG, WebIDLBuiltinKind.UNSIGNED_LONG -> "CallLongMethod"
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