
plugins {
    alias(libs.plugins.plugin.publish)
    `kotlin-dsl`
}

group = "com.huskerdev"
version = projectDir.parentFile.parentFile.resolve("VERSION").readText()

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlinx.serialization)
    implementation(libs.webidl)
    compileOnly(libs.android.tools)
}

tasks.jar {
    archiveBaseName = "native-kt-plugin"
}

gradlePlugin {
    website = "https://github.com/husker-dev/native-kt"
    vcsUrl = "https://github.com/husker-dev/native-kt"
    plugins {
        create("native-kt") {
            id = "com.huskerdev.native-kt"
            implementationClass = "com.huskerdev.nativekt.plugin.NativeKtPlugin"
            displayName = "native-kt"
            description = "Gradle plugin for convenient C/C++ integration into a Kotlin Multiplatform project."
            tags.set(listOf("kotlin", "multiplatform", "native"))
        }
    }
}

project.afterEvaluate {
    file("src/main/kotlin/com/huskerdev/nativekt/plugin/NativeKtInfo.kt").writeText("""
        package com.huskerdev.nativekt.plugin
        
        object NativeKtInfo {
            const val VERSION = "$version"
        }
    """.trimIndent().replace("\n", System.lineSeparator()))
}