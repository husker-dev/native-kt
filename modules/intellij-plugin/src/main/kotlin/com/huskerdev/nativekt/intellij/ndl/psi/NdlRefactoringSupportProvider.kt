package com.huskerdev.nativekt.intellij.ndl.psi

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.psi.PsiElement

class NdlRefactoringSupportProvider: RefactoringSupportProvider() {
    override fun isMemberInplaceRenameAvailable(
        element: PsiElement,
        context: PsiElement?
    ): Boolean = element is NdlPsiNamedDeclaration
}