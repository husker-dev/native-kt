package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.stdlibx.OS
import com.huskerdev.stdlibx.fs.SyncPath


internal fun locate(context: NativeModuleContext, binary: String): SyncPath? {
    return try {
        SyncPath(context.execute(
            command = when(OS.current) {
                OS.WINDOWS -> "where $binary"
                else -> "which $binary"
            },
            silent = true
        )).run {
            if (exists()) this else null
        }
    } catch (_: Throwable) {
        null
    }
}