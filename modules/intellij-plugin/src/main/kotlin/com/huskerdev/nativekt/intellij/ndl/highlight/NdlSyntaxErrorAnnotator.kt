package com.huskerdev.nativekt.intellij.ndl.highlight

import com.huskerdev.nativekt.NdlEnv
import com.huskerdev.webidl.WebIDL
import com.huskerdev.webidl.WebIDLErrorException
import com.huskerdev.webidl.WebIDLUnresolvedReferenceException
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

typealias Errors = List<WebIDLErrorException>

class NdlSyntaxErrorAnnotator: ExternalAnnotator<Errors, Errors>() {

    override fun collectInformation(file: PsiFile): Errors {
        return try {
            val text = file.text
            if (text.isEmpty()) return emptyList()

            WebIDL.resolve(text, NdlEnv).mergedErrors
        } catch (_: Throwable) {
            emptyList()
        }
    }

    override fun doAnnotate(collectedInfo: Errors): Errors = collectedInfo

    override fun apply(
        file: PsiFile,
        errors: Errors,
        holder: AnnotationHolder
    ) {
        if (file.textLength == 0)
            return

        errors.forEach { error ->
            val builder = holder.newAnnotation(HighlightSeverity.ERROR, error.errorMessage)
                .range(TextRange(
                    error.bounds.startOffset.coerceIn(0, file.textLength - 1),
                    error.bounds.endOffset.coerceIn(0, file.textLength)
                ))

            if(error is WebIDLUnresolvedReferenceException)
                builder.textAttributes(HighlighterColors.BAD_CHARACTER)

            builder.create()
        }
    }
}