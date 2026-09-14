@file:Suppress("UnstableApiUsage")

import com.huskerdev.nativekt.plugin.*
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

private fun NativeProject.configureNodeJsTests() {
    gradle.taskGraph.whenReady {
        gradle.taskGraph.allTasks.forEach {
            if(it.name == "jsNodeTest" || it.name == "wasmJsNodeTest")
                jsTarget = JsTarget.NODE
        }
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

    // Configure modules
    val commonNdl = file("natives/api.ndl")
    create("test") {
        cmake(Language.C)
        ndlFile = commonNdl
        configureNodeJsTests()
    }
    create("testcpp") {
        cmake(Language.CPP)
        ndlFile = commonNdl
        configureNodeJsTests()
    }
    create("testrs") {
        cargo()
        ndlFile = commonNdl
        configureNodeJsTests()
    }

    // Test cases
    val casesDir = file("natives/cases")
    for(i in 0 until 7) {
        val ndl = File(casesDir, "tc_$i.ndl")
        create("tc_${i}_rust") {
            ndlFile = ndl
            projectDir = File(casesDir, "rust/tc_$i")
            cargo()
        }
        create("tc_${i}_c") {
            ndlFile = ndl
            projectDir = File(casesDir, "c/tc_$i")
            cmake()
        }
        create("tc_${i}_cpp") {
            ndlFile = ndl
            projectDir = File(casesDir, "cpp/tc_$i")
            cmake(Language.CPP)
        }
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