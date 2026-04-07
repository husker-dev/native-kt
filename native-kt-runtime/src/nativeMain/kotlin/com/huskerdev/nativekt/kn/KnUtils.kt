@file:OptIn(ExperimentalForeignApi::class)
@file:Suppress("unused")

package com.huskerdev.nativekt.kn

import kotlinx.cinterop.*
import platform.posix.*

expect fun mallocExact(size: UInt): COpaquePointer

expect inline fun <reified T: CVariable> allocStruct(): CPointer<T>


fun toKotlinString(of: CPointer<ByteVar>, dealloc: Boolean): String {
    val result = of.toKString()
    if(dealloc)
        free(of)
    return result
}

@Suppress("unchecked_cast")
fun <T: Any> toKotlinCallback(callback: CPointer<CStructVar>?, dealloc: Boolean): T {
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