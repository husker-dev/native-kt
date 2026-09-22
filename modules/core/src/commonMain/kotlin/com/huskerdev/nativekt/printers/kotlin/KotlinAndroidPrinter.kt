package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.kotlin.jvm.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*
import io.github.vinceglb.filekit.*


class KotlinAndroidPrinter(
    val context: NativeModuleContext,
    target: PlatformFile,
    val expectActual: Boolean
) {
    private val extension = context.configuration as NativeKtAndroidConfiguration
    private val criticalEnabled = extension.useAndroidCriticalNative

    private val jniClassName = "${context.moduleName.upperCamelCase()}JNI"
    private val actual = if(expectActual) "actual " else ""

    init {
        target.parent()!!.createDirectories()
        target.writeSync(buildString {
            printHeader()
            printJvmInterfaces(context)
            printFunction()

            append("\n")
            KotlinJvmJniPrinter(context, this,
                name = jniClassName,
                parentClass = null,
                isAndroid = true
            )
        })
    }

    private fun StringBuilder.printHeader() {
        val isLibLoadedField = loadFieldName(context)

        appendLine("""
            @file:OptIn(ExperimentalUnsignedTypes::class)
            
            package ${context.classPath}
            
            import com.huskerdev.nativekt.*
            import com.huskerdev.nativekt.jvm.*
        """.trimIndent())

        if(extension.useAndroidCriticalNative)
            appendLine("import dalvik.annotation.optimization.*")

        appendLine("""
            
            private lateinit var _instance: $jniClassName
            private var _$isLibLoadedField = false

            ${actual}val $isLibLoadedField: Boolean
                get() = _$isLibLoadedField
            
            @Throws(UnsupportedOperationException::class)
            ${actual}fun ${syncLoadFunctionName(context)}() {
                if(_$isLibLoadedField) return
                _$isLibLoadedField = true
                
                _instance = $jniClassName("${context.moduleName}")
            }
            
            ${actual}fun ${asyncLoadFunctionName(context)}(onReady: () -> Unit) {
                ${syncLoadFunctionName(context)}()
                onReady()
            }
        """.trimIndent())

        if(context.configuration.useCoroutines) appendLine("""
            
            ${actual}suspend fun ${asyncLoadFunctionName(context)}() =
                ${syncLoadFunctionName(context)}()
        """.trimIndent())
    }

    private fun StringBuilder.printFunction() {
        if(context.allOperations.isEmpty())
            return
        printLabel("Functions")

        context.allOperations.forEach { operation ->
            val isCritical = criticalEnabled && operation.isCritical() && operation.isAndroidCriticalCapable()

            val name = operation.kname
            val args = operation.args.joinToString {
                "${it.kname}: ${it.type.toKotlinType()}"
            }
            val argNames = operation.args.joinToString {
                when {
                    isCritical && it.type.isInterface() && !it.type.isRawInterface() ->
                        if(it.type.isNullable) "${it.kname}?.rcPtr ?: 0"
                        else "${it.kname}.rcPtr"
                    isCritical && it.type.isEnum() -> "${it.kname}.ordinal"
                    else -> it.kname
                }
            }

            val modifiers = if(operation.isInterfaceOperation())
                "private " else actual

            val call = "_instance.$name($argNames)"
            val casted = when {
                isCritical && operation.type.isEnum() ->
                    "${(operation.type as ResolvedIdlType.Default).declaration.kname}.entries[$call]"
                isCritical && operation.type.isInterface() && !operation.isInterfaceOperationConstructor() ->
                    "${(operation.type as ResolvedIdlType.Default).declaration.kname}(Unit, $call)"
                else -> call
            }

            append("\n${modifiers}fun $name($args) = $casted")
        }
        append("\n")
    }
}