package com.huskerdev.nativekt.intellij

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.ui.SimpleTextAttributes

class NativeKtTreeDecorator(project: Project): ProjectViewNodeDecorator {
    private val service = project.service<NativeKtService>()

    override fun decorate(
        node: ProjectViewNode<*>,
        data: PresentationData
    ) {
        val file = extractVirtualFile(node) ?: return
        if (!file.isDirectory) return

        if (file.path in service.srdDirs) {
            data.setIcon(NativeKtIcons.treeIcon)
            data.clearText()
            data.addText(file.name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES)
        }
    }

    private fun extractVirtualFile(node: ProjectViewNode<*>) =
        when (val value = node.value) {
            is PsiDirectory -> value.virtualFile
            is VirtualFile -> value
            else -> null
        }
}