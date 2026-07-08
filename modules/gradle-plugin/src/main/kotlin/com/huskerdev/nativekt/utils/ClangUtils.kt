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
            val alternate = File(mingwLibs, "lib${it.substring(2)}.a")
            if(alternate.exists())
                alternate.posixPath
            else it
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

internal fun clangCompile(
    execOps: ExecOperations,
    clang: String = "clang",
    sources: List<String>,
    includeDirs: List<String> = emptyList(),
    linkerArgs: List<String> = emptyList(),
    dynamicLib: Boolean,
    workingDir: File,
    outputBaseName: String = "out" + if(dynamicLib) "" else "",
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
            "-target x86_64-pc-windows-gnu"
        )
        TargetType.LINUX_X64 -> listOf(
            "-target x86_64-unknown-linux-gnu"
        )
        TargetType.LINUX_ARM64 -> listOf(
            "-target aarch64-unknown-linux-gnu"
        )
        else -> emptyList()
    }
}

internal fun localizeSymbols(
    execOps: ExecOperations,
    nativesRootBuildDir: File,
    lib: File,
    symbols: List<String>
) {
    var ld = "ld"
    var objcopy = "objcopy"
    var ar = "ar"

    // Unpack GNU tools on Windows (because clang64 tools in MinGW does not support COFF)
    if(Os.isFamily(Os.FAMILY_WINDOWS)) {
        fun unpack(name: String): String {
            val file = File(nativesRootBuildDir, name)
            if(!file.exists()) {
                NativeKtInfo::class.java.getResourceAsStream("/com/huskerdev/nativekt/mingw64/$name").use { ins ->
                    if (ins == null)
                        throw NullPointerException("Can not find file in plugin resources: $name")
                    file.parentFile.mkdirs()
                    file.outputStream().use { ins.copyTo(it) }
                }
            }
            return "\"${file.posixPath}\""
        }
        ld = unpack("ld.exe")
        ar = unpack("ar.exe")
        objcopy = unpack("objcopy.exe")
    }

    val libDir = lib.parentFile
    val tmpDir = File(libDir, "_tmp").fresh()
    val tmpObj = File(tmpDir, "__merged.o")
    val tmpSymbolsFile = File(tmpDir, "__symbols.txt")

    // Write all symbols into .txt
    tmpSymbolsFile.writeText(symbols.joinToString("\n") {
        if(Os.isFamily(Os.FAMILY_MAC))
            "_$it"
        else it
    })

    if(Os.isFamily(Os.FAMILY_WINDOWS)) {
        // Unpack all .a into several .o + merge into one
        execOps.exec(
            "$ld -r -o ${tmpObj.name} --whole-archive ../${lib.name} --no-whole-archive",
            workingDir = tmpDir
        )
    } else {
        // Unpack all .a into several .o
        execOps.exec(
            "$ar x ../${lib.name}",
            workingDir = tmpDir
        )

        // Merge several .o into one
        execOps.exec(
            "$ld -r *.o -o ${tmpObj.name}",
            workingDir = tmpDir
        )
    }

    // Localize symbols
    if(Os.isFamily(Os.FAMILY_MAC)) {
        execOps.exec(
            command = "nmedit -R ${tmpSymbolsFile.name} ${tmpObj.name}",
            workingDir = tmpDir
        )
    } else {
        execOps.exec(
            command = "$objcopy --localize-symbols=${tmpSymbolsFile.name} ${tmpObj.name}",
            workingDir = tmpDir
        )
    }

    // Archive into .a
    lib.delete()
    execOps.exec(
        "$ar rcs ../${lib.name} ${tmpObj.name}",
        workingDir = tmpDir
    )

    // Remove temporary dir
    tmpDir.deleteRecursively()
}