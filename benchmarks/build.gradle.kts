import com.huskerdev.nativekt.plugin.Multiplatform

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.benchmark)

    id("com.huskerdev.native-kt")
    id("maven-publish")
}

group = "com.huskerdev"
version = projectDir.parentFile.resolve("VERSION").readText()

native {
    useCoroutines = false
    useJVMCI = true

    create("jniBindings", Multiplatform::class)
    create("foreignBindings", Multiplatform::class)
    create("jvmciBindings", Multiplatform::class)
}

kotlin {
    jvm()

    sourceSets.commonMain.dependencies {
        implementation(libs.kotlinx.benchmark)
    }
    sourceSets.jvmMain.dependencies {
        implementation(project(":native-kt-runtime"))
    }
}

benchmark {
    targets {
        register("jvm")
    }
    configurations {
        named("main") {
            warmups = 2
            iterationTime = 5L * 1000000000 // 5 sec
            iterationTimeUnit = "ns"
            outputTimeUnit = "ns"
            mode = "avgt"
        }
    }
}