package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.plugin.NativeKtInfo
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.process.ExecOperations
import java.io.File

internal fun locateClang(execOps: ExecOperations): File {
    return locate(execOps, "clang")
        ?: run {
            if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                File.listRoots()!!.forEach {
                    val file = File(it, "msys64/clang64/bin/clang.exe")
                    if (file.exists())
                        return@run file
                }
            }
            throw UnsupportedOperationException("Could not locate 'clang'")
        }
}

internal fun locateEMCC(execOps: ExecOperations): File {
    return locate(execOps, "emcc")
        ?: run {
            if("EMSDK" in System.getenv())
                return@run File(System.getenv()["EMSDK"], "upstream/emscripten/emcc")
            if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                File.listRoots()!!.forEach {
                    if (File(it, "emsdk/upstream/emscripten/emcc.bat").exists())
                        return@run File(it, "emsdk/upstream/emscripten/emcc")
                }
            }
            throw UnsupportedOperationException("Could not locate 'emcc'")
        }
}

internal fun mingwLibsDir(execOps: ExecOperations) =
    File(locateClang(execOps).parentFile.parentFile, "lib")

internal fun normalizeMinGWLibs(
    execOps: ExecOperations,
    linkerOpts: List<String>
): List<String> {
    val mingwLibs = mingwLibsDir(execOps)

    return linkerOpts.map {
        if(it.startsWith("-l")) {
            val name = it.substring(2)
            // WARNING: Vibecoded!
            // Prefer .dll.a (import library) over .a (static library) to avoid
            // locally defining symbols that should be imported from DLLs (e.g. std::cout)
            File(mingwLibs, "lib$name.dll.a")
                .run { if(exists()) posixPath else null }
                ?: File(mingwLibs, "lib$name.a")
                    .run { if(exists()) posixPath else null }
                ?: it
        } else it
    }
}

internal fun systemExtension(dynamicLib: Boolean): String {
    return if(dynamicLib) {
        when {
            Os.isFamily(Os.FAMILY_WINDOWS) -> "dll"
            Os.isFamily(Os.FAMILY_MAC) -> "dylib"
            Os.isFamily(Os.FAMILY_UNIX) -> "so"
            else -> ""
        }
    } else "a"
}

internal fun wholeArchive(name: String) =
    if(Os.isFamily(Os.FAMILY_MAC))
        "-force_load $name"
    else "-Wl,--whole-archive $name -Wl,--no-whole-archive"

internal fun clangCompile(
    execOps: ExecOperations,
    clang: String = "clang",
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
        execOps.exec(
            command = "$clang -c -o ${sourcesObj[i]} $source -fPIC ${includeDirs.joinToString(" ") {"-I$it"}} ${linkerArgs.joinToString(" ")}",
            workingDir = workingDir,
            silent = true
        )
    }

    if(dynamicLib) {
        execOps.exec(
            command = "$clang -shared -o $outputBaseName.$extension ${sourcesObj.joinToString(" ")} ${linkerArgs.joinToString(" ")}",
            workingDir = workingDir,
            silent = true
        )
    } else {
        execOps.exec(
            command = "ar r $outputBaseName.$extension ${sourcesObj.joinToString(" ")}",
            workingDir = workingDir,
            silent = true
        )
    }
    return File(workingDir, "$outputBaseName.$extension")
}

internal fun getClangTargetArgs(
    execOps: ExecOperations,
    targetType: TargetType,
): List<String> {
    fun xcSdkVersion(sdk: String) =
        execOps.exec("xcrun --sdk $sdk --show-sdk-platform-version", silent = true)
    fun xcSdkSysroot(sdk: String) =
        execOps.exec("xcrun --sdk $sdk --show-sdk-path", silent = true)
    fun konanSysroot(baseName: String): File {
        return File(System.getProperty("user.home"), ".konan/dependencies")
            .listFiles()
            .filter { it.name.startsWith(baseName) }
            .maxOf { it }
    }

    return when(targetType) {
        TargetType.IOS_SIMULATOR_ARM64 -> listOf(
            "-arch arm64",
            "-target arm64-apple-ios${xcSdkVersion("iphonesimulator")}-simulator",
            "-isysroot ${xcSdkSysroot("iphonesimulator")}"
        )
        TargetType.IOS_X64 -> listOf(
            "-arch x86_64",
            "-target x86_64-apple-ios${xcSdkVersion("iphonesimulator")}-simulator",
            "-isysroot ${xcSdkSysroot("iphonesimulator")}"
        )
        TargetType.IOS_ARM64 -> listOf(
            "-arch arm64",
            "-target arm64-apple-ios${xcSdkVersion("iphoneos")}",
            "-isysroot ${xcSdkSysroot("iphoneos")}"
        )
        TargetType.TVOS_ARM64 -> listOf(
            "-arch arm64",
            "-target arm64-apple-tvos${xcSdkVersion("appletvos")}",
            "-isysroot ${xcSdkSysroot("appletvos")}"
        )
        TargetType.TVOS_SIMULATOR_ARM64 -> listOf(
            "-arch arm64",
            "-target arm64-apple-tvos${xcSdkVersion("appletvsimulator")}-simulator",
            "-isysroot ${xcSdkSysroot("appletvsimulator")}"
        )
        TargetType.TVOS_X64 -> listOf(
            "-arch x86_64",
            "-target x86_64-apple-tvos${xcSdkVersion("appletvsimulator")}-simulator",
            "-isysroot ${xcSdkSysroot("appletvsimulator")}"
        )
        TargetType.WATCHOS_ARM32 -> listOf(
            "-arch armv7k",
            "-target armv7k-apple-watchos${xcSdkVersion("watchos")}",
            "-isysroot ${xcSdkSysroot("watchos")}"
        )
        TargetType.WATCHOS_ARM64 -> listOf(
            "-arch arm64_32",
            "-target arm64-apple-watchos${xcSdkVersion("watchos")}",
            "-isysroot ${xcSdkSysroot("watchos")}"
        )
        TargetType.WATCHOS_DEVICE_ARM64 -> listOf(
            "-arch arm64",
            "-target arm64-apple-watchos${xcSdkVersion("watchos")}",
            "-isysroot ${xcSdkSysroot("watchos")}"
        )
        TargetType.WATCHOS_SIMULATOR_ARM64 -> listOf(
            "-arch arm64",
            "-target arm64-apple-watchos${xcSdkVersion("watchsimulator")}-simulator",
            "-isysroot ${xcSdkSysroot("watchsimulator")}"
        )
        TargetType.WATCHOS_X64 -> listOf(
            "-arch x86_64",
            "-target x86_64-apple-watchos${xcSdkVersion("watchsimulator")}-simulator",
            "-isysroot ${xcSdkSysroot("watchsimulator")}"
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
            "--sysroot=${konanSysroot("msys2-mingw-w64-x86_64")}",
            "-target x86_64-w64-mingw32"
        )
        TargetType.LINUX_X64 -> listOf(
            "--sysroot=${konanSysroot("x86_64-unknown-linux-gnu")}/x86_64-unknown-linux-gnu/sysroot",
            "-target x86_64-unknown-linux-gnu"
        )
        TargetType.LINUX_ARM64 -> listOf(
            "--sysroot=${konanSysroot("aarch64-unknown-linux-gnu")}/aarch64-unknown-linux-gnu/sysroot",
            "-target aarch64-unknown-linux-gnu"
        )
        else -> emptyList()
    }
}

/**
 * 1. Localizes C symbols
 * 2. Adds C++ initialization function
 */
internal fun prepareNativeLibraryForKN(
    execOps: ExecOperations,
    nativesRootBuildDir: File,
    lib: File,
    symbols: List<String>,
    initSymbolName: String,
    targetArgs: List<String> = emptyList(),
) {
    fun unpack(path: String): String {
        val file = File(nativesRootBuildDir, File(path).name)
        if(!file.exists()) {
            NativeKtInfo::class.java.getResourceAsStream("/com/huskerdev/nativekt/$path").use { ins ->
                if (ins == null)
                    throw NullPointerException("Can not find file in plugin resources: $path")
                file.parentFile.mkdirs()
                file.outputStream().use { ins.copyTo(it) }
            }
            if(!Os.isFamily(Os.FAMILY_WINDOWS))
                execOps.exec("chmod +x \"${file.posixPath}\"")
        }
        return "\"${file.posixPath}\""
    }

    val libDir = lib.parentFile
    val tmpDir = File(libDir, "_tmp").fresh()
    val tmpObjFile = File(tmpDir, "__merged.o")

    var ld = "ld"
    var objcopy = "objcopy"
    var ar = "ar"

    // Unpack GNU tools on Windows (because clang64 tools in MinGW does not support COFF)
    if(Os.isFamily(Os.FAMILY_WINDOWS)) {
        ld = unpack("mingw64/ld.exe")
        ar = unpack("mingw64/ar.exe")
        objcopy = unpack("mingw64/objcopy.exe")
    }

    // Unpack all .a into several .o
    execOps.exec(
        "$ar x ../${lib.name}",
        workingDir = tmpDir
    )

    val objFiles = tmpDir
        .listFiles { it.extension == "o" || it.extension == "obj" }
        .toMutableList()

    // Generate C file with init function that calls each ctor
    objFiles += createCppInitFunction(
        execOps,
        objcopy,
        tmpDir,
        objFiles,
        initSymbolName,
        targetArgs
    )

    // Merge several .o into one
    execOps.exec(
        "$ld -r ${objFiles.joinToString(" ") { it.name }} -o ${tmpObjFile.name}",
        workingDir = tmpDir
    )

    // Localize С symbols
    localizeSymbols(
        execOps,
        objcopy,
        tmpObjFile,
        symbols
    )

    // Archive into .a
    lib.delete()
    execOps.exec(
        "$ar rcs ../${lib.name} ${tmpObjFile.name}",
        workingDir = tmpDir
    )

    // Remove temporary dir
    tmpDir.deleteRecursively()
}

private fun localizeSymbols(
    execOps: ExecOperations,
    objcopy: String,
    objFile: File,
    symbols: List<String>
) {
    val dir = objFile.parentFile
    val tmpSymbolsFile = File(dir, "__symbols.txt")

    // Localize symbols
    if(Os.isFamily(Os.FAMILY_MAC)) {
        try {
            tmpSymbolsFile.writeText(symbols.joinToString("\n") { "_$it" })
            execOps.exec(
                command = "nmedit -R ${tmpSymbolsFile.name} ${objFile.name}",
                workingDir = dir,
                silent = true
            )
        } catch (_: Throwable) {}
    } else {
        tmpSymbolsFile.writeText(symbols.joinToString("\n"))
        execOps.exec(
            command = "$objcopy --localize-symbols=${tmpSymbolsFile.name} ${objFile.name}",
            workingDir = dir
        )
    }
    tmpSymbolsFile.delete()
}

private fun createCppInitFunction(
    execOps: ExecOperations,
    objcopy: String,
    dir: File,
    objFiles: List<File>,
    initSymbolName: String,
    targetArgs: List<String>
): File {

    // Detect and globalize C++ ctor symbols in individual .o files
    val ctorSymbols = objFiles
        .asSequence()
        .map { execOps.exec("nm -a ${it.name}", workingDir = dir, silent = true) }
        .flatMap { it.split("\n") }
        .filter { it.contains("_GLOBAL__sub_I_") }
        .map { it.trim().split(Regex("\\s+")).last() }
        .distinct()
        .toList()

    if(ctorSymbols.isNotEmpty()) {
        if (Os.isFamily(Os.FAMILY_MAC)) {
            objFiles.forEach { globalizeMachOSymbols(it, ctorSymbols) }
        } else {
            val tmpSymbolsFile = File(dir, "__symbols.txt")
            tmpSymbolsFile.writeText(ctorSymbols.joinToString("\n"))
            objFiles.forEach {
                execOps.exec("$objcopy --globalize-symbols=${tmpSymbolsFile.name} ${it.name}", workingDir = dir)
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
    execOps.exec(
        "${locateClang(execOps)} -c -o ${initO.name} ${initC.name} ${targetArgs.joinToString(" ")}",
        workingDir = dir,
        silent = true
    )

    return initO
}