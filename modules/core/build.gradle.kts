plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.huskerdev"
version = projectDir.parentFile.parentFile.resolve("VERSION").readText().trim()

kotlin {
    jvm()
    macosArm64()
    mingwX64()
    linuxX64()
    linuxArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.webidl)
            implementation(libs.osutils)
            implementation(libs.filekit)
            implementation(libs.envvar)

            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.io)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

project.afterEvaluate {
    file("src/commonMain/kotlin/com/huskerdev/nativekt/NativeKtInfo.kt").writeText("""
        package com.huskerdev.nativekt.plugin
        
        object NativeKtInfo {
            const val VERSION = "$version"
        }
    """.trimIndent().replace("\n", System.lineSeparator()))
}