package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.plugin.BuildSystem
import com.huskerdev.nativekt.plugin.Language
import com.huskerdev.nativekt.plugin.NATIVE_TASK_GROUP
import com.huskerdev.nativekt.plugin.NativeKtJsInterface
import com.huskerdev.nativekt.printers.c.CApiHeaderPrinter
import com.huskerdev.nativekt.printers.c.CApiImplPrinter
import com.huskerdev.nativekt.printers.c.CEmscriptenPrinter
import com.huskerdev.nativekt.printers.cpp.CppApiHeaderPrinter
import com.huskerdev.nativekt.printers.cpp.CppApiImplPrinter
import com.huskerdev.nativekt.printers.kotlin.KotlinJsPrinter
import com.huskerdev.nativekt.utils.cmakeBuild
import com.huskerdev.nativekt.utils.cmakeGen
import com.huskerdev.nativekt.utils.dependsOnProjectReload
import com.huskerdev.nativekt.utils.fresh
import com.huskerdev.nativekt.utils.getEmccArgs
import com.huskerdev.nativekt.utils.locateEmcc
import com.huskerdev.nativekt.utils.posixPath
import com.huskerdev.nativekt.utils.upperCamelCase
import com.huskerdev.nativekt.utils.wasmBindgenBuild
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import javax.inject.Inject

internal fun configureJs(
    project: Project,
    commonTask: TaskProvider<*>?,
    context: NativeModuleContext,
    sourceSet: KotlinSourceSet,
    expectActual: Boolean,
    isWasm: Boolean
) {
    val targetName = if(isWasm) "wasmJs" else "js"
    val extension = context.extension as NativeKtJsInterface

    if(context.hasAnyLong && !extension.useJsBigInt) {
        throw UnsupportedOperationException("""
            A Long type was detected in your .ndl file, but it is not enabled by the current Kotlin/JS configuration.

            To fix this issue:
            
            1. Make sure your Kotlin Multiplatform version is >= 2.2.20
            2. Set 'useJsBigInt = true' in the plugin configuration.
            3. Add the following compiler options to the Kotlin Multiplatform JS target:
            
            kotlin {
                $targetName {
                    compilerOptions {
                        freeCompilerArgs.addAll(
                            "-Xes-long-as-bigint", 
                            "-XXLanguage:+JsAllowLongInExportedDeclarations"
                        )
                    }
                }
            }
        """.trimIndent())
    }

    val srcDir = File(context.srcGenDir, "$targetName/src")
    val resourcesDir = File(context.srcGenDir, "$targetName/resources")

    val kotlinFile = srcDir
        .resolve(context.classPath.replace(".", "/"))
        .resolve("${context.moduleName}.${targetName}.kt").absolutePath

    val nativesBuildSourcesDir = File(context.nativesBuildDir, "$targetName/sources")
    val nativesBuildOutDir = File(context.nativesBuildDir, "$targetName/out")

    // Prepare task

    val prepareTask = project.tasks.register(
        "prepareNatives${context.moduleName.upperCamelCase()}${targetName.uppercaseFirstChar()}",
        PrepareNativesJs::class.java
    )
    prepareTask.get().also {
        it.inputs.dir(context.module.projectDir)
        it.inputs.file(context.module.ndlFile())
        it.outputs.dirs(nativesBuildSourcesDir, srcDir)

        it.expectActual            = expectActual
        it.isWasm                  = isWasm

        it.context                 = context

        it.nativesBuildSourcesDir  = nativesBuildSourcesDir.absolutePath
        it.nativesBuildOutDir      = nativesBuildOutDir.absolutePath
        it.kotlinFile              = kotlinFile
    }
    if(commonTask != null)
        prepareTask.get().dependsOn(commonTask)
    prepareTask.get().dependsOnProjectReload()

    // Compilation task

    val compileTask = project.tasks.register(
        "compileNatives${context.moduleName.upperCamelCase()}${targetName.uppercaseFirstChar()}",
        CompileNativesJs::class.java
    )
    compileTask.get().also {
        it.inputs.dir(context.module.projectDir)
        it.inputs.dir(nativesBuildSourcesDir)
        it.inputs.file(context.module.ndlFile())
        it.outputs.dirs(nativesBuildOutDir, resourcesDir)

        it.context                 = context

        it.resourcesDir            = resourcesDir.absolutePath
        it.nativesBuildSourcesDir  = nativesBuildSourcesDir.absolutePath
        it.nativesBuildOutDir      = nativesBuildOutDir.absolutePath
    }
    compileTask.get().dependsOn(prepareTask)

    sourceSet.kotlin.srcDir(compileTask.map { srcDir })
    sourceSet.resources.srcDir(compileTask.map { resourcesDir })
}

private abstract class PrepareNativesJs: DefaultTask() {
    @get:Input abstract var expectActual: Boolean
    @get:Input abstract var isWasm: Boolean

    @get:Input abstract var context: NativeModuleContext

    @get:Input abstract var nativesBuildSourcesDir: String
    @get:Input abstract var nativesBuildOutDir: String
    @get:Input abstract var kotlinFile: String

    @TaskAction
    fun action() {
        val nativesBuildSourcesDir = File(nativesBuildSourcesDir).fresh()
        val nativesBuildOutDir = File(nativesBuildOutDir)

        // Create Kotlin/JS bindings
        KotlinJsPrinter(
            context = context,
            target = File(kotlinFile),
            expectActual = expectActual,
            isWasm = isWasm
        )

        if(context.language == Language.C || context.language == Language.CPP) {
            // Generate Emscripten wrapper + C header
            CEmscriptenPrinter(
                context = context,
                target = File(nativesBuildSourcesDir, "emscripten_bindings.c")
            )
            CApiHeaderPrinter(
                context = context,
                target = File(nativesBuildSourcesDir, "api.h"),
                isInternal = true
            )
            if(context.language == Language.C) {
                // Generate C sources (header is already generated)
                CApiImplPrinter(
                    context = context,
                    target = File(nativesBuildSourcesDir, "api.c")
                )
            }
            if(context.language == Language.CPP) {
                // generate C++ sources and header
                CppApiHeaderPrinter(
                    context = context,
                    target = File(nativesBuildSourcesDir, "api.hpp"),
                    tppTarget = File(nativesBuildSourcesDir, "api.tpp")
                )
                CppApiImplPrinter(
                    context = context,
                    target = File(nativesBuildSourcesDir, "api.cpp")
                )
            }
        }

        when(context.buildSystem) {
            is BuildSystem.CMake -> {
                val targetName = "lib${context.moduleName}"
                val projectPath = context.module.projectDir.posixPath
                val projectBuildPath = File(nativesBuildOutDir, "sub").posixPath

                val languages = buildList {
                    add("C")
                    if(context.language == Language.CPP)
                        add("CXX")
                }.joinToString(" ")

                val sources = buildList {
                    add("$<TARGET_OBJECTS:${context.moduleName}>")
                    add("emscripten_bindings.c")
                    add("api.${context.language.sourceExtension}")
                }.joinToString(" ")

                File(nativesBuildSourcesDir, "CMakeLists.txt").writeText($$"""
                    cmake_minimum_required(VERSION 3.15)
            
                    project("$${context.moduleName}" LANGUAGES $$languages)
                    
                    set(EXTRA_LINK_FLAGS "" CACHE STRING "Extra linker flags")
                    
                    # Some compile options
                    add_compile_options(-Wno-initializer-overrides)
                    
                    # Add project
                    add_subdirectory("$$projectPath" "$$projectBuildPath")
                
                    # Add sources + child project
                    add_executable($$targetName $$sources)
                    
                    # Add Emscripten linker flags
                    set_target_properties($$targetName PROPERTIES LINK_FLAGS "${EXTRA_LINK_FLAGS} $${getEmccArgs(context)}")
                """.trimIndent())
            }
            is BuildSystem.Cargo -> Unit // Do nothing
        }
    }
}

private abstract class CompileNativesJs @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var context: NativeModuleContext

    @get:Input abstract var resourcesDir: String
    @get:Input abstract var nativesBuildSourcesDir: String
    @get:Input abstract var nativesBuildOutDir: String

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        val emcc = locateEmcc(execOps, context)
        val resourcesDir = File(resourcesDir)

        val nativesBuildSourcesDir = File(nativesBuildSourcesDir)
        val nativesBuildOutDir = File(nativesBuildOutDir)

        fun packResult(jsFile: File, wasmFile: File) {
            jsFile.copyTo(File(resourcesDir, "lib${context.moduleName}.mjs"), overwrite = true)
            wasmFile.copyTo(File(resourcesDir, "lib${context.moduleName}.wasm"), overwrite = true)
        }

        when(val buildSystem = context.buildSystem) {
            is BuildSystem.CMake -> {

                // Generate CMake build
                val toolchain = File(emcc.parentFile, "cmake/Modules/Platform/Emscripten.cmake")

                cmakeGen(
                    execOps, context,
                    dir = nativesBuildSourcesDir,
                    buildDir = nativesBuildOutDir,
                    buildType = buildSystem.buildType,
                    args = LinkedHashSet(buildSystem.args).apply {
                        this += "-DCMAKE_TOOLCHAIN_FILE=\"$toolchain\""
                    }
                )

                // Reload with extra flags
                val extraFlags = File(nativesBuildOutDir, "sub/CMakeFiles/${context.moduleName}.dir/flags.make")
                    .readLines()
                    .firstOrNull { it.startsWith("C_FLAGS = ") }
                    ?.replaceFirst("C_FLAGS = ", "")
                    ?.trim()
                    ?: ""
                if(extraFlags.isNotEmpty()) {
                    cmakeGen(
                        execOps, context,
                        dir = nativesBuildSourcesDir,
                        buildDir = nativesBuildOutDir,
                        buildType = buildSystem.buildType,
                        args = LinkedHashSet(buildSystem.args).apply {
                            this += "-DCMAKE_TOOLCHAIN_FILE=\"$toolchain\""
                            this += "-DEXTRA_LINK_FLAGS=\"${extraFlags}\""
                        }
                    )
                }

                // Build
                cmakeBuild(execOps, context, nativesBuildOutDir)
                packResult(
                    nativesBuildOutDir.resolve("lib${context.moduleName}.js"),
                    nativesBuildOutDir.resolve("lib${context.moduleName}.wasm")
                )
            }

            is BuildSystem.Cargo -> {
                val pkgDir = wasmBindgenBuild(execOps, context,
                    context.module.projectDir,
                    buildSystem.buildType,
                    nativesBuildOutDir
                )

                val moduleName = context.moduleName
                val jsFile = File(pkgDir, "$moduleName.js")
                val wasmFile = File(pkgDir, "${moduleName}_bg.wasm")

                // Change `name_bg.wasm` references to `libname.wasm`
                jsFile.writeText(jsFile.readText()
                    .replace(wasmFile.name, "lib$moduleName.wasm")
                    .replace("/* @ts-self-types=\"./$moduleName.d.ts\" */", "")
                )

                packResult(jsFile, wasmFile)
            }
        }
    }
}