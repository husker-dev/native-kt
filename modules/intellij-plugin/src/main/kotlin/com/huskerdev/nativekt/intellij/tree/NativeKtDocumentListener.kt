package com.huskerdev.nativekt.intellij.tree

import com.huskerdev.nativekt.intellij.NativeKtUpdateService
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.ProjectLocator

class NativeKtDocumentListener: DocumentListener {

    override fun documentChanged(event: DocumentEvent) {
        val file = FileDocumentManager.getInstance()
            .getFile(event.document) ?: return

        val isNdl = file.extension == "ndl"
        val isToml = file.name == "nativekt.toml"

        if(!isNdl && !isToml) return

        val project = ProjectLocator.getInstance()
            .guessProjectForFile(file) ?: return

        if(isNdl)
            service<NativeKtUpdateService>().scheduleFromNdl(project, file, event.document.text)
        if(isToml)
            service<NativeKtUpdateService>().scheduleFromToml(project, file, event.document.text)
    }
}