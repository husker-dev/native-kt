package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.asyncFunctionName
import com.huskerdev.nativekt.utils.functionHeader
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.syncFunctionName
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import java.io.File

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

        builder.append($$"""
            |package $$classPath
            |
            |import java.lang.foreign.*
            |import java.lang.invoke.*
            |import java.io.File
            |import java.nio.file.*
            |
            |private var _isLibTestLoaded = false
            |
            |$${actual}val isLibTestLoaded: Boolean
            |    get() = _isLibTestLoaded
            |
            |@Throws(UnsupportedOperationException::class)
            |$${actual}fun $${syncFunctionName(moduleName)}() {
            |    if(_isLibTestLoaded) return
            |    _isLibTestLoaded = true
            |    
            |    val macos   = 1
            |    val windows = 2
            |    val linux   = 3
            |    
            |    // Detect OS
            |    val osName = System.getProperty("os.name", "generic").lowercase()
            |    val os = when {
            |        "mac" in osName || "darwin" in osName -> macos
            |        "win" in osName -> windows
            |        "nux" in osName -> linux
            |        else -> throw UnsupportedOperationException("Unsupported OS")
            |    }
            |    
            |    // Get lib extension
            |    val extension = when(os) {
            |        macos   -> "dylib"
            |        windows -> "dll"
            |        else    -> "so"
            |    }
            |    
            |    // Get lib arch
            |    val archName = System.getProperty("os.arch").lowercase()
            |    val arch = when {
            |        os == macos           -> "universal"
            |        archName == "aarch64" -> "arm64"
            |        else                  -> "x86"
            |    }
            |    
            |    // Construct file name
            |    val fileName = "lib$${moduleName}-$arch.$extension"
            |
            |    // Create tmp dir
            |    val tempDir = Files.createTempDirectory("natives-kt").toFile()
            |    val libPath = File(tempDir, fileName)
            |    libPath.deleteOnExit()
            |    tempDir.deleteOnExit()
            |
            |    // Copy lib from resources
            |    (Thread.currentThread().contextClassLoader ?: ClassLoader.getSystemClassLoader())
            |       .getResourceAsStream(fileName)
            |       .use { input ->
            |           if(input == null)
            |               throw NullPointerException("File '$fileName' was not found in resources")
            |           Files.copy(input, libPath.toPath(), StandardCopyOption.REPLACE_EXISTING)
            |       }
            |
            |    // Load library
            |    System.load(libPath.absolutePath.toString())
            |
            |    // Set invoker implementation
            |    impl = try {
            |        Class.forName("java.lang.foreign.SymbolLookup")
            |        JNI()
            |        
            |    } catch (_: Exception) {
            |        Foreign()
            |    }
            |}
            |
            |@Suppress("unused")
            |$${actual}fun $${asyncFunctionName(moduleName)}(onReady: () -> Unit) {
            |    $${syncFunctionName(moduleName)}()
            |    onReady()
            |}
            |$${
                if(useCoroutines) """
                    |${actual}suspend fun ${asyncFunctionName(moduleName)}() =
                    |    ${syncFunctionName(moduleName)}()
                """ else ""
            }
            |    
            |private lateinit var impl: NativeInvoker
            |
            |private sealed interface NativeInvoker {
            |    $${idl.globalOperators().joinToString("\n\t", transform = {
                    functionHeader(it, name = "_${it.name}")
                 })}
            |}
            |
            |// === Functions ===
            |
        """.trimMargin("|"))

        idl.globalOperators().forEach { printFunctionProxy(builder, it) }

        builder.append("\n\n// === Implementation ===\n\n")

        KotlinJvmForeignPrinter(idl, builder)
        builder.append("\n\n")
        KotlinJvmJniPrinter(idl, builder, parentClass = "NativeInvoker", instanceMethods = true)

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printFunctionProxy(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function, isActual = expectActual)
        append(" = \n\timpl._")
        append(function.name)
        function.args.joinTo(this, prefix = "(", postfix = ")\n") { it.name }
    }
}