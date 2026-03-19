@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("unused")

package com.huskerdev.nativekt.web

import kotlin.js.*
import kotlin.math.truncate

private val callbacks = hashMapOf<Pair<EmModule, Int>, Any>()

fun Float.truncF32(): Float {
    val factor = 10_000_000
    return truncate(this * factor) / factor
}

fun <T: JsAny> loadLib(lib: JsAny): Promise<T> =
    js("'__esModule' in lib ? lib.default() : lib()")

fun Boolean.toInt() = if(this) 1 else 0

fun allocCStr(module: EmModule, str: String): EmString {
    val len = module.lengthBytesUTF8(str) + 1
    val strMem = module._malloc(len)
    module.stringToUTF8(str, strMem, len)

    return createJsObject {
        data = strMem
        length = str.length
    }
}

fun unwrapCStr(module: EmModule, struct: EmString, dealloc: Boolean): String {
    val result = module.UTF8ToString(struct.data, struct.length)
    if(dealloc)
        module._free(struct.data)
    return result
}

// Array: char

fun toNativeCharArray(module: EmModule, arr: CharArray): EmArray {
    val elements = module._malloc(arr.size * Char.SIZE_BYTES)
    Int16Array(module.HEAP8.buffer, elements, arr.size)
        .set(arr.map { it.code.toJsNumber() }.toJsArray())
    return createJsObject {
        this.elements = elements
        this.size = arr.size
    }
}

fun toKotlinCharArray(module: EmModule, struct: EmArray, dealloc: Boolean): CharArray {
    val result = JsArrayTools.from<JsNumber>(Int16Array(module.HEAP8.buffer, struct.elements, struct.size))
        .toArray()
        .map { it.toInt().toChar() }.toCharArray()
    if(dealloc) module._free(struct.elements)
    return result
}

// Array: boolean

fun toNativeBooleanArray(module: EmModule, arr: BooleanArray): EmArray {
    val elements = module._malloc(arr.size * Byte.SIZE_BYTES)
    Int8Array(module.HEAP8.buffer, elements, arr.size)
        .set(arr.map { (if(it) 1 else 0).toJsNumber() }.toJsArray())
    return createJsObject {
        this.elements = elements
        this.size = arr.size
    }
}

fun toKotlinBooleanArray(module: EmModule, struct: EmArray, dealloc: Boolean): BooleanArray {
    val result = JsArrayTools.from<JsNumber>(Int8Array(module.HEAP8.buffer, struct.elements, struct.size))
        .toArray()
        .map { it.toInt() == 1 }.toBooleanArray()
    if(dealloc) module._free(struct.elements)
    return result
}

// Array: byte

fun toNativeByteArray(module: EmModule, arr: ByteArray): EmArray {
    val elements = module._malloc(arr.size * Byte.SIZE_BYTES)
    Int8Array(module.HEAP8.buffer, elements, arr.size)
        .set(arr.map { it.toInt().toJsNumber() }.toJsArray())
    return createJsObject {
        this.elements = elements
        this.size = arr.size
    }
}

fun toKotlinByteArray(module: EmModule, struct: EmArray, dealloc: Boolean): ByteArray {
    val result = JsArrayTools.from<JsNumber>(Int8Array(module.HEAP8.buffer, struct.elements, struct.size))
        .toArray()
        .map { it.toInt().toByte() }.toByteArray()
    if(dealloc) module._free(struct.elements)
    return result
}

// Array: short

fun toNativeShortArray(module: EmModule, arr: ShortArray): EmArray {
    val elements = module._malloc(arr.size * Short.SIZE_BYTES)
    Int16Array(module.HEAP8.buffer, elements, arr.size)
        .set(arr.map { it.toInt().toJsNumber() }.toJsArray())
    return createJsObject {
        this.elements = elements
        this.size = arr.size
    }
}

fun toKotlinShortArray(module: EmModule, struct: EmArray, dealloc: Boolean): ShortArray {
    val result = JsArrayTools.from<JsNumber>(Int16Array(module.HEAP8.buffer, struct.elements, struct.size))
        .toArray()
        .map { it.toInt().toShort() }.toShortArray()
    if(dealloc) module._free(struct.elements)
    return result
}

// Array: int

fun toNativeIntArray(module: EmModule, arr: IntArray): EmArray {
    val elements = module._malloc(arr.size * Int.SIZE_BYTES)
    Int32Array(module.HEAP8.buffer, elements, arr.size)
        .set(arr.map { it.toJsNumber() }.toJsArray())
    return createJsObject {
        this.elements = elements
        this.size = arr.size
    }
}

fun toKotlinIntArray(module: EmModule, struct: EmArray, dealloc: Boolean): IntArray {
    val result = JsArrayTools.from<JsNumber>(Int32Array(module.HEAP8.buffer, struct.elements, struct.size))
        .toArray()
        .map { it.toInt() }.toIntArray()
    if(dealloc) module._free(struct.elements)
    return result
}

// Array: long

fun toNativeLongArray(module: EmModule, arr: LongArray): EmArray {
    val elements = module._malloc(arr.size * Long.SIZE_BYTES)
    BigInt64Array(module.HEAP8.buffer, elements, arr.size)
        .set(arr.map { it.toJsBigInt() }.toJsArray())
    return createJsObject {
        this.elements = elements
        this.size = arr.size
    }
}

fun toKotlinLongArray(module: EmModule, struct: EmArray, dealloc: Boolean): LongArray {
    val result = JsArrayTools.from<JsBigInt>(BigInt64Array(module.HEAP8.buffer, struct.elements, struct.size))
        .toArray()
        .map { it.toLong() }.toLongArray()
    if(dealloc) module._free(struct.elements)
    return result
}

// Array: float

fun toNativeFloatArray(module: EmModule, arr: FloatArray): EmArray {
    val elements = module._malloc(arr.size * Float.SIZE_BYTES)
    Float32Array(module.HEAPF32.buffer, elements, arr.size)
        .set(arr.map { it.toDouble().toJsNumber() }.toJsArray())
    return createJsObject {
        this.elements = elements
        this.size = arr.size
    }
}

fun toKotlinFloatArray(module: EmModule, struct: EmArray, dealloc: Boolean): FloatArray {
    val result = JsArrayTools.from<JsNumber>(Float32Array(module.HEAPF32.buffer, struct.elements, struct.size))
        .toArray()
    if(dealloc) module._free(struct.elements)
    return FloatArray(struct.size) {
        result[it].toDouble().toFloat().truncF32()
    }
}

// Array: double

fun toNativeDoubleArray(module: EmModule, arr: DoubleArray): EmArray {
    val elements = module._malloc(arr.size * Double.SIZE_BYTES)
    Float64Array(module.HEAPF32.buffer, elements, arr.size)
        .set(arr.map { it.toJsNumber() }.toJsArray())
    return createJsObject {
        this.elements = elements
        this.size = arr.size
    }
}

fun toKotlinDoubleArray(module: EmModule, struct: EmArray, dealloc: Boolean): DoubleArray {
    val result = JsArrayTools.from<JsNumber>(Float64Array(module.HEAPF32.buffer, struct.elements, struct.size))
        .toArray().map { it.toDouble() }.toDoubleArray()
    if(dealloc) module._free(struct.elements)
    return result
}


// Callbacks

fun mallocCallback(
    module: JsAny,
    callback: Any,
    invoke: Int,
    free: Int
): Int {
    val module = module.unsafeCast<EmModule>()

    val ptr = module._malloc(12)
    // 'm' is not used
    // module.HEAP32[ptr shr 2] = 0
    module.HEAP32[(ptr shr 2) + 1] = invoke
    module.HEAP32[(ptr shr 2) + 2] = free

    callbacks[Pair(module, ptr)] = callback
    return ptr
}

@Suppress("unchecked_cast")
fun <T> unwrapCallback(module: JsAny, ptr: Int, dealloc: Boolean): T {
    val module = module.unsafeCast<EmModule>()
    val result = callbacks[Pair(module, ptr)]
    if(dealloc)
        freeCallback(module, ptr)
    return result as T
}

fun freeCallback(module: JsAny, ptr: Int) {
    val module = module.unsafeCast<EmModule>()
    callbacks.remove(Pair(module, ptr))
    module._free(ptr)
}

private fun freeCallbackJs(block: (Int) -> Unit): JsAny = js("block")

fun createCallbackFreeFunction(module: EmModule): Int =
    module.addFunction(freeCallbackJs { callback: Int ->
        freeCallback(module, callback)
    }, "vp")