package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.printers.kotlin.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.stdlibx.*
import com.huskerdev.stdlibx.fs.SyncPath

fun prepareJvm(
    context: NativeModuleContext,
    useExpectActual: Boolean
) {
    val layout = DirectoryLayout.of(context, TargetType.JVM)
    val useJni = context.module.jvm.useJNI

    // Generate all files

    KotlinJvmPrinter(
        context = context,
        target = layout.kotlinSourceFile,
        expectActual = useExpectActual
    )

    if(useJni) {
        val jniSourcesDir = layout.nativeSourcesDir.resolve("jni")
        jniSourcesDir.createDirectories()

        CJniPrinter(
            context = context,
            target = jniSourcesDir.resolve("impl.c"),
            headerTarget = jniSourcesDir.resolve("impl.h"),
            isAndroid = false
        )

        CApiHeaderPrinter(
            context = context,
            target = jniSourcesDir.resolve("nativekt.h"),
            language = null,
            isImpl = true,
        )
    }

    when(val buildSystem = context.buildSystem) {
        is BuildSystem.CMake -> {
            layout.nativeSourcesDir.resolve("dummy.${context.language.sourceExtension}").writeString("")
            layout.nativeSourcesDir.resolve("CMakeLists.txt").writeString($$"""
                cmake_minimum_required(VERSION 3.24)
        
                project("native-kt"$${if (buildSystem.language == Language.CPP) " LANGUAGES CXX" else ""})
                
                $${if (buildSystem.language == Language.CPP) "set(CMAKE_CXX_STANDARD 17)" else ""}
                $${if (buildSystem.language == Language.CPP) "set(CMAKE_CXX_STANDARD_REQUIRED ON)" else ""}
                
                if(CMAKE_C_COMPILER)
                    set(DUMMY ${CMAKE_C_COMPILER})
                endif()
                
                set(CMAKE_POSITION_INDEPENDENT_CODE ON)
                add_compile_options(-Wno-initializer-overrides)
                
                add_subdirectory("$${buildSystem.projectDir.invariantSeparatorsPath}" "$${
                    layout.nativeBuildDir.resolve("cmake/sub").invariantSeparatorsPath
                }")
                
                add_library(shared SHARED dummy.$${context.language.sourceExtension})
                target_link_libraries(shared PRIVATE $${buildSystem.libraryName})
                
                add_library(static STATIC dummy.$${context.language.sourceExtension})
                target_link_libraries(static PRIVATE $${buildSystem.libraryName})
            """.trimIndent())
        }
        is BuildSystem.Cargo -> Unit
    }
}

fun compileJvm(
    context: NativeModuleContext,
    existingJdkIncludeDir: SyncPath?
) {
    val layout = DirectoryLayout.of(context, TargetType.JVM)
    layout.nativeBuildDir.createDirectories()

    val useJni = context.module.jvm.useJNI

    // Compile JNI if needed
    val jniLib = if(useJni) {
        val jniIncludeDir = existingJdkIncludeDir ?: run {
            val javaDir = if(getenv("JAVA_HOME") != null)
                SyncPath(getenv("JAVA_HOME")!!)
            else if(OS.current != OS.WINDOWS)
                SyncPath(context.execute("which java"))
            else throw NullPointerException("Could not find JDK. Environment variable 'JAVA_HOME' is not defined.")

            if(!javaDir.exists() || !javaDir.resolve("include").exists())
                throw NullPointerException("Current Java directory is not a valid JDK: ${javaDir.absoluteString()}")
            javaDir.resolve("include")
        }
        val jniDir = layout.nativeSourcesDir.resolve("jni")

        val platformDirName = when(OS.current) {
            OS.WINDOWS -> "win32"
            OS.MACOS -> "darwin"
            OS.LINUX -> "linux"
            else -> throw UnsupportedOperationException()
        }

        clangCompile(context,
            sources = listOf(jniDir.resolve("impl.c").invariantSeparatorsPath),
            includeDirs = listOf(
                jniIncludeDir.invariantSeparatorsPath,
                jniIncludeDir.resolve(platformDirName).invariantSeparatorsPath
            ),
            linkerArgs = listOf("-O3"),
            dynamicLib = false,
            outputBaseName = "libjni",
            workingDir = layout.nativeBuildDir
        )
    } else null

    fun linkJni(
        linkerArgs: List<String>,
        staticLib: SyncPath
    ) = clangCompile(context,
        clang = if(context.language == Language.CPP) "clang++" else "clang",
        sources = emptyList(),
        includeDirs = emptyList(),
        linkerArgs = buildList {
            add("-O3")
            add(wholeArchive(jniLib!!.invariantSeparatorsPath))
            add(staticLib.absoluteString())
            addAll(linkerArgs)
            if (OS.current == OS.WINDOWS) {
                add("-Wl,--export-all-symbols")
                add("-l:libunwind.a")
            }
        },
        dynamicLib = true,
        outputBaseName = "libjni",
        workingDir = layout.nativeBuildDir
    )

    val (
        staticLib: SyncPath,
        dynLib: SyncPath,
        linkerArgs: () -> List<String>
    ) = when(val buildSystem = context.buildSystem) {
        is BuildSystem.CMake -> {
            val cmakeBuildDir = layout.nativeBuildDir.resolve("cmake")

            cmakeGen(context,
                dir = layout.nativeSourcesDir,
                buildDir = cmakeBuildDir,
                buildType = buildSystem.buildType,
                args = LinkedHashSet<String>().apply {
                    addAll(buildSystem.args)
                    add("-DCMAKE_C_COMPILER=clang")
                    add("-DCMAKE_CXX_COMPILER=clang++")
                }
            )
            cmakeBuild(context, cmakeBuildDir)

            Triple(
                cmakeBuildDir.resolve("libstatic.a"),
                cmakeBuildDir.resolve("libshared.${OS.current.dylibExtension}")
            ) {
                cmakeLinkerArgs(
                    context,
                    cmakeBuildDir = cmakeBuildDir,
                    moduleName = "shared"
                )
            }
        }

        is BuildSystem.Cargo -> {
            val cargoBuildDir = layout.nativeBuildDir.resolve("cargo")
            val rustOutDir = cargoBuild(context,
                project = buildSystem.projectDir,
                buildType = buildSystem.buildType,
                buildDir = cargoBuildDir
            )

            val prefix = if(OS.current == OS.WINDOWS) "" else "lib"
            Triple(
                rustOutDir.resolve("lib${buildSystem.packageName}.a"),
                rustOutDir.resolve("${prefix}${buildSystem.packageName}.${OS.current.dylibExtension}")
            ) {
                cargoLinkerFlags(
                    context,
                    project = buildSystem.projectDir,
                    buildType = buildSystem.buildType,
                    buildDir = cargoBuildDir
                )
            }
        }
    }

    // Link JNI if needed
    val libFile = if(useJni) {
        linkJni(
            linkerArgs = linkerArgs(),
            staticLib = staticLib
        )
    } else dynLib

    // Copy library to resources
    val targetLibFile = "lib${context.moduleName}-${Arch.current.name.lowercase()}.${OS.current.dylibExtension}"

    val dst = layout.kotlinResourcesDir!!.resolve(targetLibFile)
    dst.parent!!.createDirectories()
    libFile.copyTo(dst, overwrite = true)
}