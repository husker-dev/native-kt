package com.huskerdev.nativekt.intellij.listeners

import com.huskerdev.stdlibx.fs.SyncPath
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.readText
import org.rust.cargo.project.model.CargoProject
import org.rust.cargo.project.model.CargoProjectsService
import org.rust.cargo.project.model.cargoProjects
import org.rust.openapiext.pathAsPath
import kotlin.io.path.absolutePathString

class NativeKtRustProjectService(
    private val project: Project,
) : Disposable, CargoProjectsService.CargoProjectsListener {
    init {
        project.messageBus.connect(this).subscribe(
            CargoProjectsService.CARGO_PROJECTS_TOPIC,
            this,
        )
    }

    fun processExistingProjects() =
        process(project.cargoProjects.allProjects)

    override fun cargoProjectsUpdated(
        service: CargoProjectsService,
        projects: Collection<CargoProject>,
    ) = process(projects)

    private fun process(projects: Collection<CargoProject>) {
        val collectedModules = arrayListOf<NativeKtUpdateService.KnownDir>()

        for (cargoProject in projects) {
            for (pkg in cargoProject.workspace?.packages.orEmpty()) {
                val outDir = pkg.outDir ?: continue

                collectedModules += outDir.children
                    .filter { it.name.endsWith(".nativekt.txt") }
                    .mapNotNull {
                        val path = SyncPath(it.readText())
                        if(path.exists()) path else null
                    }
                    .map {
                        NativeKtUpdateService.KnownDir(
                            configuration = it,
                            buildDir = SyncPath(outDir.pathAsPath.absolutePathString())
                        )
                    }
            }
        }

        val service = project.service<NativeKtUpdateService>()
        service.replaceRustDirs(collectedModules)
    }

    override fun dispose() = Unit
}
