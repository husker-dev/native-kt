@file: OptIn(ExperimentalForeignApi::class)

package com.huskerdev.nativekt.kn

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import platform.posix.malloc

@Suppress("unused")
actual fun malloc(size: UInt): COpaquePointer =
    malloc(size.toULong())!!

actual inline fun <reified T : kotlinx.cinterop.CVariable> allocStruct() =
    malloc(sizeOf<T>().toULong())!!.reinterpret<T>()