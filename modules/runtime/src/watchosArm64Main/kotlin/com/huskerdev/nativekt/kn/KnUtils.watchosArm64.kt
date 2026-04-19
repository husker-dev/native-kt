@file:OptIn(ExperimentalForeignApi::class)
@file:Suppress("unused")

package com.huskerdev.nativekt.kn

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import platform.posix.malloc

actual fun mallocExact(size: UInt): COpaquePointer =
    malloc(size)!!

actual inline fun <reified T : kotlinx.cinterop.CVariable> allocStruct() =
    malloc(sizeOf<T>().toUInt())!!.reinterpret<T>()