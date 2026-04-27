package com.huskerdev.nativekt.plugin

import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.utils.dir
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import java.io.File

internal const val NATIVE_TASK_GROUP = "natives"

class NativeKtPlugin: Plugin<Project> {
    lateinit var project: Project
    lateinit var extension: ExtensiblePolymorphicDomainObjectContainer<*>

    val kotlin: KotlinProjectExtension
        get() = project.the<KotlinProjectExtension>()

    override fun apply(project: Project) {
        this.project = project

        val buildDir = project.layout.buildDirectory.get().asFile
        val cmakeDir = File(buildDir, "cmake")
        val srcGenDir = File(buildDir, "generated/natives")

        project.plugins.withId("com.android.kotlin.multiplatform.library") {
            configureAndroid(cmakeDir, srcGenDir)
        }

        project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            extension = project.extensions.create("natives", NativeKtMultiplatformExtension::class.java)
            configureKotlin(cmakeDir, srcGenDir)
        }

        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            extension = project.extensions.create("natives", NativeKtJvmExtension::class.java)
            configureKotlin(cmakeDir, srcGenDir)
        }

        project.plugins.withId("org.jetbrains.kotlin.js") {
            extension = project.extensions.create("natives", NativeKtJsExtension::class.java)
            configureKotlin(cmakeDir, srcGenDir)
        }

        project.afterEvaluate {
            val content = extension.joinToString(separator = "\n") {
                (it as NativeModule).dir(project).absolutePath
            }
            val file = File(project.layout.buildDirectory.get().asFile, "nativekt.txt")

            if(content.isNotEmpty()) {
                file.parentFile.mkdirs()
                file.writeText(content)
            } else if(file.exists())
                file.delete()
        }
    }
}

@OptIn(ExperimentalWasmDsl::class)
@Suppress("unused")
fun KotlinMultiplatformExtension.webTargets(
    configure: KotlinJsTargetDsl.() -> Unit = {}
) {
    wasmJs(configure)
    js(configure)
}

@Suppress("unused")
fun KotlinMultiplatformExtension.currentNativeDesktopTargets(
    configure: KotlinNativeTarget.() -> Unit = {}
) = currentNativeTargets(listOf(
    TargetType.MINGW_X64,
    TargetType.MACOS_ARM64,
    TargetType.LINUX_X64,
    TargetType.LINUX_ARM64
), configure)


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
    when {
        Os.isFamily(Os.FAMILY_WINDOWS) -> when {
            TargetType.MINGW_X64 in available -> mingwX64(configure)
        }
        Os.isFamily(Os.FAMILY_MAC) -> when {
            TargetType.MACOS_ARM64 in available -> macosArm64(configure)
            TargetType.IOS_ARM64 in available -> iosArm64(configure)
            TargetType.IOS_SIMULATOR_ARM64 in available -> iosSimulatorArm64(configure)
            TargetType.WATCHOS_ARM32 in available -> watchosArm32(configure)
            TargetType.WATCHOS_ARM64 in available -> watchosArm64(configure)
            TargetType.WATCHOS_DEVICE_ARM64 in available -> watchosDeviceArm64(configure)
            TargetType.WATCHOS_SIMULATOR_ARM64 in available -> watchosSimulatorArm64(configure)
            TargetType.TVOS_ARM64 in available -> tvosArm64(configure)
            TargetType.TVOS_SIMULATOR_ARM64 in available -> tvosSimulatorArm64(configure)
        }
        Os.isFamily(Os.FAMILY_UNIX) -> when {
            Os.isArch("amd64") && TargetType.LINUX_X64 in available -> linuxX64(configure)
            !Os.isArch("amd64") && TargetType.LINUX_ARM64 in available -> linuxArm64(configure)
        }
    }
}