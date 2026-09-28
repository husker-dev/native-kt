package com.huskerdev.nativekt.intellij.ndl.navigation

import com.huskerdev.nativekt.intellij.ndl.parser.NdlDefinitionType
import com.huskerdev.nativekt.intellij.ndl.psi.NdlPsi
import com.huskerdev.nativekt.intellij.ndl.psi.NdlPsiNamespace
import com.intellij.icons.AllIcons
import com.intellij.ide.IconProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import javax.swing.Icon

class NdlIconProvider: IconProvider() {
    override fun getIcon(element: PsiElement, flags: Int): Icon? {
        if(element !is NdlPsi)
            return null

        return when(element.elementType) {
            NdlDefinitionType.INTERFACE -> AllIcons.Nodes.Interface
            NdlDefinitionType.NAMESPACE -> AllIcons.Nodes.Package
            NdlDefinitionType.ENUM, NdlDefinitionType.ENUM_ELEMENT -> AllIcons.Nodes.Enum
            NdlDefinitionType.CALLBACK -> AllIcons.Nodes.Lambda
            NdlDefinitionType.OPERATION -> if(element.parent is NdlPsiNamespace) AllIcons.Nodes.Function else AllIcons.Nodes.Method
            NdlDefinitionType.CONSTRUCTOR -> AllIcons.Nodes.Constructor
            NdlDefinitionType.DICTIONARY -> AllIcons.Nodes.Models
            NdlDefinitionType.TYPEDEF, NdlDefinitionType.FIELD -> AllIcons.Nodes.Field
            NdlDefinitionType.ARGUMENT -> AllIcons.Nodes.Parameter

            NdlDefinitionType.REFERENCE_NAME -> AllIcons.Actions.ShowReadAccess
            else -> null
        }
    }
}