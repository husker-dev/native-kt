package com.huskerdev.nativekt.plugin

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class NativeKtExtension @Inject constructor(
    objects: ObjectFactory
): ExtensiblePolymorphicDomainObjectContainer<NativeModule> by objects.polymorphicDomainObjectContainer(NativeModule::class.java) {
    init {
        registerFactory(Multiplatform::class.java) { name ->
            objects.newInstance(Multiplatform::class.java, name)
        }
        registerFactory(SinglePlatform::class.java) { name ->
            objects.newInstance(SinglePlatform::class.java, name)
        }
        registerFactory(NativeModule::class.java) { name ->
            objects.newInstance(Multiplatform::class.java, name)
        }
    }

    var useCoroutines = true

    var useForeignApi = true
    var useJVMCI = false

    var useUniversalMacOSLib = false

    var ndkVersion: String? = null
    val androidTargets = arrayListOf("arm64-v8a", "armeabi-v7a", "x86_64")
}

sealed class NativeModule @Inject constructor(
    @get:JvmName("_name")
    val name: String
): Named {
    override fun getName(): String = name

    /**
     * Directory with CMake project.
     *
     * Default value: `src/nativeInterop/[name]`
     */
    var projectDir: RegularFileProperty? = null

    /**
     * CMake build type.
     *
     * Default value: `RELEASE`
     */
    var buildType: CMakeBuildType = CMakeBuildType.RELEASE

    /**
     * Classpath where bindings will be generated.
     *
     * Default value: `natives.[name]`
     */
    var classPath: String = "natives.$name"
}

open class Multiplatform @Inject constructor(
    name: String
): NativeModule(name) {

    /**
     * SourceSet that will have 'expect' api
     */
    var commonSourceSet: String = "commonMain"

    /**
     * SourceSets with stub
     */
    val stubSourceSets = arrayListOf<String>()

    /**
     * SourceSets with implementation
     */
    var targetSourceSets = setOf(
        "jvmMain",
        "jsMain",
        "androidMain",

        "mingwX64Main",

        "macosArm64Main",
        "macosX64Main",

        "linuxArm64Main",
        "linuxX64Main",

        "iosArm64Main",
        "iosX64Main",
        "iosSimulatorArm64Main",

        "watchosX64Main",
        "watchosArm32Main",
        "watchosArm64Main",
        "watchosDeviceArm64Main",
        "watchosSimulatorArm64Main",

        "tvosX64Main",
        "tvosArm64Main",
        "tvosSimulatorArm64Main",

        "androidNativeX64Main",
        "androidNativeX86Main",
        "androidNativeArm32Main",
        "androidNativeArm64Main"
    )
}

open class SinglePlatform @Inject constructor(
    name: String
): NativeModule(name) {

    /**
     * SourceSet with implementation
     */
    var targetSourceSet: String = "jvmMain"
}

@Suppress("unused")
enum class CMakeBuildType(
    val cmakeName: String
) {
    DEBUG("Debug"),
    REL_WITH_DEB_INFO("RelWithDebInfo"),
    RELEASE("Release"),
    MIN_SIZE_REL("MinSizeRel")
}