@file:Suppress("UnstableApiUsage")

rootProject.name = "native-kt"

includeBuild("modules/core")

include("modules:runtime")
include("modules:cli")
include("modules:intellij-plugin")
include("modules:android-critical-stub")

include("modules:tests")
include("modules:test-jvm-only")
include("modules:benchmarks")

include("modules:examples:glfw")
include("modules:examples:freetype")


pluginManagement {
    includeBuild("modules/gradle-plugin")

    repositories {
        if(providers.gradleProperty("mavenLocal").get().isNotEmpty())
            mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        if(providers.gradleProperty("mavenLocal").get().isNotEmpty())
            mavenLocal()
        mavenCentral()
        google()
    }

    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

gradle.beforeProject {
    repositories.addAll(dependencyResolutionManagement.repositories)
}