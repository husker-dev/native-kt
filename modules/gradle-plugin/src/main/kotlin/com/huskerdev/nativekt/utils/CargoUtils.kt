package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.plugin.CargoBuildType
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.process.ExecOperations
import java.io.File

private val rustcCurrentTaget: String
    get() {
        val arch = when(Arch.current()) {
            Arch.X86 -> "i686"
            Arch.X64 -> "x86_64"
            Arch.ARM32 -> "armv7"
            Arch.ARM64 -> "aarch64"
            Arch.RISCV32 -> "riscv32i"
            Arch.RISCV64 -> "riscv64gc"
            Arch.UNKNOWN -> throw UnsupportedOperationException()
        }
        val os = when {
            Os.isFamily(Os.FAMILY_WINDOWS) -> "pc-windows-gnu"
            Os.isFamily(Os.FAMILY_MAC) -> "apple-darwin"
            Os.isFamily(Os.FAMILY_UNIX) -> "unknown-linux-gnu"
            else -> throw UnsupportedOperationException()
        }
        return "$arch-$os"
    }

internal fun cargoTargetDir(
    buildDir: File,
    buildType: CargoBuildType,
    target: String = rustcCurrentTaget
) = File(buildDir, "$target/${buildType.cargoName}").posixPath

internal fun cargoLinkerFlags(
    execOps: ExecOperations,
    project: File,
    buildDir: File,
    buildType: CargoBuildType,
    target: String = rustcCurrentTaget
): List<String> {
    val buildDirClean = buildDir.posixPath
    val flags = execOps.exec(
        command =  "cargo rustc --target=$target --target-dir=$buildDirClean --lib --${buildType.cargoName} -- --print=native-static-libs",
        workingDir = project,
        silent = true,
        errAsStd = true
    ).split("note: native-static-libs: ")[1]
        .split("\n")[0]
        .trim()
        .splitRespectingQuotes()

    return normalizeMinGWLibs(execOps, flags)
}

internal fun cargoBuild(
    execOps: ExecOperations,
    project: File,
    buildDir: File,
    buildType: CargoBuildType,
    target: String = rustcCurrentTaget,
): String {
    val buildDirClean = buildDir.posixPath
    execOps.exec(
        command = "cargo build --target=$target --target-dir=$buildDirClean --lib --${buildType.cargoName}",
        workingDir = project,
        errAsStd = true
    )
    return cargoTargetDir(buildDir, buildType, target)
}

internal fun getCargoTarget(
    targetType: TargetType,
): String = when(targetType) {
    TargetType.IOS_SIMULATOR_ARM64 -> "aarch64-apple-ios-sim"
    TargetType.IOS_X64 -> "x86_64-apple-ios"
    TargetType.IOS_ARM64 -> "aarch64-apple-ios"
    TargetType.TVOS_ARM64 -> "aarch64-apple-tvos"
    TargetType.TVOS_SIMULATOR_ARM64 -> "aarch64-apple-tvos-sim"
    TargetType.TVOS_X64 -> "x86_64-apple-tvos"
    TargetType.WATCHOS_ARM32 -> "armv7k-apple-watchos"
    TargetType.WATCHOS_ARM64 -> "arm64_32-apple-watchos"
    TargetType.WATCHOS_DEVICE_ARM64 -> "aarch64-apple-watchos"
    TargetType.WATCHOS_SIMULATOR_ARM64 -> "aarch64-apple-watchos-sim"
    TargetType.WATCHOS_X64 -> "x86_64-apple-watchos-sim"
    TargetType.MACOS_ARM64 -> "aarch64-apple-darwin"
    TargetType.MACOS_X64 -> "x86_64-apple-darwin"
    TargetType.MINGW_X64 -> "x86_64-pc-windows-gnu"
    TargetType.LINUX_X64 -> "x86_64-unknown-linux-gnu"
    TargetType.LINUX_ARM64 -> "aarch64-unknown-linux-gnu"
    else -> throw UnsupportedOperationException()
}
