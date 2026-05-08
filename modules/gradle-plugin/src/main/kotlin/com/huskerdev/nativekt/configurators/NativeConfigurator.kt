package com.huskerdev.nativekt.configurators

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.plugin.BuildSystem
import com.huskerdev.nativekt.plugin.CMakeBuildType
import com.huskerdev.nativekt.plugin.NATIVE_TASK_GROUP
import com.huskerdev.nativekt.plugin.NativeKtNativeInterface
import com.huskerdev.nativekt.plugin.NativeProject
import com.huskerdev.nativekt.printers.HeaderPrinter
import com.huskerdev.nativekt.printers.kn.DefPrinter
import com.huskerdev.nativekt.printers.kn.KotlinNativePrinter
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.IdlResolver
import kotlinx.serialization.json.Json
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.the
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCacheApi
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.File
import javax.inject.Inject

@OptIn(KotlinNativeCacheApi::class)
internal fun configureNative(
    project: Project,
    extension: NativeKtNativeInterface,
    commonTask: TaskProvider<*>?,
    idl: IdlResolver,
    module: NativeProject,
    sourceSet: KotlinSourceSet,
    targetType: TargetType,
    srcGenDir: File,
    nativesBuildDir: File,
    expectActual: Boolean
) {
    val targetName = targetType.kotlinTarget

    if(targetName !in currentTargetType().compiles)
        return

    val kotlin = project.the<KotlinMultiplatformExtension>()

    val nativesBuildDir = File(nativesBuildDir, "native/$targetName")

    // src paths
    val srcDir = File(srcGenDir, "native/$targetName/src")
    val cinteropDir = File(srcGenDir, "native/$targetName/cinterop")

    val kotlinFile = srcDir
        .resolve(module.classPath.replace(".", "/"))
        .resolve("${module.name}.native.kt")

    val defFile = File(cinteropDir, "cinterop.def")
    val headerFile = File(cinteropDir, "header.h")

    sourceSet.kotlin.srcDir(srcDir)

    // Configure Kotlin cinterop
    val target = kotlin.targets.findByName(targetName) as? KotlinNativeTarget
        ?: throw UnsupportedOperationException()

    val compilation = target.compilations.findByName("main")
        ?: throw UnsupportedOperationException()

    // Prepare task

    val prepareTask = project.tasks.register(
        "prepareNatives${module.name.capitalized()}${targetName.capitalized()}",
        PrepareNativesKn::class.java
    )
    prepareTask.get().also {
        it.defFile.set(defFile)
        it.inputs.dir(module.dir(project))
        it.outputs.dirs(srcDir, nativesBuildDir)

        it.idl              = Json.encodeToString(idl)

        it.moduleName       = module.name
        it.moduleClasspath  = module.classPath

        it.useCoroutines    = extension.useCoroutines
        it.expectActual     = expectActual

        it.targetType       = targetType
        it.headerFile       = headerFile.absolutePath
        it.kotlinFile       = kotlinFile.absolutePath

        it.projectDir       = module.dir(project).absolutePath.replace("\\", "/")
        it.nativesBuildDir  = nativesBuildDir.absolutePath

        it.buildSystem      = module.buildSystem
    }
    if(commonTask != null)
        prepareTask.dependsOn(commonTask)

    // Init cmake only when compiling project
    project.gradle.taskGraph.whenReady {
        if (hasTask("${project.path}:compileKotlin${targetName.capitalized()}"))
            prepareTask.get().shouldInit = true
    }

    // Add cinterop
    compilation.cinterops {
        create("nativekt${module.name.capitalized()}").definitionFile.set(prepareTask.flatMap { it.defFile })
    }

    // Compilation task

    val compilationTask = project.tasks.register(
        "compileNatives${module.name.capitalized()}Kn${targetName.capitalized()}",
        CompileNativesKn::class.java
    )
    compilationTask.get().also {
        it.inputs.dir(module.dir(project))
        it.outputs.dirs(nativesBuildDir)

        it.nativesBuildDir  = nativesBuildDir.absolutePath
        it.buildSystem      = module.buildSystem
    }
    compilationTask.dependsOn(prepareTask)

    project.tasks.matching { it.name == "compileKotlin${targetName.capitalized()}" }.forEach {
        it.dependsOn(compilationTask)
    }
    project.tasks.matching { it.name == "${targetName}SourcesJar" }.forEach {
        it.dependsOn(compilationTask)
    }
}

private fun extractLinkerOpts(
    cmakeBuildDir: File,
    moduleName: String
): List<String> = buildList {
    // Tip: arguments generates only with executable or shared libraries, so our CMakeLists.txt contains `SHARED` target

    this += cmakeBuildDir.resolve("liblibstatic_$moduleName.a")
        .absolutePath.replace("\\", "/")

    val linkLibs = File(
        cmakeBuildDir,
        "CMakeFiles/lib_$moduleName.dir/linkLibs.rsp"
    )
    val link = File(
        cmakeBuildDir,
        "CMakeFiles/lib_$moduleName.dir/link.txt"
    )
    val cmakeCache = File(
        cmakeBuildDir,
        "CMakeCache.txt"
    )

    // Collect linker flags from 'linkLibs.rsp' or 'link.txt'

    if(linkLibs.exists()) {
        this += linkLibs.readText()
            .splitRespectingQuotes()
            .map {
                if(!it.startsWith("-l") && !File(it).isAbsolute)
                    File(cmakeBuildDir, it).absolutePath.replace("\\", "/")
                else it
            }
            .filter { it !in setOf("-lpthread") }
    } else if(link.exists()) {
        val parts = link.readText()
            .splitRespectingQuotes()

        var i = 0
        while(i < parts.size) {
            val part = parts[i]
            if(part.endsWith(".a")) {
                val path = if(!File(part).isAbsolute)
                    File(cmakeBuildDir, part).absolutePath
                else part
                this += path
            }
            if(part == "-framework") {
                this += part
                this += parts[++i]
            }
            i++
        }
    }

    // Try to resolve libs from 'PkgConfig'

    val cmakeCacheText = cmakeCache.readLines()

    if(cmakeCacheText.any { "_STATIC_LDFLAGS:INTERNAL=" in it && !it.endsWith("=") }) {
        this += cmakeCacheText
            .filter { "_STATIC_LDFLAGS:INTERNAL=" in it }
            .flatMap { it.split("_STATIC_LDFLAGS:INTERNAL=")[1].split(";") }
            .toSet().sorted()
        return@buildList
    }

    if(cmakeCacheText.any { "_STATIC_LIBRARY_DIRS:INTERNAL=" in it }) {

        val libDirs = cmakeCacheText
            .filter { "_STATIC_LIBRARY_DIRS:INTERNAL=" in it }
            .flatMap { it.split("_STATIC_LIBRARY_DIRS:INTERNAL=")[1].split(";") }
            .toSet().sorted()

        val libNames = cmakeCacheText.asSequence()
            .filter { "STATIC_LIBRARIES:INTERNAL=" in it }
            .flatMap { it.split("STATIC_LIBRARIES:INTERNAL=")[1].split(";") }
            .toSet().sorted()
            .toMutableList()

        if(Os.isFamily(Os.FAMILY_WINDOWS))
            libNames += "mingwex"

        libNames.forEach { lib ->
            this.remove("-l$lib")

            libDirs.forEach { dir ->
                val file = File(dir, "lib${lib}.a")
                if(file.exists())
                    this += file.absolutePath.replace("\\", "/")
            }
        }
    }
}

private fun configureCMake(
    execOps: ExecOperations,
    targetType: TargetType,
    cmakeArgs: LinkedHashSet<String>,
    cmakeDir: File,
    cmakeBuildDir: File,
    cmakeBuildType: CMakeBuildType
) {
    fun flags(vararg flags: String) = setOf(
        "-DCMAKE_C_FLAGS=\"${flags.joinToString(" ")}\"",
        "-DCMAKE_CXX_FLAGS=\"${flags.joinToString(" ")}\""
    )
    fun xcSdkVersion(sdk: String) =
        execOps.exec("xcrun --sdk $sdk --show-sdk-platform-version", silent = true)
    fun xcSdkSysroot(sdk: String) =
        execOps.exec("xcrun --sdk $sdk --show-sdk-path", silent = true)


    val args = LinkedHashSet(cmakeArgs)
    args += linkedSetOf(
        "-DCMAKE_C_COMPILER=clang",
        "-DCMAKE_CXX_COMPILER=clang++",
    )
    args += when(targetType) {
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
    cmakeGen(execOps,
        dir = cmakeDir,
        buildDir = cmakeBuildDir,
        buildType = cmakeBuildType,
        args = args
    )
}

private abstract class PrepareNativesKn @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:OutputFile
    abstract val defFile: RegularFileProperty

    @get:Input abstract var shouldInit: Boolean
    @get:Input abstract var idl: String

    @get:Input abstract var moduleName: String
    @get:Input abstract var moduleClasspath: String

    @get:Input abstract var useCoroutines: Boolean
    @get:Input abstract var expectActual: Boolean

    @get:Input abstract var targetType: TargetType
    @get:Input abstract var headerFile: String
    @get:Input abstract var kotlinFile: String

    @get:Input abstract var projectDir: String
    @get:Input abstract var nativesBuildDir: String

    @get:Input abstract var buildSystem: BuildSystem

    init {
        doLast {
            val idl = Json.decodeFromString<IdlResolver>(idl)

            val headerFile = File(headerFile)
            headerFile.parentFile.mkdirs()

            val linkerOpts = arrayListOf<String>()

            // Generate header
            HeaderPrinter(
                idl = idl,
                target = headerFile,
                guardName = moduleName.uppercase(),
            )

            when(val buildSystem = buildSystem) {
                is BuildSystem.CMake -> {
                    val buildDir = File(nativesBuildDir, "build")

                    // Create CMake file
                    File(nativesBuildDir, "CMakeLists.txt").writeText($$"""
                        cmake_minimum_required(VERSION 3.15)
                
                        project("$$moduleName")
                
                        add_subdirectory("$$projectDir" "$${
                            File(buildDir, "common").absolutePath.replace("\\", "/")
                        }")
                        
                        add_library(lib_$$moduleName SHARED stub.c)
                        target_link_libraries(lib_$$moduleName PUBLIC $$moduleName)
                        
                        add_library(libstatic_$$moduleName STATIC stub.c)
                        target_link_libraries(libstatic_$$moduleName PUBLIC $$moduleName)
                    """.trimIndent())

                    File(nativesBuildDir, "stub.c").writeText("")

                    // Configure CMake (if needed)
                    if(shouldInit) {
                        configureCMake(
                            execOps, targetType,
                            cmakeArgs = LinkedHashSet(buildSystem.args),
                            cmakeDir = File(nativesBuildDir),
                            cmakeBuildDir = buildDir,
                            cmakeBuildType = buildSystem.buildType
                        )
                    }

                    // Get linker opts
                    linkerOpts += if(shouldInit)
                        extractLinkerOpts(buildDir, moduleName)
                    else emptyList()
                }
                is BuildSystem.Cargo -> {

                }
            }

            // Create .def file
            DefPrinter(
                target = defFile.get().asFile,
                headerFile = headerFile,
                classPath = moduleClasspath,
                linkerOpts = linkerOpts
            )

            // Generate Kotlin files
            KotlinNativePrinter(
                idl = idl,
                target = File(kotlinFile),
                classPath = moduleClasspath,
                moduleName = moduleName,
                is32Bit = targetType in setOf(TargetType.WATCHOS_ARM32, TargetType.WATCHOS_ARM64),
                useCoroutines = useCoroutines,
                expectActual = expectActual
            )
        }
    }
}

private abstract class CompileNativesKn @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var nativesBuildDir: String
    @get:Input abstract var buildSystem: BuildSystem

    init {
        group = NATIVE_TASK_GROUP
        doLast {
            when(buildSystem) {
                is BuildSystem.CMake -> {
                    cmakeBuild(execOps, File(nativesBuildDir, "build"))
                }
                is BuildSystem.Cargo -> {

                }
            }
        }
    }
}

private fun String.splitRespectingQuotes(): List<String> =
    """[^\s"']+|"([^"]*)"|'([^']*)'""".toRegex()
        .findAll(this)
        .map { it.value.trim('"', '\'') }
        .toList()
