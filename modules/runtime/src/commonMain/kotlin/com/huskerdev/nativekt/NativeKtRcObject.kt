package com.huskerdev.nativekt

abstract class NativeKtRcObject(
    val rcPtr: Long,
    freeFunc: (Long) -> Unit
): AutoCloseable {
    val objPtr by lazy { _address() }

    protected val releaser = AtomicHandleReleaser(rcPtr, freeFunc)

    override fun close() =
        releaser.release()

    @Suppress("FunctionName")
    protected abstract fun _address(): Long

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        return objPtr == (other as NativeKtRcObject).objPtr
    }

    override fun hashCode(): Int =
        objPtr.hashCode()

    override fun toString(): String =
        "${this::class.simpleName}(objPtr=$objPtr, rcPtr=$rcPtr)"
}