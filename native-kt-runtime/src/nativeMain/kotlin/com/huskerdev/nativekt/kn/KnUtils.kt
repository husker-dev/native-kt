@file:OptIn(ExperimentalForeignApi::class)

package com.huskerdev.nativekt.kn

import kotlinx.cinterop.*
import platform.posix.*

expect inline fun <reified T: CVariable> allocStruct(): CPointer<T>

fun String.allocCStr(): CPointer<ByteVar> =
     strdup(this)!!

fun CPointer<ByteVar>.unwrapCStr(dealloc: Boolean): String {
    val result = toKString()
    if(dealloc)
        free(this)
    return result
}

@Suppress("unchecked_cast")
fun <T: Any> unwrapCallback(callback: CPointer<CStructVar>?, dealloc: Boolean): T {
    val result = callback!!.pointed.memberAt<CPointerVar<*>>(0).value!!.asStableRef<Any>().get()
    if(dealloc)
        free(callback)
    return result as T
}

fun freeCallback(callback: CPointer<CStructVar>?) {
    callback!!.pointed.memberAt<CPointerVar<*>>(0).value!!.asStableRef<Any>().dispose()
    free(callback)
}

val freeCallbackFunction = staticCFunction(::freeCallback)