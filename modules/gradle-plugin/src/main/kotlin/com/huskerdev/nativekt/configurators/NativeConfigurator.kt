package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.plugin.BuildSystem
import com.huskerdev.nativekt.plugin.DebugKind
import com.huskerdev.nativekt.plugin.Language
import com.huskerdev.nativekt.plugin.NATIVE_TASK_GROUP
import com.huskerdev.nativekt.printers.DefPrinter
import com.huskerdev.nativekt.printers.c.CApiHeaderPrinter
import com.huskerdev.nativekt.printers.c.CApiImplPrinter
import com.huskerdev.nativekt.printers.cpp.CppApiHeaderPrinter
import com.huskerdev.nativekt.printers.cpp.CppApiImplPrinter
import com.huskerdev.nativekt.printers.kotlin.KotlinNativePrinter
import com.huskerdev.nativekt.utils.*
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
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File
import javax.inject.Inject

internal fun configureNative(
    project: Project,
    commonTask: TaskProvider<*>?,
    context: NativeModuleContext,
    sourceSet: KotlinSourceSet,
    targetType: TargetType,
    expectActual: Boolean
) {
    val targetName = targetType.kotlinTarget

    if(targetName !in currentTargetType().compiles)
        return

    val kotlin = project.the<KotlinMultiplatformExtension>()

    val nativesBuildSourcesDir = File(context.nativesBuildDir, "native/$targetName/sources")
    val nativesBuildOutDir = File(context.nativesBuildDir, "native/$targetName/out")

    // src paths
    val srcDir = File(context.srcGenDir, "native/$targetName/src")
    val cinteropDir = File(context.srcGenDir, "native/$targetName/cinterop")

    val kotlinFile = srcDir
        .resolve(context.classPath.replace(".", "/"))
        .resolve("${context.moduleName}.native.kt")

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
        "prepareNatives${context.moduleName.upperCamelCase()}Kn${targetName.capitalized()}",
        PrepareNativesKn::class.java
    )
    prepareTask.get().let {
        it.defFile.set(defFile)
        it.inputs.dir(context.module.dir)
        it.inputs.file(context.module.ndlFile())
        it.outputs.dirs(nativesBuildSourcesDir)

        it.context                = context
        it.expectActual           = expectActual

        it.targetType             = targetType
        it.cinteropHeaderFile     = headerFile.absolutePath
        it.kotlinFile             = kotlinFile.absolutePath

        it.nativesBuildSourcesDir = nativesBuildSourcesDir.absolutePath
        it.nativesBuildOutDir     = nativesBuildOutDir.absolutePath
    }
    if(commonTask != null)
        prepareTask.get().dependsOn(commonTask)

    project.tasks.matching { it.name == "downloadKotlinNativeDistribution" }.forEach {
        prepareTask.get().dependsOn(it)
    }

    // Add cinterop
    compilation.cinterops {
        create("nativekt${context.moduleName.upperCamelCase()}").definitionFile.set(prepareTask.flatMap { it.defFile })
    }

    // Compilation task

    val compilationTask = project.tasks.register(
        "compileNatives${context.moduleName.upperCamelCase()}Kn${targetName.capitalized()}",
        CompileNativesKn::class.java
    )
    compilationTask.get().let {
        it.inputs.dir(context.module.dir)
        it.inputs.file(context.module.ndlFile())
        it.outputs.dirs(nativesBuildOutDir)

        it.context                = context
        it.targetType             = targetType

        it.nativesBuildSourcesDir = nativesBuildSourcesDir.absolutePath
        it.nativesBuildOutDir     = nativesBuildOutDir.absolutePath
    }
    compilationTask.get().dependsOn(prepareTask)

    // Depends compilation on Kotlin source-generator
    project.tasks.matching { it.name == "compileKotlin${targetName.capitalized()}" }.forEach {
        it.dependsOn(compilationTask)
    }
    project.tasks.matching { it.name == "${targetName}SourcesJar" }.forEach {
        it.dependsOn(compilationTask)
    }

    // Force Kotlin re-linking when native files are changed
    project.tasks.matching { it is KotlinNativeLink && it.project == project }.forEach {
        it.inputs.dir(context.module.dir)
        it.inputs.file(context.module.ndlFile())
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

    @get:Input abstract var context: NativeModuleContext

    @get:Input abstract var shouldInit: Boolean
    @get:Input abstract var expectActual: Boolean

    @get:Input abstract var targetType: TargetType
    @get:Input abstract var cinteropHeaderFile: String
    @get:Input abstract var kotlinFile: String

    @get:Input abstract var nativesBuildSourcesDir: String
    @get:Input abstract var nativesBuildOutDir: String

    @TaskAction
    fun action() {

        val moduleName = context.moduleName
        val nativesBuildSourcesDir = File(nativesBuildSourcesDir).fresh()
        val nativesBuildOutDir = File(nativesBuildOutDir)

        val cinteropHeaderFile = File(this@PrepareNativesKn.cinteropHeaderFile)
        cinteropHeaderFile.parentFile.mkdirs()

        val linkerOpts = arrayListOf<String>()

        // Generate header
        CApiHeaderPrinter(
            context = context,
            target = cinteropHeaderFile,
            language = null,
            isInternal = true,
        )

        // Generate api sources if needed
        fun createCApi() {
            when (context.buildSystem.language) {
                Language.C -> {
                    CApiHeaderPrinter(
                        context = context,
                        target = File(nativesBuildSourcesDir, "api.h"),
                        isInternal = true
                    )
                    CApiImplPrinter(
                        context = context,
                        target = File(nativesBuildSourcesDir, "api.c"),
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
                        target = File(nativesBuildSourcesDir, "api.cpp"),
                    )
                }
                else -> Unit
            }
        }

        when(val buildSystem = context.buildSystem) {
            is BuildSystem.CMake -> {

                // Create C/C++ Api
                createCApi()
                val sourceExtension = buildSystem.language.sourceExtension ?: "c"

                // Mangle C non-static functions
                val funcRedefines = if(buildSystem.language == Language.C && context.allOperations.isNotEmpty()) {
                    val symbolDefines = context.allOperations
                        .joinToString(" ") { "${it.cname}=${it.cnameMangled(context)}__impl" }
                    listOf(
                        "target_compile_definitions($moduleName PRIVATE $symbolDefines)",
                        "target_compile_definitions(lib_$moduleName PRIVATE $symbolDefines)",
                        "target_compile_definitions(libstatic_$moduleName PRIVATE $symbolDefines)"
                    )
                } else listOf("", "", "")

                // Create CMake file
                File(nativesBuildSourcesDir, "CMakeLists.txt").writeText($$"""
                    cmake_minimum_required(VERSION 3.15)
            
                    project("$$moduleName")
                    
                    $${if (buildSystem.language == Language.CPP) "set(CMAKE_CXX_STANDARD 17)" else ""}
                    $${if (buildSystem.language == Language.CPP) "set(CMAKE_CXX_STANDARD_REQUIRED ON)" else ""}
                    
                    set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY $${nativesBuildOutDir.posixPath})
                    set(CMAKE_LIBRARY_OUTPUT_DIRECTORY $${nativesBuildOutDir.posixPath})
                    set(CMAKE_RUNTIME_OUTPUT_DIRECTORY $${nativesBuildOutDir.posixPath})
                    
                    set(CMAKE_POSITION_INDEPENDENT_CODE ON)
                    
                    add_compile_options(-Wno-initializer-overrides)
                    
                    add_subdirectory("$${context.module.dir.posixPath}" "$${File(nativesBuildOutDir, "common").posixPath}")
                        
                    add_library(lib_$$moduleName SHARED api.$$sourceExtension)
                    target_link_libraries(lib_$$moduleName PUBLIC $$moduleName)
                    
                    add_library(libstatic_$$moduleName STATIC api.$$sourceExtension)
                    target_link_libraries(libstatic_$$moduleName PRIVATE $$moduleName)
                    
                    $${funcRedefines[0]}
                    $${funcRedefines[1]}
                    $${funcRedefines[2]}
                """.trimIndent())

                // Configure CMake (if needed)
                if(shouldInit) {
                    configureCMake(
                        execOps, context, targetType,
                        cmakeArgs = LinkedHashSet(buildSystem.args),
                        cmakeDir = nativesBuildSourcesDir,
                        cmakeBuildDir = File(nativesBuildSourcesDir, "cmake"),
                        cmakeBuildType = buildSystem.buildType
                    )

                    linkerOpts += nativesBuildOutDir.resolve("liblibstatic_$moduleName.a").posixPath

                    // Get linker opts
                    linkerOpts += extractLinkerOpts(execOps, context,
                        File(nativesBuildSourcesDir, "cmake"),
                        moduleName,
                        isKN = true
                    )
                }
            }
            is BuildSystem.Cargo -> {
                if(shouldInit) {
                    val rustFlags = cargoLinkerFlags(execOps, context,
                        project = context.module.dir,
                        buildType = buildSystem.buildType,
                        buildDir = nativesBuildOutDir,
                        target = getCargoTarget(targetType),
                        isKN = true
                    )
                    val rustBuildDir = cargoTargetDir(
                        buildDir = nativesBuildOutDir,
                        buildType = buildSystem.buildType,
                        target = getCargoTarget(targetType)
                    )

                    linkerOpts += listOf(
                        *rustFlags.toTypedArray(),
                        File(rustBuildDir, "lib$moduleName.a").posixPath
                    )
                }
            }
        }

        if(shouldInit && DebugKind.PRINT_LINKER_OPTIONS in context.debug)
            logger.error("[nativekt] Linker options for '$moduleName':\n\t${linkerOpts.joinToString("\n\t")}")

        // Create .def file
        DefPrinter(
            context = context,
            target = defFile.get().asFile,
            headerFile = cinteropHeaderFile,
            linkerOpts = linkerOpts
        )

        // Generate Kotlin files
        KotlinNativePrinter(
            context = context,
            target = File(kotlinFile),
            expectActual = expectActual
        )
    }
}

private abstract class CompileNativesKn @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var context: NativeModuleContext

    @get:Input abstract var targetType: TargetType

    @get:Input abstract var nativesBuildSourcesDir: String
    @get:Input abstract var nativesBuildOutDir: String

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        val moduleName = context.moduleName
        val nativesBuildSourcesDir = File(nativesBuildSourcesDir)
        val nativesBuildOutDir = File(nativesBuildOutDir)

        when(val buildSystem = context.buildSystem) {
            is BuildSystem.CMake -> {
                cmakeBuild(execOps, context, File(nativesBuildSourcesDir, "cmake"))

                prepareNativeLibraryForKN(execOps, context,
                    lib = File(nativesBuildOutDir, "liblibstatic_$moduleName.a"),
                    initSymbolName = context.mangle("init"),
                    targetArgs = getClangTargetArgs(execOps, context, targetType)
                )
            }
            is BuildSystem.Cargo -> {
                cargoBuild(execOps, context,
                    project = context.module.dir,
                    buildType = buildSystem.buildType,
                    buildDir = nativesBuildOutDir,
                    target = getCargoTarget(targetType)
                )
            }
        }
    }
}