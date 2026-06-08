package com.huskerdev.nativekt.utils

import com.huskerdev.webidl.resolver.BuiltinIdlDeclaration
import com.huskerdev.webidl.resolver.ResolvedIdlDictionary
import com.huskerdev.webidl.resolver.ResolvedIdlType
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind
import kotlin.math.max

class CStructLayout(
    types: List<ResolvedIdlType>,
    x86: Boolean
) {
    var count: Int = types.size

    var size: Int = 0
        private set

    var postPadding: Int = 0
        private set

    private val address = arrayListOf<Int>()
    private val padding = arrayListOf<Int>()

    constructor(
        dictionary: ResolvedIdlDictionary,
        x86: Boolean
    ): this(
        types = dictionary.allFields()
            .map { it.type }
            .toMutableList()
            .apply {
                add(ResolvedIdlType.Default(BuiltinIdlDeclaration("int", WebIDLBuiltinKind.INT), emptyList(), false))
            },
        x86
    )

    init {
        var maxAlignment = 0
        types.forEach { type ->
            val typeSize = type.getAlignment(x86)

            val rem = size % typeSize
            val padding = if (rem == 0) 0 else typeSize - rem

            maxAlignment = max(maxAlignment, typeSize)

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