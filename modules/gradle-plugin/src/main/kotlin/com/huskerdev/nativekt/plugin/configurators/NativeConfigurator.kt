package com.huskerdev.nativekt.plugin.configurators

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.configurators.compileNative
import com.huskerdev.nativekt.configurators.prepareNative
import com.huskerdev.nativekt.plugin.NATIVE_TASK_GROUP
import com.huskerdev.nativekt.plugin.currentNativeTargetType
import com.huskerdev.nativekt.utils.upperCamelCase
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.the
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import javax.inject.Inject

internal fun configureNative(
    project: Project,
    commonTask: TaskProvider<*>?,
    context: NativeModuleContext,
    sourceSet: KotlinSourceSet,
    targetType: TargetType,
    expectActual: Boolean
) {
    val layout = DirectoryLayout.of(context, targetType)
    val kotlin = project.the<KotlinMultiplatformExtension>()

    val targetName = targetType.kotlinTarget
    if(targetName !in currentNativeTargetType().compiles)
        return

    // ====================
    //     Prepare task
    // ====================

    val prepareTask = project.tasks.register(
        "prepareNatives${context.moduleName.upperCamelCase()}Kn${targetName.capitalized()}",
        PrepareNativesKn::class.java
    )
    prepareTask.get().let {
        it.inputs.dir(context.module.dir.file)
        it.inputs.file(context.module.resolveNdlFile().file)
        it.outputs.dirs(layout.kotlinSourcesDir.file, layout.nativeSourcesDir.file, layout.cinteropDir!!.file)

        it.defFile.set(layout.cinteropFile!!.file)
        it.context         = context.serialize()
        it.useExpectActual = expectActual
        it.targetType      = targetType
    }
    if(commonTask != null)
        prepareTask.get().dependsOn(commonTask)

    project.tasks.matching { it.name == "downloadKotlinNativeDistribution" }.forEach {
        prepareTask.get().dependsOn(it)
    }
    sourceSet.kotlin.srcDir(prepareTask.map { layout.kotlinSourcesDir.file })

    // ====================
    //     Add cinterop
    // ====================

    val target = kotlin.targets.findByName(targetName) as? KotlinNativeTarget
        ?: throw UnsupportedOperationException()

    val compilation = target.compilations.findByName("main")
        ?: throw UnsupportedOperationException()

    compilation.cinterops {
        create("nativekt${context.moduleName.upperCamelCase()}").definitionFile.set(prepareTask.flatMap { it.defFile })
    }

    // ====================
    //   Compilation task
    // ====================

    val compilationTask = project.tasks.register(
        "compileNatives${context.moduleName.upperCamelCase()}Kn${targetName.capitalized()}",
        CompileNativesKn::class.java
    )
    compilationTask.get().let {
        it.inputs.dir(context.module.dir.file)
        it.inputs.file(context.module.resolveNdlFile().file)
        it.inputs.dir(layout.kotlinSourcesDir.file)
        it.inputs.dir(layout.nativeSourcesDir.file)
        it.outputs.dirs(layout.nativeBuildDir.file)

        it.context    = context.serialize()
        it.targetType = targetType
    }
    compilationTask.get().dependsOn(prepareTask)

    // Depends compilation on Kotlin source-generator
    project.tasks.matching { it.name == "compileKotlin${targetName.capitalized()}" }.forEach {
        it.dependsOn(compilationTask)
    }
    project.tasks.matching { it.name == "${targetName}SourcesJar" }.forEach {
        it.dependsOn(compilationTask)
    }

    // Force Kotlin re-linking when native files are changed
    project.tasks.matching { it is KotlinNativeLink && it.project == project }.forEach {
        it.inputs.dir(context.module.dir.file)
        it.inputs.file(context.module.resolveNdlFile().file)
    }

    // Compile natives only when compiling project (to prevent compilation on Gradle reload)
    project.gradle.taskGraph.whenReady {
        if (hasTask(compilationTask.get())) {
            prepareTask.get().shouldInit = true
            prepareTask.get().outputs.dirs(layout.nativeBuildDir.file)
        }
    }
}

private abstract class PrepareNativesKn @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:OutputFile
    abstract val defFile: RegularFileProperty

    @get:Input abstract var context: String
    @get:Input abstract var targetType: TargetType
    @get:Input abstract var useExpectActual: Boolean
    @get:Input abstract var shouldInit: Boolean

    @TaskAction
    fun action() {
        prepareNative(
            context = NativeModuleContext.deserialize(
                json = context,
                executor = GradleTaskExecutor(execOps),
                logger = GradlePluginLogger(logger)
            ),
            targetType, useExpectActual, shouldInit
        )
    }
}

private abstract class CompileNativesKn @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var context: String
    @get:Input abstract var targetType: TargetType

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        compileNative(
            context = NativeModuleContext.deserialize(
                json = context,
                executor = GradleTaskExecutor(execOps),
                logger = GradlePluginLogger(logger)
            ),
            targetType
        )
    }
}