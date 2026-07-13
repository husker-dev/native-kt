package com.huskerdev.nativekt.configurators

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.plugin.BuildSystem
import com.huskerdev.nativekt.plugin.NATIVE_TASK_GROUP
import com.huskerdev.nativekt.plugin.NativeKtNativeInterface
import com.huskerdev.nativekt.plugin.NativeProject
import com.huskerdev.nativekt.printers.DefPrinter
import com.huskerdev.nativekt.printers.c.CApiHeaderPrinter
import com.huskerdev.nativekt.printers.c.CApiImplPrinter
import com.huskerdev.nativekt.printers.kotlin.KotlinNativePrinter
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.IdlResolver
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.the
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCacheApi
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
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

    val nativesBuildSourcesDir = File(nativesBuildDir, "native/$targetName/sources")
    val nativesBuildOutDir = File(nativesBuildDir, "native/$targetName/out")

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
        "prepareNatives${module.name.capitalized()}Kn${targetName.capitalized()}",
        PrepareNativesKn::class.java
    )
    prepareTask.get().also {
        it.defFile.set(defFile)
        it.inputs.dir(module.dir(project))
        it.inputs.file(module.getNDLFile(project))
        it.outputs.dirs(nativesBuildSourcesDir)

        it.idl                    = Json.encodeToString(idl)

        it.moduleName             = module.name
        it.moduleClasspath        = module.classPath

        it.useCoroutines          = extension.useCoroutines
        it.expectActual           = expectActual

        it.targetType             = targetType
        it.headerFile             = headerFile.absolutePath
        it.kotlinFile             = kotlinFile.absolutePath

        it.projectDir             = module.dir(project).posixPath
        it.nativesBuildSourcesDir = nativesBuildSourcesDir.absolutePath
        it.nativesBuildOutDir     = nativesBuildOutDir.absolutePath

        it.buildSystem            = module.buildSystem
    }
    if(commonTask != null)
        prepareTask.dependsOn(commonTask)

    project.tasks.matching { it.name == "downloadKotlinNativeDistribution" }.forEach {
        prepareTask.get().dependsOn(it)
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
        it.inputs.file(module.getNDLFile(project))
        it.outputs.dirs(nativesBuildOutDir)

        it.idl                    = Json.encodeToString(idl)

        it.moduleName             = module.name
        it.moduleClasspath        = module.classPath
        it.targetType             = targetType

        it.projectDir             = module.dir(project).posixPath
        it.nativesBuildSourcesDir = nativesBuildSourcesDir.absolutePath
        it.nativesBuildOutDir     = nativesBuildOutDir.absolutePath

        it.buildSystem            = module.buildSystem
    }
    compilationTask.dependsOn(prepareTask)

    // Depends compilation on Kotlin source-generator
    project.tasks.matching { it.name == "compileKotlin${targetName.capitalized()}" }.forEach {
        it.dependsOn(compilationTask)
    }
    project.tasks.matching { it.name == "${targetName}SourcesJar" }.forEach {
        it.dependsOn(compilationTask)
    }

    // Force Kotlin re-linking when native files are changed
    project.tasks.matching { it is KotlinNativeLink && it.project == project }.forEach {
        it.inputs.dir(module.dir(project))
        it.inputs.file(module.getNDLFile(project))
    }

    // Compile natives only when compiling project (to prevent compilation on Gradle reload)
    project.gradle.taskGraph.whenReady {
        if (hasTask(compilationTask.get()))
            prepareTask.get().shouldInit = true
    }
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
    @get:Input abstract var nativesBuildSourcesDir: String
    @get:Input abstract var nativesBuildOutDir: String

    @get:Input abstract var buildSystem: BuildSystem

    @TaskAction
    fun action() {
        val idl = Json.decodeFromString<IdlResolver>(idl)

        val nativesBuildSourcesDir = File(nativesBuildSourcesDir).fresh()
        val nativesBuildOutDir = File(nativesBuildOutDir)

        val sourceExtension = buildSystem.language.sourceExtension ?: "c"
        val headerExtension = buildSystem.language.headerExtension ?: "h"

        val headerFile = File(headerFile)
        headerFile.parentFile.mkdirs()

        val linkerOpts = arrayListOf<String>()

        // Generate header
        CApiHeaderPrinter(
            idl = idl,
            target = headerFile,
            language = null,
            classPath = moduleClasspath,
            moduleName = moduleName,
            isInternal = true,
        )

        // Generate api sources
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

        when(val buildSystem = buildSystem) {
            is BuildSystem.CMake -> {

                // Create CMake file
                File(nativesBuildSourcesDir, "CMakeLists.txt").writeText($$"""
                    cmake_minimum_required(VERSION 3.15)
            
                    project("$$moduleName")
                    
                    set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY $${nativesBuildOutDir.posixPath})
                    set(CMAKE_LIBRARY_OUTPUT_DIRECTORY $${nativesBuildOutDir.posixPath})
                    set(CMAKE_RUNTIME_OUTPUT_DIRECTORY $${nativesBuildOutDir.posixPath})
                    
                    set(CMAKE_POSITION_INDEPENDENT_CODE ON)
                    
                    add_subdirectory("$$projectDir" "$${File(nativesBuildOutDir, "common").posixPath}")
                        
                    add_library(lib_$$moduleName SHARED api.$$sourceExtension)
                    target_link_libraries(lib_$$moduleName PUBLIC $$moduleName)
                    
                    add_library(libstatic_$$moduleName STATIC api.$$sourceExtension)
                    target_link_libraries(libstatic_$$moduleName PUBLIC $$moduleName)
                """.trimIndent())

                // Configure CMake (if needed)
                if(shouldInit) {
                    configureCMake(
                        execOps, targetType,
                        cmakeArgs = LinkedHashSet(buildSystem.args),
                        cmakeDir = nativesBuildSourcesDir,
                        cmakeBuildDir = File(nativesBuildSourcesDir, "cmake"),
                        cmakeBuildType = buildSystem.buildType
                    )

                    // Get linker opts
                    linkerOpts += extractLinkerOpts(execOps, File(nativesBuildSourcesDir, "cmake"), moduleName)

                    linkerOpts += nativesBuildOutDir.resolve("liblibstatic_$moduleName.a").posixPath
                }
            }
            is BuildSystem.Cargo -> {
                if(shouldInit) {
                    val rustFlags = cargoLinkerFlags(execOps,
                        project = File(projectDir),
                        buildType = buildSystem.buildType,
                        buildDir = nativesBuildOutDir,
                        target = getCargoTarget(targetType)
                    )
                    val rustBuildDir = cargoTargetDir(
                        buildDir = nativesBuildOutDir,
                        buildType = buildSystem.buildType,
                        target = getCargoTarget(targetType)
                    )

                    linkerOpts += listOf(
                        *rustFlags.toTypedArray(),
                        File(rustBuildDir, "lib$moduleName.a").posixPath,
                        File(nativesBuildOutDir, "libnativekt.a").posixPath,
                    )
                }
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
            language = buildSystem.language,
            classPath = moduleClasspath,
            moduleName = moduleName,
            useCoroutines = useCoroutines,
            expectActual = expectActual
        )
    }
}

private abstract class CompileNativesKn @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var idl: String
    @get:Input abstract var moduleName: String
    @get:Input abstract var moduleClasspath: String

    @get:Input abstract var targetType: TargetType

    @get:Input abstract var projectDir: String
    @get:Input abstract var nativesBuildSourcesDir: String
    @get:Input abstract var nativesBuildOutDir: String

    @get:Input abstract var buildSystem: BuildSystem

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        val idl = Json.decodeFromString<IdlResolver>(idl)

        val nativesBuildSourcesDir = File(nativesBuildSourcesDir)
        val nativesBuildOutDir = File(nativesBuildOutDir)

        when(val buildSystem = buildSystem) {
            is BuildSystem.CMake -> {
                cmakeBuild(execOps, File(nativesBuildSourcesDir, "cmake"))

                prepareNativeLibraryForKN(execOps,
                    nativesBuildOutDir.parentFile,
                    File(nativesBuildOutDir, "liblibstatic_$moduleName.a"),
                    symbols = idl.globalOperators().map { it.name.snakeCase() },
                    initSymbolName = mangle(moduleClasspath, moduleName, "_init"),
                    targetArgs = getClangTargetArgs(execOps, targetType)
                )
            }
            is BuildSystem.Cargo -> {
                cargoBuild(execOps,
                    project = File(projectDir),
                    buildType = buildSystem.buildType,
                    buildDir = nativesBuildOutDir,
                    target = getCargoTarget(targetType)
                )
                clangCompile(
                    execOps,
                    sources = listOf("api.c").map { nativesBuildSourcesDir.resolve(it).posixPath },
                    linkerArgs = getClangTargetArgs(execOps, targetType),
                    dynamicLib = false,
                    workingDir = nativesBuildOutDir,
                    outputBaseName = "libnativekt"
                )
            }
        }
    }
}