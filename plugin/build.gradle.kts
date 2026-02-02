
plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

group = "com.huskerdev"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.android.tools)
    implementation("com.huskerdev:webidl-kt:1.0.1")
}

gradlePlugin {
    plugins {
        plugins.create("native-kt") {
            id = name
            implementationClass = "com.huskerdev.nativekt.plugin.NativeKtPlugin"
        }
    }
}