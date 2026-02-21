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
            
            // String
            
            void ArenaNode__freeString(Arena* arena, ArenaNode* node){
                JNIEnv *env = arena->env;
                (*env)->ReleaseStringUTFChars(env, node->obj, (const char*)node->ptr);
            }
            
            const char* Arena__unwrapString(Arena* arena, jobject str) {
                JNIEnv *env = arena->env;
                return (const char*) Arena__push(arena,
                    str,
                    (void*)(*env)->GetStringUTFChars(env, str, NULL),
                    ArenaNode__freeString
                );
            }
            
            jobject Arena__wrapString(Arena* arena, const char* ptr, bool dealloc) {
                JNIEnv *env = arena->env;
                jobject result = (*env)->NewStringUTF(env, ptr);
            
                if(dealloc && !Arena__contains(arena, (void*)ptr))
                    free((void*)ptr);
                return result;
            }
            
            // String critical
            
            void ArenaNode__freeStringCritical(Arena* arena, ArenaNode* node){
                JNIEnv *env = arena->env;
                (*env)->ReleaseStringCritical(env, node->obj, (const jchar*)node->ptr);
            }
            
            const char* Arena__unwrapStringCritical(Arena* arena, jobject str) {
                JNIEnv *env = arena->env;
                return (const char*) Arena__push(arena,
                    str,
                    (void*)(*env)->GetStringCritical(env, str, 0),
                    ArenaNode__freeStringCritical
                );
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