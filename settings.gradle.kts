
rootProject.name = "native-kt"

include("modules:runtime")
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
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
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