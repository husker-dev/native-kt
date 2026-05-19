package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.utils.printLabel
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
            
        """.trimIndent())

        printLabel(builder, "Primitive arrays")
        builder.append("""
             
            #define KArrayCast(Name, JType)		                                                    \
            static void ArenaNode__free##Name##Array(Arena* arena, ArenaNode* node){                \
                JNIEnv *env = arena->env;                                                           \
                (*env)->Release##Name##ArrayElements(env, node->obj, (JType*)node->ptr, 0);         \
            }                                                                                       \
                                                                                                    \
            K##Name##Array Arena__toNative##Name##Array(Arena* arena, JType##Array arr) {           \
                JNIEnv *env = arena->env;                                                           \
                jsize size = (*env)->GetArrayLength(env, arr);                                      \
                                                                                                    \
                K##Name* elements = (K##Name*) Arena__push(arena,                                   \
                    arr,                                                                            \
                    (void*)(*env)->Get##Name##ArrayElements(env, arr, NULL),                        \
                    ArenaNode__free##Name##Array                                                    \
                );                                                                                  \
                return (K##Name##Array) { elements, size, false, false };                           \
            }                                                                                       \
                                                                                                    \
            JType##Array Arena__toKotlin##Name##Array(Arena* arena, K##Name##Array arr, bool dealloc) {\
                return JNI_toKotlin##Name##Array(                                                      \
                    arena->env, arr,                                                                \
                    dealloc && !arr.releasable                                                      \
                );                                                                                  \
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

        printLabel(builder, "String")
        builder.append("""

            void ArenaNode__freeString(Arena* arena, ArenaNode* node){
                JNIEnv *env = arena->env;
                (*env)->ReleaseStringUTFChars(env, node->obj, (const char*)node->ptr);
            }
            
            KString Arena__toNativeString(Arena* arena, jstring str) {
                JNIEnv *env = arena->env;
                jsize length = (*env)->GetStringLength(env, str);
                const char* data = (const char*) Arena__push(arena,
                    str,
                    (void*)(*env)->GetStringUTFChars(env, str, NULL),
                    ArenaNode__freeString
                );
                return (KString) { data, length, false, false };
            }
            
            jstring Arena__toKotlinString(Arena* arena, KString str, bool dealloc) {
                return JNI_toKotlinString(arena->env, str, dealloc && !str.releasable);
            }
            
        """.trimIndent())

        printLabel(builder, "new/free")
        builder.append("""
             
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

        if(callbacks) {
            printLabel(builder, "Callback")
            builder.append("""
                
                void ArenaNode__freeCallback(Arena* arena, ArenaNode* node){
                    JNI_freeCallback((JNI_Callback*)node->ptr);
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
        }

        target.writeText(builder.toString())
    }
}