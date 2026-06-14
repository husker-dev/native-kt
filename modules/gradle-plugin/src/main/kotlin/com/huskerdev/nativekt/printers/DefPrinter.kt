package com.huskerdev.nativekt.printers

import com.huskerdev.nativekt.utils.posixPath
import java.io.File

class DefPrinter(
    target: File,
    headerFile: File,
    classPath: String,
    linkerOpts: List<String>
) {

    init {
        target.parentFile.mkdirs()

        target.writeText(buildString {
            append("headers = ${headerFile.posixPath}\n")
            append("package = cinterop.$classPath\n")
            if(linkerOpts.isNotEmpty())
                append("linkerOpts = ${linkerOpts.joinToString(" ")}")
        })
    }
}