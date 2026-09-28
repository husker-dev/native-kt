package com.huskerdev.nativekt.intellij.ndl.highlight

import com.huskerdev.nativekt.intellij.ndl.lexer.NdlTokenType
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class NdlBraceMatcher: PairedBraceMatcher {
    override fun getPairs() = arrayOf(
        BracePair(NdlTokenType.L_CURLY_BRACKET, NdlTokenType.R_CURLY_BRACKET, true),
        BracePair(NdlTokenType.L_ANGLE_BRACKET, NdlTokenType.R_ANGLE_BRACKET, false),
        BracePair(NdlTokenType.L_ROUND_BRACKET, NdlTokenType.R_ROUND_BRACKET, false),
        BracePair(NdlTokenType.L_SQUARE_BRACKET, NdlTokenType.R_SQUARE_BRACKET, false),
    )

    override fun isPairedBracesAllowedBeforeType(l: IElementType, r: IElementType?): Boolean = true
    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int) = openingBraceOffset
}