package com.huskerdev.nativekt.web

import kotlin.math.max
import kotlin.reflect.KClass

class CStructLayout(
    vararg types: KClass<*>
) {
    var size: Int = 0
        private set

    private val address = arrayListOf<Int>()

    init {
        var maxAlignment = 0
        types.forEach { type ->
            val typeSize = when(type) {
                Ptr::class -> 4
                Int::class -> 4
                Long::class -> 8
                Float::class -> 4
                Double::class -> 8
                Char::class -> 2
                Short::class -> 2
                Byte::class -> 1
                Boolean::class -> 1
                else -> throw UnsupportedOperationException(type.toString())
            }

            val rem = size % typeSize
            val padding = if (rem == 0) 0 else typeSize - rem

            maxAlignment = max(maxAlignment, typeSize)

            this.address += size + padding
            size += padding + typeSize
        }

        val rem = size % maxAlignment
        val postPadding = if (rem == 0) 0 else maxAlignment - rem
        size += postPadding
    }

    operator fun get(i: Int) = address[i]

    class Ptr
}