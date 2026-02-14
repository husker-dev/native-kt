package com.huskerdev.nativekt.printers.jvm

import java.io.File

class CArenaPrinter(
    target: File,
) {
    init {
        target.writeText("""
            #include <jni.h>
            #include <stdlib.h>
            
            typedef struct Arena Arena;
            typedef struct ArenaNode ArenaNode;
            
            struct ArenaNode {
                jobject obj;
                void* ptr;
                Arena* arena;
                ArenaNode* prev;
            
                void (*free)(ArenaNode*);
            };
            
            struct Arena {
                JNIEnv* env;
                ArenaNode* last;
            };
            
            
            void* Arena__push(
                Arena* arena,
                jobject obj,
                void* ptr,
                void (*free)(ArenaNode*)
            ) {
                ArenaNode* node = (ArenaNode*)malloc(sizeof(ArenaNode));
                node->obj = obj;
                node->ptr = ptr;
                node->arena = arena;
                node->prev = arena->last;
                node->free = free;
            
                arena->last = node;
                return ptr;
            }
            
            bool Arena__contains(Arena* arena, void* ptr) {
                ArenaNode* node = arena->last;
                while(node != NULL) {
                    if(node->ptr == ptr)
                        return true;
                    node = node->prev;
                }
                return false;
            }
            
            // String
            
            void ArenaNode__freeString(ArenaNode* node){
                JNIEnv *env = node->arena->env;
                (*env)->ReleaseStringUTFChars(env, node->obj, (const char*)node->ptr);
                free((void*)node);
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
            
            void ArenaNode__freeStringCritical(ArenaNode* node){
                JNIEnv *env = node->arena->env;
                (*env)->ReleaseStringCritical(env, node->obj, (const jchar*)node->ptr);
                free((void*)node);
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
                JNIEnv *env = arena->env;
                ArenaNode* node = arena->last;
            
                while(node != NULL) {
                    ArenaNode* prev = node->prev;
                    node->free(node);
                    node = prev;
                }
                free((void*)arena);
            }
            
            Arena* Arena__new(JNIEnv *env) {
                Arena* arena = (Arena*)malloc(sizeof(Arena));
                arena->last = NULL;
                arena->env = env;
                return arena;
            }
        """.trimIndent())
    }
}