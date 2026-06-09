package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.utils.allFields
import com.huskerdev.nativekt.utils.asyncFunctionName
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.isArray
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.printLabel
import com.huskerdev.nativekt.utils.syncFunctionName
import com.huskerdev.nativekt.utils.toKotlinType
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlCallbackFunction
import com.huskerdev.webidl.resolver.ResolvedIdlDictionary
import com.huskerdev.webidl.resolver.ResolvedIdlEnum
import com.huskerdev.webidl.resolver.ResolvedIdlField
import com.huskerdev.webidl.resolver.ResolvedIdlType
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
            printLabel(builder, "Dictionaries")
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
        append("\nfun interface ")
        append(callbackFunction.name)
        append(" ".repeat(maxLength - callbackFunction.name.length))
        append(" { operator fun invoke(")

        callbackFunction.args.joinTo(builder) {
            "${it.name}: ${it.type.toKotlinType()}"
        }
        append(")")
        if(callbackFunction.type !is ResolvedIdlType.Void) {
            append(": ")
            append(callbackFunction.type.toKotlinType())
        }
        append(" }")
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

        if(dictionary.allFields().any { it.type.isArray() }) {
            append(" {\n")
            // equals
            append("""
                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (other == null || other !is Impl) return false
                    
            """.replaceIndent("\t\t"))
            dictionary.allFields().joinTo(builder, separator = "\n\t\t\t") {
                if(it.type.isArray())
                    "if (!${it.name}.contentEquals(other.${it.name})) return false"
                else "if (${it.name} != other.${it.name}) return false"
            }
            append("\n\t\t\treturn true\n\t\t}\n\n")

            // hashCode
            fun hashFunc(field: ResolvedIdlField.Declaration) = if(field.type.isArray())
                "${field.name}.contentHashCode()"
            else "${field.name}.hashCode()"

            append("""
                override fun hashCode(): Int {
                    var result = ${hashFunc(dictionary.allFields()[0])}
                    
            """.replaceIndent("\t\t"))
            dictionary.allFields().drop(1).joinTo(builder, separator = "\n\t\t\t") {
                "result = 31 * result + ${hashFunc(it)}"
            }
            append("\n\t\t\treturn result\n\t\t}")
            append("\n\t}")
        }

        append("\n}\n")
    }
}