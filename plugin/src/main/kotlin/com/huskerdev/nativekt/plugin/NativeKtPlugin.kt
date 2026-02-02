package com.huskerdev.nativekt.plugin

import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.huskerdev.nativekt.configurators.configureAndroid
import com.huskerdev.nativekt.configurators.configureCommon
import com.huskerdev.nativekt.configurators.configureJs
import com.huskerdev.nativekt.configurators.configureJvm
import com.huskerdev.nativekt.configurators.configureNative
import com.huskerdev.webidl.resolver.IdlResolver
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

@Suppress("unused")
class NativeKtPlugin: Plugin<Project> {
    lateinit var project: Project
    lateinit var extension: NativeKtExtension

    override fun apply(project: Project) {
        this.project = project
        extension = project.extensions.create("native", NativeKtExtension::class.java)

        val buildDir = project.layout.buildDirectory.get().asFile
        val cmakeDir = File(buildDir, "cmake")
        val srcGenDir = File(buildDir, "generated/natives")
        srcGenDir.deleteRecursively()

        project.plugins.withId("com.android.kotlin.multiplatform.library") {
            initAndroid(cmakeDir, srcGenDir)
        }

        project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            configure(cmakeDir, srcGenDir)
        }
    }

    private fun configure(
        cmakeDir: File,
        srcGenDir: File
    ){
        val localNativeJvmRun = project.configurations.create("localNativeJvmRun") {
            isCanBeConsumed = false
            isCanBeResolved = true
        }

        project.afterEvaluate {
            project.configurations["jvmRuntimeClasspath"].extendsFrom(localNativeJvmRun)
            project.configurations["jvmTestRuntimeClasspath"].extendsFrom(localNativeJvmRun)

            extension.forEach { module ->
                val idl = module.idl(project)
                val cmakeModuleDir = File(cmakeDir, module.name)
                val srcGenModuleDir = File(srcGenDir, module.name)

                when(module) {
                    is Multiplatform -> configureMultiplatform(idl, cmakeModuleDir, srcGenModuleDir, module)
                    is SinglePlatform -> {
                        // TODO
                    }
                }
            }
        }
    }

    private fun initAndroid(
        cmakeDir: File,
        srcGenDir: File
    ) {
        val kotlin = project.the<KotlinMultiplatformExtension>()
        val androidComponents = project.the<KotlinMultiplatformAndroidComponentsExtension>()

        androidComponents.finalizeDsl { androidExtension ->
            extension.forEach { module ->
                val idl = module.idl(project)
                val cmakeModuleDir = File(cmakeDir, module.name)
                val srcGenModuleDir = File(srcGenDir, module.name)

                when(module) {
                    is Multiplatform -> {
                        module.getActiveSourceSets(kotlin).forEach {
                            if(it.value == TargetType.ANDROID)
                                configureAndroid(project, extension, androidExtension, idl, module, it.key, srcGenModuleDir, cmakeModuleDir)
                        }
                    }
                    is SinglePlatform -> {
                        // TODO
                    }
                }
            }
        }
    }

    private fun configureMultiplatform(
        idl: IdlResolver,
        cmakeDir: File,
        srcGenDir: File,
        module: Multiplatform
    ){
        val kotlin = project.the<KotlinMultiplatformExtension>()

        val targetSourceSets = module.getActiveSourceSets(kotlin)
        val stubSourceSets = module.getActiveStubs(kotlin)

        configureCommon(
            project = project,
            configuration = extension,
            idl = idl,
            module = module,
            sourceSet = kotlin.sourceSets.findByName(module.commonSourceSet)
                ?: throw Exception("Source set '${module.commonSourceSet}' was not found"),
            srcGenDir = srcGenDir
        )

        targetSourceSets.forEach {
            when(it.value) {
                TargetType.JVM -> configureJvm(project, extension, idl, module, it.key, srcGenDir, cmakeDir)
                TargetType.JS -> configureJs(project, extension, idl, module, it.key, srcGenDir, cmakeDir)
                TargetType.WASM -> { }
                else -> configureNative(project, kotlin, extension, idl, module, it, srcGenDir, cmakeDir)
            }
        }
    }

    private fun Multiplatform.getActiveSourceSets(kotlin: KotlinMultiplatformExtension): Map<KotlinSourceSet, TargetType> {
        return targetSourceSets
            .mapKeys { kotlin.sourceSets.findByName(it.key) }
            .filterKeys { it != null }
            .map { it.key!! to it.value }
            .toMap()
    }

    private fun Multiplatform.getActiveStubs(kotlin: KotlinMultiplatformExtension): List<KotlinSourceSet> {
        return stubSourceSets
            .mapNotNull { kotlin.sourceSets.findByName(it) }
    }
}