package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*
import io.github.vinceglb.filekit.*


class KotlinNativePrinter(
    private val context: NativeModuleContext,
    target: PlatformFile,
    private val expectActual: Boolean
) {
    init {
        target.parent()!!.createDirectories()
        target.writeSync(buildString {
            printHeader()
            printBasicCasts()
            printDictionariesCasts()
            printCallbacks()
            printFunctions()
            printInterfaces()
        })
    }

    private fun StringBuilder.printHeader() {
        val actual = if(expectActual) "actual " else ""

        append("""
            @file:OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
            @file:Suppress("SpellCheckingInspection", "LocalVariableName", "FunctionName", "PropertyName")
            
            package ${context.classPath}
            
            import cinterop.${context.classPath}.*
            import kotlinx.cinterop.*
            import kotlin.enums.enumEntries
            import kotlin.experimental.ExperimentalNativeApi
            import kotlin.native.ref.createCleaner
            import com.huskerdev.nativekt.*
            import platform.posix.*
            
            ${actual}val ${loadFieldName(context)}: Boolean = true
            
            @Throws(UnsupportedOperationException::class)
            ${actual}fun ${syncLoadFunctionName(context)}() =
                ${context.mangle("init")}()
            
            ${actual}fun ${asyncLoadFunctionName(context)}(onReady: () -> Unit) {
                ${syncLoadFunctionName(context)}()
                onReady()
            }
            
        """.trimIndent())
        if(context.configuration.useCoroutines)
            append("\n${actual}suspend fun ${asyncLoadFunctionName(context)}() = ${syncLoadFunctionName(context)}()\n")
    }

    private fun StringBuilder.printBasicCasts() {
        if(context.hasStringCast) {
            printLabel("String")
            if(context.hasStringCastToNative) appendLine("""
                
                private fun toNativeString(str: String?): COpaquePointer? = str?.encodeToByteArray()?.usePinned {
                    ${context.mangle("string_new")}(it.addressOf(0), it.get().size.convert(), true)
                }
            """.trimIndent())
            if(context.hasStringCastToKotlin) appendLine("""
                
                private fun toKotlinString(str: COpaquePointer?, free: Boolean): String? {
                    if(str == null) return null
                    val data = ${context.mangle("string_data")}(str)!!
                    val size = ${context.mangle("string_size")}(str).toInt()
                    return data.readBytes(size).decodeToString()
                        .also { if(free) ${context.mangle("string_free")}(str) }
                }
            """.trimIndent())
        }

        if(context.hasPrimitiveArrayCast || context.hasEnumArrayCast) {
            printLabel("Primitive arrays")

            if (context.hasCharArrayCast) {
                appendLine("\n// Char")
                if (context.hasCharArrayCastToNative) appendLine("""
                    
                    private fun toNativeCharArray(arr: CharArray?): COpaquePointer? = arr?.usePinned {
                        ${context.mangle("chararray_new")}(it.addressOf(0).reinterpret(), arr.size, true)
                    }
                """.trimIndent())
                if (context.hasCharArrayCastToKotlin) appendLine("""
                    
                    private fun toKotlinCharArray(arr: COpaquePointer?, free: Boolean): CharArray? {
                        if(arr == null) return null
                        val elements = ${context.mangle("chararray_elements")}(arr)
                        val length = ${context.mangle("chararray_length")}(arr)
                        return CharArray(length) { elements!![it].toInt().toChar() }
                            .also { if(free) ${context.mangle("chararray_free")}(arr) }
                    }
                """.trimIndent())
            }
            if (context.hasBooleanArrayCast) {
                appendLine("\n// Boolean")
                if (context.hasBooleanArrayCastToNative) appendLine("""
                    
                    private fun toNativeBooleanArray(arr: BooleanArray?): COpaquePointer? {
                        if(arr == null) return null
                        return ByteArray(arr.size) { arr[it].toByte() }.usePinned {
                            ${context.mangle("booleanarray_new")}(it.addressOf(0).reinterpret(), arr.size, true)
                        }
                    }
                """.trimIndent())
                if (context.hasBooleanArrayCastToKotlin) appendLine("""
                    
                    private fun toKotlinBooleanArray(arr: COpaquePointer?, free: Boolean): BooleanArray? {
                        if(arr == null) return null
                        val elements = ${context.mangle("booleanarray_elements")}(arr)
                        val length = ${context.mangle("booleanarray_length")}(arr)
                        return BooleanArray(length) { elements!![it].value }
                            .also { if(free) ${context.mangle("booleanarray_free")}(arr) }
                    }
                """.trimIndent())
            }

            if (context.hasByteArrayCast || context.hasUByteArrayCast) {
                appendLine("\n// Byte")
                if (context.hasByteArrayCastToNative || context.hasUByteArrayCastToNative) appendLine("""
                    
                    private fun toNativeByteArray(arr: ByteArray?): COpaquePointer? = arr?.usePinned {
                        ${context.mangle("bytearray_new")}(it.addressOf(0).reinterpret(), arr.size, true)
                    }
                """.trimIndent())
                if (context.hasByteArrayCastToKotlin || context.hasUByteArrayCastToKotlin) appendLine("""
                    
                    private fun toKotlinByteArray(arr: COpaquePointer?, free: Boolean): ByteArray? {
                        if(arr == null) return null
                        val elements = ${context.mangle("bytearray_elements")}(arr)
                        val length = ${context.mangle("bytearray_length")}(arr)
                        return ByteArray(length) { elements!![it] }
                            .also { if(free) ${context.mangle("bytearray_free")}(arr) }
                    }
                """.trimIndent())
            }

            if (context.hasShortArrayCast || context.hasUShortArrayCast) {
                appendLine("\n// Short")
                if (context.hasShortArrayCastToNative || context.hasUShortArrayCastToNative) appendLine("""
                    
                    private fun toNativeShortArray(arr: ShortArray?): COpaquePointer? = arr?.usePinned {
                        ${context.mangle("shortarray_new")}(it.addressOf(0).reinterpret(), arr.size, true)
                    }
                """.trimIndent())
                if (context.hasShortArrayCastToKotlin || context.hasUShortArrayCastToKotlin) appendLine("""
                    
                    private fun toKotlinShortArray(arr: COpaquePointer?, free: Boolean): ShortArray? {
                        if(arr == null) return null
                        val elements = ${context.mangle("shortarray_elements")}(arr)
                        val length = ${context.mangle("shortarray_length")}(arr)
                        return ShortArray(length) { elements!![it] }
                            .also { if(free) ${context.mangle("shortarray_free")}(arr) }
                    }
                """.trimIndent())
            }

            if (context.hasIntArrayCast || context.hasUIntArrayCast || context.hasEnumArrayCast) {
                appendLine("\n// Int")
                if (context.hasIntArrayCastToNative || context.hasUIntArrayCastToNative || context.hasEnumArrayCastToNative) appendLine("""
                    
                    private fun toNativeIntArray(arr: IntArray?): COpaquePointer? = arr?.usePinned {
                        ${context.mangle("intarray_new")}(it.addressOf(0).reinterpret(), arr.size, true)
                    }
                """.trimIndent())
                if (context.hasIntArrayCastToKotlin || context.hasUIntArrayCastToKotlin || context.hasEnumArrayCastToKotlin) appendLine("""
                    
                    private fun toKotlinIntArray(arr: COpaquePointer?, free: Boolean): IntArray? {
                        if(arr == null) return null
                        val elements = ${context.mangle("intarray_elements")}(arr)
                        val length = ${context.mangle("intarray_length")}(arr)
                        return IntArray(length) { elements!![it] }
                            .also { if(free) ${context.mangle("intarray_free")}(arr) }
                    }
                """.trimIndent())
            }

            if (context.hasLongArrayCast || context.hasULongArrayCast) {
                appendLine("\n// Long")
                if (context.hasLongArrayCastToNative || context.hasULongArrayCastToNative) appendLine("""
                    
                    private fun toNativeLongArray(arr: LongArray?): COpaquePointer? = arr?.usePinned {
                        ${context.mangle("longarray_new")}(it.addressOf(0).reinterpret(), arr.size, true)
                    }
                """.trimIndent())
                if (context.hasLongArrayCastToKotlin || context.hasULongArrayCastToKotlin) appendLine("""
                    
                    private fun toKotlinLongArray(arr: COpaquePointer?, free: Boolean): LongArray? {
                        if(arr == null) return null
                        val elements = ${context.mangle("longarray_elements")}(arr)
                        val length = ${context.mangle("longarray_length")}(arr)
                        return LongArray(length) { elements!![it] }
                            .also { if(free) ${context.mangle("longarray_free")}(arr) }
                    }
                """.trimIndent())
            }

            if (context.hasFloatArrayCast) {
                appendLine("\n// Float")
                if (context.hasFloatArrayCastToNative) appendLine("""
                    
                    private fun toNativeFloatArray(arr: FloatArray?): COpaquePointer? = arr?.usePinned {
                        ${context.mangle("floatarray_new")}(it.addressOf(0).reinterpret(), arr.size, true)
                    }
                """.trimIndent())
                if (context.hasFloatArrayCastToKotlin) appendLine("""
                    
                    private fun toKotlinFloatArray(arr: COpaquePointer?, free: Boolean): FloatArray? {
                        if(arr == null) return null
                        val elements = ${context.mangle("floatarray_elements")}(arr)
                        val length = ${context.mangle("floatarray_length")}(arr)
                        return FloatArray(length) { elements!![it] }
                            .also { if(free) ${context.mangle("floatarray_free")}(arr) }
                    }
                """.trimIndent())
            }

            if (context.hasDoubleArrayCast) {
                appendLine("\n// Double")
                if (context.hasDoubleArrayCastToNative) appendLine("""
                    
                    private fun toNativeDoubleArray(arr: DoubleArray?): COpaquePointer? = arr?.usePinned {
                        ${context.mangle("doublearray_new")}(it.addressOf(0).reinterpret(), arr.size, true)
                    }
                """.trimIndent())
                if (context.hasDoubleArrayCastToKotlin) appendLine("""
                    
                    private fun toKotlinDoubleArray(arr: COpaquePointer?, free: Boolean): DoubleArray? {
                        if(arr == null) return null
                        val elements = ${context.mangle("doublearray_elements")}(arr)
                        val length = ${context.mangle("doublearray_length")}(arr)
                        return DoubleArray(length) { elements!![it] }
                            .also { if(free) ${context.mangle("doublearray_free")}(arr) }
                    }
                """.trimIndent())
            }

            if (context.hasEnumArrayCast) {
                printLabel("Enum array")
                if (context.hasEnumArrayCastToNative) appendLine("""
                    
                    fun <T: Enum<T>> toNativeEnumArray(arr: Array<T>?): COpaquePointer? =
                        arr?.run { toNativeIntArray(IntArray(arr.size) { arr[it].ordinal }) }
                """.trimIndent())
                if (context.hasEnumArrayCastToKotlin) appendLine("""
                    
                    private inline fun <reified T: Enum<T>> toKotlinEnumArray(arr: COpaquePointer?, free: Boolean): Array<T>? {
                        if(arr == null) return null
                        val entries = enumEntries<T>()
                        val ints = toKotlinIntArray(arr, free)!!
                        return Array(ints.size) { entries[ints[it]] }
                    }
                """.trimIndent())
            }
        }

        if(context.hasObjectArraysCast) {
            printLabel("Object arrays")

            buildList {
                context.castedDictionaries.mapTo(this) { dictionary ->
                    Triple(dictionary.kname to dictionary.cname.lowercase(),
                        dictionary in context.usedObjectArrayCastToNative,
                        dictionary in context.usedObjectArrayCastToKotlin)
                }
                context.castedInterfaces.mapTo(this) { inter ->
                    Triple(inter.kname to inter.cname.lowercase(),
                        inter in context.usedObjectArrayCastToNative,
                        inter in context.usedObjectArrayCastToKotlin)
                }
                if (context.hasStringArrayCast) {
                    add(Triple("String" to "string",
                        context.hasStringArrayCastToNative,
                        context.hasStringArrayCastToKotlin))
                }
            }.forEach { (names, hasToNativeCast, hasToKotlinCast) ->
                val name = names.first
                val lower = names.second

                appendLine("\n// $name")
                if (hasToNativeCast) appendLine("""
                    
                    private fun toNative${name}Array(
                        arr: Array<$name?>?,
                        nullableElements: Boolean
                    ): COpaquePointer? {
                        if(arr == null) return null
                        val array = ${context.mangle("array_${lower}_new")}(arr.size, nullableElements)
                        arr.forEach {
                            ${context.mangle("array_${lower}_push")}(array, toNative$name(it), nullableElements)
                        }
                        return array
                    }
                    
                    @Suppress("unchecked_cast") private fun toNative${name}Array(arr: Array<$name>?) = 
                        toNative${name}Array(arr as Array<$name?>?, false)
                """.trimIndent())
                if (hasToKotlinCast) appendLine("""
                    
                    private fun toKotlin${name}Array(
                        arr: COpaquePointer?,
                        nullableElements: Boolean,
                        free: Boolean
                    ): Array<$name?>? {
                        if(arr == null) return null
                        return Array(${context.mangle("array_${lower}_length")}(arr, nullableElements)) {
                            toKotlin$name(${context.mangle("array_${lower}_get")}(arr, it, nullableElements), false)
                        }.also { if(free) ${context.mangle("array_${lower}_free")}(arr, nullableElements) }
                    }
                    
                    @Suppress("unchecked_cast") private fun toKotlin${name}Array(arr: COpaquePointer?, free: Boolean) = 
                        toKotlin${name}Array(arr, false, free) as Array<$name>?
                """.trimIndent())
            }
        }
    }

    private fun StringBuilder.printDictionariesCasts() {
        if(!context.hasDictionaryCast)
            return
        printLabel("Dictionary")

        context.castedDictionaries.forEach { dictionary ->
            val name = dictionary.kname
            val funcNew = dictionary.subCFunc(context, "new")
            val fields = context.allFields[dictionary]!!

            appendLine("\n// $name")

            // to native
            if(dictionary in context.toNativeDeclarationCasts) {
                append($$"""
                    
                    private fun toNative$$name(of: $$name?): COpaquePointer? {
                        if(of == null) return null
                        return $$funcNew(
                """.trimIndent())

                fields.joinTo(this) {
                    val value = castToNative(
                        type = it.type,
                        content = "of.${it.kname}"
                    )
                    "\n\t\t${it.cname} = $value"
                }
                append("\n\t)\n}\n")
            }

            // to kotlin
            if(dictionary in context.toKotlinDeclarationCasts) {
                append($$"""
                    
                    private fun toKotlin$$name(of: COpaquePointer?, free: Boolean): $$name? {
                        if(of == null) return null
                        return $$name(
                """.trimIndent())

                fields.joinTo(this) {
                    val func = "${dictionary.subFieldCFunc(context, it)}(of)"
                    "\n\t\t${it.kname} = ${castToKotlin(it.type, func, false)}"
                }
                append("\n\t).also { if(free) ${dictionary.subCFunc(context, "free")}(of) }")
                append("\n}\n")
            }
        }
    }

    private fun StringBuilder.printCallbacks() {
        if(!context.hasCallbackCast)
            return
        printLabel("Callbacks")

        append("""
            
            @Suppress("unchecked_cast")
            private fun <T> getCallback(id: size_t): T =
                (id.toLong().toCPointer<CPointed>()!!.asStableRef<Any>().get() as T?)!!

            private val callbackEquals = staticCFunction { self: size_t, obj: size_t ->
            	getCallback<Any>(self) == getCallback<Any>(obj)
            }

            private val callbackFree = staticCFunction { id: size_t ->
            	id.toLong().toCPointer<CPointed>()!!.asStableRef<Any>().dispose()
            }
            
        """.trimIndent())

        context.castedCallbacks.forEach { callback ->
            val name = callback.kname
            val lower = callback.name.camelCase().lowercase()
            val invokeFunc = "invoke$name"

            val invokeArgs = buildList {
                add("_id: size_t")
                callback.args.mapTo(this) {
                    "${it.kname}: ${it.type.toKnType()}"
                }
            }.joinToString()

            val castedArgs = callback.args.joinToString {
                castToKotlin(it.type, it.kname, true)
            }

            appendLine("\n// $name")

            if(callback in context.toNativeDeclarationCasts) appendLine("""
                
                private val $invokeFunc = staticCFunction { $invokeArgs ->
                    ${castToNative(callback.type, "getCallback<$name>(_id)($castedArgs)")}
                }
                
                private fun toNative$name(self: $name?): COpaquePointer? {
                	if(self == null) return null
                    val id = StableRef.create(self).asCPointer().toLong().convert<size_t>()
                	return ${context.mangle("${lower}_new")}(
                		id, self.hashCode(), $invokeFunc, callbackEquals, callbackFree,
                	)
                }
            """.trimIndent())
            if(callback in context.toKotlinDeclarationCasts) appendLine("""
                
                private fun toKotlin$name(self: COpaquePointer?, free: Boolean): $name? {
                	if(self == null) return null
                    return getCallback<$name>(${context.mangle("${lower}_id")}(self))
                        .also { if(free) ${context.mangle("${lower}_free")}(self) }
                }
            """.trimIndent())
        }
    }

    private fun StringBuilder.printFunctions(){
        if(context.allOperations.isEmpty())
            return

        printLabel("Functions")
        context.allOperations.forEach { function ->
            val critical = function.isCritical()
            val isInterface = function.isInterfaceOperation()

            val args = function.args.joinToString {
                "${it.kname}: ${it.type.toKotlinType()}"
            }

            val type = when {
                function.type.isVoid() -> ""
                else -> ": ${function.type.toKotlinType()}"
            }

            val castedArgs = function.args.joinToString {
                val name = it.kname
                when {
                    critical && it.type.isString() ->
                        if(it.type.isNullable) "_${name}_pinned?.addressOf(0), _${name}_bytes?.size ?: -1"
                        else "_${name}_pinned.addressOf(0), _${name}_bytes.size"
                    critical && (it.type.isCharArray() || it.type.isBooleanArray()) ->
                        if(it.type.isNullable) "_${name}_pinned?.addressOf(0)?.reinterpret(), $name?.size ?: -1"
                        else "_${name}_pinned.addressOf(0).reinterpret(), $name.size"
                    critical && it.type.isArray() ->
                        if(it.type.isNullable) "_${name}_pinned?.addressOf(0), $name?.size ?: -1"
                        else "_${name}_pinned.addressOf(0), $name.size"
                    else -> castToNative(it.type, name)
                }
            }

            val call = "${function.cnameMangled(context)}($castedArgs)"

            val casts = arrayListOf<String>()
            val releases = arrayListOf<String>()

            if(critical) {
                function.args.forEach {
                    val name = it.kname
                    val pinned = "_${name}_pinned"

                    if(it.type.isString() || it.type.isArray())
                        releases += "\n\t_${it.kname}_pinned${if(it.type.isNullable) "?" else ""}.unpin()"

                    when {
                        it.type.isString() ->
                            casts += if(it.type.isNullable) """
                                
                                val _${name}_bytes = $name?.encodeToByteArray()
                                val $pinned = _${name}_bytes?.pin()
                            """.replaceIndent("\t")
                            else """
                                
                                val _${name}_bytes = $name.encodeToByteArray()
                                val $pinned = _${name}_bytes.pin()
                            """.replaceIndent("\t")
                        it.type.isBooleanArray() ->
                            casts += if(it.type.isNullable) "\n\tval $pinned = $name?.run { ByteArray($name.size) { if($name[it]) 1 else 0 }.pin() }"
                            else "\n\tval $pinned = ByteArray($name.size) { if($name[it]) 1 else 0 }.pin()"
                        it.type.isEnumArray() ->
                            casts += if(it.type.isNullable) "\n\tval $pinned = $name?.run { IntArray($name.size) { $name[it].ordinal }.pin() }"
                            else "\n\tval $pinned = IntArray($name.size) { $name[it].ordinal }.pin()"
                        it.type.isArray() ->
                            casts += if(it.type.isNullable) "\n\tval $pinned = $name?.pin()"
                            else "\n\tval $pinned = $name.pin()"
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
        if(context.interfaces.isEmpty())
            return
        printLabel("Interfaces")

        context.interfaces.forEach { inter ->
            val name = inter.kname
            val lower = inter.kname.lowercase()

            if(inter in context.toKotlinDeclarationCasts) appendLine("""
                
                private fun toKotlin$name(ptr: COpaquePointer?, free: Boolean): $name? {
                    if(ptr == null) return null
                	return $name(Unit, _interface${name}Clone(ptr.toLong()))
                        .also { if(free) ${context.mangle("interface_${lower}_free")}(ptr) }
                }
            """.trimIndent())
            if(inter in context.toNativeDeclarationCasts) appendLine("""
                
                private fun toNative$name(obj: $name?): COpaquePointer? {
                    if(obj == null) return null
                	return _interface${name}Clone(obj.rcPtr).toCPointer()
                }
            """.trimIndent())
            append("""

                actual class $name(m: Unit, rcPtr: Long): NativeKtRcObject(rcPtr, ::_interface${name}Free) {
                    @Suppress("unused") private val cleaner = createCleaner(releaser) { it.release() }
                    override fun _address(): Long = _interface${name}Address(rcPtr)
                    
            """.trimIndent())

            inter.toOperations().forEach { operation ->
                val args = operation.args.map {
                    "${it.kname}: ${it.type.toKotlinType()}"
                }
                val argNames = operation.args.map { it.kname }

                append(when {
                    operation.isInterfaceOperationConstructor() ->
                        "\n\tactual constructor(${args.joinToString()}): this(Unit, ${operation.kname}(${argNames.joinToString()}))"
                    operation.isInterfaceOperationFn() -> {
                        val args = args.drop(1).joinToString()
                        val argNames = argNames.toMutableList()
                            .apply { set(0, "rcPtr") }
                            .joinToString()
                        val name = operation.interfaceFunctionName().camelCase()
                        "\n\tactual fun ${name}($args) = ${operation.kname}($argNames)"
                    }
                    else -> return@forEach
                })
            }
            append("\n}")
        }
    }

    private fun castToKotlin(
        type: ResolvedIdlType,
        content: String,
        free: Boolean
    ): String {
        val nullable1 = if(type.isNullable) "" else "!!"
        val freeArg = if(free) ", free = true" else ", free = false"
        return when {
            type.isChar() -> "$content.toInt().toChar()"
            type.isEnum() -> "${type.declaration.kname}.entries[$content]"
            type.isString() -> "toKotlinString($content$freeArg)$nullable1"
            type.isCallback() -> "toKotlin${type.declaration.kname}($content$freeArg)$nullable1"
            type.isRawInterface() -> "$content!!.toLong()"
            type.isDictionary() || type.isInterface() -> "toKotlin${type.declaration.kname}($content$freeArg)$nullable1"
            type.isArray() -> type.arrayType { arrType ->
                val nullableElements = if(arrType.isNullable) ", nullableElements = true" else ""
                when {
                    arrType.isPrimitive() -> castToUnsigned(type, "toKotlin${arrType.toKotlinType(ignoreUnsigned = true)}Array($content$freeArg)$nullable1")
                    arrType.isEnum() -> "toKotlinEnumArray<${arrType.declaration.name}>($content$freeArg)$nullable1"
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
        type.isArray() && type.isUnsigned() -> castToNative(type.toSignedType(), castToSigned(type, content))
        type.isChar() -> "$content.code.toUShort()"
        type.isEnum() -> "$content.ordinal"
        type.isString() -> "toNativeString($content)"
        type.isCallback() -> "toNative${type.declaration.kname}($content)"
        type.isRawInterface() -> "$content.toCPointer<CPointed>()"
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

    private fun ResolvedIdlType.toKnType(): String = when {
        isVoid() -> "Unit"
        isChar() -> "UShort"
        isEnum() -> "Int"
        isReleasable() -> "COpaquePointer?"
        else -> toKotlinType()
    }
}