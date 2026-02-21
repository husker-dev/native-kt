package com.huskerdev.nativekt.plugin

import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.huskerdev.nativekt.configurators.*
import com.huskerdev.nativekt.printers.HeaderPrinter
import com.huskerdev.nativekt.utils.dir
import com.huskerdev.nativekt.utils.idl
import com.huskerdev.nativekt.utils.idlFile
import com.huskerdev.webidl.resolver.IdlResolver
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File


fun NativeKtPlugin.configure(
    cmakeDir: File,
    srcGenDir: File
){
    project.afterEvaluate {
        extension.forEach { module ->

            val initTask = project.tasks.register("cmakeInit${module.name.capitalized()}") {
                group = "native"
                doLast {
                    val dir = module.dir(project)
                    dir.mkdirs()
                    if(dir.list()!!.isNotEmpty()) {
                        project.logger.error("Can not init module: directory '${dir}' is not empty.")
                        return@doLast
                    }
                    File(dir, "src").mkdirs()
                    File(dir, "include").mkdirs()

                    File(dir, "CMakeLists.txt").writeText($$"""
                            cmake_minimum_required(VERSION 3.15)

                            project("$${module.name}")
                            
                            add_library(${PROJECT_NAME} STATIC src/$${module.name}.c)
                            
                            target_include_directories(${PROJECT_NAME} PRIVATE include)
                        """.trimIndent())
                    File(dir, "api.idl").writeText("""
                            
                            namespace global {
                                void helloWorld();
                            };
                        """.trimIndent())
                    File(dir, "src/${module.name}.c").writeText("""
                            #include <api.h>
                            #include <stdio.h>
                            
                            void helloWorld() {
                                printf("Hello, World!\n");
                                fflush(stdout);
                            }
                        """.trimIndent())
                    HeaderPrinter(
                        idl = module.idl(project),
                        target = File(module.dir(project), "include/api.h"),
                        guardName = module.name.uppercase()
                    )
                }
            }

            if(!module.idlFile(project).exists()) {
                project.logger.error("""
                        Native module '${module.name}' is not loaded:
                          'api.idl' file not found.
                        
                        Possible solution: 
                          run './gradlew :${initTask.name}'
                    """.trimIndent())
                return@forEach
            }

            val idl = module.idl(project)
            val cmakeModuleDir = File(cmakeDir, module.name)
            val srcGenModuleDir = File(srcGenDir, module.name)

            when(module) {
                is Multiplatform -> configureMultiplatform(idl, cmakeModuleDir, srcGenModuleDir, module)
                is SinglePlatform -> configureSinglePlatform(idl, cmakeModuleDir, srcGenModuleDir, module)
            }

            HeaderPrinter(
                idl = idl,
                target = File(module.dir(project), "include/api.h"),
                guardName = module.name.uppercase()
            )
        }
    }
}

fun NativeKtPlugin.initAndroid(
    cmakeDir: File,
    srcGenDir: File
) {
    val kotlin = project.the<KotlinMultiplatformExtension>()
    val androidComponents = project.the<KotlinMultiplatformAndroidComponentsExtension>()

    androidComponents.finalizeDsl { androidExtension ->
        extension.forEach { module ->
            if(!module.idlFile(project).exists())
                return@forEach

            val idl = module.idl(project)
            val cmakeModuleDir = File(cmakeDir, module.name)
            val srcGenModuleDir = File(srcGenDir, module.name)

            when(module) {
                is Multiplatform -> {
                    module.getActiveSourceSets(kotlin).forEach {
                        if(it.value == TargetType.ANDROID)
                            configureAndroid(project, extension, androidExtension, idl, module, it.key, srcGenModuleDir, cmakeModuleDir, true)
                    }
                }
                is SinglePlatform -> {
                    if(module.targetSourceSet.second == TargetType.ANDROID) {
                        val sourceSet = kotlin.sourceSets.findByName(module.targetSourceSet.first)
                            ?: throw Exception("Source set '${module.targetSourceSet.first}' was not found")

                        configureAndroid(project, extension, androidExtension, idl, module, sourceSet, srcGenModuleDir, cmakeModuleDir, false)
                    }
                }
            }
        }
    }
}

private fun NativeKtPlugin.configureSinglePlatform(
    idl: IdlResolver,
    cmakeRootDir: File,
    srcGenDir: File,
    module: SinglePlatform
){
    val kotlin = project.the<KotlinMultiplatformExtension>()

    val sourceSet = kotlin.sourceSets.findByName(module.targetSourceSet.first)
        ?: throw Exception("Source set '${module.targetSourceSet.first}' was not found")

    configureSourceSet(idl, cmakeRootDir, srcGenDir, module, sourceSet to module.targetSourceSet.second, false)
}

private fun NativeKtPlugin.configureMultiplatform(
    idl: IdlResolver,
    cmakeRootDir: File,
    srcGenDir: File,
    module: Multiplatform
){
    val kotlin = project.the<KotlinMultiplatformExtension>()

    val targetSourceSets = module.getActiveSourceSets(kotlin)
    val stubSourceSets = module.getActiveStubs(kotlin)

    configureCommon(
        configuration = extension,
        idl = idl,
        module = module,
        sourceSet = kotlin.sourceSets.findByName(module.commonSourceSet)
            ?: throw Exception("Source set '${module.commonSourceSet}' was not found"),
        srcGenDir = srcGenDir
    )

    targetSourceSets.forEach {
        configureSourceSet(idl, cmakeRootDir, srcGenDir, module, it.toPair(), true)
    }

    stubSourceSets.forEach {
        configureStub(extension, idl, module, it, srcGenDir)
    }
}

private fun NativeKtPlugin.configureSourceSet(
    idl: IdlResolver,
    cmakeRootDir: File,
    srcGenDir: File,
    module: NativeModule,
    sourceSet: Pair<KotlinSourceSet, TargetType>,
    expectActual: Boolean
) = when(sourceSet.second) {
    TargetType.JVM -> configureJvm(project, extension, idl, module, sourceSet.first, srcGenDir, cmakeRootDir, expectActual)
    TargetType.JS -> configureJs(project, extension, idl, module, sourceSet.first, srcGenDir, cmakeRootDir, expectActual)
    TargetType.WASM -> { }
    else -> configureNative(project, extension, idl, module, sourceSet, srcGenDir, cmakeRootDir, expectActual)
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