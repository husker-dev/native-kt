package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.parent

class DefPrinter(
    context: NativeModuleContext,
    target: PlatformFile,
    headerFile: PlatformFile,
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

        target.parent()!!.createDirectories()
        target.writeSync(buildString {
            append("headers = ${headerFile.posixPath}\n")
            append("package = cinterop.${context.classPath}\n")
            append("noStringConversion = ${stringFunctions.joinToString(" ")}\n")
            if(linkerOpts.isNotEmpty())
                append("linkerOpts = ${linkerOpts.joinToString(" ")}")
        })
    }
}