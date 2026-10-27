@file:Suppress("UnstableApiUsage")

import com.huskerdev.nativekt.gradle.*
import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.gradle.configurators.PrepareNativesCommon
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

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
                jvmTarget = JvmTarget.JVM_23
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
        namespace = "$group.tests"
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

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines)
            implementation(project(":modules:runtime"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            compileOnly(project(":modules:android-critical-stub"))
        }
        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.test.ext.junit)
        }
    }
}

natives {
    debug += listOf(
        DebugKind.PRINT_LINKER_OPTIONS,
        DebugKind.PRINT_EXEC
    )

    applyRuntime = false
    applyAndroidCriticalStub = false

    var printedInfo = false
    fun addTest(path: String, configure: NativeKtModule.() -> Unit = {}) {
        addFile(file("$path/nativekt.toml")) {
            js.target = if(project.hasProperty("useNode"))
                JsTarget.NODE else JsTarget.WEB
            jvm.useJNI = true
            jvm.useForeignApi = !project.hasProperty("disableForeign")
            jvm.useJVMCI = !project.hasProperty("disableJVMCI")

            if(!printedInfo) {
                printedInfo = true
                logger.info("Using JS/Wasm target: ${js.target}")
                if(!jvm.useForeignApi)
                    logger.info("Disable: Foreign")
                if(!jvm.useJVMCI)
                    logger.info("Disable: JVMCI")
            }
            configure()
        }
    }

    // Add basic tests (C/C++/Rust)
    addTest("natives/test")
    addTest("natives/testcpp")
    addTest("natives/testrs")

    // Add micro test cases
    for(i in 0 until 7) {
        addTest("natives/cases/rust/tc_${i}")
        addTest("natives/cases/c/tc_${i}")
        addTest("natives/cases/cpp/tc_${i}")
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

tasks.withType<AbstractTestTask>().configureEach {
    outputs.cacheIf { false }
    outputs.upToDateWhen { false }
}

tasks.withType<KotlinNativeLink>().configureEach {
    outputs.cacheIf { false }
    outputs.upToDateWhen { false }
}

gradle.taskGraph.whenReady {
    if(allTasks.any { it is NativeKtCompileTask })
        allTasks.first { it is NativeKtPrepareTask }
            .dependsOn(":modules:cli:packAll")
}