package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.stdlibx.fs.SyncPath
import com.huskerdev.webidl.*

class DefPrinter(
    context: NativeModuleContext,
    target: SyncPath,
    headerFile: SyncPath,
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

        target.parent!!.createDirectories()
        target.writeString(buildString {
            append("headers = ${headerFile.invariantSeparatorsPath}\n")
            append("package = cinterop.${context.classPath}\n")
            append("noStringConversion = ${stringFunctions.joinToString(" ")}\n")
            if(linkerOpts.isNotEmpty())
                append("linkerOpts = ${linkerOpts.joinToString(" ")}")
        })
    }
}