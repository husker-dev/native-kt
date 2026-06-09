package com.huskerdev.nativekt.printers.c

import com.huskerdev.webidl.resolver.IdlResolver
import java.io.File

class CEmscriptenPrinter(
    val idl: IdlResolver,
    target: File
) {
    init {
        val builder = StringBuilder()
        builder.append("#include \"api.h\"\n")
        builder.append("#include <emscripten.h>\n")

        buildList {
            addAll(listOf(
                "KString",
                "KCharArray",
                "KBooleanArray",
                "KByteArray",
                "KShortArray",
                "KIntArray",
                "KLongArray",
                "KFloatArray",
                "KDoubleArray"
            ))
            idl.dictionaries.values.mapTo(this) { it.name }
        }.joinTo(builder, separator = "") {
            """
                
                EMSCRIPTEN_KEEPALIVE void* ${it}_freeAddr() {
                    return (void*) &${it}_free;
                }
                
            """.trimIndent()
        }
        target.writeText(builder.toString())
    }

}