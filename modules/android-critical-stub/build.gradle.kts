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
}


mavenPublishing {
    publishToMavenCentral()

    //signAllPublications()

    coordinates(group.toString(), "native-kt-android-critical-stub", version.toString())

    pom {
        name = "native-kt-android-critical-stub"
        description = "Critical stub for Android"
        url = "https://github.com/husker-dev/native-kt"

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "husker-dev"
                name = "Nikita Shtengauer"
                email = "redfancoestar@gmail.com"
            }
        }
        scm {
            connection = "https://github.com/husker-dev/native-kt.git"
            developerConnection = "https://github.com/husker-dev/native-kt.git"
            url = "https://github.com/husker-dev/native-kt"
        }
    }
}