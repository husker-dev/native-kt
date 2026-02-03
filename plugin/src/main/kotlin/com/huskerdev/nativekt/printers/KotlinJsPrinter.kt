package com.huskerdev.nativekt.printers

import com.huskerdev.nativekt.utils.asyncFunctionName
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.isString
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.syncFunctionName
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlField
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import java.io.File

class KotlinJsPrinter(
    idl: IdlResolver,
    target: File,
    classPath: String,
    moduleName: String,
    useCoroutines: Boolean,
    val expectActual: Boolean
) {
    private val fileName = "./lib${moduleName}.js"

    init {
        val actual = if(expectActual) "actual " else ""

        val builder = StringBuilder()
        builder.append("""
            package $classPath
            
            import kotlin.js.*
            ${
                if(useCoroutines) "import kotlinx.coroutines.await"
                else ""
            }
            
            @JsModule("$fileName")
            private external val _lib: dynamic
            private var _module: dynamic = null
            
            ${actual}val isLibTestLoaded: Boolean
                get() = _module != null
            
            ${actual}fun ${syncFunctionName(moduleName)}(): Unit = 
                throw UnsupportedOperationException("Synchronous library loading is not supported in JS")
                
            ${actual}fun ${asyncFunctionName(moduleName)}(onReady: () -> Unit) {
                if(_module != null) 
                    return
                    
                (_lib.default() as Promise<dynamic>).then { it: dynamic ->
                    _module = it
                    onReady()
                }
            }
            
        """.trimIndent())

        if(useCoroutines) {
            builder.append("""
                
                ${actual}suspend fun ${asyncFunctionName(moduleName)}() {
                    if(_module == null)
                        _module = (_lib.default() as Promise<dynamic>).await()
                }
                
            """.trimIndent())
        }

        idl.globalOperators().forEach { printFunction(builder, it) }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function, isActual = expectActual)
        append(" = \n\t")

        val func = "_module._${function.name}"
        append("$func(${castArgs(function.args)})")

        append("\n")
    }

    private fun castArgs(args: List<ResolvedIdlField.Argument>): String {
        return args.flatMap { arg ->
            if(arg.type.isString()) {
                listOf(arg.name, "${arg.name}.length")
            } else
                listOf(arg.name)
        }.joinToString()
    }
}