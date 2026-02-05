package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.plugin.NativeKtExtension
import com.huskerdev.nativekt.plugin.NativeModule
import com.huskerdev.nativekt.utils.dir
import com.huskerdev.nativekt.printers.KotlinJsPrinter
import com.huskerdev.nativekt.utils.cmakeBuild
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.webidl.resolver.IdlResolver
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

internal fun configureJs(
    project: Project,
    extension: NativeKtExtension,
    idl: IdlResolver,
    module: NativeModule,
    sourceSet: KotlinSourceSet,
    srcGenDir: File,
    cmakeRootDir: File,
    expectActual: Boolean
) {
    val jsGenDir = File(srcGenDir, "js/src")
    jsGenDir.mkdirs()

    val classPathFile = File(jsGenDir, module.classPath.replace(".", "/"))

    val rcsGenDir = File(srcGenDir, "js/resources")
    rcsGenDir.mkdirs()

    val cmakeDir = File(cmakeRootDir, "emscripten")
    cmakeDir.mkdirs()

    val cmakeBuildDir = File(cmakeDir, "out")
    cmakeBuildDir.mkdirs()

    val commonCmakeBuildDir = File(cmakeRootDir, "common")
    commonCmakeBuildDir.mkdirs()

    val exportedFunctions = idl.globalOperators().joinToString(",") { "_${it.name}" }

    sourceSet.kotlin.srcDir(jsGenDir)
    sourceSet.resources.srcDir(rcsGenDir)

    // Create CMakeLists.txt with emscripten linker flags
    File(cmakeDir, "CMakeLists.txt").writeText($$"""
        cmake_minimum_required(VERSION 3.15)

        project("$${module.name}")

        add_subdirectory("$${
            module.dir(project).absolutePath.replace("\\", "/")
        }" "$${
            commonCmakeBuildDir.absolutePath.replace("\\", "/")
        }")

        add_executable(lib$${module.name} $<TARGET_OBJECTS:$${module.name}>)
        
        set_target_properties(libtest PROPERTIES LINK_FLAGS "-s --no-entry -s MODULARIZE=1 -s EXPORT_ES6=1 -s EXPORTED_FUNCTIONS=$$exportedFunctions")
    """.trimIndent())

    // Create Kotlin/JS bindings
    KotlinJsPrinter(
        idl = idl,
        target = File(classPathFile, "${module.name}.kt"),
        classPath = module.classPath,
        moduleName = module.name,
        useCoroutines = extension.useCoroutines,
        expectActual = expectActual
    )

    // Compilation task
    val task = project.tasks.register("compileNatives${module.name.capitalized()}Js") {
        group = "native"
        doLast {
            val toolchain = File(System.getenv()["EMSDK"],
                "upstream/emscripten/cmake/Modules/Platform/Emscripten.cmake")

            cmakeBuild(
                project, cmakeDir, cmakeBuildDir,
                module.buildType,
                args = setOf(
                    "-DCMAKE_TOOLCHAIN_FILE=\"$toolchain\""
                )
            )

            // Copy .js file
            cmakeBuildDir.listFiles()!!.first {
                it.name == "lib${module.name}.js"
            }.copyTo(File(rcsGenDir, "lib${module.name}.js"), overwrite = true)

            // Cope .wasm file
            cmakeBuildDir.listFiles()!!.first {
                it.name == "lib${module.name}.wasm"
            }.copyTo(File(rcsGenDir, "lib${module.name}.wasm"), overwrite = true)
        }
    }

    project.tasks.matching { it.name == "compileKotlinJs" }.forEach {
        it.dependsOn(task)
    }
}