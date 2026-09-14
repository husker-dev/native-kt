package com.huskerdev.nativekt.plugin

import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.configurators.*
import com.huskerdev.nativekt.createContext
import com.huskerdev.nativekt.plugin.tasks.ApiGenTask
import com.huskerdev.nativekt.plugin.tasks.InitTask
import com.huskerdev.nativekt.utils.dependsOnProjectReload
import com.huskerdev.nativekt.utils.upperCamelCase
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleJavaTargetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetsContainer
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
    extension: ExtensiblePolymorphicDomainObjectContainer<*>
){
    project.afterEvaluate {
        extension.forEach { module ->
            val context = createContextWithProject(project, extension, module as NativeProject, false)
                ?: return@forEach

            val apiGenTask = project.tasks.register(
                "nativeApi${context.moduleName.upperCamelCase()}",
                ApiGenTask::class.java
            )
            apiGenTask.get().let {
                it.context = context
                it.dependsOnProjectReload()
            }

            when(module) {
                is Multiplatform -> configureMultiplatform(project, context, apiGenTask)
                is SinglePlatform -> configureSinglePlatform(project, context, apiGenTask)
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
    extensionProvider: () -> ExtensiblePolymorphicDomainObjectContainer<*>?
) {
    val kotlin = project.the<KotlinProjectExtension>()
    val androidComponents = project.the<KotlinMultiplatformAndroidComponentsExtension>()

    androidComponents.finalizeDsl { androidExtension ->
        val extension = extensionProvider()

        if(extension == null) {
            project.logger.error("[native-kt] Could not setup Android: Kotlin plugin is not applied to the current project.")
            return@finalizeDsl
        }

        extension.forEach { module ->
            module as NativeProject

            // If module is not valid, then skip.
            // INFO:
            //     Android can not exist without at least one Kotlin target (Multiplatform, JVM, etc.)
            //     So we mute error message here, because it was already printed from 'configureKotlin'.
            val context = createContextWithProject(project, extension, module, true)
                ?: return@forEach

            // Configure only Android source sets
            when(module) {
                is Multiplatform -> {
                    module.getActiveSourceSets(kotlin).forEach {
                        if(getTargetType(project, it) == TargetType.ANDROID)
                            configureAndroidSourceSet(project, context, androidExtension, it, true)
                    }
                }
                is SinglePlatform -> {
                    val sourceSet = kotlin.findSourceSet(module.targetSourceSet)

                    if(getTargetType(project, sourceSet) == TargetType.ANDROID)
                        configureAndroidSourceSet(project, context, androidExtension, sourceSet, false)
                }
            }
        }
    }
}

private fun createContextWithProject(
    project: Project,
    extension: ExtensiblePolymorphicDomainObjectContainer<*>,
    module: NativeProject,
    muteError: Boolean
): NativeModuleContext? {
    val buildDir = project.layout.buildDirectory.asFile.get()

    val context = createContext(
        buildDir = buildDir,
        extension = extension as NativeKtCommonInterface,
        module = module
    )

    val initTaskName = "init${module.name.upperCamelCase()}"
    val initTask = project.tasks.findByName(initTaskName)
        ?: project.tasks.register(initTaskName, InitTask::class.java).get().also {
            it.extension = extension
            it.module = module
            it.buildDir = buildDir.absolutePath
        }
    project.gradle.taskGraph.whenReady {
        if (context == null && !muteError && !hasTask(initTask)) {
            project.logger.error("""
                Native module '${module.name}' is not loaded:
                  'api.ndl' file not found.
                
                To initialize module: 
                  ./gradlew ${initTask.path}
            """.trimIndent())
        }
    }
    return context
}

private fun configureSinglePlatform(
    project: Project,
    context: NativeModuleContext,
    apiGenTask: TaskProvider<ApiGenTask>
){
    val kotlin = project.the<KotlinProjectExtension>()
    val sourceSet = kotlin.findSourceSet((context.module as SinglePlatform).targetSourceSet)

    // Apply runtime
    if(context.extension.applyRuntime) {
        sourceSet.dependencies {
            implementation(RUNTIME_DEPENDENCY)
        }
    }

    configureKotlinSourceSet(project, apiGenTask, context, sourceSet, false)
}

private fun configureMultiplatform(
    project: Project,
    context: NativeModuleContext,
    apiGenTask: TaskProvider<ApiGenTask>
){
    val module = context.module as Multiplatform
    val kotlin = project.the<KotlinProjectExtension>()

    val commonSourceSet = kotlin.findSourceSet(module.commonSourceSet)
    val targetSourceSets = module.getActiveSourceSets(kotlin)
    val stubSourceSets = module.getActiveStubs(kotlin)

    val commonTask = configureCommon(
        project = project,
        context = context,
        sourceSet = commonSourceSet
    )
    commonTask.get().dependsOn(apiGenTask)

    // Apply runtime to common source set
    if(context.extension.applyRuntime) {
        commonSourceSet.dependencies {
            implementation(RUNTIME_DEPENDENCY)
        }
    }

    // Add '-Xexpect-actual-classes' to all targets
    (targetSourceSets + stubSourceSets + commonSourceSet)
        .map { getKotlinTarget(project, it) }
        .filterIsInstance<HasConfigurableKotlinCompilerOptions<*>>()
        .toSet()
        .forEach { target ->
            if("-Xexpect-actual-classes" !in target.compilerOptions.freeCompilerArgs.get())
                target.compilerOptions.freeCompilerArgs.addAll("-Xexpect-actual-classes")
        }

    // Configure targets (not stubs)
    targetSourceSets.forEach { sourceSet ->
        configureKotlinSourceSet(project, commonTask, context, sourceSet, true)
    }

    // Configure stubs
    stubSourceSets.forEach {
        configureStub(project, commonTask, context, it)
    }
}

private fun configureKotlinSourceSet(
    project: Project,
    commonTask: TaskProvider<*>?,
    context: NativeModuleContext,
    sourceSet: KotlinSourceSet,
    expectActual: Boolean
) = when(val targetType = getTargetType(project, sourceSet)) {
    TargetType.JVM -> configureJvm(project, commonTask, context, sourceSet, expectActual)
    TargetType.JS -> configureJs(project, commonTask, context, sourceSet, expectActual, false)
    TargetType.WASM_JS -> configureJs(project, commonTask, context, sourceSet, expectActual, true)
    TargetType.ANDROID -> { }
    else -> configureNative(project, commonTask, context, sourceSet, targetType, expectActual)
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

private fun KotlinProjectExtension.findSourceSet(name: String): KotlinSourceSet {
    return sourceSets.findByName(name)
        ?: throw Exception("Could not find source set: '$name:'")
}
