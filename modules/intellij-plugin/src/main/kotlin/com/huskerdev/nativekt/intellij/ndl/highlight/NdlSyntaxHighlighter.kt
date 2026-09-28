package com.huskerdev.nativekt.intellij.ndl.highlight

import com.huskerdev.nativekt.intellij.ndl.lexer.NdlLexer
import com.huskerdev.nativekt.intellij.ndl.lexer.NdlTokenType
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType

class NdlSyntaxHighlighter: SyntaxHighlighterBase() {
    override fun getHighlightingLexer() = NdlLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when(tokenType) {
        NdlTokenType.KEYWORD -> DefaultLanguageHighlighterColors.KEYWORD
        NdlTokenType.LINE_COMMENT, NdlTokenType.BLOCK_COMMENT -> DefaultLanguageHighlighterColors.LINE_COMMENT
        NdlTokenType.IDENTIFIER -> DefaultLanguageHighlighterColors.IDENTIFIER
        NdlTokenType.TYPE -> DefaultLanguageHighlighterColors.KEYWORD
        NdlTokenType.STRING -> DefaultLanguageHighlighterColors.STRING
        else -> null
    }?.let { arrayOf(it) } ?: arrayOf()
}

class NdlSyntaxHighlighterFactory: SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(
        p0: Project?,
        p1: VirtualFile?
    ): SyntaxHighlighter = NdlSyntaxHighlighter()
}