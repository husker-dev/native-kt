package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.plugin.NativeKtExtension
import com.huskerdev.nativekt.plugin.NativeModule
import com.huskerdev.nativekt.printers.HeaderPrinter
import com.huskerdev.nativekt.printers.js.CEmscriptenPrinter
import com.huskerdev.nativekt.utils.dir
import com.huskerdev.nativekt.printers.js.KotlinJsPrinter
import com.huskerdev.nativekt.utils.cmakeBuild
import com.huskerdev.nativekt.utils.cmakeGen
import com.huskerdev.nativekt.utils.fresh
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.isLong
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
    if(System.getenv()["EMSDK"] == null)
        throw UnsupportedOperationException("Environment variable 'EMSDK' is not specified")

    if(idl.isUsingLong() && !extension.useJsBigInt) {
        throw UnsupportedOperationException("""
            A Long type was detected in your .ndl file, but it is not enabled by the current JS configuration.

            To fix this issue:
            
            1. Make sure your Kotlin Multiplatform version is >= 2.2.20
            2. Set 'useJsBigInt = true' in the plugin configuration.
            3. Add the following compiler options to the Kotlin Multiplatform JS target:
            
            kotlin {
                js {
                    compilerOptions {
                        freeCompilerArgs.addAll(
                            "-Xes-long-as-bigint", 
                            "-XXLanguage:+JsAllowLongInExportedDeclarations"
                        )
                    }
                }
            }
        """.trimIndent())
    }

    val jsGenDir = File(srcGenDir, "js/src")
    jsGenDir.fresh()

    val classPathFile = File(jsGenDir, module.classPath.replace(".", "/"))

    val rcsGenDir = File(srcGenDir, "js/resources")
    rcsGenDir.fresh()

    val cmakeDir = File(cmakeRootDir, "emscripten")
    cmakeDir.mkdirs()

    val cmakeBuildDir = File(cmakeDir, "out")
    cmakeBuildDir.mkdirs()

    val commonCmakeBuildDir = File(cmakeRootDir, "common")
    commonCmakeBuildDir.mkdirs()

    val exportedFunctions = listOf("_free", "_malloc")
    val exportedStr = exportedFunctions.joinToString(",")

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

        add_executable(lib$${module.name} $<TARGET_OBJECTS:$${module.name}> emscripten_bindings.cpp)
        set_target_properties(lib$${module.name} PROPERTIES CXX_STANDARD 17)
        
        set_target_properties(lib$${module.name} PROPERTIES LINK_FLAGS "-s -lembind -s --no-entry -s ALLOW_MEMORY_GROWTH=1 -s ALLOW_TABLE_GROWTH=1 -s MODULARIZE=1 -s EXPORT_ES6=1 -s WASM_BIGINT=$${if(extension.useJsBigInt) "1" else "0"} -s EXPORTED_RUNTIME_METHODS=UTF8ToString,stringToUTF8,lengthBytesUTF8,HEAP32,HEAP8,addFunction -s EXPORTED_FUNCTIONS=$$exportedStr")
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
    CEmscriptenPrinter(
       idl = idl,
       target = File(cmakeDir, "emscripten_bindings.cpp")
    )

    HeaderPrinter(
        idl = idl,
        target = File(cmakeDir, "api.h")
    )

    // Compilation task
    val task = project.tasks.register("compileNatives${module.name.capitalized()}Js") {
        group = "native"
        doLast {
            // Generate CMake build
            val toolchain = File(System.getenv()["EMSDK"],
                "upstream/emscripten/cmake/Modules/Platform/Emscripten.cmake")
            cmakeGen(
                project, cmakeDir, cmakeBuildDir,
                module.buildType,
                args = setOf(
                    "-DCMAKE_TOOLCHAIN_FILE=\"$toolchain\""
                )
            )

            // Build
            cmakeBuild(project, cmakeBuildDir)

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

internal fun IdlResolver.isUsingLong(): Boolean {
    // operators
    if(globalOperators().any { op ->
        op.type.isLong() || op.args.any { it.type.isLong() }
    }) return true

    // callbacks
    if(callbacks.values.any { cb ->
        cb.type.isLong() || cb.args.any { it.type.isLong() }
    }) return true

    return false
}