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
    val expectActual: Boolean
) {
    init {
        val builder = StringBuilder()
        val actual = if (expectActual) "actual " else ""
        val nativeInvoker = "${moduleName.capitalized()}NativeInvoker"

        builder.append($$"""
            @file:Suppress("unused")
            package $$classPath
            
            import java.lang.foreign.*
            import java.lang.invoke.*
            import java.io.File
            import java.nio.file.*
            
            private var isLibTestLoaded_ = false
            
            $${actual}val isLibTestLoaded: Boolean
                get() = isLibTestLoaded_
            
            @Throws(UnsupportedOperationException::class)
            $${actual}fun $${syncFunctionName(moduleName)}() {
                if(isLibTestLoaded_) return
                isLibTestLoaded_ = true
                
                val macos   = 1
                val windows = 2
                val linux   = 3
                
                // Detect OS
                val osName = System.getProperty("os.name", "generic").lowercase()
                val os = when {
                    "mac" in osName || "darwin" in osName -> macos
                    "win" in osName -> windows
                    "nux" in osName -> linux
                    else -> throw UnsupportedOperationException("Unsupported OS")
                }
                
                // Get lib extension
                val extension = when(os) {
                    macos   -> "dylib"
                    windows -> "dll"
                    else    -> "so"
                }
                
                // Get lib arch
                val archName = System.getProperty("os.arch").lowercase()
                val arch = when {
                    os == macos           -> "universal"
                    archName == "aarch64" -> "arm64"
                    archName == "amd64"   -> "x64"
                    else                  -> "x86"
                }
                
                // Construct file name
                val fileName = "lib$${moduleName}-$arch.$extension"
            
                // Create tmp dir
                val tempDir = Files.createTempDirectory("natives-kt").toFile()
                val libPath = File(tempDir, fileName)
                libPath.deleteOnExit()
                tempDir.deleteOnExit()
            
                // Copy lib from resources
                (Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader())
                   .getResourceAsStream(fileName)
                   .use { input ->
                       if(input == null)
                           throw NullPointerException("File '$fileName' was not found in resources")
                       Files.copy(input, libPath.toPath(), StandardCopyOption.REPLACE_EXISTING)
                   }
            
                // Load library
                System.load(libPath.absolutePath.toString())
            
                // Set invoker implementation
                impl = when(System.getProperties()["$$FORCE_INVOKER_PROPERTY"]) {
                    "foreign" -> $${moduleName.capitalized()}Foreign()
                    "jni"     -> $${moduleName.capitalized()}JNI()
                    else -> try {
                        Class.forName("java.lang.foreign.Linker")
                        $${moduleName.capitalized()}Foreign()
                    } catch (_: ClassNotFoundException) {
                        $${moduleName.capitalized()}JNI()
                    }
                } 
            }
            
            $${actual}fun $${asyncFunctionName(moduleName)}(onReady: () -> Unit) {
                $${syncFunctionName(moduleName)}()
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

        // Foreign
        builder.append("\n\n")
        KotlinJvmForeignPrinter(idl, builder,
            classPath = classPath,
            name = "${moduleName.capitalized()}Foreign",
            parentClass = nativeInvoker,
        )

        // JNI
        builder.append("\n\n")
        KotlinJvmJniPrinter(idl, builder,
            name = "${moduleName.capitalized()}JNI",
            parentClass = nativeInvoker,
            instanceMethods = true,
        )


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