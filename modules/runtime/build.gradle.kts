@file:OptIn(ExperimentalWasmDsl::class)

import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.maven.publish)
}

group = "com.huskerdev"
version = projectDir.parentFile.parentFile.resolve("VERSION").readText()

kotlin {
    jvmToolchain {
        vendor = JvmVendorSpec.GRAAL_VM
        languageVersion = JavaLanguageVersion.of(23)
    }

    jvm {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                jvmTarget = JvmTarget.JVM_11
            }
        }
    }

    wasmJs {
        browser()
        nodejs()
    }
    js {
        browser()
        nodejs()
    }

    android {
        namespace = group.toString()
        minSdk = 5
        compileSdk {
            version = release(5)
        }
    }

    mingwX64()

    linuxX64()
    linuxArm64()

    if(Os.isFamily(Os.FAMILY_MAC)) {
        macosArm64()

        iosX64()
        iosArm64()
        iosSimulatorArm64()

        watchosArm32()
        watchosArm64()
        watchosDeviceArm64()
        watchosSimulatorArm64()

        tvosArm64()
        tvosSimulatorArm64()
    }

    /*
    androidNativeX64()
    androidNativeX86()
    androidNativeArm32()
    androidNativeArm64()
    */

    targets.withType<KotlinNativeTarget>().configureEach {
        compilations.getByName("main") {
            cinterops.register("api")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinJvmCompile>().configureEach {
    this.compilerOptions.freeCompilerArgs.addAll(listOf(
        "-Xadd-modules=jdk.internal.vm.ci"
    ))
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "native-kt-runtime", version.toString())

    pom {
        name = "native-kt-runtime"
        description = "Runtime for native-kt Gradle plugin"
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
                email = "redfancoestar@gmail.com"
            }
        }
        scm {
            connection = "https://github.com/husker-dev/native-kt.git"
            developerConnection = "https://github.com/husker-dev/native-kt.git"
            url = "https://github.com/husker-dev/native-kt"
        }
    }
}