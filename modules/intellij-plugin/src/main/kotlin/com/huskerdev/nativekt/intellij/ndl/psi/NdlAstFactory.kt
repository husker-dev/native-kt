package com.huskerdev.nativekt.intellij.ndl.psi

import com.huskerdev.nativekt.intellij.ndl.lexer.NdlTokenType
import com.intellij.lang.ASTFactory
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType


class NdlAstFactory : ASTFactory() {
    override fun createLeaf(tokenType: IElementType, text: CharSequence): LeafElement? = when (tokenType) {
        NdlTokenType.IDENTIFIER -> NdlPsiIdentifier(tokenType, text)
        else -> super.createLeaf(tokenType, text)
    }
}

class NdlPsiIdentifier(type: IElementType, text: CharSequence) : LeafPsiElement(type, text) {
    override fun getReference(): PsiReference? {
        if(parent != null &&
            parent !is NdlPsiName &&
            parent !is NdlPsiBuiltinTypeReference &&
            parent !is NdlPsiTypeReference
        ) return NdlReference(this)
        return null
    }
    override fun toString(): String = "NdlPsiIdentifier"
}