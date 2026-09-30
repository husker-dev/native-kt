package com.huskerdev.nativekt

import kotlin.enums.enumEntries

fun Array<UByte?>.asByteArray() = Array(size) { this[it]?.toByte() }
fun Array<UShort?>.asShortArray() = Array(size) { this[it]?.toShort() }
fun Array<UInt?>.asIntArray() = Array(size) { this[it]?.toInt() }
fun Array<ULong?>.asLongArray() = Array(size) { this[it]?.toLong() }

fun Array<Byte?>.asUByteArray() = Array(size) { this[it]?.toUByte() }
fun Array<Short?>.asUShortArray() = Array(size) { this[it]?.toUShort() }
fun Array<Int?>.asUIntArray() = Array(size) { this[it]?.toUInt() }
fun Array<Long?>.asULongArray() = Array(size) { this[it]?.toULong() }

fun <T: Enum<T>> enumToInts(arr: Array<T>?): IntArray? {
    if(arr == null)
        return null
    return IntArray(arr.size) { arr[it].ordinal }
}

fun <T: Enum<T>> enumToInts(arr: Array<T?>?): Array<Int?>? {
    if(arr == null)
        return null
    return Array(arr.size) { arr[it]?.ordinal }
}

inline fun <reified T: Enum<T>> intsToEnum(ints: IntArray?): Array<T>? {
    if(ints == null)
        return null
    val entries = enumEntries<T>()
    return Array(ints.size) { entries[ints[it]] }
}

inline fun <reified T: Enum<T>> intsToEnum(ints: Array<Int?>?): Array<T?>? {
    if(ints == null)
        return null
    val entries = enumEntries<T>()
    return Array(ints.size) { i -> ints[i]?.let { entries[it] } }
}