package com.huskerdev.nativekt.configurators

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.huskerdev.nativekt.plugin.*
import com.huskerdev.nativekt.printers.HeaderPrinter
import com.huskerdev.nativekt.printers.KotlinAndroidPrinter
import com.huskerdev.nativekt.printers.jvm.CJniArenaPrinter
import com.huskerdev.nativekt.printers.jvm.CJniPrinter
import com.huskerdev.nativekt.printers.jvm.CJniUtilsPrinter
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.IdlResolver
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
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
    module: NativeModule,
    sourceSet: KotlinSourceSet,
    srcRootDir: File,
    cmakeRootDir: File,
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

    val toolchain = File(ndkDir, "build/cmake/android.toolchain.cmake")

    // src dirs

    val srcDir = File(srcRootDir, "android/src")
    val jniLibsDir = File(srcRootDir, "android/jniLibs")

    // CMake dirs

    val cmakeDir = File(cmakeRootDir, "android")
    val cmakeBuildDir = File(cmakeDir, "out")

    // Prepare task

    val prepareTask = project.tasks.register(
        "prepareNatives${module.name.capitalized()}Android",
        PrepareNativesAndroid::class.java
    )
    prepareTask.get().also {
        it.inputs.dir(module.dir(project))
        it.outputs.dirs(cmakeDir, srcDir)

        it.useCoroutines = extension.useCoroutines
        it.expectActual = expectActual

        it.idl = Json.encodeToString(idl)
        it.moduleName = module.name
        it.moduleClasspath = module.classPath

        it.srcDir = srcDir.absolutePath
        it.jniLibsDir = jniLibsDir.absolutePath

        it.cmakeDir = cmakeDir.absolutePath
        it.cmakeBuildDir = cmakeBuildDir.absolutePath
        it.srcFile = srcDir
            .resolve(module.classPath.replace(".", "/"))
            .resolve("${module.name}.android.kt").absolutePath
        it.nativeProjectDir = module.dir(project).absolutePath
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
        it.outputs.dirs(cmakeDir)

        it.outputFolder.set(jniLibsDir)

        it.cmakeArgs      = LinkedHashSet(module.cmakeArgs)
        it.cmakeBuildType = module.buildType
        it.androidTargets = extension.androidTargets.toTypedArray()
        it.cmakeDir       = cmakeDir.absolutePath
        it.cmakeBuildDir  = cmakeBuildDir.absolutePath
        it.moduleName     = module.name
        it.toolchain      = toolchain.absolutePath
        it.compileSdk     = androidExtension.compileSdk!!
    }
    compileTask.dependsOn(prepareTask)

    androidComponents.onVariants {
        it.sources.jniLibs?.addGeneratedSourceDirectory(
            compileTask,
            CompileNativesAndroid::outputFolder
        )
    }
}

private abstract class PrepareNativesAndroid: DefaultTask() {
    @get:Input abstract var useCoroutines: Boolean
    @get:Input abstract var expectActual: Boolean

    @get:Input abstract var idl: String
    @get:Input abstract var moduleName: String
    @get:Input abstract var moduleClasspath: String

    @get:Input abstract var srcDir: String
    @get:Input abstract var jniLibsDir: String

    @get:Input abstract var cmakeDir: String
    @get:Input abstract var cmakeBuildDir: String
    @get:Input abstract var srcFile: String
    @get:Input abstract var nativeProjectDir: String

    init {
        doLast {
            File(srcDir).fresh()
            File(jniLibsDir).fresh()

            val idl = Json.decodeFromString<IdlResolver>(idl)

            val cmakeDir = File(cmakeDir)
            cmakeDir.mkdirs()

            // Create CMakeLists.txt
            File(cmakeDir, "CMakeLists.txt").writeText($$"""
                cmake_minimum_required(VERSION 3.15)
        
                project("$$moduleName")
        
                add_subdirectory("$${
                    nativeProjectDir.replace("\\", "/")
                }" "$${
                    cmakeBuildDir.replace("\\", "/")
                }/sub/${ANDROID_ABI}")
        
                add_library(lib$$moduleName SHARED $<TARGET_OBJECTS:$$moduleName> jni_bindings.c)
            """.trimIndent())

            // Create Kotlin/Android bindings
            KotlinAndroidPrinter(
                idl = idl,
                target = File(srcFile),
                classPath = moduleClasspath,
                moduleName = moduleName,
                useCoroutines = useCoroutines,
                expectActual = expectActual
            )

            CJniUtilsPrinter(
                idl = idl,
                target = File(cmakeDir, "jni_utils.h"),
                classPath = moduleClasspath,
                name = "${moduleName.capitalized()}JNI",
                isAndroid = true
            )

            CJniPrinter(
                idl = idl,
                target = File(cmakeDir, "jni_bindings.c"),
                classPath = moduleClasspath,
                name = "${moduleName.capitalized()}JNI"
            )

            CJniArenaPrinter(
                target = File(cmakeDir, "jni_arena.h"),
                callbacks = idl.callbacks.isNotEmpty()
            )

            HeaderPrinter(
                idl = idl,
                target = File(cmakeDir, "api.h")
            )
        }
    }
}

private abstract class CompileNativesAndroid @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty

    @get:Input abstract var cmakeArgs: LinkedHashSet<String>
    @get:Input abstract var cmakeBuildType: CMakeBuildType
    @get:Input abstract var androidTargets: Array<String>
    @get:Input abstract var cmakeDir: String
    @get:Input abstract var cmakeBuildDir: String
    @get:Input abstract var moduleName: String
    @get:Input abstract var toolchain: String
    @get:Input abstract var compileSdk: Int

    init {
        group = NATIVE_TASK_GROUP
        doLast {
            androidTargets.forEach { abi ->
                val targetBuildDir = File(cmakeBuildDir, abi)

                // Generate CMake build
                cmakeGen(execOps,
                    dir = File(cmakeDir),
                    buildDir = targetBuildDir,
                    buildType = cmakeBuildType,
                    args = LinkedHashSet(cmakeArgs).apply {
                        this += "-DCMAKE_TOOLCHAIN_FILE=\"$toolchain\""
                        this += "-DANDROID_ABI=$abi"
                        this += "-DANDROID_PLATFORM=android-$compileSdk"
                    }
                )

                // Build
                cmakeBuild(execOps, targetBuildDir)

                // Copy library to jniLibs dir
                File(targetBuildDir, "liblib$moduleName.so").copyTo(
                    File(outputFolder.get().asFile, "$abi/lib$moduleName.so")
                )
            }
        }
    }
}