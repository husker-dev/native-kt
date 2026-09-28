package com.huskerdev.nativekt.intellij.ndl.lexer

import com.huskerdev.nativekt.NdlEnv
import com.huskerdev.webidl.lexer.WebIDLLexer
import com.intellij.lexer.LexerBase
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.tree.IElementType

class NdlLexer: LexerBase() {

    private var buffer: CharSequence = ""
    private var bufferStart = 0
    private var bufferEnd = 0

    private var lexer: WebIDLLexer? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        bufferStart = startOffset
        bufferEnd = endOffset

        lexer = try {
            WebIDLLexer(
                buffer.subSequence(startOffset, endOffset).iterator(),
                NdlEnv.builtinTypes.keys,
                includeSkipped = true
            )
        } catch (_: Throwable) { null }
    }

    override fun getTokenType(): IElementType? =
        lexer?.run { NdlTokenType.from(current.type) }

    override fun getTokenStart(): Int =
        lexer?.run { bufferStart + current.bounds.startOffset } ?: bufferEnd

    override fun getTokenEnd(): Int =
        lexer?.run { bufferStart + current.bounds.endOffset } ?: bufferEnd

    override fun getState(): Int = 0

    override fun getBufferSequence(): CharSequence = buffer

    override fun getBufferEnd(): Int = bufferEnd

    override fun advance() {
        try {
            val lexer = lexer ?: return
            if(lexer.hasNext()) {
                lexer.next()
                return
            }
        } catch (_: Throwable) { }
        lexer = null
    }
}