package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.printers.kotlin.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.stdlibx.OS
import com.huskerdev.stdlibx.fs.SyncPath

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
        target = jniSourcesDir.resolve("nativekt.h"),
        language = null,
        isImpl = true,
    )

    // Version script
    layout.nativeSourcesDir.resolve("nativekt.map.txt").writeString("""
        NATIVEKT {
            global:
                JNI_OnLoad;
            local:
                *;
        };
    """.trimIndent())

    when(val buildSystem = context.buildSystem) {
        is BuildSystem.CMake -> {
            val moduleName = context.moduleName

            layout.nativeSourcesDir.resolve("dummy.${context.language.sourceExtension}").writeString("")
            layout.nativeSourcesDir.resolve("CMakeLists.txt").writeString($$"""
                 cmake_minimum_required(VERSION 3.24)
        
                project("$$moduleName"$${if (buildSystem.language == Language.CPP) " LANGUAGES CXX" else ""})
                
                $${if (buildSystem.language == Language.CPP) "set(CMAKE_CXX_STANDARD 17)" else ""}
                $${if (buildSystem.language == Language.CPP) "set(CMAKE_CXX_STANDARD_REQUIRED ON)" else ""}
                
                set(CMAKE_POSITION_INDEPENDENT_CODE ON)
                add_compile_options(-Wno-initializer-overrides)
                
                add_subdirectory("$${context.module.buildSystem.projectDir.invariantSeparatorsPath}" "$${layout.nativeBuildDir.invariantSeparatorsPath}/${ANDROID_ABI}/cmake/sub")
            
                add_library(shared SHARED dummy.$${context.language.sourceExtension})
                target_link_libraries(shared PRIVATE $${buildSystem.libraryName})
                
                add_library(static STATIC dummy.$${context.language.sourceExtension})
                target_link_libraries(static PRIVATE $${buildSystem.libraryName})
            """.trimIndent())
        }
        is BuildSystem.Cargo -> Unit
        is BuildSystem.SPM -> Unit
    }
}

fun compileAndroid(
    context: NativeModuleContext,
    existingSdkDir: SyncPath?
) {
    val layout = DirectoryLayout.of(context, TargetType.ANDROID)
    val ndkDir = getNdkDir(context.module.android, existingSdkDir)

    val sdkVersion = context.module.android.sdkVersion
    val cmdExt = if(OS.isWindows) ".cmd" else ""
    val useCppStdLib = context.language == Language.CPP || context.language == Language.SWIFT

    // Find toolchain
    val toolchainDir = ndkDir.resolve("toolchains/llvm/prebuilt").resolve(when(OS.current) {
        OS.MACOS -> "darwin-x86_64"
        OS.LINUX -> "linux-x86_64"
        OS.WINDOWS -> "windows-x86_64"
        else -> throw UnsupportedOperationException()
    })
    val toolchainBinDir = toolchainDir.resolve("bin")
    val toolchainIncludeDir = toolchainDir.resolve("sysroot/usr/include")
    val ndkAr = toolchainBinDir.resolve("llvm-ar")

    // Compile each target
    context.module.android.targets.forEach { target ->
        context.logger!!.info("Compiling Android target: $target")

        val llvmTarget = toAndroidLlvmTarget(target)
        val ndkClang = toolchainBinDir.resolve("$llvmTarget${sdkVersion}-clang$cmdExt")
        val ndkClangCpp = toolchainBinDir.resolve("$llvmTarget${sdkVersion}-clang++$cmdExt")

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
            clang = ndkClang.invariantSeparatorsPath,
            ar = ndkAr.invariantSeparatorsPath,
            sources = sources,
            includeDirs = listOf(
                toolchainIncludeDir.invariantSeparatorsPath,
                toolchainIncludeDir.resolve(llvmTarget).invariantSeparatorsPath
            ),
            linkerArgs = listOf(
                "--target=$llvmTarget$sdkVersion",
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
            sources = listOf(jniSourcesDir.resolve("impl.c").invariantSeparatorsPath),
            dynamicLib = false,
            outputBaseName = "libjni"
        )

        fun linkJni(
            linkerArgs: List<String>,
            staticLib: SyncPath
        ) = clangCompile(context,
            clang = (if(useCppStdLib) ndkClangCpp else ndkClang).invariantSeparatorsPath,
            sources = emptyList(),
            includeDirs = emptyList(),
            linkerArgs = buildList {
                add("-O3")
                add("-Wl,--whole-archive")
                add(libJni.invariantSeparatorsPath)
                add("-Wl,--no-whole-archive")
                add(staticLib.absoluteString())
                addAll(linkerArgs)

                // Statically link stdc++ lib (if needed)
                if(useCppStdLib)
                    add("-static-libstdc++")

                // Only keep JNI_OnLoad function
                add("-Wl,--version-script=${layout.nativeSourcesDir.resolve("nativekt.map.txt").invariantSeparatorsPath}")
                add("-Wl,--no-undefined-version")
            },
            dynamicLib = true,
            outputBaseName = "libjni",
            workingDir = targetBuildDir
        )

        // Compile and link language

        val (staticLib, linkerArgs) = when (val buildSystem = context.buildSystem) {
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
                        this += "-DANDROID_PLATFORM=android-$sdkVersion"
                    }
                )

                // Build
                cmakeBuild(context, cmakeBuildDir)

                Pair(
                    cmakeBuildDir.resolve("libstatic.a"),
                    cmakeLinkerArgs(context,
                        cmakeBuildDir = cmakeBuildDir,
                        moduleName = "shared"
                    )
                )
            }

            is BuildSystem.Cargo -> {
                val rustBuildDir = targetBuildDir.resolve("rust")
                val rustTarget = toAndroidLlvmTarget(target, rustc = true)
                val rustTargetSnakeCase = rustTarget.replace("-", "_").uppercase()

                val rustOutDir = cargoBuild(
                    context,
                    project = buildSystem.projectDir,
                    buildType = buildSystem.buildType,
                    buildDir = rustBuildDir,
                    target = rustTarget,
                    env = mapOf(
                        "CARGO_TARGET_${rustTargetSnakeCase}_LINKER" to ndkClang.invariantSeparatorsPath
                    )
                )
                val rustLinkerFlags = cargoLinkerFlags(
                    context,
                    project = buildSystem.projectDir,
                    buildType = buildSystem.buildType,
                    buildDir = rustBuildDir,
                    target = rustTarget,
                    env = mapOf(
                        "CARGO_TARGET_${rustTargetSnakeCase}_LINKER" to ndkClang.invariantSeparatorsPath
                    )
                )
                Pair(
                    rustOutDir.resolve("lib${buildSystem.libraryName}.a"),
                    rustLinkerFlags
                )
            }
            is BuildSystem.SPM -> {
                val spmBuildDir = targetBuildDir.resolve("rust")

                val androidSwiftSdk = swiftSdkList(context)
                    .firstOrNull { it.endsWith("_android") }
                    ?: throw Exception("Android Swift SDK is not installed")
                val triple = toAndroidSwiftTarget(target, sdkVersion)

                val staticResourcePath = swiftAndroidStaticResourcesPath(
                    context = context,
                    ndkPath = ndkDir.invariantSeparatorsPath,
                    androidSwiftSdk = androidSwiftSdk,
                    triple = triple
                )

                val spmOutDir = spmBuild(
                    context = context,
                    projectDir = buildSystem.projectDir,
                    buildDir = spmBuildDir,
                    buildType = buildSystem.buildType,
                    args = "--swift-sdk $androidSwiftSdk --triple $triple --static-swift-stdlib --product ${buildSystem.staticLibraryName}",
                    env = mapOf(
                        "ANDROID_NDK_HOME" to ndkDir.invariantSeparatorsPath
                    )
                )

                val spmLinkerFlags = buildList {
                    add("-L$staticResourcePath/android")
                    add("-Wl,--start-group")
                    addAll(listOf(
                        "-lswiftCore",
                        "-lswiftAndroid",
                        "-lswiftSynchronization",
                        "-lswift_Concurrency",
                        "-lswift_StringProcessing",
                        "-lswift_RegexParser",
                    ))
                    add("-Wl,--end-group")
                    addAll(SyncPath("$staticResourcePath/android/static-stdlib-args.lnk").readLines()
                        .filter { it.isNotEmpty() && it.trim() !in setOf(
                            "-lstdc++",
                            "-Xlinker --exclude-libs",
                            "-Xlinker ALL",
                        ) })
                }

                Pair(
                    spmOutDir.resolve("lib${buildSystem.staticLibraryName}.a"),
                    spmLinkerFlags
                )
            }
        }

        // Link JNI
        val libFile = linkJni(
            linkerArgs = linkerArgs,
            staticLib = staticLib
        )

        // Copy library to jniLibs dir
        val targetLibName = "$target/lib${context.moduleName}.so"

        val dst = layout.kotlinResourcesDir!!.resolve(targetLibName)
        dst.parent!!.createDirectories()
        libFile.copyTo(dst, overwrite = true)
    }
}