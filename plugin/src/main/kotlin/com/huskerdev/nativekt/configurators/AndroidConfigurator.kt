package com.huskerdev.nativekt.configurators

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.variant.Component
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import com.huskerdev.nativekt.plugin.NativeKtExtension
import com.huskerdev.nativekt.plugin.NativeModule
import com.huskerdev.nativekt.plugin.dir
import com.huskerdev.nativekt.printers.HeaderPrinter
import com.huskerdev.nativekt.printers.KotlinAndroidPrinter
import com.huskerdev.nativekt.printers.jvm.CppJniPrinter
import com.huskerdev.webidl.resolver.IdlResolver
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

internal fun configureAndroid(
    project: Project,
    extension: NativeKtExtension,
    androidExtension: KotlinMultiplatformAndroidLibraryExtension,
    idl: IdlResolver,
    module: NativeModule,
    sourceSet: KotlinSourceSet,
    srcGenDir: File,
    cmakeRootDir: File
) {
    val androidComponents = project.the<KotlinMultiplatformAndroidComponentsExtension>()

    val androidGenDir = File(srcGenDir, "android/src")
    androidGenDir.mkdirs()

    val classPathFile = File(androidGenDir, module.classPath.replace(".", "/"))

    val jniLibsDir = File(srcGenDir, "android/jniLibs")
    jniLibsDir.mkdirs()

    val cmakeDir = File(cmakeRootDir, "android")
    cmakeDir.mkdirs()

    val cmakeBuildDir = File(cmakeDir, "out")
    cmakeBuildDir.mkdirs()

    val commonCmakeBuildDir = File(cmakeRootDir, "common")
    commonCmakeBuildDir.mkdirs()

    sourceSet.kotlin.srcDir(androidGenDir)

    // Create CMakeLists.txt
    File(cmakeDir, "CMakeLists.txt").writeText($$"""
        cmake_minimum_required(VERSION 3.15)

        project("$${module.name}")

        add_subdirectory("$${
            module.dir(project).absolutePath.replace("\\", "/")
        }" "$${
            commonCmakeBuildDir.absolutePath.replace("\\", "/")
        }/android/${ANDROID_ABI}")

        add_library(lib$${module.name} SHARED $<TARGET_OBJECTS:$${module.name}> jni_bindings.c)
    """.trimIndent())

    // Create Kotlin/Android bindings
    KotlinAndroidPrinter(
        idl = idl,
        target = File(classPathFile, "${module.name}.kt"),
        classPath = module.classPath,
        moduleName = module.name,
        useCoroutines = extension.useCoroutines
    )

    CppJniPrinter(
        idl = idl,
        target = File(cmakeDir, "jni_bindings.c"),
        classPath = module.classPath
    )

    HeaderPrinter(
        idl = idl,
        target = File(cmakeDir, "api.h")
    )

    // Compilation task
    val task = project.tasks.register("compileNatives${module.name.capitalized()}Android", CompileTask::class.java) {
        group = "native"
        outputFolder.set(jniLibsDir)

        doLast {
            if (extension.ndkVersion == null)
                throw Exception("NDK version is not specified in 'native { ... }'")

            val ndkDir = File(androidComponents.sdkComponents.sdkDirectory.get().asFile,
                "ndk/${extension.ndkVersion}")
            if (!ndkDir.exists())
                throw Exception("NDK ${extension.ndkVersion} is not installed")

            val toolchain = File(ndkDir, "build/cmake/android.toolchain.cmake")

            extension.androidTargets.forEach { abi ->
                val targetBuildDir = File(cmakeBuildDir, abi)
                cmakeBuild(
                    project, cmakeDir, targetBuildDir, module.buildType,
                    args = setOf(
                        "-DCMAKE_TOOLCHAIN_FILE=\"$toolchain\"",
                        "-DANDROID_ABI=$abi",
                        "-DANDROID_PLATFORM=android-${androidExtension.compileSdk}"
                    )
                )

                // Copy library to jniLibs dir
                File(targetBuildDir, "liblib${module.name}.so").copyTo(
                    File(jniLibsDir, "$abi/lib${module.name}.so")
                )
            }
        }
    }

    androidComponents.onVariants {
        val targets = arrayListOf<Component?>()
        targets += it
        //targets += it.androidTest
        //targets += it.hostTests.map(Map.Entry<String, HostTest>::value)
        //targets += it.deviceTests.map(Map.Entry<String, DeviceTest>::value)

        targets.forEach { target ->
            target?.sources?.jniLibs?.addGeneratedSourceDirectory(
                task,
                CompileTask::outputFolder
            )
        }
    }
}

private abstract class CompileTask: DefaultTask() {

    @get:OutputDirectory
    abstract val outputFolder: DirectoryProperty
}