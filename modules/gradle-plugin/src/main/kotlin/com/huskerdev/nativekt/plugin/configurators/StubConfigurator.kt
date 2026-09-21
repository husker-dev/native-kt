package com.huskerdev.nativekt.plugin.configurators

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.huskerdev.nativekt.DirectoryLayout
import com.huskerdev.nativekt.GradleTaskExecutor
import com.huskerdev.nativekt.GradlePluginLogger
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.configurators.prepareStub
import com.huskerdev.nativekt.utils.camelCase
import com.huskerdev.nativekt.plugin.dependsOnProjectReload
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import javax.inject.Inject

internal fun configureStub(
    project: Project,
    commonTask: TaskProvider<*>?,
    context: NativeModuleContext,
    sourceSet: KotlinSourceSet,
    targetType: TargetType
) {
    val layout = DirectoryLayout.of(context, targetType)
    val prepareTask = project.tasks.register(
        "prepareNatives${context.moduleName.camelCase()}Stub",
        PrepareNativesStub::class.java
    )
    prepareTask.get().let {
        it.inputs.dir(context.module.dir.file)
        it.inputs.file(context.module.resolveNdlFile().file)
        it.outputs.dirs(layout.kotlinSourcesDir.file)

        it.context    = context.serialize()
        it.targetType = targetType
    }
    prepareTask.dependsOn(commonTask)
    prepareTask.get().dependsOnProjectReload()
    sourceSet.kotlin.srcDir(prepareTask.map { layout.kotlinSourcesDir.file })
}

private abstract class PrepareNativesStub @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var context: String
    @get:Input abstract var targetType: TargetType

    @TaskAction
    fun action() {
        prepareStub(
            context = NativeModuleContext.deserialize(
                json = context,
                executor = GradleTaskExecutor(execOps),
                logger = GradlePluginLogger(logger)
            ),
            targetType = targetType
        )
    }
}