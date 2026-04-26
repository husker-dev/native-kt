plugins {
    alias(libs.plugins.kotlin.jvm)

    id("com.huskerdev.native-kt")
}

group = "com.huskerdev"
version = "1.0"

dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(project(":modules:runtime"))

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

natives {
    applyRuntime = false
    useJvmRecord = false
    useForeignApi = false

    create("jvmOnlyTest")
}

tasks.withType<Test>().configureEach {
    if (name.contains("jvm", ignoreCase = true)) {
        jvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
    }
}
