package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.*
import dev.scottpierce.envvar.*
import io.github.vinceglb.filekit.*

internal fun getNdkDir(
    extension: NativeKtAndroidConfiguration,
    existingSdkDir: PlatformFile?
): PlatformFile {
    val sdk = existingSdkDir
        ?: EnvVar["ANDROID_SDK_ROOT"]?.run { PlatformFile(this) }
        ?: throw NullPointerException("Could not find Android SDK. Environment variable 'ANDROID_SDK_ROOT' is not specified.")

    return if(extension.ndkVersion == null) {
        sdk.list()
            .maxByOrNull { it.name }
            ?: throw UnsupportedOperationException("Can not get latest NDK, because no NDK are installed")
    } else {
        val dir = sdk.resolve("ndk/${extension.ndkVersion}")

        if (!dir.exists()) {
            val available = arrayListOf<String>()
            if (dir.parent()!!.exists())
                available += dir.parent()!!.list().map { it.name }

            var message = "NDK ${extension.ndkVersion} is not installed."
            if (available.isNotEmpty())
                message += " Available:\n\t- ${available.joinToString("\n\t- ")}"

            throw UnsupportedOperationException(message)
        } else dir
    }
}

internal fun toAndroidLlvmTarget(target: String, rustc: Boolean = false) = when(target) {
    "x86_64"      -> "x86_64-linux-android"
    "x86"         -> "i686-linux-android"
    "armeabi-v7a" ->  if(rustc) "armv7-linux-androideabi" else "armv7a-linux-androideabi"
    "arm64-v8a"   -> "aarch64-linux-android"
    else -> throw UnsupportedOperationException("Unsupported Android target: $target")
}