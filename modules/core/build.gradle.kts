import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import kotlin.io.writeText

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
}

group = "com.huskerdev"
version = projectDir.parentFile.parentFile.resolve("VERSION").readText().trim()

val genInfoTask = tasks.register("generateInfo") {
    description = "Generates nativekt info file"
    outputs.dir(layout.buildDirectory.get().asFile.resolve("generated/nativekt-core"))

    doLast {
        val file = outputs.files.files.first().resolve("com/huskerdev/nativekt/NativeKtInfo.kt")
        file.parentFile.mkdirs()
        file.writeText("""
            package com.huskerdev.nativekt
            
            object NativeKtInfo {
                const val VERSION = "$version"
            }
        """.trimIndent())
    }
}

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
        commonMain {
            kotlin.srcDirs(genInfoTask.map { it.outputs })
            dependencies {
                api(libs.stdlibx.fs)
                api(libs.webidl)
                implementation(libs.ktoml)

                implementation(libs.kotlinx.serialization)
                implementation(libs.kotlinx.coroutines)
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

mavenPublishing {
    publishToMavenCentral()

    if(project.hasProperty("sign"))
        signAllPublications()

    coordinates(group.toString(), "native-kt-core", version.toString())

    pom {
        name = "native-kt-core"
        description = "Core native-kt library"

        val properties = Properties()
        properties.load(file("../../gradle.properties").inputStream())

        url = properties.getProperty("pom.url")
        licenses {
            license {
                name = properties.getProperty("pom.license.name")
                url = properties.getProperty("pom.license.url")
            }
        }
        developers {
            developer {
                id = properties.getProperty("pom.developer.id")
                name = properties.getProperty("pom.developer.name")
                email = properties.getProperty("pom.developer.email")
            }
        }
        scm {
            connection = properties.getProperty("pom.scm.connection")
            developerConnection = properties.getProperty("pom.scm.connection")
            url = properties.getProperty("pom.url")
        }
    }
}