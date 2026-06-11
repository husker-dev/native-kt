@file:OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
@file:Suppress("unused")

package com.huskerdev.nativekt.kn

import kotlinx.cinterop.*
import nativekt.internals.*
import platform.posix.*
import kotlin.enums.enumEntries
import kotlin.experimental.and

const val FLAG_RELEASABLE = 1.toByte()
const val FLAG_ON_STACK = 2.toByte()

// ╔════════════════╗
// ║     String     ║
// ╚════════════════╝

val _handleKStringFree = staticCFunction<COpaquePointer?, Unit> {
    KString_free(it!!.reinterpret())
}

fun MemScope.toNativeKStringOnArena(str: String, pin: Boolean = false): CPointer<KString> {
    val bytes = str.cstr
    val mem = alloc<KString>()
    mem.size = bytes.size.convert()
    mem.data = if(pin) {
        val pinObj = bytes.pin()
        defer { pinObj.unpin() }
        bytes.ptr
    } else bytes.getPointer(this)
    mem.length = str.length
    mem.__flags = 0
    return mem.ptr
}

fun toNativeKString(str: String): CPointer<KString> {
    val bytes = str.cstr
    val mem = malloc(sizeOf<KString>().convert())!!.reinterpret<KString>().pointed
    mem.size = bytes.size.convert()
    mem.data = malloc(mem.size.convert())!!.reinterpret()
    mem.length = str.length
    mem.__flags = FLAG_RELEASABLE
    memcpy(mem.data, bytes, mem.size)
    return mem.ptr
}

fun toKotlinKString(struct: CPointer<KString>): String =
    struct.pointed.data!!.toKString()

// ╔════════════════╗
// ║     Arrays     ║
// ╚════════════════╝

// Char

fun MemScope.toNativeKCharArrayOnArena(arr: CharArray, pin: Boolean): CPointer<KCharArray> {
    val mem = alloc<KCharArray>()
    mem.size = (arr.size * Char.SIZE_BYTES).convert()
    mem.elements = if(pin) {
        val pinObj = arr.pin()
        defer { pinObj.unpin() }
        pinObj.addressOf(0).reinterpret()
    } else allocArray<UShortVar>(arr.size)
    mem.length = arr.size
    mem.__flags = 0
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toNativeKCharArray(arr: CharArray): CPointer<KCharArray> {
    val mem = malloc(sizeOf<KCharArray>().convert())!!.reinterpret<KCharArray>().pointed
    mem.size = (arr.size * Char.SIZE_BYTES).convert()
    mem.elements = malloc(mem.size.convert())!!.reinterpret()
    mem.length = arr.size
    mem.__flags = FLAG_RELEASABLE
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toKotlinKCharArray(struct: CPointer<KCharArray>): CharArray =
    struct.pointed.run { CharArray(length) { elements!![it].toInt().toChar() } }

// Boolean

fun MemScope.toNativeKBooleanArrayOnArena(array: BooleanArray, pin: Boolean): CPointer<KBooleanArray> =
    toNativeKByteArrayOnArena(array.map { it.toByte() }.toByteArray(), pin).reinterpret()

fun toNativeKBooleanArray(array: BooleanArray): CPointer<KBooleanArray> =
    toNativeKByteArray(array.map { it.toByte() }.toByteArray()).reinterpret()

fun toKotlinKBooleanArray(struct: CPointer<KBooleanArray>): BooleanArray =
    struct.pointed.run { BooleanArray(length) { elements!![it].value } }

// Byte

fun MemScope.toNativeKByteArrayOnArena(arr: ByteArray, pin: Boolean): CPointer<KByteArray> {
    val mem = alloc<KByteArray>()
    mem.size = (arr.size * Byte.SIZE_BYTES).convert()
    mem.elements = if(pin) {
        val pinObj = arr.pin()
        defer { pinObj.unpin() }
        pinObj.addressOf(0).reinterpret()
    } else allocArray<ByteVar>(arr.size)
    mem.length = arr.size
    mem.__flags = 0
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toNativeKByteArray(arr: ByteArray): CPointer<KByteArray> {
    val mem = malloc(sizeOf<KByteArray>().convert())!!.reinterpret<KByteArray>().pointed
    mem.size = (arr.size * Byte.SIZE_BYTES).convert()
    mem.elements = malloc(mem.size.convert())!!.reinterpret()
    mem.length = arr.size
    mem.__flags = FLAG_RELEASABLE
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toKotlinKByteArray(struct: CPointer<KByteArray>): ByteArray =
    struct.pointed.run { ByteArray(length) { elements!![it] } }

// Short

fun MemScope.toNativeKShortArrayOnArena(arr: ShortArray, pin: Boolean): CPointer<KShortArray> {
    val mem = alloc<KShortArray>()
    mem.size = (arr.size * Short.SIZE_BYTES).convert()
    mem.elements = if(pin) {
        val pinObj = arr.pin()
        defer { pinObj.unpin() }
        pinObj.addressOf(0).reinterpret()
    } else allocArray<ShortVar>(arr.size)
    mem.length = arr.size
    mem.__flags = 0
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toNativeKShortArray(arr: ShortArray): CPointer<KShortArray> {
    val mem = malloc(sizeOf<KShortArray>().convert())!!.reinterpret<KShortArray>().pointed
    mem.size = (arr.size * Short.SIZE_BYTES).convert()
    mem.elements = malloc(mem.size.convert())!!.reinterpret()
    mem.length = arr.size
    mem.__flags = FLAG_RELEASABLE
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toKotlinKShortArray(struct: CPointer<KShortArray>): ShortArray =
    struct.pointed.run { ShortArray(length) { elements!![it] } }

// Int

fun MemScope.toNativeKIntArrayOnArena(arr: IntArray, pin: Boolean): CPointer<KIntArray> {
    val mem = alloc<KIntArray>()
    mem.size = (arr.size * Int.SIZE_BYTES).convert()
    mem.elements = if(pin) {
        val pinObj = arr.pin()
        defer { pinObj.unpin() }
        pinObj.addressOf(0).reinterpret()
    } else allocArray<IntVar>(arr.size)
    mem.length = arr.size
    mem.__flags = 0
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toNativeKIntArray(arr: IntArray): CPointer<KIntArray> {
    val mem = malloc(sizeOf<KIntArray>().convert())!!.reinterpret<KIntArray>().pointed
    mem.size = (arr.size * Int.SIZE_BYTES).convert()
    mem.elements = malloc(mem.size.convert())!!.reinterpret()
    mem.length = arr.size
    mem.__flags = FLAG_RELEASABLE
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toKotlinKIntArray(struct: CPointer<KIntArray>): IntArray =
    struct.pointed.run { IntArray(length) { elements!![it] } }

// Long

fun MemScope.toNativeKLongArrayOnArena(arr: LongArray, pin: Boolean): CPointer<KLongArray> {
    val mem = alloc<KLongArray>()
    mem.size = (arr.size * Long.SIZE_BYTES).convert()
    mem.elements = if(pin) {
        val pinObj = arr.pin()
        defer { pinObj.unpin() }
        pinObj.addressOf(0).reinterpret()
    } else allocArray<LongVar>(arr.size)
    mem.length = arr.size
    mem.__flags = 0
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toNativeKLongArray(arr: LongArray): CPointer<KLongArray> {
    val mem = malloc(sizeOf<KLongArray>().convert())!!.reinterpret<KLongArray>().pointed
    mem.size = (arr.size * Long.SIZE_BYTES).convert()
    mem.elements = malloc(mem.size.convert())!!.reinterpret()
    mem.length = arr.size
    mem.__flags = FLAG_RELEASABLE
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toKotlinKLongArray(struct: CPointer<KLongArray>): LongArray =
    struct.pointed.run { LongArray(length) { elements!![it] } }

// Float

fun MemScope.toNativeKFloatArrayOnArena(arr: FloatArray, pin: Boolean): CPointer<KFloatArray> {
    val mem = alloc<KFloatArray>()
    mem.size = (arr.size * Float.SIZE_BYTES).convert()
    mem.elements = if(pin) {
        val pinObj = arr.pin()
        defer { pinObj.unpin() }
        pinObj.addressOf(0).reinterpret()
    } else allocArray<FloatVar>(arr.size)
    mem.length = arr.size
    mem.__flags = 0
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toNativeKFloatArray(arr: FloatArray): CPointer<KFloatArray> {
    val mem = malloc(sizeOf<KFloatArray>().convert())!!.reinterpret<KFloatArray>().pointed
    mem.size = (arr.size * Float.SIZE_BYTES).convert()
    mem.elements = malloc(mem.size.convert())!!.reinterpret()
    mem.length = arr.size
    mem.__flags = FLAG_RELEASABLE
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toKotlinKFloatArray(struct: CPointer<KFloatArray>): FloatArray =
    struct.pointed.run { FloatArray(length) { elements!![it] } }

// Double

fun MemScope.toNativeKDoubleArrayOnArena(arr: DoubleArray, pin: Boolean): CPointer<KDoubleArray> {
    val mem = alloc<KDoubleArray>()
    mem.size = (arr.size * Double.SIZE_BYTES).convert()
    mem.elements = if(pin) {
        val pinObj = arr.pin()
        defer { pinObj.unpin() }
        pinObj.addressOf(0).reinterpret()
    } else allocArray<DoubleVar>(arr.size)
    mem.length = arr.size
    mem.__flags = 0
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toNativeKDoubleArray(arr: DoubleArray): CPointer<KDoubleArray> {
    val mem = malloc(sizeOf<KDoubleArray>().convert())!!.reinterpret<KDoubleArray>().pointed
    mem.size = (arr.size * Double.SIZE_BYTES).convert()
    mem.elements = malloc(mem.size.convert())!!.reinterpret()
    mem.length = arr.size
    mem.__flags = FLAG_RELEASABLE
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toKotlinKDoubleArray(struct: CPointer<KDoubleArray>): DoubleArray =
    struct.pointed.run { DoubleArray(length) { elements!![it] } }

// Enum

fun <T: Enum<T>> MemScope.toNativeEnumArrayOnArena(arr: Array<T>, pin: Boolean): CPointer<KIntArray> =
    toNativeKIntArrayOnArena(IntArray(arr.size) { arr[it].ordinal }, pin)

fun <T: Enum<T>> toNativeEnumArray(arr: Array<T>): CPointer<KIntArray> =
    toNativeKIntArray(IntArray(arr.size) { arr[it].ordinal })

inline fun <reified T: Enum<T>> toKotlinEnumArray(struct: CPointer<KIntArray>): Array<T> {
    val entries = enumEntries<T>()
    val ints = toKotlinKIntArray(struct)
    return Array(ints.size) { entries[ints[it]] }
}

// Object

fun <T: Any, N: CPointed> MemScope.toNativeKArrayOnArena(
    arr: Array<T>,
    converter: (T) -> CPointer<N>
): CPointer<KArray> {
    val mem = alloc<KArray>()
    mem.size = (arr.size * intptr_t.SIZE_BYTES).convert()
    mem.elements = allocArray<COpaquePointerVar>(arr.size)
    mem.length = arr.size
    mem.__flags = 0
    arr.forEachIndexed { i, it ->
        mem.elements!![i] = converter(it)
    }
    return mem.ptr
}

fun <T: Any, N: CPointed> toNativeKArray(
    arr: Array<T>,
    converter: (T) -> CPointer<N>
): CPointer<KArray> {
    val mem = malloc(sizeOf<KArray>().convert())!!.reinterpret<KArray>().pointed
    mem.size = (arr.size * intptr_t.SIZE_BYTES).convert()
    mem.elements = malloc(mem.size.convert())!!.reinterpret()
    mem.length = arr.size
    mem.__flags = FLAG_RELEASABLE
    arr.forEachIndexed { i, it ->
        mem.elements!![i] = converter(it)
    }
    return mem.ptr
}

inline fun <reified T: Any, N: CPointed> toKotlinKArray(
    struct: CPointer<KArray>,
    converter: (CPointer<N>) -> T
): Array<T> = struct.pointed.run { Array(length) { converter(elements!![it]!!.reinterpret()) } }


// ╔═══════════════════╗
// ║     Callbacks     ║
// ╚═══════════════════╝

private fun callbackClone(self: CPointer<_AbstractCallback>?): CPointer<_AbstractCallback>? {
    val mem = malloc(sizeOf<_AbstractCallback>().convert())!!.reinterpret<_AbstractCallback>().pointed
    mem.__flags = FLAG_RELEASABLE
    mem.invoke = self!!.pointed.invoke
    mem.clone = callbackClone
    mem.equals = callbackEquals
    mem.hashCode = callbackHashCode
    mem.free = callbackFree
    mem.__stableRef = StableRef.create(toKotlinCallback(self.reinterpret())).asCPointer()
    return mem.ptr
}

private fun callbackEquals(self: CPointer<_AbstractCallback>?, obj: CPointer<_AbstractCallback>?): Boolean =
    toKotlinCallback<Int>(self!!.reinterpret()) == toKotlinCallback<Int>(obj!!.reinterpret())

private fun callbackHashCode(self: CPointer<_AbstractCallback>?): Int =
    toKotlinCallback<Int>(self!!.reinterpret()).hashCode()

fun callbackFree(self: CPointer<_AbstractCallback>?) {
    val pointed = self!!.pointed
    if(pointed.__flags and FLAG_RELEASABLE != FLAG_RELEASABLE)
        return

    pointed.__stableRef!!.asStableRef<Any>().dispose()
    free(self)
}

val callbackClone = staticCFunction(::callbackClone)
val callbackEquals = staticCFunction(::callbackEquals)
val callbackHashCode = staticCFunction(::callbackHashCode)
val callbackFree = staticCFunction(::callbackFree)


fun MemScope.toNativeCallbackOnArena(
    of: Any,
    invoke: COpaquePointer
): COpaquePointer {
    val stableRef = StableRef.create(of)
    defer { stableRef.dispose() }

    val mem = alloc<_AbstractCallback>()
    mem.__flags = 0
    mem.invoke = invoke.reinterpret()
    mem.clone = callbackClone
    mem.equals = callbackEquals
    mem.hashCode = callbackHashCode
    mem.free = callbackFree
    mem.__stableRef = stableRef.asCPointer()
    return mem.ptr
}

fun toNativeCallback(
    of: Any,
    invoke: COpaquePointer
): COpaquePointer {
    val mem = malloc(sizeOf<_AbstractCallback>().convert())!!.reinterpret<_AbstractCallback>().pointed
    mem.__flags = FLAG_RELEASABLE
    mem.invoke = invoke.reinterpret()
    mem.clone = callbackClone
    mem.equals = callbackEquals
    mem.hashCode = callbackHashCode
    mem.free = callbackFree
    mem.__stableRef = StableRef.create(of).asCPointer()
    return mem.ptr
}

@Suppress("unchecked_cast")
fun <T: Any> toKotlinCallback(
    callback: COpaquePointer
): T = callback.reinterpret<_AbstractCallback>().pointed.__stableRef!!.asStableRef<Any>().get() as T
