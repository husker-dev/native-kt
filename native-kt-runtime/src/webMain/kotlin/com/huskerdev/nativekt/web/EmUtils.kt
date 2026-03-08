@file:OptIn(ExperimentalWasmJsInterop::class)

package com.huskerdev.nativekt.web

import kotlin.js.json

private val callbacks = hashMapOf<Pair<dynamic, Int>, Any>()

fun allocCStr(module: dynamic, str: String): Any {
    val len = module.lengthBytesUTF8(str) + 1
    val strMem = module._malloc(len)
    module.stringToUTF8(str, strMem, len)

    return json(
        "data" to strMem,
        "length" to str.length
    )
}

fun unwrapCStr(module: dynamic, ptr: dynamic, dealloc: Boolean): String {
    val data = (ptr.data as JsNumber).toInt()
    val length = (ptr.length as JsNumber).toInt()

    val result = module.UTF8ToString(data, length)
    if(dealloc)
        module._free(data)

    return result
}

fun mallocCallback(
    module: dynamic,
    callback: Any,
    invoke: dynamic,
    free: dynamic
): JsNumber {
    val rawPtr = module._malloc(12) as JsNumber
    val ptr = rawPtr.toInt()
    // 'm' is not used
    // module.HEAP32[ptr shr 2] = 0
    module.HEAP32[(ptr shr 2) + 1] = invoke
    module.HEAP32[(ptr shr 2) + 2] = free

    callbacks[Pair(module, ptr)] = callback
    return rawPtr
}

@Suppress("unchecked_cast")
fun <T> unwrapCallback(module: dynamic, ptr: JsNumber, dealloc: Boolean): T {
    val result = callbacks[Pair(module, ptr.toInt())]
    if(dealloc)
        freeCallback(module, ptr)
    return result as T
}

fun freeCallback(module: dynamic, ptr: JsNumber) {
    callbacks.remove(Pair(module, ptr.toInt()))
    module._free(ptr)
}

fun createCallbackFreeFunction(module: dynamic) =
     module.addFunction({ callback ->
        freeCallback(module, callback)
    }, "vp")