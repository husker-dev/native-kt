package com.huskerdev.nativekt.utils

import com.huskerdev.webidl.resolver.ResolvedIdlDictionary
import com.huskerdev.webidl.resolver.ResolvedIdlType
import kotlin.math.max

class CStructLayout(
    types: List<ResolvedIdlType>,
    x86: Boolean
) {
    var size: Int = 0
        private set

    var postPadding: Int = 0
        private set

    private val address = arrayListOf<Int>()
    private val padding = arrayListOf<Int>()

    constructor(
        dictionary: ResolvedIdlDictionary,
        x86: Boolean
    ): this(dictionary.allFields().map { it.type }, x86)

    init {
        var maxAlignment = 0
        types.forEach { type ->
            val alignment = type.getAlignment(x86)
            val typeSize = if(type.isString() || type.isArray()) {
                if (x86) 12 else 16
            } else alignment

            val rem = size % alignment
            val padding = if (rem == 0) 0 else alignment - rem

            maxAlignment = max(maxAlignment, alignment)

            this.padding += padding
            this.address += size + padding
            size += padding + typeSize
        }

        val rem = size % maxAlignment
        postPadding = if (rem == 0) 0 else maxAlignment - rem
        size += postPadding
    }

    fun addressOf(i: Int) = address[i]

    fun paddingOf(i: Int) = padding[i]
}