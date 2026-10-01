import io.github.vinceglb.filekit.PlatformFile
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.fprintf
import platform.posix.stderr


actual fun exit(code: Int): Nothing {
    platform.posix.exit(code)
    throw UnsupportedOperationException()
}

@OptIn(ExperimentalForeignApi::class)
actual fun printErr(message: Any?) {
    fprintf(stderr, "%s", message)
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