@file:OptIn(ExperimentalForeignApi::class, UnsafeNumber::class, ExperimentalContracts::class, ExperimentalExtendedContracts::class)
@file:Suppress("unused")

package com.huskerdev.nativekt.kn

import kotlinx.cinterop.*
import nativekt.internals.*
import platform.posix.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.ExperimentalExtendedContracts
import kotlin.contracts.contract
import kotlin.enums.enumEntries
import kotlin.experimental.and

const val FLAG_RELEASABLE = 1.toByte()
const val FLAG_ON_STACK = 2.toByte()

// ╔════════════════╗
// ║     String     ║
// ╚════════════════╝

val _handleKStringFree = staticCFunction<COpaquePointer?, Unit> {
    if(it == null) return@staticCFunction
    KString_free(it.reinterpret())
}

fun MemScope.toNativeKStringOnArena(str: String?, pin: Boolean = false): CPointer<KString>? {
    contract {
        (str != null).implies(returnsNotNull())
    }
    if(str == null) return null
    val bytes = str.encodeToByteArray()
    val mem = alloc<KString>()
    mem.size = bytes.size.convert()
    mem.data = if(pin) {
        val pinObj = bytes.pin()
        defer { pinObj.unpin() }
        pinObj.addressOf(0)
    } else {
        val allocated = interpretCPointer<ByteVar>(alloc(bytes.size, 1).rawPtr)
        bytes.usePinned { memcpy(allocated, it.addressOf(0), mem.size) }
        allocated
    }
    mem.length = str.length
    mem.__flags = 0
    return mem.ptr
}

fun toNativeKString(str: String?): CPointer<KString>? {
    contract {
        (str != null).implies(returnsNotNull())
    }
    if(str == null) return null
    val bytes = str.encodeToByteArray()
    val mem = malloc(sizeOf<KString>().convert())!!.reinterpret<KString>().pointed
    mem.size = bytes.size.convert()
    mem.data = malloc(mem.size.convert())!!.reinterpret()
    mem.length = str.length
    mem.__flags = FLAG_RELEASABLE
    bytes.usePinned { memcpy(mem.data, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toKotlinKString(struct: CPointer<KString>?): String? {
    contract {
        (struct != null).implies(returnsNotNull())
    }
    if(struct == null) return null
    val mem = struct.pointed
    return mem.data!!.readBytes(mem.size.toInt()).decodeToString()
}

// ╔════════════════╗
// ║     Arrays     ║
// ╚════════════════╝

// Char

fun MemScope.toNativeKCharArrayOnArena(arr: CharArray?, pin: Boolean): CPointer<KCharArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    val mem = alloc<KCharArray>()
    mem.size = (arr.size * Char.SIZE_BYTES).convert()
    mem.elements = if(pin) {
        val pinObj = arr.pin()
        defer { pinObj.unpin() }
        pinObj.addressOf(0).reinterpret()
    } else {
        val allocated = allocArray<UShortVar>(arr.size)
        arr.usePinned { memcpy(allocated, it.addressOf(0), mem.size) }
        allocated
    }
    mem.length = arr.size
    mem.__flags = 0
    return mem.ptr
}

fun toNativeKCharArray(arr: CharArray?): CPointer<KCharArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    val mem = malloc(sizeOf<KCharArray>().convert())!!.reinterpret<KCharArray>().pointed
    mem.size = (arr.size * Char.SIZE_BYTES).convert()
    mem.elements = malloc(mem.size.convert())!!.reinterpret()
    mem.length = arr.size
    mem.__flags = FLAG_RELEASABLE
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toKotlinKCharArray(struct: CPointer<KCharArray>?): CharArray? {
    contract {
        (struct != null).implies(returnsNotNull())
    }
    if(struct == null) return null
    return struct.pointed.run { CharArray(length) { elements!![it].toInt().toChar() } }
}

// Boolean

fun MemScope.toNativeKBooleanArrayOnArena(arr: BooleanArray?, pin: Boolean): CPointer<KBooleanArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    return toNativeKByteArrayOnArena(ByteArray(arr.size) { arr[it].toByte() }, pin).reinterpret()
}

fun toNativeKBooleanArray(arr: BooleanArray?): CPointer<KBooleanArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    return toNativeKByteArray(ByteArray(arr.size) { arr[it].toByte() }).reinterpret()
}

fun toKotlinKBooleanArray(struct: CPointer<KBooleanArray>?): BooleanArray? {
    contract {
        (struct != null).implies(returnsNotNull())
    }
    if(struct == null) return null
    return struct.pointed.run { BooleanArray(length) { elements!![it].value } }
}

// Byte

fun MemScope.toNativeKByteArrayOnArena(arr: ByteArray?, pin: Boolean): CPointer<KByteArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    val mem = alloc<KByteArray>()
    mem.size = (arr.size * Byte.SIZE_BYTES).convert()
    mem.elements = if(pin) {
        val pinObj = arr.pin()
        defer { pinObj.unpin() }
        pinObj.addressOf(0).reinterpret()
    } else {
        val allocated = allocArray<ByteVar>(arr.size)
        arr.usePinned { memcpy(allocated, it.addressOf(0), mem.size) }
        allocated
    }
    mem.length = arr.size
    mem.__flags = 0
    return mem.ptr
}

fun toNativeKByteArray(arr: ByteArray?): CPointer<KByteArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    val mem = malloc(sizeOf<KByteArray>().convert())!!.reinterpret<KByteArray>().pointed
    mem.size = (arr.size * Byte.SIZE_BYTES).convert()
    mem.elements = malloc(mem.size.convert())!!.reinterpret()
    mem.length = arr.size
    mem.__flags = FLAG_RELEASABLE
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toKotlinKByteArray(struct: CPointer<KByteArray>?): ByteArray? {
    contract {
        (struct != null).implies(returnsNotNull())
    }
    if(struct == null) return null
    return struct.pointed.run { ByteArray(length) { elements!![it] } }
}

// Short

fun MemScope.toNativeKShortArrayOnArena(arr: ShortArray?, pin: Boolean): CPointer<KShortArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    val mem = alloc<KShortArray>()
    mem.size = (arr.size * Short.SIZE_BYTES).convert()
    mem.elements = if(pin) {
        val pinObj = arr.pin()
        defer { pinObj.unpin() }
        pinObj.addressOf(0).reinterpret()
    } else {
        val allocated = allocArray<ShortVar>(arr.size)
        arr.usePinned { memcpy(allocated, it.addressOf(0), mem.size) }
        allocated
    }
    mem.length = arr.size
    mem.__flags = 0
    return mem.ptr
}

fun toNativeKShortArray(arr: ShortArray?): CPointer<KShortArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    val mem = malloc(sizeOf<KShortArray>().convert())!!.reinterpret<KShortArray>().pointed
    mem.size = (arr.size * Short.SIZE_BYTES).convert()
    mem.elements = malloc(mem.size.convert())!!.reinterpret()
    mem.length = arr.size
    mem.__flags = FLAG_RELEASABLE
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toKotlinKShortArray(struct: CPointer<KShortArray>?): ShortArray? {
    contract {
        (struct != null).implies(returnsNotNull())
    }
    if(struct == null) return null
    return struct.pointed.run { ShortArray(length) { elements!![it] } }
}

// Int

fun MemScope.toNativeKIntArrayOnArena(arr: IntArray?, pin: Boolean): CPointer<KIntArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    val mem = alloc<KIntArray>()
    mem.size = (arr.size * Int.SIZE_BYTES).convert()
    mem.elements = if(pin) {
        val pinObj = arr.pin()
        defer { pinObj.unpin() }
        pinObj.addressOf(0).reinterpret()
    } else {
        val allocated = allocArray<IntVar>(arr.size)
        arr.usePinned { memcpy(allocated, it.addressOf(0), mem.size) }
        allocated
    }
    mem.length = arr.size
    mem.__flags = 0
    return mem.ptr
}

fun toNativeKIntArray(arr: IntArray?): CPointer<KIntArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    val mem = malloc(sizeOf<KIntArray>().convert())!!.reinterpret<KIntArray>().pointed
    mem.size = (arr.size * Int.SIZE_BYTES).convert()
    mem.elements = malloc(mem.size.convert())!!.reinterpret()
    mem.length = arr.size
    mem.__flags = FLAG_RELEASABLE
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toKotlinKIntArray(struct: CPointer<KIntArray>?): IntArray? {
    contract {
        (struct != null).implies(returnsNotNull())
    }
    if(struct == null) return null
    return struct.pointed.run { IntArray(length) { elements!![it] } }
}

// Long

fun MemScope.toNativeKLongArrayOnArena(arr: LongArray?, pin: Boolean): CPointer<KLongArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    val mem = alloc<KLongArray>()
    mem.size = (arr.size * Long.SIZE_BYTES).convert()
    mem.elements = if(pin) {
        val pinObj = arr.pin()
        defer { pinObj.unpin() }
        pinObj.addressOf(0).reinterpret()
    } else {
        val allocated = allocArray<LongVar>(arr.size)
        arr.usePinned { memcpy(allocated, it.addressOf(0), mem.size) }
        allocated
    }
    mem.length = arr.size
    mem.__flags = 0
    return mem.ptr
}

fun toNativeKLongArray(arr: LongArray?): CPointer<KLongArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    val mem = malloc(sizeOf<KLongArray>().convert())!!.reinterpret<KLongArray>().pointed
    mem.size = (arr.size * Long.SIZE_BYTES).convert()
    mem.elements = malloc(mem.size.convert())!!.reinterpret()
    mem.length = arr.size
    mem.__flags = FLAG_RELEASABLE
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toKotlinKLongArray(struct: CPointer<KLongArray>?): LongArray? {
    contract {
        (struct != null).implies(returnsNotNull())
    }
    if(struct == null) return null
    return struct.pointed.run { LongArray(length) { elements!![it] } }
}

// Float

fun MemScope.toNativeKFloatArrayOnArena(arr: FloatArray?, pin: Boolean): CPointer<KFloatArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    val mem = alloc<KFloatArray>()
    mem.size = (arr.size * Float.SIZE_BYTES).convert()
    mem.elements = if(pin) {
        val pinObj = arr.pin()
        defer { pinObj.unpin() }
        pinObj.addressOf(0).reinterpret()
    } else {
        val allocated = allocArray<FloatVar>(arr.size)
        arr.usePinned { memcpy(allocated, it.addressOf(0), mem.size) }
        allocated
    }
    mem.length = arr.size
    mem.__flags = 0
    return mem.ptr
}

fun toNativeKFloatArray(arr: FloatArray?): CPointer<KFloatArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    val mem = malloc(sizeOf<KFloatArray>().convert())!!.reinterpret<KFloatArray>().pointed
    mem.size = (arr.size * Float.SIZE_BYTES).convert()
    mem.elements = malloc(mem.size.convert())!!.reinterpret()
    mem.length = arr.size
    mem.__flags = FLAG_RELEASABLE
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toKotlinKFloatArray(struct: CPointer<KFloatArray>?): FloatArray? {
    contract {
        (struct != null).implies(returnsNotNull())
    }
    if(struct == null) return null
    return struct.pointed.run { FloatArray(length) { elements!![it] } }
}

// Double

fun MemScope.toNativeKDoubleArrayOnArena(arr: DoubleArray?, pin: Boolean): CPointer<KDoubleArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    val mem = alloc<KDoubleArray>()
    mem.size = (arr.size * Double.SIZE_BYTES).convert()
    mem.elements = if(pin) {
        val pinObj = arr.pin()
        defer { pinObj.unpin() }
        pinObj.addressOf(0).reinterpret()
    } else {
        val allocated = allocArray<DoubleVar>(arr.size)
        arr.usePinned { memcpy(allocated, it.addressOf(0), mem.size) }
        allocated
    }
    mem.length = arr.size
    mem.__flags = 0
    return mem.ptr
}

fun toNativeKDoubleArray(arr: DoubleArray?): CPointer<KDoubleArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    val mem = malloc(sizeOf<KDoubleArray>().convert())!!.reinterpret<KDoubleArray>().pointed
    mem.size = (arr.size * Double.SIZE_BYTES).convert()
    mem.elements = malloc(mem.size.convert())!!.reinterpret()
    mem.length = arr.size
    mem.__flags = FLAG_RELEASABLE
    arr.usePinned { memcpy(mem.elements, it.addressOf(0), mem.size) }
    return mem.ptr
}

fun toKotlinKDoubleArray(struct: CPointer<KDoubleArray>?): DoubleArray? {
    contract {
        (struct != null).implies(returnsNotNull())
    }
    if(struct == null) return null
    return struct.pointed.run { DoubleArray(length) { elements!![it] } }
}

// Enum

fun <T: Enum<T>> MemScope.toNativeEnumArrayOnArena(arr: Array<T>?, pin: Boolean): CPointer<KIntArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    return toNativeKIntArrayOnArena(IntArray(arr.size) { arr[it].ordinal }, pin)
}

fun <T: Enum<T>> toNativeEnumArray(arr: Array<T>?): CPointer<KIntArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
    return toNativeKIntArray(IntArray(arr.size) { arr[it].ordinal })
}

inline fun <reified T: Enum<T>> toKotlinEnumArray(struct: CPointer<KIntArray>?): Array<T>? {
    contract {
        (struct != null).implies(returnsNotNull())
    }
    if(struct == null) return null
    val entries = enumEntries<T>()
    val ints = toKotlinKIntArray(struct)
    return Array(ints.size) { entries[ints[it]] }
}

// Object

fun <T, N: CPointed> MemScope.toNativeKArrayOnArena(
    arr: Array<T>?,
    converter: (T) -> CPointer<N>?
): CPointer<KArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
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

fun <T, N: CPointed> toNativeKArray(
    arr: Array<T>?,
    converter: (T) -> CPointer<N>?
): CPointer<KArray>? {
    contract {
        (arr != null).implies(returnsNotNull())
    }
    if(arr == null) return null
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

@Suppress("unchecked_cast")
fun <T, N: CPointer<*>> toKotlinKArray(
    struct: CPointer<KArray>?,
    converter: (N?) -> T?
): Array<T?>? {
    contract {
        (struct != null).implies(returnsNotNull())
    }
    if(struct == null) return null
    val arr = struct.pointed
    return Array<Any?>(arr.length) {
        (arr.elements!![it] as N?)?.run { converter(this) }
    } as Array<T?>?
}

@Suppress("unchecked_cast")
fun <T: Any, N: CPointer<*>> toKotlinKArray(
    struct: CPointer<KArray>,
    converter: (N?) -> T?
): Array<T> = toKotlinKArray<T?, N>(struct, converter) as Array<T>

// ╔═══════════════════╗
// ║     Callbacks     ║
// ╚═══════════════════╝

private fun callbackClone(self: CPointer<_AbstractCallback>?): CPointer<_AbstractCallback>? {
    contract {
        (self != null).implies(returnsNotNull())
    }
    if(self == null) return null
    val mem = malloc(sizeOf<_AbstractCallback>().convert())!!.reinterpret<_AbstractCallback>().pointed
    mem.__flags = FLAG_RELEASABLE
    mem.invoke = self.pointed.invoke
    mem.clone = callbackClone
    mem.equals = callbackEquals
    mem.hashCode = callbackHashCode
    mem.free = callbackFree
    mem.__stableRef = StableRef.create(toKotlinCallback(self)).asCPointer()
    return mem.ptr
}

private fun callbackEquals(self: CPointer<_AbstractCallback>?, obj: CPointer<_AbstractCallback>?): Boolean =
    toKotlinCallback<Int>(self!!.reinterpret()) == toKotlinCallback<Int>(obj?.reinterpret())

private fun callbackHashCode(self: CPointer<_AbstractCallback>?): Int =
    toKotlinCallback<Int>(self!!.reinterpret()).hashCode()

fun callbackFree(self: CPointer<_AbstractCallback>?) {
    if(self == null) return

    if(self.pointed.__flags and FLAG_RELEASABLE != FLAG_RELEASABLE)
        return

    self.pointed.__stableRef!!.asStableRef<Any>().dispose()
    free(self)
}

val callbackClone = staticCFunction(::callbackClone)
val callbackEquals = staticCFunction(::callbackEquals)
val callbackHashCode = staticCFunction(::callbackHashCode)
val callbackFree = staticCFunction(::callbackFree)


fun MemScope.toNativeCallbackOnArena(
    of: Any?,
    invoke: COpaquePointer
): COpaquePointer? {
    contract {
        (of != null).implies(returnsNotNull())
    }
    if(of == null) return null
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
    of: Any?,
    invoke: COpaquePointer
): COpaquePointer? {
    contract {
        (of != null).implies(returnsNotNull())
    }
    if(of == null) return null
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
fun <T> toKotlinCallback(callback: COpaquePointer?): T? {
    contract {
        (callback != null).implies(returnsNotNull())
    }
    if(callback == null) return null
    return callback.reinterpret<_AbstractCallback>().pointed.__stableRef!!.asStableRef<Any>().get() as T?
}

fun <T: Any> toKotlinCallback(callback: COpaquePointer): T =
    toKotlinCallback<T?>(callback)
