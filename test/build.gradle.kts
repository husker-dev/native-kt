import com.huskerdev.nativekt.plugin.*
import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.android)

    id("com.huskerdev.native-kt")
    id("maven-publish")
}

group = "com.huskerdev"
version = projectDir.parentFile.resolve("VERSION").readText()

native {
    ndkVersion = "29.0.14206865"
    useJVMCI = true

    create("test", Multiplatform::class)
}

kotlin {
    jvm {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                jvmTarget = JvmTarget.JVM_11
            }
        }
    }

    js {
        browser()
        nodejs()

        compilerOptions {
            target = "es2015"
            moduleKind = JsModuleKind.MODULE_COMMONJS
            main = JsMainFunctionExecutionMode.NO_CALL
        }
    }

    android {
        namespace = group.toString()
        minSdk = 25
        compileSdk {
            version = release(32)
        }
        withDeviceTest { }
    }

    mingwX64()

    macosArm64()
    macosX64()

    linuxX64()
    linuxArm64()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    watchosX64()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()

    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()

    androidNativeX64()
    androidNativeX86()
    androidNativeArm32()
    androidNativeArm64()

    sourceSets.commonMain.dependencies {
        implementation(libs.kotlinx.coroutines)
    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
    }
    sourceSets.jvmMain.dependencies {
        implementation(project(":native-kt-runtime"))
    }
    sourceSets.getByName("androidDeviceTest").dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.androidx.test.runner)
    }
}

tasks.withType<Test>().configureEach {
    if (name.contains("jvm", ignoreCase = true)) {
        jvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
    }
}
