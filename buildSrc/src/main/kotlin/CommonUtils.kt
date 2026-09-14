@file:Suppress("unused", "UnusedImport")

import org.gradle.api.publish.maven.MavenPom
import org.gradle.kotlin.dsl.assign


fun MavenPom.applyDefaultPomInfo() {
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
            email = "shtengauer.nikita@gmail.com"
        }
    }
    scm {
        connection = "https://github.com/husker-dev/native-kt.git"
        developerConnection = "https://github.com/husker-dev/native-kt.git"
        url = "https://github.com/husker-dev/native-kt"
    }
}

class PluginStub