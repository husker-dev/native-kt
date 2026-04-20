package com.huskerdev.nativekt

internal actual fun currentOS(context: Any?) = OS.WASM
internal actual fun currentArch() = Arch.UNKNOWN