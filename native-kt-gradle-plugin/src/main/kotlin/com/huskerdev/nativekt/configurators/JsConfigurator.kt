package com.huskerdev.nativekt.configurators

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.huskerdev.nativekt.plugin.CMakeBuildType
import com.huskerdev.nativekt.plugin.NativeKtExtension
import com.huskerdev.nativekt.plugin.NativeModule
import com.huskerdev.nativekt.printers.HeaderPrinter
import com.huskerdev.nativekt.printers.js.CEmscriptenPrinter
import com.huskerdev.nativekt.printers.js.KotlinJsPrinter
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.BuiltinIdlDeclaration
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlType
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import javax.inject.Inject

internal fun configureJs(
    project: Project,
    extension: NativeKtExtension,
    commonTask: TaskProvider<*>?,
    idl: IdlResolver,
    module: NativeModule,
    sourceSet: KotlinSourceSet,
    srcRootDir: File,
    cmakeRootDir: File,
    expectActual: Boolean,
    isWasm: Boolean
) {
    if(System.getenv()["EMSDK"] == null)
        throw UnsupportedOperationException("Environment variable 'EMSDK' is not specified")

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

    val srcDir = File(srcRootDir, "$targetName/src")
    val resourcesDir = File(srcRootDir, "$targetName/resources")

    val cmakeDir = File(cmakeRootDir, "emscripten")
    val cmakeBuildDir = File(cmakeDir, "build")

    // Prepare task

    val prepareTask = project.tasks.register(
        "prepareNatives${module.name.capitalized()}${targetName.capitalized()}",
        PrepareNativesJs::class.java
    )
    prepareTask.get().also {
        it.inputs.dir(module.dir(project))
        it.outputs.dirs(cmakeDir, srcDir)

        it.expectActual = expectActual
        it.useCoroutines = extension.useCoroutines
        it.useJsBigInt = extension.useJsBigInt

        it.idl = Json.encodeToString(idl)
        it.moduleName = module.name
        it.moduleClasspath = module.classPath

        it.srcDir = srcDir.absolutePath
        it.resourcesDir = resourcesDir.absolutePath

        it.cmakeDir = cmakeDir.absolutePath
        it.cmakeBuildDir = cmakeBuildDir.absolutePath
        it.srcFile = srcDir
            .resolve(module.classPath.replace(".", "/"))
            .resolve("${module.name}.${targetName}.kt").absolutePath
        it.nativeProjectDir = module.dir(project).absolutePath
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
        it.outputs.dirs(cmakeDir, resourcesDir)

        it.cmakeBuildType = module.buildType
        it.cmakeDir       = cmakeDir.absolutePath
        it.cmakeBuildDir  = cmakeBuildDir.absolutePath
        it.resourcesDir   = resourcesDir.absolutePath
        it.moduleName     = module.name
    }
    compileTask.dependsOn(prepareTask)

    sourceSet.kotlin.srcDir(compileTask.map { srcDir })
    sourceSet.resources.srcDir(compileTask.map { resourcesDir })
}

private abstract class PrepareNativesJs: DefaultTask() {
    @get:Input abstract var expectActual: Boolean
    @get:Input abstract var useCoroutines: Boolean
    @get:Input abstract var useJsBigInt: Boolean

    @get:Input abstract var idl: String
    @get:Input abstract var moduleName: String
    @get:Input abstract var moduleClasspath: String

    @get:Input abstract var srcDir: String
    @get:Input abstract var resourcesDir: String

    @get:Input abstract var cmakeDir: String
    @get:Input abstract var cmakeBuildDir: String
    @get:Input abstract var srcFile: String
    @get:Input abstract var nativeProjectDir: String

    init {
        doLast {
            File(srcDir).fresh()
            File(resourcesDir).fresh()

            val idl = Json.decodeFromString<IdlResolver>(idl)

            val cmakeDir = File(cmakeDir)
            cmakeDir.mkdirs()

            // Create CMakeLists.txt with emscripten linker flags
            File(cmakeDir, "CMakeLists.txt").writeText($$"""
                cmake_minimum_required(VERSION 3.15)
        
                project("$$moduleName")
        
                add_subdirectory("$${
                    nativeProjectDir.replace("\\", "/")
                }" "$${
                    File(cmakeBuildDir, "sub").absolutePath.replace("\\", "/")
                }")
        
                add_executable(lib$$moduleName $<TARGET_OBJECTS:$$moduleName> emscripten_bindings.cpp)
                set_target_properties(lib$$moduleName PROPERTIES CXX_STANDARD 17)
                
                set_target_properties(lib$$moduleName PROPERTIES LINK_FLAGS "-s -lembind -s --no-entry -s ALLOW_MEMORY_GROWTH=1 -s ALLOW_TABLE_GROWTH=1 -s MODULARIZE=1 -s EXPORT_ES6=1 -s WASM_BIGINT=$${if(useJsBigInt) "1" else "0"} -s EXPORTED_RUNTIME_METHODS=UTF8ToString,stringToUTF8,lengthBytesUTF8,HEAP32,HEAP8,HEAPF32,addFunction -s EXPORTED_FUNCTIONS=_free,_malloc")
            """.trimIndent())

            // Create Kotlin/JS bindings
            KotlinJsPrinter(
                idl = idl,
                target = File(srcFile),
                classPath = moduleClasspath,
                moduleName = moduleName,
                useCoroutines = useCoroutines,
                expectActual = expectActual
            )

            CEmscriptenPrinter(
                idl = idl,
                target = File(cmakeDir, "emscripten_bindings.cpp")
            )

            HeaderPrinter(
                idl = idl,
                target = File(cmakeDir, "api.h")
            )
        }
    }
}

private abstract class CompileNativesJs @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var cmakeBuildType: CMakeBuildType
    @get:Input abstract var cmakeDir: String
    @get:Input abstract var cmakeBuildDir: String
    @get:Input abstract var resourcesDir: String
    @get:Input abstract var moduleName: String

    init {
        group = "native"
        doLast {
            val cmakeBuildDir = File(cmakeBuildDir)
            val resourcesDir = File(resourcesDir)

            // Generate CMake build
            val toolchain = File(System.getenv()["EMSDK"],
                "upstream/emscripten/cmake/Modules/Platform/Emscripten.cmake")
            cmakeGen(
                execOps, File(cmakeDir), cmakeBuildDir,
                cmakeBuildType,
                args = setOf("-DCMAKE_TOOLCHAIN_FILE=\"$toolchain\"")
            )

            // Build
            cmakeBuild(execOps, cmakeBuildDir)

            // Copy .js file
            cmakeBuildDir.listFiles()!!.first {
                it.name == "lib$moduleName.js"
            }.copyTo(File(resourcesDir, "lib$moduleName.js"), overwrite = true)

            // Cope .wasm file
            cmakeBuildDir.listFiles()!!.first {
                it.name == "lib$moduleName.wasm"
            }.copyTo(File(resourcesDir, "lib$moduleName.wasm"), overwrite = true)
        }
    }
}

internal fun ResolvedIdlType.isLongType(): Boolean {
    return isLong() || (
            // is Long array
            this is ResolvedIdlType.Default &&
            declaration is BuiltinIdlDeclaration &&
            (declaration as BuiltinIdlDeclaration).kind == WebIDLBuiltinKind.LIST &&
            firstParam { type, _ -> type.isLong() }
    )
}

internal fun IdlResolver.isUsingLong(): Boolean {
    // operators
    if(globalOperators().any { op ->
        op.type.isLongType() || op.args.any { it.type.isLongType() }
    }) return true

    // callbacks
    if(callbacks.values.any { cb ->
        cb.type.isLongType() || cb.args.any { it.type.isLongType() }
    }) return true

    return false
}