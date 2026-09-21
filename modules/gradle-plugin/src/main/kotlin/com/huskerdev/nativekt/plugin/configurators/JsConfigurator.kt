package com.huskerdev.nativekt.plugin.configurators

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.configurators.compileJs
import com.huskerdev.nativekt.configurators.prepareJs
import com.huskerdev.nativekt.plugin.NATIVE_TASK_GROUP
import com.huskerdev.nativekt.plugin.dependsOnProjectReload
import com.huskerdev.nativekt.utils.*
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import javax.inject.Inject

internal fun configureJs(
    project: Project,
    commonTask: TaskProvider<*>?,
    context: NativeModuleContext,
    sourceSet: KotlinSourceSet,
    expectActual: Boolean,
    targetType: TargetType
) {
    checkForLongTypes(context, context.configuration as NativeKtJsConfiguration, targetType)
    val layout = DirectoryLayout.of(context, targetType)

    // ====================
    //     Prepare task
    // ====================

    val prepareTask = project.tasks.register(
        "prepareNatives${context.moduleName.upperCamelCase()}${targetType.kotlinTarget.uppercaseFirstChar()}",
        PrepareNativesJs::class.java
    )
    prepareTask.get().also {
        it.inputs.dir(context.module.dir.file)
        it.inputs.file(context.module.resolveNdlFile().file)
        it.outputs.dirs(layout.kotlinSourcesDir.file, layout.nativeSourcesDir.file)

        it.useExpectActual = expectActual
        it.targetType      = targetType
        it.context         = context.serialize()
    }
    if(commonTask != null)
        prepareTask.get().dependsOn(commonTask)
    prepareTask.get().dependsOnProjectReload()

    // ====================
    //   Compilation task
    // ====================

    val compileTask = project.tasks.register(
        "compileNatives${context.moduleName.upperCamelCase()}${targetType.kotlinTarget.uppercaseFirstChar()}",
        CompileNativesJs::class.java
    )
    compileTask.get().also {
        it.inputs.dir(context.module.dir.file)
        it.inputs.file(context.module.resolveNdlFile().file)
        it.inputs.dir(layout.kotlinSourcesDir.file)
        it.inputs.dir(layout.nativeSourcesDir.file)
        it.outputs.dirs(layout.nativeBuildDir.file, layout.kotlinResourcesDir!!.file)

        it.context    = context.serialize()
        it.targetType = targetType
    }
    compileTask.get().dependsOn(prepareTask)

    sourceSet.kotlin.srcDir(compileTask.map { layout.kotlinSourcesDir.file })
    sourceSet.resources.srcDir(compileTask.map { layout.kotlinResourcesDir!!.file })
}

private abstract class PrepareNativesJs @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var useExpectActual: Boolean
    @get:Input abstract var targetType: TargetType
    @get:Input abstract var context: String

    @TaskAction
    fun action() {
        prepareJs(
            context = NativeModuleContext.deserialize(
                json = context,
                executor = GradleTaskExecutor(execOps),
                logger = GradlePluginLogger(logger)
            ),
            useExpectActual, targetType
        )
    }
}

private abstract class CompileNativesJs @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var targetType: TargetType
    @get:Input abstract var context: String

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        compileJs(
            context = NativeModuleContext.deserialize(
                json = context,
                executor = GradleTaskExecutor(execOps),
                logger = GradlePluginLogger(logger)
            ),
            targetType
        )
    }
}