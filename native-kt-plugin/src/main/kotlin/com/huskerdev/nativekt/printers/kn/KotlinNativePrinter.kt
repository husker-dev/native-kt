package com.huskerdev.nativekt.printers.kn

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import java.io.File

class KotlinNativePrinter(
    idl: IdlResolver,
    target: File,
    classPath: String,
    val moduleName: String,
    useCoroutines: Boolean,
    val expectActual: Boolean
) {
    val cinteropPath = "cinterop.$classPath"

    init {
        val actual = if(expectActual) "actual " else ""

        val builder = StringBuilder()
        builder.append("""
            @file:OptIn(ExperimentalForeignApi::class)
            
            package $classPath
            
            import kotlinx.cinterop.*
            import com.huskerdev.nativekt.kn.*
            
            ${actual}val isLibTestLoaded: Boolean = true
            
            @Throws(UnsupportedOperationException::class)
            ${actual}fun ${syncFunctionName(moduleName)}() = Unit
            ${actual}fun ${asyncFunctionName(moduleName)}(onReady: () -> Unit) = onReady()
            
        """.trimIndent())
        if(useCoroutines)
            builder.append("${actual}suspend fun ${asyncFunctionName(moduleName)}() = Unit\n")

        idl.callbacks.values.forEach { printCallbackWrap(builder, it) }

        idl.globalOperators().forEach { printFunction(builder, it) }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printCallbackWrap(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        // header
        append("\nprivate fun ")
        append(callback.name)
        append(".wrap")
        append(callback.name)
        append("() =\n\t")

        // body
        append("allocStruct<")
        append(cinteropPath)
        append(".")
        append(callback.name)
        append(">().apply {\n\t\t")
        append("val struct = pointed\n\t\t")

        // m =
        append("struct.m = StableRef.create(this@wrap")
        append(callback.name)
        append(").asCPointer()\n\t\t")

        // invoke =
        val args = listOf("callback: CPointer<$cinteropPath.${callback.name}>?") +
                callback.args.map { "${it.name}: ${it.type.toKotlinNativeType()}" }

        val args1 = callback.args.joinToString { castFromNative(it.type, it.name, it.isDealloc(), false) }

        val call = "callback!!.pointed.m!!.asStableRef<${callback.name}>().get()($args1)"

        append("struct.invoke = staticCFunction { ")
        args.joinTo(builder)
        append(" ->\n\t\t\t")
        append(castToNative(callback.type, call, dealloc = false, useArena = false))
        append("\n\t\t}\n\t\t")

        // free =
        append("struct.free = freeCallbackFunction.reinterpret()\n\t}\n")
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function, isActual = expectActual)
        append(" = ")

        val useArena = function.args.any { it.type.isString() || it.isDealloc() }

        if(useArena)
            append("NativeArena.use { arena ->")
        append("\n\t")

        val args = function.args.joinToString { arg ->
            castToNative(arg.type, arg.name, arg.isDealloc(), useArena)
        }

        val call = "$cinteropPath.${function.name}($args)"
        append(castFromNative(function.type, call, function.isDealloc(), useArena))

        if(useArena)
            append("\n}")

        append("\n")
    }

    private fun castFromNative(type: ResolvedIdlType, content: String, dealloc: Boolean, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.CHAR -> "$content.toInt().toChar()"
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "arena.unwrapCStr($content!!, $dealloc)"
                    else "$content!!.unwrapCStr($dealloc)"
                else -> content
            }
            is ResolvedIdlCallbackFunction ->
                if(useArena) "arena.unwrapCallback<${decl.name}>($content!!.reinterpret(), $dealloc)"
                else "unwrapCallback<${decl.name}>($content!!.reinterpret(), $dealloc)"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }

    private fun castToNative(type: ResolvedIdlType, content: String, dealloc: Boolean, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "arena.allocCStr($content)"
                    else "$content.allocCStr()"
                WebIDLBuiltinKind.CHAR -> "$content.code.toUShort()"
                else -> content
            }
            is ResolvedIdlCallbackFunction ->
                if(dealloc) "arena.callback($content.wrap${decl.name}())"
                else "$content.wrap${decl.name}()"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }
}