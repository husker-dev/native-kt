package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.BuildSystem
import com.huskerdev.nativekt.NativeKtModule
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.TargetType
import com.huskerdev.stdlibx.OS
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

internal fun toAndroidSwiftTarget(target: String, sdkVersion: String) = when(target) {
    "x86_64"      -> "x86_64-unknown-linux-android"
    "x86"         -> "i686-unknown-linux-android"
    "armeabi-v7a" -> "armv7-unknown-linux-android"
    "arm64-v8a"   -> "aarch64-unknown-linux-android"
    else -> throw UnsupportedOperationException("Unsupported Android target: $target")
} + sdkVersion

internal fun swiftTarget(
    context: NativeModuleContext,
    targetType: TargetType
) = when (targetType) {
    TargetType.LINUX_X64 -> "x86_64-unknown-linux-gnu"
    TargetType.LINUX_ARM64 -> "aarch64-unknown-linux-gnu"
    TargetType.MACOS_ARM64,
    TargetType.MACOS_X64,
    TargetType.IOS_X64,
    TargetType.IOS_ARM64,
    TargetType.IOS_SIMULATOR_ARM64,
    TargetType.WATCHOS_X64,
    TargetType.WATCHOS_ARM64,
    TargetType.WATCHOS_ARM32,
    TargetType.WATCHOS_DEVICE_ARM64,
    TargetType.WATCHOS_SIMULATOR_ARM64,
    TargetType.TVOS_X64,
    TargetType.TVOS_ARM64,
    TargetType.TVOS_SIMULATOR_ARM64 -> appleTriple(context.module.apple, targetType)
    else -> throw UnsupportedOperationException("Unsupported swift target")
}

private fun extractSpmClangFlags(
    verboseOutput: String,
    onlyLinkerFlags: Boolean
): List<String> {
    val split = setOf(
        "clang -shared",
        "clang\" -shared",
        "clang -dynamiclib",
        "clang\" -dynamiclib"
    ).firstOrNull { it in verboseOutput } ?: return emptyList()

    val parts = verboseOutput
        .split(split).getOrNull(1)
        ?.split("\n")?.getOrNull(0)
        ?.splitRespectingQuotes()
        ?.flatMap {
            if(it.startsWith("@")) {
                val path = SyncPath(it.substring(1))
                if(path.exists())
                    path.readString().replace("\n", " ").splitRespectingQuotes()
                else listOf(it)
            } else listOf(it)
        }
        ?: emptyList()

    val xLinker = if(onlyLinkerFlags) "" else "-Xlinker"

    println("Raw Swift clang args:")
    parts.forEach {
        println("    $it")
    }

    return buildList {
        var i = 0
        while (i < parts.size) {
            val part = parts[i]
            when {
                part == "-rpath" -> {
                    val path = if(parts[i+1] == "-Xlinker"){
                        i += 2
                        parts[i]
                    } else parts[++i]
                    val safePath = if(onlyLinkerFlags) path else "'$path'"
                    add("$xLinker -rpath $xLinker $safePath")
                }

                part == "--start-group" -> {
                    add("$xLinker --start-group")
                    i++
                    while(i < parts.size && parts[i] != "--end-group" && parts[i] != "-Xlinker") {
                        val subPart = parts[i++]
                        if(!subPart.endsWith(".o"))
                            add(subPart)
                    }
                    add("$xLinker --end-group")
                }

                part == "-Fsystem" ->
                    if(onlyLinkerFlags) add("-F ${parts[++i]}")
                    else add("$part ${parts[++i]}")

                part == "-L" || part == "-F" || part == "-framework" ||
                        part == "-weak_framework" || part == "-force_load" -> add("$part ${parts[++i]}")

                part.startsWith("-L") || part.startsWith("-l") -> add(part)

                part == "-fobjc-link-runtime" ->
                    add(if(onlyLinkerFlags) "-lobjc" else part)

                part.endsWith(".a") -> add(part)
            }
            i++
        }
    }.apply {
        println("Filtered Swift args:")
        parts.forEach {
            println("    $it")
        }
    }
}

internal fun spmBuild(
    context: NativeModuleContext,
    projectDir: SyncPath,
    buildDir: SyncPath,
    buildType: BuildSystem.SPM.BuildType,
    targetType: TargetType,
    onlyLinkerFlags: Boolean
): Pair<SyncPath, List<String>> {
    context.execute(
        command = "swift package clean" +
                " --build-path \"${buildDir.invariantSeparatorsPath}\" ",
        workingDir = projectDir,
        errAsStd = true,
    )
    val flags = extractSpmClangFlags(context.execute(
        command = "swift build" +
                " -c ${buildType.argName}" +
                " --build-path \"${buildDir.invariantSeparatorsPath}\"" +
                " --triple ${swiftTarget(context, targetType)}" +
                (if(OS.isMacOS) " --sdk ${getAppleSdk(context, targetType)}" else "") +
                " -v -Xswiftc -v",
        workingDir = projectDir,
        errAsStd = true,
        silent = true
    ), onlyLinkerFlags)
    return buildDir.resolve(buildType.argName) to flags
}

internal fun spmBuild(
    context: NativeModuleContext,
    projectDir: SyncPath,
    buildDir: SyncPath,
    buildType: BuildSystem.SPM.BuildType,
    args: String,
    env: Map<String, String>,
    onlyLinkerFlags: Boolean
): Pair<SyncPath, List<String>> {
    context.execute(
        command = "swift package clean" +
                " --build-path \"${buildDir.invariantSeparatorsPath}\" ",
        workingDir = projectDir,
        errAsStd = true,
    )
    val flags = extractSpmClangFlags(context.execute(
        command = "swift build" +
                " -c ${buildType.argName}" +
                " --build-path \"${buildDir.invariantSeparatorsPath}\" " +
                " -v -Xswiftc -v " +
                args,
        workingDir = projectDir,
        errAsStd = true,
        environment = env,
        silent = true
    ), onlyLinkerFlags)
    return buildDir.resolve(buildType.argName) to flags
}