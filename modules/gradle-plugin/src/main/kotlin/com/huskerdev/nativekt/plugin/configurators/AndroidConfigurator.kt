package com.huskerdev.nativekt.plugin.configurators

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.configurators.compileAndroid
import com.huskerdev.nativekt.configurators.prepareAndroid
import com.huskerdev.nativekt.plugin.*
import com.huskerdev.nativekt.utils.*
import io.github.vinceglb.filekit.PlatformFile
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.the
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import javax.inject.Inject


internal fun configureAndroidSourceSet(
    project: Project,
    context: NativeModuleContext,
    androidExtension: KotlinMultiplatformAndroidLibraryExtension,
    sourceSet: KotlinSourceSet,
    expectActual: Boolean
) {
    val layout = DirectoryLayout.of(context, TargetType.ANDROID)
    val androidComponents = project.the<KotlinMultiplatformAndroidComponentsExtension>()

    // ====================
    //     Prepare task
    // ====================

    val prepareTask = project.tasks.register(
        "prepareNatives${context.moduleName.upperCamelCase()}Android",
        PrepareNativesAndroid::class.java
    )
    prepareTask.get().also {
        it.inputs.dir(context.module.dir.file)
        it.inputs.file(context.module.resolveNdlFile().file)
        it.outputs.dirs(layout.kotlinSourcesDir.file, layout.nativeSourcesDir.file)

        it.context                  = context.serialize()
        it.useExpectActual          = expectActual
    }
    prepareTask.get().dependsOnProjectReload()

    project.afterEvaluate {
        val commonTask = project.tasks.findByName("prepareNatives${context.moduleName.upperCamelCase()}Common")
            ?: throw UnsupportedOperationException("[native-kt] Something went wrong with Android initialization")
        prepareTask.get().dependsOn(commonTask)
    }

    sourceSet.kotlin.srcDir(prepareTask.map { layout.kotlinSourcesDir.file })

    // ====================
    //   Compilation task
    // ====================

    val compileTask = project.tasks.register(
        "compileNatives${context.moduleName.upperCamelCase()}Android",
        CompileNativesAndroid::class.java
    )
    compileTask.get().also {
        it.inputs.dir(context.module.dir.file)
        it.inputs.file(context.module.resolveNdlFile().file)
        it.inputs.dir(layout.kotlinSourcesDir.file)
        it.inputs.dir(layout.nativeSourcesDir.file)
        it.outputs.dirs(layout.nativeBuildDir.file)

        it.outputFolder.set(layout.kotlinResourcesDir!!.file)

        it.context                = context.serialize()
        it.compileSdk             = androidExtension.compileSdk!!
        it.sdkDir                 = androidComponents.sdkComponents.sdkDirectory.get().asFile.absolutePath
    }
    compileTask.dependsOn(prepareTask)

    androidComponents.onVariants {
        it.sources.jniLibs?.addGeneratedSourceDirectory(
            compileTask,
            CompileNativesAndroid::outputFolder
        )
    }

    // =========================
    //  Apply critical stub lib
    // =========================

    val extension = context.configuration as NativeKtAndroidConfiguration
    if(extension.applyAndroidCriticalStub && extension.useAndroidCriticalNative) {
        sourceSet.dependencies {
            compileOnly("com.huskerdev:native-kt-android-critical-stub:1.0.0")
        }
    }
}

private abstract class PrepareNativesAndroid @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var context: String
    @get:Input abstract var useExpectActual: Boolean

    @TaskAction
    fun action() {
        prepareAndroid(
            context = NativeModuleContext.deserialize(
                json = context,
                executor = GradleTaskExecutor(execOps),
                logger = GradlePluginLogger(logger)
            ),
            useExpectActual
        )
    }
}

private abstract class CompileNativesAndroid @Inject constructor(
    private val execOps: ExecOperations
): DefaultTask() {
    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    @get:Input abstract var context: String
    @get:Input abstract var compileSdk: Int
    @get:Input abstract var sdkDir: String

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        compileAndroid(
            context = NativeModuleContext.deserialize(
                json = context,
                executor = GradleTaskExecutor(execOps),
                logger = GradlePluginLogger(logger)
            ),
            compileSdk = compileSdk,
            existingSdkDir = PlatformFile(sdkDir)
        )
    }
}