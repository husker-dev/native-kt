package com.huskerdev.nativekt.gradle

import com.huskerdev.nativekt.plugin.NativeKtModel
import org.gradle.api.Project
import org.gradle.tooling.provider.model.ToolingModelBuilder
import java.io.File

class NativeKtModelBuilder: ToolingModelBuilder {

    override fun canBuild(modelName: String): Boolean =
        modelName == NativeKtModel::class.java.name

    override fun buildAll(
        modelName: String,
        project: Project
    ): Any {
        val extension = project.extensions
            .getByType(NativeKtExtension::class.java)

        return NativeKtModel(
            configureFiles = extension.configureFiles.toList(),
            serializedModules = extension.serializedModules.toList(),
            buildDir = File(
                project.layout.buildDirectory.get().asFile,
                "generated/nativekt"
            ).absolutePath
        )
    }
}