package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.BuildSystem
import com.huskerdev.nativekt.NativeKtModule
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.TargetType
import com.huskerdev.stdlibx.fs.SyncPath

internal fun appleTriple(
    configuration: NativeKtModule.Apple,
    targetType: TargetType
) = when (targetType) {
    TargetType.MACOS_ARM64 -> "arm64-apple-macosx${configuration.minMacos}"
    TargetType.MACOS_X64 -> "x86_64-apple-macosx${configuration.minMacos}"
    TargetType.IOS_ARM64 -> "arm64-apple-ios${configuration.minIos}"
    TargetType.IOS_SIMULATOR_ARM64 -> "arm64-apple-ios${configuration.minIos}-simulator"
    TargetType.IOS_X64 -> "x86_64-apple-ios${configuration.minIos}-simulator"
    TargetType.TVOS_ARM64 -> "arm64-apple-tvos${configuration.minTvos}"
    TargetType.TVOS_SIMULATOR_ARM64 -> "arm64-apple-tvos${configuration.minTvos}-simulator"
    TargetType.TVOS_X64 -> "x86_64-apple-tvos${configuration.minTvos}-simulator"
    TargetType.WATCHOS_ARM64 -> "arm64_32-apple-watchos${configuration.minWatchos}"
    TargetType.WATCHOS_DEVICE_ARM64 -> "arm64-apple-watchos${configuration.minWatchos}"
    TargetType.WATCHOS_ARM32 -> "armv7k-apple-watchos${configuration.minWatchos}"
    TargetType.WATCHOS_X64 -> "x86_64-apple-watchos${configuration.minWatchos}-simulator"
    TargetType.WATCHOS_SIMULATOR_ARM64 -> "arm64-apple-watchos${configuration.minWatchos}-simulator"
    else -> throw UnsupportedOperationException()
}

private fun getAppleSdkName(
    targetType: TargetType,
) = when(targetType) {
    TargetType.MACOS_ARM64,
    TargetType.MACOS_X64 -> "macosx"
    TargetType.IOS_ARM64 -> "iphoneos"
    TargetType.IOS_SIMULATOR_ARM64,
    TargetType.IOS_X64 -> "iphonesimulator"
    TargetType.TVOS_ARM64 -> "appletvos"
    TargetType.TVOS_SIMULATOR_ARM64,
    TargetType.TVOS_X64 -> "appletvsimulator"
    TargetType.WATCHOS_ARM32,
    TargetType.WATCHOS_ARM64,
    TargetType.WATCHOS_DEVICE_ARM64 -> "watchos"
    TargetType.WATCHOS_SIMULATOR_ARM64,
    TargetType.WATCHOS_X64 -> "watchsimulator"
    else -> throw UnsupportedOperationException()
}

internal fun getAppleSdk(
    context: NativeModuleContext,
    targetType: TargetType,
) = context.execute("xcrun --sdk ${getAppleSdkName(targetType)} --show-sdk-path", silent = true)

internal fun swiftSdkList(
    context: NativeModuleContext
) = context.execute("swift sdk list", silent = true).split("\n").map { it.trim() }

internal fun swiftAndroidStaticResourcesPath(
    context: NativeModuleContext,
    ndkPath: String,
    androidSwiftSdk: String,
    triple: String
) = context.execute(
    command = "swift sdk configure --show-configuration $androidSwiftSdk $triple",
    environment = mapOf(
        "ANDROID_NDK_HOME" to ndkPath
    ),
    silent = true
).split("\n")
    .first { it.startsWith("swiftStaticResourcesPath") }
    .split("swiftStaticResourcesPath: ")[1]

internal fun toAndroidSwiftTarget(target: String, sdkVersion: String) = when(target) {
    "x86_64"      -> "x86_64-unknown-linux-android"
    "x86"         -> "i686-unknown-linux-android"
    "armeabi-v7a" -> "armv7-unknown-linux-android"
    "arm64-v8a"   -> "aarch64-unknown-linux-android"
    else -> throw UnsupportedOperationException("Unsupported Android target: $target")
} + sdkVersion

internal fun spmBuild(
    context: NativeModuleContext,
    projectDir: SyncPath,
    buildDir: SyncPath,
    buildType: BuildSystem.SPM.BuildType,
    targetType: TargetType
): SyncPath {
    context.execute(
        command = "swift build" +
                " -c ${buildType.argName}" +
                " --build-path \"${buildDir.invariantSeparatorsPath}\"" +
                " --triple ${appleTriple(context.module.apple, targetType)}" +
                " --sdk ${getAppleSdk(context, targetType)}",
        workingDir = projectDir,
        errAsStd = true
    )
    return buildDir.resolve(buildType.argName)
}

internal fun spmBuild(
    context: NativeModuleContext,
    projectDir: SyncPath,
    buildDir: SyncPath,
    buildType: BuildSystem.SPM.BuildType,
    args: String,
    env: Map<String, String>
): SyncPath {
    context.execute(
        command = "swift build" +
                " -c ${buildType.argName}" +
                " --build-path \"${buildDir.invariantSeparatorsPath}\" " +
                args,
        workingDir = projectDir,
        errAsStd = true,
        environment = env
    )
    return buildDir.resolve(buildType.argName)
}

internal fun spmLinkerFlags(
    context: NativeModuleContext,
    projectDir: SyncPath,
    buildDir: SyncPath,
    buildType: BuildSystem.SPM.BuildType,
    targetType: TargetType
): List<String> = buildList {
    swiftcRuntimeLibraryPaths(context, targetType).mapTo(this) {
        "-L$it"
    }
}

private fun swiftcRuntimeLibraryPaths(
    context: NativeModuleContext,
    targetType: TargetType
) = context.execute(
        command = "swiftc -target ${appleTriple(context.module.apple, targetType)} -print-target-info",
        silent = true
    ).split("runtimeLibraryPaths").getOrNull(1)
    ?.split("[")?.getOrNull(1)
    ?.split("]")?.getOrNull(0)
    ?.split(",")?.map {
        val trim = it.trim()
        trim.substring(1, trim.lastIndex)
    }
    ?: emptyList()