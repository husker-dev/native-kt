@file:OptIn(ExperimentalWasmDsl::class)

import com.huskerdev.nativekt.plugin.*
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.android)

    id("com.huskerdev.native-kt")
}

group = "com.huskerdev"
version = "1.0"

kotlin {
    jvmToolchain {
        vendor = JvmVendorSpec.GRAAL_VM
        languageVersion = JavaLanguageVersion.of(23)
    }

    jvm {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                jvmTarget = JvmTarget.JVM_11
            }
        }
    }

    webTargets {
        browser()
        nodejs()

        compilerOptions {
            freeCompilerArgs.addAll("-Xes-long-as-bigint", "-XXLanguage:+JsAllowLongInExportedDeclarations")
            target = "es2015"
            main = JsMainFunctionExecutionMode.NO_CALL
        }
    }

    currentNativeTargets()

    android {
        namespace = group.toString()
        minSdk = 32
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

    sourceSets.commonMain.dependencies {
        implementation(libs.kotlinx.coroutines)
        implementation(project(":modules:runtime"))
    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
    }
    sourceSets.androidMain.dependencies {
        compileOnly(project(":modules:android-critical-stub"))
    }
    sourceSets.getByName("androidDeviceTest").dependencies {
        implementation(libs.androidx.test.runner)
        implementation(libs.androidx.test.ext.junit)
    }
}

natives {
    applyRuntime = false
    applyAndroidCriticalStub = false

    useJsBigInt = true
    useJvmRecord = false

    if(project.hasProperty("disableForeign")) {
        println("Disable: Foreign")
        useForeignApi = false
    }

    if(project.hasProperty("disableJVMCI")) {
        println("Disable: JVMCI")
        useJVMCI = false
    }

    create("test")
    create("testrs") {
        cargo()
    }
}


tasks.withType<Test>().configureEach {
    if (name.contains("jvm", ignoreCase = true)) {
        jvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
    }
    testLogging {
        showStandardStreams = true
    }
}
