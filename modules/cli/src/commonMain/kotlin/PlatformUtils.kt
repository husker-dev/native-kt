import com.huskerdev.nativekt.NativeModuleContext
import io.github.vinceglb.filekit.PlatformFile


expect fun exit(code: Int): Nothing

expect fun printErr(message: Any?)

expect fun exec(
    command: String,
    workingDir: PlatformFile?,
    silent: Boolean,
    errAsStd: Boolean,
    environment: Map<String, String>?
): String

fun error(message: Any?, code: Int = 1): Nothing {
    printErr(message)
    exit(code)
}


class NativeKtExecutor: NativeModuleContext.CommandExecutor {
    override fun exec(
        context: NativeModuleContext,
        command: String,
        workingDir: PlatformFile?,
        silent: Boolean,
        errAsStd: Boolean,
        environment: Map<String, String>?
    ): String = exec(command, workingDir, silent, errAsStd, environment)
}

class NativeKtLogger: NativeModuleContext.Logger {
    override fun error(text: String) = printErr(text)
    override fun info(text: String) = println(text)
}

