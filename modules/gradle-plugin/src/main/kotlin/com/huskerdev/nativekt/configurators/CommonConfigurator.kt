package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.printers.kotlin.KotlinCommonPrinter
import com.huskerdev.nativekt.utils.dependsOnProjectReload
import com.huskerdev.nativekt.utils.fresh
import com.huskerdev.nativekt.utils.upperCamelCase
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File


internal fun configureCommon(
    project: Project,
    context: NativeModuleContext,
    sourceSet: KotlinSourceSet
): TaskProvider<*> {
    val srcDir = File(context.srcGenDir, "common")

    val classPathFile = File(srcDir, context.classPath.replace(".", "/"))

    val prepareTask = project.tasks.register(
        "prepareNatives${context.moduleName.upperCamelCase()}Common",
        PrepareNativesCommon::class.java
    )
    prepareTask.get().let {
        it.srcDir.set(srcDir)
        it.inputs.dir(context.module.dir)

        it.context = context
        it.targetFile = File(classPathFile, "${context.moduleName}.kt").absolutePath
    }
    prepareTask.get().dependsOnProjectReload()
    sourceSet.kotlin.srcDir(prepareTask.map { it.srcDir })

    return prepareTask
}

private abstract class PrepareNativesCommon: DefaultTask() {
    @get:OutputDirectory
    abstract val srcDir: DirectoryProperty

    @get:Input abstract var context: NativeModuleContext
    @get:Input abstract var targetFile: String

    @TaskAction
    fun action() {
        srcDir.get().asFile.fresh()
        KotlinCommonPrinter(
            context = context,
            target = File(targetFile)
        )
    }
}