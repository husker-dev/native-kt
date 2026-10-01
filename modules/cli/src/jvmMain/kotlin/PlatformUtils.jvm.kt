import io.github.vinceglb.filekit.PlatformFile
import kotlin.system.exitProcess

actual fun exit(code: Int): Nothing =
    exitProcess(code)

actual fun printErr(message: Any?) {
    System.err.println(message)
}

actual fun exec(
    command: String,
    workingDir: PlatformFile?,
    silent: Boolean,
    errAsStd: Boolean,
    environment: Map<String, String>?
): String {
    return ""
}