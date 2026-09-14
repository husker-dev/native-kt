@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.huskerdev.nativekt.plugin.currentNativeDesktopTargets
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.huskerdev.native-kt")
}

group = "com.huskerdev"
version = "1.0.0"

kotlin {
    jvm {
        binaries {
            executable {
                mainClass = "MainKt"
                if(Os.isFamily(Os.FAMILY_MAC))
                    applicationDefaultJvmArgs.add("-XstartOnFirstThread")
            }
        }
    }

    currentNativeDesktopTargets {
        binaries {
            executable {
                entryPoint = "main"
                if(this@currentNativeDesktopTargets.konanTarget == KonanTarget.MINGW_X64)
                    linkerOpts += "-mwindows"
            }
        }
    }

    sourceSets.commonMain.dependencies {
        implementation(project(":modules:runtime"))
    }
}

natives {
    useCoroutines = false

    // Remove auto runtime applying to use local version
    applyRuntime = false

    create("glfwBindings")
}