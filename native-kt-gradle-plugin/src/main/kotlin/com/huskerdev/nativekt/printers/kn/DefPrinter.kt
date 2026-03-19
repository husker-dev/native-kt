package com.huskerdev.nativekt.printers.kn

import java.io.File

class DefPrinter(
    target: File,
    headerFile: File,
    classPath: String,
    linkerOpts: List<String>
) {

    init {
        target.parentFile.mkdirs()

        target.writeText("""
            headers = ${headerFile.absolutePath.replace("\\", "/")}
            package = cinterop.$classPath
            linkerOpts = ${linkerOpts.joinToString(" ")}
        """.trimIndent())
    }
}