package com.huskerdev.nativekt

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

enum class OS(
    val dylibExtension: String = "",
    val isUnixLike: Boolean = false,
    val isBrowser: Boolean = false,
) {
    MACOS(dylibExtension = "dylib", isUnixLike = true),
    IOS(dylibExtension = "dylib", isUnixLike = true),
    WATCHOS(dylibExtension = "dylib", isUnixLike = true),
    TVOS(dylibExtension = "dylib", isUnixLike = true),

    WINDOWS(dylibExtension = "dll"),

    LINUX(dylibExtension = "so", isUnixLike = true),
    ANDROID(dylibExtension = "so", isUnixLike = true),
    WEAROS(dylibExtension = "so", isUnixLike = true),
    ANDROID_TV(dylibExtension = "so", isUnixLike = true),

    JS(isBrowser = true),
    WASM(isBrowser = true),

    UNKNOWN
    ;
    companion object {
        @JvmOverloads
        @JvmStatic
        fun current(context: Any? = null) = currentOS(context)
    }
}

enum class Arch(
    val ptr64: Boolean
) {
    X86(false),
    X64(true),
    ARM32(false),
    ARM64(true),
    RISCV32(false),
    RISCV64(true),
    UNKNOWN(false)
    ;
    companion object {
        @JvmStatic val current = currentArch()
    }
}

internal expect fun currentOS(context: Any?): OS
internal expect fun currentArch(): Arch