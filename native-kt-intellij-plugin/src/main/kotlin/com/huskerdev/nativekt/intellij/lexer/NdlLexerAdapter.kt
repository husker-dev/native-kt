package com.huskerdev.nativekt.intellij.lexer

import com.intellij.lexer.FlexAdapter

class NdlLexerAdapter: FlexAdapter(NdlLexer(null))