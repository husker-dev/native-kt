package com.huskerdev.nativekt.configurators

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.huskerdev.nativekt.plugin.*
import com.huskerdev.nativekt.printers.c.CApiHeaderPrinter
import com.huskerdev.nativekt.printers.c.CApiImplPrinter
import com.huskerdev.nativekt.printers.kotlin.KotlinAndroidPrinter
import com.huskerdev.nativekt.printers.c.CJniPrinter
import com.huskerdev.nativekt.printers.c.CJniUtilsPrinter
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.IdlResolver
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.the
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import javax.inject.Inject

internal fun configureAndroidSourceSet(
    project: Project,
    extension: NativeKtAndroidInterface,
    androidExtension: KotlinMultiplatformAndroidLibraryExtension,
    commonTask: TaskProvider<*>?,
    idl: IdlResolver,
    module: NativeProject,
    sourceSet: KotlinSourceSet,
    srcGenDir: File,
    nativesBuildDir: File,
    expectActual: Boolean
) {
    val androidComponents = project.the<KotlinMultiplatformAndroidComponentsExtension>()

    // NDK

    var ndkDir: File
    if(extension.ndkVersion == NDK_LATEST) {
        ndkDir = androidComponents.sdkComponents.sdkDirectory.get().asFile
            .resolve("ndk").listFiles()
            .maxByOrNull { it.name }
            ?: throw UnsupportedOperationException("Can not get latest NDK, because no NDK are installed")
    } else {
        ndkDir = androidComponents.sdkComponents.sdkDirectory.get().asFile
            .resolve("ndk/${extension.ndkVersion}")

        if (!ndkDir.exists()) {
            val available = arrayListOf<String>()
            if (ndkDir.parentFile.exists())
                available += ndkDir.parentFile!!.listFiles().map { it.name }

            var message = "NDK ${extension.ndkVersion} is not installed."
            if (available.isNotEmpty())
                message += " Available:\n\t- ${available.joinToString("\n\t- ")}"

            throw UnsupportedOperationException(message)
        }
    }

    // Kotlin sources
    val srcDir = File(srcGenDir, "android/src")
    val jniLibsDir = File(srcGenDir, "android/jniLibs")

    val kotlinFile = srcDir
        .resolve(module.classPath.replace(".", "/"))
        .resolve("${module.name}.android.kt")

    val nativesBuildSourcesDir = File(nativesBuildDir, "android/sources")
    val nativesBuildOutDir = File(nativesBuildDir, "android/out")

    // Prepare task

    val prepareTask = project.tasks.register(
        "prepareNatives${module.name.capitalized()}Android",
        PrepareNativesAndroid::class.java
    )
    prepareTask.get().also {
        it.inputs.dir(module.dir(project))
        it.inputs.file(module.getNDLFile(project))
        it.outputs.dirs(nativesBuildSourcesDir, srcDir)

        it.idl                      = Json.encodeToString(idl)

        it.moduleName               = module.name
        it.moduleClasspath          = module.classPath

        it.useCoroutines            = extension.useCoroutines
        it.expectActual             = expectActual
        it.useAndroidCriticalNative = extension.useAndroidCriticalNative

        it.nativesBuildSourcesDir   = nativesBuildSourcesDir.absolutePath
        it.nativesBuildOutDir       = nativesBuildOutDir.absolutePath

        it.kotlinFile               = kotlinFile.absolutePath
        it.projectDir               = module.dir(project).absolutePath

        it.buildSystem              = module.buildSystem
    }
    if(commonTask != null)
        prepareTask.dependsOn(commonTask)
    prepareTask.dependsOnReload()

    sourceSet.kotlin.srcDir(prepareTask.map { srcDir })

    // Compilation task

    val compileTask = project.tasks.register(
        "compileNatives${module.name.capitalized()}Android",
        CompileNativesAndroid::class.java
    )
    compileTask.get().also {
        it.inputs.dir(module.dir(project))
        it.inputs.file(module.getNDLFile(project))
        it.outputs.dirs(nativesBuildOutDir)

        it.outputFolder.set(jniLibsDir)

        it.androidTargets         = extension.androidTargets.toTypedArray()
        it.moduleName             = module.name
        it.compileSdk             = androidExtension.compileSdk!!
        it.ndkDir                 = ndkDir.absolutePath

        it.projectDir             = module.dir(project).absolutePath
        it.nativesBuildSourcesDir = nativesBuildSourcesDir.absolutePath
        it.nativesBuildOutDir     = nativesBuildOutDir.absolutePath

        it.buildSystem            = module.buildSystem
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
    @get:Input abstract var idl: String
    @get:Input abstract var moduleName: String
    @get:Input abstract var moduleClasspath: String

    @get:Input abstract var useCoroutines: Boolean
    @get:Input abstract var expectActual: Boolean
    @get:Input abstract var useAndroidCriticalNative: Boolean

    @get:Input abstract var nativesBuildSourcesDir: String
    @get:Input abstract var nativesBuildOutDir: String

    @get:Input abstract var kotlinFile: String
    @get:Input abstract var projectDir: String

    @get:Input abstract var buildSystem: BuildSystem

    @TaskAction
    fun action() {
        val idl = Json.decodeFromString<IdlResolver>(idl)

        val nativesBuildSourcesDir = File(nativesBuildSourcesDir).fresh()
        val nativesBuildOutDir = File(nativesBuildOutDir)
        val projectDir = File(projectDir)

        val jniSourcesDir = File(nativesBuildSourcesDir, "jni")
        jniSourcesDir.mkdirs()

        val sourceExtension = buildSystem.language.sourceExtension ?: "c"
        val headerExtension = buildSystem.language.headerExtension ?: "h"

        // Create Kotlin/Android bindings
        KotlinAndroidPrinter(
            idl = idl,
            target = File(kotlinFile),
            classPath = moduleClasspath,
            moduleName = moduleName,
            useCoroutines = useCoroutines,
            expectActual = expectActual,
            isAndroidCriticalEnabled = useAndroidCriticalNative
        )

        // JNI sources (jni_utils.h, jni_bindings.c, api.h)

        CJniUtilsPrinter(
            idl = idl,
            target = File(jniSourcesDir, "jni_utils.h"),
            classPath = moduleClasspath,
            moduleName = moduleName,
            name = "${moduleName.capitalized()}JNI",
            isAndroid = true
        )

        CJniPrinter(
            idl = idl,
            target = File(jniSourcesDir, "jni_bindings.c"),
            classPath = moduleClasspath,
            moduleName = moduleName,
            name = "${moduleName.capitalized()}JNI",
            isAndroid = true,
            isAndroidCriticalEnabled = useAndroidCriticalNative
        )

        CApiHeaderPrinter(
            idl = idl,
            target = File(jniSourcesDir, "api.h"),
            language = null,
            classPath = moduleClasspath,
            moduleName = moduleName,
            isInternal = true
        )

        // Bindings (api.h, api.c)

        CApiHeaderPrinter(
            idl = idl,
            target = File(nativesBuildSourcesDir, "api.$headerExtension"),
            language = buildSystem.language,
            classPath = moduleClasspath,
            moduleName = moduleName,
            isInternal = true
        )

        CApiImplPrinter(
            idl = idl,
            target = File(nativesBuildSourcesDir, "api.$sourceExtension"),
            language = buildSystem.language,
            classPath = moduleClasspath,
            moduleName = moduleName
        )

        when(buildSystem) {
            is BuildSystem.CMake -> {
                File(nativesBuildSourcesDir, "CMakeLists.txt").writeText($$"""
                    cmake_minimum_required(VERSION 3.15)
            
                    project("$$moduleName"$${if (buildSystem.language == Language.CPP) " LANGUAGES CXX" else ""})
                    
                    set(CMAKE_POSITION_INDEPENDENT_CODE ON)
                    
                    add_subdirectory("$${projectDir.posixPath}" "$${nativesBuildOutDir.posixPath}/sub/${ANDROID_ABI}")
                
                    add_library(lib$$moduleName SHARED $<TARGET_OBJECTS:$$moduleName> api.$$sourceExtension)
                    
                    target_link_libraries(lib$$moduleName PRIVATE -Wl,--whole-archive $${nativesBuildOutDir.posixPath}/${ANDROID_ABI}/libjni.a -Wl,--no-whole-archive)
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

    @get:Input abstract var androidTargets: Array<String>
    @get:Input abstract var moduleName: String
    @get:Input abstract var compileSdk: Int
    @get:Input abstract var ndkDir: String

    @get:Input abstract var projectDir: String
    @get:Input abstract var nativesBuildSourcesDir: String
    @get:Input abstract var nativesBuildOutDir: String

    @get:Input abstract var buildSystem: BuildSystem

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        val nativesBuildSourcesDir = File(nativesBuildSourcesDir)
        val nativesBuildOutDir = File(nativesBuildOutDir)
        val projectDir = File(projectDir)

        // Find toolchain
        val toolchainDir = File(ndkDir, "toolchains/llvm/prebuilt")
            .listFiles().first { it.name != ".DS_Store" }

        val toolchainBinDir = File(toolchainDir, "bin")
        val toolchainIncludeDir = File(toolchainDir, "sysroot/usr/include")

        // Compile each target
        androidTargets.forEach { target ->

            val targetBuildDir = File(nativesBuildOutDir, target)
            targetBuildDir.mkdirs()

            fun clangCompileAndroidTarget(
                sources: List<String>,
                linkerArgs: List<String> = emptyList(),
                dynamicLib: Boolean = false,
                extension: String = systemExtension(dynamicLib),
                outputBaseName: String = "out"
            ) = clangCompile(execOps,
                clang = File(toolchainBinDir, "clang").posixPath,
                sources = sources,
                includeDirs = listOf(
                    toolchainIncludeDir.posixPath,
                    File(toolchainIncludeDir, toLlvmTarget(target)).posixPath
                ),
                linkerArgs = listOf(
                    "--target=${toLlvmTarget(target)}$compileSdk",
                    *linkerArgs.toTypedArray()
                ),
                dynamicLib = dynamicLib,
                workingDir = targetBuildDir,
                outputBaseName = outputBaseName,
                extension = extension
            )

            // Compile jni bindings
            val jniSourcesDir = File(nativesBuildSourcesDir, "jni")
            val libjni = clangCompileAndroidTarget(
                sources = listOf(File(jniSourcesDir, "jni_bindings.c").posixPath),
                dynamicLib = false,
                outputBaseName = "libjni"
            )

            // Compile and link language

            when (val buildSystem = buildSystem) {
                is BuildSystem.CMake -> {
                    val cmakeToolchain = File(ndkDir, "build/cmake/android.toolchain.cmake")

                    // Generate CMake build
                    cmakeGen(
                        execOps,
                        dir = nativesBuildSourcesDir,
                        buildDir = targetBuildDir,
                        buildType = buildSystem.buildType,
                        args = LinkedHashSet(buildSystem.args).apply {
                            this += "-DCMAKE_TOOLCHAIN_FILE=\"$cmakeToolchain\""
                            this += "-DANDROID_ABI=$target"
                            this += "-DANDROID_PLATFORM=android-$compileSdk"
                        }
                    )

                    // Build
                    cmakeBuild(execOps, targetBuildDir)

                    // Copy library to jniLibs dir
                    File(targetBuildDir, "liblib$moduleName.so").copyTo(
                        File(outputFolder.get().asFile, "$target/lib$moduleName.so"),
                        overwrite = true
                    )
                }
                is BuildSystem.Cargo -> {
                    val rustBuildDir = cargoBuild(
                        execOps,
                        project = projectDir,
                        buildDir = File(nativesBuildOutDir, "rust"),
                        buildType = buildSystem.buildType,
                        target = toLlvmTarget(target, rustc = true)
                    )

                    clangCompileAndroidTarget(
                        sources = listOf(File(nativesBuildSourcesDir, "api.c").posixPath),
                        linkerArgs = listOf(
                            "$rustBuildDir/lib$moduleName.a",
                            "-Wl,--whole-archive",
                            libjni.posixPath,
                            "-Wl,--no-whole-archive"
                        ),
                        dynamicLib = true,
                        extension = "so"
                    ).copyTo(
                        File(outputFolder.get().asFile, "$target/lib$moduleName.so"),
                        overwrite = true
                    )
                }
            }
        }
    }

    private fun toLlvmTarget(target: String, rustc: Boolean = false) = when(target) {
        "x86_64"      -> "x86_64-linux-android"
        "x86"         -> "i686-linux-android"
        "armeabi-v7a" ->  if(rustc) "armv7-linux-androideabi" else "armv7a-linux-androideabi"
        "arm64-v8a"   -> "aarch64-linux-android"
        else -> throw UnsupportedOperationException("Unsupported Android target: $target")
    }
}