package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.plugin.Configuration
import com.huskerdev.nativekt.plugin.cmakeGenerator
import com.huskerdev.nativekt.plugin.currentCompilationTargetName
import com.huskerdev.nativekt.plugin.dir
import com.huskerdev.nativekt.plugin.exec
import com.huskerdev.nativekt.plugin.idl
import com.huskerdev.nativekt.printers.HeaderPrinter
import com.huskerdev.nativekt.printers.KotlinCommonPrinter
import com.huskerdev.nativekt.printers.KotlinJvmPrinter
import com.huskerdev.nativekt.printers.KotlinJvmUtilsPrinter
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import java.io.File

fun configureJvm(
    project: Project,
    config: Configuration,
    kotlin: KotlinMultiplatformExtension,
    genDir: File,
    cmakeDir: File
) {
    val jvmGenDir = File(genDir, "jvm/src")
    jvmGenDir.mkdirs()

    val rscGenDir = File(genDir, "jvm/resources")
    rscGenDir.mkdirs()

    val jdkPath = System.getProperty("java.home").replace("\\", "/")
    val jdkPlatformName = when {
        Os.isFamily(Os.FAMILY_WINDOWS) -> "win32"
        Os.isFamily(Os.FAMILY_MAC) -> "linux"
        Os.isFamily(Os.FAMILY_UNIX) -> "darwin"
        else -> throw UnsupportedOperationException()
    }

    kotlin.sourceSets {
        findByName("jvmMain")?.apply {
            this.kotlin.srcDir(jvmGenDir)
            this.resources.srcDir(rscGenDir)
        }
    }

    KotlinJvmUtilsPrinter(jvmGenDir)

    config.forEach { module ->
        val idl = module.idl(project)
        val classPathFile = module.classPath.replace(".", "/")

        val cmakeBuildDir = File(cmakeDir, "${module.name}/jvm")
        cmakeBuildDir.mkdirs()

        val commonCmakeBuildDir = File(cmakeDir, "${module.name}/common")
        commonCmakeBuildDir.mkdirs()

        val cmakeOutDir = File(cmakeBuildDir, currentCompilationTargetName())
        cmakeOutDir.mkdirs()

        File(cmakeBuildDir, "CMakeLists.txt").writeText($$"""
            cmake_minimum_required(VERSION 3.15)

            project("$${module.name}")

            add_subdirectory("$${module.dir(project).absolutePath.replace("\\", "/")}" "$${commonCmakeBuildDir.absolutePath.replace("\\", "/")}")

            add_library(lib_$${module.name} SHARED $<TARGET_OBJECTS:$${module.name}>)
            
            target_include_directories(lib_$${module.name} PRIVATE "$${jdkPath}/include")
            target_include_directories(lib_$${module.name} PRIVATE "$${jdkPath}/include/$$jdkPlatformName")
        """.trimIndent())

        KotlinJvmPrinter(
            idl = idl,
            target = File(jvmGenDir, "$classPathFile/${module.name}.kt"),
            classPath = module.classPath,
            moduleName = module.name
        )

        val task = project.tasks.register("compileNatives${module.name.capitalized()}Jvm") {
            group = "native"
            doLast {
                project.exec("cmake \"${cmakeBuildDir}\" -B \"$cmakeOutDir\" -G \"${cmakeGenerator()}\"", cmakeOutDir)
                project.exec("cmake --build \"$cmakeOutDir\"", cmakeOutDir)

                val result = cmakeOutDir.listFiles()!!.first {
                    it.nameWithoutExtension == "liblib_${module.name}" && it.extension in setOf("dll", "so", "dylib")
                }

                result.copyTo(File(rscGenDir, "${module.name}.${result.extension}"), overwrite = true)
            }
        }
        project.tasks.matching { it.name == "compileKotlinJvm" }.forEach {
            it.dependsOn(task)
        }
    }


}