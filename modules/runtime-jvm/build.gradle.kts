plugins {
    id("java")
    alias(libs.plugins.maven.publish)
}

group = "com.huskerdev"
version = projectDir.parentFile.parentFile.resolve("VERSION").readText()

java {
    sourceCompatibility = JavaVersion.VERSION_1_9
    targetCompatibility = JavaVersion.VERSION_1_9
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf(
        "--add-modules", "jdk.internal.vm.ci",
        "--add-exports", "jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED",
        "--add-exports", "jdk.internal.vm.ci/jdk.vm.ci.code.site=ALL-UNNAMED",
        "--add-exports", "jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED",
        "--add-exports", "jdk.internal.vm.ci/jdk.vm.ci.meta=ALL-UNNAMED",
        "--add-exports", "jdk.internal.vm.ci/jdk.vm.ci.runtime=ALL-UNNAMED",
    ))
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("-add-modules", "jdk.internal.vm.ci")
        addMultilineStringsOption("-add-exports").value = listOf(
            "jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED",
            "jdk.internal.vm.ci/jdk.vm.ci.code.site=ALL-UNNAMED",
            "jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED",
            "jdk.internal.vm.ci/jdk.vm.ci.meta=ALL-UNNAMED",
            "jdk.internal.vm.ci/jdk.vm.ci.runtime=ALL-UNNAMED"
        )
    }
}

mavenPublishing {
    publishToMavenCentral()

    //signAllPublications()

    coordinates(group.toString(), "native-kt-runtime-jvm-impl", version.toString())

    pom {
        name = "native-kt-runtime-jvm-impl"
        description = "JVM runtime for native-kt Gradle plugin"
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