package com.huskerdev.nativekt.intellij.ndl

import com.huskerdev.nativekt.intellij.ndl.psi.NdlPsiFile
import com.huskerdev.nativekt.intellij.ndl.psi.NdlPsiTypeDeclaration
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.firstLeaf

object NdlUtils {

    fun rename(element: PsiElement, newName: String): PsiElement {
        val newElement = PsiFileFactory.getInstance(element.project)
            .createFileFromText("dummy.ndl", NdlFileType.INSTANCE, newName)
            .firstChild.firstLeaf()
        element.replace(newElement)
        return newElement
    }

    fun findTypeDeclarationNames(file: PsiFile, name: String? = null) =
        file.childrenOfType<NdlPsiTypeDeclaration>().filter {
            name == null || name == it.name
        }

    fun collectAllTypeDeclarations(project: Project, name: String? = null) = buildList {
        val virtualFiles = FileTypeIndex.getFiles(
            NdlFileType.INSTANCE,
            GlobalSearchScope.allScope(project)
        )

        for (virtualFile in virtualFiles) {
            val file = PsiManager.getInstance(project).findFile(virtualFile) as NdlPsiFile
            file.childrenOfType<NdlPsiTypeDeclaration>().forEach {
                if(name == null || name == it.name)
                    add(it)
            }
        }
    }
}

