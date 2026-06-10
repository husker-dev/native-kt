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
            "_initCallbacks()" else ""

        builder.append("""
            
            fun wrapCallback(block: (JsNumber) -> Unit): JsAny = js("block")
            
            private var isLib${moduleName.capitalized()}Loaded_: Boolean = false
            ${actual}val isLib${moduleName.capitalized()}Loaded: Boolean
                get() = isLib${moduleName.capitalized()}Loaded_
            
            ${actual}fun ${syncLoadFunctionName(moduleName)}(): Unit = 
                throw UnsupportedOperationException("Synchronous library loading is not supported in JS")
                
            ${actual}fun ${asyncLoadFunctionName(moduleName)}(onReady: () -> Unit) {
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
                
                ${actual}suspend fun ${asyncLoadFunctionName(moduleName)}() {
                    if(isLib${moduleName.capitalized()}Loaded)
                        return
                    suspendCancellableCoroutine { continuation ->
                        ${asyncLoadFunctionName(moduleName)} {
                            continuation.resume(Unit)
                        }
                    }
                }
                
            """.trimIndent())
        }

        // Callbacks loading
        if(idl.callbacks.isNotEmpty()) {
            printLabel(builder, "Callbacks")
            printCallbacks(builder)
        }

        if(idl.dictionaries.isNotEmpty()) {
            printLabel(builder, "Dictionaries")
            idl.dictionaries.values.forEach { printDictionaryLayout(builder, it) }
            builder.append("\n")
            idl.dictionaries.values.forEach { printDictionaryCasts(builder, it) }
        }

        idl.globalOperators().forEach { printFunction(builder, it) }

        printTypes(builder)

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printCallbacks(builder: StringBuilder) = builder.apply {
        // Variables
        append("\nprivate var _callbackClone: Int = 0")
        append("\nprivate var _callbackEquals: Int = 0")
        append("\nprivate var _callbackHashCode: Int = 0")
        append("\nprivate var _callbackFree: Int = 0\n")

        // toNative
        append("""
            
            fun Arena.toNativeCallbackOnArena(callback: Any, invoke: Int) =
                toNativeCallbackOnArena(callback, invoke, _callbackClone, _callbackEquals, _callbackHashCode, _callbackFree)
                
            fun toNativeCallback(callback: Any, invoke: Int) =
                toNativeCallback(_module, callback, invoke, _callbackClone, _callbackEquals, _callbackHashCode, _callbackFree)
        
        """.trimIndent())

        // Function converters
        idl.callbacks.values
            .associateBy { it.descName() }
            .forEach { (desc, callback) ->
                append("\nprivate fun $desc(block: (")
                buildList {
                    add("Int")
                    callback.args.mapTo(this) { toKtJsType(it.type) }
                }.joinTo(builder)
                append(") -> ${toKtJsType(callback.type)}): JsAny = js(\"block\")")
            }
        append("\n")

        // Invoke address
        idl.callbacks.values.joinTo(builder, separator = "") {
            "\nprivate var _invoke${it.name}: Int = 0"
        }

        // init
        append("\n\n")
        append("""
                private fun _initCallbacks() {
                    _callbackClone = createCallbackCloneFunction(_module)
                    _callbackEquals = createCallbackEqualsFunction(_module)
                    _callbackHashCode = createCallbackHashCodeFunction(_module)
                    _callbackFree = createCallbackFreeFunction(_module)
                
            """.trimIndent())
        idl.callbacks.values.forEach{ callback ->
            printCallbackInvoke(builder, callback)
        }
        append("}\n")
    }

    private fun printCallbackInvoke(
        builder: StringBuilder,
        callback: ResolvedIdlCallbackFunction
    ) = builder.apply {
        val args = buildList {
            add("_c: Int")
            callback.args.mapTo(this) { "${it.name}: ${toKtJsType(it.type)}" }
        }
        val castedArgs = callback.args.map {
            castToKotlin(it.type, it.name)
        }
        val desc = buildString {
            append(callback.type.toInternalDesc())
            append("i")
            callback.args.joinTo(this, separator = "") { it.type.toInternalDesc() }
        }

        // header
        append("\n\t_invoke${callback.name} = _module.addFunction(${callback.descName()} { ")
        args.joinTo(builder)
        append(" ->\n\t\t")

        // body
        val call = "toKotlinCallback<${callback.name}>(_module, _c)(${castedArgs.joinToString()})"
        val casted = castToNative(callback.type, call, useArena = false)

        append(casted)
        append("\n\t")

        // footer
        append("}, \"$desc\")\n")
    }

    private fun printDictionaryLayout(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        append("\nprivate val _layout${dictionary.name} = CStructLayout(")
        buildList {
            dictionary.allFields().mapTo(this) { toLayoutType(it.type) }
            add("Byte::class")
        }.joinTo(builder)
        append(")")
    }

    private fun printDictionaryCasts(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val name = dictionary.name
        val fields = dictionary.allFields()

        fun heaps(toNative: Boolean) = buildString {
            if (fields.any { it.type.isLong() })
                append("\n\tval HEAP64 = BigInt64Array(_module.HEAP8.buffer, 0, _module.HEAP8.buffer.byteLength / 8)")
            if (fields.any { it.type.isDouble() })
                append("\n\tval HEAPF64 = _module.HEAPF64")
            if (fields.any {
                it.type.isInt() || it.type.isDictionary() || it.type.isString() || it.type.isString() || it.type.isDictionary() || it.type.isEnum()
            }) append("\n\tval HEAP32 = _module.HEAP32")
            if (fields.any { it.type.isFloat() })
                append("\n\tval HEAPF32 = _module.HEAPF32")
            if (fields.any { it.type.isChar() || it.type.isShort() })
                append("\n\tval HEAP16 = _module.HEAP16")
            if (toNative || fields.any { it.type.isByte() || it.type.isBoolean() })
                append("\n\tval HEAP8 = _module.HEAP8")
        }

        fun ref(i: Int, mem: String): String {
            return when(val declaration = (fields[i].type as ResolvedIdlType.Default).declaration) {
                is BuiltinIdlDeclaration -> when(declaration.kind) {
                    WebIDLBuiltinKind.BYTE,
                    WebIDLBuiltinKind.BOOLEAN -> "HEAP8[$mem + _layout$name[$i]]"
                    WebIDLBuiltinKind.SHORT,
                    WebIDLBuiltinKind.CHAR -> "HEAP16[($mem + _layout$name[$i]) shr 1]"
                    WebIDLBuiltinKind.INT,
                    WebIDLBuiltinKind.STRING,
                    WebIDLBuiltinKind.LIST -> "HEAP32[($mem + _layout$name[$i]) shr 2]"
                    WebIDLBuiltinKind.LONG -> "HEAP64[($mem + _layout$name[$i]) shr 3]"
                    WebIDLBuiltinKind.FLOAT -> "HEAPF32[($mem + _layout$name[$i]) shr 2]"
                    WebIDLBuiltinKind.DOUBLE -> "HEAPF64[($mem + _layout$name[$i]) shr 3]"
                    else -> throw UnsupportedOperationException()
                }
                is ResolvedIdlEnum,
                is ResolvedIdlCallbackFunction,
                is ResolvedIdlDictionary -> "HEAP32[($mem + _layout$name[$i]) shr 2]"
                else -> throw UnsupportedOperationException()
            }
        }

        // to native (arena)
        append("\nfun Arena.toNative${name}OnArena(of: $name) = alloc(_layout$name.size).apply {")
        append(heaps(true))
        buildList {
            fields.forEachIndexed { i, field ->
                val ref = "of.${field.name}"
                val casted = when {
                    field.type.isBoolean() || field.type.isByte() ||
                    field.type.isShort() || field.type.isChar() -> ref
                    else -> castToNative(field.type, ref, useArena = true)
                }
                add("\n\t${ref(i, "this")} = $casted")
            }
            add("\n\tHEAP8[this + _layout$name[${fields.size}]] = 0")
        }.joinTo(builder, separator = "")
        append("\n}\n")

        // to native
        append("\nfun toNative$name(of: $name) = _module._malloc(_layout$name.size).apply {")
        append(heaps(true))
        buildList {
            fields.forEachIndexed { i, field ->
                val ref = "of.${field.name}"
                val casted = when {
                    field.type.isBoolean() || field.type.isByte() ||
                    field.type.isShort() || field.type.isChar() -> ref
                    else -> castToNative(field.type, ref, useArena = false)
                }
                add("\n\t${ref(i, "this")} = $casted")
            }
            add("\n\tHEAP8[this + _layout$name[${fields.size}]] = FLAG_RELEASABLE")
        }.joinTo(builder, separator = "")
        append("\n}\n")

        // to kotlin
        append("\nfun toKotlin$name(of: Int): $name {")
        append(heaps(false))
        append("\n\treturn $name(")
        fields.mapIndexed { i, field ->
            val ref = ref(i, "of")
            val casted = when {
                field.type.isChar() -> "$ref.toInt().toChar()"
                field.type.isByte() || field.type.isShort() -> ref
                else -> castToKotlin(field.type, ref(i, "of"))
            }
            "\n\t\t$casted,"
        }.joinTo(builder, separator = "")
        append("\n\t)\n}\n")
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        val useArena = function.args.any {
            it.type.isString() ||
                    it.type.isArray() ||
                    it.type.isDictionary() ||
                    it.type.isCallback()
        }

        val args = function.args.joinToString {
            castToNative(it.type, it.name, useArena = useArena)
        }

        val deallocFunc = if(function.isDealloc())
            freeFuncFor(function.type, "_result_native")
        else null

        val call = "_module._${function.name}($args)"

        // === Print ===

        append('\n')
        printFunctionHeader(builder, function, isActual = expectActual)

        append(when {
            useArena -> " = Arena.use(_module) {"
            deallocFunc != null -> " {"
            else -> " = "
        })

        append("\n\t")
        if(deallocFunc != null) {
            append("val _result_native = $call")
            append("\n\t")
            append("val _result_kt = ${castToKotlin(function.type, "_result_native")}")
            append("\n\t")
            append(deallocFunc)

            if(function.type !is ResolvedIdlType.Void) {
                append("\n\t")
                if(!useArena)
                    append("return ")
                append("_result_kt")
            }
        } else
            append(castToKotlin(function.type, call))

        if(useArena || deallocFunc != null)
            append("\n}")
        append("\n")
    }

    private fun printTypes(buffer: StringBuilder) = buffer.apply {
        append("""
            
            private external interface Lib: JsAny {
            	fun default(): Promise<Module>
            }

            private external interface Module: EmModule {
        """.trimIndent())

        listOf(
            "KString",
            "KCharArray",
            "KBooleanArray",
            "KByteArray",
            "KShortArray",
            "KIntArray",
            "KLongArray",
            "KFloatArray",
            "KDoubleArray",
            *idl.dictionaries.values.map { it.name }.toTypedArray()
        ).forEach {
            append("\n\tfun _${it}_free(self: Int)")
            append("\n\tfun _${it}_freeAddr(): Int")
        }
        append("\n\tfun _KArray_free(self: Int, freeOp: Int)")
        append("\n\n")

        idl.globalOperators().forEach { function ->
            append("\tfun _${function.name}")
            function.args.joinTo(buffer, prefix = "(", postfix = ")") {
                "${it.name}: ${toKtJsType(it.type)}"
            }
            if(function.type !is ResolvedIdlType.Void) {
                append(": ")
                append(toKtJsType(function.type))
            }
            append("\n")
        }
        append("}\n")
    }

    private fun freeFuncFor(
        type: ResolvedIdlType,
        content: String
    ) = when {
        type.isString() -> "_module._KString_free($content)"
        type.isArray() -> (type as ResolvedIdlType.Default).arrayType { type ->
            when (val declaration = type.declaration) {
                is BuiltinIdlDeclaration -> "_module._K${declaration.kind.simpleName()}Array_free($content)"
                is ResolvedIdlEnum -> "_module._KIntArray_free($content)"
                is ResolvedIdlDictionary -> "_module._KArray_free($content, _module._${declaration.name}_freeAddr())"
                else -> throw UnsupportedOperationException(type.toString())
            }
        }
        type.isCallback() -> "callbackFree($content)"
        type.isDictionary() -> "_module._${(type as ResolvedIdlType.Default).declaration.name.capitalized()}_free($content)"
        else -> null
    }

    private fun castToNative(type: ResolvedIdlType, content: String, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.BOOLEAN -> "$content.toInt()"
                WebIDLBuiltinKind.CHAR -> "$content.code"
                WebIDLBuiltinKind.BYTE -> "$content.toInt()"
                WebIDLBuiltinKind.SHORT -> "$content.toInt()"
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "toNativeStringOnArena($content)"
                    else "toNativeString(_module, $content)"
                WebIDLBuiltinKind.LIST -> type.arrayType { type ->
                    when (val declaration = type.declaration) {
                        is BuiltinIdlDeclaration ->
                            if (useArena) "toNative${declaration.kind.simpleName()}ArrayOnArena($content)"
                            else "toNative${declaration.kind.simpleName()}Array(_module, $content)"
                        is ResolvedIdlEnum ->
                            if (useArena) "toNativeEnumArrayOnArena($content)"
                            else "toNativeEnumArray(_module, $content)"
                        is ResolvedIdlDictionary ->
                            if (useArena) "toNativeArrayOnArena($content, ::toNative${declaration.name})"
                            else "toNativeArray(_module, $content, ::toNative${declaration.name})"
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlEnum -> "$content.ordinal"
            is ResolvedIdlCallbackFunction ->
                if(useArena) "toNativeCallbackOnArena($content, _invoke${decl.name})"
                else "toNativeCallback($content, _invoke${decl.name})"
            is ResolvedIdlDictionary ->
                if (useArena) "toNative${decl.name}OnArena($content)"
                else "toNative${decl.name}($content)"
            else -> throw UnsupportedOperationException(type.toString())
        }
        is ResolvedIdlType.Union -> throw UnsupportedOperationException(type.toString())
    }

    private fun castToKotlin(type: ResolvedIdlType, content: String): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.BOOLEAN -> "$content.toBoolean()"
                WebIDLBuiltinKind.CHAR -> "$content.toChar()"
                WebIDLBuiltinKind.BYTE -> "$content.toByte()"
                WebIDLBuiltinKind.SHORT -> "$content.toShort()"
                WebIDLBuiltinKind.FLOAT -> "$content.truncF32()"
                WebIDLBuiltinKind.STRING -> "toKotlinString(_module, $content)"
                WebIDLBuiltinKind.LIST -> type.arrayType { type ->
                    when (val declaration = type.declaration) {
                        is BuiltinIdlDeclaration -> "toKotlin${declaration.kind.simpleName()}Array(_module, $content)"
                        is ResolvedIdlEnum -> "toKotlinEnumArray<${declaration.name}>(_module, $content)"
                        is ResolvedIdlDictionary -> "toKotlinArray(_module, $content, ::toKotlin${declaration.name})"
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlEnum -> "${decl.name}.entries[$content]"
            is ResolvedIdlCallbackFunction -> "toKotlinCallback(_module, $content)"
            is ResolvedIdlDictionary -> "toKotlin${decl.name}($content)"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }

    private fun toKtJsType(type: ResolvedIdlType): String = when(type) {
        is ResolvedIdlType.Void -> "Unit"
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.BOOLEAN -> "Int"
                WebIDLBuiltinKind.FLOAT -> "Float"
                WebIDLBuiltinKind.CHAR -> "Int"
                WebIDLBuiltinKind.INT -> "Int"
                WebIDLBuiltinKind.DOUBLE -> "Double"
                WebIDLBuiltinKind.BYTE -> "Int"
                WebIDLBuiltinKind.SHORT -> "Int"
                WebIDLBuiltinKind.LONG -> "Long"
                WebIDLBuiltinKind.STRING -> "Int"
                WebIDLBuiltinKind.LIST -> "Int"
                else -> throw UnsupportedOperationException()
            }
            is ResolvedIdlEnum -> "Int"
            else -> "Int"
        }
        else -> throw UnsupportedOperationException(type.toString())
    }

    private fun toLayoutType(type: ResolvedIdlType): String = when(type) {
        is ResolvedIdlType.Void -> throw UnsupportedOperationException(type.toString())
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.BOOLEAN -> "Boolean::class"
                WebIDLBuiltinKind.FLOAT -> "Float::class"
                WebIDLBuiltinKind.CHAR -> "Char::class"
                WebIDLBuiltinKind.INT -> "Int::class"
                WebIDLBuiltinKind.DOUBLE -> "Double::class"
                WebIDLBuiltinKind.BYTE -> "Byte::class"
                WebIDLBuiltinKind.SHORT -> "Short::class"
                WebIDLBuiltinKind.LONG -> "Long::class"
                WebIDLBuiltinKind.STRING -> "CStructLayout.Ptr::class"
                WebIDLBuiltinKind.LIST -> "CStructLayout.Ptr::class"
                else -> throw UnsupportedOperationException()
            }
            is ResolvedIdlEnum -> "Int::class"
            else -> "CStructLayout.Ptr::class"
        }
        else -> throw UnsupportedOperationException(type.toString())
    }

    private fun ResolvedIdlCallbackFunction.descName() = buildString {
        append("_func")
        append(type.toInternalDesc().capitalized())
        append("I")
        args.joinTo(this, separator = "") { it.type.toInternalDesc().capitalized() }
    }

    fun ResolvedIdlType.toInternalDesc(): String = when(this) {
        is ResolvedIdlType.Void -> "v"
        is ResolvedIdlType.Default -> when(val declaration = declaration) {
            is BuiltinIdlDeclaration -> when(declaration.kind) {
                WebIDLBuiltinKind.CHAR -> "i"
                WebIDLBuiltinKind.BOOLEAN -> "i"
                WebIDLBuiltinKind.BYTE -> "i"
                WebIDLBuiltinKind.SHORT -> "i"
                WebIDLBuiltinKind.INT -> "i"
                WebIDLBuiltinKind.LONG -> "j"
                WebIDLBuiltinKind.FLOAT -> "f"
                WebIDLBuiltinKind.DOUBLE -> "d"
                WebIDLBuiltinKind.STRING -> "p"
                WebIDLBuiltinKind.LIST -> "p"
                else -> throw UnsupportedOperationException(toString())
            }
            is ResolvedIdlEnum -> "i"
            else -> "p"
        }
        else -> throw UnsupportedOperationException(toString())
    }
}