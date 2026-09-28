package com.huskerdev.nativekt.intellij.ndl.highlight

import com.huskerdev.nativekt.intellij.ndl.psi.*
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childrenOfType

class NdlHighlightingAnnotator: Annotator, DumbAware {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {

        // Attributes
        if(element is NdlPsiAttributeBlock) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element.textRange)
                .textAttributes(DefaultLanguageHighlighterColors.METADATA)
                .create()
        }

        // Operation
        if(element is NdlPsiOperation) {
            val name = element.nameIdentifier

            if(name.text.isNotEmpty()) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(name.textRange)
                    .textAttributes(DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
                    .create()
            }
        }

        // Constructor
        if(element is NdlPsiConstructor) {
            val header = element.childrenOfType<NdlPsiName>().first()

            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(header.textRange)
                .textAttributes(DefaultLanguageHighlighterColors.KEYWORD)
                .create()
        }
    }
}