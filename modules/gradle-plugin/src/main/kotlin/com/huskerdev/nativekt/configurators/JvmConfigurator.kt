package com.huskerdev.nativekt.configurators

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.huskerdev.nativekt.plugin.CMakeBuildType
import com.huskerdev.nativekt.plugin.NATIVE_TASK_GROUP
import com.huskerdev.nativekt.plugin.NativeKtJvmInterface
import com.huskerdev.nativekt.plugin.NativeModule
import com.huskerdev.nativekt.printers.HeaderPrinter
import com.huskerdev.nativekt.printers.jvm.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.IdlResolver
import kotlinx.serialization.json.Json
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
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
    module: NativeModule,
    sourceSet: KotlinSourceSet,
    srcRootDir: File,
    cmakeRootDir: File,
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
    val srcDir = File(srcRootDir, "jvm/src")
    val libsDir = File(srcRootDir, "jvm/libs")
    val targetLibFile = File(libsDir, libFullFileName)

    // cmake dirs
    val cmakeDir = File(cmakeRootDir, "jvm")
    val cmakeBuildDir = File(cmakeDir, "${platformName()}${libArch.capitalized()}")

    // Prepare task

    val prepareTask = project.tasks.register(
        "prepareNatives${module.name.capitalized()}Jvm",
        PrepareNativesJvm::class.java
    )
    prepareTask.get().also {
        it.inputs.dir(module.dir(project))
        it.outputs.dirs(cmakeDir, srcDir)

        it.idl                  = Json.encodeToString(idl)

        it.moduleName           = module.name
        it.moduleClasspath      = module.classPath

        it.useJNI               = extension.useJNI
        it.useForeignApi        = extension.useForeignApi
        it.useJVMCI             = extension.useJVMCI
        it.useUniversalMacOSLib = extension.useUniversalMacOSLib
        it.useCoroutines        = extension.useCoroutines
        it.expectActual         = expectActual

        it.srcDir               = srcDir.absolutePath
        it.libsDir              = libsDir.absolutePath

        it.srcFile = srcDir
            .resolve(module.classPath.replace(".", "/"))
            .resolve("${module.name}.jvm.kt").absolutePath
        it.cmakeDir             = cmakeDir.absolutePath
        it.cmakeBuildDir        = cmakeBuildDir.absolutePath
        it.nativeProjectDir     = module.dir(project).absolutePath
    }
    if(commonTask != null)
        prepareTask.dependsOn(commonTask)
    prepareTask.dependsOnReload()

    sourceSet.kotlin.srcDirs(prepareTask.map { srcDir })

    // Compile task

    val compileTask = project.tasks.register("compileNatives${module.name.capitalized()}Jvm", CompileNativesJvm::class.java).get().also {
        it.inputs.dir(module.dir(project))
        it.outputs.dir(cmakeDir)

        it.cmakeArgs            = LinkedHashSet(module.cmakeArgs)
        it.cmakeBuildType       = module.buildType
        it.useUniversalMacOSLib = extension.useUniversalMacOSLib
        it.cmakeDir             = cmakeDir.absolutePath
        it.cmakeBuildDir        = cmakeBuildDir.absolutePath
        it.libOutFileName       = libOutFileName
        it.targetLibFile        = targetLibFile.absolutePath
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

    @get:Input abstract var srcDir: String
    @get:Input abstract var libsDir: String

    @get:Input abstract var srcFile: String
    @get:Input abstract var cmakeDir: String
    @get:Input abstract var cmakeBuildDir: String
    @get:Input abstract var nativeProjectDir: String

    init {
        doLast {
            File(srcDir).fresh()
            File(libsDir).fresh()

            val idl = Json.decodeFromString<IdlResolver>(idl)

            val cmakeDir = File(cmakeDir)
            cmakeDir.fresh()

            val srcList = arrayListOf<String>()
            if(useJNI)
                srcList += "jni_bindings.c"
            if(useForeignApi || useJVMCI)
                srcList += "externals.c"

            File(cmakeDir, "CMakeLists.txt").writeText($$"""
                cmake_minimum_required(VERSION 3.15)
        
                project("$$moduleName")
        
                add_subdirectory("$${nativeProjectDir.replace("\\", "/")}" 
                    "$${File(cmakeBuildDir, "sub").absolutePath.replace("\\", "/")}")
        
                add_library(lib_$$moduleName SHARED $${srcList.joinToString(" ")})
                
                target_link_libraries(lib_$$moduleName PRIVATE $$moduleName)
                
                target_include_directories(lib_$$moduleName PRIVATE "include" "include/$${jdkPlatformName()}")
            """.trimIndent())

            KotlinJvmPrinter(
                idl = idl,
                target = File(srcFile),
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
                CJniUtilsPrinter(
                    idl = idl,
                    target = File(cmakeDir, "jni_utils.h"),
                    classPath = moduleClasspath,
                    name = "${moduleName.capitalized()}JNI",
                    isAndroid = false
                )

                CJniPrinter(
                    idl = idl,
                    target = File(cmakeDir, "jni_bindings.c"),
                    classPath = moduleClasspath,
                    name = "${moduleName.capitalized()}JNI",
                    isAndroid = false,
                    isAndroidCriticalEnabled = false
                )

                CJniArenaPrinter(
                    target = File(cmakeDir, "jni_arena.h"),
                    callbacks = idl.callbacks.isNotEmpty()
                )

                // unpack jni headers
                val includeDir = File(cmakeDir, "include")
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

            if(useForeignApi || useJVMCI) {
                CExportedPrinter(
                    idl = idl,
                    target = File(cmakeDir, "externals.c"),
                    classPath = moduleClasspath
                )
            }

            HeaderPrinter(
                idl = idl,
                target = File(cmakeDir, "api.h")
            )
        }
    }
}

private abstract class CompileNativesJvm @Inject constructor(
    private val execOps: ExecOperations,
): DefaultTask() {
    @get:Input abstract var cmakeArgs: LinkedHashSet<String>
    @get:Input abstract var cmakeBuildType: CMakeBuildType
    @get:Input abstract var useUniversalMacOSLib: Boolean
    @get:Input abstract var cmakeDir: String
    @get:Input abstract var cmakeBuildDir: String
    @get:Input abstract var libOutFileName: String
    @get:Input abstract var targetLibFile: String

    init {
        group = NATIVE_TASK_GROUP
        doLast {
            // Generate CMake build
            val args = linkedSetOf(
                "-DCMAKE_C_COMPILER=clang",
                "-DCMAKE_CXX_COMPILER=clang++"
            )
            args += cmakeArgs
            if (Os.isFamily(Os.FAMILY_MAC) && useUniversalMacOSLib) {
                args += setOf(
                    "-DCMAKE_C_FLAGS=\"-arch x86_64 -arch arm64\"",
                    "-DCMAKE_CXX_FLAGS=\"-arch x86_64 -arch arm64\""
                )
            }
            cmakeGen(execOps,
                dir = File(cmakeDir),
                buildDir = File(cmakeBuildDir),
                buildType = cmakeBuildType,
                args = args
            )

            // Build
            cmakeBuild(execOps, File(cmakeBuildDir))

            File(cmakeBuildDir).listFiles()!!.first {
                it.name == libOutFileName
            }.copyTo(File(targetLibFile), overwrite = true)
        }
    }
}