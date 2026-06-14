package com.huskerdev.nativekt.configurators

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.plugin.BuildSystem
import com.huskerdev.nativekt.plugin.CMakeBuildType
import com.huskerdev.nativekt.plugin.NATIVE_TASK_GROUP
import com.huskerdev.nativekt.plugin.NativeKtNativeInterface
import com.huskerdev.nativekt.plugin.NativeProject
import com.huskerdev.nativekt.printers.c.CApiHeaderPrinter
import com.huskerdev.nativekt.printers.DefPrinter
import com.huskerdev.nativekt.printers.c.CApiImplPrinter
import com.huskerdev.nativekt.printers.kotlin.KotlinNativePrinter
import com.huskerdev.nativekt.utils.*
import com.huskerdev.nativekt.utils.posixPath
import com.huskerdev.webidl.resolver.IdlResolver
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
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

        it.projectDir       = module.dir(project).posixPath
        it.nativesBuildDir  = nativesBuildDir.absolutePath

        it.buildSystem      = module.buildSystem
    }
    if(commonTask != null)
        prepareTask.dependsOn(commonTask)

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

        it.moduleName       = module.name

        it.targetType       = targetType

        it.projectDir       = module.dir(project).posixPath
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

    // Init cmake only when compiling project
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
    @get:OutputDirectory abstract var nativesBuildDir: String

    @get:Input abstract var buildSystem: BuildSystem

    @TaskAction
    fun action() {
        val idl = Json.decodeFromString<IdlResolver>(idl)

        val headerFile = File(headerFile)
        headerFile.parentFile.mkdirs()

        val linkerOpts = arrayListOf<String>()

        // Generate header
        CApiHeaderPrinter(
            idl = idl,
            target = headerFile,
            guardName = moduleName.uppercase(),
        )

        // Generate api sources
        val buildDir = File(nativesBuildDir, "build")

        CApiHeaderPrinter(
            idl = idl,
            target = File(buildDir, "api.h"),
            isInternal = true
        )

        CApiImplPrinter(
            idl = idl,
            target = File(buildDir, "api.c"),
            classPath = moduleClasspath
        )

        when(val buildSystem = buildSystem) {
            is BuildSystem.CMake -> {
                // Create CMake file
                File(nativesBuildDir, "CMakeLists.txt").writeText($$"""
                    cmake_minimum_required(VERSION 3.15)
            
                    project("$$moduleName")
            
                    add_subdirectory("$$projectDir" "$${
                        File(buildDir, "common").posixPath
                    }")
                        
                    add_library(lib_$$moduleName SHARED api.c)
                    target_link_libraries(lib_$$moduleName PUBLIC $$moduleName)
                    
                    add_library(libstatic_$$moduleName STATIC api.c)
                    target_link_libraries(libstatic_$$moduleName PUBLIC $$moduleName)
                """.trimIndent())

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
                    extractLinkerOpts(execOps, buildDir, moduleName)
                else emptyList()
            }
            is BuildSystem.Cargo -> {
                if(shouldInit) {
                    val rustFlags = cargoLinkerFlags(execOps,
                        project = File(projectDir),
                        buildType = buildSystem.buildType,
                        buildDir = buildDir,
                        target = getCargoTarget(targetType)
                    )
                    val rustBuildDir = cargoTargetDir(
                        buildDir = buildDir,
                        buildType = buildSystem.buildType,
                        target = getCargoTarget(targetType)
                    )

                    val buildDir = buildDir.posixPath

                    linkerOpts += listOf(
                        *rustFlags.toTypedArray(),
                        File(rustBuildDir, "lib$moduleName.a").posixPath,
                        File(buildDir, "libnativekt.a").posixPath,
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
    @get:Input abstract var moduleName: String

    @get:Input abstract var targetType: TargetType

    @get:Input abstract var projectDir: String
    @get:Input abstract var nativesBuildDir: String
    @get:Input abstract var buildSystem: BuildSystem

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        val buildDir = File(nativesBuildDir, "build")

        when(val buildSystem = buildSystem) {
            is BuildSystem.CMake -> {
                cmakeBuild(execOps, buildDir)
            }
            is BuildSystem.Cargo -> {
                cargoBuild(execOps,
                    project = File(projectDir),
                    buildType = buildSystem.buildType,
                    buildDir = buildDir,
                    target = getCargoTarget(targetType)
                )
                clangCompile(
                    execOps,
                    sources = listOf("api.c"),
                    linkerArgs = getClangTargetArgs(execOps, targetType),
                    dynamicLib = false,
                    workingDir = buildDir,
                    outputBaseName = "libnativekt"
                )
            }
        }
    }
}