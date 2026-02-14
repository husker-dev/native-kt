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

private const val FORCE_INVOKER_PROPERTY = "nativekt.jvm.forceInvoker"

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

        fun invokerChooser(indent: String) = if(useForeignApi) """
            impl = when(System.getProperties()["$FORCE_INVOKER_PROPERTY"]) {
                "foreign" -> ${moduleName.capitalized()}Foreign()
                "jni"     -> ${moduleName.capitalized()}JNI()
                else -> if(NativeKtUtils.isForeignAvailable())
                    ${moduleName.capitalized()}Foreign()
                else 
                    ${moduleName.capitalized()}JNI()
            } 
            """.replaceIndent(indent)
        else """
            impl = ${moduleName.capitalized()}JNI()
            """.replaceIndent(indent)

        builder.append($$"""
            @file:Suppress("unused")
            package $$classPath
            
            $${if(useJVMCI) "import com.huskerdev.nativekt.jvmci.*" else ""}
            $${if(useForeignApi) "import com.huskerdev.nativekt.foreign.*" else ""}
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
                
                if(System.getProperty("nativekt.jvm.disableJVMCI", "false") != "true" && NativeKtUtils.isJvmciAvailable()) 
                    impl = ${moduleName.capitalized()}JVMCI(fileName, impl)
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
        idl.globalOperators().forEach { printFunctionProxy(builder, it) }

        // Implementation
        builder.append("\n\n// === Implementation ===\n\n")
        builder.append("""
            private lateinit var impl: $nativeInvoker
            
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

    private fun printFunctionProxy(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function, isActual = expectActual, forcePrintVoid = true)
        append(" = \n\timpl._")
        append(function.name)
        function.args.joinTo(this, prefix = "(", postfix = ")\n") { it.name }
    }
}