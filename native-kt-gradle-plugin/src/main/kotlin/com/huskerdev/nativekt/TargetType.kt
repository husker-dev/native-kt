package com.huskerdev.nativekt

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