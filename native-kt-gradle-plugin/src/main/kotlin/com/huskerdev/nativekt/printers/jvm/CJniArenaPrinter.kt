package com.huskerdev.nativekt.printers.jvm

import java.io.File

class CJniArenaPrinter(
    target: File,
    callbacks: Boolean
) {
    init {
        val builder = StringBuilder()
        builder.append("""
            #include "jni_utils.h"
            
            typedef struct Arena Arena;
            typedef struct ArenaNode ArenaNode;
            
            struct ArenaNode {
                jobject obj;
                void* ptr;
            
                void (*free)(Arena*, ArenaNode*);
            };
            
            struct Arena {
                JNIEnv* env;
                ArenaNode nodes[12];
                uint32_t count;
            };
            
            
            void* Arena__push(
                Arena* arena,
                jobject obj,
                void* ptr,
                void (*free)(Arena*, ArenaNode*)
            ) {
                ArenaNode* node = &arena->nodes[arena->count++];
                node->obj = obj;
                node->ptr = ptr;
                node->free = free;
                return ptr;
            }
            
            bool Arena__contains(Arena* arena, void* ptr) {
                for(int i = 0; i < arena->count; i++) {
                    ArenaNode node = arena->nodes[i];
                    if(node.ptr == ptr)
                        return true;
                }
                return false;
            }
            
            // Primite arrays
            
            #define KArrayCast(Name, JType)		                                                    \
            static void ArenaNode__free##Name##Array(Arena* arena, ArenaNode* node){                \
                JNIEnv *env = arena->env;                                                           \
                (*env)->Release##Name##ArrayElements(env, node->obj, (JType*)node->ptr, 0);         \
            }                                                                                       \
                                                                                                    \
            K##Name##Array Arena__unwrap##Name##Array(Arena* arena, JType##Array arr) {             \
                JNIEnv *env = arena->env;                                                           \
                jsize size = (*env)->GetArrayLength(env, arr);                                      \
                                                                                                    \
                K##Name* elements = (K##Name*) Arena__push(arena,                                   \
                    arr,                                                                            \
                    (void*)(*env)->Get##Name##ArrayElements(env, arr, NULL),                        \
                    ArenaNode__free##Name##Array                                                    \
                );                                                                                  \
                return (K##Name##Array) { elements, size };                                         \
            }                                                                                       \
                                                                                                    \
            JType##Array Arena__wrap##Name##Array(Arena* arena, K##Name##Array arr, bool dealloc) { \
                JNIEnv *env = arena->env;                                                           \
                JType##Array result = (*env)->New##Name##Array(env, arr.size);                      \
                (*env)->Set##Name##ArrayRegion(env, result, 0, arr.size, (JType*)arr.elements);     \
                                                                                                    \
                if(dealloc && !Arena__contains(arena, (void*)arr.elements))                         \
                    free((void*)arr.elements);                                                      \
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
            
            // Arrays
            
            static void ArenaNode__freeEnumArray(Arena* arena, ArenaNode* node){
                JNIEnv *env = arena->env;
                (*env)->ReleaseIntArrayElements(env, node->obj, (jint*)node->ptr, 0);
            }

            KArray Arena__unwrapEnumArray(Arena* arena, jintArray arr) {
                JNIEnv *env = arena->env;
                jsize size = (*env)->GetArrayLength(env, arr);

                void* elements = Arena__push(arena,
                    arr,
                    (void*)(*env)->GetIntArrayElements(env, arr, NULL),
                    ArenaNode__freeEnumArray
                );
                return (KArray) { elements, size };
            }

            jintArray Arena__wrapEnumArray(Arena* arena, KArray arr, bool dealloc) {
                JNIEnv *env = arena->env;
                jintArray result = (*env)->NewIntArray(env, arr.size);
                (*env)->SetIntArrayRegion(env, result, 0, arr.size, (jint*)arr.elements);

                if(dealloc && !Arena__contains(arena, (void*)arr.elements))
                    free((void*)arr.elements);
                return result;
            }
            
            // String

            void ArenaNode__freeString(Arena* arena, ArenaNode* node){
                JNIEnv *env = arena->env;
                (*env)->ReleaseStringUTFChars(env, node->obj, (const char*)node->ptr);
            }
            
            KString Arena__unwrapString(Arena* arena, jstring str) {
                JNIEnv *env = arena->env;
                jsize length = (*env)->GetStringLength(env, str);
                const char* data = (const char*) Arena__push(arena,
                    str,
                    (void*)(*env)->GetStringUTFChars(env, str, NULL),
                    ArenaNode__freeString
                );
                return (KString) { data, length };
            }
            
            jstring Arena__wrapString(Arena* arena, KString str, bool dealloc) {
                JNIEnv *env = arena->env;
                jstring result = JNI_createJString(env, str);
                
                if(dealloc && !Arena__contains(arena, (void*)str.data))
                    free((void*)str.data);
                return result;
            }
            
            // String critical
            
            void ArenaNode__freeStringCritical(Arena* arena, ArenaNode* node){
                JNIEnv *env = arena->env;
                (*env)->ReleaseStringCritical(env, node->obj, (const jchar*)node->ptr);
            }
            
            KString Arena__unwrapStringCritical(Arena* arena, jstring str) {
                JNIEnv *env = arena->env;
                jsize length = (*env)->GetStringLength(env, str);
                const char* data = (const char*) Arena__push(arena,
                    str,
                    (void*)(*env)->GetStringCritical(env, str, 0),
                    ArenaNode__freeStringCritical
                );
                return (KString) { data, length };
            }
            
            // new/free
            
            void Arena__free(Arena* arena) {
                for(int i = 0; i < arena->count; i++) {
                    ArenaNode node = arena->nodes[i];
                    node.free(arena, &node);
                }
            }
            
            void Arena__init(Arena* arena, JNIEnv *env) {
                arena->env = env;
                arena->count = 0;
            }
        """.trimIndent())

        if(callbacks) builder.append("""
            
            
            // Callback

            void ArenaNode__freeCallback(Arena* arena, ArenaNode* node){
                JNI_CALLBACK_free((JNI_Callback*)node->ptr);
            }

            JNI_Callback* Arena__callback(Arena* arena, JNI_Callback* callback) {
                Arena__push(arena,
                    NULL,
                    (void*)callback,
                    ArenaNode__freeCallback
                );
                return callback;
            }
            
        """.trimIndent())

        target.writeText(builder.toString())
    }
}