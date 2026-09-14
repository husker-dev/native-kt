package com.huskerdev.nativekt

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class AtomicHandleReleaser<T>(
    val handle: T,
    private val releaseAction: (T) -> Unit
) {
    private val released = AtomicBoolean(false)

    fun release() {
        if (released.compareAndSet(expectedValue = false, newValue = true))
            releaseAction(handle)
    }
}