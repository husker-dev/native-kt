package com.huskerdev.nativekt.intellij.ndl.structure

import com.huskerdev.nativekt.intellij.ndl.psi.NdlPsiFile
import com.huskerdev.nativekt.intellij.ndl.psi.*
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.childrenOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType


class NdlStructureViewFactory: PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile) = object: TreeBasedStructureViewBuilder() {
        override fun createStructureViewModel(ed: Editor?) = NdlStructureViewModel(psiFile)
    }
}

class NdlStructureViewModel(psiFile: PsiFile): StructureViewModelBase(
    psiFile,
    NdlStructureViewTreeElement(psiFile)
), StructureViewModel.ElementInfoProvider {
    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean = false
    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean = false
}

class NdlStructureViewTreeElement(element: PsiElement) : PsiTreeElementBase<PsiElement>(element) {

    override fun getPresentableText() = when(val element = element!!) {
        is PsiFile -> element.name
        is NdlPsiCallback -> buildCallbackName(element)
        is NdlPsiOperation -> buildOperationName(element)
        is NdlPsiField -> "${element.name}: ${element.getChildOfType<NdlPsiType>()?.text ?: "<unknown>"}"
        is NdlPsiEnumElement -> element.text.replace("\"", "")
        is NdlPsiNamedDeclaration -> element.name
        is NdlPsiConstructor -> buildConstructorName(element)
        else -> element.text ?: ""
    }

    override fun getChildrenBase() = when(val element = element!!) {
        is NdlPsiFile,
        is NdlPsiNamespace,
        is NdlPsiInterface,
        is NdlPsiDictionary,
        is NdlPsiEnum -> element.children
            .filter {
                it is NdlPsiTypeDeclaration ||
                        it is NdlPsiNamespace ||
                        it is NdlPsiOperation ||
                        it is NdlPsiConstructor ||
                        it is NdlPsiField ||
                        it is NdlPsiEnumElement
            }
            .map(::NdlStructureViewTreeElement)
            .toMutableList()
        else -> mutableListOf()
    }

    private fun buildCallbackName(callback: NdlPsiCallback) = buildString {
        append(callback.name)
        append("(")
        val operation = callback.getChildOfType<NdlPsiOperation>() ?: return@buildString
        operation.childrenOfType<NdlPsiArgument>().joinTo(this) {
            it.getChildOfType<NdlPsiType>()?.text ?: "<unknown>"
        }
        append(")")
        operation.getChildOfType<NdlPsiType>()?.let {
            append(": ${it.text}")
        }
    }

    private fun buildOperationName(operation: NdlPsiOperation) = buildString {
        append(operation.name)
        append("(")
        operation.childrenOfType<NdlPsiArgument>().joinTo(this) {
            it.getChildOfType<NdlPsiType>()?.text ?: "<unknown>"
        }
        append(")")
        operation.getChildOfType<NdlPsiType>()?.let {
            append(": ${it.text}")
        }
    }

    private fun buildConstructorName(constructor: NdlPsiConstructor) = buildString {
        append("constructor ")
        append((constructor.parent as? NdlPsiNamedDeclaration)?.name ?: "<unknown>")
        append("(")
        constructor.childrenOfType<NdlPsiArgument>().joinTo(this) {
            it.getChildOfType<NdlPsiType>()?.text ?: "<unknown>"
        }
        append(")")
    }
}