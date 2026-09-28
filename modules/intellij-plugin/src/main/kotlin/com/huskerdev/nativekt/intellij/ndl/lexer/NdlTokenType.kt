package com.huskerdev.nativekt.intellij.ndl.lexer

import com.huskerdev.nativekt.intellij.ndl.NdlLanguage
import com.huskerdev.webidl.lexer.WebIDLLexer.LexemeType
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class NdlTokenType(
    debugName: String
): IElementType(debugName, NdlLanguage.INSTANCE) {
    companion object {
        @JvmField val IDENTIFIER = NdlTokenType("IDENTIFIER")
        @JvmField val TYPE = NdlTokenType("TYPE")
        @JvmField val KEYWORD = NdlTokenType("KEYWORD")
        @JvmField val STRING = NdlTokenType("STRING")
        @JvmField val INTEGER = NdlTokenType("INTEGER")
        @JvmField val DECIMAL = NdlTokenType("DECIMAL")
        @JvmField val TRUE = NdlTokenType("TRUE")
        @JvmField val FALSE = NdlTokenType("FALSE")
        @JvmField val NULL = NdlTokenType("NULL")

        @JvmField val L_CURLY_BRACKET = NdlTokenType("L_CURLY_BRACKET")
        @JvmField val R_CURLY_BRACKET = NdlTokenType("R_CURLY_BRACKET")

        @JvmField val L_ROUND_BRACKET = NdlTokenType("L_ROUND_BRACKET")
        @JvmField val R_ROUND_BRACKET = NdlTokenType("R_ROUND_BRACKET")

        @JvmField val L_SQUARE_BRACKET = NdlTokenType("L_SQUARE_BRACKET")
        @JvmField val R_SQUARE_BRACKET = NdlTokenType("R_SQUARE_BRACKET")

        @JvmField val L_ANGLE_BRACKET = NdlTokenType("L_ANGLE_BRACKET")
        @JvmField val R_ANGLE_BRACKET = NdlTokenType("R_ANGLE_BRACKET")

        @JvmField val COMMA = NdlTokenType("COMMA")
        @JvmField val SEMICOLON = NdlTokenType("SEMICOLON")
        @JvmField val COLON = NdlTokenType("COLON")
        @JvmField val EQUALS = NdlTokenType("EQUALS")
        @JvmField val QUESTION = NdlTokenType("QUESTION")
        @JvmField val ELLIPSIS = NdlTokenType("ELLIPSIS")
        @JvmField val WILDCARD = NdlTokenType("WILDCARD")

        @JvmField val LINE_COMMENT = NdlTokenType("LINE_COMMENT")
        @JvmField val BLOCK_COMMENT = NdlTokenType("BLOCK_COMMENT")
        @JvmField val UNKNOWN = NdlTokenType("UNKNOWN")
        @JvmField val END = NdlTokenType("END")

        fun from(type: LexemeType): IElementType = when (type) {
            LexemeType.IDENTIFIER       -> IDENTIFIER
            LexemeType.TYPE             -> TYPE
            LexemeType.KEYWORD          -> KEYWORD
            LexemeType.STRING           -> STRING
            LexemeType.INTEGER          -> INTEGER
            LexemeType.DECIMAL          -> DECIMAL
            LexemeType.TRUE             -> TRUE
            LexemeType.FALSE            -> FALSE
            LexemeType.NULL             -> NULL
            LexemeType.L_CURLY_BRACKET  -> L_CURLY_BRACKET
            LexemeType.R_CURLY_BRACKET  -> R_CURLY_BRACKET
            LexemeType.L_ROUND_BRACKET  -> L_ROUND_BRACKET
            LexemeType.R_ROUND_BRACKET  -> R_ROUND_BRACKET
            LexemeType.L_SQUARE_BRACKET -> L_SQUARE_BRACKET
            LexemeType.R_SQUARE_BRACKET -> R_SQUARE_BRACKET
            LexemeType.L_ANGLE_BRACKET  -> L_ANGLE_BRACKET
            LexemeType.R_ANGLE_BRACKET  -> R_ANGLE_BRACKET
            LexemeType.COMMA            -> COMMA
            LexemeType.SEMICOLON        -> SEMICOLON
            LexemeType.COLON            -> COLON
            LexemeType.EQUALS           -> EQUALS
            LexemeType.QUESTION         -> QUESTION
            LexemeType.ELLIPSIS         -> ELLIPSIS
            LexemeType.WILDCARD         -> WILDCARD
            LexemeType.WHITE_SPACE      -> TokenType.WHITE_SPACE
            LexemeType.LINE_COMMENT     -> LINE_COMMENT
            LexemeType.BLOCK_COMMENT    -> BLOCK_COMMENT
            LexemeType.UNKNOWN          -> UNKNOWN
            LexemeType.END              -> END
        }
    }
}