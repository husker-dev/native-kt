package com.huskerdev.nativekt.intellij

import com.huskerdev.stdlibx.fs.SyncPath
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.vfs.VirtualFile


fun VirtualFile.asSyncPath() = SyncPath(path)

fun isRustPluginEnabled(): Boolean =
    PluginManagerCore.isLoaded(PluginId.getId("com.jetbrains.rust"))
