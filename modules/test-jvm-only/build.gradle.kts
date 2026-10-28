import com.huskerdev.nativekt.gradle.NativeKtCompileTask
import com.huskerdev.nativekt.gradle.NativeKtPrepareTask
import com.huskerdev.nativekt.gradle.configurators.PrepareNativesCommon

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

    add("jvmOnlyTest")
}

tasks.withType<Test>().configureEach {
    if (name.contains("jvm", ignoreCase = true)) {
        jvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
    }
}

afterEvaluate {
    tasks.filterIsInstance<NativeKtCompileTask>()
        .forEach { it.dependsOn(":modules:cli:packAll") }
    tasks.filterIsInstance<NativeKtPrepareTask>()
        .forEach { it.mustRunAfter(":modules:cli:packAll") }
}