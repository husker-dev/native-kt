@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("UNUSED", "UNNECESSARY_NOT_NULL_ASSERTION", "REDUNDANT_CALL_OF_CONVERSION_METHOD")

package com.huskerdev.nativekt.web


import kotlin.collections.set
import kotlin.js.*
import kotlin.math.truncate

const val FLAG_RELEASABLE = 1
const val FLAG_DATA_OWNER = 2

private val callbacks = hashMapOf<Pair<EmModule, Int>, Any>()

fun Float.truncF32(): Float {
    val factor = 10_000_000
    return truncate(this * factor) / factor
}

fun <T: JsAny> loadLib(lib: JsAny): Promise<T> =
    js("'__esModule' in lib ? lib.default() : lib()")

fun Boolean.toInt() = if(this) 1 else 0
fun Int.toBoolean() = this == 1
fun Byte.toBoolean() = this == 1.toByte()

private val layoutString = CStructLayout(CStructLayout.Ptr::class, Int::class, Int::class, Byte::class)
private val layoutArray = CStructLayout(CStructLayout.Ptr::class, Int::class, Int::class, Byte::class)
private val layoutCallback = CStructLayout(Byte::class, CStructLayout.Ptr::class, CStructLayout.Ptr::class, CStructLayout.Ptr::class, CStructLayout.Ptr::class, CStructLayout.Ptr::class)


// String

fun Arena.toNativeKStringOnArena(str: String?): Int {
    if(str == null) return 0
    val size = module.lengthBytesUTF8(str) + 1
    val strMem = alloc(size)
    module.stringToUTF8(str, strMem, size)

    val mem = alloc(layoutString.size)
    module.HEAP32[(mem + layoutString[0]) shr 2] = strMem
    module.HEAP32[(mem + layoutString[1]) shr 2] = size - 1
    module.HEAP32[(mem + layoutString[2]) shr 2] = str.length
    module.HEAP8[mem + layoutString[3]] = 0
    return mem
}

fun toNativeKString(module: EmModule, str: String?): Int {
    if(str == null) return 0
    val size = module.lengthBytesUTF8(str) + 1
    val strMem = module._malloc(size)
    module.stringToUTF8(str, strMem, size)

    val mem = module._malloc(layoutString.size)
    module.HEAP32[(mem + layoutString[0]) shr 2] = strMem
    module.HEAP32[(mem + layoutString[1]) shr 2] = size - 1
    module.HEAP32[(mem + layoutString[2]) shr 2] = str.length
    module.HEAP8[mem + layoutString[3]] = (FLAG_RELEASABLE or FLAG_DATA_OWNER).toByte()
    return mem
}

fun toKotlinKString(module: EmModule, mem: Int): String? {
    if(mem == 0) return null
    return module.UTF8ToString(
        module.HEAP32[(mem + layoutString[0]) shr 2],
        module.HEAP32[(mem + layoutString[1]) shr 2]
    )
}

// Array

private fun fillArray(module: EmModule, mem: Int, data: Int, size: Int, length: Int, flags: Byte): Int {
    module.HEAP32[(mem + layoutArray[0]) shr 2] = data
    module.HEAP32[(mem + layoutArray[1]) shr 2] = size
    module.HEAP32[(mem + layoutArray[2]) shr 2] = length
    module.HEAP8[mem + layoutArray[3]] = flags
    return mem
}

private fun arrayData(module: EmModule, mem: Int) = module.HEAP32[(mem + layoutArray[0]) shr 2]
private fun arrayLength(module: EmModule, mem: Int) = module.HEAP32[(mem + layoutArray[2]) shr 2]

// Array: char

fun Arena.toNativeKCharArrayOnArena(arr: CharArray?): Int {
    if(arr == null) return 0
    val size = arr.size * 2
    val data = alloc(size)
    val heap = Uint16Array(module.HEAP8.buffer, data, arr.size)
    arr.forEachIndexed { i, it -> heap[i] = it }
    return fillArray(module, alloc(layoutArray.size), data, size, arr.size, 0)
}

fun toNativeKCharArray(module: EmModule, arr: CharArray?): Int {
    if(arr == null) return 0
    val size = arr.size * 2
    val data = module._malloc(size)
    val heap = Uint16Array(module.HEAP8.buffer, data, arr.size)
    arr.forEachIndexed { i, it -> heap[i] = it }
    return fillArray(module, module._malloc(layoutArray.size), data, size, arr.size, (FLAG_RELEASABLE or FLAG_DATA_OWNER).toByte())
}

fun toKotlinKCharArray(module: EmModule, mem: Int): CharArray? {
    if(mem == 0) return null
    val length = arrayLength(module, mem)
    val arr = Uint16Array(module.HEAP8.buffer, arrayData(module, mem), length)
    return CharArray(length) { arr[it] }
}

// Array: boolean

fun Arena.toNativeKBooleanArrayOnArena(arr: BooleanArray?): Int {
    if(arr == null) return 0
    return toNativeKByteArrayOnArena(ByteArray(arr.size) { if (arr[it]) 1.toByte() else 0.toByte() })
}

fun toNativeKBooleanArray(module: EmModule, arr: BooleanArray?): Int {
    if(arr == null) return 0
    return toNativeKByteArray(module, ByteArray(arr.size) { if (arr[it]) 1.toByte() else 0.toByte() })
}

fun toKotlinKBooleanArray(module: EmModule, mem: Int): BooleanArray? {
    if(mem == 0) return null
    return toKotlinKByteArray(module, mem)!!.run { BooleanArray(size) { get(it) == 1.toByte() } }
}

// Array: byte

fun Arena.toNativeKByteArrayOnArena(arr: ByteArray?): Int {
    if(arr == null) return 0
    val data = alloc(arr.size)
    val heap = Int8Array(module.HEAP8.buffer, data, arr.size)
    arr.forEachIndexed { i, it -> heap[i] = it }
    return fillArray(module, alloc(layoutArray.size), data, arr.size, arr.size, 0)
}

fun toNativeKByteArray(module: EmModule, arr: ByteArray?): Int {
    if(arr == null) return 0
    val data = module._malloc(arr.size)
    val heap = Int8Array(module.HEAP8.buffer, data, arr.size)
    arr.forEachIndexed { i, it -> heap[i] = it }
    return fillArray(module, module._malloc(layoutArray.size), data, arr.size, arr.size, (FLAG_RELEASABLE or FLAG_DATA_OWNER).toByte())
}

fun toKotlinKByteArray(module: EmModule, mem: Int): ByteArray? {
    if(mem == 0) return null
    val length = arrayLength(module, mem)
    val arr = Int8Array(module.HEAP8.buffer, arrayData(module, mem), length)
    return ByteArray(length) { arr[it] }
}

// Array: short

fun Arena.toNativeKShortArrayOnArena(arr: ShortArray?): Int {
    if(arr == null) return 0
    val size = arr.size * 2
    val data = alloc(size)
    val heap = Int16Array(module.HEAP8.buffer, data, arr.size)
    arr.forEachIndexed { i, it -> heap[i] = it }
    return fillArray(module, alloc(layoutArray.size), data, size, arr.size, 0)
}

fun toNativeKShortArray(module: EmModule, arr: ShortArray?): Int {
    if(arr == null) return 0
    val size = arr.size * 2
    val data = module._malloc(size)
    val heap = Int16Array(module.HEAP8.buffer, data, arr.size)
    arr.forEachIndexed { i, it -> heap[i] = it }
    return fillArray(module, module._malloc(layoutArray.size), data, size, arr.size, (FLAG_RELEASABLE or FLAG_DATA_OWNER).toByte())
}

fun toKotlinKShortArray(module: EmModule, mem: Int): ShortArray? {
    if(mem == 0) return null
    val length = arrayLength(module, mem)
    val arr = Int16Array(module.HEAP8.buffer, arrayData(module, mem), length)
    return ShortArray(length) { arr[it] }
}

// Array: int

fun Arena.toNativeKIntArrayOnArena(arr: IntArray?): Int {
    if(arr == null) return 0
    val size = arr.size * 4
    val data = alloc(size)
    val heap = Int32Array(module.HEAP8.buffer, data, arr.size)
    arr.forEachIndexed { i, it -> heap[i] = it }
    return fillArray(module, alloc(layoutArray.size), data, size, arr.size, 0)
}

fun toNativeKIntArray(module: EmModule, arr: IntArray?): Int {
    if(arr == null) return 0
    val size = arr.size * 4
    val data = module._malloc(size)
    val heap = Int32Array(module.HEAP8.buffer, data, arr.size)
    arr.forEachIndexed { i, it -> heap[i] = it }
    return fillArray(module, module._malloc(layoutArray.size), data, size, arr.size, (FLAG_RELEASABLE or FLAG_DATA_OWNER).toByte())
}

fun toKotlinKIntArray(module: EmModule, mem: Int): IntArray? {
    if(mem == 0) return null
    val length = arrayLength(module, mem)
    val arr = Int32Array(module.HEAP8.buffer, arrayData(module, mem), length)
    return IntArray(length) { arr[it] }
}

// Array: long

fun Arena.toNativeKLongArrayOnArena(arr: LongArray?): Int {
    if(arr == null) return 0
    val size = arr.size * 8
    val data = alloc(size)
    val heap = BigInt64Array(module.HEAP8.buffer, data, arr.size)
    arr.forEachIndexed { i, it -> heap[i] = it }
    return fillArray(module, alloc(layoutArray.size), data, size, arr.size, 0)
}

fun toNativeKLongArray(module: EmModule, arr: LongArray?): Int {
    if(arr == null) return 0
    val size = arr.size * 8
    val data = module._malloc(size)
    val heap = BigInt64Array(module.HEAP8.buffer, data, arr.size)
    arr.forEachIndexed { i, it -> heap[i] = it }
    return fillArray(module, module._malloc(layoutArray.size), data, size, arr.size, (FLAG_RELEASABLE or FLAG_DATA_OWNER).toByte())
}

fun toKotlinKLongArray(module: EmModule, mem: Int): LongArray? {
    if(mem == 0) return null
    val length = arrayLength(module, mem)
    val arr = BigInt64Array(module.HEAP8.buffer, arrayData(module, mem), length)
    return LongArray(length) { arr[it] }
}

// Array: float

fun Arena.toNativeKFloatArrayOnArena(arr: FloatArray?): Int {
    if(arr == null) return 0
    val size = arr.size * 4
    val data = alloc(size)
    val heap = Float32Array(module.HEAPF32.buffer, data, arr.size)
    arr.forEachIndexed { i, it -> heap[i] = it }
    return fillArray(module, alloc(layoutArray.size), data, size, arr.size, 0)
}

fun toNativeKFloatArray(module: EmModule, arr: FloatArray?): Int {
    if(arr == null) return 0
    val size = arr.size * 4
    val data = module._malloc(size)
    val heap = Float32Array(module.HEAPF32.buffer, data, arr.size)
    arr.forEachIndexed { i, it -> heap[i] = it }
    return fillArray(module, module._malloc(layoutArray.size), data, size, arr.size, (FLAG_RELEASABLE or FLAG_DATA_OWNER).toByte())
}

fun toKotlinKFloatArray(module: EmModule, mem: Int): FloatArray? {
    if(mem == 0) return null
    val length = arrayLength(module, mem)
    val arr = Float32Array(module.HEAPF32.buffer, arrayData(module, mem), length)
    return FloatArray(length) { arr[it] }
}

// Array: double

fun Arena.toNativeKDoubleArrayOnArena(arr: DoubleArray?): Int {
    if(arr == null) return 0
    val size = arr.size * 8
    val data = alloc(size)
    val heap = Float64Array(module.HEAPF64.buffer, data, arr.size)
    arr.forEachIndexed { i, it -> heap[i] = it }
    return fillArray(module, alloc(layoutArray.size), data, size, arr.size, 0)
}

fun toNativeKDoubleArray(module: EmModule, arr: DoubleArray?): Int {
    if(arr == null) return 0
    val size = arr.size * 8
    val data = module._malloc(size)
    val heap = Float64Array(module.HEAPF64.buffer, data, arr.size)
    arr.forEachIndexed { i, it -> heap[i] = it }
    return fillArray(module, module._malloc(layoutArray.size), data, size, arr.size, (FLAG_RELEASABLE or FLAG_DATA_OWNER).toByte())
}

fun toKotlinKDoubleArray(module: EmModule, mem: Int): DoubleArray? {
    if(mem == 0) return null
    val length = arrayLength(module, mem)
    val arr = Float64Array(module.HEAPF64.buffer, arrayData(module, mem), length)
    return DoubleArray(length) { arr[it] }
}

// Array: enum

fun <T: Enum<T>> Arena.toNativeEnumArrayOnArena(arr: Array<T>?): Int {
    if(arr == null) return 0
    return toNativeKIntArrayOnArena(IntArray(arr.size) { arr[it].ordinal })
}

fun <T: Enum<T>> toNativeEnumArray(module: EmModule, arr: Array<T>?): Int {
    if(arr == null) return 0
    return toNativeKIntArray(module, IntArray(arr.size) { arr[it].ordinal })
}

inline fun <reified T: Enum<T>> toKotlinEnumArray(module: EmModule, mem: Int): Array<T>? {
    if(mem == 0) return null
    val entries = enumValues<T>()
    val ints = toKotlinKIntArray(module, mem)!!
    return Array(ints.size) { entries[ints[it]] }
}

// Array: objects

fun <T> Arena.toNativeKArrayOnArena(
    arr: Array<T>?,
    converter: (T) -> Int
): Int {
    if(arr == null) return 0
    return toNativeKIntArrayOnArena(IntArray(arr.size) { converter(arr[it]) })
}

fun <T, A: EmModule> toNativeKArray(
    module: A,
    arr: Array<T>?,
    converter: (A, T) -> Int
): Int {
    if(arr == null) return 0
    return toNativeKIntArray(module, IntArray(arr.size) { converter(module, arr[it]) })
}

@Suppress("unchecked_cast")
fun <T, A: EmModule> toKotlinKArray(
    module: A,
    struct: Int,
    converter: (A, Int) -> T?
): Array<T>? {
    if(struct == 0) return null
    val ints = toKotlinKIntArray(module, struct)!!
    return Array<Any?>(ints.size) { converter(module, ints[it]) } as Array<T>
}


// Callbacks

fun Arena.toNativeCallbackOnArena(
    callback: Any?,
    invoke: Int,
    clone: Int,
    equals: Int,
    hashCode: Int,
    free: Int
): Int {
    if(callback == null) return 0
    val module = module
    val mem = alloc(layoutCallback.size)
    module.HEAP8[mem + layoutCallback[0]] = 0
    module.HEAP32[(mem + layoutCallback[1]) shr 2] = invoke
    module.HEAP32[(mem + layoutCallback[2]) shr 2] = clone
    module.HEAP32[(mem + layoutCallback[3]) shr 2] = equals
    module.HEAP32[(mem + layoutCallback[4]) shr 2] = hashCode
    module.HEAP32[(mem + layoutCallback[5]) shr 2] = free

    val key = Pair(module, mem)
    callbacks[key] = callback
    defer { callbacks -= key }
    return mem
}

fun toNativeCallback(
    module: EmModule,
    callback: Any?,
    invoke: Int,
    clone: Int,
    equals: Int,
    hashCode: Int,
    free: Int
): Int {
    if(callback == null) return 0
    val mem = module._malloc(layoutCallback.size)
    module.HEAP8[mem + layoutCallback[0]] = FLAG_RELEASABLE.toByte()
    module.HEAP32[(mem + layoutCallback[1]) shr 2] = invoke
    module.HEAP32[(mem + layoutCallback[2]) shr 2] = clone
    module.HEAP32[(mem + layoutCallback[3]) shr 2] = equals
    module.HEAP32[(mem + layoutCallback[4]) shr 2] = hashCode
    module.HEAP32[(mem + layoutCallback[5]) shr 2] = free
    callbacks[Pair(module, mem)] = callback
    return mem
}

@Suppress("UNCHECKED_CAST")
fun <T> toKotlinCallback(
    module: EmModule,
    mem: Int,
): T? {
    if(mem == 0) return null
    return callbacks[Pair(module, mem)] as T
}

fun callbackFree(
    module: EmModule,
    self: Int
) {
    if(self == 0 || module.HEAP8[self + layoutCallback[0]].toInt() and FLAG_RELEASABLE != FLAG_RELEASABLE)
        return

    callbacks.remove(Pair(module, self))
    module._free(self)
}

private fun callbackClone(
    module: EmModule,
    self: Int
): Int {
    if(self == 0)
        return 0
    val mem = module._malloc(layoutCallback.size)
    module.HEAP8[mem + layoutCallback[0]] = FLAG_RELEASABLE.toByte()
    module.HEAP32[(mem + layoutCallback[1]) shr 2] = module.HEAP32[(self + layoutCallback[1]) shr 2]
    module.HEAP32[(mem + layoutCallback[2]) shr 2] = module.HEAP32[(self + layoutCallback[2]) shr 2]
    module.HEAP32[(mem + layoutCallback[3]) shr 2] = module.HEAP32[(self + layoutCallback[3]) shr 2]
    module.HEAP32[(mem + layoutCallback[4]) shr 2] = module.HEAP32[(self + layoutCallback[4]) shr 2]
    module.HEAP32[(mem + layoutCallback[5]) shr 2] = module.HEAP32[(self + layoutCallback[5]) shr 2]
    callbacks[Pair(module, mem)] = toKotlinCallback(module, self)!!
    return mem
}

private fun callbackEquals(
    module: EmModule,
    self: Int,
    obj: Int
): Boolean = callbacks[Pair(module, self)] == callbacks[Pair(module, obj)]

private fun callbackHashCode(
    module: EmModule,
    self: Int
): Int = callbacks[Pair(module, self)]!!.hashCode()

private fun funcVP(block: (Int) -> Unit): JsAny = js("block")
private fun funcPP(block: (Int) -> Int): JsAny = js("block")
private fun funcIPP(block: (Int, Int) -> Boolean): JsAny = js("block")
private fun funcIP(block: (Int) -> Int): JsAny = js("block")

fun createCallbackCloneFunction(module: EmModule): Int {
    return module.addFunction(funcPP { self: Int ->
        callbackClone(module, self)
    }, "pp")
}

fun createCallbackEqualsFunction(module: EmModule): Int {
    return module.addFunction(funcIPP { self: Int, obj: Int ->
        callbackEquals(module, self, obj)
    }, "ipp")
}

fun createCallbackHashCodeFunction(module: EmModule): Int {
    return module.addFunction(funcIP { self: Int ->
        callbackHashCode(module, self)
    }, "ip")
}

fun createCallbackFreeFunction(module: EmModule): Int {
    return module.addFunction(funcVP { self: Int ->
        callbackFree(module, self)
    }, "vp")
}