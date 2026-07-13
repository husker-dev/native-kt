package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.plugin.BuildSystem
import com.huskerdev.nativekt.plugin.Language
import com.huskerdev.nativekt.plugin.NATIVE_TASK_GROUP
import com.huskerdev.nativekt.plugin.NativeKtJvmInterface
import com.huskerdev.nativekt.plugin.NativeProject
import com.huskerdev.nativekt.printers.c.CApiHeaderPrinter
import com.huskerdev.nativekt.printers.c.CApiImplPrinter
import com.huskerdev.nativekt.printers.c.CJniPrinter
import com.huskerdev.nativekt.printers.c.CJniUtilsPrinter
import com.huskerdev.nativekt.printers.kotlin.KotlinJvmPrinter
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.IdlResolver
import kotlinx.serialization.json.Json
import org.apache.tools.ant.taskdefs.condition.Os
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

private fun platformName() = when {
    Os.isFamily(Os.FAMILY_WINDOWS) -> "windows"
    Os.isFamily(Os.FAMILY_MAC) -> "macos"
    Os.isFamily(Os.FAMILY_UNIX) -> "linux"
    else -> throw UnsupportedOperationException()
}

private fun jdkPlatformName() = when {
    Os.isFamily(Os.FAMILY_WINDOWS) -> "win32"
    Os.isFamily(Os.FAMILY_MAC) -> "darwin"
    Os.isFamily(Os.FAMILY_UNIX) -> "linux"
    else -> throw UnsupportedOperationException()
}

private fun libArch(useUniversalMacOSLib: Boolean) = when {
    Os.isFamily(Os.FAMILY_MAC) && useUniversalMacOSLib -> "universal"
    Os.isArch("aarch64") -> "arm64"
    Os.isArch("amd64") -> "x64"
    Os.isArch("riscv") -> "riscv"
    else -> "x86"
}

internal fun configureJvm(
    project: Project,
    extension: NativeKtJvmInterface,
    commonTask: TaskProvider<*>?,
    idl: IdlResolver,
    module: NativeProject,
    sourceSet: KotlinSourceSet,
    srcGenDir: File,
    nativesBuildDir: File,
    expectActual: Boolean
) {
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
    val libOutFileName = "liblib_${module.name}.${libExtension}"
    val libFullFileName = "lib${module.name}-$libArch.${libExtension}"

    // src dirs
    val srcDir = File(srcGenDir, "jvm/src")
    val libsDir = File(srcGenDir, "jvm/libs")
    val targetLibFile = File(libsDir, libFullFileName)

    val nativesBuildSourcesDir = File(nativesBuildDir, "jvm/sources")
    val nativesBuildOutDir = File(nativesBuildDir, "jvm/out")

    val kotlinFile = srcDir
        .resolve(module.classPath.replace(".", "/"))
        .resolve("${module.name}.jvm.kt")

    // Prepare task

    val prepareTask = project.tasks.register(
        "prepareNatives${module.name.capitalized()}Jvm",
        PrepareNativesJvm::class.java
    )
    prepareTask.get().also {
        it.inputs.dir(module.dir(project))
        it.inputs.file(module.getNDLFile(project))
        it.outputs.dirs(nativesBuildSourcesDir, srcDir)

        it.idl                    = Json.encodeToString(idl)

        it.moduleName             = module.name
        it.moduleClasspath        = module.classPath

        it.useJNI                 = extension.useJNI
        it.useForeignApi          = extension.useForeignApi
        it.useJVMCI               = extension.useJVMCI
        it.useUniversalMacOSLib   = extension.useUniversalMacOSLib
        it.useCoroutines          = extension.useCoroutines
        it.expectActual           = expectActual

        it.libArch                = libArch

        it.projectDir             = module.dir(project).absolutePath
        it.nativesBuildSourcesDir = nativesBuildSourcesDir.absolutePath
        it.nativesBuildOutDir     = nativesBuildOutDir.absolutePath

        it.kotlinFile             = kotlinFile.absolutePath

        it.buildSystem            = module.buildSystem
    }
    if(commonTask != null)
        prepareTask.get().dependsOn(commonTask)
    prepareTask.dependsOnReload()

    sourceSet.kotlin.srcDirs(prepareTask.map { srcDir })

    // Compile task

    val compileTask = project.tasks.register("compileNatives${module.name.capitalized()}Jvm", CompileNativesJvm::class.java).get().also {
        it.inputs.dir(module.dir(project))
        it.inputs.file(module.getNDLFile(project))
        it.outputs.dir(nativesBuildOutDir)

        it.useJNI                 = extension.useJNI
        it.useUniversalMacOSLib   = extension.useUniversalMacOSLib

        it.moduleName             = module.name

        it.projectDir             = module.dir(project).absolutePath
        it.nativesBuildSourcesDir = nativesBuildSourcesDir.absolutePath
        it.nativesBuildOutDir     = nativesBuildOutDir.absolutePath

        it.libOutFileName         = libOutFileName
        it.targetLibFile          = targetLibFile.absolutePath

        it.libArch                = libArch

        it.buildSystem            = module.buildSystem
    }
    compileTask.dependsOn(prepareTask)

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
    @get:Input abstract var idl: String

    @get:Input abstract var moduleName: String
    @get:Input abstract var moduleClasspath: String

    @get:Input abstract var useJNI: Boolean
    @get:Input abstract var useForeignApi: Boolean
    @get:Input abstract var useJVMCI: Boolean
    @get:Input abstract var useUniversalMacOSLib: Boolean
    @get:Input abstract var useCoroutines: Boolean
    @get:Input abstract var expectActual: Boolean

    @get:Input abstract var libArch: String

    @get:Input abstract var projectDir: String
    @get:Input abstract var nativesBuildSourcesDir: String
    @get:Input abstract var nativesBuildOutDir: String

    @get:Input abstract var kotlinFile: String

    @get:Input abstract var buildSystem: BuildSystem

    @TaskAction
    fun action() {
        val idl = Json.decodeFromString<IdlResolver>(idl)

        val nativesBuildSourcesDir = File(nativesBuildSourcesDir).fresh()
        val nativesBuildOutDir = File(nativesBuildOutDir)
        val projectDir = File(projectDir)

        val sourceExtension = buildSystem.language.sourceExtension ?: "c"
        val headerExtension = buildSystem.language.headerExtension ?: "h"

        // Generate all files

        KotlinJvmPrinter(
            idl = idl,
            target = File(kotlinFile),
            classPath = moduleClasspath,
            moduleName = moduleName,
            useCoroutines = useCoroutines,
            expectActual = expectActual,
            useJNI = useJNI,
            useForeignApi = useForeignApi,
            useJVMCI = useJVMCI,
            useUniversalMacOSLib = useUniversalMacOSLib
        )

        if(useJNI) {
            val jniSourcesDir = File(nativesBuildSourcesDir, "jni")
            jniSourcesDir.mkdirs()

            CJniUtilsPrinter(
                idl = idl,
                target = File(jniSourcesDir, "jni_utils.h"),
                classPath = moduleClasspath,
                moduleName = moduleName,
                name = "${moduleName.capitalized()}JNI",
                isAndroid = false
            )

            CJniPrinter(
                idl = idl,
                target = File(jniSourcesDir, "jni_bindings.c"),
                classPath = moduleClasspath,
                moduleName = moduleName,
                name = "${moduleName.capitalized()}JNI",
                isAndroid = false,
                isAndroidCriticalEnabled = false
            )

            CApiHeaderPrinter(
                idl = idl,
                target = File(jniSourcesDir, "api.h"),
                language = null,
                classPath = moduleClasspath,
                moduleName = moduleName,
                isInternal = true,
            )

            // unpack jni headers
            val includeDir = File(jniSourcesDir, "include")
            if(!includeDir.exists()) {
                arrayOf(
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
                    "jvmticmlr.h",
                ).forEach { path ->
                    this::class.java.getResourceAsStream("/com/huskerdev/nativekt/include/${path}").use { ins ->
                        if(ins == null)
                            throw NullPointerException("Can not find header: $path")
                        val file = File(includeDir, path)
                        file.parentFile.mkdirs()
                        file.outputStream().use { ins.copyTo(it) }
                    }
                }
            }
        }

        CApiHeaderPrinter(
            idl = idl,
            target = File(nativesBuildSourcesDir, "api.$headerExtension"),
            language = buildSystem.language,
            classPath = moduleClasspath,
            moduleName = moduleName,
            isInternal = true,
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
                val platformBuildDir = File(nativesBuildOutDir, "${platformName()}${libArch.capitalized()}")

                File(nativesBuildSourcesDir, "CMakeLists.txt").writeText($$"""
                    cmake_minimum_required(VERSION 3.15)
            
                    project("$$moduleName"$${if(buildSystem.language == Language.CPP) " LANGUAGES CXX" else ""})
                    
                    if(CMAKE_C_COMPILER)
                        set(DUMMY ${CMAKE_C_COMPILER})
                    endif()
            
                    add_subdirectory("$${projectDir.posixPath}" "$${File(platformBuildDir, "sub").posixPath}")
            
                    add_library(lib_$$moduleName SHARED api.$$sourceExtension)
                    
                    target_link_libraries(lib_$$moduleName PRIVATE $$moduleName)
                    
                    $${if(!useJNI) "" else "target_link_libraries(lib_$moduleName PRIVATE ${wholeArchive(File(platformBuildDir, "libjni.a").posixPath)})" }
                """.trimIndent())
            }
            is BuildSystem.Cargo -> Unit
        }
    }
}

private abstract class CompileNativesJvm @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var useJNI: Boolean
    @get:Input abstract var useUniversalMacOSLib: Boolean

    @get:Input abstract var moduleName: String

    @get:Input abstract var projectDir: String
    @get:Input abstract var nativesBuildSourcesDir: String
    @get:Input abstract var nativesBuildOutDir: String

    @get:Input abstract var libOutFileName: String
    @get:Input abstract var targetLibFile: String

    @get:Input abstract var libArch: String

    @get:Input abstract var buildSystem: BuildSystem

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        val nativesBuildSourcesDir = File(nativesBuildSourcesDir)
        val nativesBuildOutDir = File(nativesBuildOutDir)

        val platformBuildDir = File(nativesBuildOutDir, "${platformName()}${libArch.capitalized()}")
        platformBuildDir.mkdirs()

        // Compile JNI if needed
        if(useJNI) {
            val jniSourcesDir = File(nativesBuildSourcesDir, "jni")

            clangCompile(execOps,
                sources = listOf(File(jniSourcesDir, "jni_bindings.c").posixPath),
                includeDirs = listOf(
                    File(jniSourcesDir, "include").posixPath,
                    File(jniSourcesDir, "include/${jdkPlatformName()}").posixPath
                ),
                linkerArgs = emptyList(),
                dynamicLib = false,
                outputBaseName = "libjni",
                workingDir = platformBuildDir
            )
        }

        when(val buildSystem = buildSystem) {
            is BuildSystem.CMake -> {

                // Generate CMake build
                val args = LinkedHashSet(buildSystem.args)
                args += setOf(
                    "-DCMAKE_C_COMPILER=clang",
                    "-DCMAKE_CXX_COMPILER=clang++"
                )
                if (Os.isFamily(Os.FAMILY_MAC) && useUniversalMacOSLib) {
                    args += setOf(
                        "-DCMAKE_C_FLAGS=\"-arch x86_64 -arch arm64\"",
                        "-DCMAKE_CXX_FLAGS=\"-arch x86_64 -arch arm64\""
                    )
                }
                cmakeGen(execOps,
                    dir = nativesBuildSourcesDir,
                    buildDir = platformBuildDir,
                    buildType = buildSystem.buildType,
                    args = args
                )

                // Build
                cmakeBuild(execOps, platformBuildDir)

                platformBuildDir.listFiles()!!.first {
                    it.name == libOutFileName
                }.copyTo(File(targetLibFile), overwrite = true)
            }

            is BuildSystem.Cargo -> {
                val rustBuildDir = cargoBuild(execOps,
                    project = File(projectDir),
                    buildType = buildSystem.buildType,
                    buildDir = platformBuildDir
                )
                val rustLinkerFlags = cargoLinkerFlags(execOps,
                    project = File(projectDir),
                    buildType = buildSystem.buildType,
                    buildDir = platformBuildDir
                )

                clangCompile(execOps,
                    sources = listOf(File(nativesBuildSourcesDir, "api.c").posixPath),
                    includeDirs = emptyList(),
                    linkerArgs = listOfNotNull(
                        *rustLinkerFlags.toTypedArray(),
                        "-L$rustBuildDir",
                        "-l$moduleName",
                        if(useJNI) wholeArchive(File(platformBuildDir, "libjni.a").posixPath) else null,
                        if(Os.isFamily(Os.FAMILY_WINDOWS)) "-Wl,--export-all-symbols" else null
                    ),
                    dynamicLib = true,
                    workingDir = nativesBuildOutDir
                ).copyTo(File(targetLibFile), overwrite = true)
            }
        }
    }
}