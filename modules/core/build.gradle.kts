import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
}

group = "com.huskerdev"
version = projectDir.parentFile.parentFile.resolve("VERSION").readText().trim()

kotlin {
    jvm {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                jvmTarget = JvmTarget.JVM_11
            }
        }
    }

    macosArm64()
    mingwX64()
    linuxX64()
    linuxArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.webidl)
            api(libs.osutils)
            api(libs.filekit.get().toString()) {
                exclude(group = "androidx.annotation")
            }
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

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

project.afterEvaluate {
    file("src/commonMain/kotlin/com/huskerdev/nativekt/NativeKtInfo.kt").writeText("""
        package com.huskerdev.nativekt.plugin
        
        object NativeKtInfo {
            const val VERSION = "$version"
        }
    """.trimIndent().replace("\n", System.lineSeparator()))
}

mavenPublishing {
    publishToMavenCentral()

    if(project.hasProperty("sign"))
        signAllPublications()

    coordinates(group.toString(), "native-kt-core", version.toString())

    pom {
        name = "native-kt-core"
        description = "Core native-kt library"

        url = "https://github.com/husker-dev/native-kt"

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "husker-dev"
                name = "Nikita Shtengauer"
                email = "shtengauer.nikita@gmail.com"
            }
        }
        scm {
            connection = "https://github.com/husker-dev/native-kt.git"
            developerConnection = "https://github.com/husker-dev/native-kt.git"
            url = "https://github.com/husker-dev/native-kt"
        }
    }
}