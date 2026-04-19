package com.huskerdev.nativekt.intellij.highlight

import com.huskerdev.nativekt.intellij.NdlLanguage
import com.intellij.psi.tree.IElementType

class NdlTokenType(
    debugName: String
): IElementType(debugName, NdlLanguage.INSTANCE) {
    companion object {
        @JvmField val KEYWORD = NdlTokenType("KEYWORD")
        @JvmField val TYPE = NdlTokenType("TYPE")
        @JvmField val IDENTIFIER = NdlTokenType("IDENTIFIER")
        @JvmField val COMMENT = NdlTokenType("COMMENT")
        @JvmField val STRING = NdlTokenType("STRING")
        @JvmField val NUMBER = NdlTokenType("NUMBER")
        @JvmField val OPERATOR = NdlTokenType("OPERATOR")
        @JvmField val SEMICOLON = NdlTokenType("SEMICOLON")
        @JvmField val COMMA = NdlTokenType("COMMA")
        @JvmField val BRACE = NdlTokenType("BRACE")
        @JvmField val BRACKET = NdlTokenType("BRACKET")
        @JvmField val PARENTHESIS = NdlTokenType("PARENTHESIS")
        @JvmField val FUNCTION_NAME = NdlTokenType("FUNCTION_NAME")
        @JvmField val ANNOTATION = NdlTokenType("ANNOTATION")
    }

    override fun toString(): String =
        "SimpleTokenType." + super.toString()
}