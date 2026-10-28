package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.printers.cpp.*
import com.huskerdev.nativekt.printers.kotlin.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.stdlibx.fs.SyncPath
import com.huskerdev.stdlibx.systemLineSeparator

fun prepareJs(
    context: NativeModuleContext,
    useExpectActual: Boolean,
    targetType: TargetType
) {
    val layout = DirectoryLayout.of(context, targetType)

    // Create Kotlin/JS bindings
    KotlinJsPrinter(
        context = context,
        target = layout.kotlinSourceFile,
        expectActual = useExpectActual,
        isWasm = targetType == TargetType.WASM_JS
    )

    when(val buildSystem = context.buildSystem) {
        is BuildSystem.CMake -> {
            val projectPath = buildSystem.projectDir.invariantSeparatorsPath
            val projectBuildPath = layout.nativeBuildDir.resolve("sub").invariantSeparatorsPath

            val languages = buildList {
                add("C")
                if(context.language == Language.CPP)
                    add("CXX")
            }.joinToString(" ")

            layout.nativeSourcesDir.resolve("dummy.${context.language.sourceExtension}").writeString("")
            layout.nativeSourcesDir.resolve("CMakeLists.txt").writeString($$"""
                cmake_minimum_required(VERSION 3.24)
        
                project("$${context.moduleName}" LANGUAGES $$languages)
                
                set(EXTRA_LINK_FLAGS "" CACHE STRING "Extra linker flags")
                
                # Some compile options
                add_compile_options(-Wno-initializer-overrides)
                
                # Add project
                add_subdirectory("$$projectPath" "$$projectBuildPath")
            
                # Add sources + child project
                add_executable(lib$${context.moduleName} dummy.$${context.language.sourceExtension})
                target_link_libraries(lib$${context.moduleName} PRIVATE $${buildSystem.libraryName})
                
                # Add Emscripten linker flags
                set_target_properties(lib$${context.moduleName} PROPERTIES LINK_FLAGS "$${getEmccArgs(context)}")
            """.trimIndent())
        }
        is BuildSystem.Cargo -> Unit // Do nothing
    }
}

fun compileJs(
    context: NativeModuleContext,
    targetType: TargetType
) {
    val layout = DirectoryLayout.of(context, targetType)

    fun packResult(jsFile: SyncPath, wasmFile: SyncPath) {
        jsFile.copyTo(layout.kotlinResourcesDir!!.resolve("lib${context.moduleName}.mjs"), overwrite = true)
        wasmFile.copyTo(layout.kotlinResourcesDir.resolve("lib${context.moduleName}.wasm"), overwrite = true)
    }

    when(val buildSystem = context.buildSystem) {
        is BuildSystem.CMake -> {
            val emcc = locateEmcc(context)
            val toolchain = emcc.parent!!.resolve("cmake/Modules/Platform/Emscripten.cmake")

            cmakeGen(
                context,
                dir = layout.nativeSourcesDir,
                buildDir = layout.nativeBuildDir,
                buildType = buildSystem.buildType,
                args = LinkedHashSet(buildSystem.args).apply {
                    this += "-DCMAKE_TOOLCHAIN_FILE=\"$toolchain\""
                }
            )

            // Build
            cmakeBuild(context, layout.nativeBuildDir)
            packResult(
                layout.nativeBuildDir.resolve("lib${context.moduleName}.js"),
                layout.nativeBuildDir.resolve("lib${context.moduleName}.wasm")
            )
        }

        is BuildSystem.Cargo -> {
            val pkgDir = wasmBindgenBuild(
                context,
                project = buildSystem.projectDir,
                buildType = buildSystem.buildType,
                buildDir = layout.nativeBuildDir
            )

            val moduleName = context.moduleName
            val jsFile = pkgDir.resolve("$moduleName.js")
            val wasmFile = pkgDir.resolve("${moduleName}_bg.wasm")

            // Change `name_bg.wasm` references to `libname.wasm`
            jsFile.writeString(jsFile.readString()
                .replace(wasmFile.name, "lib$moduleName.wasm")
                .replace("/* @ts-self-types=\"./$moduleName.d.ts\" */", "")
            )

            packResult(jsFile, wasmFile)
        }
    }
}