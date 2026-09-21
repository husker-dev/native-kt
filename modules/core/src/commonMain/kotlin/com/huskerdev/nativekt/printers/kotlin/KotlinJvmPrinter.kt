package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.kotlin.jvm.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*
import io.github.vinceglb.filekit.*

class KotlinJvmPrinter(
    val context: NativeModuleContext,
    target: PlatformFile,
    val expectActual: Boolean
) {
    private val moduleName = context.moduleName
    private val extension = context.configuration as NativeKtJvmConfiguration

    private val useJNI = extension.useJNI
    private val useForeignApi = extension.useForeignApi
    private val useJVMCI = extension.useJVMCI

    private val commonInvokerName = "${moduleName.upperCamelCase()}NativeInvoker"
    private val jniInvokerName = "${moduleName.upperCamelCase()}Jni"
    private val foreignInvokerName = "${moduleName.upperCamelCase()}Foreign"
    private val jvmciInvokerName = "${moduleName.upperCamelCase()}Jvmci"

    private val implFieldName = "${moduleName}Impl"

    init {
        target.parent()!!.createDirectories()
        target.writeSync(buildString {
            printHeader()
            printJvmInterfaces(context)
            printFunctions()
            printInvokerInterface()

            // JNI
            if(useJNI) {
                append("\n\n")
                KotlinJvmJniPrinter(
                    context, this,
                    name = jniInvokerName,
                    parentClass = commonInvokerName,
                    isAndroid = false
                )
            }

            // Foreign
            if(useForeignApi) {
                append("\n\n")
                KotlinJvmForeignPrinter(context, this,
                    name = foreignInvokerName,
                    parentClass = commonInvokerName,
                )
            }

            // JVMCI
            if(useJVMCI) {
                append("\n\n")
                KotlinJvmCIPrinter(context, this,
                    name = jvmciInvokerName,
                    parentClass = commonInvokerName,
                    standalone = !useJNI && !useForeignApi,
                )
            }
        })
    }

    private fun StringBuilder.printHeader() {
        val actual = if (expectActual) "actual " else ""

        appendLine("""
            @file:Suppress("SpellCheckingInspection", "LocalVariableName", "FunctionName", "PropertyName", "ObjectPropertyName", "SameParameterValue", "ClassName", "UnsafeDynamicallyLoadedCode")
            @file:OptIn(ExperimentalUnsignedTypes::class, ExperimentalAtomicApi::class)
            
            package ${context.classPath}
            
            import kotlin.concurrent.atomics.*
            import com.huskerdev.nativekt.*
            import com.huskerdev.nativekt.jvm.*
        """.trimIndent())

        if(useJVMCI)
            appendLine("import com.huskerdev.nativekt.jvm.jvmci.*")
        if(useForeignApi)
            appendLine("""
                import com.huskerdev.nativekt.jvm.foreign.*
                import java.lang.foreign.*
            """.trimIndent())

        val isLibLoadedField = loadFieldName(context)

        appendLine($$"""
            
            
            private var _$$isLibLoadedField = false
            
            $${actual}val $$isLibLoadedField: Boolean
                get() = _$$isLibLoadedField
            
            @Throws(UnsupportedOperationException::class)
            $${actual}fun $${syncLoadFunctionName(context)}() {
                if(_$$isLibLoadedField) return
                _$$isLibLoadedField = true
                
                val libraryPath = NativeKtUtils.resolveLibraryFile("$$moduleName", $${extension.useUniversalMacOSLib})

        """.trimIndent())

        append(when {
            useForeignApi && useJNI -> """
                $implFieldName = when(NativeKtUtils.getInvoker()) {
                    NativeKtUtils.Invoker.FOREIGN -> $foreignInvokerName(libraryPath)
                    NativeKtUtils.Invoker.JNI     -> $jniInvokerName(libraryPath)
                }
            """.replaceIndent("\t")
            useJNI -> "\t$implFieldName = $jniInvokerName(libraryPath)"
            useForeignApi -> "\t$implFieldName = $foreignInvokerName(libraryPath)"
            else -> ""
        })
        if(useJVMCI) {
            if(useJNI || useForeignApi) {
                append("""
                
                    if(NativeKtUtils.isJVMCIAvailable()) 
                        $implFieldName = $jvmciInvokerName($implFieldName)
                """.replaceIndent("\t"))
            } else
                append("\t$implFieldName = $jvmciInvokerName(libraryPath)")
        }
        appendLine("""
            
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

    private fun StringBuilder.printInvokerInterface() {
        printLabel("Implementation")

        appendLine("""
            
            private lateinit var $implFieldName: $commonInvokerName
            
            private sealed interface $commonInvokerName {
                fun _address(name: String): Long
        """.trimIndent())

        context.allOperations.forEach { function ->
            val type = when {
                function.type.isVoid() -> ""
                function.isInterfaceOperationConstructor() || function.isInterfaceOperationClone() -> ": Long"
                else -> ": ${function.type.toKotlinType()}"
            }

            val args = function.args.mapIndexed { i, it ->
                if(i == 0 && function.isInterfaceOperation() && !function.isInterfaceOperationConstructor())
                    "${it.kname}: Long"
                else "${it.kname}: ${it.type.toKotlinType()}"
            }.joinToString()

            append("\n\tfun ${function.kname}($args)$type")
        }
        append("\n}")
    }

    private fun StringBuilder.printFunctions() {
        if(context.allOperations.isEmpty())
            return
        printLabel("Functions")

        context.allOperations.forEach { function ->
            val isInterfaceFunction = function.isInterfaceOperation()

            val actual = if(expectActual && !isInterfaceFunction) "actual " else ""
            val private = if(isInterfaceFunction) "private " else ""

            val args = function.args.mapIndexed { i, it ->
                if(i == 0 && function.isInterfaceOperation() && !function.isInterfaceOperationConstructor())
                    "${it.kname}: Long"
                else "${it.kname}: ${it.type.toKotlinType()}"
            }.joinToString()
            val argNames = function.args.joinToString { it.kname }

            append("""
                
                $actual${private}fun ${function.kname}($args) = $implFieldName.${function.kname}($argNames)
            """.trimIndent())
        }
        append("\n")
    }
}

internal fun StringBuilder.printJvmInterfaces(context: NativeModuleContext) {
    if(context.interfaces.isEmpty())
        return
    printLabel("Interfaces")

    context.interfaces.forEach(::printJvmInterface)
}

internal fun StringBuilder.printJvmInterface(inter: ResolvedIdlInterface) {
    val name = inter.kname

    appendLine("""
        
        actual class $name(m: Unit, ptr: Long): NativeKtRcObject(ptr, ::_interface${name}Free) {
            @Suppress("unused")
            private val cleaner = createCleaner(this, releaser) { it.release() }
            override fun _address(): Long = _interface${name}Address(rcPtr)
    """.trimIndent())

    inter.toOperations().forEach { operation ->
        val args = operation.args.map {
            "${it.kname}: ${it.type.toKotlinType()}"
        }
        val argNames = operation.args.map { it.kname }

        append(when {
            operation.isInterfaceOperationConstructor() ->
                "\n\tactual constructor(${args.joinToString()}): this(Unit, ${operation.kname}(${argNames.joinToString()}))"
            operation.isInterfaceOperationFn() -> {
                val name = operation.interfaceFunctionName().camelCase()
                val args = args.drop(1).joinToString()
                val argNames = argNames.toMutableList()
                    .apply { set(0, "rcPtr") }
                    .joinToString()
                "\n\tactual fun $name($args) = ${operation.kname}($argNames)"
            }
            else -> return@forEach
        })
    }
    append("\n}\n")
}