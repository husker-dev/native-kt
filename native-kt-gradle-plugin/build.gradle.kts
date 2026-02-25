
plugins {
    alias(libs.plugins.plugin.publish)
    `kotlin-dsl`
}

version = projectDir.parentFile.resolve("VERSION").readText()
group = "com.huskerdev"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.tools)
    implementation(libs.webidl)
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