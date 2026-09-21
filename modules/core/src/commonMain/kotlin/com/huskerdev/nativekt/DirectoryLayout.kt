package com.huskerdev.nativekt

import com.huskerdev.nativekt.utils.uppercaseFirstChar
import com.huskerdev.osutils.Arch
import com.huskerdev.osutils.OS
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.resolve

data class DirectoryLayout(
    val nativeSourcesDir: PlatformFile,
    val nativeBuildDir: PlatformFile,
    val kotlinSourcesDir: PlatformFile,
    val kotlinSourceFile: PlatformFile,
    val kotlinResourcesDir: PlatformFile?,
    val cinteropDir: PlatformFile?,
    val cinteropFile: PlatformFile?,
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
                    val arch = when {
                        OS.current == OS.MACOS && (context.configuration as NativeKtJvmConfiguration).useUniversalMacOSLib
                            -> "universal"
                        else -> Arch.current.name.lowercase()
                    }
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