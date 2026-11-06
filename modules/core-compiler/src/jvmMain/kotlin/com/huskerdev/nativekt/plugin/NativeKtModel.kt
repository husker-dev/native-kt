package com.huskerdev.nativekt.plugin

import java.io.Serializable

interface NativeKtModel {
    val configureFiles: List<String>
    val serializedModules: List<String>
    val buildDir: String

    companion object {
        operator fun invoke(
            configureFiles: List<String>,
            serializedModules: List<String>,
            buildDir: String
        ) = Impl(configureFiles, serializedModules, buildDir)
    }

    data class Impl(
        override val configureFiles: List<String>,
        override val serializedModules: List<String>,
        override val buildDir: String
    ): NativeKtModel, Serializable
}