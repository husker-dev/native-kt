package com.huskerdev.nativekt.intellij.ndl.navigation

import com.huskerdev.nativekt.intellij.ndl.psi.*
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.usages.PsiNamedElementUsageGroupBase
import com.intellij.usages.Usage
import com.intellij.usages.UsageGroup
import com.intellij.usages.UsageTarget
import com.intellij.usages.rules.PsiElementUsage
import com.intellij.usages.rules.UsageGroupingRule
import com.intellij.usages.rules.UsageGroupingRuleProvider

class NdlUsageGroupingRuleProvider: UsageGroupingRuleProvider {
    override fun getActiveRules(project: Project): Array<UsageGroupingRule> =
        arrayOf(NdlUsageGroupingRule())
}

class NdlUsageGroupingRule: UsageGroupingRule {
    override fun getParentGroupsFor(
        usage: Usage,
        targets: Array<UsageTarget?>
    ): List<UsageGroup> {
        val element = (usage as? PsiElementUsage)?.element as? NdlPsi
            ?: return emptyList()

        val targetElements = when(val parent = findNamedParent(element)) {
            is NdlPsiField -> listOf(parent.parent!!, parent)
            is NdlPsiOperation -> when (val parent2 = findNamedParent(parent)) {
                is NdlPsiCallback -> listOf(parent2)
                is NdlPsiInterface, is NdlPsiNamespace -> listOf(parent2, parent)
                else -> listOf(parent)
            }
            is NdlPsiNamedDeclaration -> listOf(parent)
            else -> null
        }
        return targetElements
            ?.map { PsiNamedElementUsageGroupBase(it as NdlPsiNamedDeclaration) }
            ?: emptyList()
    }

    private fun findNamedParent(of: PsiElement): NdlPsiNamedDeclaration? {
        var parent = of.parent
        while(parent !is NdlPsiNamedDeclaration) {
            if(parent == null || parent is PsiFile)
                return null
            parent = parent.parent
        }
        return parent
    }
}