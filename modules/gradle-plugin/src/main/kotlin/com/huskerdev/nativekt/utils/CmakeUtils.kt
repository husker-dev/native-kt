package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.plugin.CMakeBuildType
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.process.ExecOperations
import java.io.File

private val cmakeGenerator: String = when {
    Os.isFamily(Os.FAMILY_WINDOWS) -> "MinGW Makefiles"
    else -> "Unix Makefiles"
}

val libExtension = when {
    Os.isFamily(Os.FAMILY_WINDOWS) -> "dll"
    Os.isFamily(Os.FAMILY_MAC) -> "dylib"
    Os.isFamily(Os.FAMILY_UNIX) -> "so"
    else -> throw UnsupportedOperationException()
}

internal fun cmakeGen(
    execOps: ExecOperations,
    dir: File,
    buildDir: File,
    buildType: CMakeBuildType,
    args: LinkedHashSet<String> = linkedSetOf()
){
    buildDir.mkdirs()
    val command = arrayListOf(
        "cmake \"${dir}\"",
        "-B \"$buildDir\"",
        "-G \"${cmakeGenerator}\"",
        "-DCMAKE_BUILD_TYPE=${buildType.cmakeName}"
    )
    command += args

    execOps.exec(command.joinToString(" "), buildDir)
}

internal fun cmakeBuild(
    execOps: ExecOperations,
    buildDir: File,
) = execOps.exec("cmake --build \"${buildDir.posixPath}\"", buildDir)

internal fun extractLinkerOpts(
    execOps: ExecOperations,
    cmakeBuildDir: File,
    moduleName: String
): List<String> = buildList {
    // Tip: arguments generates only with executable or shared libraries, so our CMakeLists.txt contains `SHARED` target

    this += cmakeBuildDir.resolve("liblibstatic_$moduleName.a").posixPath

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
                    File(cmakeBuildDir, it).posixPath
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
                    this += file.posixPath
            }
        }
    }

    // Try to resolve libraries in MinGW
    if(Os.isFamily(Os.FAMILY_WINDOWS)) {
        normalizeMinGWLibs(execOps, this)
            .forEachIndexed { i, it -> this[i] = it }
    }
}

fun String.splitRespectingQuotes(): List<String> =
    """[^\s"']+|"([^"]*)"|'([^']*)'""".toRegex()
        .findAll(this)
        .map { it.value.trim('"', '\'') }
        .toList()

internal fun configureCMake(
    execOps: ExecOperations,
    targetType: TargetType,
    cmakeArgs: LinkedHashSet<String>,
    cmakeDir: File,
    cmakeBuildDir: File,
    cmakeBuildType: CMakeBuildType
) {
    val args = LinkedHashSet(cmakeArgs)
    args += linkedSetOf(
        "-DCMAKE_C_COMPILER=clang",
        "-DCMAKE_CXX_COMPILER=clang++",
    )
    getClangTargetArgs(execOps, targetType).run {
        if(isNotEmpty()) {
            args += "-DCMAKE_C_FLAGS=\"${joinToString(" ")}\""
            args += "-DCMAKE_CXX_FLAGS=\"${joinToString(" ")}\""
        }
    }
    cmakeGen(execOps,
        dir = cmakeDir,
        buildDir = cmakeBuildDir,
        buildType = cmakeBuildType,
        args = args
    )
}