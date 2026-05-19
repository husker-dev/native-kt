package com.huskerdev.nativekt.plugin

import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.configurators.*
import com.huskerdev.nativekt.printers.c.CHeaderPrinter
import com.huskerdev.nativekt.printers.rust.RustPrinter
import com.huskerdev.nativekt.utils.validateIDL
import com.huskerdev.nativekt.utils.dir
import com.huskerdev.nativekt.utils.getHeaderFile
import com.huskerdev.nativekt.utils.idl
import com.huskerdev.nativekt.utils.getNDLFile
import com.huskerdev.webidl.resolver.IdlResolver
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleJavaTargetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetsContainer
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import kotlin.collections.set
import kotlin.concurrent.getOrSet

private const val RUNTIME_DEPENDENCY = "com.huskerdev:native-kt-runtime:${NativeKtInfo.VERSION}"

private val tmpCommonTasks = ThreadLocal<MutableMap<NativeProject, TaskProvider<*>>>()

private fun NativeKtPlugin.validateModule(module: NativeProject): IdlResolver? {
    val initTask = project.tasks.register("cmakeInit${module.name.capitalized()}", InitTask::class.java)
    initTask.get().apply {
        this.dir = module.dir(project).absolutePath
        this.moduleName = module.name
    }

    project.gradle.taskGraph.whenReady {
        if (!module.getNDLFile(project).exists() && !hasTask(initTask.get())) {
            project.logger.error("""
                Native module '${module.name}' is not loaded:
                  'api.ndl' file not found.
                
                Possible solution: 
                  run './gradlew :${initTask.name}'
            """.trimIndent())
        }
    }

    if(!module.getNDLFile(project).exists())
        return null

    return module.idl(project)
        .also { validateIDL(it) }
}

fun NativeKtPlugin.configureKotlin(
    nativesBuildDir: File,
    srcGenDir: File
){
    extension.whenObjectAdded {
        val module = this as NativeProject

        val idl = validateModule(module)
            ?: return@whenObjectAdded

        val nativesBuildDir = File(nativesBuildDir, module.name)
        val srcGenDir = File(srcGenDir, module.name)

        when(module) {
            is Multiplatform -> configureMultiplatform(idl, nativesBuildDir, srcGenDir, module)
            is SinglePlatform -> configureSinglePlatform(idl, nativesBuildDir, srcGenDir, module)
        }

        when(module.buildSystem) {
            is BuildSystem.CMake -> {
                CHeaderPrinter(
                    idl = idl,
                    target = module.getHeaderFile(project),
                    guardName = module.name.uppercase()
                )
            }
            is BuildSystem.Cargo -> {
                RustPrinter(
                    idl = idl,
                    target = module.getHeaderFile(project)
                )
            }
        }
    }
}

fun NativeKtPlugin.configureAndroid(
    nativesBuildDir: File,
    srcGenDir: File
) {
    val androidComponents = project.the<KotlinMultiplatformAndroidComponentsExtension>()

    androidComponents.finalizeDsl { androidExtension ->
        extension.forEach { module ->
            module as NativeProject

            if(!module.getNDLFile(project).exists())
                return@forEach

            val idl = module.idl(project)

            val nativesBuildDir = File(nativesBuildDir, module.name)
            val srcGenDir = File(srcGenDir, module.name)

            when(module) {
                is Multiplatform -> {
                    val commonTask = tmpCommonTasks.get()?.remove(module)
                    module.getActiveSourceSets(kotlin)
                        .filter {
                            getTargetType(kotlin, it) == TargetType.ANDROID
                        }
                        .forEach {
                            configureAndroidSourceSet(project, extension as NativeKtAndroidInterface, androidExtension, commonTask, idl, module, it, srcGenDir, nativesBuildDir, true)
                        }
                }
                is SinglePlatform -> {
                    val sourceSet = kotlin.findSourceSet(module.targetSourceSet)

                    if(getTargetType(kotlin, sourceSet) == TargetType.ANDROID)
                        configureAndroidSourceSet(project, extension as NativeKtAndroidInterface, androidExtension, null, idl, module, sourceSet, srcGenDir, nativesBuildDir, false)
                }
            }
        }
        tmpCommonTasks.get()?.clear()
    }
}

private fun NativeKtPlugin.configureSinglePlatform(
    idl: IdlResolver,
    nativesBuildDir: File,
    srcGenDir: File,
    module: SinglePlatform
){
    val extension = extension as NativeKtCommonInterface
    val sourceSet = kotlin.findSourceSet(module.targetSourceSet)

    // Apply runtime
    if(extension.applyRuntime) {
        sourceSet.dependencies {
            implementation(RUNTIME_DEPENDENCY)
        }
    }

    configureKotlinSourceSet(kotlin, null, idl, nativesBuildDir, srcGenDir, module, sourceSet, false)
}

private fun NativeKtPlugin.configureMultiplatform(
    idl: IdlResolver,
    nativesBuildDir: File,
    srcGenDir: File,
    module: Multiplatform
){
    val extension = extension as NativeKtCommonInterface
    val kotlin = project.the<KotlinMultiplatformExtension>()

    val commonSourceSet = kotlin.sourceSets.findByName(module.commonSourceSet)
        ?: throw Exception("Source set '${module.commonSourceSet}' was not found")
    val targetSourceSets = module.getActiveSourceSets(kotlin)
    val stubSourceSets = module.getActiveStubs(kotlin)

    val commonTask = configureCommon(
        project = project,
        extension = extension,
        idl = idl,
        module = module,
        sourceSet = commonSourceSet,
        srcRootDir = srcGenDir
    )
    tmpCommonTasks.getOrSet { hashMapOf() }[module] = commonTask

    // Apply runtime
    if(extension.applyRuntime) {
        commonSourceSet.dependencies {
            implementation(RUNTIME_DEPENDENCY)
        }
    }

    targetSourceSets.forEach {
        configureKotlinSourceSet(kotlin, commonTask, idl, nativesBuildDir, srcGenDir, module, it, true)
    }

    stubSourceSets.forEach {
        configureStub(project, commonTask, extension, idl, module, it, srcGenDir)
    }
}

private fun NativeKtPlugin.configureKotlinSourceSet(
    kotlin: KotlinProjectExtension,
    commonTask: TaskProvider<*>?,
    idl: IdlResolver,
    nativesBuildDir: File,
    srcGenDir: File,
    module: NativeProject,
    sourceSet: KotlinSourceSet,
    expectActual: Boolean
) = when(val targetType = getTargetType(kotlin, sourceSet)) {
    TargetType.JVM -> configureJvm(project, extension as NativeKtJvmInterface, commonTask, idl, module, sourceSet, srcGenDir, nativesBuildDir, expectActual)
    TargetType.JS -> configureJs(project, extension as NativeKtJsInterface, commonTask, idl, module, sourceSet, srcGenDir, nativesBuildDir, expectActual, false)
    TargetType.WASM_JS -> configureJs(project, extension as NativeKtJsInterface, commonTask, idl, module, sourceSet, srcGenDir, nativesBuildDir, expectActual, true)
    TargetType.ANDROID -> { }
    else -> configureNative(project, extension as NativeKtNativeInterface, commonTask, idl, module, sourceSet, targetType, srcGenDir, nativesBuildDir, expectActual)
}

private fun Multiplatform.getActiveSourceSets(kotlin: KotlinProjectExtension): List<KotlinSourceSet> {
    return targetSourceSets
        .mapNotNull { kotlin.sourceSets.findByName(it) }
}

private fun Multiplatform.getActiveStubs(kotlin: KotlinProjectExtension): List<KotlinSourceSet> {
    return stubSourceSets
        .mapNotNull { kotlin.sourceSets.findByName(it) }
}

private fun getTargetType(
    kotlin: KotlinProjectExtension,
    sourceSet: KotlinSourceSet
): TargetType {
    when (kotlin) {
        is KotlinSingleJavaTargetExtension -> return TargetType.JVM
        is KotlinJsProjectExtension -> return TargetType.JS
        is KotlinAndroidProjectExtension -> return TargetType.ANDROID
    }
    kotlin as KotlinTargetsContainer

    val target = kotlin.targets.first { target ->
        target.compilations.forEach { compilation ->
            if(compilation.allKotlinSourceSets.any { it == sourceSet })
                return@first true
        }
        false
    }
    return when(target.platformType) {
        KotlinPlatformType.common -> throw UnsupportedOperationException()
        KotlinPlatformType.jvm -> TargetType.JVM
        KotlinPlatformType.js -> TargetType.JS
        KotlinPlatformType.wasm -> TargetType.WASM_JS
        KotlinPlatformType.androidJvm -> TargetType.ANDROID
        KotlinPlatformType.native -> when((target as KotlinNativeTarget).konanTarget) {
            KonanTarget.MINGW_X64 -> TargetType.MINGW_X64

            KonanTarget.MACOS_ARM64 -> TargetType.MACOS_ARM64
            KonanTarget.MACOS_X64 -> TargetType.MACOS_X64

            KonanTarget.LINUX_X64 -> TargetType.LINUX_X64
            KonanTarget.LINUX_ARM64 -> TargetType.LINUX_ARM64
            KonanTarget.LINUX_ARM32_HFP -> throw UnsupportedOperationException("LINUX_ARM32_HFP is unsupported")

            KonanTarget.IOS_X64 -> TargetType.IOS_X64
            KonanTarget.IOS_ARM64 -> TargetType.IOS_ARM64
            KonanTarget.IOS_SIMULATOR_ARM64 -> TargetType.IOS_SIMULATOR_ARM64

            KonanTarget.WATCHOS_X64 -> TargetType.WATCHOS_X64
            KonanTarget.WATCHOS_ARM64 -> TargetType.WATCHOS_ARM64
            KonanTarget.WATCHOS_ARM32 -> TargetType.WATCHOS_ARM32
            KonanTarget.WATCHOS_DEVICE_ARM64 -> TargetType.WATCHOS_DEVICE_ARM64
            KonanTarget.WATCHOS_SIMULATOR_ARM64 -> TargetType.WATCHOS_SIMULATOR_ARM64

            KonanTarget.TVOS_X64 -> TargetType.TVOS_X64
            KonanTarget.TVOS_ARM64 -> TargetType.TVOS_ARM64
            KonanTarget.TVOS_SIMULATOR_ARM64 -> TargetType.TVOS_SIMULATOR_ARM64

            KonanTarget.ANDROID_X64 -> TargetType.ANDROID_NATIVE_X64
            KonanTarget.ANDROID_X86 -> TargetType.ANDROID_NATIVE_X86
            KonanTarget.ANDROID_ARM32 -> TargetType.ANDROID_NATIVE_ARM32
            KonanTarget.ANDROID_ARM64 -> TargetType.ANDROID_NATIVE_ARM64
        }
    }
}

private fun KotlinProjectExtension.findSourceSet(name: String): KotlinSourceSet {
    return sourceSets.findByName(name)
        ?: throw Exception("Source set '$name:' was not found")
}
