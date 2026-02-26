@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

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
            }
        }
    }

    setOf(
        mingwX64(),
        macosArm64(), macosX64(),
        linuxX64(), linuxArm64()
    ).forEach {
        it.binaries {
            executable {
                if(it == mingwX64())
                    linkerOpts += "-mwindows"
                entryPoint = "main"
            }
        }
    }

    sourceSets.commonMain.dependencies {
        implementation(project(":native-kt-runtime"))
    }
}

native {
    useCoroutines = false
    useUniversalMacOSLib = false

    create("glfwBindings")
}