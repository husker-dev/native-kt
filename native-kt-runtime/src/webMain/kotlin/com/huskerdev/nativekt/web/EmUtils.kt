@file:OptIn(ExperimentalWasmJsInterop::class)

package com.huskerdev.nativekt.web

private val callbacks = hashMapOf<Pair<dynamic, Int>, Any>()

fun unwrapCStr(module: dynamic, ptr: dynamic, dealloc: Boolean): String {
    val result = module.UTF8ToString(ptr)
    if(dealloc)
        module._free(ptr)
    return result
}

fun allocCStr(module: dynamic, str: String): Any {
    val len = module.lengthBytesUTF8(str) + 1
    val mem = module._malloc(len)
    module.stringToUTF8(str, mem, len)
    return mem
}

fun unwrapLong(module: dynamic, value: dynamic): Long {
    val ptr = (value as JsNumber).toInt() shr 2

    val low = (module.HEAP32[ptr] as JsNumber).toLong()
    val high = (module.HEAP32[ptr + 1] as JsNumber).toLong()
    module._free(ptr)

    return high shl 32 or (low and 0xffffffff)
}

fun wrapLong(module: dynamic, value: Long): Int {
    val ptr = (module._malloc(8) as JsNumber).toInt()
    module.HEAP32[ptr shr 2] = (value and 0xffffffff).toInt()
    module.HEAP32[(ptr shr 2) + 1] = (value shr 32).toInt()
    return ptr
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