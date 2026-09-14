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
    signAllPublications()

    coordinates(group.toString(), "native-kt-android-critical-stub", version.toString())

    pom {
        name = "native-kt-android-critical-stub"
        description = "Critical stub for Android"

        applyDefaultPomInfo()
    }
}