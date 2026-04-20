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

enum class Arch {
    X86,
    X64,
    ARM32,
    ARM64,
    RISCV32,
    RISCV64,
    UNKNOWN
    ;
    companion object {
        @JvmStatic fun current() = currentArch()
    }
}

internal expect fun currentOS(context: Any?): OS
internal expect fun currentArch(): Arch