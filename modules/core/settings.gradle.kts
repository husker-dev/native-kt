@file:Suppress("UnstableApiUsage")

rootProject.name = "core"

pluginManagement {
    val properties = java.util.Properties()
    properties.load(file("../../gradle.properties").inputStream())

    repositories {
        if(properties.getProperty("mavenLocal").isNotEmpty())
            mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    val properties = java.util.Properties()
    properties.load(file("../../gradle.properties").inputStream())

    repositories {
        if(properties.getProperty("mavenLocal").isNotEmpty())
            mavenLocal()
        mavenCentral()
        google()
    }
    versionCatalogs {
        create("libs") {
            from(files("../../libs.versions.toml"))
        }
    }
}

gradle.beforeProject {
    repositories.addAll(dependencyResolutionManagement.repositories)
}