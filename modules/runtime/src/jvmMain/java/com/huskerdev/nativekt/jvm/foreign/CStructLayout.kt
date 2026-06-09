package com.huskerdev.nativekt.jvm.foreign

import java.lang.foreign.ValueLayout
import kotlin.math.max

class CStructLayout(
    vararg types: ValueLayout
) {
    var size: Long = 0
        private set

    private val address = arrayListOf<Long>()

    init {
        var maxAlignment = 0
        types.forEach { type ->
            val typeSize = type.byteSize().toInt()

            val rem = (size % typeSize).toInt()
            val padding = if (rem == 0) 0 else typeSize - rem

            maxAlignment = max(maxAlignment, typeSize)

            this.address += size + padding
            size += padding + typeSize
        }

        val rem = (size % maxAlignment).toInt()
        val postPadding = if (rem == 0) 0 else maxAlignment - rem
        size += postPadding
    }

    operator fun get(i: Int) = address[i]

}