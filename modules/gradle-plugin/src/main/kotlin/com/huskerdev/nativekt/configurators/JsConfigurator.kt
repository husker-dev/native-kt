package com.huskerdev.nativekt.configurators

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.huskerdev.nativekt.plugin.BuildSystem
import com.huskerdev.nativekt.plugin.NATIVE_TASK_GROUP
import com.huskerdev.nativekt.plugin.NativeKtJsInterface
import com.huskerdev.nativekt.plugin.NativeProject
import com.huskerdev.nativekt.printers.c.CApiHeaderPrinter
import com.huskerdev.nativekt.printers.c.CApiImplPrinter
import com.huskerdev.nativekt.printers.c.CEmscriptenPrinter
import com.huskerdev.nativekt.printers.kotlin.KotlinJsPrinter
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.IdlResolver
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import javax.inject.Inject

internal fun configureJs(
    project: Project,
    extension: NativeKtJsInterface,
    commonTask: TaskProvider<*>?,
    idl: IdlResolver,
    module: NativeProject,
    sourceSet: KotlinSourceSet,
    srcGenDir: File,
    nativesBuildDir: File,
    expectActual: Boolean,
    isWasm: Boolean
) {
    val targetName = if(isWasm) "wasmJs" else "js"

    if(idl.isUsingLong() && !extension.useJsBigInt) {
        throw UnsupportedOperationException("""
            A Long type was detected in your .ndl file, but it is not enabled by the current JS configuration.

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

    val srcDir = File(srcGenDir, "$targetName/src")
    val resourcesDir = File(srcGenDir, "$targetName/resources")

    val kotlinFile = srcDir
        .resolve(module.classPath.replace(".", "/"))
        .resolve("${module.name}.${targetName}.kt").absolutePath

    val nativesBuildDir = File(nativesBuildDir, "emscripten")
    nativesBuildDir.mkdirs()

    // Prepare task

    val prepareTask = project.tasks.register(
        "prepareNatives${module.name.capitalized()}${targetName.capitalized()}",
        PrepareNativesJs::class.java
    )
    prepareTask.get().also {
        it.inputs.dir(module.dir(project))
        it.outputs.dirs(nativesBuildDir, srcDir)

        it.expectActual     = expectActual
        it.useCoroutines    = extension.useCoroutines
        it.useJsBigInt      = extension.useJsBigInt

        it.idl              = Json.encodeToString(idl)
        it.moduleName       = module.name
        it.moduleClasspath  = module.classPath

        it.projectDir       = module.dir(project).absolutePath
        it.nativesBuildDir  = nativesBuildDir.absolutePath
        it.kotlinFile       = kotlinFile

        it.buildSystem      = module.buildSystem
    }
    if(commonTask != null)
        prepareTask.dependsOn(commonTask)
    prepareTask.dependsOnReload()

    // Compilation task

    val compileTask = project.tasks.register(
        "compileNatives${module.name.capitalized()}${targetName.capitalized()}",
        CompileNativesJs::class.java
    )
    compileTask.get().also {
        it.inputs.dir(module.dir(project))
        it.outputs.dirs(nativesBuildDir, resourcesDir)

        it.resourcesDir     = resourcesDir.absolutePath
        it.moduleName       = module.name

        it.projectDir       = module.dir(project).absolutePath
        it.nativesBuildDir  = nativesBuildDir.absolutePath
        it.buildSystem      = module.buildSystem
    }
    compileTask.dependsOn(prepareTask)

    sourceSet.kotlin.srcDir(compileTask.map { srcDir })
    sourceSet.resources.srcDir(compileTask.map { resourcesDir })
}

private abstract class PrepareNativesJs @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var expectActual: Boolean
    @get:Input abstract var useCoroutines: Boolean
    @get:Input abstract var useJsBigInt: Boolean

    @get:Input abstract var idl: String
    @get:Input abstract var moduleName: String
    @get:Input abstract var moduleClasspath: String

    @get:Input abstract var projectDir: String
    @get:Input abstract var nativesBuildDir: String
    @get:Input abstract var kotlinFile: String

    @get:Input abstract var buildSystem: BuildSystem

    @TaskAction
    fun action() {
        val idl = Json.decodeFromString<IdlResolver>(idl)

        // Create Kotlin/JS bindings
        KotlinJsPrinter(
            idl = idl,
            target = File(kotlinFile),
            classPath = moduleClasspath,
            moduleName = moduleName,
            useCoroutines = useCoroutines,
            expectActual = expectActual
        )

        CApiHeaderPrinter(
            idl = idl,
            target = File(nativesBuildDir, "api.h"),
            isInternal = true
        )
        CApiImplPrinter(
            idl = idl,
            target = File(nativesBuildDir, "api.c"),
            classPath = moduleClasspath
        )
        CEmscriptenPrinter(
            idl = idl,
            target = File(nativesBuildDir, "emscripten_bindings.c")
        )

        val exportedFunctions = buildList {
            addAll(listOf(
                "free",
                "malloc",
                "KString_free",
                "KCharArray_free",
                "KBooleanArray_free",
                "KByteArray_free",
                "KShortArray_free",
                "KIntArray_free",
                "KLongArray_free",
                "KFloatArray_free",
                "KDoubleArray_free",
                "KArray_free"
            ))
            idl.dictionaries.values.mapTo(this) { "${it.name}_free" }
            idl.globalOperators().mapTo(this) { it.name }
        }.joinToString(separator = ",") { "_$it" }

        val runtimeFunctions = listOf(
            "UTF8ToString", "stringToUTF8", "lengthBytesUTF8",
            "HEAP8", "HEAP16", "HEAP32", "HEAPF32", "HEAPF64",
            "addFunction", "wasmTable"
        ).joinToString(separator = ",")

        // ASSERTIONS=2 -s SAFE_HEAP=1 -s STACK_OVERFLOW_CHECK=1
        val args = listOf(
            "--no-entry",

            "SAFE_HEAP=1",
            "ASSERTIONS=2",
            "STACK_OVERFLOW_CHECK=1",

            "ALLOW_MEMORY_GROWTH=1",
            "ALLOW_TABLE_GROWTH=1",
            "MODULARIZE=1",
            "EXPORT_ES6=1",
            "WASM_BIGINT=${if (useJsBigInt) "1" else "0"}",
            "EXPORTED_RUNTIME_METHODS=$runtimeFunctions",
            "EXPORTED_FUNCTIONS=$exportedFunctions",
        ).joinToString(separator = " ") { "-s $it" }

        when(val buildSystem = buildSystem) {
            is BuildSystem.CMake -> {

                // Create CMakeLists.txt with Emscripten linker flags
                File(nativesBuildDir, "CMakeLists.txt").writeText($$"""
                    cmake_minimum_required(VERSION 3.15)
            
                    project("$$moduleName")
                    
                    set(EXTRA_LINK_FLAGS "" CACHE STRING "Extra linker flags")
            
                    add_subdirectory("$${
                        projectDir.replace("\\", "/")
                    }" "$${
                        File(nativesBuildDir, "sub").posixPath
                    }")
                
                    add_executable(lib$$moduleName $<TARGET_OBJECTS:$$moduleName> emscripten_bindings.c api.c)
                    
                    set_target_properties(lib$$moduleName PROPERTIES LINK_FLAGS "${EXTRA_LINK_FLAGS} $$args")
                """.trimIndent())
            }
            is BuildSystem.Cargo -> {
                val buildDir = File(nativesBuildDir)

                val rustBuildDir = cargoTargetDir(
                    buildDir = buildDir,
                    buildType = buildSystem.buildType,
                    target = "wasm32-unknown-emscripten"
                )
                val rustLinkedFlags = cargoLinkerFlags(execOps,
                    project = File(projectDir),
                    buildDir = buildDir,
                    buildType = buildSystem.buildType,
                    target = "wasm32-unknown-emscripten"
                )

                val args = listOf(
                    args,
                    rustLinkedFlags.joinToString(" "),
                    "emscripten_bindings.c", "api.c", "-o lib$moduleName.js", "-Oz",
                    File(rustBuildDir, "lib$moduleName.a").posixPath
                ).joinToString(" ")

                File(nativesBuildDir, "args.txt").writeText(args)
            }
        }
    }
}

private abstract class CompileNativesJs @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var resourcesDir: String
    @get:Input abstract var moduleName: String

    @get:Input abstract var projectDir: String
    @get:Input abstract var nativesBuildDir: String

    @get:Input abstract var buildSystem: BuildSystem

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        val emcc = locate(execOps, "emcc")
            ?: throw UnsupportedOperationException("Could not locate 'emcc'")

        // val cmakeBuildDir = File(cmakeBuildDir)
        val resourcesDir = File(resourcesDir)

        fun copyFiles(js: File, wasm: File) {
            // Copy .js file
            js.copyTo(File(resourcesDir, "lib$moduleName.js"), overwrite = true)

            // Cope .wasm file
            wasm.copyTo(File(resourcesDir, "lib$moduleName.wasm"), overwrite = true)
        }

        when(val buildSystem = buildSystem) {
            is BuildSystem.CMake -> {
                // Generate CMake build
                val toolchain = File(emcc.parentFile, "cmake/Modules/Platform/Emscripten.cmake")
                val buildDir = File(nativesBuildDir, "build")

                cmakeGen(
                    execOps,
                    dir = File(nativesBuildDir),
                    buildDir = buildDir,
                    buildType = buildSystem.buildType,
                    args = LinkedHashSet(buildSystem.args).apply {
                        this += "-DCMAKE_TOOLCHAIN_FILE=\"$toolchain\""
                    }
                )

                // Reload with extra flags
                val extraFlags = File(nativesBuildDir, "sub/CMakeFiles/${moduleName}.dir/flags.make")
                    .readLines()
                    .firstOrNull { it.startsWith("C_FLAGS =") }
                    ?.replace("C_FLAGS =", "")
                    ?.replace("-O3", "")
                    ?.replace("-DNDEBUG", "")
                    ?.trim()
                    ?: ""
                if(extraFlags.isNotEmpty()) {
                    cmakeGen(
                        execOps,
                        dir = File(nativesBuildDir),
                        buildDir = buildDir,
                        buildType = buildSystem.buildType,
                        args = LinkedHashSet(buildSystem.args).apply {
                            this += "-DCMAKE_TOOLCHAIN_FILE=\"$toolchain\""
                            this += "-DEXTRA_LINK_FLAGS=\"${extraFlags}\""
                        }
                    )
                }

                // Build
                cmakeBuild(execOps, buildDir)

                // Copy files
                copyFiles(
                    js = buildDir.listFiles()!!.first {
                        it.name == "lib$moduleName.js"
                    },
                    wasm = buildDir.listFiles()!!.first {
                        it.name == "lib$moduleName.wasm"
                    }
                )
            }

            is BuildSystem.Cargo -> {
                cargoBuild(execOps,
                    project = File(projectDir),
                    buildDir = File(nativesBuildDir),
                    buildType = buildSystem.buildType,
                    target = "wasm32-unknown-emscripten"
                )

                execOps.execWithArgsFile(
                    command = "emcc",
                    args = File(nativesBuildDir, "args.txt"),
                    workingDir = File(nativesBuildDir)
                )
                copyFiles(
                    js = File(nativesBuildDir, "lib$moduleName.js"),
                    wasm = File(nativesBuildDir, "lib$moduleName.wasm")
                )
            }
        }
    }
}