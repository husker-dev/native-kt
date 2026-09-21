package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.printers.cpp.*
import com.huskerdev.nativekt.printers.kotlin.*
import com.huskerdev.nativekt.utils.*
import io.github.vinceglb.filekit.*


fun prepareNative(
    context: NativeModuleContext,
    targetType: TargetType,
    useExpectActual: Boolean,
    shouldInit: Boolean
) {
    val layout = DirectoryLayout.of(context, targetType)
    val moduleName = context.moduleName

    layout.cinteropDir!!.createDirectories()
    val cinteropHeaderFile = layout.cinteropDir.resolve("header.h")

    val linkerOpts = arrayListOf<String>()

    // Generate header
    CApiHeaderPrinter(
        context = context,
        target = cinteropHeaderFile,
        language = null,
        isInternal = true,
    )

    // Generate api sources if needed
    fun createCApi() {
        when (context.buildSystem.language) {
            Language.C -> {
                CApiHeaderPrinter(
                    context = context,
                    target = layout.nativeSourcesDir.resolve("api.h"),
                    isInternal = true
                )
                CApiImplPrinter(
                    context = context,
                    target = layout.nativeSourcesDir.resolve("api.c"),
                )
            }
            Language.CPP -> {
                CppApiHeaderPrinter(
                    context = context,
                    target = layout.nativeSourcesDir.resolve("api.hpp"),
                    tppTarget = layout.nativeSourcesDir.resolve("api.tpp"),
                )
                CppApiImplPrinter(
                    context = context,
                    target = layout.nativeSourcesDir.resolve("api.cpp"),
                )
            }
            else -> Unit
        }
    }

    when(val buildSystem = context.buildSystem) {
        is BuildSystem.CMake -> {

            // Create C/C++ Api
            createCApi()
            val sourceExtension = buildSystem.language.sourceExtension ?: "c"

            // Mangle C non-static functions
            val funcRedefines = if(buildSystem.language == Language.C && context.allOperations.isNotEmpty()) {
                val symbolDefines = context.allOperations
                    .joinToString(" ") { "${it.cname}=${it.cnameMangled(context)}__impl" }
                listOf(
                    "target_compile_definitions($moduleName PRIVATE $symbolDefines)",
                    "target_compile_definitions(lib_$moduleName PRIVATE $symbolDefines)",
                    "target_compile_definitions(libstatic_$moduleName PRIVATE $symbolDefines)"
                )
            } else listOf("", "", "")

            // Create CMake file
            layout.nativeSourcesDir.resolve("CMakeLists.txt").writeSync($$"""
                cmake_minimum_required(VERSION 3.15)
        
                project("$$moduleName")
                
                $${if (buildSystem.language == Language.CPP) "set(CMAKE_CXX_STANDARD 17)" else ""}
                $${if (buildSystem.language == Language.CPP) "set(CMAKE_CXX_STANDARD_REQUIRED ON)" else ""}
                
                set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY $${layout.nativeBuildDir.posixPath})
                set(CMAKE_LIBRARY_OUTPUT_DIRECTORY $${layout.nativeBuildDir.posixPath})
                set(CMAKE_RUNTIME_OUTPUT_DIRECTORY $${layout.nativeBuildDir.posixPath})
                
                set(CMAKE_POSITION_INDEPENDENT_CODE ON)
                
                add_compile_options(-Wno-initializer-overrides)
                
                add_subdirectory("$${context.module.dir.posixPath}" "$${layout.nativeBuildDir.resolve("sub").posixPath}")
                    
                add_library(lib_$$moduleName SHARED api.$$sourceExtension)
                target_link_libraries(lib_$$moduleName PUBLIC $$moduleName)
                
                add_library(libstatic_$$moduleName STATIC api.$$sourceExtension)
                target_link_libraries(libstatic_$$moduleName PRIVATE $$moduleName)
                
                $${funcRedefines[0]}
                $${funcRedefines[1]}
                $${funcRedefines[2]}
            """.trimIndent())

            // Configure CMake (if needed)
            if(shouldInit) {
                configureCMake(
                    context, targetType,
                    cmakeArgs = LinkedHashSet(buildSystem.args),
                    cmakeDir = layout.nativeSourcesDir,
                    cmakeBuildDir = layout.nativeBuildDir,
                    cmakeBuildType = buildSystem.buildType
                )

                linkerOpts += layout.nativeBuildDir.resolve("liblibstatic_$moduleName.a").posixPath

                // Get linker opts
                linkerOpts += extractLinkerOpts(
                    context,
                    layout.nativeBuildDir,
                    moduleName,
                    isKN = true
                )
            }
        }
        is BuildSystem.Cargo -> {
            if(shouldInit) {
                val rustFlags = cargoLinkerFlags(
                    context,
                    project = context.module.dir,
                    buildType = buildSystem.buildType,
                    buildDir = layout.nativeBuildDir,
                    target = getCargoTarget(targetType),
                    isKN = true
                )
                val rustBuildDir = cargoTargetDir(
                    buildDir = layout.nativeBuildDir,
                    buildType = buildSystem.buildType,
                    target = getCargoTarget(targetType)
                )

                linkerOpts += listOf(
                    *rustFlags.toTypedArray(),
                    rustBuildDir.resolve("lib$moduleName.a").posixPath
                )
            }
        }
    }

    if(shouldInit && DebugKind.PRINT_LINKER_OPTIONS in context.debug)
        context.logger?.error("Linker options for '$moduleName':\n\t${linkerOpts.joinToString("\n\t")}")

    // Create .def file
    DefPrinter(
        context = context,
        target = layout.cinteropFile!!,
        headerFile = cinteropHeaderFile,
        linkerOpts = linkerOpts
    )

    // Generate Kotlin files
    KotlinNativePrinter(
        context = context,
        target = layout.kotlinSourceFile,
        expectActual = useExpectActual
    )
}

fun compileNative(
    context: NativeModuleContext,
    targetType: TargetType,
) {
    val layout = DirectoryLayout.of(context, targetType)
    val moduleName = context.moduleName

    when(val buildSystem = context.buildSystem) {
        is BuildSystem.CMake -> {
            cmakeBuild(context, layout.nativeBuildDir)

            prepareNativeLibraryForKN(context,
                lib = layout.nativeBuildDir.resolve("liblibstatic_$moduleName.a"),
                initSymbolName = context.mangle("init"),
                targetArgs = getClangTargetArgs(context, targetType)
            )
        }
        is BuildSystem.Cargo -> {
            cargoBuild(context,
                project = context.module.dir,
                buildType = buildSystem.buildType,
                buildDir = layout.nativeBuildDir,
                target = getCargoTarget(targetType)
            )
        }
    }
}