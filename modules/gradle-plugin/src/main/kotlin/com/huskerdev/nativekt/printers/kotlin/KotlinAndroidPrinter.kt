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
            @file:OptIn(ExperimentalUnsignedTypes::class)
            @file:Suppress("unchecked_cast")
            
            package $classPath
            
        """.trimIndent())

        if(isAndroidCriticalEnabled)
            builder.append("""
                
                import android.os.Build
                import com.huskerdev.nativekt.jvm.*
                import dalvik.annotation.optimization.*
                
                
                private val supportsCritical = Build.VERSION.SDK_INT >= 26
                
            """.trimIndent())

        val isLibLoadedField = "isLib${moduleName.capitalized()}Loaded"

        builder.append("""
            
            private var _$isLibLoadedField = false

            ${actual}val $isLibLoadedField: Boolean
                get() = _$isLibLoadedField
            
            @Throws(UnsupportedOperationException::class)
            ${actual}fun ${syncLoadFunctionName(moduleName)}() {
                if(_$isLibLoadedField) return
                _$isLibLoadedField = true
                
                $jniClassName("$moduleName")
            }
            
            ${actual}fun ${asyncLoadFunctionName(moduleName)}(onReady: () -> Unit) {
                ${syncLoadFunctionName(moduleName)}()
                onReady()
            }
            
        """.trimIndent())

        if(useCoroutines) {
            builder.append("""
                
                ${actual}suspend fun ${asyncLoadFunctionName(moduleName)}() =
                    ${syncLoadFunctionName(moduleName)}()
                
            """.trimIndent())
        }

        idl.allOperators().forEach {
            printFunction(builder, it)
        }

        builder.append("\n\n")
        KotlinJvmJniPrinter(idl, builder,
            name = jniClassName,
            parentClass = null,
            isAndroid = true,
            isAndroidCriticalEnabled = isAndroidCriticalEnabled
        )

        if(idl.interfaces.isNotEmpty()) {
            printLabel(builder, "Interfaces")
            idl.interfaces.values.forEach {
                printJvmInterface(builder, it)
            }
        }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printFunction(
        builder: StringBuilder,
        function: ResolvedIdlOperation
    ) = builder.apply {
        val isInterfaceFunction = function.isInterfaceOperation()
        val isInterfaceConstructor = function.isInterfaceOperationConstructor()

        append('\n')
        if(expectActual && !isInterfaceFunction)
            append("actual ")
        if(isInterfaceFunction)
            append("private ")
        val kArgs = function.args.joinToString {
            "${it.kname}: ${it.type.toKotlinType()}"
        }
        val type = if(!function.type.isVoid()) {
            ": " + if(isInterfaceConstructor)
                "Long"
            else function.type.toKotlinType()
        } else ""

        append("fun ${function.kname}($kArgs)$type = \n\t")

        if(isAndroidCriticalEnabled && function.isCritical() && function.isAndroidCriticalCapable()) {
            val args = function.args.joinToString {
                toNativeCriticalType(it.type, it.kname, ignoreUnsigned = true)
            }
            val call = "$jniClassName.c_${function.kname}($args)"

            val castedCall = toKotlinCriticalType(function.type, call, ignoreUnsigned = true)
            append("if(supportsCritical) $castedCall\n\telse ")
        }

        val args = function.args.joinToString { it.kname }

        append("$jniClassName.${function.kname}($args)")
        append("\n")
    }
}