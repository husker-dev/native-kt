package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.printers.kotlin.jvm.KotlinJvmCIPrinter
import com.huskerdev.nativekt.printers.kotlin.jvm.KotlinJvmForeignPrinter
import com.huskerdev.nativekt.printers.kotlin.jvm.KotlinJvmJniPrinter
import com.huskerdev.nativekt.utils.*
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
            printLabel(builder, "Interfaces")
            idl.interfaces.values.forEach {
                printJvmInterface(builder, it)
            }
        }

        // Functions
        printLabel(builder, "Functions")
        idl.allOperators().forEach { printFunctionProxy(builder, it) }

        // Implementation
        printLabel(builder, "Implementation")
        builder.append("""
            
            private var $implName: $nativeInvoker? = null
            
            private sealed interface $nativeInvoker {
                fun _address(name: String): Long
                
        """.trimIndent())

        idl.allOperators().forEach {
            val isInterfaceConstructor = it.isInterfaceOperationConstructor()

            builder.append("\n\t")
            builder.append(functionHeader(it,
                printType = !isInterfaceConstructor
            ))
            if(isInterfaceConstructor)
                builder.append(": Long")
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
        function: ResolvedIdlOperation
    ) = builder.apply {
        val isInterfaceFunction = function.isInterfaceOperation()
        val isInterfaceConstructor = function.isInterfaceOperationConstructor()

        append('\n')
        printFunctionHeader(builder, function,
            name = function.kname,
            printType = !isInterfaceConstructor,
            isActual = expectActual && !isInterfaceFunction,
            isPrivate = isInterfaceFunction,
            forcePrintVoid = true
        )
        if(isInterfaceConstructor)
            append(": Long")
        append(" = \n\t$implName!!.${function.kname}")
        function.args.joinTo(this, prefix = "(", postfix = ")\n") { it.kname }
    }
}

internal fun printJvmInterface(builder: StringBuilder, inter: ResolvedIdlInterface) = builder.apply {
    val name = inter.kname
    append("""
            
            actual class $name(_ptr: Long): NativeKtResourceJvm(_ptr) {
                companion object {
                    @JvmStatic fun _wrap(ptr: Long): $name? = 
                        if(ptr == 0L) null else $name(ptr)
                }
        """.trimIndent())

    inter.toOperations().forEach { operation ->
        val args = operation.args.map {
            "${it.kname}: ${it.type.toKotlinType()}"
        }
        val argNames = operation.args.map { it.kname }

        append("\n\t")
        append(when {
            operation.isInterfaceOperationConstructor() ->
                "actual constructor(${args.joinToString()}): this(${operation.kname}(${argNames.joinToString()}))"
            operation.isInterfaceOperationFn() -> {
                val name = operation.interfaceFunctionName()
                val args = args.drop(1).joinToString()
                val argNames = argNames.toMutableList()
                    .apply { set(0, "this") }
                    .joinToString()
                "actual fun $name($args) = ${operation.kname}($argNames)"
            }
            operation.isInterfaceOperationFree() ->
                "override fun _close() = ${operation.kname}(this)"
            else -> throw UnsupportedOperationException()
        })
    }
    append("\n}\n")
}