package com.huskerdev.nativekt

import com.huskerdev.nativekt.utils.uppercaseFirstChar
import com.huskerdev.stdlibx.Arch
import com.huskerdev.stdlibx.OS
import com.huskerdev.stdlibx.fs.SyncPath

data class DirectoryLayout(
    val nativeSourcesDir: SyncPath,
    val nativeBuildDir: SyncPath,
    val kotlinSourcesDir: SyncPath,
    val kotlinSourceFile: SyncPath,
    val kotlinResourcesDir: SyncPath?,
    val cinteropDir: SyncPath?,
    val cinteropFile: SyncPath?,
) {
    companion object {
        fun of(
            context: NativeModuleContext,
            targetType: TargetType?
        ): DirectoryLayout {
            val moduleName = context.moduleName
            val platformName = targetType?.kotlinTarget ?: "common"

            val kotlinResources = when(targetType) {
                TargetType.JVM,
                TargetType.JS,
                TargetType.ANDROID,
                TargetType.WASM_JS ->
                    context.buildDir.resolve("$moduleName/$platformName/resources")
                else -> null
            }

            val cinterop = when(targetType) {
                TargetType.JVM, TargetType.JS, TargetType.ANDROID, TargetType.WASM_JS, null -> null
                else -> context.buildDir.resolve("$moduleName/$platformName/cinterop")
            }
            val cinteropFile = cinterop?.resolve("cinterop.def")

            var nativeBuild = context.buildDir.resolve("$moduleName/$platformName/build")
            when(targetType) {
                TargetType.JVM -> {
                    val platformName = when(OS.current) {
                        OS.WINDOWS -> "windows"
                        OS.MACOS -> "macos"
                        OS.LINUX -> "linux"
                        else -> throw UnsupportedOperationException()
                    }
                    val arch = Arch.current.name.lowercase()

                    nativeBuild = nativeBuild.resolve("${platformName}${arch.uppercaseFirstChar()}")
                }
                else -> Unit
            }

            val kotlinSourceDir = context.buildDir.resolve("$moduleName/$platformName/kotlin")

            val classPath = context.classPath.replace(".", "/")
            val kotlinSourceFile = kotlinSourceDir.resolve("$classPath/${moduleName}.$platformName.kt")

            return DirectoryLayout(
                nativeSourcesDir = context.buildDir.resolve("$moduleName/$platformName/natives"),
                nativeBuildDir = nativeBuild,
                kotlinSourcesDir = kotlinSourceDir,
                kotlinSourceFile = kotlinSourceFile,
                kotlinResourcesDir = kotlinResources,
                cinteropDir = cinterop,
                cinteropFile = cinteropFile
            )
        }
    }
}