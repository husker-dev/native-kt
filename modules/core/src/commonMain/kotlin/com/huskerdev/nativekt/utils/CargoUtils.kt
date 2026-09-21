package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.CargoBuildType
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.TargetType
import com.huskerdev.osutils.Arch
import com.huskerdev.osutils.OS
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.resolve

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
    context: NativeModuleContext,
    target: String
) {
    context.execute("rustup target list", silent = true)
        .split("\n")
        .firstOrNull {
            it.startsWith(target)
        }?.run {
            if(!endsWith("(installed)")) {
                println("Installing rust target: $target...")
                context.execute("rustup target add $target")
            }
        } ?: throw Exception("The $target is not supported on the current system (maybe try the nightly build?).")
}

internal fun ensureWasmBindgenInstalled(context: NativeModuleContext) {
    try {
        context.execute(
            command = "wasm-bindgen --version",
            silent = true
        )
    } catch (_: Exception) {
        println("Installing wasm-bindgen...")
        context.execute(
            command = "cargo install wasm-bindgen-cli",
            silent = true
        )
    }
}

internal fun ensureWasmOptInstalled(context: NativeModuleContext) {
    try {
        context.execute(
            command = "wasm-opt --version",
            silent = true
        )
    } catch (_: Exception) {
        println("Installing wasm-opt...")
        context.execute(
            command = "cargo install wasm-opt",
            silent = true
        )
    }
}

internal fun cargoTargetDir(
    buildDir: PlatformFile,
    buildType: CargoBuildType,
    target: String = rustcCurrentTaget
) = PlatformFile(buildDir, "$target/${buildType.cargoName}")

internal fun cargoLinkerFlags(
    context: NativeModuleContext,
    project: PlatformFile,
    buildDir: PlatformFile,
    buildType: CargoBuildType,
    target: String = rustcCurrentTaget,
    env: Map<String, String>? = null,
    isKN: Boolean = false
): List<String> {
    val buildDirClean = buildDir.posixPath

    ensureTargetInstalled(context, target)

    val flags = context.execute(
        command = "cargo rustc --target=$target --target-dir=$buildDirClean --lib --${buildType.cargoName} --color never -- --print=native-static-libs",
        workingDir = project,
        silent = true,
        errAsStd = true,
        environment = env
    ).split("native-static-libs:").getOrNull(1)
        ?.split("\n")
        ?.getOrNull(0)
        ?.trim()
        ?.splitRespectingQuotes()
        ?.toMutableList()
        ?: return emptyList()

    if(OS.current == OS.WINDOWS) {
        flags.removeAll { it == "-lsynchronization" }
        flags.add(0, "-lsynchronization")
        if(isKN)
            flags.add(0, "-L${konanSysroot(KONAN_SYSROOT_MINGW)}/x86_64-w64-mingw32/lib")
        else
            flags.add(0, "-L${locateMingw(context).posixPath}/lib")
    }
    if(OS.current == OS.MACOS) {
        flags -= "-lm"
        flags -= "-lc"
        flags -= "-ldl"
        flags -= "-lpthread"
        flags -= "-lresolv"
        flags -= "-ldispatch"
        flags -= "-lxpc"
        flags -= "-lcommonCrypto"
        flags -= "-lsys"
    }
    return flags
}

internal fun cargoBuild(
    context: NativeModuleContext,
    project: PlatformFile,
    buildDir: PlatformFile,
    buildType: CargoBuildType,
    target: String = rustcCurrentTaget,
    additionalArgs: String = "",
    env: Map<String, String>? = null
): PlatformFile {
    val buildDirClean = buildDir.posixPath

    ensureTargetInstalled(context, target)

    context.execute(
        command = "cargo build --target=$target --target-dir=$buildDirClean --lib --${buildType.cargoName} $additionalArgs",
        workingDir = project,
        errAsStd = true,
        environment = env
    )
    return cargoTargetDir(buildDir, buildType, target)
}

internal fun wasmBindgenBuild(
    context: NativeModuleContext,
    project: PlatformFile,
    buildType: CargoBuildType,
    buildDir: PlatformFile
): PlatformFile {
    val buildDirClean = buildDir.posixPath
    val pkgDir = buildDir.resolve("pkg")

    val target = "wasm32-unknown-unknown"
    val wasmFilePath = "${cargoTargetDir(buildDir, buildType, target).posixPath}/${context.moduleName}.wasm"
    val pkgWasmFilePath = pkgDir.resolve("${context.moduleName}_bg.wasm").posixPath

    ensureTargetInstalled(context, target)
    ensureWasmBindgenInstalled(context)
    ensureWasmOptInstalled(context)

    context.execute(
        command = "cargo build --target $target --target-dir=$buildDirClean --lib --${buildType.cargoName}",
        workingDir = project,
        errAsStd = true
    )
    context.execute(
        command = "wasm-bindgen --target web --out-dir ${pkgDir.posixPath} $wasmFilePath",
        workingDir = project,
        errAsStd = true
    )
    context.execute(
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
