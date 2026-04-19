
rootProject.name = "native-kt"

include("modules:runtime")
include("modules:runtime-jvm")
include("modules:intellij-plugin")

include("modules:tests")
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

gradle.beforeProject {
    repositories.addAll(dependencyResolutionManagement.repositories)
}