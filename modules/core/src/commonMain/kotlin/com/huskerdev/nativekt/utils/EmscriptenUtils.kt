package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.JsTarget
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.stdlibx.getenv
import com.huskerdev.stdlibx.fs.SyncPath

internal fun locateEmcc(context: NativeModuleContext): SyncPath {
    return locate(context, "emcc")
        ?: run {
            if(getenv("EMSDK") != null)
                return@run SyncPath(getenv("EMSDK")!!).resolve("upstream/emscripten/emcc")
            throw UnsupportedOperationException("Could not locate 'emcc'")
        }
}

internal fun getEmccArgs(context: NativeModuleContext): String = listOfNotNull(
    "--no-entry",
    "ALLOW_MEMORY_GROWTH=1",
    "ALLOW_TABLE_GROWTH=1",
    "MODULARIZE=1",
    "EXPORT_ES6=1",
    "WASM_BIGINT=${if (context.module.js.useJsBigInt) "1" else "0"}",
    "ENVIRONMENT=${if (context.module.js.target == JsTarget.WEB) "web" else "node"}",
    "EXPORTED_RUNTIME_METHODS=addFunction,wasmMemory",
).joinToString(separator = " ") { "-s $it" }
