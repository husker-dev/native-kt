@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("unused")

package com.huskerdev.nativekt.web

import kotlin.js.*
import kotlin.math.truncate

fun <T: JsAny> loadLib(lib: JsAny): Promise<T> =
    js("'__esModule' in lib ? lib.default() : lib()")

private fun createReadStream(path: String): JsAny =
    js("process.getBuiltinModule('fs').createReadStream(path)")

private fun getWasmPath(name: String): String =
    js("new URL('.', import.meta.url).pathname + name")

private fun makeResponse(stream: JsAny): JsAny =
    js("new Response(stream, { headers: { 'Content-Type': 'application/wasm' } })")

private fun compileWasm(response: JsAny): Promise<WebAssemblyModule> =
    js("WebAssembly.compileStreaming(response)")

fun wrapModule(mod: WebAssemblyModule): JsAny =
    js("(function(a){ return { module: mod }; })(mod)")

fun compileWasmFromFileAsync(name: String): Promise<WebAssemblyModule> {
    val stream = createReadStream(getWasmPath(name))
    val response = makeResponse(stream)
    return compileWasm(response)
}

fun Float.truncF32(): Float {
    val factor = 10_000_000
    return truncate(this * factor) / factor
}

fun Boolean.toInt() = if(this) 1 else 0
fun Int.toBoolean() = this == 1
fun Byte.toBoolean() = this == 1.toByte()

fun Long.fromUnsignedBigInt(): ULong =
    fromUnsignedBigIntImpl(this).toULong()

private fun fromUnsignedBigIntImpl(of: Long): Long =
    js("BigInt.asIntN(64, of)")

external interface WebAssemblyModule: JsAny

external interface WebAssemblyMemory: JsAny {
    val buffer: ArrayBuffer
}

external interface ArrayBuffer: JsAny

external interface TypedArray: JsAny

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

fun Int8Array.getBoolean(index: Int): Boolean =
    getTypedArray(this, index).toInt() == 1

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