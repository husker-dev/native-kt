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
            
            
            JNIEXPORT jlong JNICALL Java_${fullPath}_getFunctionAddress(JNIEnv *env, jclass cls, jstring _libName, jstring _funcName) {
                const char* funcName = (*env)->GetStringUTFChars(env, _funcName, NULL);
            #ifdef _WIN32
                const char* libName = (*env)->GetStringUTFChars(env, _libName, NULL);
                jlong result = (jlong) GetProcAddress(GetModuleHandle(libName), funcName);
                (*env)->ReleaseStringUTFChars(env, _libName, libName);
            #else
                jlong result = (jlong) dlsym(RTLD_DEFAULT, funcName);
            #endif
                (*env)->ReleaseStringUTFChars(env, _funcName, funcName);
                return result;
            }
        """.trimIndent())
    }
}