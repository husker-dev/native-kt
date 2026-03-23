package com.huskerdev.nativekt.configurators

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.plugin.CMakeBuildType
import com.huskerdev.nativekt.plugin.NativeKtExtension
import com.huskerdev.nativekt.plugin.NativeModule
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
    extension: NativeKtExtension,
    commonTask: TaskProvider<*>?,
    idl: IdlResolver,
    module: NativeModule,
    sourceSet: KotlinSourceSet,
    targetType: TargetType,
    srcRootDir: File,
    cmakeRootDir: File,
    expectActual: Boolean
) {
    val targetName = targetType.kotlinTarget

    if(targetName !in currentTargetType().compiles)
        return

    val kotlin = project.the<KotlinMultiplatformExtension>()

    // cmake paths
    val cmakeDir = File(cmakeRootDir, "native/$targetName")
    val cmakeBuildDir = File(cmakeDir, "build")

    // src paths
    val srcTargetDir = File(srcRootDir, "native/$targetName")

    val srcDir = File(srcTargetDir, "src")
    val cinteropDir = File(srcTargetDir, "cinterop")

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
        it.outputs.dirs(srcDir, cmakeDir)

        it.idl = Json.encodeToString(idl)
        it.useCoroutines = extension.useCoroutines
        it.expectActual = expectActual

        it.srcDir = srcDir.absolutePath

        it.cmakeBuildType = module.buildType
        it.targetType = targetType
        it.cmakeDir = cmakeDir.absolutePath
        it.cmakeBuildDir = cmakeBuildDir.absolutePath
        it.headerFile = headerFile.absolutePath
        it.moduleName = module.name
        it.moduleClasspath = module.classPath
        it.srcFile = srcDir.resolve(module.classPath.replace(".", "/")).resolve("${module.name}.native.kt").absolutePath
        it.nativeProjectDir = module.dir(project).absolutePath.replace("\\", "/")
    }
    if(commonTask != null)
        prepareTask.dependsOn(commonTask)

    project.gradle.taskGraph.whenReady {
        if (hasTask("${project.path}:compileKotlin${targetName.capitalized()}"))
            prepareTask.get().shouldInit = true
    }

    compilation.cinterops {
        create("natives_${module.name}").definitionFile.set(prepareTask.flatMap { it.defFile })
    }

    // Compilation task

    val compilationTask = project.tasks.register(
        "compileNatives${module.name.capitalized()}Kn${targetName.capitalized()}",
        CompileNativesKn::class.java
    )
    compilationTask.get().also {
        it.inputs.dir(module.dir(project))
        it.outputs.dirs(cmakeDir)

        it.cmakeBuildDir = cmakeBuildDir.absolutePath
    }
    compilationTask.dependsOn(prepareTask)

    project.tasks.matching { it.name == "compileKotlin${targetName.capitalized()}" }.forEach {
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


    val args = hashSetOf(
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
    cmakeGen(execOps, cmakeDir, cmakeBuildDir, cmakeBuildType, args)
}

private abstract class PrepareNativesKn @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:OutputFile
    abstract val defFile: RegularFileProperty

    @get:Input abstract var shouldInit: Boolean

    @get:Input abstract var idl: String
    @get:Input abstract var useCoroutines: Boolean
    @get:Input abstract var expectActual: Boolean

    @get:Input abstract var srcDir: String

    @get:Input abstract var cmakeBuildType: CMakeBuildType
    @get:Input abstract var targetType: TargetType
    @get:Input abstract var cmakeDir: String
    @get:Input abstract var cmakeBuildDir: String
    @get:Input abstract var headerFile: String
    @get:Input abstract var moduleName: String
    @get:Input abstract var moduleClasspath: String
    @get:Input abstract var srcFile: String
    @get:Input abstract var nativeProjectDir: String

    init {
        doLast {
            File(srcDir).fresh()

            val idl = Json.decodeFromString<IdlResolver>(idl)

            val cmakeBuildDir = File(cmakeBuildDir)
            val cmakeDir = File(cmakeDir)
            val headerFile = File(headerFile)

            cmakeDir.mkdirs()
            headerFile.parentFile.mkdirs()

            // Generate header
            HeaderPrinter(
                idl = idl,
                target = headerFile,
                guardName = moduleName.uppercase(),
            )

            // Create CMake file
            File(cmakeDir, "CMakeLists.txt").writeText($$"""
                cmake_minimum_required(VERSION 3.15)
        
                project("$$moduleName")
        
                add_subdirectory("$$nativeProjectDir" "$${
                File(cmakeBuildDir, "common").absolutePath.replace("\\", "/")
            }")
                
                add_library(lib_$$moduleName SHARED stub.c)
                target_link_libraries(lib_$$moduleName PUBLIC $$moduleName)
                
                add_library(libstatic_$$moduleName STATIC stub.c)
                target_link_libraries(libstatic_$$moduleName PUBLIC $$moduleName)
            """.trimIndent())

            File(cmakeDir, "stub.c").writeText("")

            // Configure CMake (if needed)
            if(shouldInit)
                configureCMake(execOps, targetType, cmakeDir, cmakeBuildDir, cmakeBuildType)

            // Get linker opts
            val linkerOpts = if(shouldInit)
                extractLinkerOpts(cmakeBuildDir, moduleName)
            else emptyList()

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
                target = File(srcFile),
                classPath = moduleClasspath,
                moduleName = moduleName,
                useCoroutines = useCoroutines,
                expectActual = expectActual
            )
        }
    }
}

private abstract class CompileNativesKn @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var cmakeBuildDir: String

    init {
        group = "native"
        doLast {
            cmakeBuild(execOps, File(cmakeBuildDir))
        }
    }
}

private fun String.splitRespectingQuotes(): List<String> =
    """[^\s"']+|"([^"]*)"|'([^']*)'""".toRegex()
        .findAll(this)
        .map { it.value.trim('"', '\'') }
        .toList()
