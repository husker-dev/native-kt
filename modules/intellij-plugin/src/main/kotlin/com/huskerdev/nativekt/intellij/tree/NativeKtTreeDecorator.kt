package com.huskerdev.nativekt.intellij.tree

import com.huskerdev.nativekt.intellij.NativeKtIcons
import com.huskerdev.nativekt.intellij.asSyncPath
import com.huskerdev.nativekt.intellij.listeners.NativeKtUpdateService
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.ui.SimpleTextAttributes

class NativeKtTreeDecorator: ProjectViewNodeDecorator {
    override fun decorate(
        node: ProjectViewNode<*>,
        data: PresentationData
    ) {
        val file = extractVirtualFile(node) ?: return
        if (!file.isDirectory) return

        val project = node.project ?: return
        val path = file.asSyncPath()
        val modules = project.service<NativeKtUpdateService>().modules

        if (modules.any { it.nativeProjectDir.isSameFileAs(path) }) {
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