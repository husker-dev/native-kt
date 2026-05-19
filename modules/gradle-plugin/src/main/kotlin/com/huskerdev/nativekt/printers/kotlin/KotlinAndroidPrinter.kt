package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.printers.kotlin.jvm.KotlinJvmJniPrinter
import com.huskerdev.nativekt.printers.kotlin.jvm.toKotlinCriticalType
import com.huskerdev.nativekt.printers.kotlin.jvm.toNativeCriticalType
import com.huskerdev.nativekt.utils.*
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
    val expectActual: Boolean,
    val isAndroidCriticalEnabled: Boolean
) {
    val jniClassName = "${moduleName.capitalized()}JNI"

    init {
        val actual = if(expectActual) "actual " else ""

        val builder = StringBuilder()
        builder.append("""
            @file:Suppress("unchecked_cast")
            
            package $classPath
            
        """.trimIndent())

        if(isAndroidCriticalEnabled)
            builder.append("""
                
                import android.os.Build
                import dalvik.annotation.optimization.*
                
                
                private val supportsCritical = Build.VERSION.SDK_INT >= 26
                
            """.trimIndent())

        builder.append("""
            
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
        KotlinJvmJniPrinter(idl, builder,
            name = jniClassName,
            parentClass = null,
            isAndroid = true,
            isAndroidCriticalEnabled = isAndroidCriticalEnabled
        )

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function, isActual = expectActual)
        append(" = \n\t")

        if(isAndroidCriticalEnabled && function.isCritical() && function.isAndroidCriticalCapable()) {
            val args = function.args.joinToString {
                toNativeCriticalType(it.type, it.name)
            }
            val call = "$jniClassName.${function.name}_($args)"

            val castedCall = toKotlinCriticalType(function.type, call)
            append("if(supportsCritical) $castedCall\n\telse ")
        }

        val args = function.args.joinToString { it.name }

        append("$jniClassName.${function.name}($args)")
        append("\n")
    }
}