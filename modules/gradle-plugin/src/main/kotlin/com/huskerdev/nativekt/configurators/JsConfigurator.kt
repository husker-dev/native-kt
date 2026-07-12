package com.huskerdev.nativekt.configurators

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.huskerdev.nativekt.plugin.BuildSystem
import com.huskerdev.nativekt.plugin.Language
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
import org.gradle.api.tasks.Optional
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

    val nativesBuildSourcesDir = File(nativesBuildDir, "$targetName/sources")
    val nativesBuildOutDir = File(nativesBuildDir, "$targetName/out")

    // Prepare task

    val prepareTask = project.tasks.register(
        "prepareNatives${module.name.capitalized()}${targetName.capitalized()}",
        PrepareNativesJs::class.java
    )
    prepareTask.get().also {
        it.inputs.dir(module.dir(project))
        it.inputs.file(module.getNDLFile(project))
        it.outputs.dirs(nativesBuildSourcesDir, srcDir)

        it.expectActual            = expectActual
        it.useCoroutines           = extension.useCoroutines
        it.useJsBigInt             = extension.useJsBigInt
        it.emscriptenEnv           = extension.emscriptenEnv

        it.idl                     = Json.encodeToString(idl)
        it.moduleName              = module.name
        it.moduleClasspath         = module.classPath

        it.projectDir              = module.dir(project).absolutePath
        it.nativesBuildSourcesDir  = nativesBuildSourcesDir.absolutePath
        it.nativesBuildOutDir      = nativesBuildOutDir.absolutePath
        it.kotlinFile              = kotlinFile

        it.buildSystem             = module.buildSystem
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
        it.inputs.dir(nativesBuildSourcesDir)
        it.inputs.file(module.getNDLFile(project))
        it.outputs.dirs(nativesBuildOutDir, resourcesDir)

        it.useJsBigInt             = extension.useJsBigInt

        it.resourcesDir            = resourcesDir.absolutePath
        it.idl                     = Json.encodeToString(idl)
        it.moduleName              = module.name

        it.projectDir              = module.dir(project).absolutePath
        it.nativesBuildSourcesDir  = nativesBuildSourcesDir.absolutePath
        it.nativesBuildOutDir      = nativesBuildOutDir.absolutePath
        it.buildSystem             = module.buildSystem
    }
    compileTask.dependsOn(prepareTask)

    sourceSet.kotlin.srcDir(compileTask.map { srcDir })
    sourceSet.resources.srcDir(compileTask.map { resourcesDir })
}

private abstract class PrepareNativesJs: DefaultTask() {
    @get:Input abstract var expectActual: Boolean
    @get:Input abstract var useCoroutines: Boolean
    @get:Input abstract var useJsBigInt: Boolean
    @get:Input @get:Optional abstract var emscriptenEnv: List<String>?

    @get:Input abstract var idl: String
    @get:Input abstract var moduleName: String
    @get:Input abstract var moduleClasspath: String

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

        // Collect all available functions and mangle them into the short name to minimize .js file
        val jsMangle = buildList {
            addAll(listOf(
                "kstring",
                "kchar_array",
                "kboolean_array",
                "kbyte_array",
                "kshort_array",
                "kint_array",
                "klong_array",
                "kfloat_array",
                "kdouble_array",
                "karray",
                *idl.dictionaries.values.map { it.name.lowercase() }.toTypedArray()
            ).flatMap {
                listOf("${it}_free", "${it}_free_addr")
            })
            idl.allOperators().mapTo(this) { it.cname }
        }.mapIndexed { index, name ->
            val sb = StringBuilder()
            var n = index
            do {
                val d = n % 52
                sb.insert(0, (if (d < 26) 'A'.code + d else 'a'.code + d - 26).toChar())
                n /= 52
            } while (n > 0)
            name to (sb.toString() + "_")
        }.toMap()

        // Create Kotlin/JS bindings
        KotlinJsPrinter(
            idl = idl,
            target = File(kotlinFile),
            jsMangle = jsMangle,
            classPath = moduleClasspath,
            moduleName = moduleName,
            useCoroutines = useCoroutines,
            expectActual = expectActual
        )

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
        CEmscriptenPrinter(
            idl = idl,
            target = File(nativesBuildSourcesDir, "emscripten_bindings.$sourceExtension"),
            language = buildSystem.language,
            jsMangle = jsMangle,
            classPath = moduleClasspath,
            moduleName = moduleName
        )

        val exportedFunctions = listOf(
            "_free",
            "_malloc"
        )

        val runtimeFunctions = listOf(
            "UTF8ToString", "stringToUTF8", "lengthBytesUTF8",
            "HEAP8", "HEAP16", "HEAP32", "HEAPF32", "HEAPF64",
            "addFunction"
        ).joinToString(separator = ",")

        // ASSERTIONS=2 -s SAFE_HEAP=1 -s STACK_OVERFLOW_CHECK=1
        val args = listOfNotNull(
            "--no-entry",
            "ALLOW_MEMORY_GROWTH=1",
            "ALLOW_TABLE_GROWTH=1",
            "GROWABLE_ARRAYBUFFERS=0", // TODO: Remove (https://github.com/emscripten-core/emscripten/issues/27241)

            "MODULARIZE=1",
            "EXPORT_ES6=1",
            "WASM_BIGINT=${if (useJsBigInt) "1" else "0"}",
            emscriptenEnv?.joinToString(separator = ",", prefix = "ENVIRONMENT="),

            "EXPORTED_RUNTIME_METHODS=$runtimeFunctions",
            "EXPORTED_FUNCTIONS=${exportedFunctions.joinToString(separator = ",")}",
        ).joinToString(separator = " ") { "-s $it" }

        when(buildSystem) {
            is BuildSystem.CMake -> {
                // Create CMakeLists.txt with Emscripten linker flags
                File(nativesBuildSourcesDir, "CMakeLists.txt").writeText($$"""
                    cmake_minimum_required(VERSION 3.15)
            
                    project("$$moduleName"$${if (buildSystem.language == Language.CPP) " LANGUAGES CXX" else ""})
                    
                    set(EXTRA_LINK_FLAGS "" CACHE STRING "Extra linker flags")
            
                    add_subdirectory("$${projectDir.posixPath}" "$${File(nativesBuildOutDir, "sub").posixPath}")
                
                    add_executable(lib$$moduleName $<TARGET_OBJECTS:$$moduleName> emscripten_bindings.$$sourceExtension api.$$sourceExtension)
                    
                    set_target_properties(lib$$moduleName PROPERTIES LINK_FLAGS "${EXTRA_LINK_FLAGS} $$args")
                """.trimIndent())
            }
            is BuildSystem.Cargo -> {
                val sources = listOf("emscripten_bindings.c", "api.c")
                    .joinToString(" ") { File(nativesBuildSourcesDir, it).posixPath }

                File(nativesBuildSourcesDir, "args.txt").writeText(listOf(
                    args,
                    sources,
                    "-o lib$moduleName.js", "-Oz",
                ).joinToString(" "))
            }
        }
    }
}

private abstract class CompileNativesJs @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var useJsBigInt: Boolean

    @get:Input abstract var resourcesDir: String
    @get:Input abstract var idl: String
    @get:Input abstract var moduleName: String

    @get:Input abstract var projectDir: String
    @get:Input abstract var nativesBuildSourcesDir: String
    @get:Input abstract var nativesBuildOutDir: String

    @get:Input abstract var buildSystem: BuildSystem

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        val emcc = locateEMCC(execOps)
        val resourcesDir = File(resourcesDir)

        val nativesBuildSourcesDir = File(nativesBuildSourcesDir)
        val nativesBuildOutDir = File(nativesBuildOutDir)

        when(val buildSystem = buildSystem) {
            is BuildSystem.CMake -> {

                // Generate CMake build
                val toolchain = File(emcc.parentFile, "cmake/Modules/Platform/Emscripten.cmake")

                cmakeGen(
                    execOps,
                    dir = nativesBuildSourcesDir,
                    buildDir = nativesBuildOutDir,
                    buildType = buildSystem.buildType,
                    args = LinkedHashSet(buildSystem.args).apply {
                        this += "-DCMAKE_TOOLCHAIN_FILE=\"$toolchain\""
                    }
                )

                // Reload with extra flags
                val extraFlags = File(nativesBuildOutDir, "sub/CMakeFiles/${moduleName}.dir/flags.make")
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
                cmakeBuild(execOps, nativesBuildOutDir)
            }

            is BuildSystem.Cargo -> {
                val rustBuildDir = cargoTargetDir(
                    buildDir = nativesBuildOutDir,
                    buildType = buildSystem.buildType,
                    target = "wasm32-unknown-emscripten"
                )
                val rustLinkedFlags = cargoLinkerFlags(execOps,
                    project = File(projectDir),
                    buildDir = nativesBuildOutDir,
                    buildType = buildSystem.buildType,
                    target = "wasm32-unknown-emscripten"
                )

                cargoBuild(execOps,
                    project = File(projectDir),
                    buildDir = nativesBuildOutDir,
                    buildType = buildSystem.buildType,
                    target = "wasm32-unknown-emscripten"
                )

                // Build using emcc
                execOps.execWithArgsFile(
                    command = "${emcc.posixPath} ${rustLinkedFlags.joinToString(" ")} ${File(rustBuildDir, "lib$moduleName.a").posixPath}",
                    args = File(nativesBuildSourcesDir, "args.txt"),
                    workingDir = nativesBuildOutDir
                )
            }
        }

        // Copy .js file
        File(nativesBuildOutDir, "lib$moduleName.js")
            .copyTo(File(resourcesDir, "lib$moduleName.mjs"), overwrite = true)

        // Cope .wasm file
        File(nativesBuildOutDir, "lib$moduleName.wasm")
            .copyTo(File(resourcesDir, "lib$moduleName.wasm"), overwrite = true)
    }
}