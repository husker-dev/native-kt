package com.huskerdev.nativekt.configurators

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.printers.kotlin.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.nativekt.utils.getCargoEnv
import com.huskerdev.stdlibx.OS


fun prepareNative(
    context: NativeModuleContext,
    targetType: TargetType,
    useExpectActual: Boolean,
    shouldInit: Boolean
) {
    val layout = DirectoryLayout.of(context, targetType)
    val moduleName = context.moduleName

    layout.cinteropDir!!.createDirectories()
    val cinteropHeaderFile = layout.cinteropDir.resolve("header.h")

    val linkerOpts = arrayListOf<String>()

    // Generate header
    CApiHeaderPrinter(
        context = context,
        target = cinteropHeaderFile,
        language = null,
        isImpl = true,
    )

    when(val buildSystem = context.buildSystem) {
        is BuildSystem.CMake -> if(shouldInit) {
            // Configure CMake (if needed)
            configureCMake(
                context, targetType,
                cmakeArgs = LinkedHashSet(buildSystem.args),
                cmakeDir = buildSystem.projectDir,
                cmakeBuildDir = layout.nativeBuildDir,
                cmakeBuildType = buildSystem.buildType
            )

            val targetName = layout.nativeBuildDir.resolve("nativekt/target.txt").readString()
            linkerOpts += layout.nativeBuildDir.resolve("libnativekt_${targetName}_static.a").invariantSeparatorsPath

            // Get linker opts
            linkerOpts += cmakeLinkerArgs(
                context,
                layout.nativeBuildDir,
                "nativekt_${targetName}_shared",
                isKN = true
            )
        }
        is BuildSystem.Cargo -> if(shouldInit) {
            val rustFlags = cargoLinkerFlags(
                context,
                project = buildSystem.projectDir,
                buildType = buildSystem.buildType,
                buildDir = layout.nativeBuildDir,
                target = getCargoTarget(targetType),
                env = getCargoEnv(context, targetType),
                isKN = true
            )
            val rustBuildDir = cargoTargetDir(
                buildDir = layout.nativeBuildDir,
                buildType = buildSystem.buildType,
                target = getCargoTarget(targetType)
            )

            linkerOpts += listOf(
                *rustFlags.toTypedArray(),
                rustBuildDir.resolve("lib${buildSystem.libraryName}.a").invariantSeparatorsPath
            )
        }
        is BuildSystem.SPM -> {
            if(OS.isWindows) throw UnsupportedOperationException("Swift is not supported on Windows yet.")
            if(shouldInit) {
                val (outDir, spmLinkerFlags) = spmBuild(
                    context = context,
                    projectDir = buildSystem.projectDir,
                    buildType = buildSystem.buildType,
                    buildDir = layout.nativeBuildDir,
                    targetType = targetType,
                    onlyLinkerFlags = true
                )

                linkerOpts += listOf(
                    *spmLinkerFlags.toTypedArray(),
                    outDir.resolve("lib${buildSystem.staticLibraryName}.a").invariantSeparatorsPath
                )
            }
        }
    }

    if(shouldInit && DebugKind.PRINT_LINKER_OPTIONS in context.debug)
        context.logger?.error("Linker options for '$moduleName':\n\t${linkerOpts.joinToString("\n\t")}")

    // Create .def file
    DefPrinter(
        context = context,
        target = layout.cinteropFile!!,
        headerFile = cinteropHeaderFile,
        linkerOpts = linkerOpts
    )

    // Generate Kotlin files
    KotlinNativePrinter(
        context = context,
        target = layout.kotlinSourceFile,
        expectActual = useExpectActual
    )
}

fun compileNative(
    context: NativeModuleContext,
    targetType: TargetType,
) {
    val layout = DirectoryLayout.of(context, targetType)

    when(val buildSystem = context.buildSystem) {
        is BuildSystem.CMake -> {
            cmakeBuild(context, layout.nativeBuildDir)

            val targetName = layout.nativeBuildDir.resolve("nativekt/target.txt").readString()
            prepareNativeLibraryForKN(context,
                lib = layout.nativeBuildDir.resolve("libnativekt_${targetName}_static.a"),
                initSymbolName = context.mangle("init"),
                targetArgs = getClangTargetArgs(context, targetType)
            )
        }
        is BuildSystem.Cargo -> {
            cargoBuild(context,
                project = buildSystem.projectDir,
                buildType = buildSystem.buildType,
                buildDir = layout.nativeBuildDir,
                target = getCargoTarget(targetType),
                env = getCargoEnv(context, targetType)
            )
        }
        is BuildSystem.SPM -> {
            spmBuild(context,
                projectDir = buildSystem.projectDir,
                buildType = buildSystem.buildType,
                buildDir = layout.nativeBuildDir,
                targetType = targetType,
                onlyLinkerFlags = true
            )
        }
    }
}
