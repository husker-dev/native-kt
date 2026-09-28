package com.huskerdev.nativekt.intellij.ndl.navigation

import com.huskerdev.nativekt.intellij.ndl.lexer.*
import com.huskerdev.nativekt.intellij.ndl.psi.*
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet

class NdlFindUsagesProvider: FindUsagesProvider {

    override fun getWordsScanner(): WordsScanner {
        return DefaultWordsScanner(
            NdlLexer(),
            TokenSet.create(NdlTokenType.IDENTIFIER),
            TokenSet.create(NdlTokenType.BLOCK_COMMENT, NdlTokenType.LINE_COMMENT),
            TokenSet.create(
                NdlTokenType.STRING,
                NdlTokenType.DECIMAL,
                NdlTokenType.INTEGER,
                NdlTokenType.TRUE,
                NdlTokenType.FALSE
            )
        )
    }

    override fun canFindUsagesFor(element: PsiElement): Boolean =
        element is NdlPsiTypeDeclaration

    override fun getHelpId(element: PsiElement): String = ""

    override fun getType(element: PsiElement): String = when (element) {
        is NdlPsiInterface -> "interface"
        is NdlPsiDictionary -> "dictionary"
        is NdlPsiCallback -> "callback"
        is NdlPsiTypedef -> "typedef"
        is NdlPsiEnum -> "enum"
        else -> ""
    }

    override fun getDescriptiveName(element: PsiElement): String =
        (element as? NdlPsiTypeDeclaration)?.name ?: ""

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String =
        (element as? NdlPsiTypeDeclaration)?.name ?: ""
}
