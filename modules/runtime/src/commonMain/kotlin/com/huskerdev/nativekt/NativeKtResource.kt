package com.huskerdev.nativekt

abstract class NativeKtResource: AutoCloseable {
    private var alive = true

    override fun close() {
        if(!alive)
            return
        alive = false
        _close()
    }

    protected abstract fun _close()
}