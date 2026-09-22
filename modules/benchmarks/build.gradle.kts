import gobley.gradle.GobleyHost
import gobley.gradle.cargo.dsl.jvm

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.benchmark)
    alias(libs.plugins.kotlin.atomicfu)

    id("com.huskerdev.native-kt")

    alias(libs.plugins.gobley.cargo)
    alias(libs.plugins.gobley.uniffy)
}

group = "com.huskerdev"
version = projectDir.parentFile.parentFile.resolve("VERSION").readText().trim()

val boltFfiJar = tasks.register<BoltFFIJar>("generateBoltFffiJni") {
    description = "Generates BoltFFI JNI bindings"
    projectDir = file("natives/boltFFI")
}

kotlin {
    jvmToolchain {
        vendor = JvmVendorSpec.GRAAL_VM
        languageVersion = JavaLanguageVersion.of(23)
    }
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.benchmark)
            implementation(project(":modules:runtime"))
        }
        jvmMain {
            kotlin.srcDirs(boltFfiJar.flatMap { it.sourcesDir })
            resources.srcDirs(boltFfiJar.flatMap { it.resourcesDir })
        }
    }
}

natives {
    applyRuntime = false
    useCoroutines = false

    create("jniBindings") {
        cargo()
    }
    create("foreignBindings") {
        cargo()
    }
    create("jvmciBindings") {
        cargo()
    }
}

cargo {
    packageDirectory = layout.projectDirectory.dir("natives/gobley")
    builds.jvm {
        embedRustLibrary = (rustTarget == GobleyHost.current.rustTarget)
    }
}
uniffi {
    generateFromLibrary {
        namespace = "gobley"
        packageName = "com.huskerdev"
    }
    generateDuringSync = false
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