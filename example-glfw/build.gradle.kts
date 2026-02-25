@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)

    id("com.huskerdev.native-kt")
    id("maven-publish")
}

group = "com.huskerdev"
version = projectDir.parentFile.resolve("VERSION").readText()

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
    useJVMCI = true

    create("glfwBindings")
}