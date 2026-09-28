package com.huskerdev.nativekt.intellij.ndl.psi

import com.huskerdev.nativekt.intellij.ndl.NdlUtils
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.firstLeaf

class NdlReference(
    private val element: PsiElement
): PsiReferenceBase<PsiElement>(
    element,
    TextRange(0, element.textLength),
    false
) {
    private val target = NdlUtils.findTypeDeclarationNames(element.containingFile, element.text).firstOrNull()

    override fun resolve(): PsiElement? = target

    override fun handleElementRename(newName: String): PsiElement {
        NdlUtils.rename(element.firstLeaf(), newName)
        return element
    }

    override fun getVariants(): Array<Any> =
        NdlUtils.findTypeDeclarationNames(element.containingFile).toTypedArray()
}