package com.huskerdev.nativekt.printers

import com.huskerdev.nativekt.printers.jvm.KotlinJvmJniPrinter
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import java.io.File

class KotlinAndroidPrinter(
    idl: IdlResolver,
    target: File,
    classPath: String,
    moduleName: String,
    useCoroutines: Boolean
) {
    init {
        val builder = StringBuilder()

        builder.append("""
            package $classPath
            
            @Throws(UnsupportedOperationException::class)
            actual fun ${syncFunctionName(moduleName)}() {
                System.loadLibrary("$moduleName")
            }
            
            actual fun ${asyncFunctionName(moduleName)}(onReady: () -> Unit) {
                ${syncFunctionName(moduleName)}()
                onReady()
            }
            
        """.trimIndent())

        if(useCoroutines) {
            builder.append("""
                
                actual suspend fun ${asyncFunctionName(moduleName)}() =
                    ${syncFunctionName(moduleName)}()
                
            """.trimIndent())
        }

        idl.globalOperators().forEach { printFunction(builder, it) }

        builder.append("\n\n")
        KotlinJvmJniPrinter(idl, builder, parentClass = null, instanceMethods = false)

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function, isActual = true)
        append(" = \n\t")

        append("JNI.")
        append(function.name)
        append("(")
        function.args.joinTo(this) { it.name }
        append(")\n")
    }
}