package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.printers.cpp.*
import com.huskerdev.nativekt.printers.kotlin.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.osutils.*
import io.github.vinceglb.filekit.*

fun prepareAndroid(
    context: NativeModuleContext,
    useExpectActual: Boolean
) {
    val layout = DirectoryLayout.of(context, TargetType.ANDROID)

    // Kotlin bindings
    KotlinAndroidPrinter(
        context = context,
        target = layout.kotlinSourceFile,
        expectActual = useExpectActual
    )

    // JNI
    val jniSourcesDir = layout.nativeSourcesDir.resolve("jni")
    jniSourcesDir.createDirectories()

    CJniPrinter(
        context = context,
        target = jniSourcesDir.resolve("impl.c"),
        headerTarget = jniSourcesDir.resolve("impl.h"),
        isAndroid = true
    )

    CApiHeaderPrinter(
        context = context,
        target = jniSourcesDir.resolve("api.h"),
        language = null,
        isInternal = true,
    )

    // Native API

    when (context.language) {
        Language.C -> {
            CApiHeaderPrinter(
                context = context,
                target = layout.nativeSourcesDir.resolve("api.h"),
                isInternal = true,
            )
            CApiImplPrinter(
                context = context,
                target = layout.nativeSourcesDir.resolve("api.c")
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
                target = layout.nativeSourcesDir.resolve("api.cpp")
            )
        }
        Language.RUST -> Unit
    }

    when(val buildSystem = context.buildSystem) {
        is BuildSystem.CMake -> {
            val moduleName = context.moduleName
            layout.nativeSourcesDir.resolve("CMakeLists.txt").writeSync($$"""
                cmake_minimum_required(VERSION 3.15)
        
                project("$$moduleName"$${if (buildSystem.language == Language.CPP) " LANGUAGES CXX" else ""})
                
                $${if (buildSystem.language == Language.CPP) "set(CMAKE_CXX_STANDARD 17)" else ""}
                $${if (buildSystem.language == Language.CPP) "set(CMAKE_CXX_STANDARD_REQUIRED ON)" else ""}
                
                set(CMAKE_POSITION_INDEPENDENT_CODE ON)
                add_compile_options(-Wno-initializer-overrides)
                
                add_subdirectory("$${context.module.dir.posixPath}" "$${layout.nativeBuildDir.posixPath}/${ANDROID_ABI}/cmake/sub")
            
                add_library(lib$$moduleName SHARED $<TARGET_OBJECTS:$$moduleName> api.$${context.language.sourceExtension})
                
                target_link_libraries(lib$$moduleName PRIVATE -Wl,--whole-archive ${JNI_LIBRARY} -Wl,--no-whole-archive)
            """.trimIndent())
        }
        is BuildSystem.Cargo -> Unit
    }
}

fun compileAndroid(
    context: NativeModuleContext,
    compileSdk: Int,
    existingSdkDir: PlatformFile?
) {
    val layout = DirectoryLayout.of(context, TargetType.ANDROID)
    val extension = context.configuration as NativeKtAndroidConfiguration
    val ndkDir = getNdkDir(extension, existingSdkDir)

    // Find toolchain
    val toolchainDir = ndkDir.resolve("toolchains/llvm/prebuilt")
        .list().first { it.name != ".DS_Store" }

    val toolchainBinDir = toolchainDir.resolve("bin")
    val toolchainIncludeDir = toolchainDir.resolve("sysroot/usr/include")

    // Compile each target
    extension.androidTargets.forEach { target ->
        println("Compiling Android target: $target")

        val llvmTarget = toAndroidLlvmTarget(target)
        val ndkClang = toolchainBinDir.list()
            .filter { it.name.startsWith(llvmTarget) && it.name.endsWith("clang") }
            .maxByOrNull { it.name }
            ?: throw UnsupportedOperationException("Could not find capable Android LLVM target: $target")
        val ndkAr = toolchainBinDir.resolve("llvm-ar")

        val targetBuildDir = layout.nativeBuildDir.resolve(target)
        targetBuildDir.createDirectories()

        fun clangCompileAndroidTarget(
            sources: List<String>,
            linkerArgs: List<String> = emptyList(),
            dynamicLib: Boolean = false,
            extension: String = if (dynamicLib) OS.current.dylibExtension else OS.current.staticLibExtension,
            outputBaseName: String = "out"
        ) = clangCompile(
            context,
            clang = ndkClang.posixPath,
            ar = ndkAr.posixPath,
            sources = sources,
            includeDirs = listOf(
                toolchainIncludeDir.posixPath,
                toolchainIncludeDir.resolve(llvmTarget).posixPath
            ),
            linkerArgs = listOf(
                "--target=$llvmTarget$compileSdk",
                *linkerArgs.toTypedArray()
            ),
            dynamicLib = dynamicLib,
            workingDir = targetBuildDir,
            outputBaseName = outputBaseName,
            extension = extension
        )

        // Compile jni bindings
        val jniSourcesDir = layout.nativeSourcesDir.resolve("jni")
        val libJni = clangCompileAndroidTarget(
            sources = listOf(jniSourcesDir.resolve("impl.c").posixPath),
            dynamicLib = false,
            outputBaseName = "libjni"
        )

        // Compile and link language

        val libFile = when (val buildSystem = context.buildSystem) {
            is BuildSystem.CMake -> {
                val cmakeBuildDir = targetBuildDir.resolve("cmake")
                val cmakeToolchain = ndkDir.resolve("build/cmake/android.toolchain.cmake")

                // Generate CMake build
                cmakeGen(
                    context,
                    dir = layout.nativeSourcesDir,
                    buildDir = cmakeBuildDir,
                    buildType = buildSystem.buildType,
                    args = LinkedHashSet(buildSystem.args).apply {
                        this += "-DCMAKE_TOOLCHAIN_FILE=\"$cmakeToolchain\""
                        this += "-DANDROID_ABI=$target"
                        this += "-DANDROID_PLATFORM=android-$compileSdk"
                        this += "-DJNI_LIBRARY=${libJni.posixPath}"
                    }
                )

                // Build
                cmakeBuild(context, cmakeBuildDir)

                cmakeBuildDir.resolve("liblib${context.moduleName}.so")
            }

            is BuildSystem.Cargo -> {
                val rustBuildDir = targetBuildDir.resolve("rust")

                val rustTarget = toAndroidLlvmTarget(target, rustc = true)
                val rustOutDir = cargoBuild(
                    context,
                    project = context.module.dir,
                    buildDir = rustBuildDir,
                    buildType = buildSystem.buildType,
                    target = rustTarget,
                    env = mapOf(
                        "CARGO_TARGET_${rustTarget.replace("-", "_").uppercase()}_LINKER" to ndkClang.posixPath
                    )
                )

                val rustLinkerFlags = cargoLinkerFlags(
                    context,
                    project = context.module.dir,
                    buildType = buildSystem.buildType,
                    buildDir = rustBuildDir,
                    target = rustTarget,
                    env = mapOf(
                        "CARGO_TARGET_${rustTarget.replace("-", "_").uppercase()}_LINKER" to ndkClang.posixPath
                    )
                )

                clangCompileAndroidTarget(
                    sources = listOf(),
                    linkerArgs = listOf(
                        "-Wl,--whole-archive",
                        libJni.posixPath,
                        "-Wl,--no-whole-archive",
                        rustOutDir.resolve("lib${context.moduleName}.a").posixPath,
                        *rustLinkerFlags.toTypedArray()
                    ),
                    dynamicLib = true,
                    extension = "so"
                )
            }
        }

        // Copy library to jniLibs dir
        val targetLibName = "$target/lib${context.moduleName}.so"

        libFile.copyToSync(layout.kotlinResourcesDir!!.resolve(targetLibName))
    }
}