package com.huskerdev.nativekt.intellij.tree

import com.huskerdev.nativekt.NativeKtModule
import com.huskerdev.nativekt.intellij.NativeKtIcons
import com.huskerdev.nativekt.intellij.getAllModels
import com.huskerdev.stdlibx.fs.SyncPath
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ProjectViewNodeDecorator
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.ui.SimpleTextAttributes
import kotlinx.serialization.json.Json

class NativeKtTreeDecorator: ProjectViewNodeDecorator {
    override fun decorate(
        node: ProjectViewNode<*>,
        data: PresentationData
    ) {
        val file = extractVirtualFile(node) ?: return
        if (!file.isDirectory) return

        val path = SyncPath(file.canonicalPath!!)

        if (getAllModels(node.project).any { model ->
                model.serializedModules.any {
                    Json.decodeFromString<NativeKtModule>(it)
                        .buildSystem.projectDir.isSameFileAs(path)
                }
        }) {
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