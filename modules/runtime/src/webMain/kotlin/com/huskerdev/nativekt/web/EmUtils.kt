@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("UNUSED", "UNNECESSARY_NOT_NULL_ASSERTION", "REDUNDANT_CALL_OF_CONVERSION_METHOD")

package com.huskerdev.nativekt.web

import kotlin.js.*
import kotlin.js.set
import kotlin.math.truncate

private val callbacks = hashMapOf<Pair<EmModule, Int>, Any>()

private inline fun <T: JsAny> createJsArray(size: Int, block: (Int) -> T): JsArray<T> {
    val arr = JsArray<T>()
    for (i in 0 until size)
        arr[i] = block(i)
    return arr
}

fun Float.truncF32(): Float {
    val factor = 10_000_000
    return truncate(this * factor) / factor
}

fun <T: JsAny> loadLib(lib: JsAny): Promise<T> =
    js("'__esModule' in lib ? lib.default() : lib()")

fun Boolean.toInt() = if(this) 1 else 0

fun toNativeString(module: EmModule, str: String, releasable: Boolean): EmString {
    val len = module.lengthBytesUTF8(str) + 1
    val strMem = module._malloc(len)
    module.stringToUTF8(str, strMem, len)

    return createJsObject {
        this.data = strMem
        this.length = str.length
        this.releasable = releasable
        this.released = false
    }
}

fun toKotlinString(module: EmModule, struct: EmString, dealloc: Boolean): String {
    val result = module.UTF8ToString(struct.data, struct.length)
    if(dealloc)
        module._free(struct.data)
    return result
}

fun fillEmString(module: EmModule, ptr: Int, str: EmString) {
    module.HEAP32[ptr shr 2] = str.data
    module.HEAP32[(ptr shr 2) + 1] = str.length
    module.HEAP32[(ptr shr 2) + 2] = str.releasable.toInt()
    module.HEAP32[(ptr shr 2) + 3] = str.released.toInt()
}

fun extractEmString(module: EmModule, ptr: Int): EmString = createJsObject {
    data = module.HEAP32[ptr shr 2]
    length = module.HEAP32[(ptr shr 2) + 1]
}

// Array

fun fillEmArray(module: EmModule, ptr: Int, arr: EmArray) {
    module.HEAP32[ptr shr 2] = arr.elements
    module.HEAP32[(ptr shr 2) + 1] = arr.size
    module.HEAP32[(ptr shr 2) + 2] = arr.releasable.toInt()
    module.HEAP32[(ptr shr 2) + 3] = arr.released.toInt()
}

fun extractEmArray(module: EmModule, ptr: Int): EmArray = createJsObject {
    elements = module.HEAP32[ptr shr 2]
    size = module.HEAP32[(ptr shr 2) + 1]
}

// Array: char

fun toNativeCharArray(module: EmModule, arr: CharArray, releasable: Boolean): EmArray {
    val elements = module._malloc(arr.size * Char.SIZE_BYTES)
    Int16Array(module.HEAP8.buffer, elements, arr.size)
        .set(createJsArray(arr.size) {
            arr[it].code.toJsNumber()
        })
    return createJsObject {
        this.elements = elements
        this.size = arr.size
        this.releasable = releasable
        this.released = false
    }
}

fun toKotlinCharArray(module: EmModule, struct: EmArray, dealloc: Boolean): CharArray {
    val result = JsArrayTools.from<JsNumber>(Int16Array(module.HEAP8.buffer, struct.elements, struct.size))
        .run { CharArray(struct.size) {
            this[it]!!.toInt().toChar()
        }}
    if(dealloc) module._free(struct.elements)
    return result
}

// Array: boolean

fun toNativeBooleanArray(module: EmModule, arr: BooleanArray, releasable: Boolean): EmArray {
    val elements = module._malloc(arr.size * Byte.SIZE_BYTES)
    Int8Array(module.HEAP8.buffer, elements, arr.size)
        .set(createJsArray(arr.size) {
            (if(arr[it]) 1 else 0).toJsNumber()
        })
    return createJsObject {
        this.elements = elements
        this.size = arr.size
        this.releasable = releasable
        this.released = false
    }
}

fun toKotlinBooleanArray(module: EmModule, struct: EmArray, dealloc: Boolean): BooleanArray {
    val result = JsArrayTools.from<JsNumber>(Int8Array(module.HEAP8.buffer, struct.elements, struct.size))
        .run { BooleanArray(struct.size) {
            this[it]!!.toInt() == 1
        }}
    if(dealloc) module._free(struct.elements)
    return result
}

// Array: byte

fun toNativeByteArray(module: EmModule, arr: ByteArray, releasable: Boolean): EmArray {
    val elements = module._malloc(arr.size * Byte.SIZE_BYTES)
    Int8Array(module.HEAP8.buffer, elements, arr.size)
        .set(createJsArray(arr.size) {
            arr[it].toInt().toJsNumber()
        })
    return createJsObject {
        this.elements = elements
        this.size = arr.size
        this.releasable = releasable
        this.released = false
    }
}

fun toKotlinByteArray(module: EmModule, struct: EmArray, dealloc: Boolean): ByteArray {
    val result = JsArrayTools.from<JsNumber>(Int8Array(module.HEAP8.buffer, struct.elements, struct.size))
        .run { ByteArray(struct.size) {
            this[it]!!.toInt().toByte()
        }}
    if(dealloc) module._free(struct.elements)
    return result
}

// Array: short

fun toNativeShortArray(module: EmModule, arr: ShortArray, releasable: Boolean): EmArray {
    val elements = module._malloc(arr.size * Short.SIZE_BYTES)
    Int16Array(module.HEAP8.buffer, elements, arr.size)
        .set(createJsArray(arr.size) {
            arr[it].toInt().toJsNumber()
        })
    return createJsObject {
        this.elements = elements
        this.size = arr.size
        this.releasable = releasable
        this.released = false
    }
}

fun toKotlinShortArray(module: EmModule, struct: EmArray, dealloc: Boolean): ShortArray {
    val result = JsArrayTools.from<JsNumber>(Int16Array(module.HEAP8.buffer, struct.elements, struct.size))
        .run { ShortArray(struct.size) {
            this[it]!!.toInt().toShort()
        }}
    if(dealloc) module._free(struct.elements)
    return result
}

// Array: int

fun toNativeIntArray(module: EmModule, arr: IntArray, releasable: Boolean): EmArray {
    val elements = module._malloc(arr.size * Int.SIZE_BYTES)
    Int32Array(module.HEAP8.buffer, elements, arr.size)
        .set(createJsArray(arr.size) {
            arr[it].toJsNumber()
        })
    return createJsObject {
        this.elements = elements
        this.size = arr.size
        this.releasable = releasable
        this.released = false
    }
}

fun toKotlinIntArray(module: EmModule, struct: EmArray, dealloc: Boolean): IntArray {
    val result = JsArrayTools.from<JsNumber>(Int32Array(module.HEAP8.buffer, struct.elements, struct.size))
        .run { IntArray(struct.size) {
            this[it]!!.toInt()
        }}
    if(dealloc) module._free(struct.elements)
    return result
}

// Array: long

fun toNativeLongArray(module: EmModule, arr: LongArray, releasable: Boolean): EmArray {
    val elements = module._malloc(arr.size * Long.SIZE_BYTES)
    BigInt64Array(module.HEAP8.buffer, elements, arr.size)
        .set(createJsArray(arr.size) {
            arr[it].toJsBigInt()
        })
    return createJsObject {
        this.elements = elements
        this.size = arr.size
        this.releasable = releasable
        this.released = false
    }
}

fun toKotlinLongArray(module: EmModule, struct: EmArray, dealloc: Boolean): LongArray {
    val result = JsArrayTools.from<JsBigInt>(BigInt64Array(module.HEAP8.buffer, struct.elements, struct.size))
        .run { LongArray(struct.size) {
            this[it]!!.toLong()
        }}
    if(dealloc) module._free(struct.elements)
    return result
}

// Array: float

fun toNativeFloatArray(module: EmModule, arr: FloatArray, releasable: Boolean): EmArray {
    val elements = module._malloc(arr.size * Float.SIZE_BYTES)
    Float32Array(module.HEAPF32.buffer, elements, arr.size)
        .set(createJsArray(arr.size) {
            arr[it].toDouble().toJsNumber()
        })
    return createJsObject {
        this.elements = elements
        this.size = arr.size
        this.releasable = releasable
        this.released = false
    }
}

fun toKotlinFloatArray(module: EmModule, struct: EmArray, dealloc: Boolean): FloatArray {
    val result = JsArrayTools.from<JsNumber>(Float32Array(module.HEAPF32.buffer, struct.elements, struct.size))
        .run { FloatArray(struct.size) {
            this[it]!!.toDouble().toFloat().truncF32()
        }}
    if(dealloc) module._free(struct.elements)
    return result
}

// Array: double

fun toNativeDoubleArray(module: EmModule, arr: DoubleArray, releasable: Boolean): EmArray {
    val elements = module._malloc(arr.size * Double.SIZE_BYTES)
    Float64Array(module.HEAPF32.buffer, elements, arr.size)
        .set(createJsArray(arr.size) {
            arr[it].toJsNumber()
        })
    return createJsObject {
        this.elements = elements
        this.size = arr.size
        this.releasable = releasable
        this.released = false
    }
}

fun toKotlinDoubleArray(module: EmModule, struct: EmArray, dealloc: Boolean): DoubleArray {
    val result = JsArrayTools.from<JsNumber>(Float64Array(module.HEAPF32.buffer, struct.elements, struct.size))
        .run { DoubleArray(struct.size) {
            this[it]!!.toDouble()
        }}
    if(dealloc) module._free(struct.elements)
    return result
}

// Array: enum

fun <T: Enum<T>> toNativeEnumArray(module: EmModule, arr: Array<T>, releasable: Boolean): EmArray =
    toNativeIntArray(module, IntArray(arr.size) { arr[it].ordinal }, releasable)

inline fun <reified T: Enum<T>> toKotlinEnumArray(
    module: EmModule,
    struct: EmArray,
    dealloc: Boolean,
    noinline create: (Int) -> T
): Array<T> = toKotlinIntArray(module, struct, dealloc).run { Array(size, create) }

// Array: objects

fun <T> toNativeArray(
    module: EmModule,
    arr: Array<T>,
    converter: (T, Boolean) -> Int,
    releasable: Boolean
) = toNativeIntArray(module, IntArray(arr.size) { converter(arr[it], releasable) }, releasable)

@Suppress("unchecked_cast")
fun <T: Any> toKotlinArray(
    module: EmModule,
    struct: EmArray,
    converter: (Int, Boolean) -> T,
    dealloc: Boolean,
    deallocContent: Boolean
) = toKotlinIntArray(module, struct, dealloc).run { Array<Any>(size) { converter(this[it], deallocContent) } as Array<T> }


// Callbacks

fun mallocCallback(
    module: EmModule,
    callback: Any,
    invoke: Int,
    free: Int
): Int {
    val ptr = module._malloc(12)
    // 'm' is not used
    // module.HEAP32[ptr shr 2] = 0
    module.HEAP32[(ptr shr 2) + 1] = invoke
    module.HEAP32[(ptr shr 2) + 2] = free

    callbacks[Pair(module, ptr)] = callback
    return ptr
}

@Suppress("UNCHECKED_CAST")
fun <T> toKotlinCallback(
    module: EmModule,
    ptr: Int,
    dealloc: Boolean
): T {
    val result = callbacks[Pair(module, ptr)]
    if(dealloc)
        freeCallback(module, ptr)
    return result as T
}

fun freeCallback(
    module: EmModule,
    ptr: Int
) {
    callbacks.remove(Pair(module, ptr))
    module._free(ptr)
}

private fun freeCallbackJs(block: (Int) -> Unit): JsAny = js("block")

fun createCallbackFreeFunction(module: EmModule): Int =
    module.addFunction(freeCallbackJs { callback: Int ->
        freeCallback(module, callback)
    }, "vp")