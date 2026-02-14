plugins {
    id("java")
    id("maven-publish")
}

group = "com.huskerdev"
version = projectDir.parentFile.resolve("VERSION").readText()

java {
    sourceCompatibility = JavaVersion.VERSION_1_9
    targetCompatibility = JavaVersion.VERSION_1_9
}

tasks.jar {
    archiveBaseName = "native-kt-runtime"
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