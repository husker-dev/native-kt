package com.huskerdev.nativekt.intellij

import com.huskerdev.nativekt.plugin.NativeKtModel
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ModuleData
import org.gradle.tooling.model.idea.IdeaModule
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension

class NativeKtProjectResolver: AbstractProjectResolverExtension() {

    override fun getExtraProjectModelClasses(): Set<Class<*>> =
        setOf(NativeKtModel::class.java)

    override fun getToolingExtensionsClasses(): Set<Class<*>> =
        setOf(NativeKtModel::class.java, NativeKtModel.Impl::class.java)

    override fun populateModuleExtraModels(
        gradleModule: IdeaModule,
        ideModule: DataNode<ModuleData>
    ) {
        resolverCtx.getExtraProject(
            gradleModule,
            NativeKtModel::class.java
        )?.let {
            ideModule.createChild(NATIVEKT_KEY, it)
        }
        super.populateModuleExtraModels(gradleModule, ideModule)
    }
}