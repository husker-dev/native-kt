package com.huskerdev.nativekt.configurators

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.printers.kotlin.KotlinStubPrinter
import com.huskerdev.nativekt.utils.camelCase
import com.huskerdev.nativekt.utils.dependsOnProjectReload
import com.huskerdev.nativekt.utils.fresh
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

internal fun configureStub(
    project: Project,
    commonTask: TaskProvider<*>?,
    context: NativeModuleContext,
    sourceSet: KotlinSourceSet
) {
    val srcDir = File(context.srcGenDir, "stub")

    val classPathFile = File(srcDir, context.classPath.replace(".", "/"))

    val prepareTask = project.tasks.register(
        "prepareNatives${context.moduleName.camelCase()}Stub",
        PrepareNativesStub::class.java
    )
    prepareTask.get().let {
        it.srcDir.set(srcDir)
        it.inputs.dir(context.module.projectDir)

        it.context = context
        it.targetFile = File(classPathFile, "${context.moduleName}.kt").absolutePath
    }
    prepareTask.dependsOn(commonTask)
    prepareTask.get().dependsOnProjectReload()
    sourceSet.kotlin.srcDir(prepareTask.flatMap { it.srcDir })
}

private abstract class PrepareNativesStub: DefaultTask() {
    @get:OutputDirectory
    abstract val srcDir: DirectoryProperty

    @get:Input abstract var context: NativeModuleContext
    @get:Input abstract var targetFile: String

    @TaskAction
    fun action() {
        srcDir.get().asFile.fresh()

        KotlinStubPrinter(
            context = context,
            target = File(targetFile)
        )
    }
}