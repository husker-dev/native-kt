import com.huskerdev.nativekt.BuildSystem
import com.huskerdev.nativekt.Multiplatform
import com.huskerdev.nativekt.NativeKtMultiplatformConfiguration
import com.huskerdev.nativekt.NativeProject
import com.huskerdev.nativekt.createContext
import com.huskerdev.nativekt.printers.rust.RustPrinter
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.path


fun main(args: Array<String>) {
    var ndlFile: PlatformFile? = null
    val module = Multiplatform("example", PlatformFile("./"))

    var i = 0
    while (i < args.size) {
        if(args[i].startsWith("-")) {
            if(args[i] == "-cargo") {
                module.cargo {
                    apiRsFile = PlatformFile(args[++i])
                    suppressUnused = false
                }
            }
            if(args[i] == "-name")
                module.name = args[++i]
            if(args[i] == "-classpath")
                module.classPath = args[++i]
        } else {
            ndlFile = PlatformFile(args[i])
            if(!ndlFile.exists())
                error("NDL file does not exist: '${ndlFile.path}'")
        }
        i++
    }

    if(ndlFile == null)
        error("NDL file is not specified")

    run(ndlFile, module)
}

private fun run(
    ndlFile: PlatformFile,
    module: NativeProject
) {
    module.ndlFile = ndlFile

    val context = createContext(
        buildDir = PlatformFile("./tmp"),
        configuration = NativeKtMultiplatformConfiguration.Impl(),
        module = module,
        executor = NativeKtExecutor(),
        logger = NativeKtLogger()
    ) ?: exit(1)

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