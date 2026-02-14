
rootProject.name = "native-kt"

include("native-kt-runtime")

include("test")
include("test-glfw")
include("benchmarks")

pluginManagement {
    includeBuild("native-kt-plugin")

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

gradle.beforeProject {
    repositories.addAll(dependencyResolutionManagement.repositories)
}