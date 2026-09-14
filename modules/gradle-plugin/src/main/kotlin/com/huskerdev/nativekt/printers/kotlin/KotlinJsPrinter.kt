package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.plugin.JsTarget
import com.huskerdev.nativekt.plugin.Language
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import java.io.File

class KotlinJsPrinter(
    private val context: NativeModuleContext,
    target: File,
    private val expectActual: Boolean,
    private val isWasm: Boolean
) {
    private val fileName = "./lib${context.moduleName}.mjs"

    private val actual = if(expectActual) "actual " else ""

    init {
        target.parentFile.mkdirs()
        target.writeText(buildString {
            printHeader()
            printBasicCasts()
            printCallbacks()
            printModuleApi()
            printDictionaries()
            printFunctions()
            printInterfaces()
        })
    }

    private fun StringBuilder.printHeader() {
        val isLibLoadedField = loadFieldName(context)

        appendLine("""
            @file:OptIn(ExperimentalWasmJsInterop::class, ExperimentalUnsignedTypes::class)
            @file:Suppress("SpellCheckingInspection", "ACTUAL_ANNOTATIONS_NOT_MATCH_EXPECT", "LocalVariableName", "FunctionName", "PropertyName", "ObjectPropertyName")
            
            package ${context.classPath}
            
            import com.huskerdev.nativekt.*
            import com.huskerdev.nativekt.web.*
            import kotlin.js.*
            import kotlin.enums.enumEntries
        """.trimIndent())

        if(context.extension.useCoroutines) {
            appendLine("""
                import kotlinx.coroutines.suspendCancellableCoroutine
                import kotlin.coroutines.resume
            """.trimIndent())
        }

        when (context.language) {
            Language.RUST -> appendLine("""
                
                private val _module = RustLib
                private lateinit var _wasmMemory: WebAssemblyMemory
                
                private val _memory: ArrayBuffer
                    get() = _wasmMemory.buffer
            """.trimIndent())
            else -> appendLine("""
                
                private val _lib = EmscriptenLib
                private lateinit var _module: EmscriptenModule
                
                private val _memory: ArrayBuffer
                    get() = _module.wasmMemory.buffer
            """.trimIndent())
        }

        appendLine("""
            
            private var _$isLibLoadedField: Boolean = false
            ${actual}val $isLibLoadedField: Boolean
                get() = _$isLibLoadedField
            
            ${actual}fun ${syncLoadFunctionName(context)}(): Unit = 
                throw UnsupportedOperationException("Synchronous library loading is not supported in JS")
                
            ${actual}fun ${asyncLoadFunctionName(context)}(onReady: () -> Unit) {
                if($isLibLoadedField) 
                    return
                
        """.trimIndent())

        when (context.language) {
            Language.RUST -> when (context.module.jsTarget) {
                JsTarget.WEB -> appendLine("""
                    _module.default().then {
                        _wasmMemory = it.memory
                        _$isLibLoadedField = true
                        onReady()
                        it
                    }
                """.replaceIndent("\t"))
                JsTarget.NODE -> appendLine("""
                    compileWasmFromFileAsync("lib${context.moduleName}.wasm").then { wasmModule ->
                        _wasmMemory = _module.initSync(wrapModule(wasmModule)).memory
                        _$isLibLoadedField = true
                        onReady()
                        wasmModule
                    }
                """.replaceIndent("\t"))
            }
            else -> {
                appendLine("""
                    _lib.default().then {
                        _module = it
                        _$isLibLoadedField = true
                """.replaceIndent("\t"))
                if(context.callbacks.isNotEmpty())
                    append("\t\t_initializeCallbacks()\n")
                appendLine("""
                        onReady()
                        _lib
                    }
                """.replaceIndent("\t"))
            }
        }
        append("}\n")

        if(context.extension.useCoroutines) {
            appendLine("""
                
                ${actual}suspend fun ${asyncLoadFunctionName(context)}() {
                    if($isLibLoadedField)
                        return
                    suspendCancellableCoroutine { continuation ->
                        ${asyncLoadFunctionName(context)} {
                            continuation.resume(Unit)
                        }
                    }
                }
            """.trimIndent())
        }
    }

    private fun StringBuilder.printBasicCasts() {
        if(context.hasString) {
            printLabel("String")

            if (context.hasStringToNativeCast) appendLine("""
                
                private fun toNativeString(of: String?): Int {
                    if(of == null) return 0
                    val bytes = of.encodeToByteArray()
                    val data = _module.alloc(bytes.size)
                    bytes.forEachIndexed(Int8Array(_memory, data, bytes.size)::set)
                    return _module.string_new(data, of.length, bytes.size, false)
                }
            """.trimIndent())
            if (context.hasStringToKotlinCast) appendLine("""
                
                private fun toKotlinString(of: Int, free: Boolean): String? {
                    if(of == 0) return null
                    val size = _module.string_size(of)
                    val bytes = ByteArray(size, Int8Array(_memory, _module.string_data(of), size)::get)
                    if(free) _module.string_free(of)
                    return bytes.decodeToString()
                }
            """.trimIndent())
        }

        if(context.hasPrimitiveArray) {
            printLabel("Primitive arrays")

            if (context.hasCharArrayToNativeCast) appendLine("""
                
                private fun toNativeCharArray(of: CharArray?, pure: Boolean = false): Int {
                    if(of == null) return 0
                    val data = _module.alloc(of.size * Char.SIZE_BYTES)
                    of.forEachIndexed(Uint16Array(_memory, data, of.size)::set)
                    if(pure) return data
                    return _module.chararray_new(data, of.size, false)
                }
            """.trimIndent())
            if (context.hasCharArrayToKotlinCast) appendLine("""
                
                private fun toKotlinCharArray(of: Int, free: Boolean): CharArray? {
                    if(of == 0) return null
                    val length = _module.chararray_length(of)
                    return CharArray(length, Uint16Array(_memory, _module.chararray_elements(of), length)::get)
                        .also { if(free) _module.chararray_free(of) }
                }
            """.trimIndent())
            if (context.hasBooleanArrayToNativeCast) appendLine("""
                
                private fun toNativeBooleanArray(of: BooleanArray?, pure: Boolean = false): Int {
                    if(of == null) return 0
                    val data = _module.alloc(of.size)
                    of.forEachIndexed(Int8Array(_memory, data, of.size)::set)
                    if(pure) return data
                    return _module.booleanarray_new(data, of.size, false)
                }
            """.trimIndent())
            if (context.hasBooleanArrayToKotlinCast) appendLine("""
                
                private fun toKotlinBooleanArray(of: Int, free: Boolean): BooleanArray? {
                    if(of == 0) return null
                    val length = _module.booleanarray_length(of)
                    return BooleanArray(length, Int8Array(_memory, _module.booleanarray_elements(of), length)::getBoolean)
                        .also { if(free) _module.booleanarray_free(of) }
                }
            """.trimIndent())
            if (context.hasByteArrayToNativeCast || context.hasUByteArrayToNativeCast ||
                context.hasCriticalString || context.hasCriticalStringOpt) appendLine("""
                
                private fun toNativeByteArray(of: ByteArray?, pure: Boolean = false): Int {
                    if(of == null) return 0
                    val data = _module.alloc(of.size * Byte.SIZE_BYTES)
                    of.forEachIndexed(Int8Array(_memory, data, of.size)::set)
                    if(pure) return data
                    return _module.bytearray_new(data, of.size, false)
                }
            """.trimIndent())
            if (context.hasByteArrayToKotlinCast || context.hasUByteArrayToKotlinCast) appendLine("""
                
                private fun toKotlinByteArray(of: Int, free: Boolean): ByteArray? {
                    if(of == 0) return null
                    val length = _module.bytearray_length(of)
                    return ByteArray(length, Int8Array(_memory, _module.bytearray_elements(of), length)::get)
                        .also { if(free) _module.bytearray_free(of) }
                }
            """.trimIndent())
            if (context.hasShortArrayToNativeCast || context.hasUShortArrayToNativeCast) appendLine("""
                
                private fun toNativeShortArray(of: ShortArray?, pure: Boolean = false): Int {
                    if(of == null) return 0
                    val data = _module.alloc(of.size * Short.SIZE_BYTES)
                    of.forEachIndexed(Int16Array(_memory, data, of.size)::set)
                    if(pure) return data
                    return _module.shortarray_new(data, of.size, false)
                }
            """.trimIndent())
            if (context.hasShortArrayToKotlinCast || context.hasUShortArrayToKotlinCast) appendLine("""
                
                private fun toKotlinShortArray(of: Int, free: Boolean): ShortArray? {
                    if(of == 0) return null
                    val length = _module.shortarray_length(of)
                    return ShortArray(length, Int16Array(_memory, _module.shortarray_elements(of), length)::get)
                        .also { if(free) _module.shortarray_free(of) }
                }
            """.trimIndent())
            if (context.hasIntArrayToNativeCast || context.hasUIntArrayToNativeCast || context.hasEnumArrayToNativeCast) appendLine("""
                
                private fun toNativeIntArray(of: IntArray?, pure: Boolean = false): Int {
                    if(of == null) return 0
                    val data = _module.alloc(of.size * Int.SIZE_BYTES)
                    of.forEachIndexed(Int32Array(_memory, data, of.size)::set)
                    if(pure) return data
                    return _module.intarray_new(data, of.size, false)
                }
            """.trimIndent())
            if (context.hasIntArrayToKotlinCast || context.hasUIntArrayToKotlinCast || context.hasEnumArrayToKotlinCast) appendLine("""
                
                private fun toKotlinIntArray(of: Int, free: Boolean): IntArray? {
                    if(of == 0) return null
                    val length = _module.intarray_length(of)
                    return IntArray(length, Int32Array(_memory, _module.intarray_elements(of), length)::get)
                        .also { if(free) _module.intarray_free(of) }
                }
            """.trimIndent())
            if (context.hasLongArrayToNativeCast || context.hasULongArrayToNativeCast) appendLine("""
                
                private fun toNativeLongArray(of: LongArray?, pure: Boolean = false): Int {
                    if(of == null) return 0
                    val data = _module.alloc(of.size * Long.SIZE_BYTES)
                    of.forEachIndexed(BigInt64Array(_memory, data, of.size)::set)
                    if(pure) return data
                    return _module.longarray_new(data, of.size, false)
                }
            """.trimIndent())
            if (context.hasLongArrayToKotlinCast || context.hasULongArrayToKotlinCast) appendLine("""
                
                private fun toKotlinLongArray(of: Int, free: Boolean): LongArray? {
                    if(of == 0) return null
                    val length = _module.longarray_length(of)
                    return LongArray(length, BigInt64Array(_memory, _module.longarray_elements(of), length)::get)
                        .also { if(free) _module.longarray_free(of) }
                }
            """.trimIndent())
            if (context.hasFloatArrayToNativeCast) appendLine("""
                
                private fun toNativeFloatArray(of: FloatArray?, pure: Boolean = false): Int {
                    if(of == null) return 0
                    val data = _module.alloc(of.size * Float.SIZE_BYTES)
                    of.forEachIndexed(Float32Array(_memory, data, of.size)::set)
                    if(pure) return data
                    return _module.floatarray_new(data, of.size, false)
                }
            """.trimIndent())
            if (context.hasFloatArrayToKotlinCast) appendLine("""
                
                private fun toKotlinFloatArray(of: Int, free: Boolean): FloatArray? {
                    if(of == 0) return null
                    val length = _module.floatarray_length(of)
                    return FloatArray(length, Float32Array(_memory, _module.floatarray_elements(of), length)::get)
                        .also { if(free) _module.floatarray_free(of) }
                }
            """.trimIndent())
            if (context.hasDoubleArrayToNativeCast) appendLine("""
                
                private fun toNativeDoubleArray(of: DoubleArray?, pure: Boolean = false): Int {
                    if(of == null) return 0
                    val data = _module.alloc(of.size * Double.SIZE_BYTES)
                    of.forEachIndexed(Float64Array(_memory, data, of.size)::set)
                    if(pure) return data
                    return _module.doublearray_new(data, of.size, false)
                }
            """.trimIndent())
            if (context.hasDoubleArrayToKotlinCast) appendLine("""
                
                private fun toKotlinDoubleArray(of: Int, free: Boolean): DoubleArray? {
                    if(of == 0) return null
                    val length = _module.doublearray_length(of)
                    return DoubleArray(length, Float64Array(_memory, _module.doublearray_elements(of), length)::get)
                        .also { if(free) _module.doublearray_free(of) }
                }
            """.trimIndent())
        }

        if(context.hasEnumArray) {
            if(context.hasEnumArrayToNativeCast) appendLine("""
                
                private fun <T: Enum<T>> toNativeEnumArray(of: Array<T>?, pure: Boolean = false): Int {
                    if(of == null) return 0
                    val data = _module.alloc(of.size * Int.SIZE_BYTES)
                    val arr = Int32Array(_memory, data, of.size)
                    of.forEachIndexed { i, it -> arr[i] = it.ordinal }
                    if(pure) return data
                    return _module.intarray_new(data, of.size, false)
                }
            """.trimIndent())
            if(context.hasEnumArrayToKotlinCast) appendLine("""
                
                private inline fun <reified T: Enum<T>> toKotlinEnumArray(arr: Int, free: Boolean): Array<T>? {
                    if(arr == 0) return null
                    val entries = enumEntries<T>()
                    val ints = toKotlinIntArray(arr, free)!!
                    return Array(ints.size) { entries[ints[it]] }
                }
            """.trimIndent())
        }

        // Typed arrays
        if(context.hasObjectArrays) {
            printLabel("Typed arrays")
            buildList {
                context.dictionaries.mapTo(this) { dictionary ->
                    Triple(dictionary.kname,
                        dictionary in context.usedObjectArrayToNativeCast,
                        dictionary in context.usedObjectArrayToKotlinCast)
                }
                context.interfaces.mapTo(this) { inter ->
                    Triple(inter.kname,
                        inter in context.usedObjectArrayToNativeCast,
                        inter in context.usedObjectArrayToKotlinCast)
                }
                if (context.hasStringArray) {
                    add(Triple("String",
                        context.hasStringArrayToNativeCast,
                        context.hasStringArrayToKotlinCast))
                }
            }.forEach { (name, hasToNativeCast, hasToKotlinCast) ->
                val lower = name.lowercase()

                if (hasToNativeCast) appendLine("""
                    
                    private fun toNative${name}Array(of: Array<$name?>?, nullableElements: Boolean): Int {
                        if(of == null) return 0
                        val arr = _module.array_${lower}_new(of.size, nullableElements)
                        of.forEach { _module.array_${lower}_push(arr, toNative$name(it), nullableElements) }
                        return arr
                    }
                    
                    @Suppress("unchecked_cast") private fun toNative${name}Array(of: Array<$name>?): Int =
                        toNative${name}Array(of as Array<${name}?>?, false)
                """.trimIndent())
                if (hasToKotlinCast) appendLine("""
                    
                    private fun toKotlin${name}Array(of: Int, free: Boolean, nullableElements: Boolean): Array<$name?>? {
                        if(of == 0) return null
                        return Array(_module.array_${lower}_length(of, nullableElements)) {
                            toKotlin$name(_module.array_${lower}_get(of, it, nullableElements), false)
                        }.also { if(free) _module.array_${lower}_free(of, nullableElements) }
                    }
                    
                    @Suppress("unchecked_cast") private fun toKotlin${name}Array(of: Int, free: Boolean): Array<$name>? =
                        toKotlin${name}Array(of, free, false) as Array<${name}>?
                """.trimIndent())
            }
        }
    }

    private fun StringBuilder.printCallbacks() {
        if(!context.hasCallbacks)
            return
        printLabel("Callbacks")

        if(context.language != Language.RUST) {
            append("""
                
                private fun _initializeCallbacks() {
                    _equals
                    _free
            """.trimIndent())
            context.usedCallbacks.joinTo(this, "") { "\n\t_invoke${it.kname}" }
            append("\n}\n")
        }

        append("""
            
            private val _callbacks = hashMapOf<Int, Any>()
            private var _counter = Int.MIN_VALUE
            
            @Suppress("unchecked_cast")
            private fun <T: Any> toKotlinCallback(id: Int): T =
                _callbacks[id] as T
                
            private fun _saveCallback(obj: Any): Int {
                do {
                    if(_counter == Int.MAX_VALUE)
                        _counter = Int.MIN_VALUE
                    _counter++
                } while (_counter in _callbacks)
                _callbacks[_counter] = obj
                return _counter
            }
            
        """.trimIndent())

        when (context.language) {
            Language.RUST -> appendLine("""
                
                private val _equals = { self: Int, second: Int ->
                    toKotlinCallback<Any>(self) == toKotlinCallback(second)
                }
                
                private val _free = { self: Int ->
                    _callbacks -= self
                }
            """.trimIndent())
            else -> appendLine("""

                private fun _create_equals(@Suppress("unused") callback: (Int, Int) -> Int): JsAny = js("callback")
                private val _equals by lazy {
                    _module.addFunction(_create_equals { self: Int, second: Int ->
                        (toKotlinCallback<Any>(self) == toKotlinCallback(second)).toInt()
                    }, "iii")
                }

                private fun _create_free(@Suppress("unused") callback: (Int) -> Unit): JsAny = js("callback")
                private val _free by lazy {
                    _module.addFunction(_create_free { self: Int ->
                        _callbacks -= self
                    }, "vi")
                }
            """.trimIndent())
        }

        context.usedCallbacks.forEach { callback ->
            val name = callback.kname
            val lower = name.lowercase()

            val args = buildList {
                add("_self: Int")
                callback.args.mapTo(this) {
                    "${it.kname}: ${it.type.toKtJsType()}"
                }
            }.joinToString()

            val castedArgs = callback.args.joinToString {
                castToKotlin(it.type, it.kname, free = true)
            }

            if(callback in context.toNativeDeclarations) {
                when (context.language) {
                    Language.RUST -> appendLine("""
                        
                        private val _invoke$name = { $args ->
                            ${castToNative(callback.type, "toKotlinCallback<$name>(_self)($castedArgs)")}
                        }
                    """.trimIndent())
                    else -> {
                        val argTypes = buildList {
                            add("Int")
                            callback.args.mapTo(this) { it.type.toKtJsType() }
                        }.joinToString()
                        val desc = buildString {
                            append(callback.type.toInternalDesc())
                            append("i")
                            callback.args.joinTo(this, separator = "") { it.type.toInternalDesc() }
                        }
                        appendLine("""
                            
                            private fun _create$name(@Suppress("unused") callback: ($argTypes) -> ${callback.type.toKtJsType()}): JsAny = js("callback")
                            private val _invoke$name by lazy {
                                _module.addFunction(_create$name { $args ->
                                    ${castToNative(callback.type, "toKotlinCallback<$name>(_self)($castedArgs)")}
                                }, "$desc")
                            }
                        """.trimIndent())
                    }
                }
                appendLine("""
                    
                    private fun toNative$name(of: $name?): Int {
                        if(of == null) return 0
                        return _module.${lower}_new(_saveCallback(of), of.hashCode(), _invoke$name, _equals, _free)
                    }
                """.trimIndent())
            }
            if(callback in context.toKotlinDeclarations) {
                appendLine("""
                    
                    private fun toKotlin$name(of: Int, free: Boolean): $name? {
                        if(of == 0) return null
                        return toKotlinCallback<$name>(_module.${lower}_id(of))
                            .also { if(free) _module.${lower}_free(of) }
                    }
                """.trimIndent())
            }
        }
    }

    private fun StringBuilder.printModuleApi() {
        printLabel("Module API")

        val prefix = if(context.language == Language.RUST) "" else "_"
        fun jsMangle(key: String) = prefix + context.jsMangle[key]

        when (context.language) {
            Language.RUST -> {
                appendLine("""
                    
                    private external interface InitOutput: JsAny {
                        val memory: WebAssemblyMemory
                    }
                    
                    @JsModule("$fileName") ${if(isWasm) "" else "@JsNonModule"}
                    private external object RustLib: JsAny {
                """.trimIndent())
                appendLine(when (context.module.jsTarget) {
                    JsTarget.NODE -> "\tfun initSync(module: JsAny): InitOutput"
                    else -> "\tfun default(): Promise<InitOutput>"
                })
            }
            else -> appendLine("""
                
                @JsModule("$fileName") ${if (isWasm) "" else "@JsNonModule"}
                private external object EmscriptenLib: JsAny {
                    fun default(): Promise<EmscriptenModule>
                }
                
                private external interface EmscriptenModule: JsAny {
                    val wasmMemory: WebAssemblyMemory
                    fun addFunction(func: JsAny, signature: String): Int
            """.trimIndent())
        }

        if(context.needsAllocFunctions) append("""
            
            @JsName("${jsMangle("alloc")}") fun alloc(size: Int): Int
            @JsName("${jsMangle("dealloc")}") fun dealloc(ptr: Int, size: Int)
        """.replaceIndent("\t"))

        if(context.hasString) {
            if(context.hasStringToNativeCast) append("""
                
                @JsName("${jsMangle("string_new")}") fun string_new(data: Int, length: Int, size: Int, makeCopy: Boolean): Int
            """.replaceIndent("\t"))
            if(context.hasStringToKotlinCast) append("""
                
                @JsName("${jsMangle("string_data")}") fun string_data(self: Int): Int
                @JsName("${jsMangle("string_size")}") fun string_size(self: Int): Int
                @JsName("${jsMangle("string_free")}") fun string_free(self: Int)
            """.trimIndent())
        }

        // Primitive arrays
        buildList {
            add(Triple("char",
                context.hasCharArrayToNativeCast,
                context.hasCharArrayToKotlinCast))
            add(Triple("boolean",
                context.hasBooleanArrayToNativeCast,
                context.hasBooleanArrayToKotlinCast))
            add(Triple("byte",
                context.hasByteArrayToNativeCast || context.hasUByteArrayToNativeCast || context.hasCriticalString || context.hasCriticalStringOpt,
                context.hasByteArrayToKotlinCast || context.hasUByteArrayToKotlinCast))
            add(Triple("short",
                context.hasShortArrayToNativeCast || context.hasUShortArrayToNativeCast,
                context.hasShortArrayToKotlinCast || context.hasUShortArrayToKotlinCast))
            add(Triple("int",
                context.hasIntArrayToNativeCast || context.hasUIntArrayToNativeCast || context.hasEnumArrayToNativeCast,
                context.hasIntArrayToKotlinCast || context.hasUIntArrayToKotlinCast || context.hasEnumArrayToKotlinCast))
            add(Triple("long",
                context.hasLongArrayToNativeCast || context.hasULongArrayToNativeCast,
                context.hasLongArrayToKotlinCast || context.hasULongArrayToKotlinCast))
            add(Triple("float", context.hasFloatArrayToNativeCast, context.hasFloatArrayToKotlinCast))
            add(Triple("double", context.hasDoubleArrayToNativeCast, context.hasDoubleArrayToKotlinCast))
        }.forEach { (name, hasToNativeCast, hasToKotlinCast) ->
            val name = "${name}array"
            if(hasToNativeCast) append("""
                
                @JsName("${jsMangle("${name}_new")}") fun ${name}_new(elements: Int, length: Int, makeCopy: Boolean): Int
            """.replaceIndent("\t"))
            if(hasToKotlinCast) append("""
                
                @JsName("${jsMangle("${name}_elements")}") fun ${name}_elements(self: Int): Int
                @JsName("${jsMangle("${name}_length")}") fun ${name}_length(self: Int): Int
                @JsName("${jsMangle("${name}_free")}") fun ${name}_free(self: Int)
            """.replaceIndent("\t"))
        }
        // Types arrays
        buildList {
            context.dictionaries.mapTo(this) { dictionary ->
                Triple(dictionary.name.camelCase().lowercase(),
                    dictionary in context.usedObjectArrayToNativeCast,
                    dictionary in context.usedObjectArrayToKotlinCast)
            }
            context.interfaces.mapTo(this) { inter ->
                Triple(inter.name.camelCase().lowercase(),
                    inter in context.usedObjectArrayToNativeCast,
                    inter in context.usedObjectArrayToKotlinCast)
            }
            if (context.hasStringArray) {
                add(Triple("string",
                    context.hasStringArrayToNativeCast,
                    context.hasStringArrayToKotlinCast))
            }
        }.forEach { (name, hasToNativeCast, hasToKotlinCast) ->
            if(hasToNativeCast) append("""
                
                @JsName("${jsMangle("array_${name}_new")}") fun array_${name}_new(capacity: Int, nullable_elements: Boolean): Int
            """.replaceIndent("\t"))
            if(hasToKotlinCast) append("""
                
                @JsName("${jsMangle("array_${name}_length")}") fun array_${name}_length(arr: Int, nullable_elements: Boolean): Int
                @JsName("${jsMangle("array_${name}_push")}") fun array_${name}_push(arr: Int, element: Int, nullable_elements: Boolean)
                @JsName("${jsMangle("array_${name}_get")}") fun array_${name}_get(arr: Int, index: Int, nullable_elements: Boolean): Int
                @JsName("${jsMangle("array_${name}_free")}") fun array_${name}_free(self: Int, nullable_elements: Boolean)
            """.replaceIndent("\t"))
        }

        // Dictionaries
        context.usedDictionaries.forEach { dictionary ->
            val name = dictionary.name.camelCase().lowercase()
            val fields = dictionary.allFields()
            val args = fields.joinToString { "${it.kname}: ${it.type.toKtJsType()}" }

            if(dictionary in context.toNativeDeclarations) append("""
                
                @JsName("${jsMangle("${name}_new")}") fun ${name}_new($args): Int
            """.replaceIndent("\t"))
            if(dictionary in context.toKotlinDeclarations) {
                append("""
                    
                    @JsName("${jsMangle("${name}_free")}") fun ${name}_free(self: Int)
                """.replaceIndent("\t"))
                fields.forEach {
                    val name = "${name}__${it.name.camelCase().lowercase()}"
                    append("\n\t@JsName(\"${jsMangle(name)}\") fun $name(self: Int): ${it.type.toKtJsType()}")
                }
            }
        }

        // Callbacks
        context.usedCallbacks.forEach { callback ->
            val name = callback.name.camelCase().lowercase()
            val useFuncDesc = context.language == Language.RUST

            val invoke = if(useFuncDesc) buildString {
                buildList {
                    add("Int")
                    callback.args.mapTo(this) { it.type.toKtJsType() }
                }.joinTo(this, prefix = "(", postfix = ") -> ")
                append(callback.type.toKtJsType())
            } else "Int"
            val equals = if(useFuncDesc) "(Int, Int) -> Boolean" else "Int"
            val free = if(useFuncDesc) "(Int) -> Unit" else "Int"

            if(callback in context.toNativeDeclarations) {
                append("""
                    
                    @JsName("${jsMangle("${name}_new")}") fun ${name}_new(id: Int, hash_code: Int, invoke: $invoke, equals: $equals, free: $free): Int
                """.replaceIndent("\t"))
            }
            if(callback in context.toKotlinDeclarations) {
                append("""
                    @JsName("${jsMangle("${name}_id")}") fun ${name}_id(self: Int): Int
                    @JsName("${jsMangle("${name}_free")}") fun ${name}_free(self: Int): Int
                """.replaceIndent("\t"))
            }
        }

        // Operations
        context.allOperations.forEach { operation ->
            val name = operation.cname
            val type = when {
                operation.isInterfaceOperationAddress() -> ": Int"
                !operation.type.isVoid() -> ": ${operation.type.toKtJsType()}"
                else -> ""
            }
            val critical = operation.isCritical()
            val args = operation.args.joinToString {
                when {
                    critical && it.type.isString() -> "${it.kname}: ${it.type.toKtJsType()}, _${it.kname}_size: Int, _${it.kname}_length: Int"
                    critical && it.type.isArray() -> "${it.kname}: ${it.type.toKtJsType()}, _${it.kname}_length: Int"
                    else -> "${it.kname}: ${it.type.toKtJsType()}"
                }
            }
            append("\n\t@JsName(\"${jsMangle(name)}\") fun $name($args)$type")
        }
        append("\n}\n")
    }

    private fun StringBuilder.printDictionaries() {
        if(!context.hasDictionaries)
            return
        printLabel("Dictionaries")

        context.usedDictionaries.forEach { dictionary ->
            val name = dictionary.kname
            val lower = name.lowercase()
            val fields = context.allFields[dictionary]!!

            if(dictionary in context.toNativeDeclarations) {
                append("""
                    
                    private fun toNative$name(of: $name?) = of?.run {
                        _module.${lower}_new(
                """.trimIndent())
                fields.forEachIndexed { i, it ->
                    append("\n\t\t")
                    append(castToNative(it.type, it.kname)).append(",")
                    if(i == fields.lastIndex)
                        append("\n\t")
                }
                append(")\n} ?: 0\n")
            }
            if(dictionary in context.toKotlinDeclarations) {
                append("""
                    
                    private fun toKotlin$name(of: Int, free: Boolean): $name? {
                        if(of == 0) return null
                        return $name(
                """.trimIndent())
                fields.forEachIndexed { i, it ->
                    append("\n\t\t")
                    append(castToKotlin(it.type, "_module.${lower}__${it.kname}(of)")).append(",")
                    if(i == fields.lastIndex)
                        append("\n\t")
                }
                append(").also { if(free) _module.${lower}_free(of) }\n}\n")
            }
        }
    }

    private fun StringBuilder.printFunctions() {
        if(context.allOperations.isEmpty())
            return
        printLabel("Functions")

        context.allOperations.forEach { function ->
            val critical = function.isCritical()
            val isInterface = function.isInterfaceOperation()

            val args = function.args.joinToString {
                "${it.kname}: ${it.type.toKotlinType(rawInterfaceAsInt = true)}"
            }

            val type = when {
                function.isInterfaceOperationAddress() -> ": Int"
                function.type.isVoid() -> ""
                else -> ": ${function.type.toKotlinType(rawInterfaceAsInt = true)}"
            }

            val castedArgs = function.args.joinToString {
                val name = it.kname
                when {
                    critical && it.type.isString() ->
                        if(it.type.isNullable) "_${name}_native, $name?.length ?: -1, _${name}_bytes?.size ?: -1"
                        else "_${name}_native, $name.length, _${name}_bytes.size"
                    critical && it.type.isArray() ->
                        if(it.type.isNullable) "_${name}_native, $name?.size ?: -1"
                        else "_${name}_native, $name.size"
                    else -> castToNative(it.type, name)
                }
            }

            val call = "_module.${function.cname}($castedArgs)"

            val casts = arrayListOf<String>()
            val releases = arrayListOf<String>()

            if(critical) {
                function.args.forEach {
                    val name = it.kname
                    val native = "_${name}_native"
                    when {
                        it.type.isString() -> {
                            casts += if (it.type.isNullable) """
                                
                                val _${name}_bytes = $name?.encodeToByteArray()
                                val $native = toNativeByteArray(_${name}_bytes, pure = true)
                            """.replaceIndent("\t")
                            else """
                                
                                val _${name}_bytes = $name.encodeToByteArray()
                                val $native = toNativeByteArray(_${name}_bytes, pure = true)
                            """.replaceIndent("\t")
                            releases += "\n\t${if(it.type.isNullable) "if(_${it.kname}_bytes != null) " else "" }" +
                                "_module.dealloc(_${it.kname}_native, _${it.kname}_bytes.size)"
                        }
                        it.type.isArray() ->
                        casts += "\n\tval $native = ${castToNative(it.type, name, pureArrayData = true)}"
                    }
                    if(it.type.isArray()) {
                        val type = it.type.arrayTypeOrNull()!!
                        val typeSize = when {
                            type.isChar() || type.isShort() || type.isUShort() -> 2
                            type.isBoolean() || type.isByte() || type.isUByte() -> 1
                            type.isInt() || type.isUInt() || type.isFloat() || type.isEnum() || type.isInterface() -> 4
                            type.isLong() || type.isULong() || type.isDouble() -> 8
                            else -> throw UnsupportedOperationException(type.declaration.toString())
                        }
                        releases += "\n\t${if(it.type.isNullable) "if(${it.kname} != null) " else "" }" +
                            "_module.dealloc(_${it.kname}_native, ${it.kname}.size * $typeSize)"
                    }
                }
            }

            // === Print ===

            append('\n')
            if(expectActual && !isInterface)
                append("actual ")
            if(isInterface)
                append("private ")

            append("fun ${function.kname}(${args})$type ")
            append(if(casts.isNotEmpty()) "{" else "=")
            casts.forEach { append(it) }

            append(if(releases.isNotEmpty()) "\n\tval result = " else "\n\t")
            append(castToKotlin(function.type, call, free = true))

            releases.forEach { append(it) }

            append(if(releases.isNotEmpty()) "\n\treturn result\n}\n" else "\n")
        }
    }

    private fun StringBuilder.printInterfaces() {
        if(context.usedInterfaces.isEmpty())
            return
        printLabel("Interfaces")

        appendLine("""
            
            @Suppress("unused")
            private fun _createCleaner(target: JsReference<*>, heldValue: JsAny, callback: (JsAny) -> Unit): JsAny =
                js("(function() { let registry = new FinalizationRegistry(callback); registry.register(target, heldValue); return registry; })()")
        """.trimIndent())

        context.usedInterfaces.forEach { inter ->
            val name = inter.kname

            if(inter in context.toNativeDeclarations) appendLine("""
                
                private fun toNative$name(obj: $name?): Int {
                    if(obj == null) return 0
                	return _interface${name}Clone(obj._ptr)
                }
            """.trimIndent())
            if(inter in context.toKotlinDeclarations) appendLine("""
                
                private fun toKotlin$name(ptr: Int, free: Boolean): $name? {
                    if(ptr == 0) return null
                	return $name(_module, _interface${name}Clone(ptr))
                        .also { if(free) _module._interface_${name.lowercase()}_free(ptr) }
                }
            """.trimIndent())

            append("""
                
                actual class $name internal constructor(m: JsAny, val _ptr: Int): NativeKtRcObject(_ptr.toLong(), { _interface${name}Free(it.toInt()) }) {
                    @Suppress("unused", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "unchecked_cast") 
                    private val cleaner = _createCleaner(this.toJsReference(), releaser.toJsReference()) {
                        (it as JsReference<AtomicHandleReleaser<Long>>).get().release()
                    }
                    override fun _address(): Long = _interface${name}Address(_ptr).toLong()

            """.trimIndent())

            inter.toOperations().forEach { operation ->
                val args = operation.args.map {
                    "${it.kname}: ${it.type.toKotlinType()}"
                }
                val argNames = operation.args.map { it.kname }
                val kname = operation.kname

                append(when {
                    operation.isInterfaceOperationConstructor() ->
                        "\n\tactual constructor(${args.joinToString()}): this(_module, $kname(${argNames.joinToString()}))"

                    operation.isInterfaceOperationFn() -> {
                        val args = args.drop(1).joinToString()
                        val argNames = argNames.toMutableList()
                            .apply { set(0, "_ptr") }
                            .joinToString()
                        "\n\tactual fun ${operation.interfaceFunctionName().camelCase()}($args) = $kname($argNames)"
                    }
                    else -> return@forEach
                })
            }
            append("\n}\n")
        }
    }

    private fun castToNative(
        type: ResolvedIdlType,
        content: String,
        pureArrayData: Boolean = false
    ): String {
        val isRust = context.language == Language.RUST
        val pure = if(pureArrayData) ", pure = true" else ""
        return when {
            !isRust && type.isBoolean() -> "$content.toInt()"
            isRust && type.isUInt() -> "$content.toDouble()"
            type.isUByte() || type.isUShort() -> castToSigned(type, content, smallTypesAsInt = true)
            type.isUnsigned() -> castToNative(type.toSignedType(), castToSigned(type, content), pureArrayData)
            type.isChar() -> "$content.code"
            type.isByte() || type.isUByte() ||
                    type.isShort() || type.isUShort() ||
                    type.isUInt() -> "$content.toInt()"
            type.isULong() -> "$content.toLong()"
            type.isEnum() -> "$content.ordinal"
            type.isString() -> "toNativeString($content)"
            type.isRawInterface() -> content
            type.isCallback() || type.isDictionary() || type.isInterface() ->
                "toNative${type.declaration.kname}($content)"
            type.isArray() -> type.arrayType { type ->
                val nullable = if(type.isNullable) ", nullableElements = true" else ""
                when {
                    type.isPrimitive() -> "toNative${type.toKotlinType()}Array($content$pure)"
                    type.isEnum() -> "toNativeEnumArray($content$pure)"
                    type.isString() -> "toNativeStringArray($content$nullable)"
                    else -> "toNative${type.declaration.kname}Array($content$nullable)"
                }
            }
            else -> content
        }
    }

    private fun castToKotlin(
        type: ResolvedIdlType,
        content: String,
        free: Boolean = false
    ): String {
        val isRust = context.language == Language.RUST
        val assert = if(type.isNullable) "" else "!!"
        return when {
            !isRust && type.isBoolean() -> "$content.toBoolean()"
            isRust && type.isULong() -> "$content.fromUnsignedBigInt()"
            type.isUnsigned() -> castToUnsigned(type, castToKotlin(type.toSignedType(), content, free))
            type.isChar() -> "$content.toChar()"
            type.isByte() -> "$content.toByte()"
            type.isUByte() -> "$content.toUByte()"
            type.isShort() -> "$content.toShort()"
            type.isUShort() -> "$content.toUShort()"
            type.isUInt() -> "$content.toUInt()"
            type.isULong() -> "$content.toULong()"
            type.isFloat() -> "$content.truncF32()"
            type.isEnum() -> "${type.declaration.name}.entries[$content]"
            type.isString() -> "toKotlinString($content, free = $free)$assert"
            type.isRawInterface() -> content
            type.isCallback() || type.isDictionary() || type.isInterface() ->
                "toKotlin${type.declaration.kname}($content, free = $free)$assert"
            type.isArray() -> type.arrayType { type ->
                val nullable = if(type.isNullable) ", nullableElements = true" else ""
                when {
                    type.isPrimitive() -> "toKotlin${type.toKotlinType()}Array($content, free = $free)$assert"
                    type.isEnum() -> "toKotlinEnumArray<${type.declaration.name}>($content, free = $free)$assert"
                    type.isString() -> "toKotlinStringArray($content, free = $free$nullable)$assert"
                    else -> "toKotlin${type.declaration.kname}Array($content, free = $free$nullable)$assert"
                }
            }
            else -> content
        }
    }

    private fun ResolvedIdlType.toKtJsType(): String {
        val isRust = context.language == Language.RUST
        return when {
            isRust && isUInt() -> "Double"
            isRust && isBoolean() -> "Boolean"
            isVoid() -> "Unit"
            isFloat() -> "Float"
            isDouble() -> "Double"
            isLong() || isULong() -> "Long"
            else -> "Int"
        }
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