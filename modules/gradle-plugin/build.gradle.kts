import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.plugin.publish)
    `kotlin-dsl`
}

group = "com.huskerdev"
version = projectDir.parentFile.parentFile.resolve("VERSION").readText().trim()

repositories {
    mavenCentral()
    google()
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.webidl)
    implementation(libs.osutils)
    implementation(libs.filekit)

    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlinx.serialization)

    compileOnly(libs.android.gradle)
    implementation("$group:core")
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
            description = "Gradle plugin for convenient C/C++/Rust integration into a Kotlin Multiplatform project."
            tags.set(listOf("kotlin", "multiplatform", "native"))
        }
    }
}