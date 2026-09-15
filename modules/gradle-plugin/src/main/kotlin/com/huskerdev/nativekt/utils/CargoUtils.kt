package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.plugin.CargoBuildType
import com.huskerdev.osutils.Arch
import com.huskerdev.osutils.OS
import org.gradle.process.ExecOperations
import java.io.File

private val rustcCurrentTaget: String
    get() {
        val arch = when(Arch.current) {
            Arch.X86 -> "i686"
            Arch.X64 -> "x86_64"
            Arch.ARM32 -> "armv7"
            Arch.ARM64 -> "aarch64"
            Arch.RISCV32 -> "riscv32i"
            Arch.RISCV64 -> "riscv64gc"
            Arch.UNKNOWN -> throw UnsupportedOperationException()
        }
        val os = when(OS.current) {
            OS.WINDOWS -> "pc-windows-gnu"
            OS.MACOS -> "apple-darwin"
            OS.LINUX -> "unknown-linux-gnu"
            else -> throw UnsupportedOperationException()
        }
        return "$arch-$os"
    }

internal fun ensureTargetInstalled(
    execOps: ExecOperations,
    context: NativeModuleContext,
    target: String
) {
    execOps.exec(context, "rustup target list", silent = true)
        .split("\n")
        .firstOrNull {
            it.startsWith(target)
        }?.run {
            if(!endsWith("(installed)")) {
                println("Installing rust target: $target...")
                execOps.exec(context, "rustup target add $target")
            }
        } ?: throw Exception("The $target is not supported on the current system (maybe try the nightly build?).")
}

internal fun ensureWasmBindgenInstalled(execOps: ExecOperations, context: NativeModuleContext) {
    try {
        execOps.exec(context,
            command = "wasm-bindgen --version",
            silent = true
        )
    } catch (_: Exception) {
        println("Installing wasm-bindgen...")
        execOps.exec(context,
            command = "cargo install wasm-bindgen-cli",
            silent = true
        )
    }
}

internal fun ensureWasmOptInstalled(execOps: ExecOperations, context: NativeModuleContext) {
    try {
        execOps.exec(context,
            command = "wasm-opt --version",
            silent = true
        )
    } catch (_: Exception) {
        println("Installing wasm-opt...")
        execOps.exec(context,
            command = "cargo install wasm-opt",
            silent = true
        )
    }
}

internal fun cargoTargetDir(
    buildDir: File,
    buildType: CargoBuildType,
    target: String = rustcCurrentTaget
) = File(buildDir, "$target/${buildType.cargoName}").posixPath

internal fun cargoLinkerFlags(
    execOps: ExecOperations,
    context: NativeModuleContext,
    project: File,
    buildDir: File,
    buildType: CargoBuildType,
    target: String = rustcCurrentTaget,
    env: Map<String, String>? = null,
    resolveMingwLibs: Boolean = false
): List<String> {
    val buildDirClean = buildDir.posixPath

    ensureTargetInstalled(execOps, context, target)

    val flags = execOps.exec(
        context,
        command = "cargo rustc --target=$target --target-dir=$buildDirClean --lib --${buildType.cargoName} -- --print=native-static-libs",
        workingDir = project,
        silent = true,
        errAsStd = true,
        env = env
    ).split("native-static-libs:").getOrNull(1)
        ?.split("\n")
        ?.getOrNull(0)
        ?.trim()
        ?.splitRespectingQuotes()
        ?.toMutableList()
        ?: return emptyList()

    if(OS.current == OS.WINDOWS) {
        flags.add(0, "-lsynchronization")
        if(resolveMingwLibs)
            flags.add(0, "-L${konanSysroot(KONAN_SYSROOT_MINGW)}/lib")
    }
    return flags
}

internal fun cargoBuild(
    execOps: ExecOperations,
    context: NativeModuleContext,
    project: File,
    buildDir: File,
    buildType: CargoBuildType,
    target: String = rustcCurrentTaget,
    additionalArgs: String = "",
    env: Map<String, String>? = null
): String {
    val buildDirClean = buildDir.posixPath

    ensureTargetInstalled(execOps, context, target)

    execOps.exec(context,
        command = "cargo build --target=$target --target-dir=$buildDirClean --lib --${buildType.cargoName} $additionalArgs",
        workingDir = project,
        errAsStd = true,
        env = env
    )
    return cargoTargetDir(buildDir, buildType, target)
}

internal fun wasmBindgenBuild(
    execOps: ExecOperations,
    context: NativeModuleContext,
    project: File,
    buildType: CargoBuildType,
    buildDir: File
): String {
    val buildDirClean = buildDir.posixPath
    val pkgDir = File(buildDir, "pkg").posixPath

    val target = "wasm32-unknown-unknown"
    val wasmFilePath = "${cargoTargetDir(buildDir, buildType, target)}/${context.moduleName}.wasm"
    val pkgWasmFilePath = File(pkgDir, "${context.moduleName}_bg.wasm").posixPath

    ensureTargetInstalled(execOps, context, target)
    ensureWasmBindgenInstalled(execOps, context)
    ensureWasmOptInstalled(execOps, context)

    execOps.exec(context,
        command = "cargo build --target $target --target-dir=$buildDirClean --lib --${buildType.cargoName}",
        workingDir = project,
        errAsStd = true
    )
    execOps.exec(context,
        command = "wasm-bindgen --target web --out-dir $pkgDir $wasmFilePath",
        workingDir = project,
        errAsStd = true
    )
    execOps.exec(context,
        command = "wasm-opt $pkgWasmFilePath -o $pkgWasmFilePath -O --enable-bulk-memory -O --enable-nontrapping-float-to-int",
        workingDir = project,
        errAsStd = true
    )
    return pkgDir
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
