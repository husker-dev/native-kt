package com.huskerdev.nativekt.intellij

import com.huskerdev.nativekt.intellij.listeners.NativeKtRustProjectService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class NativeKtActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        if(isRustPluginEnabled())
            project.service<NativeKtRustProjectService>().processExistingProjects()
    }
}