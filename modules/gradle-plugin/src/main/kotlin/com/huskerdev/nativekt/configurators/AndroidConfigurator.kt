package com.huskerdev.nativekt.configurators

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.plugin.*
import com.huskerdev.nativekt.printers.c.CApiHeaderPrinter
import com.huskerdev.nativekt.printers.c.CApiImplPrinter
import com.huskerdev.nativekt.printers.c.CJniPrinter
import com.huskerdev.nativekt.printers.cpp.CppApiHeaderPrinter
import com.huskerdev.nativekt.printers.cpp.CppApiImplPrinter
import com.huskerdev.nativekt.printers.kotlin.KotlinAndroidPrinter
import com.huskerdev.nativekt.utils.*
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.the
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import javax.inject.Inject


private fun getNdkDir(
    extension: NativeKtAndroidInterface,
    androidComponents: KotlinMultiplatformAndroidComponentsExtension
): File {
    return if(extension.ndkVersion == null) {
        androidComponents.sdkComponents.sdkDirectory.get().asFile
            .resolve("ndk").listFiles()
            ?.maxByOrNull { it.name }
            ?: throw UnsupportedOperationException("Can not get latest NDK, because no NDK are installed")
    } else {
        val dir = androidComponents.sdkComponents.sdkDirectory.get().asFile
            .resolve("ndk/${extension.ndkVersion}")

        if (!dir.exists()) {
            val available = arrayListOf<String>()
            if (dir.parentFile.exists())
                available += dir.parentFile!!.listFiles()!!.map { it.name }

            var message = "NDK ${extension.ndkVersion} is not installed."
            if (available.isNotEmpty())
                message += " Available:\n\t- ${available.joinToString("\n\t- ")}"

            throw UnsupportedOperationException(message)
        } else dir
    }
}

private fun toLlvmTarget(target: String, rustc: Boolean = false) = when(target) {
    "x86_64"      -> "x86_64-linux-android"
    "x86"         -> "i686-linux-android"
    "armeabi-v7a" ->  if(rustc) "armv7-linux-androideabi" else "armv7a-linux-androideabi"
    "arm64-v8a"   -> "aarch64-linux-android"
    else -> throw UnsupportedOperationException("Unsupported Android target: $target")
}

internal fun configureAndroidSourceSet(
    project: Project,
    context: NativeModuleContext,
    androidExtension: KotlinMultiplatformAndroidLibraryExtension,
    sourceSet: KotlinSourceSet,
    expectActual: Boolean
) {
    val extension = context.extension as NativeKtAndroidInterface
    val androidComponents = project.the<KotlinMultiplatformAndroidComponentsExtension>()

    // NDK
    val ndkDir = getNdkDir(extension, androidComponents)

    // Kotlin sources
    val srcDir = File(context.srcGenDir, "android/src")
    val jniLibsDir = File(context.srcGenDir, "android/jniLibs")

    val kotlinFile = srcDir
        .resolve(context.classPath.replace(".", "/"))
        .resolve("${context.moduleName}.android.kt")

    val nativesBuildSourcesDir = File(context.nativesBuildDir, "android/sources")
    val nativesBuildOutDir = File(context.nativesBuildDir, "android/out")

    // Prepare task

    val prepareTask = project.tasks.register(
        "prepareNatives${context.moduleName.upperCamelCase()}Android",
        PrepareNativesAndroid::class.java
    )
    prepareTask.get().also {
        it.inputs.dir(context.module.dir)
        it.inputs.file(context.module.ndlFile())
        it.outputs.dirs(nativesBuildSourcesDir, srcDir)

        it.context                  = context
        it.expectActual             = expectActual

        it.nativesBuildSourcesDir   = nativesBuildSourcesDir.absolutePath
        it.nativesBuildOutDir       = nativesBuildOutDir.absolutePath

        it.kotlinFile               = kotlinFile.absolutePath
    }
    prepareTask.get().dependsOnProjectReload()

    project.afterEvaluate {
        val commonTask = project.tasks.findByName("prepareNatives${context.moduleName.upperCamelCase()}Common")
            ?: throw UnsupportedOperationException("[native-kt] Something went wrong with Android initialization")
        prepareTask.get().dependsOn(commonTask)
    }

    sourceSet.kotlin.srcDir(prepareTask.map { srcDir })

    // Compilation task

    val compileTask = project.tasks.register(
        "compileNatives${context.moduleName.upperCamelCase()}Android",
        CompileNativesAndroid::class.java
    )
    compileTask.get().also {
        it.inputs.dir(context.module.dir)
        it.inputs.file(context.module.ndlFile())
        it.outputs.dirs(nativesBuildOutDir)

        it.outputFolder.set(jniLibsDir)

        it.context                = context

        it.compileSdk             = androidExtension.compileSdk!!
        it.ndkDir                 = ndkDir.absolutePath

        it.nativesBuildSourcesDir = nativesBuildSourcesDir.absolutePath
        it.nativesBuildOutDir     = nativesBuildOutDir.absolutePath
    }
    compileTask.dependsOn(prepareTask)

    androidComponents.onVariants {
        it.sources.jniLibs?.addGeneratedSourceDirectory(
            compileTask,
            CompileNativesAndroid::outputFolder
        )
    }

    // Apply critical stub lib
    if(extension.applyAndroidCriticalStub && extension.useAndroidCriticalNative) {
        sourceSet.dependencies {
            compileOnly("com.huskerdev:native-kt-android-critical-stub:1.0.0")
        }
    }
}

private abstract class PrepareNativesAndroid: DefaultTask() {
    @get:Input abstract var context: NativeModuleContext
    @get:Input abstract var expectActual: Boolean

    @get:Input abstract var nativesBuildSourcesDir: String
    @get:Input abstract var nativesBuildOutDir: String

    @get:Input abstract var kotlinFile: String

    @TaskAction
    fun action() {
        val moduleName = context.moduleName
        val buildSystem = context.buildSystem

        val nativesBuildSourcesDir = File(nativesBuildSourcesDir).fresh()
        val nativesBuildOutDir = File(nativesBuildOutDir)
        val projectDir = File(context.module.dir.absolutePath)

        // Kotlin bindings
        KotlinAndroidPrinter(
            context = context,
            target = File(kotlinFile),
            expectActual = expectActual
        )

        // JNI
        val jniSourcesDir = File(nativesBuildSourcesDir, "jni")
        jniSourcesDir.mkdirs()

        CJniPrinter(
            context = context,
            target = File(jniSourcesDir, "impl.c"),
            headerTarget = File(jniSourcesDir, "impl.h"),
            isAndroid = true
        )

        CApiHeaderPrinter(
            context = context,
            target = File(jniSourcesDir, "api.h"),
            language = null,
            isInternal = true,
        )

        // Native API

        when (context.language) {
            Language.C -> {
                CApiHeaderPrinter(
                    context = context,
                    target = File(nativesBuildSourcesDir, "api.h"),
                    isInternal = true,
                )
                CApiImplPrinter(
                    context = context,
                    target = File(nativesBuildSourcesDir, "api.c")
                )
            }
            Language.CPP -> {
                CppApiHeaderPrinter(
                    context = context,
                    target = File(nativesBuildSourcesDir, "api.hpp"),
                    tppTarget = File(nativesBuildSourcesDir, "api.tpp"),
                )
                CppApiImplPrinter(
                    context = context,
                    target = File(nativesBuildSourcesDir, "api.cpp")
                )
            }
            Language.RUST -> Unit
        }

        when(buildSystem) {
            is BuildSystem.CMake -> {
                File(nativesBuildSourcesDir, "CMakeLists.txt").writeText($$"""
                    cmake_minimum_required(VERSION 3.15)
            
                    project("$$moduleName"$${if (buildSystem.language == Language.CPP) " LANGUAGES CXX" else ""})
                    
                    $${if (buildSystem.language == Language.CPP) "set(CMAKE_CXX_STANDARD 17)" else ""}
                    $${if (buildSystem.language == Language.CPP) "set(CMAKE_CXX_STANDARD_REQUIRED ON)" else ""}
                    
                    set(CMAKE_POSITION_INDEPENDENT_CODE ON)
                    add_compile_options(-Wno-initializer-overrides)
                    
                    add_subdirectory("$${projectDir.posixPath}" "$${nativesBuildOutDir.posixPath}/sub/${ANDROID_ABI}")
                
                    add_library(lib$$moduleName SHARED $<TARGET_OBJECTS:$$moduleName> api.$${context.language.sourceExtension})
                    
                    target_link_libraries(lib$$moduleName PRIVATE -Wl,--whole-archive ${JNI_LIBRARY} -Wl,--no-whole-archive)
                """.trimIndent())
            }
            is BuildSystem.Cargo -> Unit
        }
    }
}

private abstract class CompileNativesAndroid @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    @get:Input abstract var context: NativeModuleContext

    @get:Input abstract var compileSdk: Int
    @get:Input abstract var ndkDir: String

    @get:Input abstract var nativesBuildSourcesDir: String
    @get:Input abstract var nativesBuildOutDir: String

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        val extension = context.extension as NativeKtAndroidInterface

        val moduleName = context.moduleName
        val buildSystem = context.buildSystem

        val nativesBuildSourcesDir = File(nativesBuildSourcesDir)
        val nativesBuildOutDir = File(nativesBuildOutDir).fresh()
        val projectDir = File(context.module.dir.absolutePath)

        // Find toolchain
        val toolchainDir = File(ndkDir, "toolchains/llvm/prebuilt")
            .listFiles()!!.first { it.name != ".DS_Store" }

        val toolchainBinDir = File(toolchainDir, "bin")
        val toolchainIncludeDir = File(toolchainDir, "sysroot/usr/include")

        // Compile each target
        extension.androidTargets.forEach { target ->
            println("Compiling Android target: $target")

            val llvmTarget = toLlvmTarget(target)
            val ndkClang = toolchainBinDir.listFiles()!!
                .filter { it.name.startsWith(llvmTarget) && it.name.endsWith("clang") }
                .maxOfOrNull { it }
                ?: throw UnsupportedOperationException("Could not find capable Android LLVM target: $target")
            val ndkAr = File(toolchainBinDir, "llvm-ar")

            val targetBuildDir = File(nativesBuildOutDir, target)
            targetBuildDir.mkdirs()

            fun clangCompileAndroidTarget(
                sources: List<String>,
                linkerArgs: List<String> = emptyList(),
                dynamicLib: Boolean = false,
                extension: String = systemExtension(dynamicLib),
                outputBaseName: String = "out"
            ) = clangCompile(execOps, context,
                clang = ndkClang.posixPath,
                ar = ndkAr.posixPath,
                sources = sources,
                includeDirs = listOf(
                    toolchainIncludeDir.posixPath,
                    File(toolchainIncludeDir, llvmTarget).posixPath
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
            val jniSourcesDir = File(nativesBuildSourcesDir, "jni")
            val libJni = clangCompileAndroidTarget(
                sources = listOf(File(jniSourcesDir, "impl.c").posixPath),
                dynamicLib = false,
                outputBaseName = "libjni"
            )

            // Compile and link language

            val libFile = when (val buildSystem = buildSystem) {
                is BuildSystem.CMake -> {
                    val cmakeToolchain = File(ndkDir, "build/cmake/android.toolchain.cmake")

                    // Generate CMake build
                    cmakeGen(
                        execOps, context,
                        dir = nativesBuildSourcesDir,
                        buildDir = targetBuildDir,
                        buildType = buildSystem.buildType,
                        args = LinkedHashSet(buildSystem.args).apply {
                            this += "-DCMAKE_TOOLCHAIN_FILE=\"$cmakeToolchain\""
                            this += "-DANDROID_ABI=$target"
                            this += "-DANDROID_PLATFORM=android-$compileSdk"
                            this += "-DJNI_LIBRARY=${libJni.posixPath}"
                        }
                    )

                    // Build
                    cmakeBuild(execOps, context, targetBuildDir)

                    File(targetBuildDir, "liblib$moduleName.so")
                }
                is BuildSystem.Cargo -> {
                    val rustTarget = toLlvmTarget(target, rustc = true)
                    val rustBuildDir = cargoBuild(
                        execOps, context,
                        project = projectDir,
                        buildDir = File(nativesBuildOutDir, "rust"),
                        buildType = buildSystem.buildType,
                        target = rustTarget,
                        env = mapOf(
                            "CARGO_TARGET_${rustTarget.replace("-", "_").uppercase()}_LINKER" to ndkClang.posixPath
                        )
                    )

                    val rustLinkerFlags = cargoLinkerFlags(execOps, context,
                        project = projectDir,
                        buildType = buildSystem.buildType,
                        buildDir = File(nativesBuildOutDir, "rust"),
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
                            "$rustBuildDir/lib$moduleName.a",
                            *rustLinkerFlags.toTypedArray()
                        ),
                        dynamicLib = true,
                        extension = "so"
                    )
                }
            }

            // Copy library to jniLibs dir
            libFile.copyTo(
                File(outputFolder.get().asFile, "$target/lib$moduleName.so"),
                overwrite = true
            )
        }
    }
}