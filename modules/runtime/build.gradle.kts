@file:OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
@file:Suppress("UnstableApiUsage")

import com.android.build.api.withAndroid
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.maven.publish)
}

group = "com.huskerdev"
version = projectDir.parentFile.parentFile.resolve("VERSION").readText().trim()

kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("commonJvm") {
                withJvm()
                withAndroid()
            }
        }
    }

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
        namespace = "$group.runtime"
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

    sourceSets.commonMain.dependencies {
        implementation(libs.stdlibx)
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

    if(project.hasProperty("sign"))
        signAllPublications()

    coordinates(group.toString(), "native-kt-runtime", version.toString())

    pom {
        name = "native-kt-runtime"
        description = "Runtime for native-kt Gradle plugin"

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