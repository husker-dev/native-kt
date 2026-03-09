@file:OptIn(ExperimentalForeignApi::class)

package com.huskerdev.nativekt.kn

import kotlinx.cinterop.*
import platform.posix.free

@Suppress("unused")
class NativeArena(
    val scope: MemScope
) {
    companion object {
        fun <T> use(block: (NativeArena) -> T) = memScoped {
            NativeArena(this).run {
                block(this).also { free() }
            }
        }
    }
    private val allocated = hashSetOf<Long>()
    private val callbacks = arrayListOf<CPointer<CStructVar>>()
    private val pinned = arrayListOf<Pinned<*>>()

    fun <T: Any> pin(obj: T): Pinned<T> =
         obj.pin().also { pinned += it }

    fun freeMem(ptr: CPointer<*>) {
        if(ptr.rawValue.toLong() !in allocated)
            free(ptr)
    }

    fun ptr(ptr: CPointer<*>){
        allocated += ptr.getPointer(scope).rawValue.toLong()
    }

    fun unwrapCStr(mem: CPointer<ByteVar>, dealloc: Boolean): String {
        val result = mem.toKString()
        if(dealloc && mem.rawValue.toLong() !in allocated)
            free(mem)
        return result
    }

    fun <T: Any> unwrapCallback(callback: CPointer<CStructVar>?, dealloc: Boolean): T {
        val result = com.huskerdev.nativekt.kn.unwrapCallback<T>(callback, dealloc)
        if(dealloc && callback!!.rawValue.toLong() !in allocated)
            freeCallback(callback)
        return result
    }

    @Suppress("unchecked_cast")
    fun <T: CStructVar> callback(callback: CPointer<T>): CPointer<T> {
        callbacks += callback as CPointer<CStructVar>
        return callback
    }

    private fun free() {
        callbacks.forEach(::freeCallback)
        pinned.forEach(Pinned<*>::unpin)
    }
}