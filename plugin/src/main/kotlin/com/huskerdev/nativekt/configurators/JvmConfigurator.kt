package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.plugin.NativeKtExtension
import com.huskerdev.nativekt.plugin.NativeModule
import com.huskerdev.nativekt.printers.HeaderPrinter
import com.huskerdev.nativekt.printers.jvm.CppJniPrinter
import com.huskerdev.nativekt.printers.jvm.KotlinJvmPrinter
import com.huskerdev.nativekt.utils.cmakeBuild
import com.huskerdev.nativekt.utils.currentTargetType
import com.huskerdev.nativekt.utils.dir
import com.huskerdev.nativekt.utils.libExtension
import com.huskerdev.webidl.resolver.IdlResolver
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

private const val LOCAL_RUN_CONFIGURATION = "_localNativeJvmRun"

internal fun configureJvm(
    project: Project,
    extension: NativeKtExtension,
    idl: IdlResolver,
    module: NativeModule,
    sourceSet: KotlinSourceSet,
    srcGenDir: File,
    cmakeRootDir: File,
    expectActual: Boolean
) {
    if(project.configurations.findByName(LOCAL_RUN_CONFIGURATION) == null) {
        project.configurations.create(LOCAL_RUN_CONFIGURATION) {
            isCanBeConsumed = false
            isCanBeResolved = true
        }.apply {
            project.configurations["jvmRuntimeClasspath"].extendsFrom(this)
            project.configurations["jvmTestRuntimeClasspath"].extendsFrom(this)
            project.configurations["jvmMainRuntimeClasspath"].extendsFrom(this)
        }
    }

    val jvmGenDir = File(srcGenDir, "jvm/src")
    jvmGenDir.mkdirs()

    val libsGenDir = File(srcGenDir, "jvm/libs")
    libsGenDir.mkdirs()

    val cmakeDir = File(cmakeRootDir, "jvm")
    cmakeDir.mkdirs()

    val commonCmakeDir = File(cmakeRootDir, "common")
    commonCmakeDir.mkdirs()

    val cmakeBuildDir = File(cmakeDir, currentTargetType().kotlinTarget)
    cmakeBuildDir.mkdirs()

    val classPathFile = File(jvmGenDir, module.classPath.replace(".", "/"))

    val jdkPath = System.getProperty("java.home").replace("\\", "/")
    val jdkPlatformName = when {
        Os.isFamily(Os.FAMILY_WINDOWS) -> "win32"
        Os.isFamily(Os.FAMILY_MAC) -> "darwin"
        Os.isFamily(Os.FAMILY_UNIX) -> "linux"
        else -> throw UnsupportedOperationException()
    }

    val platformName = when {
        Os.isFamily(Os.FAMILY_WINDOWS) -> "windows"
        Os.isFamily(Os.FAMILY_MAC) -> "macos"
        Os.isFamily(Os.FAMILY_UNIX) -> "linux"
        else -> throw UnsupportedOperationException()
    }
    val libArch = when {
        Os.isFamily(Os.FAMILY_MAC) -> "universal"
        Os.isArch("aarch64") -> "arm64"
        else -> "x86"
    }
    val libOutFileName = "liblib_${module.name}.${libExtension}"
    val libFullFileName = "lib${module.name}-$libArch.${libExtension}"
    val targetLibFile = File(libsGenDir, libFullFileName)

    sourceSet.kotlin.srcDir(jvmGenDir)


    File(cmakeDir, "CMakeLists.txt").writeText($$"""
        cmake_minimum_required(VERSION 3.15)

        project("$${module.name}")

        add_subdirectory("$${module.dir(project).absolutePath.replace("\\", "/")}" "$${commonCmakeDir.absolutePath.replace("\\", "/")}")

        add_library(lib_$${module.name} SHARED $<TARGET_OBJECTS:$${module.name}> jni_bindings.c)
        
        target_include_directories(lib_$${module.name} PRIVATE "$${jdkPath}/include")
        target_include_directories(lib_$${module.name} PRIVATE "$${jdkPath}/include/$$jdkPlatformName")
    """.trimIndent())

    KotlinJvmPrinter(
        idl = idl,
        target = File(classPathFile, "${module.name}.kt"),
        classPath = module.classPath,
        moduleName = module.name,
        useCoroutines = extension.useCoroutines,
        expectActual = expectActual
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

    val compileTask = project.tasks.register("compileNatives${module.name.capitalized()}Jvm") {
        group = "native"
        doLast {
            val args = hashSetOf(
                "-DCMAKE_C_COMPILER=clang",
                "-DCMAKE_CXX_COMPILER=clang++"
            )
            if(Os.isFamily(Os.FAMILY_MAC)) {
                args += setOf(
                    "-DCMAKE_C_FLAGS=\"-arch x86_64 -arch arm64\"",
                    "-DCMAKE_CXX_FLAGS=\"-arch x86_64 -arch arm64\""
                )
            }

            cmakeBuild(project, cmakeDir, cmakeBuildDir, module.buildType, args)

            cmakeBuildDir.listFiles()!!.first {
                it.name == libOutFileName
            }.copyTo(targetLibFile, overwrite = true)
        }
    }

    val packNativeJar = project.tasks.findByName("allNativesJar") as Jar?
        ?: project.tasks.register("allNativesJar", Jar::class.java) {
            group = "native"
            archiveAppendix.set("jvm")
            archiveClassifier.set("$platformName-$libArch")

            project.dependencies.add(LOCAL_RUN_CONFIGURATION, project.files(this@register))
        }.get()

    packNativeJar.dependsOn(compileTask)
    packNativeJar.from(targetLibFile)
}