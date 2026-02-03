package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.plugin.NativeKtExtension
import com.huskerdev.nativekt.plugin.NativeModule
import com.huskerdev.nativekt.plugin.TargetType
import com.huskerdev.nativekt.utils.currentTargetType
import com.huskerdev.nativekt.utils.dir
import com.huskerdev.nativekt.utils.exec
import com.huskerdev.nativekt.printers.kn.DefPrinter
import com.huskerdev.nativekt.printers.HeaderPrinter
import com.huskerdev.nativekt.printers.kn.KotlinNativePrinter
import com.huskerdev.nativekt.utils.cmakeBuild
import com.huskerdev.webidl.resolver.IdlResolver
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.File

internal fun configureNative(
    project: Project,
    extension: NativeKtExtension,
    idl: IdlResolver,
    module: NativeModule,
    sourceSet: Pair<KotlinSourceSet, TargetType>,
    srcGenDir: File,
    cmakeRootDir: File,
    expectActual: Boolean
) {
    if(sourceSet.second.kotlinTarget !in currentTargetType().compiles)
        return

    val kotlin = project.the<KotlinMultiplatformExtension>()

    val targetName = sourceSet.second.kotlinTarget
    val sourceSetName = sourceSet.first.name

    val nativeGenDir = File(srcGenDir, "native/$sourceSetName")
    nativeGenDir.mkdirs()

    val cinteropGenDir = File(srcGenDir, "cinterop")
    cinteropGenDir.mkdirs()

    val classPathFile = File(nativeGenDir, module.classPath.replace(".", "/"))

    val cinteropDefFile = File(cinteropGenDir, "${module.name}.def")
    val cinteropHeaderFile = File(cinteropGenDir, "${module.name}.h")

    val cmakeDir = File(cmakeRootDir, "kn")
    cmakeDir.mkdirs()

    val cmakeBuildDir = File(cmakeDir, targetName)
    cmakeBuildDir.mkdirs()

    val commonCmakeBuildDir = File(cmakeRootDir, "common")
    commonCmakeBuildDir.mkdirs()

    sourceSet.first.kotlin.srcDir(nativeGenDir)


    File(cmakeDir, "CMakeLists.txt").writeText($$"""
        cmake_minimum_required(VERSION 3.15)

        project("$${module.name}")

        add_subdirectory("$${
            module.dir(project).absolutePath.replace("\\", "/")
        }" "$${
            commonCmakeBuildDir.absolutePath.replace("\\", "/")
        }")

        add_library(lib_$${module.name} STATIC $<TARGET_OBJECTS:$${module.name}>)
    """.trimIndent())

    KotlinNativePrinter(
        idl = idl,
        target = File(classPathFile, "${module.name}.kt"),
        classPath = module.classPath,
        moduleName = module.name,
        useCoroutines = extension.useCoroutines,
        expectActual = expectActual
    )

    HeaderPrinter(
        idl = idl,
        target = cinteropHeaderFile,
        guardName = module.name.uppercase()
    )
    DefPrinter(
        target = cinteropDefFile,
        headerFile = cinteropHeaderFile,
        classPath = module.classPath
    )


    val target = kotlin.targets.findByName(targetName) as? KotlinNativeTarget
        ?: throw UnsupportedOperationException()

    val compilation = target.compilations.findByName("main")
        ?: throw UnsupportedOperationException()

    target.binaries {
        all {
            linkerOpts(
                "-L${cmakeBuildDir.absolutePath.replace("\\", "/")}",
                "-llib_${module.name}"
            )
        }
    }

    compilation.cinterops {
        create("natives_${module.name}").definitionFile.set(cinteropDefFile)
    }

    val task = project.tasks.register("compileNatives${module.name.capitalized()}Native${targetName.capitalized()}") {
        group = "native"
        doLast {
            fun flags(vararg flags: String) = setOf(
                "-DCMAKE_C_FLAGS=\"${flags.joinToString(" ")}\"",
                "-DCMAKE_CXX_FLAGS=\"${flags.joinToString(" ")}\""
            )
            fun xcSdkVersion(sdk: String) =
                project.exec("xcrun --sdk $sdk --show-sdk-platform-version", silent = true)
            fun xcSdkSysroot(sdk: String) =
                project.exec("xcrun --sdk $sdk --show-sdk-path", silent = true)


            val args = hashSetOf(
                "-DCMAKE_C_COMPILER=clang",
                "-DCMAKE_CXX_COMPILER=clang++"
            )
            args += when(sourceSet.second) {
                TargetType.IOS_SIMULATOR_ARM64 -> flags(
                    "-arch arm64",
                    "-target arm64-apple-ios${xcSdkVersion("iphonesimulator")}-simulator",
                    "-isysroot ${xcSdkSysroot("iphonesimulator")}"
                )
                TargetType.IOS_X64 -> flags(
                    "-arch x86_64",
                    "-target x86_64-apple-ios${xcSdkVersion("iphonesimulator")}-simulator",
                    "-isysroot ${xcSdkSysroot("iphonesimulator")}"
                )
                TargetType.IOS_ARM64 -> flags(
                    "-arch arm64",
                    "-target arm64-apple-ios${xcSdkVersion("iphoneos")}",
                    "-isysroot ${xcSdkSysroot("iphoneos")}"
                )
                TargetType.TVOS_ARM64 -> flags(
                    "-arch arm64",
                    "-target arm64-apple-tvos${xcSdkVersion("appletvos")}",
                    "-isysroot ${xcSdkSysroot("appletvos")}"
                )
                TargetType.TVOS_SIMULATOR_ARM64 -> flags(
                    "-arch arm64",
                    "-target arm64-apple-tvos${xcSdkVersion("appletvsimulator")}-simulator",
                    "-isysroot ${xcSdkSysroot("appletvsimulator")}"
                )
                TargetType.TVOS_X64 -> flags(
                    "-arch x86_64",
                    "-target x86_64-apple-tvos${xcSdkVersion("appletvsimulator")}-simulator",
                    "-isysroot ${xcSdkSysroot("appletvsimulator")}"
                )
                TargetType.WATCHOS_ARM32 -> flags(
                    "-arch armv7k",
                    "-target armv7k-apple-watchos${xcSdkVersion("watchos")}",
                    "-isysroot ${xcSdkSysroot("watchos")}"
                )
                TargetType.WATCHOS_ARM64 -> flags(
                    "-arch arm64_32",
                    "-target arm64-apple-watchos${xcSdkVersion("watchos")}",
                    "-isysroot ${xcSdkSysroot("watchos")}"
                )
                TargetType.WATCHOS_DEVICE_ARM64 -> flags(
                    "-arch arm64",
                    "-target arm64-apple-watchos${xcSdkVersion("watchos")}",
                    "-isysroot ${xcSdkSysroot("watchos")}"
                )
                TargetType.WATCHOS_SIMULATOR_ARM64 -> flags(
                    "-arch arm64",
                    "-target arm64-apple-watchos${xcSdkVersion("watchsimulator")}-simulator",
                    "-isysroot ${xcSdkSysroot("watchsimulator")}"
                )
                TargetType.WATCHOS_X64 -> flags(
                    "-arch x86_64",
                    "-target x86_64-apple-watchos${xcSdkVersion("watchsimulator")}-simulator",
                    "-isysroot ${xcSdkSysroot("watchsimulator")}"
                )
                TargetType.MACOS_ARM64 -> flags("-arch arm64")
                TargetType.MACOS_X64 -> flags("-arch x86_64")
                else -> emptySet()
            }

            cmakeBuild(project, cmakeDir, cmakeBuildDir, module.buildType, args)
        }
    }

    project.tasks.matching { it.name == "compileKotlin${targetName.capitalized()}" }.forEach {
        it.dependsOn(task)
    }
}