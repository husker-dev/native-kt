package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.plugin.Configuration
import com.huskerdev.nativekt.plugin.cmakeGenerator
import com.huskerdev.nativekt.plugin.currentCompilationTargetName
import com.huskerdev.nativekt.plugin.dir
import com.huskerdev.nativekt.plugin.exec
import com.huskerdev.nativekt.plugin.idl
import com.huskerdev.nativekt.printers.DefPrinter
import com.huskerdev.nativekt.printers.HeaderPrinter
import com.huskerdev.nativekt.printers.KotlinNativePrinter
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.utils.`is`
import java.io.File

fun configureNative(
    project: Project,
    config: Configuration,
    kotlin: KotlinMultiplatformExtension,
    genDir: File,
    cmakeDir: File
){
    val nativeTargets = setOf(
        "mingwX64", "linuxX64", "macosX64"
    )

    val nativeGenDir = File(genDir, "native")
    nativeGenDir.mkdirs()

    val cinteropGenDir = File(genDir, "cinterop")
    cinteropGenDir.mkdirs()

    kotlin.sourceSets {
        nativeTargets.forEach {
            findByName("${it}Main")?.kotlin?.srcDir(nativeGenDir)
        }
    }

    config.forEach { module ->
        val idl = module.idl(project)
        val classPathFile = module.classPath.replace(".", "/")

        val cinteropDefFile = File(cinteropGenDir, "${module.name}.def")
        val cinteropHeaderFile = File(cinteropGenDir, "${module.name}.h")

        val cmakeBuildDir = File(cmakeDir, "${module.name}/kn")
        cmakeBuildDir.mkdirs()

        val commonCmakeBuildDir = File(cmakeDir, "${module.name}/common")
        commonCmakeBuildDir.mkdirs()

        File(cmakeBuildDir, "CMakeLists.txt").writeText($$"""
            cmake_minimum_required(VERSION 3.15)

            project("$${module.name}")

            add_subdirectory("$${module.dir(project).absolutePath.replace("\\", "/")}" "$${commonCmakeBuildDir.absolutePath.replace("\\", "/")}")

            add_library(lib_$${module.name} STATIC $<TARGET_OBJECTS:$${module.name}>)
        """.trimIndent())


        KotlinNativePrinter(
            idl = idl,
            target = File(nativeGenDir, "$classPathFile/${module.name}.kt"),
            classPath = module.classPath,
            moduleName = module.name
        )
        HeaderPrinter(
            idl = idl,
            target = cinteropHeaderFile,
            guardName = module.name.uppercase()
        )
        DefPrinter(
            target = cinteropDefFile,
            headerFile = cinteropHeaderFile,
            classPath = module.classPath
        )

        val targetName = currentCompilationTargetName()
        val target = kotlin.targets.findByName(targetName) as? KotlinNativeTarget
            ?: return@forEach

        val compilation = target.compilations.findByName("main")
            ?: return@forEach

        val cmakeOutDir = File(cmakeBuildDir, targetName)
        cmakeOutDir.mkdirs()

        target.binaries {
            all {
                linkerOpts(
                    "-L${cmakeOutDir.absolutePath.replace("\\", "/")}",
                    "-llib_${module.name}"
                )
            }
        }

        compilation.cinterops {
            create("natives_${module.name}").definitionFile.set(cinteropDefFile)
        }

        val task = project.tasks.register("compileNatives${module.name.capitalized()}Kn") {
            group = "native"
            doLast {
                project.exec("cmake \"${cmakeBuildDir}\" -B \"$cmakeOutDir\" -G \"${cmakeGenerator()}\"", cmakeOutDir)
                project.exec("cmake --build \"$cmakeOutDir\"", cmakeOutDir)
            }
        }

        project.tasks.matching { it.name == "compileKotlin${targetName.capitalized()}" }.forEach {
            it.dependsOn(task)
        }

    }
}