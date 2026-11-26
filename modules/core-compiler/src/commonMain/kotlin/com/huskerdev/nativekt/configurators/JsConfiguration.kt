package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.kotlin.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.stdlibx.fs.SyncPath

fun prepareJs(
    context: NativeModuleContext,
    useExpectActual: Boolean,
    targetType: TargetType
) {
    val layout = DirectoryLayout.of(context, targetType)

    if(context.buildSystem is BuildSystem.SPM)
        throw UnsupportedOperationException("JS target for Swift is not supported (for now)")

    // Create Kotlin/JS bindings
    KotlinJsPrinter(
        context = context,
        target = layout.kotlinSourceFile,
        expectActual = useExpectActual,
        isWasm = targetType == TargetType.WASM_JS
    )
}

fun compileJs(
    context: NativeModuleContext,
    targetType: TargetType
) {
    val layout = DirectoryLayout.of(context, targetType)

    fun packResult(jsFile: SyncPath, wasmFile: SyncPath) {
        jsFile.copyTo(layout.kotlinResourcesDir!!.resolve("lib${context.moduleName}.mjs"), overwrite = true)
        wasmFile.copyTo(layout.kotlinResourcesDir.resolve("lib${context.moduleName}.wasm"), overwrite = true)
    }

    when(val buildSystem = context.buildSystem) {
        is BuildSystem.CMake -> {
            val emcc = locateEmcc(context)
            val toolchain = emcc.parent!!.resolve("cmake/Modules/Platform/Emscripten.cmake")

            cmakeGen(
                context,
                dir = buildSystem.projectDir,
                buildDir = layout.nativeBuildDir,
                buildType = buildSystem.buildType,
                args = LinkedHashSet(buildSystem.args).apply {
                    this += "-DCMAKE_TOOLCHAIN_FILE=\"$toolchain\""
                    this += "-DCMAKE_EXE_LINKER_FLAGS=\"${getEmccArgs(context)}\""
                }
            )

            // Build
            cmakeBuild(context, layout.nativeBuildDir)

            val targetName = layout.nativeBuildDir.resolve("nativekt/target.txt").readString()
            val jsFile = layout.nativeBuildDir.resolve("nativekt_${targetName}_executable.js")
            val wasmFile = layout.nativeBuildDir.resolve("nativekt_${targetName}_executable.wasm")

            // Change `nativekt_executable.wasm` references to `libname.wasm`
            jsFile.writeString(jsFile.readString()
                .replace(wasmFile.name, "lib${context.moduleName}.wasm")
            )
            packResult(jsFile, wasmFile)
        }

        is BuildSystem.Cargo -> {
            val pkgDir = wasmBindgenBuild(
                context,
                project = buildSystem.projectDir,
                buildType = buildSystem.buildType,
                buildDir = layout.nativeBuildDir
            )

            val libName = buildSystem.libraryName

            val jsFile = pkgDir.resolve("$libName.js")
            val wasmFile = pkgDir.resolve("${libName}_bg.wasm")

            // Change `name_bg.wasm` references to `libname.wasm`
            jsFile.writeString(jsFile.readString()
                .replace(wasmFile.name, "lib${context.moduleName}.wasm")
                .replace("/* @ts-self-types=\"./$libName.d.ts\" */", "")
            )
            packResult(jsFile, wasmFile)
        }
        is BuildSystem.SPM -> Unit
    }
}
