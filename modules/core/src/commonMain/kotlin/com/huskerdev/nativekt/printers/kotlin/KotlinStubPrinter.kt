package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.isVoid
import io.github.vinceglb.filekit.*

class KotlinStubPrinter(
    context: NativeModuleContext,
    target: PlatformFile
) {
    init {
        val builder = StringBuilder()
        builder.append("""
            package ${context.classPath}
            
            actual val ${loadFieldName(context)}: Boolean = false
            
            actual fun ${syncLoadFunctionName(context)}() = Unit
            actual fun ${asyncLoadFunctionName(context)}(onReady: () -> Unit) = Unit
            
        """.trimIndent())

        if(context.configuration.useCoroutines)
            builder.append("actual suspend fun ${asyncLoadFunctionName(context)}() = Unit")

        context.operations.forEach { operation ->
            val type = if(!operation.type.isVoid())
                ": ${operation.type.toKotlinType()}" else ""

            val args = operation.args.map {
                "${it.kname}: ${it.type.toKotlinType()}"
            }
            builder.append("\nexpect fun ${operation.kname}($args):$type{\n\t// TODO\n}\n")
        }

        target.parent()!!.createDirectories()
        target.writeSync(builder.toString())
    }

}