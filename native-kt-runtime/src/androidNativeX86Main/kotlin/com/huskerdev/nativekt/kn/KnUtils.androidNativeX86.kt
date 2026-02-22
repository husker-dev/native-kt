package com.huskerdev.nativekt.kn

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import platform.posix.malloc

@OptIn(ExperimentalForeignApi::class)
actual inline fun <reified T : kotlinx.cinterop.CVariable> allocStruct() =
    malloc(sizeOf<T>().toUInt())!!.reinterpret<T>()