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
            ${actual}fun ${syncFunctionName(moduleName)}() = Unit
            ${actual}fun ${asyncFunctionName(moduleName)}(onReady: () -> Unit) = onReady()
            
        """.trimIndent())
        if(useCoroutines)
            builder.append("${actual}suspend fun ${asyncFunctionName(moduleName)}() = Unit\n")

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
    ) = when {
        type.isString() -> "${cinteropPath}.KString_free($content?.reinterpret())"
        type.isArray() -> (type as ResolvedIdlType.Default).firstParam { _, declaration ->
            when (declaration) {
                is BuiltinIdlDeclaration -> "${cinteropPath}.K${declaration.kind.simpleName()}Array_free($content?.reinterpret())"
                is ResolvedIdlEnum -> "${cinteropPath}.KIntArray_free($content?.reinterpret())"
                is ResolvedIdlDictionary -> "${cinteropPath}.KArray_free($content, _handle${declaration.name}Free)"
                else -> throw UnsupportedOperationException(type.toString())
            }
        }
        type.isCallback() -> "callbackFree($content?.reinterpret())"
        type.isDictionary() -> "${cinteropPath}.${(type as ResolvedIdlType.Default).declaration.name.capitalized()}_free($content)"
        else -> null
    }

    private fun castFromNative(type: ResolvedIdlType, content: String): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.CHAR -> "$content.toInt().toChar()"
                WebIDLBuiltinKind.STRING -> "toKotlinString($content!!.reinterpret())"
                WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                    when (declaration) {
                        is BuiltinIdlDeclaration -> "toKotlin${declaration.kind.simpleName()}Array($content!!.reinterpret())"
                        is ResolvedIdlEnum -> "toKotlinEnumArray<${declaration.name}>($content!!.reinterpret())"
                        is ResolvedIdlDictionary -> "toKotlinArray($content!!.reinterpret(), ::toKotlin${declaration.name})"
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlCallbackFunction -> "toKotlinCallback($content!!)"
            is ResolvedIdlEnum -> "${decl.name}.entries[${content}.ordinal]"
            is ResolvedIdlDictionary -> "toKotlin${decl.name}(${content}!!)"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }

    private fun castToNative(type: ResolvedIdlType, content: String, useArena: Boolean, pin: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "toNativeStringOnArena($content, $pin).reinterpret()"
                    else "toNativeString($content).reinterpret()"
                WebIDLBuiltinKind.CHAR -> "$content.code.toUShort()"
                WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                    when (declaration) {
                        is BuiltinIdlDeclaration ->
                            if (useArena) "toNative${declaration.kind.simpleName()}ArrayOnArena($content, $pin).reinterpret()"
                            else "toNative${declaration.kind.simpleName()}Array($content).reinterpret()"
                        is ResolvedIdlEnum ->
                            if (useArena) "toNativeEnumArrayOnArena($content, $pin).reinterpret()"
                            else "toNativeEnumArray($content).reinterpret()"
                        is ResolvedIdlDictionary ->
                            if (useArena) "toNativeArrayOnArena($content, ::toNative${declaration.name}OnArena).reinterpret()"
                            else "toNativeArray($content, ::toNative${declaration.name}).reinterpret()"
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlEnum -> "${cinteropPath}.${decl.name}.entries[$content.ordinal]"
            is ResolvedIdlCallbackFunction ->
                if(useArena) "toNativeCallbackOnArena($content, _invoke${decl.name.capitalized()}).reinterpret()"
                else "toNativeCallback($content, _invoke${decl.name.capitalized()}).reinterpret()"
            is ResolvedIdlDictionary ->
                if (useArena) "toNative${decl.name}OnArena($content)"
                else "toNative${decl.name}($content)"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }

    private fun ResolvedIdlType.toKnType(): String = when(this) {
        is ResolvedIdlType.Void -> "Unit"
        is ResolvedIdlType.Default -> {
            val value = "${cinteropPath}.${toCDefType(ptr = false)}"
            when {
                isString() || isArray() || isCallback() || isDictionary() -> "CPointer<$value>?"
                else -> value
            }
        }
        else -> throw UnsupportedOperationException(toString())
    }
}