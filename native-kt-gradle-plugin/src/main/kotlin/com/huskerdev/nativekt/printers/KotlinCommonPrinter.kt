package com.huskerdev.nativekt.printers

import com.huskerdev.nativekt.utils.allFields
import com.huskerdev.nativekt.utils.asyncFunctionName
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.printLabel
import com.huskerdev.nativekt.utils.syncFunctionName
import com.huskerdev.nativekt.utils.toKotlinType
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlCallbackFunction
import com.huskerdev.webidl.resolver.ResolvedIdlDictionary
import com.huskerdev.webidl.resolver.ResolvedIdlEnum
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File

class KotlinCommonPrinter(
    idl: IdlResolver,
    target: File,
    classPath: String,
    moduleName: String,
    useCoroutines: Boolean,
    val useJvmRecord: Boolean
) {
    init {
        val builder = StringBuilder()

        builder.append("""
            package $classPath
            
            /**
             * Initializes the native library `${moduleName}` synchronously.
             * @throws UnsupportedOperationException When called in Kotlin/JS
             */
            @Throws(UnsupportedOperationException::class)
            expect fun ${syncFunctionName(moduleName)}()
            
            /**
             * Initializes the native library `${moduleName}` asynchronously.
             * @param onReady Invoked when the native library is loaded.
             */
            expect fun ${asyncFunctionName(moduleName)}(onReady: () -> Unit)
            
        """.trimIndent())
        if(useCoroutines)
            builder.append("""
                
                /**
                 * Initializes the native library `${moduleName}` asynchronously.
                 */
                expect suspend fun ${asyncFunctionName(moduleName)}()
                
            """.trimIndent())

        builder.append("""
            
            /**
             * Indicates when library `${moduleName}` is loaded
             */
            expect val isLib${moduleName.capitalized()}Loaded: Boolean
            
        """.trimIndent())

        if(idl.enums.isNotEmpty()) {
            printLabel(builder, "Enums")
            idl.enums.values.forEach { printEnum(builder, it) }
        }

        if(idl.dictionaries.isNotEmpty()) {
            printLabel(builder, "Structs")
            idl.dictionaries.values.forEach { printDictionary(builder, it) }
        }

        if(idl.callbacks.isNotEmpty()) {
            printLabel(builder, "Callbacks")
            val maxLength = idl.callbacks.values.maxOf { it.name.length }

            idl.callbacks.values.forEach { printCallback(builder, maxLength, it) }
            builder.append("\n")
        }

        printLabel(builder, "Functions")
        idl.globalOperators().forEach {
            builder.append("\n")
            printFunctionHeader(builder, it, isExpect = true)
        }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printCallback(builder: StringBuilder, maxLength: Int, callbackFunction: ResolvedIdlCallbackFunction) = builder.apply {
        // typealias TestCallback = (status: Int) -> Unit

        append("\ntypealias ")
        append(callbackFunction.name)
        append(" ".repeat(maxLength - callbackFunction.name.length))
        append(" = (")

        callbackFunction.args.joinTo(builder) {
            "${it.name}: ${it.type.toKotlinType()}"
        }
        append(") -> ")
        append(callbackFunction.type.toKotlinType())

    }

    private fun printEnum(builder: StringBuilder, callbackFunction: ResolvedIdlEnum) = builder.apply {
        append("\nenum class ")
        append(callbackFunction.name)
        append(" {\n\t")

        callbackFunction.elements.joinTo(builder, separator = ",\n\t")

        append("\n}\n")
    }

    private fun printDictionary(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        append("\ninterface ")
        append(dictionary.name)
        if(dictionary.implements != null)
            append(": ").append(dictionary.implements!!.name)
        append(" {\n\t")

        // Interface fields
        dictionary.fields.joinTo(builder, separator = "\n\t") { field ->
            "val ${field.name}: ${field.type.toKotlinType()}"
        }

        // Companion
        append("\n\n\tcompanion object {\n\t\t")
        append("@kotlin.jvm.JvmStatic\n\t\t")
        append("@kotlin.jvm.JvmName(\"of\")\n\t\t")
        append("operator fun invoke(")
        dictionary.allFields().joinTo(builder) { field ->
            "${field.name}: ${field.type.toKotlinType()}"
        }
        append("): ")
        append(dictionary.name)
        append(" =\n\t\t\t")
        append("Impl(")
        dictionary.allFields().joinTo(builder) { field -> field.name }
        append(")")
        append("\n\t}\n")

        // Impl (data class)
        if(useJvmRecord)
            append("\n\t@kotlin.jvm.JvmRecord")
        append("\n\tdata class Impl(\n\t\t")
        dictionary.allFields().joinTo(builder, separator = ",\n\t\t") { field ->
            "override val ${field.name}: ${field.type.toKotlinType()}"
        }
        append("\n\t): ")
        append(dictionary.name)
        append("\n}\n")
    }
}