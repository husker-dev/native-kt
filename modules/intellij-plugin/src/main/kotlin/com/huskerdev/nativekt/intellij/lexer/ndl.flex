// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.huskerdev.nativekt.intellij.lexer;

import com.intellij.psi.tree.IElementType;
import com.huskerdev.nativekt.intellij.highlight.NdlTokenType;
import com.intellij.psi.TokenType;

%%

%class NdlLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}


%state AFTER_EQUALS
%state IN_PARAMETERS

CRLF=\R
WHITE_SPACE=[\ \n\t\f]
IDENTIFIER=[a-zA-Z][a-zA-Z0-9_]*
INTEGER=[0-9]+
FLOAT=[0-9]+\.[0-9]+([Ee][+-]?[0-9]+)?
STRING=\"[^\"]*\"
SINGLE_LINE_COMMENT="//"[^\r\n]*
MULTI_LINE_COMMENT="/"\*([^*]|[\r\n]|(\*+([^*/]|[\r\n])))*\*+"/"
ANNOTATION=\[[^\]]*\]

%%

<YYINITIAL, AFTER_EQUALS, IN_PARAMETERS> {
  {WHITE_SPACE}               { return TokenType.WHITE_SPACE; }
  {SINGLE_LINE_COMMENT}       { return NdlTokenType.COMMENT; }
  {MULTI_LINE_COMMENT}        { return NdlTokenType.COMMENT; }
  {ANNOTATION}                { return NdlTokenType.ANNOTATION; }

  // Symbols - same behavior in all states
  ";"                         { yybegin(YYINITIAL); return NdlTokenType.SEMICOLON; }
  ","                         { return NdlTokenType.COMMA; }
  "?"                         { return NdlTokenType.OPERATOR; }
  "{"                         { yybegin(YYINITIAL); return NdlTokenType.BRACE; }
  "}"                         { return NdlTokenType.BRACE; }
  "["                         { return NdlTokenType.BRACKET; }
  "]"                         { return NdlTokenType.BRACKET; }
  ":"                         { return NdlTokenType.OPERATOR; }
  "<"                         { return NdlTokenType.OPERATOR; }
  ">"                         { return NdlTokenType.OPERATOR; }
  "|"                         { return NdlTokenType.OPERATOR; }

  // Literals - same behavior in all states
  {INTEGER}                   { return NdlTokenType.NUMBER; }
  {FLOAT}                     { return NdlTokenType.NUMBER; }
  {STRING}                    { return NdlTokenType.STRING; }

  // Types - same behavior in all states
  "void"|"boolean"|"byte"|"char"|"short"|"long"|"int"|
  "float"|"double"|"string"  { return NdlTokenType.TYPE; }
}

<YYINITIAL, AFTER_EQUALS> {
  // Keywords - only in normal state and after equals, but not in parameters
  "namespace"|"typedef"|"interface"|"partial"|"dictionary"|"enum"|"callback"|"const"|
  "null"|"true"|"false"|"readonly"|"attribute"|"static"|"stringifier"|
  "optional"|"sequence"|"deleter"|"getter"|"setter"  { return NdlTokenType.KEYWORD; }
}

<YYINITIAL> {
  // Transition to the state after equals sign
  "="                         { yybegin(AFTER_EQUALS); return NdlTokenType.OPERATOR; }

  // Identifier before parenthesis - function name
  {IDENTIFIER}/[ \t\r\n]*"("  { return NdlTokenType.FUNCTION_NAME; }

  // Regular identifiers
  {IDENTIFIER}                { return NdlTokenType.IDENTIFIER; }

  // Opening parenthesis - transition to parameters state
  "("                         { yybegin(IN_PARAMETERS); return NdlTokenType.PARENTHESIS; }

  // Closing parenthesis
  ")"                         { return NdlTokenType.PARENTHESIS; }

  [^]                         { return TokenType.BAD_CHARACTER; }
}

<AFTER_EQUALS> {
  // In this state, identifiers before parenthesis are not considered function names
  {IDENTIFIER}                { return NdlTokenType.IDENTIFIER; }

  // Opening parenthesis - transition to parameters state
  "("                         { yybegin(IN_PARAMETERS); return NdlTokenType.PARENTHESIS; }

  // Closing parenthesis
  ")"                         { return NdlTokenType.PARENTHESIS; }

  // Equals sign - remain in the same state
  "="                         { return NdlTokenType.OPERATOR; }

  [^]                         { yybegin(YYINITIAL); return TokenType.BAD_CHARACTER; }
}

<IN_PARAMETERS> {
  // In this state, all identifiers are regular identifiers,
  // regardless of their text (even if they match keywords)
  {IDENTIFIER}                { return NdlTokenType.IDENTIFIER; }

  // Opening parenthesis - nested parameters, remain in the same state
  "("                         { return NdlTokenType.PARENTHESIS; }

  // Closing parenthesis - return to normal state
  ")"                         { yybegin(YYINITIAL); return NdlTokenType.PARENTHESIS; }

  // Equals sign inside parameters
  "="                         { return NdlTokenType.OPERATOR; }

  [^]                         { return TokenType.BAD_CHARACTER; }
}