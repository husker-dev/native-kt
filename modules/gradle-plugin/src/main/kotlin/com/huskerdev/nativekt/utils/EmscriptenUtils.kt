package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.plugin.JsTarget
import com.huskerdev.nativekt.plugin.NativeKtJsInterface
import com.huskerdev.osutils.OS
import org.gradle.process.ExecOperations
import java.io.File

internal fun locateEmcc(execOps: ExecOperations, context: NativeModuleContext): File {
    return locate(execOps, context, "emcc")
        ?: run {
            if("EMSDK" in System.getenv())
                return@run File(System.getenv()["EMSDK"], "upstream/emscripten/emcc")
            if (OS.current == OS.WINDOWS) {
                File.listRoots()!!.forEach {
                    if (File(it, "emsdk/upstream/emscripten/emcc.bat").exists())
                        return@run File(it, "emsdk/upstream/emscripten/emcc")
                }
            }
            throw UnsupportedOperationException("Could not locate 'emcc'")
        }
}

internal fun getEmccArgs(context: NativeModuleContext): String {
    val extension = context.extension as NativeKtJsInterface

    return listOfNotNull(
        "--no-entry",
        "ALLOW_MEMORY_GROWTH=1",
        "ALLOW_TABLE_GROWTH=1",
        "MODULARIZE=1",
        "EXPORT_ES6=1",
        "WASM_BIGINT=${if (extension.useJsBigInt) "1" else "0"}",
        "ENVIRONMENT=${if (context.module.jsTarget == JsTarget.WEB) "web" else "node"}",
        "EXPORTED_RUNTIME_METHODS=addFunction,wasmMemory",
    ).joinToString(separator = " ") { "-s $it" }
}