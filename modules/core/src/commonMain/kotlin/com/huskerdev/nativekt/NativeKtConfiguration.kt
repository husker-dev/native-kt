@file:Suppress("CanSealedSubClassBeObject")

package com.huskerdev.nativekt

import io.github.vinceglb.filekit.*
import kotlinx.serialization.*


interface NativeKtMultiplatformConfiguration:
    NativeKtNativeConfiguration,
    NativeKtAndroidConfiguration,
    NativeKtJsConfiguration,
    NativeKtJvmConfiguration,
    NativeKtConfiguration
{
    @Serializable
    @SerialName("multiplatform")
    class Impl(
        override var debug: MutableList<DebugKind> = NativeKtConfiguration.DEFAULT_DEBUG.toMutableList(),
        override var useCoroutines: Boolean = NativeKtConfiguration.DEFAULT_USE_COROUTINES,
        override var applyRuntime: Boolean = NativeKtConfiguration.DEFAULT_APPLY_RUNTIME,
        override var ndkVersion: String? = NativeKtAndroidConfiguration.DEFAULT_NDK_VERSION,
        override var androidTargets: MutableList<String> = NativeKtAndroidConfiguration.DEFAULT_ANDROID_TARGETS.toMutableList(),
        override var useAndroidCriticalNative: Boolean = NativeKtAndroidConfiguration.DEFAULT_USE_ANDROID_CRITICAL_NATIVE,
        override var applyAndroidCriticalStub: Boolean = NativeKtAndroidConfiguration.DEFAULT_APPLY_DEFAULT_CRITICAL_STUB,
        override var useJsBigInt: Boolean = NativeKtJsConfiguration.DEFAULT_USE_JS_BIGINT,
        override var useJvmRecord: Boolean = NativeKtJvmConfiguration.DEFAULT_USE_JVM_RECORD,
        override var useUniversalMacOSLib: Boolean = NativeKtJvmConfiguration.DEFAULT_USE_UNIVERSAL_MACOS_LIB,
        override var useJNI: Boolean = NativeKtJvmConfiguration.DEFAULT_USE_JNI,
        override var useForeignApi: Boolean = NativeKtJvmConfiguration.DEFAULT_USE_FOREIGN_API,
        override var useJVMCI: Boolean = NativeKtJvmConfiguration.DEFAULT_USE_JVMCI,
    ): NativeKtConfiguration.Impl, NativeKtMultiplatformConfiguration

}

interface NativeKtNativeConfiguration: NativeKtConfiguration {
    @Serializable
    @SerialName("native")
    class Impl(
        override var debug: MutableList<DebugKind> = NativeKtConfiguration.DEFAULT_DEBUG.toMutableList(),
        override var useCoroutines: Boolean = NativeKtConfiguration.DEFAULT_USE_COROUTINES,
        override var applyRuntime: Boolean = NativeKtConfiguration.DEFAULT_APPLY_RUNTIME,
    ): NativeKtNativeConfiguration, NativeKtConfiguration.Impl
}

interface NativeKtAndroidConfiguration: NativeKtConfiguration {
    var ndkVersion: String?
    var androidTargets: MutableList<String>

    var useAndroidCriticalNative: Boolean
    var applyAndroidCriticalStub: Boolean

    companion object {
        val DEFAULT_NDK_VERSION: String? = null
        val DEFAULT_ANDROID_TARGETS = listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        const val DEFAULT_APPLY_DEFAULT_CRITICAL_STUB = true
        const val DEFAULT_USE_ANDROID_CRITICAL_NATIVE = true
    }
    @Serializable
    @SerialName("android")
    class Impl(
        override var ndkVersion: String? = DEFAULT_NDK_VERSION,
        override var androidTargets: MutableList<String> = DEFAULT_ANDROID_TARGETS.toMutableList(),
        override var applyAndroidCriticalStub: Boolean = DEFAULT_APPLY_DEFAULT_CRITICAL_STUB,
        override var useAndroidCriticalNative: Boolean = DEFAULT_USE_ANDROID_CRITICAL_NATIVE,
        override var debug: MutableList<DebugKind> = NativeKtConfiguration.DEFAULT_DEBUG.toMutableList(),
        override var useCoroutines: Boolean = NativeKtConfiguration.DEFAULT_USE_COROUTINES,
        override var applyRuntime: Boolean = NativeKtConfiguration.DEFAULT_APPLY_RUNTIME,
    ): NativeKtAndroidConfiguration, NativeKtConfiguration.Impl
}

interface NativeKtJsConfiguration: NativeKtConfiguration {
    var useJsBigInt: Boolean

    companion object {
        const val DEFAULT_USE_JS_BIGINT = false
    }
    @Serializable
    @SerialName("js")
    open class Impl(
        override var useJsBigInt: Boolean = DEFAULT_USE_JS_BIGINT,
        override var debug: MutableList<DebugKind> = NativeKtConfiguration.DEFAULT_DEBUG.toMutableList(),
        override var useCoroutines: Boolean = NativeKtConfiguration.DEFAULT_USE_COROUTINES,
        override var applyRuntime: Boolean = NativeKtConfiguration.DEFAULT_APPLY_RUNTIME,
    ): NativeKtJsConfiguration, NativeKtConfiguration.Impl
}

interface NativeKtJvmConfiguration: NativeKtConfiguration {
    var useJvmRecord: Boolean
    var useUniversalMacOSLib: Boolean

    var useJNI: Boolean
    var useForeignApi: Boolean
    var useJVMCI: Boolean

    companion object {
        const val DEFAULT_USE_JVM_RECORD = true
        const val DEFAULT_USE_UNIVERSAL_MACOS_LIB = false
        const val DEFAULT_USE_JNI = true
        const val DEFAULT_USE_FOREIGN_API = true
        const val DEFAULT_USE_JVMCI = true
    }
    @Serializable
    @SerialName("jvm")
    open class Impl(
        override var useJvmRecord: Boolean = DEFAULT_USE_JVM_RECORD,
        override var useUniversalMacOSLib: Boolean = DEFAULT_USE_UNIVERSAL_MACOS_LIB,
        override var useJNI: Boolean = DEFAULT_USE_JNI,
        override var useForeignApi: Boolean = DEFAULT_USE_FOREIGN_API,
        override var useJVMCI: Boolean = DEFAULT_USE_JVMCI,
        override var debug: MutableList<DebugKind> = NativeKtConfiguration.DEFAULT_DEBUG.toMutableList(),
        override var useCoroutines: Boolean = NativeKtConfiguration.DEFAULT_USE_COROUTINES,
        override var applyRuntime: Boolean = NativeKtConfiguration.DEFAULT_APPLY_RUNTIME,
    ): NativeKtJvmConfiguration, NativeKtConfiguration.Impl
}

interface NativeKtConfiguration {
    var debug: MutableList<DebugKind>
    var useCoroutines: Boolean
    var applyRuntime: Boolean

    companion object {
        val DEFAULT_DEBUG = listOf<DebugKind>()
        const val DEFAULT_USE_COROUTINES = true
        const val DEFAULT_APPLY_RUNTIME = true
    }
    @Serializable
    sealed interface Impl
}


// ==============
//     Debug
// ==============

@Suppress("unused")
enum class DebugKind {
    PRINT_EXEC,
    PRINT_LINKER_OPTIONS
}

// ==============
//  Build system
// ==============

@Serializable
sealed interface BuildSystem {
    val language: Language

    @Serializable
    @SerialName("cmake")
    open class CMake(
        /**
         * CMake target language
         *
         * Default value: C
         */
        override val language: Language
    ): BuildSystem {

        /**
         * Generated header file.
         *
         * Default value: `natives/[name]/include/api.*`
         */
        var headerFile: PlatformFile? = null

        fun headerFile(module: NativeProject) =
            headerFile ?: module.dir.resolve("include/api.${language.headerExtension}")

        /**
         * CMake build type.
         *
         * Default value: `RELEASE`
         */
        var buildType: CMakeBuildType = CMakeBuildType.RELEASE

        /**
         * Cmake command-line args
         */
        var args = arrayListOf<String>()
    }

    @Serializable
    @SerialName("cargo")
    open class Cargo: BuildSystem {
        override val language: Language = Language.RUST

        var printApi: Boolean = false

        var buildType: CargoBuildType = CargoBuildType.RELEASE

        var apiRsFile: PlatformFile? = null

        fun apiRsFile(module: NativeProject) =
            apiRsFile ?: module.dir.resolve("src/nativekt.rs")
    }
}

// ==============
//    Modules
// ==============

@Serializable
sealed class NativeProject {

    abstract val name: String

    /**
     * Directory with CMake project.
     *
     * Default value: `natives/[name]`
     */
    abstract var dir: PlatformFile

    var buildSystem: BuildSystem = BuildSystem.CMake(Language.C)
        private set

    /**
     * NDL file
     *
     * Default value: `natives/[name]/api.ndl`
     */
    var ndlFile: PlatformFile? = null

    fun resolveNdlFile() = ndlFile ?: dir.resolve("api.ndl")

    /**
     * Classpath where bindings will be generated.
     *
     * Default value: `natives.[name]`
     */
    var classPath: String? = null

    fun resolveClassPath() = classPath ?: "natives.$name"

    /**
     *
     */
    var jsTarget: JsTarget = JsTarget.WEB

    fun cmake(language: Language = Language.C, configure: BuildSystem.CMake.() -> Unit = {}) {
        val buildSystem = BuildSystem.CMake(language)
        buildSystem.configure()
        this.buildSystem = buildSystem
    }

    fun cargo(configure: BuildSystem.Cargo.() -> Unit = {}) {
        val buildSystem = BuildSystem.Cargo()
        buildSystem.configure()
        this.buildSystem = buildSystem
    }
}

@Serializable
@SerialName("multiplatform")
open class Multiplatform(
    override val name: String,
    override var dir: PlatformFile
): NativeProject() {

    /**
     * SourceSet that will have 'expect' api
     */
    var commonSourceSet: String = "commonMain"

    /**
     * SourceSets with stub
     */
    val stubSourceSets = arrayListOf<String>()

    /**
     * SourceSets with implementation
     */
    var targetSourceSets = setOf(
        "jvmMain",
        "jsMain",
        "wasmJsMain",
        "androidMain",

        "mingwX64Main",

        "macosArm64Main",
        "macosX64Main",

        "linuxArm64Main",
        "linuxX64Main",

        "iosArm64Main",
        "iosX64Main",
        "iosSimulatorArm64Main",

        "watchosX64Main",
        "watchosArm32Main",
        "watchosArm64Main",
        "watchosDeviceArm64Main",
        "watchosSimulatorArm64Main",

        "tvosX64Main",
        "tvosArm64Main",
        "tvosSimulatorArm64Main",

        "androidNativeX64Main",
        "androidNativeX86Main",
        "androidNativeArm32Main",
        "androidNativeArm64Main"
    )
}

@Serializable
@SerialName("single")
class SinglePlatform(
    override val name: String,
    override var dir: PlatformFile
): NativeProject() {

    /**
     * SourceSet with implementation
     */
    var targetSourceSet: String = "jvmMain"
}

@Suppress("unused")
enum class Language(
    val sourceExtension: String? = null,
    val headerExtension: String? = null
) {
    C("c", "h"),
    CPP("cpp", "hpp"),
    RUST
}

@Suppress("unused")
enum class CMakeBuildType(
    val cmakeName: String
) {
    DEBUG("Debug"),
    REL_WITH_DEB_INFO("RelWithDebInfo"),
    RELEASE("Release"),
    MIN_SIZE_REL("MinSizeRel")
}

@Suppress("unused")
enum class CargoBuildType(
    val cargoName: String
) {
    RELEASE("release"),
    DEBUG("debug")
}

@Suppress("unused")
enum class JsTarget {
    WEB,
    NODE
}