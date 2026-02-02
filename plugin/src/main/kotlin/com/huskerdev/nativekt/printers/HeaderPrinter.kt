package com.huskerdev.nativekt.printers

import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlNamespace
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import java.io.File

class HeaderPrinter(
    idl: IdlResolver,
    target: File,
    guardName: String? = null
) {
    private val defName = "KOTLIN_NATIVE_${guardName}_H"

    init {
        target.parentFile.mkdirs()


        val builder = StringBuilder()

        if(guardName != null) {
            builder.append("#ifndef $defName\n")
            builder.append("#define $defName\n\n")
        }

        builder.append("#include <stdint.h>\n")
        builder.append("#include <stddef.h>\n")
        builder.append("\n")

        idl.namespaces.values.forEach { printNamespace(builder, it) }

        if(guardName != null)
            builder.append("\n\n#endif // $defName")

        target.writeText(builder.toString())
    }

    private fun printNamespace(builder: StringBuilder, namespace: ResolvedIdlNamespace){
        namespace.operations.forEach {
            printFunction(builder, it)
        }
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\n")
        append(function.type.toCType())
        append(" ")
        append(function.name)
        append("(")

        function.args.forEachIndexed { index, arg ->
            append(arg.type.toCType())
            append(" ")
            append(arg.name)

            if(arg.type.isString()) {
                append(", size_t ")
                append(arg.name)
                append("_len")
            }
            if(index != function.args.lastIndex)
                append(", ")
        }
        append(");")
    }




}