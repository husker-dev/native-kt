@file:OptIn(ExperimentalWasmJsInterop::class)
@file:Suppress("unused")

package com.huskerdev.nativekt.web

import kotlin.js.*

class Arena(
    val module: EmModule
) {
    companion object {
        inline fun <T> use(module: EmModule, block: Arena.() -> T): T {
            val arena = Arena(module)
            try {
                return block(arena)
            } finally {
                arena.close()
            }
        }
    }

    private val allocated = arrayListOf<Int>()
    private val onCloseActions = arrayListOf<() -> Unit>()

    fun alloc(size: Int): Int {
        val addr = module._malloc(size)
        allocated += addr
        return addr
    }

    fun defer(action: () -> Unit) {
        onCloseActions += action
    }

    fun close() {
        allocated.forEach {
            module._free(it)
        }
        onCloseActions.forEach { it() }
    }
}