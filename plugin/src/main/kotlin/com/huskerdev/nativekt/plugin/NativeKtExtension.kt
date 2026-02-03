package com.huskerdev.nativekt.plugin

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Named
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

    var ndkVersion: String? = null
    val androidTargets = arrayListOf("arm64-v8a", "armeabi-v7a", "x86_64")
}

sealed class NativeModule @Inject constructor(
    @get:JvmName("_name")
    val name: String
): Named {
    override fun getName(): String = name

    var buildType: LibBuildType = LibBuildType.RELEASE

    var classPath: String = "natives.$name"
}

open class Multiplatform @Inject constructor(
    name: String
): NativeModule(name) {

    /**
     * SourceSet that will have 'expect' interface
     */
    var commonSourceSet: String = "commonMain"

    /**
     * SourceSets with stub
     */
    val stubSourceSets = arrayListOf<String>()

    /**
     * SourceSets with implementation
     */
    var targetSourceSets = hashMapOf(
        TARGET_JVM,
        TARGET_JS,
        TARGET_ANDROID,

        TARGET_MINGW_X64,

        TARGET_MACOS_ARM64,
        TARGET_MACOS_X64,

        TARGET_LINUX_X64,
        TARGET_LINUX_ARM64,

        TARGET_IOS_X64,
        TARGET_IOS_ARM64,
        TARGET_IOS_SIMULATOR_ARM64,

        TARGET_WATCHOS_X64,
        TARGET_WATCHOS_ARM32,
        TARGET_WATCHOS_ARM64,
        TARGET_WATCHOS_DEVICE_ARM64,
        TARGET_WATCHOS_SIMULATOR_ARM64,

        TARGET_TVOS_X64,
        TARGET_TVOS_ARM64,
        TARGET_TVOS_SIMULATOR_ARM64,

        TARGET_ANDROID_NATIVE_X64,
        TARGET_ANDROID_NATIVE_X86,
        TARGET_ANDROID_NATIVE_ARM32,
        TARGET_ANDROID_NATIVE_ARM64
    )
}

open class SinglePlatform @Inject constructor(
    name: String
): NativeModule(name) {

    /**
     * SourceSet with implementation
     */
    var targetSourceSet: Pair<String, TargetType> = TARGET_JVM
}

@Suppress("unused")
enum class LibBuildType(
    val cmakeName: String
) {
    DEBUG("Debug"),
    REL_WITH_DEB_INFO("RelWithDebInfo"),
    RELEASE("Release"),
    MIN_SIZE_REL("MinSizeRel")
}

enum class TargetType(
    val kotlinTarget: String,
    val compiles: Set<String> = setOf(kotlinTarget)
) {
    JVM("jvm"),
    JS("js"),
    WASM("wasm"),

    ANDROID("android"),

    MINGW_X64("mingwX64"),

    MACOS_ARM64("macosArm64", setOf(
        "macosArm64", "macosX64",
        "iosX64", "iosArm64", "iosSimulatorArm64",
        "watchosX64", "watchosArm64", "watchosArm32", "watchosDeviceArm64", "watchosSimulatorArm64",
        "tvosX64", "tvosArm64", "tvosSimulatorArm64"
    )),
    MACOS_X64("macosX64", setOf(
        "macosArm64", "macosX64",
        "iosX64", "iosArm64", "iosSimulatorArm64",
        "watchosX64", "watchosArm64", "watchosArm32", "watchosDeviceArm64", "watchosSimulatorArm64",
        "tvosX64", "tvosArm64", "tvosSimulatorArm64"
    )),

    LINUX_X64("linuxX64"),
    LINUX_ARM64("linuxArm64"),

    IOS_X64("iosX64"),
    IOS_ARM64("iosArm64"),
    IOS_SIMULATOR_ARM64("iosSimulatorArm64"),

    WATCHOS_X64("watchosX64"),
    WATCHOS_ARM64("watchosArm64"),
    WATCHOS_ARM32("watchosArm32"),
    WATCHOS_DEVICE_ARM64("watchosDeviceArm64"),
    WATCHOS_SIMULATOR_ARM64("watchosSimulatorArm64"),

    TVOS_X64("tvosX64"),
    TVOS_ARM64("tvosArm64"),
    TVOS_SIMULATOR_ARM64("tvosSimulatorArm64"),

    ANDROID_NATIVE_X64("androidNativeX64"),
    ANDROID_NATIVE_X86("androidNativeX86"),
    ANDROID_NATIVE_ARM32("androidNativeArm32"),
    ANDROID_NATIVE_ARM64("androidNativeArm64"),
}


// Predefined target source sets

val TARGET_JVM = "jvmMain" to TargetType.JVM
val TARGET_JS = "jsMain" to TargetType.JS
val TARGET_ANDROID = "androidMain" to TargetType.ANDROID

val TARGET_MINGW_X64 = "mingwX64Main" to TargetType.MINGW_X64

val TARGET_MACOS_ARM64 = "macosArm64Main" to TargetType.MACOS_ARM64
val TARGET_MACOS_X64 = "macosX64Main" to TargetType.MACOS_X64

val TARGET_LINUX_ARM64 = "linuxArm64Main" to TargetType.LINUX_ARM64
val TARGET_LINUX_X64 = "linuxX64Main" to TargetType.LINUX_X64

val TARGET_IOS_ARM64 = "iosArm64Main" to TargetType.IOS_ARM64
val TARGET_IOS_X64 = "iosX64Main" to TargetType.IOS_X64
val TARGET_IOS_SIMULATOR_ARM64 = "iosSimulatorArm64Main" to TargetType.IOS_SIMULATOR_ARM64

val TARGET_WATCHOS_X64 = "watchosX64Main" to TargetType.WATCHOS_X64
val TARGET_WATCHOS_ARM32 = "watchosArm32Main" to TargetType.WATCHOS_ARM32
val TARGET_WATCHOS_ARM64 = "watchosArm64Main" to TargetType.WATCHOS_ARM64
val TARGET_WATCHOS_DEVICE_ARM64 = "watchosDeviceArm64Main" to TargetType.WATCHOS_DEVICE_ARM64
val TARGET_WATCHOS_SIMULATOR_ARM64 = "watchosSimulatorArm64Main" to TargetType.WATCHOS_SIMULATOR_ARM64

val TARGET_TVOS_X64 = "tvosX64Main" to TargetType.TVOS_X64
val TARGET_TVOS_ARM64 = "tvosArm64Main" to TargetType.TVOS_ARM64
val TARGET_TVOS_SIMULATOR_ARM64 = "tvosSimulatorArm64Main" to TargetType.TVOS_SIMULATOR_ARM64

val TARGET_ANDROID_NATIVE_X64 = "androidNativeX64Main" to TargetType.ANDROID_NATIVE_X64
val TARGET_ANDROID_NATIVE_X86 = "androidNativeX86Main" to TargetType.ANDROID_NATIVE_X86
val TARGET_ANDROID_NATIVE_ARM32 = "androidNativeArm32Main" to TargetType.ANDROID_NATIVE_ARM32
val TARGET_ANDROID_NATIVE_ARM64 = "androidNativeArm64Main" to TargetType.ANDROID_NATIVE_ARM64
