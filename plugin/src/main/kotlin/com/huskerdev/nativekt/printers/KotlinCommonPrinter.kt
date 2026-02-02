package com.huskerdev.nativekt.printers

import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlNamespace
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import java.io.File

class KotlinCommonPrinter(
    idl: IdlResolver,
    target: File,
    classPath: String,
    moduleName: String,
    useCoroutines: Boolean
) {
    init {
        val builder = StringBuilder()

        builder.append("package $classPath\n")
        builder.append("\n")

        builder.append("@Throws(UnsupportedOperationException::class)\n")
        builder.append("expect fun ${syncFunctionName(moduleName)}()\n")
        builder.append("expect fun ${asyncFunctionName(moduleName)}(onReady: () -> Unit)\n")

        if(useCoroutines)
            builder.append("expect suspend fun ${asyncFunctionName(moduleName)}()\n")

        idl.namespaces.values.forEach { printNamespace(builder, it) }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printNamespace(builder: StringBuilder, namespace: ResolvedIdlNamespace){
        namespace.operations.forEach {
            printFunction(builder, it)
        }
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\nexpect fun ")
        append(function.name)
        append("(")

        function.args.forEachIndexed { index, arg ->
            append(arg.name)
            append(": ")
            append(arg.type.toKotlinType())

            if(index != function.args.lastIndex)
                append(", ")
        }
        append(")")

        if(function.type !is ResolvedIdlType.Void) {
            append(": ")
            append(function.type.toKotlinType())
        }
        append("\n")
    }
}