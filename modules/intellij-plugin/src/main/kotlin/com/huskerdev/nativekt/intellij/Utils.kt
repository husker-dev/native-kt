package com.huskerdev.nativekt.intellij

import com.huskerdev.nativekt.plugin.NativeKtModel
import com.intellij.openapi.externalSystem.model.Key
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.gradle.util.GradleConstants

val NATIVEKT_KEY: Key<NativeKtModel> = Key.create(
    NativeKtModel::class.java,
    ProjectKeys.MODULE.processingWeight + 1
)

fun getAllModels(project: Project): List<NativeKtModel> {
    return ProjectDataManager.getInstance()
        .getExternalProjectsData(project, GradleConstants.SYSTEM_ID)
        .mapNotNull { it.externalProjectStructure }
        .flatMap { structure ->
            ExternalSystemApiUtil.findAllRecursively(structure, NATIVEKT_KEY)
        }
        .map { it.data }
}