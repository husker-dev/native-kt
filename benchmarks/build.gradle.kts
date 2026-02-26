plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.benchmark)

    id("com.huskerdev.native-kt")
    id("maven-publish")
}

group = "com.huskerdev"
version = projectDir.parentFile.resolve("VERSION").readText()

kotlin {
    jvm()

    sourceSets.commonMain.dependencies {
        implementation(libs.kotlinx.benchmark)
        implementation(project(":native-kt-runtime"))
    }
}

native {
    useCoroutines = false

    create("jniBindings")
    create("foreignBindings")
    create("jvmciBindings")
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