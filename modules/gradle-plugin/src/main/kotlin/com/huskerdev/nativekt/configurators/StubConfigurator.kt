package com.huskerdev.nativekt.configurators

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.huskerdev.nativekt.plugin.NativeKtExtension
import com.huskerdev.nativekt.plugin.NativeModule
import com.huskerdev.nativekt.printers.KotlinStubPrinter
import com.huskerdev.nativekt.utils.dependsOnReload
import com.huskerdev.nativekt.utils.dir
import com.huskerdev.nativekt.utils.fresh
import com.huskerdev.webidl.resolver.IdlResolver
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

internal fun configureStub(
    project: Project,
    commonTask: TaskProvider<*>?,
    extension: NativeKtExtension,
    idl: IdlResolver,
    module: NativeModule,
    sourceSet: KotlinSourceSet,
    srcRootDir: File
) {
    val srcDir = File(srcRootDir, "common")

    val classPathFile = File(srcDir, module.classPath.replace(".", "/"))

    val prepareTask = project.tasks.register(
        "prepareNatives${module.name.capitalized()}Common",
        PrepareNativesStub::class.java
    )
    prepareTask.get().also {
        it.srcDir.set(srcDir)
        it.inputs.dir(module.dir(project))

        it.idl = Json.encodeToString(idl)
        it.targetFile = File(classPathFile, "${module.name}.stub.kt").absolutePath
        it.moduleName = module.name
        it.moduleClasspath = module.classPath
        it.useCoroutines = extension.useCoroutines
    }
    if(commonTask != null)
        prepareTask.dependsOn(commonTask)
    prepareTask.dependsOnReload()

    sourceSet.kotlin.srcDir(prepareTask.flatMap { it.srcDir })
}

private abstract class PrepareNativesStub: DefaultTask() {
    @get:OutputDirectory
    abstract val srcDir: DirectoryProperty

    @get:Input abstract var idl: String
    @get:Input abstract var targetFile: String
    @get:Input abstract var moduleName: String
    @get:Input abstract var moduleClasspath: String
    @get:Input abstract var useCoroutines: Boolean

    init {
        doLast {
            srcDir.get().asFile.fresh()

            // Create stub sources
            KotlinStubPrinter(
                idl = Json.decodeFromString<IdlResolver>(idl),
                target = File(targetFile),
                classPath = moduleClasspath,
                moduleName = moduleName,
                useCoroutines = useCoroutines
            )
        }
    }
}