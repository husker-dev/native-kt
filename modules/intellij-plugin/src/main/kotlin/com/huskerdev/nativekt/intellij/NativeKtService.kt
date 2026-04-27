package com.huskerdev.nativekt.intellij

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter
import com.intellij.openapi.externalSystem.service.notification.ExternalSystemProgressNotificationManager
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Service(Service.Level.PROJECT)
class NativeKtService(
    private val project: Project,
): Disposable {

    val srdDirs = mutableSetOf<String>()

    private val listener = object: ExternalSystemTaskNotificationListenerAdapter(null) {
        override fun onSuccess(id: ExternalSystemTaskId) {
            CoroutineScope(Dispatchers.Default).launch {
                update()
            }
        }
    }

    init {
        service<ExternalSystemProgressNotificationManager>()
            .addNotificationListener(listener)
        update()
    }

    private fun update(){
        srdDirs.clear()

        ModuleManager.getInstance(project).modules.forEach { module ->
            val roots = ModuleRootManager.getInstance(module).contentRoots.toList()

            roots.firstOrNull { vf ->
                File(vf.canonicalPath, "build.gradle.kts").exists() ||
                        File(vf.canonicalPath, "build.gradle").exists()
            }?.apply {
                configureGradleModule(File(canonicalPath!!))
            }
        }
    }

    private fun configureGradleModule(root: File) {
        val srcFile = File(root, "build/nativekt.txt")
        if (!srcFile.exists())
            return

        srdDirs += srcFile.readLines().map { it.replace("\\", "/") }
    }

    override fun dispose() {
        service<ExternalSystemProgressNotificationManager>()
            .removeNotificationListener(listener)
    }
}