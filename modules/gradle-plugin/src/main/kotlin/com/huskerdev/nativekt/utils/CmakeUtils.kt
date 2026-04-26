package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.plugin.CMakeBuildType
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.process.ExecOperations
import java.io.File

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
) = execOps.exec("cmake --build \"$buildDir\"", buildDir)

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