package com.huskerdev.nativekt.gradle

import com.huskerdev.nativekt.NativeKtModule
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.NativeModuleContext.NdlCompilationException
import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.createContext
import com.huskerdev.stdlibx.Arch
import com.huskerdev.stdlibx.OS
import com.huskerdev.stdlibx.fs.SyncPath
import org.gradle.api.Project
import org.gradle.api.Task


// Invoke task when reloading using IDEA
internal fun Task.dependsOnProjectReload() {
    project.rootProject.tasks
        .matching { it.name == "prepareKotlinBuildScriptModel" }
        .configureEach {
            dependsOn(this@dependsOnProjectReload)
        }
}

internal fun createContextWithProject(
    project: Project,
    module: NativeKtModule,
    extension: NativeKtExtension
): NativeModuleContext? {
    val buildDir = project.layout.buildDirectory.asFile.get()

    module.debug = extension.debug.toList()

    return try {
        createContext(
            buildDir = SyncPath(buildDir.absolutePath).resolve("generated/nativekt"),
            module = module,
            executor = GradleTaskExecutor(project),
            logger = GradlePluginLogger(project.logger)
        )
    } catch (e: NdlCompilationException) {
        e.errors.forEach {
            project.logger.error(it.message)
        }
        throw UnsupportedOperationException()
    }
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