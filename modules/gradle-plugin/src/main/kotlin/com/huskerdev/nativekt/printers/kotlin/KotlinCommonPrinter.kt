package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.plugin.NativeKtJvmInterface
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.ResolvedIdlField
import java.io.File

class KotlinCommonPrinter(
    val context: NativeModuleContext,
    target: File
) {
    init {
        target.parentFile.mkdirs()
        target.writeText(buildString {
            printHeader()
            printEnums()
            printDictionaries()
            printInterface()
            printCallbacks()
            printOperations()
        })
    }

    private fun StringBuilder.printHeader() {
        append("""
            @file:OptIn(ExperimentalUnsignedTypes::class)
            
            package ${context.classPath}
            
            /**
             * Initializes the native library `${context.moduleName}` synchronously.
             * @throws UnsupportedOperationException When called in Kotlin/JS
             */
            @Throws(UnsupportedOperationException::class)
            expect fun ${syncLoadFunctionName(context)}()
            
            /**
             * Initializes the native library `${context.moduleName}` asynchronously.
             * @param onReady Invoked when the native library is loaded.
             */
            expect fun ${asyncLoadFunctionName(context)}(onReady: () -> Unit)
            
        """.trimIndent())
        if(context.extension.useCoroutines) append("""
            
            /**
             * Initializes the native library `${context.moduleName}` asynchronously.
             */
            expect suspend fun ${asyncLoadFunctionName(context)}()
            
        """.trimIndent())

        append("""
            
            /**
             * Indicates when library `${context.moduleName}` is loaded
             */
            expect val ${loadFieldName(context)}: Boolean
            
        """.trimIndent())
    }

    private fun StringBuilder.printOperations() {
        if(context.globalOperations.isEmpty())
            return

        printLabel("Functions")
        context.globalOperations.forEach {
            append("\n")
            printFunctionHeader(this, it, isExpect = true)
        }
    }

    private fun StringBuilder.printCallbacks() {
        if(context.callbacks.isEmpty())
            return

        printLabel("Callbacks")
        val maxLength = context.callbacks.maxOf { it.kname.length }

        context.callbacks.forEach { callback ->
            val name = callback.kname
            val spaces = " ".repeat(maxLength - name.length)
            val args = callback.args.joinToString {
                "${it.kname}: ${it.type.toKotlinType()}"
            }
            val type = if(!callback.type.isVoid())
                ": ${callback.type.toKotlinType()}"
            else ""

            append("\nfun interface $name$spaces { operator fun invoke($args)$type }")
        }
        append("\n")
    }

    private fun StringBuilder.printEnums() {
        if(context.enums.isEmpty())
            return

        printLabel("Enums")
        context.enums.forEach { enum ->
            append("\nenum class ${enum.kname} {")
            enum.elements.joinTo(this, separator = ",") { "\n\t$it" }
            append("\n}\n")
        }
    }

    private fun StringBuilder.printInterface() {
        if(context.interfaces.isEmpty())
            return

        printLabel("Interfaces")
        context.interfaces.forEach { inter ->
            append("\nexpect class ${inter.kname}")

            listOfNotNull(
                inter.implements?.kname,
                "AutoCloseable"
            ).joinTo(this, prefix = ": ", postfix = " {")

            inter.constructors.forEach { constructor ->
                append("\n\tconstructor(")
                constructor.args.joinTo(this) {
                    "${it.kname}: ${it.type.toKotlinType()}"
                }
                append(")")
            }
            inter.operations.forEach { operation ->
                append("\n\tfun ${operation.kname}(")
                operation.args.joinTo(this) {
                    "${it.kname}: ${it.type.toKotlinType()}"
                }
                append(")")
                if (!operation.type.isVoid())
                    append(": ${operation.type.toKotlinType()}")
            }
            append("\n\toverride fun close()")
            append("\n}\n")
        }
    }

    private fun StringBuilder.printDictionaries() {
        if(context.dictionaries.isEmpty())
            return

        printLabel("Dictionaries")
        context.dictionaries.forEach { dictionary ->
            val name = dictionary.kname
            val parent = if (dictionary.implements != null)
                ": ${dictionary.implements!!.kname}"
            else ""

            val fields = dictionary.allFields()
            val args = fields.joinToString {
                "${it.kname}: ${it.type.toKotlinType()}"
            }
            val argNames = fields.joinToString { it.kname }

            // Print

            append("\ninterface $name$parent {")

            // Interface fields
            dictionary.fields.joinTo(this, separator = "") {
                "\n\tval ${it.kname}: ${it.type.toKotlinType()}"
            }

            // Companion
            append("""
                
                
                companion object {
                    @kotlin.jvm.JvmStatic
                    @kotlin.jvm.JvmName("of")
                    operator fun invoke($args): ${dictionary.kname} = 
                        Impl($argNames)
                }
            """.replaceIndent("\t"))

            // Impl (data class)
            if ((context.extension as? NativeKtJvmInterface)?.useJvmRecord ?: false)
                append("\n\t@kotlin.jvm.JvmRecord")

            if(fields.isNotEmpty()) {
                append("\n\tdata class Impl(")
                fields.joinTo(this, separator = ",") { field ->
                    "\n\t\toverride val ${field.kname}: ${field.type.toKotlinType()}"
                }
                append("\n\t): ${dictionary.kname}")
            } else append("\n\tclass Impl: ${dictionary.kname}")

            if (fields.any { it.type.isArray() }) {
                fun hashFunc(field: ResolvedIdlField.Declaration) = if (field.type.isArray())
                    "${field.kname}.contentHashCode()"
                else "${field.kname}.hashCode()"

                fun equalFunc(field: ResolvedIdlField.Declaration) = if (field.type.isArray())
                    "if (!${field.kname}.contentEquals(other.${field.kname})) return false"
                else "if (${field.kname} != other.${field.kname}) return false"

                append(" {\n")

                // equals
                append("""
                    override fun equals(other: Any?): Boolean {
                        if (this === other) return true
                        if (other == null || other !is Impl) return false
                        
                """.replaceIndent("\t\t"))
                fields.joinTo(this, separator = "") {
                    "\n\t\t\t${equalFunc(it)}"
                }
                append("\n\t\t\treturn true\n\t\t}\n\n")

                // hashCode
                append("""
                    override fun hashCode(): Int {
                        var result = ${hashFunc(fields[0])}
                """.replaceIndent("\t\t"))
                fields.drop(1).joinTo(this, separator = "") {
                    "\n\t\t\tresult = 31 * result + ${hashFunc(it)}"
                }
                append("\n\t\t\treturn result\n\t\t}")
                append("\n\t}")
            }

            append("\n}\n")
        }
    }
}