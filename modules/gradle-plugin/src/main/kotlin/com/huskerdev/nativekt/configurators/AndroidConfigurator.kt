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

    val ndkDir: File
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

    val nativesBuildDir = File(nativesBuildDir, "android")

    // Prepare task

    val prepareTask = project.tasks.register(
        "prepareNatives${module.name.capitalized()}Android",
        PrepareNativesAndroid::class.java
    )
    prepareTask.get().also {
        it.inputs.dir(module.dir(project))
        it.outputs.dirs(nativesBuildDir, srcDir)

        it.idl                      = Json.encodeToString(idl)

        it.moduleName               = module.name
        it.moduleClasspath          = module.classPath

        it.useCoroutines            = extension.useCoroutines
        it.expectActual             = expectActual
        it.useAndroidCriticalNative = extension.useAndroidCriticalNative

        it.nativesBuildDir          = nativesBuildDir.absolutePath

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
        it.outputs.dirs(nativesBuildDir)

        it.outputFolder.set(jniLibsDir)

        it.androidTargets   = extension.androidTargets.toTypedArray()
        it.moduleName       = module.name
        it.compileSdk       = androidExtension.compileSdk!!
        it.ndkDir           = ndkDir.absolutePath

        it.projectDir        = module.dir(project).absolutePath
        it.nativesBuildDir  = nativesBuildDir.absolutePath

        it.buildSystem      = module.buildSystem
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

    @get:Input abstract var nativesBuildDir: String

    @get:Input abstract var kotlinFile: String
    @get:Input abstract var projectDir: String

    @get:Input abstract var buildSystem: BuildSystem

    @TaskAction
    fun action() {
        val idl = Json.decodeFromString<IdlResolver>(idl)

        val srcList = arrayListOf("api.c", "jni_bindings.c")

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

        CJniUtilsPrinter(
            idl = idl,
            target = File(nativesBuildDir, "jni_utils.h"),
            classPath = moduleClasspath,
            name = "${moduleName.capitalized()}JNI",
            isAndroid = true
        )

        CJniPrinter(
            idl = idl,
            target = File(nativesBuildDir, "jni_bindings.c"),
            classPath = moduleClasspath,
            name = "${moduleName.capitalized()}JNI",
            isAndroid = true,
            isAndroidCriticalEnabled = useAndroidCriticalNative
        )

        CApiHeaderPrinter(
            idl = idl,
            target = File(nativesBuildDir, "api.h"),
            isInternal = true
        )

        CApiImplPrinter(
            idl = idl,
            target = File(nativesBuildDir, "api.c"),
            classPath = moduleClasspath
        )

        when(buildSystem) {
            is BuildSystem.CMake -> {
                File(nativesBuildDir, "CMakeLists.txt").writeText($$"""
                    cmake_minimum_required(VERSION 3.15)
            
                    project("$$moduleName")
            
                    add_subdirectory("$${
                        projectDir.replace("\\", "/")
                    }" "$${
                        nativesBuildDir.replace("\\", "/")
                    }/sub/${ANDROID_ABI}")
                
                    add_library(lib$$moduleName SHARED $<TARGET_OBJECTS:$$moduleName> $${srcList.joinToString(" ")})
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
    @get:Input abstract var nativesBuildDir: String

    @get:Input abstract var buildSystem: BuildSystem

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        when(val buildSystem = buildSystem) {
            is BuildSystem.CMake -> {
                val toolchain = File(ndkDir, "build/cmake/android.toolchain.cmake")

                androidTargets.forEach { abi ->
                    val targetBuildDir = File(nativesBuildDir, abi)

                    // Generate CMake build
                    cmakeGen(execOps,
                        dir = File(nativesBuildDir),
                        buildDir = targetBuildDir,
                        buildType = buildSystem.buildType,
                        args = LinkedHashSet(buildSystem.args).apply {
                            this += "-DCMAKE_TOOLCHAIN_FILE=\"$toolchain\""
                            this += "-DANDROID_ABI=$abi"
                            this += "-DANDROID_PLATFORM=android-$compileSdk"
                        }
                    )

                    // Build
                    cmakeBuild(execOps, targetBuildDir)

                    // Copy library to jniLibs dir
                    File(targetBuildDir, "liblib$moduleName.so").copyTo(
                        File(outputFolder.get().asFile, "$abi/lib$moduleName.so"),
                        overwrite = true
                    )
                }
            }
            is BuildSystem.Cargo -> {
                val toolchainDir = File(ndkDir, "toolchains/llvm/prebuilt")
                    .listFiles().first { it.name != ".DS_Store" }

                val toolchainBinDir = File(toolchainDir, "bin")
                val toolchainIncludeDir = File(toolchainDir, "sysroot/usr/include")

                androidTargets.forEach { target ->
                    val rustBuildDir = cargoBuild(execOps,
                        project = File(projectDir),
                        buildDir = File(nativesBuildDir, "rust"),
                        buildType = buildSystem.buildType,
                        target = toLlvmTarget(target, rustc = true)
                    )

                    val targetBuildDir = File(nativesBuildDir, target)
                    targetBuildDir.mkdirs()

                    clangCompile(execOps,
                        clang = File(toolchainBinDir, "clang").posixPath,
                        sources = listOf("../api.c", "../jni_bindings.c"),
                        includeDirs = listOf(
                            toolchainIncludeDir.posixPath,
                            File(toolchainIncludeDir, toLlvmTarget(target)).posixPath
                        ),
                        linkerArgs = listOf(
                            "--target=${toLlvmTarget(target)}$compileSdk",
                            "$rustBuildDir/lib$moduleName.a"
                        ),
                        dynamicLib = true,
                        workingDir = targetBuildDir,
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