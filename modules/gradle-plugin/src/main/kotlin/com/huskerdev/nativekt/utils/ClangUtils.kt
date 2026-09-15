package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.TargetType
import com.huskerdev.osutils.OS
import org.gradle.process.ExecOperations
import java.io.File

internal const val KONAN_SYSROOT_MINGW = "msys2-mingw-w64-x86_64"
internal const val KONAN_SYSROOT_LINUX_X86_64 = "x86_64-unknown-linux-gnu"
internal const val KONAN_SYSROOT_LINUX_AARCH64 = "aarch64-unknown-linux-gnu"

internal fun locateClang(execOps: ExecOperations, context: NativeModuleContext): File {
    return locate(execOps, context, "clang")
        ?: run {
            if (OS.current == OS.WINDOWS) {
                File.listRoots()!!.forEach {
                    val file = File(it, "msys64/clang64/bin/clang.exe")
                    if (file.exists())
                        return@run file
                }
            }
            throw UnsupportedOperationException("Could not locate 'clang'")
        }
}

internal fun systemExtension(dynamicLib: Boolean): String =
    if(dynamicLib) OS.current.dylibExtension
    else OS.current.staticLibExtension

internal fun wholeArchive(name: String) =
    if(OS.current == OS.MACOS)
        "-force_load $name"
    else "-Wl,--whole-archive $name -Wl,--no-whole-archive"

internal fun clangCompile(
    execOps: ExecOperations,
    context: NativeModuleContext,
    clang: String = "clang",
    ar: String = "ar",
    sources: List<String>,
    includeDirs: List<String> = emptyList(),
    linkerArgs: List<String> = emptyList(),
    dynamicLib: Boolean,
    workingDir: File,
    outputBaseName: String = "out",
    extension: String = systemExtension(dynamicLib)
): File {
    val sourcesObj = sources.map {
        File(it).name.replace(".c", ".o")
    }

    sources.forEachIndexed { i, source ->
        execOps.exec(context,
            command = "$clang -c -o ${sourcesObj[i]} $source -fPIC ${includeDirs.joinToString(" ") {"-I$it"}} ${linkerArgs.joinToString(" ")}",
            workingDir = workingDir,
            silent = true
        )
    }

    if(dynamicLib) {
        execOps.exec(context,
            command = "$clang -shared -o $outputBaseName.$extension ${sourcesObj.joinToString(" ")} ${linkerArgs.joinToString(" ")}",
            workingDir = workingDir,
            silent = true
        )
    } else {
        execOps.exec(context,
            command = "$ar r $outputBaseName.$extension ${sourcesObj.joinToString(" ")}",
            workingDir = workingDir,
            silent = true
        )
    }
    return File(workingDir, "$outputBaseName.$extension")
}

internal fun konanSysroot(name: String): String {
    return File(System.getProperty("user.home"), ".konan/dependencies")
        .listFiles()!!
        .filter { it.name.startsWith(name) }
        .maxOf { it }
        .posixPath
}

internal fun getAppleSdkName(
    targetType: TargetType,
) = when(targetType) {
    TargetType.IOS_SIMULATOR_ARM64,
    TargetType.IOS_X64 -> "iphonesimulator"
    TargetType.IOS_ARM64 -> "iphoneos"
    TargetType.TVOS_ARM64 -> "appletvos"
    TargetType.TVOS_SIMULATOR_ARM64,
    TargetType.TVOS_X64 -> "appletvsimulator"
    TargetType.WATCHOS_ARM32,
    TargetType.WATCHOS_ARM64,
    TargetType.WATCHOS_DEVICE_ARM64 -> "watchos"
    TargetType.WATCHOS_SIMULATOR_ARM64,
    TargetType.WATCHOS_X64 -> "watchsimulator"
    else -> throw UnsupportedOperationException()
}
internal fun getAppleSdkSysroot(
    execOps: ExecOperations,
    context: NativeModuleContext,
    targetType: TargetType,
) = execOps.exec(context, "xcrun --sdk ${getAppleSdkName(targetType)} --show-sdk-path", silent = true)

internal fun getAppleSdkVersion(
    execOps: ExecOperations,
    context: NativeModuleContext,
    targetType: TargetType,
) = execOps.exec(context, "xcrun --sdk ${getAppleSdkName(targetType)} --show-sdk-platform-version", silent = true)


internal fun getClangTargetArgs(
    execOps: ExecOperations,
    context: NativeModuleContext,
    targetType: TargetType,
): List<String> {
    fun appleSdkVersion() = getAppleSdkVersion(execOps, context, targetType)
    fun appleSdkSysroot() = getAppleSdkSysroot(execOps, context, targetType)

    return when(targetType) {
        TargetType.IOS_SIMULATOR_ARM64 -> listOf(
            "-arch arm64",
            "-target arm64-apple-ios${appleSdkVersion()}-simulator",
            "-isysroot ${appleSdkSysroot()}"
        )
        TargetType.IOS_X64 -> listOf(
            "-arch x86_64",
            "-target x86_64-apple-ios${appleSdkVersion()}-simulator",
            "-isysroot ${appleSdkSysroot()}"
        )
        TargetType.IOS_ARM64 -> listOf(
            "-arch arm64",
            "-target arm64-apple-ios${appleSdkVersion()}",
            "-isysroot ${appleSdkSysroot()}"
        )
        TargetType.TVOS_ARM64 -> listOf(
            "-arch arm64",
            "-target arm64-apple-tvos${appleSdkVersion()}",
            "-isysroot ${appleSdkSysroot()}"
        )
        TargetType.TVOS_SIMULATOR_ARM64 -> listOf(
            "-arch arm64",
            "-target arm64-apple-tvos${appleSdkVersion()}-simulator",
            "-isysroot ${appleSdkSysroot()}"
        )
        TargetType.TVOS_X64 -> listOf(
            "-arch x86_64",
            "-target x86_64-apple-tvos${appleSdkVersion()}-simulator",
            "-isysroot ${appleSdkSysroot()}"
        )
        TargetType.WATCHOS_ARM32 -> listOf(
            "-arch armv7k",
            "-target armv7k-apple-watchos${appleSdkVersion()}",
            "-isysroot ${appleSdkSysroot()}"
        )
        TargetType.WATCHOS_ARM64 -> listOf(
            "-arch arm64_32",
            "-target arm64-apple-watchos${appleSdkVersion()}",
            "-isysroot ${appleSdkSysroot()}"
        )
        TargetType.WATCHOS_DEVICE_ARM64 -> listOf(
            "-arch arm64",
            "-target arm64-apple-watchos${appleSdkVersion()}",
            "-isysroot ${appleSdkSysroot()}"
        )
        TargetType.WATCHOS_SIMULATOR_ARM64 -> listOf(
            "-arch arm64",
            "-target arm64-apple-watchos${appleSdkVersion()}-simulator",
            "-isysroot ${appleSdkSysroot()}"
        )
        TargetType.WATCHOS_X64 -> listOf(
            "-arch x86_64",
            "-target x86_64-apple-watchos${appleSdkVersion()}-simulator",
            "-isysroot ${appleSdkSysroot()}"
        )
        TargetType.MACOS_ARM64 -> listOf(
            "-arch arm64",
            "-target aarch64-apple-darwin"
        )
        TargetType.MACOS_X64 -> listOf(
            "-arch x86_64",
            "-target x86_64-apple-darwin"
        )
        TargetType.MINGW_X64 -> listOf(
            "-Qunused-arguments",
            "--rtlib=libgcc",
            "--unwindlib=libgcc",
            "-stdlib=libstdc++",
            "--sysroot=${konanSysroot(KONAN_SYSROOT_MINGW)}",
            "-target x86_64-w64-mingw32",
            "-L${konanSysroot(KONAN_SYSROOT_MINGW)}/x86_64-w64-mingw32/lib"
        )
        TargetType.LINUX_X64 -> listOf(
            "--sysroot=${konanSysroot(KONAN_SYSROOT_LINUX_X86_64)}/x86_64-unknown-linux-gnu/sysroot",
            "--gcc-toolchain=${konanSysroot(KONAN_SYSROOT_LINUX_X86_64)}",
            "-target x86_64-unknown-linux-gnu"
        )
        TargetType.LINUX_ARM64 -> listOf(
            "--sysroot=${konanSysroot(KONAN_SYSROOT_LINUX_AARCH64)}/aarch64-unknown-linux-gnu/sysroot",
            "--gcc-toolchain=${konanSysroot(KONAN_SYSROOT_LINUX_AARCH64)}",
            "-target aarch64-unknown-linux-gnu"
        )
        else -> emptyList()
    }
}

/**
 * 1. Globalizes C++ ctor symbols and adds C++ initialization function
 * 2. Repacks the library into an archive with individual object members
 */
internal fun prepareNativeLibraryForKN(
    execOps: ExecOperations,
    context: NativeModuleContext,
    lib: File,
    initSymbolName: String,
    targetArgs: List<String> = emptyList(),
) {
    val libDir = lib.parentFile
    val tmpDir = File(libDir, "_tmp").fresh()

    var objcopy = "objcopy"
    var ar = "ar"

    // Use konan tools
    if(OS.current == OS.WINDOWS) {
        val mingwSysroot = konanSysroot(KONAN_SYSROOT_MINGW)
        ar = "$mingwSysroot/bin/ar.exe"
        objcopy = "$mingwSysroot/bin/objcopy.exe"
    }

    // Unpack all .a into several .o
    execOps.exec(context,
        "$ar x ../${lib.name}",
        workingDir = tmpDir
    )

    val objFiles = tmpDir
        .listFiles { it.extension == "o" || it.extension == "obj" }!!
        .toMutableList()

    // Generate C file with init function that calls each ctor
    objFiles += createCppInitFunction(
        execOps,
        context,
        objcopy,
        tmpDir,
        objFiles,
        initSymbolName,
        targetArgs
    )

    // Archive into .a
    lib.delete()
    execOps.exec(context,
        "$ar rcs ../${lib.name} ${objFiles.joinToString(" ") { it.name }}",
        workingDir = tmpDir
    )

    // Remove temporary dir
    tmpDir.deleteRecursively()
}

private fun createCppInitFunction(
    execOps: ExecOperations,
    context: NativeModuleContext,
    objcopy: String,
    dir: File,
    objFiles: List<File>,
    initSymbolName: String,
    targetArgs: List<String>
): File {

    // Detect and globalize C++ ctor symbols in individual .o files
    val ctorSymbols = objFiles
        .asSequence()
        .map { execOps.exec(context, "nm -a ${it.name}", workingDir = dir, silent = true) }
        .flatMap { it.split("\n") }
        .filter { it.contains("_GLOBAL__sub_I_") }
        .map { it.trim().split(Regex("\\s+")).last() }
        .distinct()
        .toList()

    if(ctorSymbols.isNotEmpty()) {
        if (OS.current == OS.MACOS) {
            objFiles.forEach { globalizeMachOSymbols(it, ctorSymbols) }
        } else {
            val tmpSymbolsFile = File(dir, "__symbols.txt")
            tmpSymbolsFile.writeText(ctorSymbols.joinToString("\n"))
            objFiles.forEach {
                execOps.exec(context, "$objcopy --globalize-symbols=${tmpSymbolsFile.name} ${it.name}", workingDir = dir)
            }
            tmpSymbolsFile.delete()
        }
    }

    // Generate C file with init function that calls each ctor
    val initC = File(dir, "__init.c")
    initC.writeText(buildString {
        append("void $initSymbolName() {")
        ctorSymbols.forEachIndexed { i, sym ->
            append("\n\textern void _ctor_$i(void) __asm__(\"$sym\");")
            append("\n\t_ctor_$i();")
        }
        append("\n}")
    })

    // Compile C file
    val initO = File(dir, "${initC.nameWithoutExtension}.o")
    execOps.exec(context,
        "${locateClang(execOps, context)} -c -o ${initO.name} ${initC.name} ${targetArgs.joinToString(" ")}",
        workingDir = dir,
        silent = true
    )

    return initO
}