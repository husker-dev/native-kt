plugins {
    alias(libs.plugins.kotlin.js)

    id("com.huskerdev.native-kt")
}

group = "com.huskerdev"
version = "1.0"

kotlin {
    js {
        nodejs()
        browser()
        compilerOptions {
            target = "es2015"
        }
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(project(":modules:runtime"))

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

natives {
    applyRuntime = false

    create("jsOnlyTest")
}