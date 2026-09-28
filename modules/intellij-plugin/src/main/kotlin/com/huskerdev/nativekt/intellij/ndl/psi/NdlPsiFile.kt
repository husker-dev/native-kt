package com.huskerdev.nativekt.intellij.ndl.psi

import com.huskerdev.nativekt.intellij.ndl.NdlFileType
import com.huskerdev.nativekt.intellij.ndl.NdlLanguage
import com.huskerdev.nativekt.intellij.ndl.parser.NdlDefinitionType
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.PsiFileImpl

class NdlPsiFile(
    viewProvider: FileViewProvider,
): PsiFileImpl(NdlDefinitionType.FILE, NdlDefinitionType.FILE, viewProvider) {

    override fun getFileType(): FileType =
        NdlFileType.INSTANCE

    override fun getLanguage(): Language =
        NdlLanguage.INSTANCE

    override fun accept(visitor: PsiElementVisitor) {
        visitor.visitFile(this)
    }

    override fun toString(): String =
        "NdlFile:${name}"
}