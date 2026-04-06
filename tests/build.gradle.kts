@file:OptIn(ExperimentalWasmDsl::class)

import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JsMainFunctionExecutionMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.android)

    id("com.huskerdev.native-kt")
}

group = "com.huskerdev"
version = projectDir.parentFile.resolve("VERSION").readText()

kotlin {
    jvm {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                jvmTarget = JvmTarget.JVM_11
            }
        }
    }

    configure(listOf(
        wasmJs(), js()
    )) {
        browser()
        nodejs()

        compilerOptions {
            freeCompilerArgs.addAll("-Xes-long-as-bigint", "-XXLanguage:+JsAllowLongInExportedDeclarations")
            target = "es2015"
            main = JsMainFunctionExecutionMode.NO_CALL
        }
    }

    android {
        namespace = group.toString()
        minSdk = 25
        compileSdk {
            version = release(32)
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

            managedDevices {
                localDevices {
                    create("api32") {
                        device = "Pixel 6"
                        apiLevel = 32
                        systemImageSource = "aosp-atd"
                    }
                }
            }
        }

    }

    when {
        Os.isFamily(Os.FAMILY_WINDOWS) -> {
            mingwX64()
        }
        Os.isFamily(Os.FAMILY_MAC) -> {
            macosArm64()

            iosArm64()
            iosSimulatorArm64()

            watchosArm32()
            watchosArm64()
            watchosDeviceArm64()
            watchosSimulatorArm64()

            tvosArm64()
            tvosSimulatorArm64()
        }
        Os.isFamily(Os.FAMILY_UNIX) -> {
            if(Os.isArch("amd64"))
                linuxX64()
            else linuxArm64()
        }
    }

    sourceSets.commonMain.dependencies {
        implementation(libs.kotlinx.coroutines)
        implementation(project(":native-kt-runtime"))
    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
    }
    sourceSets.getByName("androidDeviceTest").dependencies {
        implementation(libs.androidx.test.runner)
        implementation(libs.androidx.test.ext.junit)
    }
}

natives {
    useJsBigInt = true
    useJvmRecord = false

    useForeignApi = false

    if(project.hasProperty("disableForeign")) {
        println("Foreign disabled")
        useForeignApi = false
    }

    create("test")
}


tasks.withType<Test>().configureEach {
    if (name.contains("jvm", ignoreCase = true)) {
        jvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
    }
}
