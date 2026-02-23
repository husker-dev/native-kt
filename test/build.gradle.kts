import com.huskerdev.nativekt.plugin.*
import org.apache.tools.ant.taskdefs.condition.Os
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
    useForeignApi = false

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

    when {
        Os.isFamily(Os.FAMILY_WINDOWS) -> {
            mingwX64()
        }
        Os.isFamily(Os.FAMILY_MAC) -> {
            macosArm64()
            macosX64()

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
        }
        Os.isFamily(Os.FAMILY_UNIX) -> {
            if(Os.isArch("amd64"))
                linuxX64()
            else linuxArm64()
        }
    }

    androidNativeX64()
    androidNativeX86()
    androidNativeArm32()
    androidNativeArm64()

    sourceSets.commonMain.dependencies {
        implementation(libs.kotlinx.coroutines)
        implementation(project(":native-kt-runtime"))
    }
    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
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
