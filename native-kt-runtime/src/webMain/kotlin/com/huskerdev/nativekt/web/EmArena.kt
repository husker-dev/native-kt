@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("unused")

package com.huskerdev.nativekt.web

import kotlin.js.json

class EmArena(
    val module: dynamic
): AutoCloseable {
    companion object {
        fun <T> use(module: dynamic, block: (EmArena) -> T) =
            EmArena(module).use { block(it) }
    }

    private val allocated = hashSetOf<Any>()
    private val callbacks = hashSetOf<JsNumber>()

    private fun ptr(ptr: dynamic): dynamic {
        allocated += ptr as Any
        return ptr
    }

    fun malloc(size: Int): Any =
        ptr(module._malloc(size)) as Any

    fun allocCStr(str: String): Any {
        val len = module.lengthBytesUTF8(str) + 1
        val strMem = malloc(len)
        module.stringToUTF8(str, strMem, len)

        return json(
            "data" to strMem,
            "length" to str.length
        )
    }

    fun unwrapCStr(ptr: dynamic, dealloc: Boolean): String {
        val data = (ptr.data as JsNumber).toInt()
        val length = (ptr.length as JsNumber).toInt()

        val result = module.UTF8ToString(data, length)
        if(dealloc && data !in allocated)
            module._free(data)

        return result
    }

    // Primitive Arrays

    // Array: char

    fun toNativeCharArray( arr: CharArray) =
        ptr(toNativeCharArray(module, arr))

    fun toKotlinCharArray(struct: dynamic, dealloc: Boolean) =
        toKotlinCharArray(module, struct, dealloc && struct.elements !in allocated)

    // Array: boolean

    fun toNativeBooleanArray(arr: BooleanArray) =
        ptr(toNativeBooleanArray(module, arr))

    fun toKotlinBooleanArray(struct: dynamic, dealloc: Boolean) =
        toKotlinBooleanArray(module, struct, dealloc && struct.elements !in allocated)

    // Array: byte

    fun toNativeByteArray(arr: ByteArray) =
        ptr(toNativeByteArray(module, arr))

    fun toKotlinByteArray(struct: dynamic, dealloc: Boolean) =
        toKotlinByteArray(module, struct, dealloc && struct.elements !in allocated)

    // Array: short

    fun toNativeShortArray(arr: ShortArray): dynamic =
        ptr(toNativeShortArray(module, arr))

    fun toKotlinShortArray(struct: dynamic, dealloc: Boolean): ShortArray =
        toKotlinShortArray(module, struct, dealloc && struct.elements !in allocated)

    // Array: int

    fun toNativeIntArray(arr: IntArray) =
        ptr(toNativeIntArray(module, arr))

    fun toKotlinIntArray(struct: dynamic, dealloc: Boolean) =
        toKotlinIntArray(module, struct, dealloc && struct.elements !in allocated)

    // Array: long

    fun toNativeLongArray(arr: LongArray): dynamic =
        ptr(toNativeLongArray(module, arr))

    fun toKotlinLongArray(struct: dynamic, dealloc: Boolean): LongArray =
        toKotlinLongArray(module, struct, dealloc && struct.elements !in allocated)

    // Array: float

    fun toNativeFloatArray(arr: FloatArray): dynamic =
        ptr(toNativeFloatArray(module, arr))

    fun toKotlinFloatArray(struct: dynamic, dealloc: Boolean): FloatArray =
        toKotlinFloatArray(module, struct, dealloc && struct.elements !in allocated)

    // Array: double

    fun toNativeDoubleArray(arr: DoubleArray): dynamic =
        ptr(toNativeDoubleArray(module, arr))

    fun toKotlinDoubleArray(struct: dynamic, dealloc: Boolean): DoubleArray =
        toKotlinDoubleArray(module, struct, dealloc && struct.elements !in allocated)


    // Callbacks

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