import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.maven.publish)
}

group = "com.huskerdev"
version = "1.0.0"

kotlin {
    android {
        namespace = group.toString()
        minSdk = 5
        compileSdk {
            version = release(5)
        }
    }

    sourceSets {
        remove(commonTest.get())
    }
}


mavenPublishing {
    publishToMavenCentral()

    if(project.hasProperty("sign"))
        signAllPublications()

    coordinates(group.toString(), "native-kt-android-critical-stub", version.toString())

    pom {
        name = "native-kt-android-critical-stub"
        description = "Critical stub for Android"

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