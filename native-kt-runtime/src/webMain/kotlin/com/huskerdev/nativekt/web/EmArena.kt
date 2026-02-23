@file:OptIn(ExperimentalWasmJsInterop::class)

package com.huskerdev.nativekt.web

class EmArena(
    val module: dynamic
): AutoCloseable {
    companion object {
        fun <T> use(module: dynamic, block: (EmArena) -> T) =
            EmArena(module).use { block(it) }
    }

    private val allocated = hashSetOf<Any>()
    private val callbacks = hashSetOf<JsNumber>()

    fun malloc(size: Int): Any =
        (module._malloc(size) as Any).also { allocated += this }

    fun allocCStr(str: String): Any {
        val len = module.lengthBytesUTF8(str) + 1
        val mem = malloc(len)
        module.stringToUTF8(str, mem, len)
        return mem
    }

    fun unwrapCStr(ptr: Any, dealloc: Boolean): String {
        val result = module.UTF8ToString(ptr)
        if(dealloc && ptr !in allocated)
            module._free(ptr)
        return result
    }

    fun <T> unwrapCallback(ptr: JsNumber, dealloc: Boolean): T {
        val result = unwrapCallback<T>(module, ptr, dealloc)
        if(dealloc && ptr !in allocated)
            module._free(ptr)
        return result
    }

    fun callback(callback: JsNumber): JsNumber {
        callbacks += callback
        return callback
    }

    override fun close() = allocated.forEach {
        module._free(it)
        callbacks.forEach { callback ->
            freeCallback(module, callback)
        }
    }
}