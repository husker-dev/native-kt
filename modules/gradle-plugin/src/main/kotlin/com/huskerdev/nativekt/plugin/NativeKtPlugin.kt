package com.huskerdev.nativekt.plugin

import com.huskerdev.nativekt.TargetType
import com.huskerdev.osutils.Arch
import com.huskerdev.osutils.OS
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import java.io.File

internal const val NATIVE_TASK_GROUP = "natives"
internal const val RUNTIME_DEPENDENCY = "com.huskerdev:native-kt-runtime:${NativeKtInfo.VERSION}"

@Suppress("unused")
class NativeKtPlugin: Plugin<Project> {

    /**
     * Here we wait for various plugins to load.
     * The Android plugin is expected to load after Kotlin,
     * so it uses the existing 'extension'.
     */
    override fun apply(project: Project) {
        var extension: ExtensiblePolymorphicDomainObjectContainer<*>? = null

        // Android (9.0.0+)
        project.plugins.withId("com.android.kotlin.multiplatform.library") {
            configureAndroid(project) { extension }
        }

        // Kotlin/Multiplatform
        project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            extension = project.extensions.create("natives", NativeKtMultiplatformExtension::class.java, project.projectDir)
            configureKotlin(project, extension!!)
        }

        // Kotlin/JVM
        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            extension = project.extensions.create("natives", NativeKtJvmExtension::class.java, project.projectDir)
            configureKotlin(project, extension!!)
        }

        // Kotlin/JS (Legacy)
        project.plugins.withId("org.jetbrains.kotlin.js") {
            extension = project.extensions.create("natives", NativeKtJsExtension::class.java, project.projectDir)
            configureKotlin(project, extension!!)
        }

        /**
         * This block is required for the native-kt IDEA plugin.
         * It's an easy way to let the plugin know which
         * native modules are loaded in the Gradle project.
         */
        project.afterEvaluate {
            File(project.layout.buildDirectory.get().asFile, "nativekt.txt").apply {
                val content = extension?.joinToString(separator = "\n") {
                    (it as NativeProject).projectDir.absolutePath
                } ?: ""

                if(content.isNotEmpty()) {
                    parentFile.mkdirs()
                    writeText(content)
                } else if(exists())
                    delete()
            }
        }
    }
}


/**
 * Loads JS and WASM (web)
 */
@OptIn(ExperimentalWasmDsl::class)
@Suppress("unused")
fun KotlinMultiplatformExtension.webTargets(
    configure: KotlinJsTargetDsl.() -> Unit = {}
) {
    wasmJs(configure)
    js(configure)
}


/**
 * Loads Kotlin/Native desktop target for
 * the current platform: MinGW, Linux or macOS.
 */
@Suppress("unused")
fun KotlinMultiplatformExtension.currentNativeDesktopTargets(
    configure: KotlinNativeTarget.() -> Unit = {}
) = currentNativeTargets(listOf(
    TargetType.MINGW_X64,
    TargetType.MACOS_ARM64,
    TargetType.LINUX_X64,
    TargetType.LINUX_ARM64
), configure)

/**
 * Loads all Kotlin/Native targets supported by
 * the current platform (except Android native).
 */
fun KotlinMultiplatformExtension.currentNativeTargets(
    available: List<TargetType> = listOf(
        TargetType.MINGW_X64,
        TargetType.MACOS_ARM64,
        TargetType.IOS_ARM64,
        TargetType.IOS_SIMULATOR_ARM64,
        TargetType.WATCHOS_ARM32,
        TargetType.WATCHOS_ARM64,
        TargetType.WATCHOS_DEVICE_ARM64,
        TargetType.WATCHOS_SIMULATOR_ARM64,
        TargetType.TVOS_ARM64,
        TargetType.TVOS_SIMULATOR_ARM64,
        TargetType.LINUX_X64,
        TargetType.LINUX_ARM64
    ),
    configure: KotlinNativeTarget.() -> Unit = {}
) {
    when(OS.current) {
        OS.WINDOWS -> when {
            TargetType.MINGW_X64 in available -> mingwX64(configure)
        }
        OS.MACOS -> {
            if(TargetType.MACOS_ARM64 in available)             macosArm64(configure)
            if(TargetType.IOS_ARM64 in available)               iosArm64(configure)
            if(TargetType.IOS_SIMULATOR_ARM64 in available)     iosSimulatorArm64(configure)
            if(TargetType.WATCHOS_ARM32 in available)           watchosArm32(configure)
            if(TargetType.WATCHOS_ARM64 in available)           watchosArm64(configure)
            if(TargetType.WATCHOS_DEVICE_ARM64 in available)    watchosDeviceArm64(configure)
            if(TargetType.WATCHOS_SIMULATOR_ARM64 in available) watchosSimulatorArm64(configure)
            if(TargetType.TVOS_ARM64 in available)              tvosArm64(configure)
            if(TargetType.TVOS_SIMULATOR_ARM64 in available)    tvosSimulatorArm64(configure)
        }
        OS.LINUX -> when (Arch.current) {
            Arch.X64 if TargetType.LINUX_X64 in available -> linuxX64(configure)
            Arch.ARM64 if TargetType.LINUX_ARM64 in available -> linuxArm64(configure)
            else -> Unit
        }
        else -> throw UnsupportedOperationException()
    }
}