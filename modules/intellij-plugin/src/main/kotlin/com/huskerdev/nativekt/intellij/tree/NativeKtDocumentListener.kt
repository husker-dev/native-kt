package com.huskerdev.nativekt.intellij.tree

import com.huskerdev.nativekt.intellij.asSyncPath
import com.huskerdev.nativekt.intellij.listeners.NativeKtUpdateService
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.ProjectLocator

class NativeKtDocumentListener: DocumentListener {

    override fun documentChanged(event: DocumentEvent) {
        val file = FileDocumentManager.getInstance()
            .getFile(event.document) ?: return

        if(file.extension != "ndl" && file.extension != "toml") return

        val project = ProjectLocator.getInstance()
            .guessProjectForFile(file) ?: return

        val filePath = file.asSyncPath()
        if(!filePath.exists()) return
        
        val service = project.service<NativeKtUpdateService>()

        service.modules.forEach { module ->
            if(
                filePath.isSameFileAs(module.ndlFile) ||
                filePath.isSameFileAs(module.configFile)
            ) service.schedule(module.configFile)
        }
    }
}