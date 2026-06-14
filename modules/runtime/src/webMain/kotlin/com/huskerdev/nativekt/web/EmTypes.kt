@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("unused")

package com.huskerdev.nativekt.web

import kotlin.js.*

fun <T: JsAny> createJsObject(): T = js("({})")

fun <T: JsAny> createJsObject(block: T.() -> Unit): T =
    createJsObject<T>().apply(block)

external interface EmModule: JsAny {
    fun lengthBytesUTF8(str: String): Int
    fun stringToUTF8(str: String, mem: Int, length: Int)
    fun UTF8ToString(data: Int, length: Int): String
    fun addFunction(func: JsAny, s: String): Int

    val HEAP8: Int8Array
    val HEAP16: Int16Array
    val HEAP32: Int32Array
    val HEAPF32: Float32Array
    val HEAPF64: Float64Array

    fun _malloc(size: Int): Int
    fun _free(mem: Int)
}

external interface EmString: JsAny {
    var data: Int
    var length: Int
    var releasable: Boolean
    var released: Boolean
}

external interface EmArray: JsAny {
    var elements: Int
    var size: Int
    var releasable: Boolean
    var released: Boolean
}

// Types arrays

@JsName("Array")
external object JsArrayTools {
    fun <T: JsAny> from(arrayLike: JsAny): JsArray<T>
}

external class ArrayBuffer: JsAny {
    val byteLength: Int
}

abstract external class TypedArray: JsAny {
    fun set(array: JsArray<JsNumber>)
}

private fun setTypedArrayAt(array: TypedArray, index: Int, value: Double): Unit =
    js("array[index] = value")

private fun getTypedArray(array: TypedArray, index: Int): Double =
    js("array[index]")

private fun setBigIntArray(array: BigInt64Array, index: Int, value: JsBigInt): Unit =
    js("array[index] = value")

private fun getBigIntArray(array: BigInt64Array, index: Int): Long =
    js("array[index]")

external class Int8Array(
    val buffer: ArrayBuffer,
    byteOffset: Int,
    length: Int
): TypedArray

operator fun Int8Array.set(index: Int, value: Byte) =
    setTypedArrayAt(this, index, value.toDouble())

operator fun Int8Array.set(index: Int, value: Boolean) =
    setTypedArrayAt(this, index, value.toInt().toDouble())

operator fun Int8Array.get(index: Int): Byte =
    getTypedArray(this, index).toInt().toByte()

external class Int16Array(
    val buffer: ArrayBuffer,
    byteOffset: Int,
    length: Int
): TypedArray

operator fun Int16Array.set(index: Int, value: Short) =
    setTypedArrayAt(this, index, value.toDouble())

operator fun Int16Array.set(index: Int, value: Char) =
    setTypedArrayAt(this, index, value.code.toDouble())

operator fun Int16Array.set(index: Int, value: Int) =
    setTypedArrayAt(this, index, value.toDouble())

operator fun Int16Array.get(index: Int): Short =
    getTypedArray(this, index).toInt().toShort()

external class Uint16Array(
    val buffer: ArrayBuffer,
    byteOffset: Int,
    length: Int
): TypedArray

operator fun Uint16Array.set(index: Int, value: Char) =
    setTypedArrayAt(this, index, value.code.toDouble())

operator fun Uint16Array.get(index: Int): Char =
    getTypedArray(this, index).toInt().toChar()

external class Int32Array(
    val buffer: ArrayBuffer,
    byteOffset: Int,
    length: Int
): TypedArray

operator fun Int32Array.set(index: Int, value: Int) =
    setTypedArrayAt(this, index, value.toDouble())

operator fun Int32Array.get(index: Int): Int =
    getTypedArray(this, index).toInt()

external class Float32Array(
    val buffer: ArrayBuffer,
    byteOffset: Int,
    length: Int
): TypedArray

operator fun Float32Array.set(index: Int, value: Float) =
    setTypedArrayAt(this, index, value.toDouble())

operator fun Float32Array.get(index: Int): Float =
    getTypedArray(this, index).toFloat()

external class Float64Array(
    val buffer: ArrayBuffer,
    byteOffset: Int,
    length: Int
): TypedArray

operator fun Float64Array.set(index: Int, value: Double) =
    setTypedArrayAt(this, index, value)

operator fun Float64Array.get(index: Int): Double =
    getTypedArray(this, index)

external class BigInt64Array(
    val buffer: ArrayBuffer,
    byteOffset: Int,
    length: Int
): JsAny {
    fun set(array: JsArray<JsBigInt>)
}

operator fun BigInt64Array.set(index: Int, value: Long) =
    setBigIntArray(this, index, value.toJsBigInt())

operator fun BigInt64Array.get(index: Int): Long =
    getBigIntArray(this, index)