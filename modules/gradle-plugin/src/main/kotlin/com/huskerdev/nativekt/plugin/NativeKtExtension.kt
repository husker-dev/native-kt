package com.huskerdev.nativekt.plugin

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.jvm.tasks.Jar
import javax.inject.Inject

const val NDK_LATEST = "~latest~"

@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
open class NativeKtMultiplatformExtension @Inject constructor(
    objects: ObjectFactory
): ExtensiblePolymorphicDomainObjectContainer<NativeModule> by objects.polymorphicDomainObjectContainer(NativeModule::class.java),
    NativeKtJvmInterface, NativeKtJsInterface, NativeKtAndroidInterface, NativeKtNativeInterface, NativeKtCommonInterface
{
    init {
        registerFactory(Multiplatform::class.java) { name ->
            objects.newInstance(Multiplatform::class.java, name)
        }
        registerFactory(SinglePlatform::class.java) { name ->
            objects.newInstance(SinglePlatform::class.java, name)
        }
        registerFactory(NativeModule::class.java) { name ->
            objects.newInstance(Multiplatform::class.java, name)
        }
    }

    // JVM
    override var useJvmRecord = true
    override var useUniversalMacOSLib = false

    override var useJNI = true
    override var useForeignApi = true
    override var useJVMCI = true

    override var jvmNativesJarTask: Jar? = null

    // Android
    override var ndkVersion: String = NDK_LATEST
    override var androidTargets = arrayListOf("arm64-v8a", "armeabi-v7a", "x86_64")

    override var applyAndroidCriticalStub = true
    override var useAndroidCriticalNative = true

    // JS
    override var useJsBigInt = false

    // Common
    override var useCoroutines = true
    override var applyRuntime = true
}

@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
open class NativeKtJvmExtension @Inject constructor(
    objects: ObjectFactory
): ExtensiblePolymorphicDomainObjectContainer<SinglePlatform> by objects.polymorphicDomainObjectContainer(SinglePlatform::class.java),
    NativeKtJvmInterface
{
    init {
        registerFactory(SinglePlatform::class.java) { name ->
            objects.newInstance(SinglePlatform::class.java, name).also {
                it.targetSourceSet = "main"
            }
        }
    }
    override var useCoroutines = true
    override var useJvmRecord = true

    override var useJNI = true
    override var useForeignApi = true
    override var useJVMCI = true

    override var useUniversalMacOSLib = false

    override var applyRuntime = true

    override var jvmNativesJarTask: Jar? = null
}

@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
open class NativeKtJsExtension @Inject constructor(
    objects: ObjectFactory
): ExtensiblePolymorphicDomainObjectContainer<SinglePlatform> by objects.polymorphicDomainObjectContainer(SinglePlatform::class.java),
    NativeKtJsInterface
{
    init {
        registerFactory(SinglePlatform::class.java) { name ->
            objects.newInstance(SinglePlatform::class.java, name).also {
                it.targetSourceSet = "main"
            }
        }
    }

    override var useCoroutines = true
    override var applyRuntime = true

    override var useJsBigInt = false
}

interface NativeKtNativeInterface: NativeKtCommonInterface

interface NativeKtAndroidInterface: NativeKtCommonInterface {
    var ndkVersion: String
    var androidTargets: ArrayList<String>

    var applyAndroidCriticalStub: Boolean
    var useAndroidCriticalNative: Boolean
}

interface NativeKtJsInterface: NativeKtCommonInterface {
    var useJsBigInt: Boolean
}

interface NativeKtJvmInterface: NativeKtCommonInterface {
    var useJvmRecord: Boolean
    var useUniversalMacOSLib: Boolean

    var useJNI: Boolean
    var useForeignApi: Boolean
    var useJVMCI: Boolean

    var jvmNativesJarTask: Jar?
}

interface NativeKtCommonInterface {
    var useCoroutines: Boolean
    var applyRuntime: Boolean
}

// ==============
//    Modules
// ==============

sealed class NativeModule @Inject constructor(
    @get:JvmName("_name")
    val name: String
): Named {
    override fun getName(): String = name

    /**
     * Directory with CMake project.
     *
     * Default value: `src/nativeInterop/[name]`
     */
    var projectDir: RegularFileProperty? = null

    /**
     * CMake build type.
     *
     * Default value: `RELEASE`
     */
    var buildType: CMakeBuildType = CMakeBuildType.RELEASE

    /**
     * Cmake command-line args
     */
    var cmakeArgs = arrayListOf<String>()

    /**
     * Classpath where bindings will be generated.
     *
     * Default value: `natives.[name]`
     */
    var classPath: String = "natives.$name"
}

open class Multiplatform @Inject constructor(
    name: String
): NativeModule(name) {

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

open class SinglePlatform @Inject constructor(
    name: String
): NativeModule(name) {

    /**
     * SourceSet with implementation
     */
    var targetSourceSet: String = "jvmMain"
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