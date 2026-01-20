
rootProject.name = "native-kt"

include("test")

pluginManagement {
    includeBuild("plugin")

    repositories {
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