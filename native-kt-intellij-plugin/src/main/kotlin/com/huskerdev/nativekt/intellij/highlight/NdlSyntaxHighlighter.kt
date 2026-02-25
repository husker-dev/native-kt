package com.huskerdev.nativekt.intellij.highlight

import com.huskerdev.nativekt.intellij.lexer.NdlLexerAdapter
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class NdlSyntaxHighlighter: SyntaxHighlighterBase() {
    override fun getHighlightingLexer() = NdlLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType) = arrayListOf<TextAttributesKey>().apply {

        if (tokenType == NdlTokenType.FUNCTION_NAME)
            add(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)

        if (tokenType == NdlTokenType.KEYWORD)
            add(DefaultLanguageHighlighterColors.KEYWORD)

        if (tokenType == NdlTokenType.COMMENT)
            add(DefaultLanguageHighlighterColors.LINE_COMMENT)

        if (tokenType == NdlTokenType.ANNOTATION)
            add(DefaultLanguageHighlighterColors.METADATA)

        if (tokenType == NdlTokenType.IDENTIFIER)
            add(DefaultLanguageHighlighterColors.IDENTIFIER)

        if (tokenType == NdlTokenType.TYPE)
            add(DefaultLanguageHighlighterColors.KEYWORD)

    }.toTypedArray()
}