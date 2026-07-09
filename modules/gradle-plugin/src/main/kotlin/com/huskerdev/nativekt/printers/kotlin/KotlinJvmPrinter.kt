package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.printers.kotlin.jvm.KotlinJvmCIPrinter
import com.huskerdev.nativekt.printers.kotlin.jvm.KotlinJvmForeignPrinter
import com.huskerdev.nativekt.printers.kotlin.jvm.KotlinJvmJniPrinter
import com.huskerdev.nativekt.utils.asyncLoadFunctionName
import com.huskerdev.nativekt.utils.camelCase
import com.huskerdev.nativekt.utils.functionHeader
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.interfaceConstructorCName
import com.huskerdev.nativekt.utils.interfaceFreeCName
import com.huskerdev.nativekt.utils.interfaceOperationCName
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.syncLoadFunctionName
import com.huskerdev.nativekt.utils.toKotlinType
import com.huskerdev.nativekt.utils.toOperations
import com.huskerdev.nativekt.utils.upperCamelCase
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlInterface
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File

class KotlinJvmPrinter(
    idl: IdlResolver,
    target: File,
    classPath: String,
    moduleName: String,
    useCoroutines: Boolean,
    val expectActual: Boolean,
    useJNI: Boolean,
    useForeignApi: Boolean,
    useJVMCI: Boolean,
    useUniversalMacOSLib: Boolean
) {
    private val implName = "${moduleName}Impl"

    init {
        val builder = StringBuilder()
        val actual = if (expectActual) "actual " else ""
        val nativeInvoker = "${moduleName.capitalized()}NativeInvoker"

        fun invokerChooser(indent: String) = when {
            useForeignApi && useJNI -> """
                $implName = when(NativeKtUtils.getInvoker()) {
                    NativeKtUtils.Invoker.FOREIGN -> ${moduleName.capitalized()}Foreign(libraryPath)
                    NativeKtUtils.Invoker.JNI     -> ${moduleName.capitalized()}JNI(libraryPath)
                }
            """.replaceIndent(indent)
            useJNI -> """
                $implName = ${moduleName.capitalized()}JNI(libraryPath)
            """.replaceIndent(indent)
            useForeignApi -> """
                $implName = ${moduleName.capitalized()}Foreign(libraryPath)
            """.replaceIndent(indent)
           else -> ""
        }

        builder.append("""
            @file:Suppress("unused", "unchecked_cast")
            @file:OptIn(ExperimentalUnsignedTypes::class)
            
            package $classPath
            
            
        """.trimIndent())

        if(useJVMCI)
            builder.append("import com.huskerdev.nativekt.jvm.jvmci.*\n")
        if(useForeignApi)
            builder.append("""
                import com.huskerdev.nativekt.jvm.foreign.*
                import java.lang.foreign.*
                import java.lang.invoke.*
                
            """.trimIndent())

        val isLibLoadedField = "isLib${moduleName.capitalized()}Loaded"

        builder.append($$"""
            import com.huskerdev.nativekt.jvm.*
            
            
            private var _$$isLibLoadedField = false
            
            $${actual}val $$isLibLoadedField: Boolean
                get() = _$$isLibLoadedField
            
            @Throws(UnsupportedOperationException::class)
            $${actual}fun $${syncLoadFunctionName(moduleName)}() {
                if(_$$isLibLoadedField) return
                _$$isLibLoadedField = true
                
                val libraryPath = NativeKtUtils.resolveLibraryFile("$$moduleName", $$useUniversalMacOSLib)


        """.trimIndent())

        builder.append(invokerChooser("    "))
        if(useJVMCI) {
            if(useJNI || useForeignApi) {
                builder.append("""
                
                    if(NativeKtUtils.isJVMCIAvailable()) 
                        $implName = ${moduleName.capitalized()}JVMCI(libraryPath, $implName!!)
                """.replaceIndent("\t"))
            } else {
                builder.append("""
                    $implName = ${moduleName.capitalized()}JVMCI(libraryPath)
                """.replaceIndent("\t"))
            }
        }
        builder.append("""
            
            }
            
            ${actual}fun ${asyncLoadFunctionName(moduleName)}(onReady: () -> Unit) {
                ${syncLoadFunctionName(moduleName)}()
                onReady()
            }
        """.trimIndent())

        if(useCoroutines) builder.append("""
            
            
            ${actual}suspend fun ${asyncLoadFunctionName(moduleName)}() =
                ${syncLoadFunctionName(moduleName)}()
        """.trimIndent())

        if(idl.interfaces.isNotEmpty()) {
            builder.append("\n\n// === Interfaces ===\n")
            idl.interfaces.values.forEach {
                printInterface(builder, it)
            }
        }

        // Functions
        builder.append("\n\n// === Functions ===\n")
        idl.globalOperators().forEach { printFunctionProxy(builder, it) }
        idl.interfaces.values
            .flatMap { it.toOperations() }
            .forEach { printFunctionProxy(builder, it, true) }

        // Implementation
        builder.append("\n\n// === Implementation ===\n\n")
        builder.append("""
            private var $implName: $nativeInvoker? = null
            
            private sealed interface $nativeInvoker {
                fun _address(name: String): Long
                
        """.trimIndent())

        listOf(
            // Default functions
            *idl.globalOperators()
                .map { it to false }.toTypedArray(),

            // Interface functions
            *idl.interfaces.values
                .flatMap { it.toOperations() }
                .map { it to true }.toTypedArray()
        ).joinTo(builder, "\n\t") {
            functionHeader(it.first, name = (if(it.second) "_" else "") + it.first.name.camelCase())
        }
        builder.append("\n}")

        // JNI
        if(useJNI) {
            builder.append("\n\n")
            KotlinJvmJniPrinter(
                idl, builder,
                name = "${moduleName.capitalized()}JNI",
                parentClass = nativeInvoker,
                isAndroid = false,
                isAndroidCriticalEnabled = false
            )
        }

        // Foreign
        if(useForeignApi) {
            builder.append("\n\n")
            KotlinJvmForeignPrinter(
                idl, builder,
                classPath = classPath,
                moduleName = moduleName,
                name = "${moduleName.capitalized()}Foreign",
                parentClass = nativeInvoker,
            )
        }

        // JVMCI
        if(useJVMCI) {
            builder.append("\n\n")
            KotlinJvmCIPrinter(
                idl, builder,
                implementFields = !useJNI && !useForeignApi,
                classPath = classPath,
                moduleName = moduleName,
                name = "${moduleName.capitalized()}JVMCI",
                parentClass = nativeInvoker,
            )
        }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printFunctionProxy(
        builder: StringBuilder,
        function: ResolvedIdlOperation,
        isInterfaceFunction: Boolean = false
    ) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function,
            name = (if(isInterfaceFunction) "_" else "") + function.name.camelCase(),
            isActual = expectActual && !isInterfaceFunction,
            isPrivate = isInterfaceFunction,
            forcePrintVoid = true
        )
        append(" = \n\t$implName!!.")
        if(isInterfaceFunction)
            append("_")
        append(function.name.camelCase())
        function.args.joinTo(this, prefix = "(", postfix = ")\n") { it.name.camelCase() }
    }

    private fun printInterface(builder: StringBuilder, inter: ResolvedIdlInterface) = builder.apply {
        val name = inter.name.upperCamelCase()
        append("""
            
            actual class $name(_ptr: Long): NativeKtResourceJvm(_ptr) {
                companion object {
                    @JvmStatic fun _wrap(ptr: Long): $name? = 
                        if(ptr == 0L) null else $name(ptr)
                }
                
        """.trimIndent())

        if(inter.constructors.size == 1) {
            val constructor = inter.constructors[0]
            val nativeFunc = "_" + interfaceConstructorCName(inter, constructor).camelCase()

            append("\n\tactual constructor(")
            constructor.args.joinTo(this) {
                "${it.name.camelCase()}: ${it.type.toKotlinType()}"
            }
            append("): this($nativeFunc(")
            constructor.args.joinTo(this) { it.name }
            append(")._ptr)")
        }
        inter.operations.forEach { operation ->
            val nativeFunc = "_" + interfaceOperationCName(inter, operation).camelCase()

            append("\n\tactual fun ${operation.name.camelCase()}(")
            operation.args.joinTo(this) {
                "${it.name.camelCase()}: ${it.type.toKotlinType()}"
            }
            append(") = $nativeFunc(")
            buildList {
                add("this")
                operation.args.mapTo(this) { it.name }
            }.joinTo(this)
            append(")")
        }
        append("""
            
            
                override fun _close() = _${interfaceFreeCName(inter).camelCase()}(this)
            }
            
        """.trimIndent())
    }

}