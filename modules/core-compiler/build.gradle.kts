import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.maven.publish)
}

group = "com.huskerdev"
version = projectDir.parentFile.parentFile.resolve("VERSION").readText().trim()

kotlin {
    jvm {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                jvmTarget = JvmTarget.JVM_11
            }
        }
    }

    macosArm64()
    mingwX64()
    linuxX64()
    linuxArm64()

    sourceSets {
        commonMain.dependencies {
            api("${group}:native-kt-core:${version}")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

mavenPublishing {
    publishToMavenCentral()

    if(project.hasProperty("sign"))
        signAllPublications()

    coordinates(group.toString(), "native-kt-core-compiler", version.toString())

    pom {
        name = "native-kt-core-compiler"
        description = "Core compiler native-kt library"

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