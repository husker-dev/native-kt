package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.printers.cpp.*
import com.huskerdev.nativekt.printers.kotlin.*
import com.huskerdev.nativekt.utils.*
import io.github.vinceglb.filekit.*
import kotlinx.io.SystemLineSeparator

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

    if(context.language == Language.C || context.language == Language.CPP) {
        // Generate Emscripten wrapper + C header
        CEmscriptenPrinter(
            context = context,
            target = layout.nativeSourcesDir.resolve("emscripten_bindings.c")
        )
        CApiHeaderPrinter(
            context = context,
            target = layout.nativeSourcesDir.resolve("api.h"),
            isInternal = true
        )
        if(context.language == Language.C) {
            // Generate C sources (header is already generated)
            CApiImplPrinter(
                context = context,
                target = layout.nativeSourcesDir.resolve("api.c")
            )
        }
        if(context.language == Language.CPP) {
            // generate C++ sources and header
            CppApiHeaderPrinter(
                context = context,
                target = layout.nativeSourcesDir.resolve("api.hpp"),
                tppTarget = layout.nativeSourcesDir.resolve("api.tpp")
            )
            CppApiImplPrinter(
                context = context,
                target = layout.nativeSourcesDir.resolve("api.cpp")
            )
        }
    }

    when(context.buildSystem) {
        is BuildSystem.CMake -> {
            val targetName = "lib${context.moduleName}"
            val projectPath = context.module.dir.posixPath
            val projectBuildPath = layout.nativeBuildDir.resolve("sub").posixPath

            val languages = buildList {
                add("C")
                if(context.language == Language.CPP)
                    add("CXX")
            }.joinToString(" ")

            val sources = buildList {
                add("$<TARGET_OBJECTS:${context.moduleName}>")
                add("emscripten_bindings.c")
                add("api.${context.language.sourceExtension}")
            }.joinToString(" ")

            layout.nativeSourcesDir.resolve("CMakeLists.txt").writeSync($$"""
                cmake_minimum_required(VERSION 3.15)
        
                project("$${context.moduleName}" LANGUAGES $$languages)
                
                set(EXTRA_LINK_FLAGS "" CACHE STRING "Extra linker flags")
                
                # Some compile options
                add_compile_options(-Wno-initializer-overrides)
                
                # Add project
                add_subdirectory("$$projectPath" "$$projectBuildPath")
            
                # Add sources + child project
                add_executable($$targetName $$sources)
                
                # Add Emscripten linker flags
                set_target_properties($$targetName PROPERTIES LINK_FLAGS "${EXTRA_LINK_FLAGS} $${getEmccArgs(context)}")
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

    fun packResult(jsFile: PlatformFile, wasmFile: PlatformFile) {
        jsFile.copyToSync(layout.kotlinResourcesDir!!.resolve("lib${context.moduleName}.mjs"))
        wasmFile.copyToSync(layout.kotlinResourcesDir.resolve("lib${context.moduleName}.wasm"))
    }

    when(val buildSystem = context.buildSystem) {
        is BuildSystem.CMake -> {
            val emcc = locateEmcc(context)

            // Generate CMake build
            val toolchain = emcc.parent()!!.resolve("cmake/Modules/Platform/Emscripten.cmake")

            cmakeGen(
                context,
                dir = layout.nativeSourcesDir,
                buildDir = layout.nativeBuildDir,
                buildType = buildSystem.buildType,
                args = LinkedHashSet(buildSystem.args).apply {
                    this += "-DCMAKE_TOOLCHAIN_FILE=\"$toolchain\""
                }
            )

            // Reload with extra flags
            val extraFlags = layout.nativeBuildDir.resolve("sub/CMakeFiles/${context.moduleName}.dir/flags.make")
                .readSync()
                .split(SystemLineSeparator)
                .firstOrNull { it.startsWith("C_FLAGS = ") }
                ?.replaceFirst("C_FLAGS = ", "")
                ?.trim()
                ?: ""
            if(extraFlags.isNotEmpty()) {
                cmakeGen(
                    context,
                    dir = layout.nativeSourcesDir,
                    buildDir = layout.nativeBuildDir,
                    buildType = buildSystem.buildType,
                    args = LinkedHashSet(buildSystem.args).apply {
                        this += "-DCMAKE_TOOLCHAIN_FILE=\"$toolchain\""
                        this += "-DEXTRA_LINK_FLAGS=\"${extraFlags}\""
                    }
                )
            }

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
                context.module.dir,
                buildSystem.buildType,
                layout.nativeBuildDir
            )

            val moduleName = context.moduleName
            val jsFile = pkgDir.resolve("$moduleName.js")
            val wasmFile = pkgDir.resolve("${moduleName}_bg.wasm")

            // Change `name_bg.wasm` references to `libname.wasm`
            jsFile.writeSync(jsFile.readSync()
                .replace(wasmFile.name, "lib$moduleName.wasm")
                .replace("/* @ts-self-types=\"./$moduleName.d.ts\" */", "")
            )

            packResult(jsFile, wasmFile)
        }
    }
}