package com.huskerdev.nativekt.printers

import com.huskerdev.nativekt.printers.jvm.KotlinJvmJniPrinter
import com.huskerdev.nativekt.utils.asyncFunctionName
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.syncFunctionName
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File

class KotlinAndroidPrinter(
    idl: IdlResolver,
    target: File,
    classPath: String,
    moduleName: String,
    useCoroutines: Boolean,
    val expectActual: Boolean
) {
    val jniClassName = "${moduleName.capitalized()}JNI"

    init {
        val actual = if(expectActual) "actual " else ""

        val builder = StringBuilder()
        builder.append("""
            @file:Suppress("unchecked_cast")
            
            package $classPath
            
            private var _isLib${moduleName.capitalized()}Loaded = false

            ${actual}val isLib${moduleName.capitalized()}Loaded: Boolean
                get() = _isLib${moduleName.capitalized()}Loaded
            
            @Throws(UnsupportedOperationException::class)
            ${actual}fun ${syncFunctionName(moduleName)}() {
                if(_isLib${moduleName.capitalized()}Loaded) return
                _isLib${moduleName.capitalized()}Loaded = true
    
                System.loadLibrary("$moduleName")
            }
            
            ${actual}fun ${asyncFunctionName(moduleName)}(onReady: () -> Unit) {
                ${syncFunctionName(moduleName)}()
                onReady()
            }
            
        """.trimIndent())

        if(useCoroutines) {
            builder.append("""
                
                ${actual}suspend fun ${asyncFunctionName(moduleName)}() =
                    ${syncFunctionName(moduleName)}()
                
            """.trimIndent())
        }

        idl.globalOperators().forEach { printFunction(builder, it) }

        builder.append("\n\n")
        KotlinJvmJniPrinter(idl, builder, name = jniClassName, parentClass = null, forAndroid = true)

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function, isActual = expectActual)
        append(" = \n\t")

        val args = function.args.joinToString { it.name }
        val call = "$jniClassName.${function.name}($args)"

        append(call)
        append("\n")
    }
}