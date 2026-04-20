@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.maven.publish)
}

group = "com.huskerdev"
version = projectDir.parentFile.parentFile.resolve("VERSION").readText()

kotlin {
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
            version = release(26)
        }
    }

    mingwX64()

    linuxX64()
    linuxArm64()

    macosX64()
    macosArm64()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    watchosX64()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()

    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()

    androidNativeX64()
    androidNativeX86()
    androidNativeArm32()
    androidNativeArm64()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf(
        "--add-modules", "jdk.internal.vm.ci",
        "--add-exports", "jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED",
        "--add-exports", "jdk.internal.vm.ci/jdk.vm.ci.code.site=ALL-UNNAMED",
        "--add-exports", "jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED",
        "--add-exports", "jdk.internal.vm.ci/jdk.vm.ci.meta=ALL-UNNAMED",
        "--add-exports", "jdk.internal.vm.ci/jdk.vm.ci.runtime=ALL-UNNAMED",
    ))
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("-add-modules", "jdk.internal.vm.ci")
        addMultilineStringsOption("-add-exports").value = listOf(
            "jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED",
            "jdk.internal.vm.ci/jdk.vm.ci.code.site=ALL-UNNAMED",
            "jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED",
            "jdk.internal.vm.ci/jdk.vm.ci.meta=ALL-UNNAMED",
            "jdk.internal.vm.ci/jdk.vm.ci.runtime=ALL-UNNAMED"
        )
    }
}

mavenPublishing {
    publishToMavenCentral()

    //signAllPublications()

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