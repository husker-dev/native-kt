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

gradle.taskGraph.whenReady {
    if(allTasks.any { it is NativeKtCompileTask })
        allTasks.first { it is NativeKtPrepareTask }
            .dependsOn(":modules:cli:packAll")
}