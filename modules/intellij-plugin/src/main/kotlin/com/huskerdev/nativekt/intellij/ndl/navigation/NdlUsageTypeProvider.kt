package com.huskerdev.nativekt.intellij.ndl.navigation

import com.huskerdev.nativekt.intellij.ndl.psi.*
import com.intellij.psi.PsiElement
import com.intellij.usages.impl.rules.UsageType
import com.intellij.usages.impl.rules.UsageTypeProvider

class NdlUsageTypeProvider: UsageTypeProvider {
    companion object {
        val fieldType = UsageType { "Field type" }
        val parameterType = UsageType { "Parameter type" }
        val operationType = UsageType { "Operation return type" }
        val typeParameter = UsageType { "Type parameter" }
    }

    override fun getUsageType(element: PsiElement): UsageType? {
        if(element !is NdlPsiTypeReference)
            return null
        return when(element.parent?.parent) {
            is NdlPsiField -> fieldType
            is NdlPsiArgument -> parameterType
            is NdlPsiOperation -> operationType
            is NdlPsiType -> typeParameter
            else -> null
        }
    }
}