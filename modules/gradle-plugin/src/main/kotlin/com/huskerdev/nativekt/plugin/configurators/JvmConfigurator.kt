package com.huskerdev.nativekt.plugin.configurators

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.configurators.compileJvm
import com.huskerdev.nativekt.configurators.prepareJvm
import com.huskerdev.nativekt.plugin.JarTaskContainer
import com.huskerdev.nativekt.plugin.LOCAL_RUN_CONFIGURATION
import com.huskerdev.nativekt.plugin.NATIVE_TASK_GROUP
import com.huskerdev.nativekt.plugin.dependsOnProjectReload
import com.huskerdev.nativekt.utils.*
import com.huskerdev.osutils.Arch
import com.huskerdev.osutils.OS
import io.github.vinceglb.filekit.resolve
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import javax.inject.Inject


private val JNI_INCLUDE_FILES = listOf(
    "darwin/jawt_md.h",
    "darwin/jni_md.h",
    "linux/jawt_md.h",
    "linux/jni_md.h",
    "win32/jawt_md.h",
    "win32/jni_md.h",
    "win32/bridge/AccessBridgeCallbacks.h",
    "win32/bridge/AccessBridgeCalls.h",
    "win32/bridge/AccessBridgePackages.h",
    "classfile_constants.h",
    "jawt.h",
    "jdwpTransport.h",
    "jni.h",
    "jvmti.h",
    "jvmticmlr.h"
).associateWith { "/com/huskerdev/nativekt/include/$it" }

internal fun configureJvm(
    project: Project,
    commonTask: TaskProvider<*>?,
    context: NativeModuleContext,
    jarTaskContainer: JarTaskContainer,
    sourceSet: KotlinSourceSet,
    expectActual: Boolean
) {
    val layout = DirectoryLayout.of(context, TargetType.JVM)
    val extension = context.configuration as NativeKtJvmConfiguration

    if(!extension.useJNI && !extension.useForeignApi && !extension.useJVMCI)
        throw UnsupportedOperationException("All JVM native implementation are disabled (JNI, Foreign, JVMCI)")

    if(project.configurations.findByName(LOCAL_RUN_CONFIGURATION) == null) {
        project.configurations.create(LOCAL_RUN_CONFIGURATION) {
            isCanBeConsumed = false
            isCanBeResolved = true
        }.apply {
            // multi-target
            project.configurations.findByName("jvmRuntimeClasspath")?.extendsFrom(this)
            project.configurations.findByName("jvmTestRuntimeClasspath")?.extendsFrom(this)
            project.configurations.findByName("jvmMainRuntimeClasspath")?.extendsFrom(this)

            // jvm-only
            project.configurations.findByName("runtimeClasspath")?.extendsFrom(this)
            project.configurations.findByName("testRuntimeClasspath")?.extendsFrom(this)
            project.configurations.findByName("mainRuntimeClasspath")?.extendsFrom(this)
        }
    }

    val libArch = when (OS.current) {
        OS.MACOS if extension.useUniversalMacOSLib -> "universal"
        else -> Arch.current.name.lowercase()
    }

    val platformName = when(OS.current) {
        OS.WINDOWS -> "windows"
        OS.MACOS -> "macos"
        OS.LINUX -> "linux"
        else -> throw UnsupportedOperationException()
    }

    // ====================
    //     Prepare task
    // ====================

    val prepareTask = project.tasks.register(
        "prepareNatives${context.moduleName.upperCamelCase()}Jvm",
        PrepareNativesJvm::class.java
    )
    prepareTask.get().let {
        it.inputs.dir(context.module.dir.file)
        it.inputs.file(context.module.resolveNdlFile().file)
        it.outputs.dirs(layout.kotlinSourcesDir.file, layout.nativeSourcesDir.file)

        it.context         = context.serialize()
        it.useExpectActual = expectActual
    }
    if(commonTask != null)
        prepareTask.get().dependsOn(commonTask)
    prepareTask.get().dependsOnProjectReload()

    sourceSet.kotlin.srcDirs(prepareTask.map { layout.kotlinSourcesDir.file })

    // ====================
    //   Compilation task
    // ====================

    val compileTask = project.tasks.register(
        "compileNatives${context.moduleName.upperCamelCase()}Jvm",
        CompileNativesJvm::class.java
    )
    compileTask.get().let {
        it.inputs.dir(context.module.dir.file)
        it.inputs.file(context.module.resolveNdlFile().file)
        it.inputs.dir(layout.kotlinSourcesDir.file)
        it.inputs.dir(layout.nativeSourcesDir.file)
        it.outputs.dirs(layout.nativeBuildDir.file, layout.kotlinResourcesDir!!.file)

        it.context = context.serialize()
    }
    compileTask.get().dependsOn(prepareTask)

    // Pack task
    val packNativeJar = project.tasks.findByName("packNativesJvm") as Jar?
        ?: project.tasks.register("packNativesJvm", Jar::class.java) {
            group = NATIVE_TASK_GROUP
            archiveAppendix.set("jvm")
            archiveClassifier.set("$platformName-$libArch")

            project.dependencies.add(LOCAL_RUN_CONFIGURATION, project.files(this@register))
        }.get()

    packNativeJar.dependsOn(compileTask)
    packNativeJar.from(layout.kotlinResourcesDir!!.file)

    jarTaskContainer.jvmNativesJarTask = packNativeJar
}

private abstract class PrepareNativesJvm @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var context: String
    @get:Input abstract var useExpectActual: Boolean

    @TaskAction
    fun action() {
        val context = NativeModuleContext.deserialize(
            json = context,
            executor = GradleTaskExecutor(execOps),
            logger = GradlePluginLogger(logger)
        )
        val layout = DirectoryLayout.of(context, TargetType.JVM)

        prepareJvm(context, useExpectActual)

        // unpack jni headers
        if ((context.configuration as NativeKtJvmConfiguration).useJNI) {
            val includeDir = File(layout.nativeSourcesDir.file, "jni/include")
            if (!includeDir.exists()) {
                JNI_INCLUDE_FILES.forEach { (name, path) ->
                    this::class.java.getResourceAsStream(path).use { ins ->
                        if (ins == null)
                            throw NullPointerException("Can not find header: $name")
                        val file = File(includeDir, name)
                        file.parentFile.mkdirs()
                        file.outputStream().use { ins.copyTo(it) }
                    }
                }
            }
        }
    }
}

private abstract class CompileNativesJvm @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var context: String

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        val context = NativeModuleContext.deserialize(
            json = context,
            executor = GradleTaskExecutor(execOps),
            logger = GradlePluginLogger(logger)
        )
        val layout = DirectoryLayout.of(context, TargetType.JVM)

        compileJvm(
            context = context,
            existingJdkIncludeDir = layout.nativeSourcesDir.resolve("jni/include")
        )
    }
}