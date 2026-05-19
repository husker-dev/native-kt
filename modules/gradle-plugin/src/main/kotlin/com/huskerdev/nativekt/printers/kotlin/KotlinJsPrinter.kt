package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File

class KotlinJsPrinter(
    val idl: IdlResolver,
    target: File,
    classPath: String,
    val moduleName: String,
    useCoroutines: Boolean,
    val expectActual: Boolean
) {
    private val fileName = "./lib${moduleName}.js"

    init {
        val actual = if(expectActual) "actual " else ""

        val builder = StringBuilder()
        builder.append("""
            @file:OptIn(ExperimentalWasmJsInterop::class)
            @file:Suppress("unused", "ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT")
            
            package $classPath
            
            import com.huskerdev.nativekt.web.*
            import kotlin.js.*
            
        """.trimIndent())

        if(useCoroutines)
            builder.append("""
                import kotlinx.coroutines.suspendCancellableCoroutine
                import kotlin.coroutines.resume
                
            """.trimIndent())

        builder.append("""
            
            @JsModule("$fileName")
            private external val _lib: JsAny
            
            private lateinit var _module: Module
            
        """.trimIndent())

        val initCallbacks = if(idl.callbacks.isNotEmpty())
            "initCallbacks()" else ""

        builder.append("""
            
            fun wrapCallback(block: (JsNumber) -> Unit): JsAny = js("block")
            
            private var isLib${moduleName.capitalized()}Loaded_: Boolean = false
            ${actual}val isLib${moduleName.capitalized()}Loaded: Boolean
                get() = isLib${moduleName.capitalized()}Loaded_
            
            ${actual}fun ${syncFunctionName(moduleName)}(): Unit = 
                throw UnsupportedOperationException("Synchronous library loading is not supported in JS")
                
            ${actual}fun ${asyncFunctionName(moduleName)}(onReady: () -> Unit) {
                if(isLib${moduleName.capitalized()}Loaded) 
                    return
                
                loadLib<Module>(_lib).then {
                    _module = it
                    $initCallbacks
                    isLib${moduleName.capitalized()}Loaded_ = true
                    onReady()
                    _lib // does nothing, but required
                }
            }
            
        """.trimIndent())

        if(useCoroutines) {
            builder.append("""
                
                ${actual}suspend fun ${asyncFunctionName(moduleName)}() {
                    if(isLib${moduleName.capitalized()}Loaded)
                        return
                    suspendCancellableCoroutine { continuation ->
                        ${asyncFunctionName(moduleName)} {
                            continuation.resume(Unit)
                        }
                    }
                }
                
            """.trimIndent())
        }

        // Callbacks loading
        if(idl.callbacks.isNotEmpty()) {
            builder.append("\nprivate var _freeCallback: Int = 0\n")
            idl.callbacks.values.joinTo(builder, separator = "\n") {
                "private var _invoke${it.name} = 0"
            }

            builder.append("\n")
            idl.callbacks.values.forEachIndexed { index, callback ->
                builder.append("\nprivate fun _callback")
                builder.append(index)
                builder.append("Js(block: (")
                (arrayListOf("Int") + callback.args.map { toSimpleJsType(it.type) })
                    .joinTo(builder)
                builder.append(") -> ")
                builder.append(toSimpleJsType(callback.type))
                builder.append("): JsAny = js(\"block\")")
            }

            builder.append("""
                
                
                private fun initCallbacks() {
                    _freeCallback = createCallbackFreeFunction(_module)
                
            """.trimIndent())
            idl.callbacks.values.forEachIndexed { index, callback ->
                printCallbackInvoke(builder, callback, index)
            }
            builder.append("}\n")

            // wrap
            idl.callbacks.values.forEach { callback -> printCallbackCast(builder, callback) }
        }
        idl.dictionaries.values.forEach { printDictionaryCasts(builder, it) }
        idl.globalOperators().forEach { printFunction(builder, it) }

        printTypes(builder)

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printDictionaryCasts(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val structLayout = CStructLayout(dictionary, true)

        val heaps = StringBuilder()
        if(dictionary.allFields().any { it.type.isLong() })
            heaps.append("val HEAP64 = BigInt64Array(_module.HEAP8.buffer, 0, _module.HEAP8.buffer.byteLength / 8)\n\t")
        if(dictionary.allFields().any { it.type.isDouble() })
            heaps.append("val HEAPF64 = _module.HEAPF64\n\t")
        if(dictionary.allFields().any { it.type.isInt() || it.type.isDictionary() || it.type.isEnum() })
            heaps.append("val HEAP32 = _module.HEAP32\n\t")
        if(dictionary.allFields().any { it.type.isFloat() })
            heaps.append("val HEAPF32 = _module.HEAPF32\n\t")
        if(dictionary.allFields().any { it.type.getAlignment() == 2 })
            heaps.append("val HEAP16 = _module.HEAP16\n\t")
        if(dictionary.allFields().any { it.type.getAlignment() == 1 })
            heaps.append("val HEAP8 = _module.HEAP8\n\t")

        // to native
        append("\nfun toNativeDictionary")
        append(dictionary.name)
        append("(of: ")
        append(dictionary.name)
        append(") = _module._malloc(")
        append(structLayout.size)
        append(").apply {\n\t")
        append(heaps)

        dictionary.allFields().forEachIndexed { i, field ->
            append("\n\t")
            val fieldRef = castToNative(field.type, "of.${field.name}", dealloc = false, useArena = false)
            val address = "this + ${structLayout.addressOf(i)}"

            append(when(val declaration = (field.type as ResolvedIdlType.Default).declaration) {
                is BuiltinIdlDeclaration -> when(declaration.kind) {
                    WebIDLBuiltinKind.BYTE,
                    WebIDLBuiltinKind.BOOLEAN -> "HEAP8[$address] = $fieldRef"
                    WebIDLBuiltinKind.SHORT,
                    WebIDLBuiltinKind.CHAR -> "HEAP16[($address) shr 1] = $fieldRef"
                    WebIDLBuiltinKind.INT -> "HEAP32[($address) shr 2] = $fieldRef"
                    WebIDLBuiltinKind.LONG -> "HEAP64[($address) shr 3] = $fieldRef"
                    WebIDLBuiltinKind.FLOAT -> "HEAPF32[($address) shr 2] = $fieldRef"
                    WebIDLBuiltinKind.DOUBLE -> "HEAPF64[($address) shr 3] = $fieldRef"
                    WebIDLBuiltinKind.STRING -> "fillEmString(_module, $address, $fieldRef)"
                    WebIDLBuiltinKind.LIST -> "fillEmArray(_module, $address, $fieldRef)"
                    else -> throw UnsupportedOperationException(field.type.toString())
                }
                is ResolvedIdlEnum -> "HEAP32[($address) shr 2] = $fieldRef"
                is ResolvedIdlCallbackFunction,
                is ResolvedIdlDictionary -> "HEAP32[($address) shr 2] = $fieldRef"
                else -> throw UnsupportedOperationException(field.type.toString())
            })
        }
        append("\n\t// padding: ${structLayout.postPadding}")
        append("\n}\n")

        // to kotlin
        append("\nfun toKotlinDictionary")
        append(dictionary.name)
        append("(of: Int, dealloc: Boolean): ")
        append(dictionary.name)
        append(" {\n\t")
        append(heaps)
        append("return ")
        append(dictionary.name)
        append("(")

        dictionary.allFields().forEachIndexed { i, field ->
            append("\n\t\t")
            val address = "of + ${structLayout.addressOf(i)}"
            val target = when(val declaration = (field.type as ResolvedIdlType.Default).declaration) {
                is BuiltinIdlDeclaration -> when(declaration.kind) {
                    WebIDLBuiltinKind.BYTE -> "HEAP8[$address]"
                    WebIDLBuiltinKind.BOOLEAN -> "HEAP8[$address] == 1.toByte()"
                    WebIDLBuiltinKind.SHORT -> "HEAP16[($address) shr 1].toShort()"
                    WebIDLBuiltinKind.CHAR -> "HEAP16[($address) shr 1]"
                    WebIDLBuiltinKind.INT -> "HEAP32[($address) shr 2]"
                    WebIDLBuiltinKind.LONG -> "HEAP64[($address) shr 3]"
                    WebIDLBuiltinKind.FLOAT -> "HEAPF32[($address) shr 2]"
                    WebIDLBuiltinKind.DOUBLE -> "HEAPF64[($address) shr 3]"
                    WebIDLBuiltinKind.STRING -> "extractEmString(_module, $address)"
                    WebIDLBuiltinKind.LIST -> "extractEmArray(_module, $address)"
                    else -> throw UnsupportedOperationException(field.type.toString())
                }
                is ResolvedIdlEnum -> "HEAP32[($address) shr 2]"
                is ResolvedIdlCallbackFunction,
                is ResolvedIdlDictionary -> "HEAP32[($address) shr 2]"
                else -> throw UnsupportedOperationException(field.type.toString())
            }
            append(castToJS(field.type, target, dealloc = false, deallocContent = false, useArena = false))
            append(",")
        }
        append("\n\t).also { if(dealloc) _module._free(of) }\n}\n")
    }

    private fun printCallbackCast(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        append("\nprivate fun toNativeCallback")
        append(callback.name)
        append("(of: ${callback.name}) =\n\t")
        append("mallocCallback(_module, of, _invoke")
        append(callback.name)
        append(", _freeCallback)\n")
    }

    private fun printCallbackInvoke(
        builder: StringBuilder,
        callback: ResolvedIdlCallbackFunction,
        index: Int
    ) = builder.apply {
        val args = listOf("_c: Int") + callback.args.map { "${it.name}: ${toSimpleJsType(it.type)}" }
        val castedArgs = callback.args.map { castToJS(it.type, it.name, it.isDealloc(), it.isDeallocContent(), false) }

        // header
        append("\n\t// ").append(callback.name)
        append("\n\t_invoke")
        append(callback.name)
        append(" = _module._setCallback(${index}, _callback${index}Js { ")
        args.joinTo(builder)
        append(" ->\n\t\t")

        // body
        val call = "toKotlinCallback<${callback.name}>(_module, _c, false)(${castedArgs.joinToString()})"
        append(castToNative(callback.type, call, dealloc = false, useArena = false))
        append("\n\t")

        // footer
        append("})\n")
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function, isActual = expectActual, forcePrintVoid = true)
        append(" = ")

        val useArena = function.args.any { it.type.isString() || it.type.isArray() || it.isDealloc() }

        if(useArena)
            append("EmArena.use(_module) { arena ->")
        append("\n\t")

        val args = function.args.joinToString {
            castToNative(it.type, it.name, it.isDealloc(), useArena)
        }
        val func = "_module.${function.name}"
        append(castToJS(function.type, "$func($args)", function.isDealloc(), function.isDeallocContent(), useArena))

        if(useArena)
            append("\n}")

        append("\n")
    }

    private fun printTypes(buffer: StringBuilder) = buffer.apply {
        append("""
            
            private external interface Lib: JsAny {
            	fun default(): Promise<Module>
            }

            private external interface Module: EmModule {
            	fun _setCallback(index: Int, callback: JsAny): Int

        """.trimIndent())

        idl.globalOperators().forEach { function ->
            append("\tfun ")
            append(function.name)
            append("(")
            function.args.joinTo(buffer) { "${it.name}: ${toSimpleJsType(it.type)}" }
            append(")")
            if(function.type !is ResolvedIdlType.Void) {
                append(": ")
                append(toSimpleJsType(function.type))
            }
            append("\n")
        }
        append("}\n")
    }

    private fun castToNative(type: ResolvedIdlType, content: String, dealloc: Boolean, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.CHAR -> "${content}.code"
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "arena.toNativeString($content)"
                    else "toNativeString(_module, $content)"
                WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                    when (declaration) {
                        is BuiltinIdlDeclaration -> {
                            val name = declaration.kind.simpleName()
                            if (useArena) "arena.toNative${name}Array($content)"
                            else "toNative${name}Array(_module, $content)"
                        }
                        is ResolvedIdlEnum ->
                            "toNativeEnumArray(_module, $content)"
                        is ResolvedIdlDictionary ->
                            "toNativeArray(_module, $content, ::toNativeDictionary${declaration.name})"
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlEnum -> "${content}.ordinal"
            is ResolvedIdlCallbackFunction ->
                if(dealloc) "arena.callback(toNativeCallback${decl.name}($content))"
                else "toNativeCallback${decl.name}($content)"
            is ResolvedIdlDictionary -> "toNativeDictionary${decl.name}($content)"
            else -> throw UnsupportedOperationException(type.toString())
        }
        is ResolvedIdlType.Union -> throw UnsupportedOperationException(type.toString())
    }

    private fun castToJS(type: ResolvedIdlType, content: String, dealloc: Boolean, deallocContent: Boolean, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.FLOAT -> "$content.truncF32()"
                WebIDLBuiltinKind.CHAR -> "$content.toChar()"
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "arena.toKotlinString($content, $dealloc)"
                    else "toKotlinString(_module, $content, $dealloc)"
                WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                    when (declaration) {
                        is BuiltinIdlDeclaration -> {
                            val name = declaration.kind.simpleName()
                            if (useArena) "arena.toKotlin${name}Array($content, $dealloc)"
                            else "toKotlin${name}Array(_module, $content, $dealloc)"
                        }
                        is ResolvedIdlEnum ->
                            "toKotlinEnumArray(_module, $content, $dealloc, ${declaration.name}.entries::get)"
                        is ResolvedIdlDictionary ->
                            "toKotlinArray(_module, $content, ::toKotlinDictionary${declaration.name}, $dealloc, $deallocContent)"
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlEnum -> "${decl.name}.entries[$content]"
            is ResolvedIdlCallbackFunction ->
                if(useArena) "arena.toKotlinCallback<${decl.name}>($content, $dealloc)"
                else "toKotlinCallback<${decl.name}>(_module, $content, $dealloc)"
            is ResolvedIdlDictionary -> "toKotlinDictionary${decl.name}($content, $dealloc)"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }

    private fun toSimpleJsType(type: ResolvedIdlType): String = when(type) {
        is ResolvedIdlType.Void -> "Unit"
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.BOOLEAN -> "Boolean"
                WebIDLBuiltinKind.FLOAT -> "Float"
                WebIDLBuiltinKind.CHAR -> "Int"
                WebIDLBuiltinKind.INT -> "Int"
                WebIDLBuiltinKind.DOUBLE -> "Double"
                WebIDLBuiltinKind.BYTE -> "Byte"
                WebIDLBuiltinKind.SHORT -> "Short"
                WebIDLBuiltinKind.LONG -> "Long"
                WebIDLBuiltinKind.STRING -> "EmString"
                WebIDLBuiltinKind.LIST -> "EmArray"
                else -> throw UnsupportedOperationException()
            }
            is ResolvedIdlEnum -> "Int"
            is ResolvedIdlCallbackFunction -> "Int"
            is ResolvedIdlDictionary -> "Int"
            else -> "Int"
        }
        else -> throw UnsupportedOperationException(type.toString())
    }
}