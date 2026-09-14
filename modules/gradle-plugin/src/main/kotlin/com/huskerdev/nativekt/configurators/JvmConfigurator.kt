package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.plugin.BuildSystem
import com.huskerdev.nativekt.plugin.Language
import com.huskerdev.nativekt.plugin.NATIVE_TASK_GROUP
import com.huskerdev.nativekt.plugin.NativeKtJvmInterface
import com.huskerdev.nativekt.printers.c.CApiHeaderPrinter
import com.huskerdev.nativekt.printers.c.CApiImplPrinter
import com.huskerdev.nativekt.printers.c.CJniPrinter
import com.huskerdev.nativekt.printers.cpp.CppApiHeaderPrinter
import com.huskerdev.nativekt.printers.cpp.CppApiImplPrinter
import com.huskerdev.nativekt.printers.kotlin.KotlinJvmPrinter
import com.huskerdev.nativekt.utils.*
import com.huskerdev.osutils.Arch
import com.huskerdev.osutils.OS
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.jvm.tasks.Jar
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import javax.inject.Inject

private const val LOCAL_RUN_CONFIGURATION = "_localNativeJvmRun"

private fun platformName() = when(OS.current) {
    OS.WINDOWS -> "windows"
    OS.MACOS -> "macos"
    OS.LINUX -> "linux"
    else -> throw UnsupportedOperationException()
}

private fun jdkPlatformName() = when(OS.current) {
    OS.WINDOWS -> "win32"
    OS.MACOS -> "darwin"
    OS.LINUX -> "linux"
    else -> throw UnsupportedOperationException()
}

private fun libArch(useUniversalMacOSLib: Boolean) = when {
    OS.current == OS.MACOS && useUniversalMacOSLib -> "universal"
    else -> Arch.current.name.lowercase()
}

private val JNI_INCLUDE_FILES = listOf(
    "darwin/jawt_md.h",
    "darwin/jni_md.h",
    "linux/jawt_md.h",
    "linux/jni_md.h",
    "win32/jawt_md.h",
    "win32/jni_md.h",
    "win32/bridge/AccessBridgeCallbacks.h",
    "win32/bridge/AccessBridgeCalls.h",
    "win32/bridge/AccessBridgePackages.h",
    "classfile_constants.h",
    "jawt.h",
    "jdwpTransport.h",
    "jni.h",
    "jvmti.h",
    "jvmticmlr.h"
).associateWith { "/com/huskerdev/nativekt/include/$it" }

internal fun configureJvm(
    project: Project,
    commonTask: TaskProvider<*>?,
    context: NativeModuleContext,
    sourceSet: KotlinSourceSet,
    expectActual: Boolean
) {
    val extension = context.extension as NativeKtJvmInterface

    if(!extension.useJNI && !extension.useForeignApi && !extension.useJVMCI)
        throw UnsupportedOperationException("All JVM native implementation are disabled (JNI, Foreign, JVMCI)")

    if(project.configurations.findByName(LOCAL_RUN_CONFIGURATION) == null) {
        project.configurations.create(LOCAL_RUN_CONFIGURATION) {
            isCanBeConsumed = false
            isCanBeResolved = true
        }.apply {
            // multi-target
            project.configurations.findByName("jvmRuntimeClasspath")?.extendsFrom(this)
            project.configurations.findByName("jvmTestRuntimeClasspath")?.extendsFrom(this)
            project.configurations.findByName("jvmMainRuntimeClasspath")?.extendsFrom(this)

            // jvm-only
            project.configurations.findByName("runtimeClasspath")?.extendsFrom(this)
            project.configurations.findByName("testRuntimeClasspath")?.extendsFrom(this)
            project.configurations.findByName("mainRuntimeClasspath")?.extendsFrom(this)
        }
    }

    val libArch = libArch(extension.useUniversalMacOSLib)
    val libOutFileName = "liblib_${context.moduleName}.$libExtension"
    val libFullFileName = "lib${context.moduleName}-$libArch.$libExtension"

    // src dirs
    val srcDir = File(context.srcGenDir, "jvm/src")
    val libsDir = File(context.srcGenDir, "jvm/libs")
    val targetLibFile = File(libsDir, libFullFileName)

    val nativesBuildSourcesDir = File(context.nativesBuildDir, "jvm/sources")
    val nativesBuildOutDir = File(context.nativesBuildDir, "jvm/out")

    val kotlinFile = srcDir
        .resolve(context.classPath.replace(".", "/"))
        .resolve("${context.moduleName}.jvm.kt")

    // Prepare task

    val prepareTask = project.tasks.register(
        "prepareNatives${context.moduleName.upperCamelCase()}Jvm",
        PrepareNativesJvm::class.java
    ) {
        this.inputs.dir(context.module.projectDir)
        this.inputs.file(context.module.ndlFile())
        this.outputs.dirs(nativesBuildSourcesDir, srcDir)

        this.context                = context
        this.expectActual           = expectActual

        this.nativesBuildSourcesDir = nativesBuildSourcesDir.absolutePath
        this.nativesBuildOutDir     = nativesBuildOutDir.absolutePath

        this.kotlinFile             = kotlinFile.absolutePath
    }
    if(commonTask != null)
        prepareTask.get().dependsOn(commonTask)
    prepareTask.get().dependsOnProjectReload()

    sourceSet.kotlin.srcDirs(prepareTask.map { srcDir })

    // Compile task

    val compileTask = project.tasks.register(
        "compileNatives${context.moduleName.upperCamelCase()}Jvm",
        CompileNativesJvm::class.java
    ) {
        this.inputs.dir(context.module.projectDir)
        this.inputs.file(context.module.ndlFile())
        this.outputs.dir(nativesBuildOutDir)

        this.context                = context

        this.nativesBuildSourcesDir = nativesBuildSourcesDir.absolutePath
        this.nativesBuildOutDir     = nativesBuildOutDir.absolutePath

        this.libOutFileName         = libOutFileName
        this.targetLibFile          = targetLibFile.absolutePath
    }
    compileTask.get().dependsOn(prepareTask)

    // Pack task
    val packNativeJar = project.tasks.findByName("packNativesJvm") as Jar?
        ?: project.tasks.register("packNativesJvm", Jar::class.java) {
            group = NATIVE_TASK_GROUP
            archiveAppendix.set("jvm")
            archiveClassifier.set("${platformName()}-$libArch")

            project.dependencies.add(LOCAL_RUN_CONFIGURATION, project.files(this@register))
        }.get()

    packNativeJar.dependsOn(compileTask)
    packNativeJar.from(targetLibFile)

    extension.jvmNativesJarTask = packNativeJar
}

private abstract class PrepareNativesJvm: DefaultTask() {
    @get:Input abstract var context: NativeModuleContext

    @get:Input abstract var expectActual: Boolean

    @get:Input abstract var nativesBuildSourcesDir: String
    @get:Input abstract var nativesBuildOutDir: String

    @get:Input abstract var kotlinFile: String

    @TaskAction
    fun action() {
        val extension = context.extension as NativeKtJvmInterface
        val useJni = extension.useJNI

        val moduleName = context.moduleName
        val buildSystem = context.buildSystem

        val nativesBuildSourcesDir = File(nativesBuildSourcesDir).fresh()
        val nativesBuildOutDir = File(nativesBuildOutDir)
        val projectDir = File(context.module.projectDir.absolutePath)

        // Generate all files

        KotlinJvmPrinter(
            context = context,
            target = File(kotlinFile),
            expectActual = expectActual
        )

        if(useJni) {
            val jniSourcesDir = File(nativesBuildSourcesDir, "jni")
            jniSourcesDir.mkdirs()

            CJniPrinter(
                context = context,
                target = File(jniSourcesDir, "impl.c"),
                headerTarget = File(jniSourcesDir, "impl.h"),
                isAndroid = false
            )

            CApiHeaderPrinter(
                context = context,
                target = File(jniSourcesDir, "api.h"),
                language = null,
                isInternal = true,
            )

            // unpack jni headers
            val includeDir = File(jniSourcesDir, "include")
            if(!includeDir.exists()) {
                JNI_INCLUDE_FILES.forEach { (name, path) ->
                    this::class.java.getResourceAsStream(path).use { ins ->
                        if(ins == null)
                            throw NullPointerException("Can not find header: $name")
                        val file = File(includeDir, name)
                        file.parentFile.mkdirs()
                        file.outputStream().use { ins.copyTo(it) }
                    }
                }
            }
        }

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
                val platformBuildDir = File(nativesBuildOutDir, "${platformName()}${libArch(extension.useUniversalMacOSLib).capitalized()}")

                File(nativesBuildSourcesDir, "CMakeLists.txt").writeText($$"""
                    cmake_minimum_required(VERSION 3.15)
            
                    project("$$moduleName"$${if(buildSystem.language == Language.CPP) " LANGUAGES CXX" else ""})
                    
                    $${if (buildSystem.language == Language.CPP) "set(CMAKE_CXX_STANDARD 17)" else ""}
                    $${if (buildSystem.language == Language.CPP) "set(CMAKE_CXX_STANDARD_REQUIRED ON)" else ""}
                    
                    if(CMAKE_C_COMPILER)
                        set(DUMMY ${CMAKE_C_COMPILER})
                    endif()
                    
                    set(CMAKE_POSITION_INDEPENDENT_CODE ON)
                    add_compile_options(-Wno-initializer-overrides)
                    
                    add_subdirectory("$${projectDir.posixPath}" "$${File(platformBuildDir, "sub").posixPath}")
                    
                    add_library(lib_$$moduleName SHARED api.$${context.language.sourceExtension})
                    
                    target_link_libraries(lib_$$moduleName PRIVATE $$moduleName)
                    
                    $${if(!useJni) "" else "target_link_libraries(lib_$moduleName PRIVATE ${wholeArchive($$"${JNI_LIBRARY}")})" }
                """.trimIndent())
            }
            is BuildSystem.Cargo -> Unit
        }
    }
}

private abstract class CompileNativesJvm @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var context: NativeModuleContext

    @get:Input abstract var nativesBuildSourcesDir: String
    @get:Input abstract var nativesBuildOutDir: String

    @get:Input abstract var libOutFileName: String
    @get:Input abstract var targetLibFile: String

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        val extension = context.extension as NativeKtJvmInterface
        val useJni = extension.useJNI

        val nativesBuildSourcesDir = File(nativesBuildSourcesDir)
        val nativesBuildOutDir = File(nativesBuildOutDir)
        val projectDir = File(context.module.projectDir.absolutePath)

        val platformBuildDir = File(nativesBuildOutDir, "${platformName()}${libArch(extension.useUniversalMacOSLib).capitalized()}")
        platformBuildDir.mkdirs()

        // Compile JNI if needed
        var jniLib: File? = null
        if(useJni) {
            val jniSourcesDir = File(nativesBuildSourcesDir, "jni")

            jniLib = clangCompile(execOps, context,
                sources = listOf(File(jniSourcesDir, "impl.c").posixPath),
                includeDirs = listOf(
                    File(jniSourcesDir, "include").posixPath,
                    File(jniSourcesDir, "include/${jdkPlatformName()}").posixPath
                ),
                linkerArgs = listOf("-O3"),
                dynamicLib = false,
                outputBaseName = "libjni",
                workingDir = platformBuildDir
            )
        }

        val libFile = when(val buildSystem = context.buildSystem) {
            is BuildSystem.CMake -> {

                // Generate CMake build
                val args = LinkedHashSet(buildSystem.args)
                args += setOf(
                    "-DCMAKE_C_COMPILER=clang",
                    "-DCMAKE_CXX_COMPILER=clang++"
                )
                if(jniLib != null)
                    args += "-DJNI_LIBRARY=${jniLib.posixPath}"
                if (OS.current == OS.MACOS && extension.useUniversalMacOSLib) {
                    args += setOf(
                        "-DCMAKE_C_FLAGS=\"-arch x86_64 -arch arm64\"",
                        "-DCMAKE_CXX_FLAGS=\"-arch x86_64 -arch arm64\""
                    )
                }
                cmakeGen(execOps, context,
                    dir = nativesBuildSourcesDir,
                    buildDir = platformBuildDir,
                    buildType = buildSystem.buildType,
                    args = args
                )

                // Build
                cmakeBuild(execOps, context, platformBuildDir)

                platformBuildDir.listFiles()!!.first {
                    it.name == libOutFileName
                }
            }

            is BuildSystem.Cargo -> {
                val rustBuildDir = cargoBuild(execOps, context,
                    project = projectDir,
                    buildType = buildSystem.buildType,
                    buildDir = platformBuildDir
                )

                if (useJni) {
                    val rustLinkerFlags = cargoLinkerFlags(execOps, context,
                        project = projectDir,
                        buildType = buildSystem.buildType,
                        buildDir = platformBuildDir
                    )

                    clangCompile(execOps, context,
                        sources = emptyList(),
                        includeDirs = emptyList(),
                        linkerArgs = buildList {
                            addAll(rustLinkerFlags)
                            add("$rustBuildDir/lib${context.moduleName}.a")
                            add(wholeArchive(File(platformBuildDir, "libjni.a").posixPath))
                            if (OS.current == OS.WINDOWS)
                                add("-Wl,--export-all-symbols")
                        },
                        dynamicLib = true,
                        workingDir = nativesBuildOutDir
                    )
                } else
                    File(rustBuildDir, "lib${context.moduleName}.${systemExtension(true)}")
            }
        }

        // Copy library to resources
        libFile.copyTo(File(targetLibFile), overwrite = true)
    }
}