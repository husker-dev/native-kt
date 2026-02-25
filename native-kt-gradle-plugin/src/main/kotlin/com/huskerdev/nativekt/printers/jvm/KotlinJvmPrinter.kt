package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.asyncFunctionName
import com.huskerdev.nativekt.utils.functionHeader
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.syncFunctionName
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
    useForeignApi: Boolean,
    useJVMCI: Boolean,
    useUniversalMacOSLib: Boolean
) {
    init {
        val builder = StringBuilder()
        val actual = if (expectActual) "actual " else ""
        val nativeInvoker = "${moduleName.capitalized()}NativeInvoker"
        val implName = "${moduleName}Impl"

        fun invokerChooser(indent: String) = if(useForeignApi) """
            $implName = when(NativeKtUtils.getInvoker()) {
                NativeKtUtils.Invoker.FOREIGN -> ${moduleName.capitalized()}Foreign()
                NativeKtUtils.Invoker.JNI     -> ${moduleName.capitalized()}JNI()
            }
            """.replaceIndent(indent)
        else """
            $implName = ${moduleName.capitalized()}JNI()
            """.replaceIndent(indent)

        builder.append("""
            @file:Suppress("unused", "unchecked_cast")
            package $classPath
            
            
        """.trimIndent())

        if(useJVMCI)
            builder.append("import com.huskerdev.nativekt.jvmci.*")
        if(useForeignApi)
            builder.append("""
                
                import com.huskerdev.nativekt.foreign.*
                import java.lang.foreign.*
                import java.lang.invoke.*
                
            """.trimIndent())

        builder.append($$"""
            import com.huskerdev.nativekt.*
            
            
            private var isLibTestLoaded_ = false
            
            $${actual}val isLibTestLoaded: Boolean
                get() = isLibTestLoaded_
            
            @Throws(UnsupportedOperationException::class)
            $${actual}fun $${syncFunctionName(moduleName)}() {
                if(isLibTestLoaded_) return
                isLibTestLoaded_ = true
                
                $${if(useJVMCI) "val fileName = " else ""}NativeKtUtils.loadLibrary("$$moduleName", $$useUniversalMacOSLib)


        """.trimIndent())

        builder.append(invokerChooser("\t"))
        if(useJVMCI) {
            builder.append("""
                
                if(NativeKtUtils.isJvmciAvailable()) 
                    $implName = ${moduleName.capitalized()}JVMCI(fileName, $implName!!)
            """.replaceIndent("\t"))
        }
        builder.append("""
            
            }
            
            ${actual}fun ${asyncFunctionName(moduleName)}(onReady: () -> Unit) {
                ${syncFunctionName(moduleName)}()
                onReady()
            }
        """.trimIndent())

        if(useCoroutines) builder.append("""
            
            
            ${actual}suspend fun ${asyncFunctionName(moduleName)}() =
                ${syncFunctionName(moduleName)}()
        """.trimIndent())

        // Functions
        builder.append("\n\n// === Functions ===\n")
        idl.globalOperators().forEach { printFunctionProxy(builder, it, implName) }

        // Implementation
        builder.append("\n\n// === Implementation ===\n\n")
        builder.append("""
            private var $implName: $nativeInvoker? = null
            
            private sealed interface $nativeInvoker {
                
        """.trimIndent())

        idl.globalOperators().joinTo(builder, "\n\t") {
            functionHeader(it, name = "_${it.name}")
        }
        builder.append("\n}")

        // JNI
        builder.append("\n\n")
        KotlinJvmJniPrinter(idl, builder,
            name = "${moduleName.capitalized()}JNI",
            parentClass = nativeInvoker,
            instanceMethods = true,
        )

        // Foreign
        if(useForeignApi) {
            builder.append("\n\n")
            KotlinJvmForeignPrinter(
                idl, builder,
                classPath = classPath,
                name = "${moduleName.capitalized()}Foreign",
                parentClass = nativeInvoker,
            )
        }

        // JVMCI
        if(useJVMCI) {
            builder.append("\n\n")
            KotlinJvmCIPrinter(
                idl, builder,
                classPath = classPath,
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
        append(" = \n\t$implName!!._")
        append(function.name)
        function.args.joinTo(this, prefix = "(", postfix = ")\n") { it.name }
    }
}