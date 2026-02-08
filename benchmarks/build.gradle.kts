import com.huskerdev.nativekt.plugin.Multiplatform

plugins {
    alias(libs.plugins.kotlin.multiplatform)

    id("org.jetbrains.kotlinx.benchmark") version "0.4.16"

    id("native-kt")
    id("maven-publish")
}

group = "com.huskerdev"
version = "1.0.0"

native {
    useCoroutines = false

    create("jniBindings", Multiplatform::class)
    create("foreignBindings", Multiplatform::class)
}

kotlin {
    jvm()

    sourceSets.commonMain.dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.16")
    }
}

benchmark {
    targets {
        register("jvm")
    }
}