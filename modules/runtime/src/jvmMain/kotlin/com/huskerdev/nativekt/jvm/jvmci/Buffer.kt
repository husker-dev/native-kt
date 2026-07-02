package com.huskerdev.nativekt.jvm.jvmci;

import java.nio.ByteBuffer
import java.nio.ByteOrder

class Buffer {
    private var data = ByteBuffer.allocate(32).order(ByteOrder.nativeOrder())

    private fun ensureSize(length: Int) {
        if (length >= data.limit()) {
            val newData = ByteBuffer.wrap(data.array().copyOf(data.limit() * 2))
            newData.order(data.order())
            newData.position(data.position())
            data = newData
        }
    }

    fun emitByte(b: Int) {
        ensureSize(data.position() + 1)
        data.put((b and 0xFF).toByte())
    }

    fun emitInt(b: Int) {
        ensureSize(data.position() + 4)
        data.putInt(b)
    }

    fun emitLong(b: Long) {
        ensureSize(data.position() + 8)
        data.putLong(b)
    }

    fun position() =
        data.position()

    fun finish() =
        data.array().copyOf(data.position())
}