package com.huskerdev.nativekt.intellij.listeners

import com.huskerdev.nativekt.intellij.listeners.NativeKtUpdateService.KnownDir
import com.huskerdev.nativekt.plugin.NativeKtModel
import com.huskerdev.stdlibx.fs.SyncPath
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.model.project.ProjectData
import org.gradle.tooling.model.idea.IdeaModule
import org.gradle.tooling.model.idea.IdeaProject
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension

class NativeKtGradleProjectResolver: AbstractProjectResolverExtension() {

    private var kotlinDirs = listOf<KnownDir>()

    override fun getExtraProjectModelClasses(): Set<Class<*>> =
        setOf(NativeKtModel::class.java)

    override fun getToolingExtensionsClasses(): Set<Class<*>> =
        setOf(NativeKtModel::class.java, NativeKtModel.Impl::class.java)

    override fun populateProjectExtraModels(
        gradleProject: IdeaProject,
        ideProject: DataNode<ProjectData?>
    ) {
        kotlinDirs = listOf()
        super.populateProjectExtraModels(gradleProject, ideProject)
    }

    override fun populateModuleExtraModels(
        gradleModule: IdeaModule,
        ideModule: DataNode<ModuleData>
    ) {
        val model = resolverCtx.getExtraProject(gradleModule, NativeKtModel::class.java)
        if(model == null) {
            super.populateModuleExtraModels(gradleModule, ideModule)
            return
        }

        val buildDir = SyncPath(model.buildDir)

        kotlinDirs += model.configureFiles.mapNotNull {
            val path = SyncPath(it)
            if(!path.exists()) return@mapNotNull null

            KnownDir(
                configuration = path,
                buildDir = buildDir
            )
        }

        super.populateModuleExtraModels(gradleModule, ideModule)
    }

    override fun resolveFinished(projectDataNode: DataNode<ProjectData?>) {
        val service = resolverCtx.project.service<NativeKtUpdateService>()
        service.replaceGradleDirs(projectDataNode.data.linkedExternalProjectPath, kotlinDirs)

        super.resolveFinished(projectDataNode)
    }
}
