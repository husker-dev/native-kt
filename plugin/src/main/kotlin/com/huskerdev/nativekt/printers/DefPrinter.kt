package com.huskerdev.nativekt.printers

import java.io.File

class DefPrinter(
    target: File,
    headerFile: File,
    classPath: String,
) {

    init {
        target.parentFile.mkdirs()

        target.writeText("""
            headers = ${headerFile.absolutePath.replace("\\", "/")}
            package = cinterop.$classPath
        """.trimIndent())
    }
}