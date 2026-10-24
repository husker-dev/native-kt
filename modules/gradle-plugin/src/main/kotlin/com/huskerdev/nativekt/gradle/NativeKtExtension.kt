@file:Suppress("JavaDefaultMethodsNotOverriddenByDelegation")

package com.huskerdev.nativekt.gradle

import com.huskerdev.nativekt.DebugKind
import com.huskerdev.nativekt.NativeKtModule
import com.huskerdev.nativekt.parseModuleFromToml
import com.huskerdev.stdlibx.fs.SyncPath
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import java.io.File

open class NativeKtExtension(
    val project: Project
) {
    val configureFiles = linkedSetOf<String>()
    val serializedModules = linkedSetOf<String>()

    var applyRuntime: Boolean = true
    var applyAndroidCriticalStub: Boolean = true

    var jvmNativesJarTask: Jar? = null

    var debug = arrayListOf<DebugKind>()

    fun add(name: String, configure: NativeKtModule.() -> Unit = {}): NativeKtModule? =
        addDir(project.projectDir.resolve("natives/$name"), configure)

    fun addDir(path: File, configure: NativeKtModule.() -> Unit = {}): NativeKtModule? =
        addFile(path.resolve("nativekt.toml"), configure)

    fun addFile(path: File, configure: NativeKtModule.() -> Unit = {}): NativeKtModule? {
        if(!path.exists()) {
            project.logger.error("""
                Could not load native module. Configuration file not found at: 
                    ${path.absolutePath}
            """.trimIndent())
            return null
        }
        val path = SyncPath(path.absolutePath)
        val module = parseModuleFromToml(path)
        configure(module)
        configureFiles += path.absoluteString()
        serializedModules += Json.encodeToString<NativeKtModule>(module)
        return module
    }
}