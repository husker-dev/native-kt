package com.huskerdev.nativekt.gradle

import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.gradle.configurators.configureAndroidSourceSet
import com.huskerdev.nativekt.gradle.configurators.configureCommon
import com.huskerdev.nativekt.gradle.configurators.configureJs
import com.huskerdev.nativekt.gradle.configurators.configureJvm
import com.huskerdev.nativekt.gradle.configurators.configureNative
import com.huskerdev.nativekt.gradle.configurators.configureStub
import com.huskerdev.nativekt.gradle.tasks.ApiGenTask
import com.huskerdev.nativekt.utils.upperCamelCase
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget


/* ========================================
    Configuration call sequence:
    1. 'configureAndroid'
    2. 'configureKotlin'
    3. finalizeDsl in 'configureAndroid'
    4. afterEvaluate in 'configureKotlin'
=========================================== */

/**
 * Configures the Kotlin plugin, regardless of its type (JVM, Multiplatform, etc.)
 */
internal fun configureKotlin(
    project: Project,
    extension: NativeKtExtension,
    singleSourceSet: Boolean
){
    project.afterEvaluate {
        extension.serializedModules.forEach { serializedModule ->
            val kotlin = project.the<KotlinProjectExtension>()
            val module = Json.decodeFromString<NativeKtModule>(serializedModule)

            val context = createContextWithProject(project, module, extension)
                ?: return@forEach

            val commonSourceSet: KotlinSourceSet
            val targetSourceSets: List<KotlinSourceSet>
            val stubSourceSets: List<KotlinSourceSet>

            if(singleSourceSet && module.commonTarget == "jvm" && !module.expectActual) {
                commonSourceSet = kotlin.sourceSets.findByName("main")
                    ?: throw Exception("Could not find source set: '${name}Main'")
                targetSourceSets = listOf(commonSourceSet)
                stubSourceSets = emptyList()
            } else {
                commonSourceSet = kotlin.findSourceSet(module.commonTarget)
                targetSourceSets = kotlin.getActiveSourceSets(module)
                stubSourceSets = kotlin.getActiveStubs(module)
            }

            // Apply runtime to common source set
            if(extension.applyRuntime) {
                commonSourceSet.dependencies {
                    api(RUNTIME_DEPENDENCY)
                }
            }

            // Add '-Xexpect-actual-classes' to freeCompilerArgs
            if(kotlin is HasConfigurableKotlinCompilerOptions<*>) {
                val args = kotlin.compilerOptions.freeCompilerArgs
                if ("-Xexpect-actual-classes" !in args.get())
                    args.add("-Xexpect-actual-classes")
            }

            // API generation task
            val apiGenTask = project.tasks.register(
                "nativeApi${context.moduleName.upperCamelCase()}",
                ApiGenTask::class.java
            )
            apiGenTask.get().let {
                it.context = context.serialize()
                it.dependsOnProjectReload()
            }

            // Common generation task
            var commonTask: TaskProvider<*>? = null
            if(module.expectActual) {
                commonTask = configureCommon(
                    project = project,
                    context = context,
                    sourceSet = commonSourceSet
                )
                commonTask.get().dependsOn(apiGenTask)
            }

            // Configure targets
            targetSourceSets.forEach { sourceSet ->
                configureKotlinSourceSet(project, commonTask, context, extension, sourceSet, module.expectActual)
            }

            // Configure stubs
            stubSourceSets.forEach { sourceSet ->
                configureStub(project, commonTask, context, sourceSet, getTargetType(project, sourceSet))
            }
        }
    }
}

/**
 * Android is a separate plugin that also requires remote configuration.
 * It uses finalizeDsl instead of afterEvaluate.
 */
internal fun configureAndroid(
    project: Project,
    extensionProvider: () -> NativeKtExtension?
) {
    val kotlin = project.the<KotlinProjectExtension>()
    val androidComponents = project.the<KotlinMultiplatformAndroidComponentsExtension>()

    androidComponents.finalizeDsl { androidExtension ->
        val extension = extensionProvider()

        if(extension == null) {
            project.logger.error("[native-kt] Could not setup Android: Kotlin plugin is not applied to the current project.")
            return@finalizeDsl
        }

        extension.serializedModules.forEach { serializedModule ->
            val module = Json.decodeFromString<NativeKtModule>(serializedModule)

            // If module is not valid, then skip
            val context = createContextWithProject(project, module, extension)
                ?: return@forEach

            // Configure only Android source sets
            kotlin.getActiveSourceSets(module).forEach { sourceSet ->
                if(getTargetType(project, sourceSet) != TargetType.ANDROID)
                    return@forEach

                // Apply critical stub lib
                if(extension.applyAndroidCriticalStub && context.module.android.useCriticalNative) {
                    sourceSet.dependencies {
                        compileOnly(ANDROID_STUB_DEPENDENCY)
                    }
                }

                // Configure
                configureAndroidSourceSet(project, context, androidExtension, sourceSet, module.expectActual)
            }
        }
    }
}

private fun configureKotlinSourceSet(
    project: Project,
    commonTask: TaskProvider<*>?,
    context: NativeModuleContext,
    extension: NativeKtExtension,
    sourceSet: KotlinSourceSet,
    expectActual: Boolean
) = when(val targetType = getTargetType(project, sourceSet)) {
    TargetType.JVM -> configureJvm(project, commonTask, context, extension, sourceSet, expectActual)

    TargetType.JS, TargetType.WASM_JS ->
        configureJs(project, commonTask, context, sourceSet, expectActual, targetType)

    TargetType.ANDROID -> { }
    else -> configureNative(project, commonTask, context, sourceSet, targetType, expectActual)
}

private fun getTargetType(
    project: Project,
    sourceSet: KotlinSourceSet
): TargetType {
    val kotlin = project.the<KotlinProjectExtension>()
    when (kotlin) {
        is KotlinSingleJavaTargetExtension -> return TargetType.JVM
        is KotlinAndroidProjectExtension -> return TargetType.ANDROID
    }

    val target = getKotlinTarget(project, sourceSet)
        ?: throw UnsupportedOperationException("KotlinTarget not found for KotlinSourceSet '${sourceSet.name}'")

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

private fun getKotlinTarget(project: Project, sourceSet: KotlinSourceSet): KotlinTarget? {
    val kotlin = project.the<KotlinProjectExtension>() as KotlinTargetsContainer

    return kotlin.targets.firstOrNull { target ->
        target.compilations.any { compilation ->
            compilation.allKotlinSourceSets.any {
                it == sourceSet
            }
        }
    }
}

private fun KotlinProjectExtension.getActiveSourceSets(module: NativeKtModule): List<KotlinSourceSet> =
    module.targets.mapNotNull { sourceSets.findByName("${it}Main") }

private fun KotlinProjectExtension.getActiveStubs(module: NativeKtModule): List<KotlinSourceSet> =
    module.stubs.mapNotNull { sourceSets.findByName("${it}Main") }

private fun KotlinProjectExtension.findSourceSet(name: String): KotlinSourceSet =
    sourceSets.findByName("${name}Main")
        ?: throw Exception("Could not find source set: '${name}Main'")
