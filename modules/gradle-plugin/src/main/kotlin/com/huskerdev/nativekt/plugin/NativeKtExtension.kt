package com.huskerdev.nativekt.plugin

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.io.Serializable
import javax.inject.Inject


@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
open class NativeKtMultiplatformExtension @Inject @JvmOverloads constructor(
    objects: ObjectFactory,
    projectDir: File,
    internal val impl: NativeKtCommonInterface = NativeKtMultiplatformInterface.Impl()
): ExtensiblePolymorphicDomainObjectContainer<NativeProject> by objects.polymorphicDomainObjectContainer(NativeProject::class.java),
    NativeKtMultiplatformInterface by impl as NativeKtMultiplatformInterface
{
    init {
        registerFactory(Multiplatform::class.java) { name ->
            objects.newInstance(Multiplatform::class.java, name, projectDir.resolve("natives/$name"))
        }
        registerFactory(SinglePlatform::class.java) { name ->
            objects.newInstance(SinglePlatform::class.java, name, projectDir.resolve("natives/$name"))
        }
        registerFactory(NativeProject::class.java) { name ->
            objects.newInstance(Multiplatform::class.java, name, projectDir.resolve("natives/$name"))
        }
    }
}

@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
open class NativeKtJvmExtension @Inject @JvmOverloads constructor(
    objects: ObjectFactory,
    projectDir: File,
    internal val impl: NativeKtCommonInterface = NativeKtJvmInterface.Impl()
): ExtensiblePolymorphicDomainObjectContainer<SinglePlatform> by objects.polymorphicDomainObjectContainer(SinglePlatform::class.java),
    NativeKtJvmInterface by impl as NativeKtJvmInterface,
    NativeKtCommonInterface by impl
{
    init {
        registerFactory(SinglePlatform::class.java) { name ->
            objects.newInstance(SinglePlatform::class.java, name, projectDir.resolve("natives/$name")).also {
                it.targetSourceSet = "main"
            }
        }
    }
}

@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
open class NativeKtJsExtension @Inject @JvmOverloads constructor(
    objects: ObjectFactory,
    projectDir: File,
    internal val impl: NativeKtCommonInterface = NativeKtJsInterface.Impl()
): ExtensiblePolymorphicDomainObjectContainer<SinglePlatform> by objects.polymorphicDomainObjectContainer(SinglePlatform::class.java),
    NativeKtJsInterface by impl as NativeKtJsInterface,
    NativeKtCommonInterface by impl
{
    init {
        registerFactory(SinglePlatform::class.java) { name ->
            objects.newInstance(SinglePlatform::class.java, name, projectDir.resolve("natives/$name")).also {
                it.targetSourceSet = "main"
            }
        }
    }
}

// ==================
//     Interfaces
// ==================

interface NativeKtMultiplatformInterface:
    NativeKtCommonInterface,
    NativeKtJvmInterface,
    NativeKtJsInterface,
    NativeKtAndroidInterface,
    NativeKtNativeInterface
{
    class Impl: NativeKtMultiplatformInterface,
        NativeKtJvmInterface by NativeKtJvmInterface.Defaults(),
        NativeKtJsInterface by NativeKtJsInterface.Defaults(),
        NativeKtAndroidInterface by NativeKtAndroidInterface.Defaults(),
        NativeKtNativeInterface by NativeKtNativeInterface.Defaults(),
        NativeKtCommonInterface by NativeKtCommonInterface.Defaults(),
        Serializable
}

interface NativeKtNativeInterface {
    class Defaults: NativeKtNativeInterface, Serializable
}

interface NativeKtAndroidInterface {
    var ndkVersion: String?
    var androidTargets: ArrayList<String>

    var useAndroidCriticalNative: Boolean
    var applyAndroidCriticalStub: Boolean

    class Defaults: NativeKtAndroidInterface, Serializable {
        override var ndkVersion: String? = null
        override var androidTargets = arrayListOf("arm64-v8a", "armeabi-v7a", "x86_64")

        override var applyAndroidCriticalStub = true
        override var useAndroidCriticalNative = true
    }
}

interface NativeKtJsInterface {
    var useJsBigInt: Boolean

    open class Defaults: NativeKtJsInterface, Serializable {
        override var useJsBigInt = false
    }
    class Impl: Defaults(), NativeKtCommonInterface by NativeKtCommonInterface.Defaults()
}

interface NativeKtJvmInterface {
    var useJvmRecord: Boolean
    var useUniversalMacOSLib: Boolean

    var useJNI: Boolean
    var useForeignApi: Boolean
    var useJVMCI: Boolean

    var jvmNativesJarTask: Jar?

    open class Defaults: NativeKtJvmInterface, Serializable {
        override var useJvmRecord = true
        override var useUniversalMacOSLib = false

        override var useJNI = true
        override var useForeignApi = true
        override var useJVMCI = true

        @Transient override var jvmNativesJarTask: Jar? = null
    }
    class Impl: Defaults(), NativeKtCommonInterface by NativeKtCommonInterface.Defaults()
}

interface NativeKtCommonInterface {
    var debug: MutableList<DebugKind>
    var useCoroutines: Boolean
    var applyRuntime: Boolean

    class Defaults: NativeKtCommonInterface, Serializable {
        override var debug = mutableListOf<DebugKind>()
        override var useCoroutines = true
        override var applyRuntime = true
    }
}

// ==============
//     Debug
// ==============

enum class DebugKind {
    PRINT_EXEC
}

// ==============
//  Build system
// ==============

sealed interface BuildSystem: Serializable {
    val language: Language

    open class CMake(
        private val module: NativeProject,

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
        var headerFile: File? = null

        fun headerFile() = headerFile ?: module.projectDir.resolve("include/api.${language.headerExtension}")

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

    open class Cargo(
        private val module: NativeProject
    ): BuildSystem {
        override val language: Language = Language.RUST

        var printApi: Boolean = false

        var buildType: CargoBuildType = CargoBuildType.RELEASE

        var apiRsFile: File? = null

        fun apiRsFile() = apiRsFile ?: module.projectDir.resolve("src/nativekt.rs")
    }
}

// ==============
//    Modules
// ==============

sealed class NativeProject @Inject constructor(
    private val _name: String,
    defaultDir: File
): Named, Serializable {
    override fun getName(): String = _name

    /**
     * Directory with CMake project.
     *
     * Default value: `natives/[name]`
     */
    var projectDir: File = defaultDir

    var buildSystem: BuildSystem = BuildSystem.CMake(this, Language.C)
        private set

    /**
     * NDL file
     *
     * Default value: `natives/[name]/api.ndl`
     */
    var ndlFile: File? = null

    fun ndlFile() = ndlFile ?: projectDir.resolve("api.ndl")

    /**
     * Classpath where bindings will be generated.
     *
     * Default value: `natives.[name]`
     */
    var classPath: String = "natives.$name"

    var jsTarget: JsTarget = JsTarget.WEB

    fun cmake(language: Language = Language.C, configure: BuildSystem.CMake.() -> Unit = {}) {
        val buildSystem = BuildSystem.CMake(this, language)
        buildSystem.configure()
        this.buildSystem = buildSystem
    }

    fun cargo(configure: BuildSystem.Cargo.() -> Unit = {}) {
        val buildSystem = BuildSystem.Cargo(this)
        buildSystem.configure()
        this.buildSystem = buildSystem
    }
}

abstract class Multiplatform @Inject constructor(
    name: String,
    dir: File
): NativeProject(name, dir), Serializable {

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

abstract class SinglePlatform @Inject constructor(
    name: String,
    dir: File
): NativeProject(name, dir), Serializable {

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