package com.huskerdev.nativekt.intellij.listeners

import com.huskerdev.stdlibx.fs.SyncPath
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspaceListener
import kotlinx.coroutines.*
import java.io.File
import java.nio.file.Path

class NativeKtCMakeProjectService(
    private val project: Project,
    private val scope: CoroutineScope,
) : Disposable, CMakeWorkspaceListener {
    private var discoveryJob: Job? = null

    init {
        project.messageBus.connect(this).subscribe(CMakeWorkspaceListener.TOPIC, this)
    }

    @Synchronized
    fun processExistingProjects() {
        discoveryJob?.cancel()
        val workspace = CMakeWorkspace.getInstance(project)
        val buildDirs = workspace.profileInfos.map { it.generationDir }

        discoveryJob = scope.launch(Dispatchers.IO) {
            val dirs = collectModules(buildDirs)
            ensureActive()
            if (!project.isDisposed)
                project.service<NativeKtUpdateService>().replaceCMakeDirs(dirs)
        }
    }

    override fun reloadingFinished(canceled: Boolean) {
        if (!canceled) processExistingProjects()
    }

    override fun generationDirCleared(generationDir: Path) = processExistingProjects()

    private fun collectModules(
        buildDirs: List<File>,
    ): List<NativeKtUpdateService.KnownDir> {
        val modules = arrayListOf<NativeKtUpdateService.KnownDir>()
        for (buildDir in buildDirs) {
            for (file in buildDir.walkTopDown()) {
                if (!file.isFile || file.name != "config.txt")
                    continue

                val config = File(file.readText().trim())
                if (!config.isAbsolute || !config.isFile)
                    continue

                modules += NativeKtUpdateService.KnownDir(
                    configuration = SyncPath(config.normalize().absolutePath),
                    buildDir = SyncPath(file.parentFile.parentFile.absolutePath),
                )
            }
        }
        return modules.distinct()
    }

    override fun dispose() {
        discoveryJob?.cancel()
    }
}
