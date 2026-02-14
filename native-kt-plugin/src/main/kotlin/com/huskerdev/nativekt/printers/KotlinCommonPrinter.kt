package com.huskerdev.nativekt.printers

import com.huskerdev.nativekt.utils.asyncFunctionName
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.syncFunctionName
import com.huskerdev.webidl.resolver.IdlResolver
import java.io.File

class KotlinCommonPrinter(
    idl: IdlResolver,
    target: File,
    classPath: String,
    moduleName: String,
    useCoroutines: Boolean
) {
    init {
        val builder = StringBuilder()

        builder.append("""
            package $classPath
            
            /**
             * Initializes the native library `${moduleName}` synchronously.
             *
             * ##### Kotlin/JVM + Android
             * Loads dynamic library from resources using `System.load(...)`.
             *
             * ##### Kotlin/JS
             * Synchronous loading is not supported!
             *
             * ##### Kotlin/Native
             * Does nothing (yet).
             *
             * @throws UnsupportedOperationException When called in Kotlin/JS
             */
            @Throws(UnsupportedOperationException::class)
            expect fun ${syncFunctionName(moduleName)}()
            
            /**
             * Initializes the native library `${moduleName}` asynchronously.
             *
             * ##### Kotlin/JVM + Android
             * Loads dynamic library from resources using `System.load(...)`.
             *
             * ##### Kotlin/JS
             * Loads the `.wasm` file.
             *
             * ##### Kotlin/Native
             * Does nothing (yet).
             *
             * @param onReady Invoked when the native library is loaded.
             */
            expect fun ${asyncFunctionName(moduleName)}(onReady: () -> Unit)
            
        """.trimIndent())
        if(useCoroutines)
            builder.append("""
                
                /**
                 * Initializes the native library `${moduleName}` asynchronously.
                 *
                 * ##### Kotlin/JVM + Android
                 * Loads dynamic library from resources using `System.load(...)`.
                 *
                 * ##### Kotlin/JS
                 * Loads the `.wasm` file.
                 *
                 * ##### Kotlin/Native
                 * Does nothing (yet).
                 */
                expect suspend fun ${asyncFunctionName(moduleName)}()
                
            """.trimIndent())

        builder.append("""
            
            /**
             * Indicates when library `test` is loaded
             */
            expect val isLibTestLoaded: Boolean
            
            /* ================== *\
                    Functions
            \* ================== */
            
        """.trimIndent())

        idl.globalOperators().forEach {
            builder.append("\n")
            printFunctionHeader(builder, it, isExpect = true)
        }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }
}