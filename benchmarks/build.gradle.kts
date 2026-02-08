import com.huskerdev.nativekt.plugin.Multiplatform

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.benchmark)

    id("com.huskerdev.native-kt")
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
        implementation(libs.kotlinx.benchmark)
    }
}

benchmark {
    targets {
        register("jvm")
    }
}