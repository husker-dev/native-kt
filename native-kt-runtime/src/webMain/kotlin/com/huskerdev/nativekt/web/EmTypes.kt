@file:OptIn(ExperimentalWasmJsInterop::class)

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
    val HEAP32: Int32Array
    val HEAPF32: Float32Array

    fun _malloc(size: Int): Int
    fun _free(mem: Int)
}

external interface EmString: JsAny {
    var data: Int
    var length: Int
}

external interface EmArray: JsAny {
    var elements: Int
    var size: Int
}

// Types arrays

@JsName("Array")
external object JsArrayTools {
    fun <T: JsAny> from(arrayLike: JsAny): JsArray<T>
}

external class ArrayBuffer: JsAny

abstract external class TypedArray: JsAny {
    fun set(array: JsArray<JsNumber>)
}

@Suppress("unused")
private fun setTypedArray(array: TypedArray, index: Int, value: Int): Unit =
    js("array[index] = value")

@Suppress("unused")
private fun getTypedArray(array: TypedArray, index: Int): Int =
    js("array[index]")

operator fun TypedArray.set(index: Int, value: Int) =
    setTypedArray(this, index, value)

operator fun TypedArray.get(index: Int): Int =
    getTypedArray(this, index)

external class Int8Array(
    val buffer: ArrayBuffer,
    byteOffset: Int,
    length: Int
): TypedArray

external class Int16Array(
    val buffer: ArrayBuffer,
    byteOffset: Int,
    length: Int
): TypedArray

external class Int32Array(
    val buffer: ArrayBuffer,
    byteOffset: Int,
    length: Int
): TypedArray

external class Float32Array(
    val buffer: ArrayBuffer,
    byteOffset: Int,
    length: Int
): TypedArray

external class Float64Array(
    val buffer: ArrayBuffer,
    byteOffset: Int,
    length: Int
): TypedArray

external class BigInt64Array(
    val buffer: ArrayBuffer,
    byteOffset: Int,
    length: Int
): JsAny {
    fun set(array: JsArray<JsBigInt>)
}