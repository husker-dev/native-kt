package com.huskerdev.nativekt.intellij.ndl.completion

import com.huskerdev.nativekt.intellij.ndl.psi.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

internal class NdlCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .inside(NdlPsiFile::class.java)
                .andNot(PlatformPatterns.psiElement().inside(NdlPsi::class.java)),
            object : CompletionProvider<CompletionParameters?>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    resultSet: CompletionResultSet
                ) {
                    resultSet.addAllElements(listOf(
                        LookupElementBuilder.create("dictionary "),
                        LookupElementBuilder.create("interface "),
                        LookupElementBuilder.create("dictionary "),
                        LookupElementBuilder.create("enum "),
                        LookupElementBuilder.create("namespace "),
                        LookupElementBuilder.create("callback "),
                    ))
                }
            }
        )
    }
}