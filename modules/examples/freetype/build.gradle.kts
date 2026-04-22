@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.targets.js.npm.LockFileMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.npm.NpmExtension

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
        macosArm64(),
        linuxX64(), linuxArm64()
    ).forEach {
        it.binaries {
            executable {
                entryPoint = "main"
            }
        }
    }

    js {
        nodejs()
        browser()
        binaries.executable()
        compilerOptions {
            target = "es2015"
            moduleKind = JsModuleKind.MODULE_COMMONJS
        }
        // Disable npm package-lock file errors
        rootProject.the<NpmExtension>().apply {
            packageLockMismatchReport = LockFileMismatchReport.NONE
            reportNewPackageLock = true
            packageLockAutoReplace = true
        }
    }

    sourceSets.commonMain.dependencies {
        implementation(project(":modules:runtime"))
    }
}

natives {
    applyRuntime = false
    useCoroutines = false
    useUniversalMacOSLib = false

    create("freetypeBindings")
}