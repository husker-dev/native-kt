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
    private val fileName = "./lib${moduleName}.mjs"

    init {
        val actual = if(expectActual) "actual " else ""

        val builder = StringBuilder()
        builder.append("""
            @file:OptIn(ExperimentalWasmJsInterop::class, ExperimentalUnsignedTypes::class)
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

        val isLibLoadedField = "isLib${moduleName.capitalized()}Loaded"
        val initCallbacks = if(idl.callbacks.isNotEmpty())
            "_initCallbacks()" else ""

        builder.append("""
            
            fun wrapCallback(block: (JsNumber) -> Unit): JsAny = js("block")
            
            private var _$isLibLoadedField: Boolean = false
            ${actual}val $isLibLoadedField: Boolean
                get() = _$isLibLoadedField
            
            ${actual}fun ${syncLoadFunctionName(moduleName)}(): Unit = 
                throw UnsupportedOperationException("Synchronous library loading is not supported in JS")
                
            ${actual}fun ${asyncLoadFunctionName(moduleName)}(onReady: () -> Unit) {
                if($isLibLoadedField) 
                    return
                
                loadLib<Module>(_lib).then {
                    _module = it
                    $initCallbacks
                    _$isLibLoadedField = true
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
            
            fun Arena.toNativeCallbackOnArena(callback: Any?, invoke: Int) =
                toNativeCallbackOnArena(callback, invoke, _callbackClone, _callbackEquals, _callbackHashCode, _callbackFree)
                
            fun toNativeCallback(callback: Any?, invoke: Int) =
                toNativeCallback(_module, callback, invoke, _callbackClone, _callbackEquals, _callbackHashCode, _callbackFree)
        
        """.trimIndent())

        // Function converters
        idl.callbacks.values
            .associateBy { it.descName() }
            .forEach { (desc, callback) ->
                append("\nprivate fun $desc(block: (")
                buildList {
                    add("Int")
                    callback.args.mapTo(this) { it.type.toKtJsType() }
                }.joinTo(builder)
                append(") -> ${callback.type.toKtJsType()}): JsAny = js(\"block\")")
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
            callback.args.mapTo(this) { "${it.name.camelCase()}: ${it.type.toKtJsType()}" }
        }
        val castedArgs = callback.args.map {
            castToKotlin(it.type, it.name.camelCase())
        }
        val desc = buildString {
            append(callback.type.toInternalDesc())
            append("i")
            callback.args.joinTo(this, separator = "") { it.type.toInternalDesc() }
        }

        // header
        append("\n\t_invoke${callback.name.upperCamelCase()} = _module.addFunction(${callback.descName()} { ")
        args.joinTo(builder)
        append(" ->\n\t\t")

        // body
        val call = "toKotlinCallback<${callback.name.upperCamelCase()}>(_module, _c)!!(${castedArgs.joinToString()})"
        val casted = castToNative(callback.type, call, useArena = false)

        append(casted)
        append("\n\t")

        // footer
        append("}, \"$desc\")\n")
    }

    private fun printDictionaryLayout(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        append("\nprivate val _layout${dictionary.name.upperCamelCase()} = CStructLayout(")
        buildList {
            dictionary.allFields().mapTo(this) { it.type.toLayoutType() }
            add("Byte::class")
        }.joinTo(builder)
        append(")")
    }

    private fun printDictionaryCasts(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val name = dictionary.name.upperCamelCase()
        val fields = dictionary.allFields()

        fun heaps(toNative: Boolean) = buildString {
            if (fields.any { it.type.isLong() })
                append("\n\tval HEAP64 = BigInt64Array(module.HEAP8.buffer, 0, _module.HEAP8.buffer.byteLength / 8)")
            if (fields.any { it.type.isDouble() })
                append("\n\tval HEAPF64 = module.HEAPF64")
            if (fields.any {
                it.type.isInt() || it.type.isUInt() || it.type.isDictionary() || it.type.isString() || it.type.isString() || it.type.isDictionary() || it.type.isEnum()
            }) append("\n\tval HEAP32 = module.HEAP32")
            if (fields.any { it.type.isFloat() })
                append("\n\tval HEAPF32 = module.HEAPF32")
            if (fields.any { it.type.isChar() || it.type.isShort() || it.type.isUShort() })
                append("\n\tval HEAP16 = module.HEAP16")
            if (toNative || fields.any { it.type.isByte() || it.type.isUByte() || it.type.isBoolean() })
                append("\n\tval HEAP8 = module.HEAP8")
        }

        fun ref(i: Int, mem: String): String {
            val type = fields[i].type
            return when {
                type.isByte() || type.isUByte() || type.isBoolean() -> "HEAP8[$mem + _layout$name[$i]]"
                type.isShort() || type.isUShort() || type.isChar() -> "HEAP16[($mem + _layout$name[$i]) shr 1]"
                type.isLong() || type.isULong() -> "HEAP64[($mem + _layout$name[$i]) shr 3]"
                type.isFloat() -> "HEAPF32[($mem + _layout$name[$i]) shr 2]"
                type.isDouble() -> "HEAPF64[($mem + _layout$name[$i]) shr 3]"
                else -> "HEAP32[($mem + _layout$name[$i]) shr 2]"
            }
        }

        // to native (arena)
        append("\nprivate fun Arena.toNative${name}OnArena(of: $name?) = of?.run { alloc(_layout$name.size).apply {")
        append(heaps(true))
        buildList {
            fields.forEachIndexed { i, field ->
                val ref = field.name.camelCase()
                val casted = when {
                    field.type.isBoolean() || field.type.isChar() ||
                            field.type.isByte() || field.type.isShort()-> ref
                    field.type.isUByte() -> "$ref.toByte()"
                    field.type.isUShort() -> "$ref.toShort()"
                    else -> castToNative(field.type, ref, useArena = true)
                }
                add("\n\t${ref(i, "this")} = $casted")
            }
            add("\n\tHEAP8[this + _layout$name[${fields.size}]] = 0")
        }.joinTo(builder, separator = "")
        append("\n} } ?: 0\n")

        // to native
        append("\nprivate fun toNative$name(module: Module, of: $name?) = of?.run { _module._malloc(_layout$name.size).apply {")
        append(heaps(true))
        buildList {
            fields.forEachIndexed { i, field ->
                val ref = field.name.camelCase()
                val casted = when {
                    field.type.isBoolean() || field.type.isChar() ||
                            field.type.isByte() || field.type.isShort()-> ref
                    field.type.isUByte() -> "$ref.toByte()"
                    field.type.isUShort() -> "$ref.toShort()"
                    else -> castToNative(field.type, ref, useArena = false)
                }
                add("\n\t${ref(i, "this")} = $casted")
            }
            add("\n\tHEAP8[this + _layout$name[${fields.size}]] = FLAG_RELEASABLE.toByte()")
        }.joinTo(builder, separator = "")
        append("\n} } ?: 0\n")

        // to kotlin
        append("\nprivate fun toKotlin$name(module: Module, of: Int): $name? {")
        append("\n\tif(of == 0) return null")
        append(heaps(false))
        append("\n\treturn $name(")
        fields.mapIndexed { i, field ->
            val ref = ref(i, "of")
            val casted = when {
                field.type.isChar() -> "$ref.toInt().toChar()"
                field.type.isUByte() -> "$ref.toUByte()"
                field.type.isUShort() -> "$ref.toUShort()"
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
            castToNative(it.type, it.name.camelCase(), useArena = useArena)
        }

        val deallocFunc = if(function.type.isReleasable())
            freeFuncFor(function.type, "_result_native")
        else null

        val call = "_module._${function.name.snakeCase()}($args)"

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
            append("\n\tfun _${it}_free_addr(): Int")
        }
        append("\n\tfun _KArray_free(self: Int, freeOp: Int)")
        append("\n\n")

        idl.globalOperators().forEach { function ->
            append("\tfun _${function.name.snakeCase()}")
            function.args.joinTo(buffer, prefix = "(", postfix = ")") {
                "${it.name.camelCase()}: ${it.type.toKtJsType()}"
            }
            if(function.type !is ResolvedIdlType.Void) {
                append(": ")
                append(function.type.toKtJsType())
            }
            append("\n")
        }
        append("}\n")
    }

    private fun freeFuncFor(
        type: ResolvedIdlType,
        content: String
    ): String = when {
        type.isCallback() -> "callbackFree(_module, $content)"
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() -> "_module._${type.toCType(ignoreUnsigned = true)}Array_free($content)"
                type.isEnum() -> "_module._KIntArray_free($content)"
                else -> {
                    val fn = freeFuncFor(type, "").split("(")[0]
                    "_module._KArray_free($content, ${fn}_addr())"
                }
            }
        }
        else -> "_module._${type.toCType(ptr = false)}_free($content)"
    }

    private fun castToNative(
        type: ResolvedIdlType,
        content: String,
        useArena: Boolean
    ): String = when {
        type.isUByte() || type.isUShort() -> castToSigned(type, content, smallTypesAsInt = true)
        type.isUnsigned() -> castToNative(type.toSignedType(), castToSigned(type, content), useArena)
        type.isBoolean() -> "$content.toInt()"
        type.isChar() -> "$content.code"
        type.isBoolean() || type.isByte() ||
                type.isUByte() || type.isShort() ||
                type.isUShort() || type.isUInt() -> "$content.toInt()"
        type.isULong() -> "$content.toLong()"
        type.isEnum() -> "$content.ordinal"
        type.isString() ->
            if(useArena) "toNativeKStringOnArena($content)"
            else "toNativeKString(_module, $content)"
        type.isCallback() ->
            if(useArena) "toNativeCallbackOnArena($content, _invoke${type.declaration.name})"
            else "toNativeCallback($content, _invoke${type.declaration.name})"
        type.isDictionary() ->
            if (useArena) "toNative${type.declaration.name}OnArena($content)"
            else "toNative${type.declaration.name}(_module, $content)"
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() ->
                    if (useArena) "toNative${type.toCType()}ArrayOnArena($content)"
                    else "toNative${type.toCType()}Array(_module, $content)"
                type.isEnum() ->
                    if (useArena) "toNativeEnumArrayOnArena($content)"
                    else "toNativeEnumArray(_module, $content)"
                else -> {
                    val fn = castToNative(type, "", useArena).split("(")[0]
                    if (useArena) "toNativeKArrayOnArena($content, ::$fn)"
                    else "toNativeKArray(_module, $content, ::$fn)"
                }
            }
        }
        else -> content
    }

    private fun castToKotlin(
        type: ResolvedIdlType,
        content: String
    ): String {
        val assert = if(type.isNullable) "" else "!!"
        return when {
            type.isUnsigned() -> castToUnsigned(type, castToKotlin(type.toSignedType(), content))
            type.isBoolean() -> "$content.toBoolean()"
            type.isChar() -> "$content.toChar()"
            type.isByte() -> "$content.toByte()"
            type.isUByte() -> "$content.toUByte()"
            type.isShort() -> "$content.toShort()"
            type.isUShort() -> "$content.toUShort()"
            type.isUInt() -> "$content.toUInt()"
            type.isULong() -> "$content.toULong()"
            type.isFloat() -> "$content.truncF32()"
            type.isEnum() -> "${type.declaration.name}.entries[$content]"
            type.isString() -> "toKotlinKString(_module, $content)$assert"
            type.isCallback() -> "toKotlinCallback<${type.toKotlinType()}>(_module, $content)$assert"
            type.isDictionary() -> "toKotlin${type.declaration.name}(_module, $content)$assert"
            type.isArray() -> type.arrayType { type ->
                when {
                    type.isPrimitive() -> "toKotlin${type.toCType()}Array(_module, $content)$assert"
                    type.isEnum() -> "toKotlinEnumArray<${type.declaration.name}>(_module, $content)$assert"
                    else -> {
                        val fn = castToKotlin(type, "").split("(")[0]
                        if(type.isNullable)
                            "toKotlinKArray<${type.toKotlinType()}, Module>(_module, $content, ::$fn)$assert"
                        else "toKotlinKArray(_module, $content, ::$fn)$assert"
                    }
                }
            }
            else -> content
        }
    }

    private fun ResolvedIdlType.toKtJsType(): String = when {
        isVoid() -> "Unit"
        isFloat() -> "Float"
        isDouble() -> "Double"
        isLong() || isULong() -> "Long"
        else -> "Int"
    }

    private fun ResolvedIdlType.toLayoutType(): String = when {
        isBoolean() -> "Boolean::class"
        isChar() -> "Char::class"
        isByte() || isUByte() -> "Byte::class"
        isShort() || isUShort() -> "Short::class"
        isInt() || isUInt() -> "Int::class"
        isLong() || isULong() -> "Long::class"
        isFloat() -> "Float::class"
        isDouble() -> "Double::class"
        isEnum() -> "Int::class"
        else -> "CStructLayout.Ptr::class"
    }

    private fun ResolvedIdlCallbackFunction.descName() = buildString {
        append("_func")
        append(type.toInternalDesc().capitalized())
        append("I")
        args.joinTo(this, separator = "") { it.type.toInternalDesc().capitalized() }
    }

    private fun ResolvedIdlType.toInternalDesc(): String = when {
        isVoid() -> "v"
        isFloat() -> "f"
        isDouble() -> "d"
        isLong() || isULong() -> "j"
        isEnum() || isChar() || isBoolean() ||
                isByte() || isUByte() ||
                isShort() || isUShort() ||
                isInt() || isUInt() -> "i"
        else -> "p"
    }
}