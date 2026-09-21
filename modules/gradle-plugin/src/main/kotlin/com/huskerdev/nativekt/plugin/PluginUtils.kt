package com.huskerdev.nativekt.plugin

import com.huskerdev.nativekt.GradlePluginLogger
import com.huskerdev.nativekt.GradleTaskExecutor
import com.huskerdev.nativekt.NativeKtConfiguration
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.NativeProject
import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.createContext
import com.huskerdev.nativekt.plugin.tasks.InitTask
import com.huskerdev.nativekt.utils.upperCamelCase
import com.huskerdev.osutils.Arch
import com.huskerdev.osutils.OS
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.resolve
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty


internal fun Task.dependsOnProjectReload() {
    // Invoke task when reloading using IDEA
    project.rootProject.tasks
        .matching { it.name == "prepareKotlinBuildScriptModel" }
        .configureEach {
            dependsOn(this@dependsOnProjectReload)
        }
}

internal fun createContextWithProject(
    project: Project,
    extension: ExtensiblePolymorphicDomainObjectContainer<*>,
    module: NativeProject,
    muteError: Boolean
): NativeModuleContext? {
    val buildDir = project.layout.buildDirectory.asFile.get()
    val configuration = extension.cleanWrapper()

    val context = createContext(
        buildDir = PlatformFile(buildDir).resolve("generated/nativekt"),
        configuration = configuration,
        module = module,
        executor = GradleTaskExecutor(project),
        logger = GradlePluginLogger(project.logger)
    )

    val initTaskName = "init${module.name.upperCamelCase()}"
    val initTask = project.tasks.findByName(initTaskName)
        ?: project.tasks.register(initTaskName, InitTask::class.java).get().also {
            it.configuration = configuration
            it.module = module
            it.buildDir = buildDir.absolutePath
        }
    project.gradle.taskGraph.whenReady {
        if (context == null && !muteError && !hasTask(initTask)) {
            project.logger.error("""
                Native module '${module.name}' is not loaded:
                  'api.ndl' file not found.
                
                To initialize module: 
                  ./gradlew ${initTask.path}
            """.trimIndent())
        }
    }
    return context
}

internal fun currentNativeTargetType(): TargetType = when(OS.current) {
    OS.WINDOWS -> TargetType.MINGW_X64
    OS.MACOS -> when(Arch.current) {
        Arch.ARM64 -> TargetType.MACOS_ARM64
        else -> TargetType.MACOS_X64
    }
    OS.LINUX -> when(Arch.current) {
        Arch.ARM64 -> TargetType.LINUX_ARM64
        else -> TargetType.LINUX_X64
    }
    else -> throw UnsupportedOperationException()
}

internal fun ExtensiblePolymorphicDomainObjectContainer<*>.cleanWrapper(): NativeKtConfiguration = when(this) {
    is NativeKtMultiplatformExtension -> impl
    is NativeKtJvmExtension -> impl
    is NativeKtJsExtension -> impl
    else -> throw UnsupportedOperationException()
}

class JavaFile<T: PlatformFile?>(val file: KMutableProperty<T>): ReadWriteProperty<Any, File?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): File =
        file.getter.call()!!.file

    override fun setValue(thisRef: Any, property: KProperty<*>, value: File?) =
        file.setter.call(value?.run { PlatformFile(this) })
}