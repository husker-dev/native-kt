package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.plugin.LibBuildType
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import java.io.File

internal fun cmakeBuild(
    project: Project,
    dir: File,
    buildDir: File,
    buildType: LibBuildType,
    args: Set<String> = emptySet()
){
    buildDir.mkdirs()
    val command = arrayListOf(
        "cmake \"${dir}\"",
        "-B \"$buildDir\"",
        "-G \"${cmakeGenerator()}\"",
        "-DCMAKE_BUILD_TYPE=${buildType.cmakeName}"
    )
    command += args

    project.exec(command.joinToString(" "), buildDir)
    project.exec("cmake --build \"$buildDir\"", buildDir)
}

val libExtension = when {
    Os.isFamily(Os.FAMILY_WINDOWS) -> "dll"
    Os.isFamily(Os.FAMILY_MAC) -> "dylib"
    Os.isFamily(Os.FAMILY_UNIX) -> "so"
    else -> throw UnsupportedOperationException()
}