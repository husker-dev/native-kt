package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized
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
            @file:Suppress("unused", "UNNECESSARY_SAFE_CALL")
            
            package $classPath
            
            import kotlinx.cinterop.*
            import com.huskerdev.nativekt.kn.*
            import platform.posix.*
            
            ${actual}val isLib${moduleName.capitalized()}Loaded: Boolean = true
            
            @Throws(UnsupportedOperationException::class)
            ${actual}fun ${syncLoadFunctionName(moduleName)}() = Unit
            ${actual}fun ${asyncLoadFunctionName(moduleName)}(onReady: () -> Unit) = onReady()
            
        """.trimIndent())
        if(useCoroutines)
            builder.append("${actual}suspend fun ${asyncLoadFunctionName(moduleName)}() = Unit\n")

        if(idl.dictionaries.isNotEmpty()) {
            printLabel(builder, "Dictionary")
            idl.dictionaries.values.forEach { printDictionaryCasts(builder, it) }
        }

        if(idl.callbacks.isNotEmpty()) {
            printLabel(builder, "Callbacks")
            idl.callbacks.values.forEach { printCallbackWrap(builder, it) }
        }

        if(idl.globalOperators().isNotEmpty()) {
            printLabel(builder, "Functions")
            idl.globalOperators().forEach { printFunction(builder, it) }
        }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printDictionaryCasts(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {

        // free handle
        append("\nprivate val _handle${dictionary.name}Free = staticCFunction<COpaquePointer?, Unit> {")
        append("\n\t$cinteropPath.${dictionary.name}_free(it!!.reinterpret())")
        append("\n}\n")

        // native (arena)

        append("\nprivate fun MemScope.toNative${dictionary.name}OnArena(of: ${dictionary.name}): CPointer<$cinteropPath.${dictionary.name}> {")
        append("\n\tval mem = alloc<$cinteropPath.${dictionary.name}>()")

        dictionary.allFields().forEach {
            append("\n\tmem.${it.name} = ${castToNative(it.type, "of.${it.name}", useArena = true, pin = false)}")
        }

        append("\n\tmem.__flags = 0")
        append("\n\treturn mem.ptr")
        append("\n}\n")

        // native

        append("\nprivate fun toNative${dictionary.name}(of: ${dictionary.name}): CPointer<$cinteropPath.${dictionary.name}> {")
        append("\n\tval mem = malloc(sizeOf<$cinteropPath.${dictionary.name}>().convert())!!.reinterpret<$cinteropPath.${dictionary.name}>().pointed")

        dictionary.allFields().forEach {
            append("\n\tmem.${it.name} = ${castToNative(it.type, "of.${it.name}", useArena = false, pin = false)}")
        }

        append("\n\tmem.__flags = FLAG_RELEASABLE")
        append("\n\treturn mem.ptr")
        append("\n}\n")

        // kotlin

        append("\nprivate fun toKotlin${dictionary.name}(of: CPointer<$cinteropPath.${dictionary.name}>): ${dictionary.name} = of.pointed.let { mem -> ${dictionary.name}(")

        dictionary.allFields().forEach {
            append("\n\t${it.name} = ${castFromNative(it.type, "mem.${it.name}")},")
        }
        append("\n) }\n")
    }

    private fun printCallbackWrap(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {

        // Header
        append("\nprivate val _invoke${callback.name.capitalized()}: CPointer<CFunction<(")
        buildList {
            add("CPointer<$cinteropPath.${callback.name}>")
            callback.args.mapTo(this) { it.type.toKnType() }
        }.joinTo(builder)
        append(") -> ${callback.type.toKnType()}>> =")

        // staticCFunction
        append("\n\tstaticCFunction { ")
        buildList {
            add("_callback")
            callback.args.mapTo(this) { it.name }
        }.joinTo(builder)
        append(" ->")

        // Call
        val args = callback.args.joinToString {
            castFromNative(it.type, it.name)
        }
        val call = "toKotlinCallback<${callback.name}>(_callback)($args)"
        append("\n\t\t${castToNative(callback.type, call, useArena = false, pin = false)}")

        // End
        append("\n\t}\n")
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {

        val useArena = function.args.any {
            it.type.isString() ||
            it.type.isArray() ||
            it.type.isDictionary() ||
            it.type.isCallback()
        }

        val args = function.args.joinToString {
            castToNative(it.type, it.name, useArena = useArena, pin = function.isCritical())
        }

        val deallocFunc = if(function.isDealloc())
            freeFuncFor(function.type, "_result_native")
        else null

        val call = "$cinteropPath.${function.name}($args)"

        // === Print ===

        append('\n')
        printFunctionHeader(builder, function, isActual = expectActual)

        append(when {
            useArena -> " = memScoped {"
            deallocFunc != null -> " {"
            else -> " = "
        })

        append("\n\t")
        if(deallocFunc != null) {
            append("val _result_native = $call")
            append("\n\t")
            append("val _result_kt = ${castFromNative(function.type, "_result_native")}")
            append("\n\t")
            append(deallocFunc)

            if(function.type !is ResolvedIdlType.Void) {
                append("\n\t")
                if(!useArena)
                    append("return ")
                append("_result_kt")
            }
        } else
            append(castFromNative(function.type, call))

        if(useArena || deallocFunc != null)
            append("\n}")
        append("\n")
    }

    private fun freeFuncFor(
        type: ResolvedIdlType,
        content: String
    ): String? = when {
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() -> "${cinteropPath}.${type.toCType()}Array_free($content?.reinterpret())"
                type.isEnum() -> "${cinteropPath}.KIntArray_free($content?.reinterpret())"
                else -> "${cinteropPath}.KArray_free($content, _handle${type.toCType(ptr = false)}Free)"
            }
        }
        type.isCallback() -> "callbackFree($content?.reinterpret())"
        type.isDictionary() -> "${cinteropPath}.${type.toCType(ptr = false)}_free($content)"
        else -> null
    }

    private fun castFromNative(type: ResolvedIdlType, content: String): String = when {
        type.isChar() -> "$content.toInt().toChar()"
        type.isString() -> "toKotlinKString($content!!.reinterpret())"
        type.isCallback() -> "toKotlinCallback($content!!)"
        type.isEnum() -> "${type.declaration.name}.entries[${content}.ordinal]"
        type.isDictionary() -> "toKotlin${type.declaration.name}(${content}!!)"
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() -> "toKotlin${type.toCType()}Array($content!!.reinterpret())"
                type.isEnum() -> "toKotlinEnumArray<${type.declaration.name}>($content!!.reinterpret())"
                else -> {
                    val fn = castFromNative(type, "").split("(")[0]
                    "toKotlinKArray($content!!.reinterpret(), ::$fn)"
                }
            }
        }
        else -> content
    }

    private fun castToNative(type: ResolvedIdlType, content: String, useArena: Boolean, pin: Boolean): String = when {
        type.isChar() -> "$content.code.toUShort()"
        type.isEnum() -> "${cinteropPath}.${type.declaration.name}.entries[$content.ordinal]"
        type.isString() ->
            if(useArena) "toNativeKStringOnArena($content, $pin).reinterpret()"
            else "toNativeKString($content).reinterpret()"
        type.isCallback() ->
            if(useArena) "toNativeCallbackOnArena($content, _invoke${type.declaration.name.capitalized()}).reinterpret()"
            else "toNativeCallback($content, _invoke${type.declaration.name.capitalized()}).reinterpret()"
        type.isDictionary() ->
            if (useArena) "toNative${type.declaration.name}OnArena($content)"
            else "toNative${type.declaration.name}($content)"
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() ->
                    if (useArena) "toNative${type.toCType()}ArrayOnArena($content, $pin).reinterpret()"
                    else "toNative${type.toCType()}Array($content).reinterpret()"
                type.isEnum() ->
                    if (useArena) "toNativeEnumArrayOnArena($content, $pin).reinterpret()"
                    else "toNativeEnumArray($content).reinterpret()"
                else -> {
                    val fn = castToNative(type, "", useArena, false).split("(")[0]
                    if (useArena) "toNativeKArrayOnArena($content, ::$fn).reinterpret()"
                    else "toNativeKArray($content, ::$fn).reinterpret()"
                }
            }
        }
        else -> content
    }

    private fun ResolvedIdlType.toKnType(): String = when {
        isVoid() -> "Unit"
        else -> {
            val ref = "${cinteropPath}.${toCType(ptr = false)}"
            if(isString() || isArray() || isCallback() || isDictionary())
                "CPointer<$ref>?" else ref
        }
    }
}