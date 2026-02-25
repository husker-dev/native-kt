
rootProject.name = "native-kt"

include("native-kt-runtime")
include("native-kt-runtime-jvm")
include("native-kt-intellij-plugin")

include("test")
include("example-glfw")
include("benchmarks")

pluginManagement {
    includeBuild("native-kt-gradle-plugin")

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