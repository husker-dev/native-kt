package com.huskerdev.nativekt.printers

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.utils.cnameMangled
import com.huskerdev.nativekt.utils.isCritical
import com.huskerdev.nativekt.utils.isString
import com.huskerdev.nativekt.utils.posixPath
import java.io.File

class DefPrinter(
    context: NativeModuleContext,
    target: File,
    headerFile: File,
    linkerOpts: List<String>
) {
    init {
        val stringFunctions = buildList {
            add(context.mangle("string_new"))
            context.allOperations.forEach { func ->
                if(func.isCritical() && func.args.any { it.type.isString() })
                    add(func.cnameMangled(context))
            }
        }

        target.parentFile.mkdirs()
        target.writeText(buildString {
            append("headers = ${headerFile.posixPath}\n")
            append("package = cinterop.${context.classPath}\n")
            append("noStringConversion = ${stringFunctions.joinToString(" ")}\n")
            if(linkerOpts.isNotEmpty())
                append("linkerOpts = ${linkerOpts.joinToString(" ")}")
        })
    }
}