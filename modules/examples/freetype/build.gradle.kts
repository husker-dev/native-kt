@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.huskerdev.nativekt.plugin.currentNativeDesktopTargets
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

    currentNativeDesktopTargets {
        binaries {
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
    useCoroutines = false
    useUniversalMacOSLib = false

    // Remove auto runtime applying to use local version
    applyRuntime = false

    create("freetypeBindings")
}