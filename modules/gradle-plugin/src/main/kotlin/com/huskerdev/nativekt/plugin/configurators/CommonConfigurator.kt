package com.huskerdev.nativekt.plugin.configurators

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.configurators.prepareCommon
import com.huskerdev.nativekt.plugin.dependsOnProjectReload
import com.huskerdev.nativekt.utils.*
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import javax.inject.Inject


internal fun configureCommon(
    project: Project,
    context: NativeModuleContext,
    sourceSet: KotlinSourceSet
): TaskProvider<*> {
    val layout = DirectoryLayout.of(context, null)

    val prepareTask = project.tasks.register(
        "prepareNatives${context.moduleName.upperCamelCase()}Common",
        PrepareNativesCommon::class.java
    )
    prepareTask.get().let {
        it.inputs.dir(context.module.dir.file)
        it.inputs.file(context.module.resolveNdlFile().file)
        it.outputs.dir(layout.kotlinSourcesDir.file)

        it.context = context.serialize()
    }
    prepareTask.get().dependsOnProjectReload()

    sourceSet.kotlin.srcDir(prepareTask.map { layout.kotlinSourcesDir.file })

    return prepareTask
}

private abstract class PrepareNativesCommon @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var context: String

    @TaskAction
    fun action() {
        prepareCommon(
            context = NativeModuleContext.deserialize(
                json = context,
                executor = GradleTaskExecutor(execOps),
                logger = GradlePluginLogger(logger)
            )
        )
    }
}