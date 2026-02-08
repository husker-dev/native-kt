package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.plugin.CMakeBuildType
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import java.io.File

internal fun cmakeGen(
    project: Project,
    dir: File,
    buildDir: File,
    buildType: CMakeBuildType,
    args: Set<String> = emptySet()
){
    buildDir.mkdirs()
    val command = arrayListOf(
        "cmake \"${dir}\"",
        "-B \"$buildDir\"",
        "-G \"${cmakeGenerator}\"",
        "-DCMAKE_BUILD_TYPE=${buildType.cmakeName}"
    )
    command += args

    project.exec(command.joinToString(" "), buildDir)
}

internal fun cmakeBuild(
    project: Project,
    buildDir: File,
) = project.exec("cmake --build \"$buildDir\"", buildDir)

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