package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.stdlibx.fs.SyncPath

class KotlinStubPrinter(
    val context: NativeModuleContext,
    target: SyncPath
) {
    init {
        target.parent!!.createDirectories()
        target.writeString(buildString {
            appendLine("""
                @file:Suppress("ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT")
                @file:OptIn(ExperimentalUnsignedTypes::class)
                
                package ${context.classPath}
                
                actual val ${loadFieldName(context)}: Boolean = false
                
                actual fun ${syncLoadFunctionName(context)}(): Unit = TODO("Not yet implemented")
                actual fun ${asyncLoadFunctionName(context)}(onReady: () -> Unit): Unit = TODO("Not yet implemented")
            """.trimIndent())

            if(context.module.useCoroutines)
                appendLine("actual suspend fun ${asyncLoadFunctionName(context)}(): Unit = TODO(\"Not yet implemented\")")

            printOperations()
            printDictionaries(context, true, isCommon = false)
            printInterfaces()
        })
    }

    private fun StringBuilder.printOperations() {
        context.operations.forEach { operation ->
            val type = operation.type.toKotlinType()
            val args = operation.args.joinToString {
                "${it.kname}: ${it.type.toKotlinType()}"
            }
            append("\nactual fun ${operation.kname}($args): $type = TODO(\"Not yet implemented\")")
        }
        append("\n")
    }

    private fun StringBuilder.printInterfaces() {
        if(context.castedInterfaces.isEmpty())
            return
        printLabel("Interfaces")

        context.interfaces.forEach { inter ->
            val name = inter.kname

            append("""
                
                actual class $name(dummy: Unit): com.huskerdev.nativekt.NativeKtRcObject(0, {}) {
                    override fun _address(): Long = 0L

            """.trimIndent())

            inter.toOperations().forEach { operation ->
                val args = operation.args.map {
                    "${it.kname}: ${it.type.toKotlinType()}"
                }
                append(when {
                    operation.isInterfaceOperationConstructor() ->
                        "\n\tactual constructor(${args.joinToString()}): this(Unit)"

                    operation.isInterfaceOperationFn() -> {
                        val args = args.drop(1).joinToString()
                        val type = operation.type.toKotlinType()
                        "\n\tactual fun ${operation.interfaceFunctionName().camelCase()}($args): $type = TODO(\"Not yet implemented\")"
                    }
                    else -> return@forEach
                })
            }
            append("\n}\n")
        }
    }
}