package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.utils.asyncLoadFunctionName
import com.huskerdev.nativekt.utils.loadFieldName
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.syncLoadFunctionName
import java.io.File

class KotlinStubPrinter(
    context: NativeModuleContext,
    target: File
) {
    init {
        val builder = StringBuilder()
        builder.append("""
            package ${context.classPath}
            
            actual val ${loadFieldName(context)}: Boolean = false
            
            actual fun ${syncLoadFunctionName(context)}() = Unit
            actual fun ${asyncLoadFunctionName(context)}(onReady: () -> Unit) = Unit
            
        """.trimIndent())

        if(context.extension.useCoroutines)
            builder.append("actual suspend fun ${asyncLoadFunctionName(context)}() = Unit")

        context.globalOperations.forEach {
            builder.append("\n")
            printFunctionHeader(builder, it)
            builder.append("{\n\t// TODO\n}\n")
        }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

}