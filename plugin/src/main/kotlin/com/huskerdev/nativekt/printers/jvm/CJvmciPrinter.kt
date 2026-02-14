package com.huskerdev.nativekt.printers.jvm

import java.io.File

class CJvmciPrinter(
    target: File,
    val classPath: String,
    val name: String
) {
    init {
        val fullPath = (classPath.split(".") + name).joinToString("_")

        target.writeText("""
            #include <jni.h>
            
            
            #ifdef _WIN32
                #include <windows.h>
            #else
                #include <dlfcn.h>
            #endif

            JNIEXPORT jlong JNICALL Java_${fullPath}_getLibraryHandle(JNIEnv *env, jclass cls, jstring _name) {
                const char* name = (*env)->GetStringUTFChars(env, _name, NULL);
            #ifdef _WIN32
                jlong result = (jlong) LoadLibraryA(name);
            #else
                jlong result = (jlong) dlopen(name, RTLD_LAZY);
            #endif
                (*env)->ReleaseStringUTFChars(env, _name, name);
                return result;
            }

            JNIEXPORT jlong JNICALL Java_${fullPath}_getFunctionAddress(JNIEnv *env, jclass cls, jlong libHandle, jstring _funcName) {
                const char* funcName = (*env)->GetStringUTFChars(env, _funcName, NULL);
            #ifdef _WIN32
                jlong result = (jlong) GetProcAddress((HMODULE) libHandle, funcName);
            #else
                jlong result = (jlong) dlsym((void*) libHandle, funcName);
            #endif
                (*env)->ReleaseStringUTFChars(env, _funcName, funcName);
                return result;
            }

            JNIEXPORT void JNICALL Java_${fullPath}_freeLibHandle(JNIEnv *env, jclass cls, jlong libHandle) {
            #ifdef _WIN32
                FreeLibrary((HMODULE) libHandle);
            #else
                dlclose((void*) libHandle);
            #endif
            }
        """.trimIndent())
    }
}