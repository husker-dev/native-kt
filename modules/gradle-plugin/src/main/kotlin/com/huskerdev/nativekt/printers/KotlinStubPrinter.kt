package com.huskerdev.nativekt.printers

import com.huskerdev.nativekt.utils.asyncFunctionName
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.syncFunctionName
import com.huskerdev.webidl.resolver.IdlResolver
import java.io.File

class KotlinStubPrinter(
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
            
            actual val isLibTestLoaded: Boolean = false
            
            actual fun ${syncFunctionName(moduleName)}() = Unit
            actual fun ${asyncFunctionName(moduleName)}(onReady: () -> Unit) = Unit
            
        """.trimIndent())

        if(useCoroutines)
            builder.append("actual suspend fun ${asyncFunctionName(moduleName)}() = Unit")

        idl.globalOperators().forEach {
            builder.append("\n")
            printFunctionHeader(builder, it)
            builder.append("{\n\t// TODO\n}\n")
        }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

}