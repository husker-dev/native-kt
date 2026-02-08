import com.huskerdev.nativekt.plugin.Multiplatform

plugins {
    alias(libs.plugins.kotlin.multiplatform)

    id("com.huskerdev.native-kt")
    id("maven-publish")
}

group = "com.huskerdev"
version = "1.0.0"

native {
    useCoroutines = false

    create("glfwBindings", Multiplatform::class)
}

kotlin {
    jvm()

    setOf(
        mingwX64(),
        macosArm64(), macosX64(),
        linuxX64(), linuxArm64()
    ).forEach {
        it.binaries {
            executable {
                linkerOpts += "-mwindows"
                entryPoint = "main"
            }
        }
    }
}
