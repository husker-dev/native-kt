import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.shadow)
}

group = "com.huskerdev"
version = projectDir.parentFile.parentFile.resolve("VERSION").readText().trim()

kotlin {
    sourceSets.commonMain.dependencies {
        implementation("$group:native-kt-core:$version")
    }

    jvm {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                jvmTarget = JvmTarget.JVM_11
            }
        }
    }
    listOf(
        macosArm64(),
        mingwX64(),
        linuxX64(),
        linuxArm64(),
    ).forEach {
        it.binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.shadowJar {
    archiveBaseName = "nativekt-cli"
    archiveClassifier = ""
    manifest {
        attributes(
            "Main-Class" to "MainKt",
            "Implementation-Title" to "native-kt-cli",
            "Implementation-Version" to project.version
        )
    }
}