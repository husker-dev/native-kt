package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.BuildSystem
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.TargetType
import com.huskerdev.stdlibx.OS
import com.huskerdev.stdlibx.fs.SyncPath
import com.huskerdev.stdlibx.systemLineSeparator

internal fun cmakeGen(
    context: NativeModuleContext,
    dir: SyncPath,
    buildDir: SyncPath,
    buildType: BuildSystem.CMake.BuildType,
    args: LinkedHashSet<String> = linkedSetOf()
){
    buildDir.createDirectories()
    val command = arrayListOf(
        "cmake \"${dir}\"",
        "-B \"$buildDir\"",
        "-G \"${if(OS.isWindows) "MinGW Makefiles" else "Unix Makefiles"}\"",
        "-DCMAKE_BUILD_TYPE=${buildType.cmakeName}"
    )
    command += args

    context.execute(command.joinToString(" "), buildDir)
}

internal fun cmakeBuild(
    context: NativeModuleContext,
    buildDir: SyncPath,
) = context.execute("cmake --build \"${buildDir.invariantSeparatorsPath}\"", buildDir)

internal fun configureCMake(
    context: NativeModuleContext,
    targetType: TargetType,
    cmakeArgs: LinkedHashSet<String>,
    cmakeDir: SyncPath,
    cmakeBuildDir: SyncPath,
    cmakeBuildType: BuildSystem.CMake.BuildType
) {
    val args = LinkedHashSet(cmakeArgs)
    args += linkedSetOf(
        "-DCMAKE_C_COMPILER=clang",
        "-DCMAKE_CXX_COMPILER=clang++",
    )
    getClangTargetArgs(context, targetType).run {
        if(isNotEmpty()) {
            args += "-DCMAKE_C_FLAGS=\"${joinToString(" ")}\""
            args += "-DCMAKE_CXX_FLAGS=\"${joinToString(" ")}\""
        }
    }
    cmakeGen(context,
        dir = cmakeDir,
        buildDir = cmakeBuildDir,
        buildType = cmakeBuildType,
        args = args
    )
}

internal fun extractLinkerOpts(
    context: NativeModuleContext,
    cmakeBuildDir: SyncPath,
    moduleName: String,
    isKN: Boolean = false
): List<String> = buildList {
    // Tip: arguments generates only with executable or shared libraries, so our CMakeLists.txt contains `SHARED` target

    val linkLibs = cmakeBuildDir.resolve("CMakeFiles/lib_$moduleName.dir/linkLibs.rsp")
    val link = cmakeBuildDir.resolve("CMakeFiles/lib_$moduleName.dir/link.txt")
    val cmakeCache = cmakeBuildDir.resolve("CMakeCache.txt")

    // Collect linker flags from 'linkLibs.rsp' or 'link.txt'

    if(linkLibs.exists()) {
        addAll(linkLibs.readString()
            .splitRespectingQuotes()
            .map {
                if(!it.startsWith("-l") && !SyncPath(it).isAbsolute)
                    cmakeBuildDir.resolve(it).invariantSeparatorsPath
                else it
            }
            .filter { it !in setOf("-lpthread") })
    } else if(link.exists()) {
        val parts = link.readString()
            .splitRespectingQuotes()

        var i = 0
        while(i < parts.size) {
            val part = parts[i]
            if(part in setOf("-o", "-install_name")) {
                i += 2
                continue
            }
            if(part.endsWith(".a") ||
                part.endsWith(".dylib") ||
                part.endsWith(".so") ||
                part.endsWith(".dll")
            ) {
                val path = if(!SyncPath(part).isAbsolute)
                    cmakeBuildDir.resolve(part).absoluteString()
                else part
                if(SyncPath(path).exists())
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

    val cmakeCacheText = cmakeCache.readString().split(systemLineSeparator)

    if(cmakeCacheText.any { "_STATIC_LDFLAGS:INTERNAL=" in it && !it.endsWith("=") }) {
        addAll(cmakeCacheText
            .filter { "_STATIC_LDFLAGS:INTERNAL=" in it }
            .flatMap { it.split("_STATIC_LDFLAGS:INTERNAL=")[1].split(";") }
            .toSet().sorted())
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

        libNames.forEach { lib ->
            this.remove("-l$lib")

            libDirs.forEach { dir ->
                val file = SyncPath(dir).resolve("lib${lib}.a")
                if(file.exists())
                    this += file.invariantSeparatorsPath
            }
        }
    }

    if(OS.current == OS.WINDOWS) {
        removeAll { it == "-lsynchronization" }
        add(0, "-lsynchronization")
        if(isKN)
            add(0, "-L${konanSysroot(KONAN_SYSROOT_MINGW)}/x86_64-w64-mingw32/lib")
        else
            add(0, "-L${locateMingw(context).invariantSeparatorsPath}/lib")
    }
}



