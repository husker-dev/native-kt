package com.huskerdev.nativekt.cli

import com.huskerdev.nativekt.BuildSystem
import com.huskerdev.nativekt.Multiplatform
import com.huskerdev.nativekt.NativeKtMultiplatformConfiguration
import com.huskerdev.nativekt.NativeProject
import com.huskerdev.nativekt.createContext
import com.huskerdev.nativekt.printers.rust.RustPrinter
import com.huskerdev.stdlibx.fs.SyncPath
import com.huskerdev.stdlibx.process.exitProcess


fun main(args: Array<String>) {
    var ndlFile: SyncPath? = null
    val module = Multiplatform("example", SyncPath("./"))

    var i = 0
    while (i < args.size) {
        if(args[i].startsWith("-")) {
            if(args[i] == "-cargo") {
                module.cargo {
                    apiRsFile = SyncPath(args[++i])
                    suppressUnused = false
                }
            }
            if(args[i] == "-name")
                module.name = args[++i]
            if(args[i] == "-classpath")
                module.classPath = args[++i]
        } else {
            ndlFile = SyncPath(args[i])
            if(!ndlFile.exists())
                error("NDL file does not exist: '${ndlFile.pathString}'")
        }
        i++
    }

    if(ndlFile == null)
        error("NDL file is not specified")

    run(ndlFile, module)
}

private fun run(
    ndlFile: SyncPath,
    module: NativeProject
) {
    module.ndlFile = ndlFile

    val context = createContext(
        buildDir = SyncPath("./tmp"),
        configuration = NativeKtMultiplatformConfiguration.Impl(),
        module = module,
        executor = NativeKtExecutor(),
        logger = NativeKtLogger()
    ) ?: exitProcess(1)

    when (val buildSystem = context.buildSystem) {
        is BuildSystem.Cargo -> {
            RustPrinter(
                context,
                buildSystem.apiRsFile!!
            )
        }
        else -> throw UnsupportedOperationException()
    }
}