package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.printers.kotlin.jvm.KotlinJvmCIPrinter
import com.huskerdev.nativekt.printers.kotlin.jvm.KotlinJvmForeignPrinter
import com.huskerdev.nativekt.printers.kotlin.jvm.KotlinJvmJniPrinter
import com.huskerdev.nativekt.utils.asyncLoadFunctionName
import com.huskerdev.nativekt.utils.camelCase
import com.huskerdev.nativekt.utils.functionHeader
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.syncLoadFunctionName
import com.huskerdev.webidl.resolver.IdlResolver
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
    init {
        val builder = StringBuilder()
        val actual = if (expectActual) "actual " else ""
        val nativeInvoker = "${moduleName.capitalized()}NativeInvoker"
        val implName = "${moduleName}Impl"

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

        // Functions
        builder.append("\n\n// === Functions ===\n")
        idl.globalOperators().forEach { printFunctionProxy(builder, it, implName) }

        // Implementation
        builder.append("\n\n// === Implementation ===\n\n")
        builder.append("""
            private var $implName: $nativeInvoker? = null
            
            private sealed interface $nativeInvoker {
                fun _address(name: String): Long
                
        """.trimIndent())

        idl.globalOperators().joinTo(builder, "\n\t") {
            functionHeader(it, name = it.name)
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

    private fun printFunctionProxy(builder: StringBuilder, function: ResolvedIdlOperation, implName: String) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function, isActual = expectActual, forcePrintVoid = true)
        append(" = \n\t$implName!!.")
        append(function.name.camelCase())
        function.args.joinTo(this, prefix = "(", postfix = ")\n") { it.name.camelCase() }
    }
}