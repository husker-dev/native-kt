@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("unused")

package com.huskerdev.nativekt.web

import kotlin.js.*

class EmArena(
    module: JsAny
): AutoCloseable {
    companion object {
        fun <T> use(module: JsAny, block: (EmArena) -> T) =
            EmArena(module).use { block(it) }
    }

    private val module = module.unsafeCast<EmModule>()
    private val allocated = hashSetOf<Int>()
    private val callbacks = hashSetOf<Int>()

    private fun ptr(ptr: Int): Int {
        allocated += ptr
        return ptr
    }

    fun malloc(size: Int): Int =
        ptr(module._malloc(size))

    fun allocCStr(str: String): EmString {
        val len = module.lengthBytesUTF8(str) + 1
        val strMem = malloc(len)
        module.stringToUTF8(str, strMem, len)

        return createJsObject {
            data = strMem
            length = str.length
        }
    }

    fun unwrapCStr(struct: EmString, dealloc: Boolean): String {
        val struct = struct.unsafeCast<EmString>()

        val result = module.UTF8ToString(struct.data, struct.length)
        if(dealloc && struct.data !in allocated)
            module._free(struct.data)

        return result
    }

    // Primitive Arrays

    // Array: char

    fun toNativeCharArray(arr: CharArray) =
        toNativeCharArray(module, arr).also { ptr(it.elements) }

    fun toKotlinCharArray(struct: EmArray, dealloc: Boolean) =
        toKotlinCharArray(module, struct, dealloc && struct.unsafeCast<EmArray>().elements !in allocated)

    // Array: boolean

    fun toNativeBooleanArray(arr: BooleanArray) =
        toNativeBooleanArray(module, arr).also { ptr(it.elements) }

    fun toKotlinBooleanArray(struct: EmArray, dealloc: Boolean) =
        toKotlinBooleanArray(module, struct, dealloc && struct.unsafeCast<EmArray>().elements !in allocated)

    // Array: byte

    fun toNativeByteArray(arr: ByteArray) =
        toNativeByteArray(module, arr).also { ptr(it.elements) }

    fun toKotlinByteArray(struct: EmArray, dealloc: Boolean) =
        toKotlinByteArray(module, struct, dealloc && struct.unsafeCast<EmArray>().elements !in allocated)

    // Array: short

    fun toNativeShortArray(arr: ShortArray) =
        toNativeShortArray(module, arr).also { ptr(it.elements) }

    fun toKotlinShortArray(struct: EmArray, dealloc: Boolean): ShortArray =
        toKotlinShortArray(module, struct, dealloc && struct.unsafeCast<EmArray>().elements !in allocated)

    // Array: int

    fun toNativeIntArray(arr: IntArray) =
        toNativeIntArray(module, arr).also { ptr(it.elements) }

    fun toKotlinIntArray(struct: EmArray, dealloc: Boolean) =
        toKotlinIntArray(module, struct, dealloc && struct.unsafeCast<EmArray>().elements !in allocated)

    // Array: long

    fun toNativeLongArray(arr: LongArray) =
        toNativeLongArray(module, arr).also { ptr(it.elements) }

    fun toKotlinLongArray(struct: EmArray, dealloc: Boolean): LongArray =
        toKotlinLongArray(module, struct, dealloc && struct.unsafeCast<EmArray>().elements !in allocated)

    // Array: float

    fun toNativeFloatArray(arr: FloatArray) =
        toNativeFloatArray(module, arr).also { ptr(it.elements) }

    fun toKotlinFloatArray(struct: EmArray, dealloc: Boolean): FloatArray =
        toKotlinFloatArray(module, struct, dealloc && struct.unsafeCast<EmArray>().elements !in allocated)

    // Array: double

    fun toNativeDoubleArray(arr: DoubleArray) =
        toNativeDoubleArray(module, arr).also { ptr(it.elements) }

    fun toKotlinDoubleArray(struct: EmArray, dealloc: Boolean): DoubleArray =
        toKotlinDoubleArray(module, struct, dealloc && struct.unsafeCast<EmArray>().elements !in allocated)


    // Callbacks

    fun <T> unwrapCallback(ptr: Int, dealloc: Boolean): T {
        val result = unwrapCallback<T>(module, ptr, dealloc)
        if(dealloc && ptr !in allocated)
            module._free(ptr)
        return result
    }

    fun callback(callback: Int): Int {
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