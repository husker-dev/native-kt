package com.huskerdev.nativekt.intellij

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class StartupActivity: ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<NativeKtService>()
    }
}