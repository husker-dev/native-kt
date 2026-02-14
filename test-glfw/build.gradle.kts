@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.huskerdev.nativekt.plugin.Multiplatform
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)

    id("com.huskerdev.native-kt")
    id("maven-publish")
}

group = "com.huskerdev"
version = projectDir.parentFile.resolve("VERSION").readText()

native {
    useCoroutines = false
    useUniversalMacOSLib = false
    useJVMCI = true

    create("glfwBindings", Multiplatform::class)
}

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

    sourceSets.jvmMain.dependencies {
        implementation(project(":native-kt-runtime"))
    }
}
