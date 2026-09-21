package com.huskerdev.nativekt.plugin

import com.huskerdev.nativekt.*
import io.github.vinceglb.filekit.PlatformFile
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.provideDelegate
import java.io.File
import javax.inject.Inject


@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
open class NativeKtMultiplatformExtension @Inject @JvmOverloads constructor(
    objects: ObjectFactory,
    projectDir: File,
    internal val impl: NativeKtConfiguration = NativeKtMultiplatformConfiguration.Impl()
): ExtensiblePolymorphicDomainObjectContainer<WrappedNativeProject<*>> by objects.polymorphicDomainObjectContainer(WrappedNativeProject::class.java),
    NativeKtMultiplatformConfiguration by impl as NativeKtMultiplatformConfiguration,
    JarTaskContainer
{
    override var jvmNativesJarTask: Jar? = null

    init {
        registerFactory(WrappedMultiplatform::class.java) { name ->
            objects.newInstance(WrappedMultiplatform::class.java, name, projectDir.resolve("natives/$name"))
        }
        registerFactory(WrappedSinglePlatform::class.java) { name ->
            objects.newInstance(WrappedSinglePlatform::class.java, name, projectDir.resolve("natives/$name"))
        }
        registerFactory(WrappedNativeProject::class.java) { name ->
            objects.newInstance(WrappedMultiplatform::class.java, name, projectDir.resolve("natives/$name"))
        }
    }
}

@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
open class NativeKtJvmExtension @Inject @JvmOverloads constructor(
    objects: ObjectFactory,
    projectDir: File,
    internal val impl: NativeKtConfiguration = NativeKtJvmConfiguration.Impl()
): ExtensiblePolymorphicDomainObjectContainer<WrappedSinglePlatform> by objects.polymorphicDomainObjectContainer(WrappedSinglePlatform::class.java),
    NativeKtJvmConfiguration by impl as NativeKtJvmConfiguration,
    JarTaskContainer
{
    override var jvmNativesJarTask: Jar? = null

    init {
        registerFactory(WrappedSinglePlatform::class.java) { name ->
            objects.newInstance(WrappedSinglePlatform::class.java, name, projectDir.resolve("natives/$name")).also {
                it.targetSourceSet = "main"
            }
        }
    }
}

@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
open class NativeKtJsExtension @Inject @JvmOverloads constructor(
    objects: ObjectFactory,
    projectDir: File,
    internal val impl: NativeKtConfiguration = NativeKtJsConfiguration.Impl()
): ExtensiblePolymorphicDomainObjectContainer<WrappedSinglePlatform> by objects.polymorphicDomainObjectContainer(WrappedSinglePlatform::class.java),
    NativeKtJsConfiguration by impl as NativeKtJsConfiguration
{
    init {
        registerFactory(WrappedSinglePlatform::class.java) { name ->
            objects.newInstance(WrappedSinglePlatform::class.java, name, projectDir.resolve("natives/$name")).also {
                it.targetSourceSet = "main"
            }
        }
    }
}

interface JarTaskContainer {
    var jvmNativesJarTask: Jar?
}

// Wrapped objects

open class WrappedNativeProject<T: NativeProject>(val instance: T): Named {
    override fun getName(): String = instance.name

    /**
     * Directory with CMake project.
     *
     * Default value: `natives/[name]`
     */
    var dir: File by JavaFile(instance::dir)

    /**
     * NDL file
     *
     * Default value: `natives/[name]/api.ndl`
     */
    var ndlFile: File? by JavaFile(instance::ndlFile)

    /**
     * Classpath where bindings will be generated.
     *
     * Default value: `natives.[name]`
     */
    var classPath: String? by instance::classPath

    var jsTarget: JsTarget by instance::jsTarget

    fun cmake(language: Language = Language.C, configure: WrappedCMake.() -> Unit = {}) =
        instance.cmake(language) { configure(WrappedCMake(this)) }

    fun cargo(configure: WrappedCargo.() -> Unit = {}) =
        instance.cargo { configure(WrappedCargo(this)) }
}

open class WrappedMultiplatform @Inject constructor(
    name: String,
    dir: File
): WrappedNativeProject<Multiplatform>(
    Multiplatform(name, PlatformFile(dir))
) {
    /**
     * SourceSet that will have 'expect' api
     */
    var commonSourceSet: String by instance::commonSourceSet

    /**
     * SourceSets with stub
     */
    val stubSourceSets: ArrayList<String> by instance::stubSourceSets

    /**
     * SourceSets with implementation
     */
    var targetSourceSets: Set<String> by instance::targetSourceSets
}

open class WrappedSinglePlatform @Inject constructor(
    name: String,
    dir: File
): WrappedNativeProject<SinglePlatform>(
    SinglePlatform(name, PlatformFile(dir))
) {
    var targetSourceSet: String by instance::targetSourceSet
}

// BuildSystem

abstract class WrappedBuildSystem<T: BuildSystem>(val instance: T)

open class WrappedCMake(instance: BuildSystem.CMake): WrappedBuildSystem<BuildSystem.CMake>(instance) {

    /**
     * CMake target language
     *
     * Default value: C
     */
    val language: Language by instance::language

    /**
     * Generated header file.
     *
     * Default value: `natives/[name]/include/api.*`
     */
    var headerFile: File? by JavaFile(instance::headerFile)

    /**
     * CMake build type.
     *
     * Default value: `RELEASE`
     */
    var buildType: CMakeBuildType by instance::buildType

    /**
     * Cmake command-line args
     */
    var args: ArrayList<String> by instance::args
}

open class WrappedCargo(instance: BuildSystem.Cargo): WrappedBuildSystem<BuildSystem.Cargo>(instance) {
    var printApi: Boolean by instance::printApi

    var buildType: CargoBuildType by instance::buildType

    var apiRsFile: File? by JavaFile(instance::apiRsFile)
}