package com.huskerdev.nativekt.printers.kotlin.jvm

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.printers.kotlin.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*

const val TYPE_VOID = "null"
const val TYPE_ADDRESS = "ValueLayout.ADDRESS"
const val TYPE_CHAR = "ValueLayout.JAVA_CHAR"
const val TYPE_BYTE = "ValueLayout.JAVA_BYTE"
const val TYPE_SHORT = "ValueLayout.JAVA_SHORT"
const val TYPE_INT = "ValueLayout.JAVA_INT"
const val TYPE_BOOLEAN = "ValueLayout.JAVA_BOOLEAN"
const val TYPE_LONG = "ValueLayout.JAVA_LONG"
const val TYPE_FLOAT = "ValueLayout.JAVA_FLOAT"
const val TYPE_DOUBLE = "ValueLayout.JAVA_DOUBLE"

class KotlinJvmForeignPrinter(
    private val context: NativeModuleContext,
    builder: StringBuilder,
    private val name: String = "Foreign",
    private val parentClass: String? = null,
    private val indent: String = ""
) {
    private val indent1 = indent + "\t"
    private val indent2 = indent1 + "\t"

    init {
        builder.apply {
            printHeader()
            printHandles()
            printBasicCasts()
            printInterfaces()
            printDictionaries()
            printCallbacks()
            printFunctions()
            append("${indent}}")
        }
    }

    private fun StringBuilder.printHeader() {
        val parent = if(parentClass != null) ": $parentClass" else ""
        append("""
            private class $name(libraryPath: String)$parent {
                private val _handle = SymbolLookup.libraryLookup(java.nio.file.Paths.get(libraryPath), Arena.ofAuto())
                
                override fun _address(name: String): Long =
                    _handle.find(name).orElseThrow().address()
            
        """.replaceIndent(indent))
    }

    private fun StringBuilder.printHandles() {
        fun printHandle(name: String, isCritical: Boolean, retType: String, vararg args: String) {
            val args = buildList {
                add(retType)
                addAll(args)
            }.joinToString()
            append("\n${indent1}private val _${name.camelCase()} = lookup(_handle, \"${context.mangle(name)}\", $isCritical, $args)")
        }

        // String
        if(context.hasStringCastToNative)
            printHandle("string_new", true, TYPE_ADDRESS, TYPE_ADDRESS, TYPE_INT, TYPE_INT, TYPE_BOOLEAN)
        if(context.hasStringCastToKotlin) {
            printHandle("string_data", true, TYPE_ADDRESS, TYPE_ADDRESS)
            printHandle("string_length", true, TYPE_INT, TYPE_ADDRESS)
            printHandle("string_size", true, TYPE_LONG, TYPE_ADDRESS)
            printHandle("string_free", true, TYPE_VOID, TYPE_ADDRESS)
        }

        // Primitive arrays
        listOf(
            Triple("char", context.hasCharArrayCastToNative, context.hasCharArrayCastToKotlin),
            Triple("boolean", context.hasBooleanArrayCastToNative, context.hasBooleanArrayCastToKotlin),
            Triple("byte",
                context.hasByteArrayCastToNative || context.hasUByteArrayCastToNative,
                context.hasByteArrayCastToKotlin || context.hasUByteArrayCastToKotlin),
            Triple("short",
                context.hasShortArrayCastToNative || context.hasUShortArrayCastToNative,
                context.hasShortArrayCastToKotlin || context.hasUShortArrayCastToKotlin),
            Triple("int",
                context.hasIntArrayCastToNative || context.hasEnumArrayCastToNative || context.hasUIntArrayCastToNative,
                context.hasIntArrayCastToKotlin || context.hasEnumArrayCastToKotlin || context.hasUIntArrayCastToKotlin),
            Triple("long",
                context.hasLongArrayCastToNative || context.hasULongArrayCastToNative,
                context.hasLongArrayCastToKotlin || context.hasULongArrayCastToKotlin),
            Triple("float", context.hasFloatArrayCastToNative, context.hasFloatArrayCastToKotlin),
            Triple("double", context.hasDoubleArrayCastToNative, context.hasDoubleArrayCastToKotlin),
        ).forEach { (name, hasToNative, hasToKotlin) ->
            if(hasToNative)
                printHandle("${name}array_new", true, TYPE_ADDRESS, TYPE_ADDRESS, TYPE_INT, TYPE_BOOLEAN)
            if(hasToKotlin) {
                printHandle("${name}array_elements", true, TYPE_ADDRESS, TYPE_ADDRESS)
                printHandle("${name}array_length", true, TYPE_INT, TYPE_ADDRESS)
                printHandle("${name}array_free", true, TYPE_VOID, TYPE_ADDRESS)
            }
        }

        // Object arrays
        buildList {
            context.castedDictionaries.mapTo(this) { dictionary ->
                Triple(dictionary.cname.lowercase(),
                    dictionary in context.usedObjectArrayCastToNative,
                    dictionary in context.usedObjectArrayCastToKotlin)
            }
            context.castedInterfaces.mapTo(this) { inter ->
                Triple(inter.cname.lowercase(),
                    inter in context.usedObjectArrayCastToNative,
                    inter in context.usedObjectArrayCastToKotlin)
            }
            add(Triple("string",
                context.hasStringArrayCastToNative,
                context.hasStringArrayCastToKotlin))
        }.forEach { (name, hasToNative, hasToKotlin) ->
            if(hasToNative) {
                printHandle("array_${name}_new", true, TYPE_ADDRESS, TYPE_INT, TYPE_BOOLEAN)
                printHandle("array_${name}_push", true, TYPE_VOID, TYPE_ADDRESS, TYPE_ADDRESS, TYPE_BOOLEAN)
            }
            if(hasToKotlin) {
                printHandle("array_${name}_length", true, TYPE_INT, TYPE_ADDRESS, TYPE_BOOLEAN)
                printHandle("array_${name}_get", true, TYPE_ADDRESS, TYPE_ADDRESS, TYPE_INT, TYPE_BOOLEAN)
                printHandle("array_${name}_free", false, TYPE_VOID, TYPE_ADDRESS, TYPE_BOOLEAN)
            }
        }

        // Dictionary
        context.castedDictionaries.forEach { dictionary ->
            val lower = dictionary.name.camelCase().lowercase()
            val fields = context.allFields[dictionary]!!

            if(dictionary in context.toNativeDeclarationCasts)
                printHandle("${lower}_new", true, TYPE_ADDRESS, *fields.map { it.type.toForeignType() }.toTypedArray())
            if(dictionary in  context.toKotlinDeclarationCasts) {
                printHandle("${lower}_free", false, TYPE_VOID, TYPE_ADDRESS)
                fields.forEach { field ->
                    printHandle("${lower}__${field.cname}", true, field.type.toForeignType(), TYPE_ADDRESS)
                }
            }
        }

        // Callbacks
        context.castedCallbacks.forEach { callback ->
            val lower = callback.name.camelCase().lowercase()

            if(callback in context.toNativeDeclarationCasts)
                printHandle("${lower}_new", true, TYPE_ADDRESS, TYPE_LONG, TYPE_INT, TYPE_ADDRESS, TYPE_ADDRESS, TYPE_ADDRESS)
            if(callback in context.toKotlinDeclarationCasts) {
                printHandle("${lower}_id", true, TYPE_LONG, TYPE_ADDRESS)
                printHandle("${lower}_free", false, TYPE_VOID, TYPE_ADDRESS)
            }
        }

        // Functions
        context.allOperations.forEach { operation ->
            val critical = operation.isCritical()
            val args = buildList {
                add(operation.type.toForeignType())
                operation.args.mapTo(this) {
                    when {
                        critical && it.type.isString() -> "$TYPE_ADDRESS, $TYPE_INT, $TYPE_INT"
                        critical && it.type.isArray() -> "$TYPE_ADDRESS, $TYPE_INT"
                        else -> it.type.toForeignType()
                    }
                }
            }.joinToString()
            append("\n${indent1}private val ${operation.cname.camelCase()} = lookup(_handle, \"${operation.cnameMangled(context)}\", ${operation.isCritical()}, $args)")
        }
        append("\n")
    }

    private fun StringBuilder.printBasicCasts() {
        if(context.hasStringCast) {
            printLabel("String", indent = indent1)

            if (context.hasStringCastToNative) appendLine("""
                
                private fun toNativeString(of: String?): MemorySegment {
                    of ?: return MemorySegment.NULL
                    val bytes = of.toByteArray()
                    return _stringNew(MemorySegment.ofArray(bytes), of.length, bytes.size, true) as MemorySegment
                }
            """.replaceIndent(indent1))
            if (context.hasStringCastToKotlin) appendLine("""
                
                private fun toKotlinString(of: MemorySegment, free: Boolean): String? {
                    if (of.address() == 0L) return null
                    val data = (_stringData(of) as MemorySegment).reinterpret(_stringSize(of) as Long)
                    val bytes = ByteArray(data.byteSize().toInt())
                    MemorySegment.copy(data, $TYPE_BYTE, 0, bytes, 0, data.byteSize().toInt())
                    return String(bytes, java.nio.charset.StandardCharsets.UTF_8)
                        .also { if(free) _stringFree(of) }
                }
            """.replaceIndent(indent1))
        }

        if(context.hasPrimitiveArrayCast || context.hasEnumArrayCast) {
            printLabel("Primitive arrays", indent = indent1)

            // CharArray
            if (context.hasCharArrayCast) appendLine("\n$indent1// Char")
            if (context.hasCharArrayCastToNative) appendLine("""
                
                private fun toNativeCharArray(of: CharArray?): MemorySegment {
                    of ?: return MemorySegment.NULL
                    return _chararrayNew(MemorySegment.ofArray(of), of.size, true) as MemorySegment
                }
            """.replaceIndent(indent1))
            if (context.hasCharArrayCastToKotlin) appendLine("""
                
                private fun toKotlinCharArray(of: MemorySegment, free: Boolean): CharArray? {
                    if (of.address() == 0L) return null
                    val length = _chararrayLength(of) as Int
                    val elements = (_chararrayElements(of) as MemorySegment).reinterpret(length * Char.SIZE_BYTES.toLong())
                    val result = CharArray(length)
                    MemorySegment.copy(elements, $TYPE_CHAR, 0, result, 0, length)
                    return result.also { if(free) _chararrayFree(of) }
                }
            """.replaceIndent(indent1))

            // BooleanArray
            if (context.hasBooleanArrayCast) appendLine("\n$indent1// Boolean")
            if (context.hasBooleanArrayCastToNative) appendLine("""
                
                private fun toNativeBooleanArray(of: BooleanArray?): MemorySegment {
                    of ?: return MemorySegment.NULL
                    return _booleanarrayNew(MemorySegment.ofArray(ByteArray(of.size) { if(of[it]) 1 else 0 }), of.size, true) as MemorySegment
                }
            """.replaceIndent(indent1))
            if (context.hasBooleanArrayCastToKotlin) appendLine("""
                
                private fun toKotlinBooleanArray(of: MemorySegment, free: Boolean): BooleanArray? {
                    if (of.address() == 0L) return null
                    val length = _booleanarrayLength(of) as Int
                    val elements = (_booleanarrayElements(of) as MemorySegment).reinterpret(length.toLong())
                    val result = ByteArray(length)
                    MemorySegment.copy(elements, $TYPE_BYTE, 0, result, 0, length)
                    return BooleanArray(result.size) { result[it] == 1.toByte() }
                        .also { if(free) _booleanarrayFree(of) }
                }
            """.replaceIndent(indent1))

            // ByteArray
            if (context.hasByteArrayCast || context.hasUByteArrayCast || context.hasBooleanArrayCast) appendLine("\n$indent1// Byte")
            if (context.hasByteArrayCastToNative || context.hasUByteArrayCastToNative) appendLine("""
                
                private fun toNativeByteArray(of: ByteArray?): MemorySegment {
                    of ?: return MemorySegment.NULL
                    return _bytearrayNew(MemorySegment.ofArray(of), of.size, true) as MemorySegment
                }
            """.replaceIndent(indent1))
            if (context.hasByteArrayCastToKotlin || context.hasUByteArrayCastToKotlin) appendLine("""
                
                private fun toKotlinByteArray(of: MemorySegment, free: Boolean): ByteArray? {
                    if (of.address() == 0L) return null
                    val length = _bytearrayLength(of) as Int
                    val elements = (_bytearrayElements(of) as MemorySegment).reinterpret(length.toLong())
                    val result = ByteArray(length)
                    MemorySegment.copy(elements, $TYPE_BYTE, 0, result, 0, length)
                    return result.also { if(free) _bytearrayFree(of) }
                }
            """.replaceIndent(indent1))

            // ShortArray
            if (context.hasShortArrayCast || context.hasUShortArrayCast) appendLine("\n$indent1// Short")
            if (context.hasShortArrayCastToNative || context.hasUShortArrayCastToNative) appendLine("""
                
                private fun toNativeShortArray(of: ShortArray?): MemorySegment {
                    of ?: return MemorySegment.NULL
                    return _shortarrayNew(MemorySegment.ofArray(of), of.size, true) as MemorySegment
                }
            """.replaceIndent(indent1))
            if (context.hasShortArrayCastToKotlin || context.hasUShortArrayCastToKotlin) appendLine("""
                
                private fun toKotlinShortArray(of: MemorySegment, free: Boolean): ShortArray? {
                    if (of.address() == 0L) return null
                    val length = _shortarrayLength(of) as Int
                    val elements = (_shortarrayElements(of) as MemorySegment).reinterpret(length * Short.SIZE_BYTES.toLong())
                    val result = ShortArray(length)
                    MemorySegment.copy(elements, $TYPE_SHORT, 0, result, 0, length)
                    return result.also { if(free) _shortarrayFree(of) }
                }
            """.replaceIndent(indent1))

            // IntArray
            if (context.hasIntArrayCast || context.hasUIntArrayCast || context.hasEnumArrayCast) appendLine("\n$indent1// Int")
            if (context.hasIntArrayCastToNative || context.hasUIntArrayCastToNative || context.hasEnumArrayCastToNative) appendLine("""
                
                private fun toNativeIntArray(of: IntArray?): MemorySegment {
                    of ?: return MemorySegment.NULL
                    return _intarrayNew(MemorySegment.ofArray(of), of.size, true) as MemorySegment
                }
            """.replaceIndent(indent1))
            if (context.hasIntArrayCastToKotlin || context.hasUIntArrayCastToKotlin || context.hasEnumArrayCastToKotlin) appendLine("""
                
                private fun toKotlinIntArray(of: MemorySegment, free: Boolean): IntArray? {
                    if (of.address() == 0L) return null
                    val length = _intarrayLength(of) as Int
                    val elements = (_intarrayElements(of) as MemorySegment).reinterpret(length * Int.SIZE_BYTES.toLong())
                    val result = IntArray(length)
                    MemorySegment.copy(elements, $TYPE_INT, 0, result, 0, length)
                    return result.also { if(free) _intarrayFree(of) }
                }
            """.replaceIndent(indent1))

            // LongArray
            if (context.hasLongArrayCast || context.hasULongArrayCast) appendLine("\n$indent1// Long")
            if (context.hasLongArrayCastToNative || context.hasULongArrayCastToNative) appendLine("""
                
                private fun toNativeLongArray(of: LongArray?): MemorySegment {
                    of ?: return MemorySegment.NULL
                    return _longarrayNew(MemorySegment.ofArray(of), of.size, true) as MemorySegment
                }
            """.replaceIndent(indent1))
            if (context.hasLongArrayCastToKotlin || context.hasULongArrayCastToKotlin) appendLine("""
                
                private fun toKotlinLongArray(of: MemorySegment, free: Boolean): LongArray? {
                    if (of.address() == 0L) return null
                    val length = _longarrayLength(of) as Int
                    val elements = (_longarrayElements(of) as MemorySegment).reinterpret(length * Long.SIZE_BYTES.toLong())
                    val result = LongArray(length)
                    MemorySegment.copy(elements, $TYPE_LONG, 0, result, 0, length)
                    return result.also { if(free) _longarrayFree(of) }
                }
            """.replaceIndent(indent1))

            // FloatArray
            if (context.hasFloatArrayCast) appendLine("\n$indent1// Float")
            if (context.hasFloatArrayCastToNative) appendLine("""
                
                private fun toNativeFloatArray(of: FloatArray?): MemorySegment {
                    of ?: return MemorySegment.NULL
                    return _floatarrayNew(MemorySegment.ofArray(of), of.size, true) as MemorySegment
                }
            """.replaceIndent(indent1))
            if (context.hasFloatArrayCastToKotlin) appendLine("""
                
                private fun toKotlinFloatArray(of: MemorySegment, free: Boolean): FloatArray? {
                    if (of.address() == 0L) return null
                    val length = _floatarrayLength(of) as Int
                    val elements = (_floatarrayElements(of) as MemorySegment).reinterpret(length * Float.SIZE_BYTES.toLong())
                    val result = FloatArray(length)
                    MemorySegment.copy(elements, $TYPE_FLOAT, 0, result, 0, length)
                    return result.also { if(free) _floatarrayFree(of) }
                }
            """.replaceIndent(indent1))

            // DoubleArray
            if (context.hasDoubleArrayCast) appendLine("\n$indent1// Double")
            if (context.hasDoubleArrayCastToNative) appendLine("""
                
                private fun toNativeDoubleArray(of: DoubleArray?): MemorySegment {
                    of ?: return MemorySegment.NULL
                    return _doublearrayNew(MemorySegment.ofArray(of), of.size, true) as MemorySegment
                }
            """.replaceIndent(indent1))
            if (context.hasDoubleArrayCastToKotlin) appendLine("""
                
                private fun toKotlinDoubleArray(of: MemorySegment, free: Boolean): DoubleArray? {
                    if (of.address() == 0L) return null
                    val length = _doublearrayLength(of) as Int
                    val elements = (_doublearrayElements(of) as MemorySegment).reinterpret(length * Double.SIZE_BYTES.toLong())
                    val result = DoubleArray(length)
                    MemorySegment.copy(elements, $TYPE_DOUBLE, 0, result, 0, length)
                    return result.also { if(free) _doublearrayFree(of) }
                }
            """.replaceIndent(indent1))

            // Enum
            if (context.hasEnumArrayCast) appendLine("\n$indent1// Enum")
            if (context.hasEnumArrayCastToNative) appendLine("""
                
                fun <T : Enum<T>> toNativeEnumArray(of: Array<T>?): MemorySegment = of?.run {
                    toNativeIntArray(IntArray(of.size) { of[it].ordinal })
                } ?: MemorySegment.NULL
            """.replaceIndent(indent1))
            if (context.hasEnumArrayCastToKotlin) appendLine("""
                
                @Suppress("unchecked_cast")
                fun <T : Enum<T>> toKotlinEnumArray(of: MemorySegment, enumClass: Class<T>, free: Boolean): Array<T>? {
                    if (of.address() == 0L) return null
                    val ordinals = toKotlinIntArray(of, free)!!
                    val enumConstants = enumClass.getEnumConstants()
                    val result = java.lang.reflect.Array.newInstance(enumClass, ordinals.size) as Array<T>
                    for (i in ordinals.indices)
                        result[i] = enumConstants[ordinals[i]]
                    return result
                }
            """.replaceIndent(indent1))
        }

        if(context.hasObjectArraysCast) {
            printLabel("Object arrays", indent = indent1)

            buildList {
                context.dictionaries.mapTo(this) { dictionary ->
                    Triple(dictionary.kname,
                        dictionary in context.usedObjectArrayCastToNative,
                        dictionary in context.usedObjectArrayCastToKotlin)
                }
                context.interfaces.mapTo(this) { inter ->
                    Triple(inter.kname,
                        inter in context.usedObjectArrayCastToNative,
                        inter in context.usedObjectArrayCastToKotlin)
                }
                add(Triple("String",
                    context.hasStringArrayCastToNative,
                    context.hasStringArrayCastToKotlin))
            }.forEach { (name, hasToNative, hasToKotlin) ->
                val lower = name.lowercase().uppercaseFirstChar()

                if (hasToNative || hasToKotlin) appendLine("\n$indent1// $name")
                if (hasToNative) appendLine("""
                    
                    private fun toNative${name}Array(of: Array<$name?>?, nullableElements: Boolean = false): MemorySegment {
                        of ?: return MemorySegment.NULL
                        val array = _array${lower}New(of.size, nullableElements) as MemorySegment
                        of.forEach {
                            _array${lower}Push(array, toNative$name(it), nullableElements)
                        }
                        return array
                    }
                    
                    @Suppress("unchecked_cast") private fun toNative${name}Array(arr: Array<$name>?) =
                        toNative${name}Array(arr as Array<$name?>?, false)
                """.replaceIndent(indent1))
                if (hasToKotlin) appendLine("""
                    
                    private fun toKotlin${name}Array(of: MemorySegment, nullableElements: Boolean = false, free: Boolean): Array<$name?>? {
                        if (of.address() == 0L) return null
                        return Array(_array${lower}Length(of, nullableElements) as Int) {
                            toKotlin$name(_array${lower}Get(of, it, nullableElements) as MemorySegment, false)
                        }.also { if(free) _array${lower}Free(of, nullableElements) }
                    }
                    
                    @Suppress("unchecked_cast") private fun toKotlin${name}Array(arr: MemorySegment, free: Boolean) =
                        toKotlin${name}Array(arr, false, free) as Array<$name>?
                """.replaceIndent(indent1))
            }
        }
    }

    private fun StringBuilder.printInterfaces() {
        if(!context.hasInterfaceCast)
            return
        printLabel("Interfaces", indent = indent1)

        context.castedInterfaces.forEach { inter ->
            val name = inter.kname
            val lower = inter.name.camelCase().lowercase().uppercaseFirstChar()

            appendLine("\n$indent1// $name")

            if(inter in context.toNativeDeclarationCasts) appendLine("""
                
                private fun toNative$name(of: $name?): MemorySegment {
                    of ?: return MemorySegment.NULL
                    return interface${lower}Clone(MemorySegment.ofAddress(of.rcPtr)) as MemorySegment
                }
            """.replaceIndent(indent1))
            if(inter in context.toKotlinDeclarationCasts) appendLine("""
                
                private fun toKotlin$name(of: MemorySegment, free: Boolean): $name? {
                    if(of.address() == 0L) return null
                    return $name(Unit, (interface${lower}Clone(of) as MemorySegment).address())
                        .also { if(free) interface${lower}Free(of) }
                }
            """.replaceIndent(indent1))
        }
    }

    private fun StringBuilder.printDictionaries() {
        if(!context.hasDictionaryCast)
            return
        printLabel("Dictionaries", indent = indent1)

        context.castedDictionaries.forEach { dictionary ->
            val name = dictionary.kname
            val lower = dictionary.name.camelCase().lowercase()
            val fields = context.allFields[dictionary]!!

            appendLine("\n$indent1// $name")

            if(dictionary in context.toNativeDeclarationCasts) {
                append("""
                    
                    private fun toNative$name(of: $name?): MemorySegment {
                        of ?: return MemorySegment.NULL
                        return _${lower}New(
                """.replaceIndent(indent1))
                fields.forEach {
                    val value = "of.${it.kname}"
                    val casted = castToNative(it.type, value)
                    append("\n$indent1\t\t$casted,")
                }
                appendLine("""
                        
                        ) as MemorySegment
                    }
                """.replaceIndent(indent1))
            }
            if(dictionary in context.toKotlinDeclarationCasts) {
                append("""
                    
                    private fun toKotlin$name(of: MemorySegment, free: Boolean): $name? {
                        if(of.address() == 0L) return null
                        return $name(
                """.replaceIndent(indent1))
                fields.forEach {
                    val value = "_${lower}${it.cname.camelCase().uppercaseFirstChar()}(of) as ${it.type.toForeignKotlinType()}"
                    val casted = castToKotlin(it.type, value, false, brackets = true)
                    append("\n$indent1\t\t$casted,")
                }
                appendLine("""
                        
                        ).also { if(free) _${lower}Free(of) }
                    }
                """.replaceIndent(indent1))
            }
        }
    }

    private fun StringBuilder.printCallbacks() {
        if(!context.hasCallbackCast)
            return
        printLabel("Callbacks", indent = indent1)

        appendLine("""
            
            private val _callbacks = java.util.concurrent.ConcurrentHashMap<Long, Any>()
            private var _counter = AtomicLong(Long.MIN_VALUE)
            
            @Suppress("unchecked_cast")
            private fun <T> getCallback(id: Long): T =
                (_callbacks[id] as T?)!!
            
            private fun _saveCallback(obj: Any): Long {
                var i: Long
                do {
                    _counter.compareAndSet(Long.MAX_VALUE, Long.MIN_VALUE)
                    i = _counter.incrementAndFetch()
                } while (_callbacks.containsKey(i))
                _callbacks[i] = obj
                return i
            }
            
            private val _callbackEquals = upcall(object {
                init { keep(::invoke) }
                fun invoke(self: Long, obj: Long) = getCallback<Any>(self) == getCallback<Any>(obj)
            })
        
            private val _callbackFree = upcall(object {
                init { keep(::invoke) }
                fun invoke(_self: Long) { _callbacks.remove(_self) }
            })
        """.replaceIndent(indent1))

        context.castedCallbacks.forEach { callback ->
            val name = callback.kname
            val lower = callback.name.camelCase().lowercase()

            appendLine("\n$indent1// $name")

            if(callback in context.toNativeDeclarationCasts) {
                val args = buildList {
                    add("_self: Long")
                    callback.args.mapTo(this) { "${it.kname}: ${it.type.toForeignKotlinType()}" }
                }.joinToString()
                val castedArgs = callback.args.joinToString {
                    castToKotlin(it.type, it.kname, true)
                }
                val castedResult = castToNative(callback.type, "getCallback<$name>(_self)($castedArgs)")

                appendLine("""
                    
                    private val _invoke$name = upcall(object {
                        init { keep(::invoke) }
                        fun invoke($args) = $castedResult
                    })
                    
                    private fun toNative$name(of: $name?): MemorySegment {
                        of ?: return MemorySegment.NULL
                        return _${lower}New(_saveCallback(of), of.hashCode(), _invoke${name}, _callbackEquals, _callbackFree) as MemorySegment
                    }
                """.replaceIndent(indent1))
            }
            if(callback in context.toKotlinDeclarationCasts) appendLine("""
                
                private fun toKotlin$name(of: MemorySegment, free: Boolean): $name? {
                    if(of.address() == 0L) return null
                    return getCallback<$name>(_${lower}Id(of) as Long)
                        .also { if(free) _${lower}Free(of) }
                }
            """.replaceIndent(indent1))
        }
    }

    private fun StringBuilder.printFunctions() {
        if(context.allOperations.isEmpty())
            return
        printLabel("Operations", indent = indent1)

        context.allOperations.forEach { operation ->
            val critical = operation.isCritical()

            val casts = operation.args.mapNotNull {
                val name = it.kname
                when {
                    critical && it.type.isString() ->
                        if(it.type.isNullable) listOf(
                            "\n${indent2}val _${name}_bytes = $name?.encodeToByteArray()",
                            "\n${indent2}val _${name}_data = _${name}_bytes?.run { MemorySegment.ofArray(this) } ?: MemorySegment.NULL",
                        ) else listOf(
                            "\n${indent2}val _${name}_bytes = $name.encodeToByteArray()",
                            "\n${indent2}val _${name}_data = MemorySegment.ofArray(_${name}_bytes)",
                        )
                    critical && it.type.isEnumArray() ->
                        if(it.type.isNullable) listOf(
                            "\n${indent2}val _${name}_ints = $name?.run { IntArray(size) { this[it].ordinal } }",
                            "\n${indent2}val _${name}_elements = $name?.run { MemorySegment.ofArray(_${name}_ints) } ?: MemorySegment.NULL",
                        ) else listOf(
                            "\n${indent2}val _${name}_ints = IntArray($name.size) { $name[it].ordinal }",
                            "\n${indent2}val _${name}_elements = MemorySegment.ofArray(_${name}_ints)",
                        )
                    critical && it.type.isBooleanArray() ->
                        if(it.type.isNullable) listOf(
                            "\n${indent2}val _${name}_bytes = $name?.run { ByteArray(size) { if(this[it]) 1 else 0 } }",
                            "\n${indent2}val _${name}_elements = $name?.run { MemorySegment.ofArray(_${name}_bytes) } ?: MemorySegment.NULL",
                        ) else listOf(
                            "\n${indent2}val _${name}_bytes = ByteArray($name.size) { if($name[it]) 1 else 0 }",
                            "\n${indent2}val _${name}_elements = MemorySegment.ofArray(_${name}_bytes)",
                        )
                    critical && it.type.isArray() -> (it.type as ResolvedIdlType.Default).arrayType { arrayType ->
                        val signed = if(arrayType.isUnsigned())
                            ".as${arrayType.toSignedType().toKotlinType(printNullable = false)}Array()" else ""
                        if (it.type.isNullable) listOf(
                            "\n${indent2}val _${name}_elements = $name?.run { MemorySegment.ofArray(this$signed) } ?: MemorySegment.NULL",
                        ) else listOf(
                            "\n${indent2}val _${name}_elements = MemorySegment.ofArray($name$signed)",
                        )
                    }
                    else -> null
                }
            }.flatten()

            val args = operation.args.joinToString {
                "${it.kname}: ${it.type.toKotlinType()}"
            }

            val castedArgs = operation.args.joinToString {
                when {
                    critical && it.type.isString() ->
                        if(it.type.isNullable) "_${it.kname}_data, ${it.kname}?.length ?: -1, _${it.kname}_bytes?.size ?: -1"
                        else "_${it.kname}_data, ${it.kname}.length, _${it.kname}_bytes.size"
                    critical && it.type.isArray() ->
                        if(it.type.isNullable) "_${it.kname}_elements, ${it.kname}?.size ?: -1"
                        else "_${it.kname}_elements, ${it.kname}.size"
                    else -> castToNative(it.type, it.kname)
                }
            }

            val expression = "${operation.cname.camelCase()}.invoke($castedArgs)"
            val casted = castToKotlin(operation.type, "$expression as ${operation.type.toForeignKotlinType()}", true, brackets = true)

            append("\n${indent1}override fun ${operation.kname}($args) ")
            if(casts.isNotEmpty()) {
                val type = if(!operation.type.isVoid())
                    ": ${operation.type.toKotlinType()}" else ""

                append("$type {")
                casts.joinTo(this, separator = "")
                append("\n$indent2")
                if(!operation.type.isVoid())
                    append("return ")
                append(casted)
                append("\n$indent1}")
            } else append("= ").append(casted)
        }
        append("\n")
    }


    private fun castToKotlin(
        type: ResolvedIdlType,
        content: String,
        free: Boolean,
        brackets: Boolean = false
    ): String {
        val nullable1 = if(type.isNullable) "" else "!!"
        val freeArg = if(free) ", free = true" else ", free = false"
        val expr = if(brackets) "($content)" else content
        return when {
            type.isUByte() -> "$expr.toUByte()"
            type.isUShort() -> "$expr.toUShort()"
            type.isUInt() -> "$expr.toUInt()"
            type.isULong() -> "$expr.toULong()"
            type.isArray() && type.isUnsigned() -> castToUnsigned(type, castToKotlin(type.toSignedType(), content, free))
            type.isEnum() -> "${type.declaration.kname}.entries[$content]"
            type.isString() -> "toKotlinString($content$freeArg)$nullable1"
            type.isCallback() -> "toKotlin${type.declaration.kname}($content$freeArg)$nullable1"
            type.isRawInterface() -> "$expr.address()"
            type.isDictionary() || type.isInterface() -> "toKotlin${type.declaration.kname}($content$freeArg)$nullable1"
            type.isArray() -> type.arrayType { arrType ->
                val nullableElements = if(arrType.isNullable) ", nullableElements = true" else ""
                when {
                    arrType.isPrimitive() -> castToUnsigned(type, "toKotlin${arrType.toKotlinType(ignoreUnsigned = true)}Array($content$freeArg)$nullable1")
                    arrType.isEnum() -> "toKotlinEnumArray($content, ${arrType.declaration.name}::class.java$freeArg)$nullable1"
                    arrType.isString() -> "toKotlinStringArray($content$nullableElements$freeArg)$nullable1"
                    else -> "toKotlin${arrType.declaration.kname}Array($content$nullableElements$freeArg)$nullable1"
                }
            }
            else -> content
        }
    }

    private fun castToNative(
        type: ResolvedIdlType,
        content: String
    ): String = when {
        type.isUByte() -> "$content.toInt() and 0x000000ff"
        type.isUShort() -> "$content.toInt() and 0x0000ffff"
        type.isUInt() -> "$content.toInt()"
        type.isULong() -> "$content.toLong()"
        type.isPrimitive() && type.isUnsigned() -> castToNative(type.toSignedType(), castToSigned(type, content))
        type.isEnum() -> "$content.ordinal"
        type.isString() -> "toNativeString($content)"
        type.isCallback() -> "toNative${type.declaration.kname}($content)"
        type.isRawInterface() -> "MemorySegment.ofAddress($content)"
        type.isDictionary() || type.isInterface() -> "toNative${type.declaration.kname}($content)"
        type.isArray() -> type.arrayType { arrType ->
            val nullableElements = if(arrType.isNullable) ", true" else ""
            when {
                arrType.isPrimitive() -> "toNative${arrType.toKotlinType(ignoreUnsigned = true)}Array(${castToSigned(type, content)})"
                arrType.isEnum() -> "toNativeEnumArray($content)"
                arrType.isString() -> "toNativeStringArray($content$nullableElements)"
                else -> "toNative${arrType.declaration.kname}Array($content$nullableElements)"
            }
        }
        else -> content
    }

    private fun ResolvedIdlType.toForeignKotlinType(): String = when {
        isUByte() || isUShort() -> "Int"
        isVoid() -> "Unit"
        isPrimitive() && isUnsigned() -> toSignedType().toForeignKotlinType()
        isPrimitive() -> toKotlinType()
        isEnum() -> "Int"
        else -> "MemorySegment"
    }

    private fun ResolvedIdlType.toForeignType(): String = when {
        isVoid() -> TYPE_VOID
        isChar() -> TYPE_CHAR
        isBoolean() -> TYPE_BOOLEAN
        isByte() -> TYPE_BYTE
        isShort() -> TYPE_SHORT
        isInt() || isUInt() || isUByte() || isUShort() -> TYPE_INT
        isLong() || isULong() -> TYPE_LONG
        isFloat() -> TYPE_FLOAT
        isDouble() -> TYPE_DOUBLE
        isEnum() -> TYPE_INT
        else -> TYPE_ADDRESS
    }
}