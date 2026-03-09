@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("unused")

package com.huskerdev.nativekt.web

import org.khronos.webgl.*
import kotlin.js.json
import kotlin.math.truncate

private val callbacks = hashMapOf<Pair<dynamic, Int>, Any>()

fun Float.truncF32(): Float {
    val factor = 10_000_000
    return truncate(this * factor) / factor
}

fun allocCStr(module: dynamic, str: String): Any {
    val len = module.lengthBytesUTF8(str) + 1
    val strMem = module._malloc(len)
    module.stringToUTF8(str, strMem, len)

    return json(
        "data" to strMem,
        "length" to str.length
    )
}

fun unwrapCStr(module: dynamic, struct: dynamic, dealloc: Boolean): String {
    val data = (struct.data as JsNumber).toInt()
    val length = (struct.length as JsNumber).toInt()

    val result = module.UTF8ToString(data, length)
    if(dealloc)
        module._free(data)
    return result
}


// Primitive Arrays

private fun <T> toArray(obj: Any): Array<T> =
    js("function(e) { return Array.from(e); }")(obj)

// Array: char

fun toNativeCharArray(module: dynamic, arr: CharArray): dynamic {
    val elements = module._malloc(arr.size * Char.SIZE_BYTES)
    Int16Array(module.HEAP8.buffer, elements, arr.size)
        .set(arr.map { it.code.toShort() }.toTypedArray())
    return json("elements" to elements, "size" to arr.size)
}

fun toKotlinCharArray(module: dynamic, struct: dynamic, dealloc: Boolean): CharArray {
    val elements = (struct.elements as JsNumber).toInt()
    val size = (struct.size as JsNumber).toInt()

    val result = toArray<Short>(Int16Array(module.HEAP8.buffer, elements, size))
        .map { it.toInt().toChar() }.toCharArray()
    if(dealloc) module._free(elements)
    return result
}

// Array: boolean

fun toNativeBooleanArray(module: dynamic, arr: BooleanArray): dynamic {
    val elements = module._malloc(arr.size * Byte.SIZE_BYTES)
    Int8Array(module.HEAP8.buffer, elements, arr.size)
        .set(arr.map { (if(it) 1 else 0).toByte() }.toTypedArray())
    return json("elements" to elements, "size" to arr.size)
}

fun toKotlinBooleanArray(module: dynamic, struct: dynamic, dealloc: Boolean): BooleanArray {
    val elements = (struct.elements as JsNumber).toInt()
    val size = (struct.size as JsNumber).toInt()

    val result = toArray<Byte>(Int8Array(module.HEAP8.buffer, elements, size))
        .map { it == 1.toByte() }.toBooleanArray()
    if(dealloc) module._free(elements)
    return result
}

// Array: byte

fun toNativeByteArray(module: dynamic, arr: ByteArray): dynamic {
    val elements = module._malloc(arr.size * Byte.SIZE_BYTES)
    Int8Array(module.HEAP8.buffer, elements, arr.size).set(arr.toTypedArray())
    return json("elements" to elements, "size" to arr.size)
}

fun toKotlinByteArray(module: dynamic, struct: dynamic, dealloc: Boolean): ByteArray {
    val elements = (struct.elements as JsNumber).toInt()
    val size = (struct.size as JsNumber).toInt()

    val result = toArray<Byte>(Int8Array(module.HEAP8.buffer, elements, size)).toByteArray()
    if(dealloc) module._free(elements)
    return result
}

// Array: short

fun toNativeShortArray(module: dynamic, arr: ShortArray): dynamic {
    val elements = module._malloc(arr.size * Short.SIZE_BYTES)
    Int16Array(module.HEAP8.buffer, elements, arr.size).set(arr.toTypedArray())
    return json("elements" to elements, "size" to arr.size)
}

fun toKotlinShortArray(module: dynamic, struct: dynamic, dealloc: Boolean): ShortArray {
    val elements = (struct.elements as JsNumber).toInt()
    val size = (struct.size as JsNumber).toInt()

    val result = toArray<Short>(Int16Array(module.HEAP8.buffer, elements, size)).toShortArray()
    if(dealloc) module._free(elements)
    return result
}

// Array: int

fun toNativeIntArray(module: dynamic, arr: IntArray): dynamic {
    val elements = module._malloc(arr.size * Int.SIZE_BYTES)
    Int32Array(module.HEAP8.buffer, elements, arr.size).set(arr.toTypedArray())
    return json("elements" to elements, "size" to arr.size)
}

fun toKotlinIntArray(module: dynamic, struct: dynamic, dealloc: Boolean): IntArray {
    val elements = (struct.elements as JsNumber).toInt()
    val size = (struct.size as JsNumber).toInt()

    val result = toArray<Int>(Int32Array(module.HEAP8.buffer, elements, size)).toIntArray()
    if(dealloc) module._free(elements)
    return result
}

// Array: long

external class BigInt64Array(buffer: dynamic, byteOffset: Int, length: Int) {
    fun set(value: dynamic)
}

fun toNativeLongArray(module: dynamic, arr: LongArray): dynamic {
    val elements = module._malloc(arr.size * Long.SIZE_BYTES)
    BigInt64Array(module.HEAP8.buffer, elements, arr.size)
        .set(arr.toTypedArray())
    return json("elements" to elements, "size" to arr.size)
}

fun toKotlinLongArray(module: dynamic, struct: dynamic, dealloc: Boolean): LongArray {
    val elements = (struct.elements as JsNumber).toInt()
    val size = (struct.size as JsNumber).toInt()

    val result = toArray<Long>(BigInt64Array(module.HEAP8.buffer, elements, size)).toLongArray()
    if(dealloc) module._free(elements)
    return result
}

// Array: float

fun toNativeFloatArray(module: dynamic, arr: FloatArray): dynamic {
    val elements = module._malloc(arr.size * Float.SIZE_BYTES)
    Float32Array(module.HEAPF32.buffer, elements, arr.size).set(arr.toTypedArray())
    return json("elements" to elements, "size" to arr.size)
}

fun toKotlinFloatArray(module: dynamic, struct: dynamic, dealloc: Boolean): FloatArray {
    val elements = (struct.elements as JsNumber).toInt()
    val size = (struct.size as JsNumber).toInt()

    val result = toArray<Float>(Float32Array(module.HEAPF32.buffer, elements, size))
    if(dealloc) module._free(elements)
    return FloatArray(size) { result[it].truncF32() }
}

// Array: double

fun toNativeDoubleArray(module: dynamic, arr: DoubleArray): dynamic {
    val elements = module._malloc(arr.size * Double.SIZE_BYTES)
    Float64Array(module.HEAPF32.buffer, elements, arr.size).set(arr.toTypedArray())
    return json("elements" to elements, "size" to arr.size)
}

fun toKotlinDoubleArray(module: dynamic, struct: dynamic, dealloc: Boolean): DoubleArray {
    val elements = (struct.elements as JsNumber).toInt()
    val size = (struct.size as JsNumber).toInt()

    val result = toArray<Double>(Float64Array(module.HEAPF32.buffer, elements, size)).toDoubleArray()
    if(dealloc) module._free(elements)
    return result
}


// Callbacks

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