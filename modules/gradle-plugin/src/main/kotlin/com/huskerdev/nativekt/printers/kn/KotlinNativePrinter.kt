package com.huskerdev.nativekt.printers.kn

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File

class KotlinNativePrinter(
    idl: IdlResolver,
    target: File,
    classPath: String,
    val moduleName: String,
    val is32Bit: Boolean,
    useCoroutines: Boolean,
    val expectActual: Boolean
) {
    val cinteropPath = "cinterop.$classPath"

    init {
        val actual = if(expectActual) "actual " else ""

        val builder = StringBuilder()
        builder.append("""
            @file:OptIn(ExperimentalForeignApi::class)
            @file:Suppress("unused")
            
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

        printLabel(builder, "String")
        builder.append("""
            
            private fun toNativeString(of: String, arena: NativeArena) = cValue<$cinteropPath.KString> {
                data = of.cstr.getPointer(arena.scope)
                length = of.length
                arena.ptr(data!!)
            }
            
            private fun toNativeString(of: String) = 
            	cValue<$cinteropPath.KString> { toNativeString(of, this) }

            private fun toNativeString(of: String, struct: $cinteropPath.KString) = struct.apply {
            	data = strdup(of)
            	length = of.length
            }
            
            private fun toKotlinString(of: $cinteropPath.KString, dealloc: Boolean): String =
                toKotlinString(of.data!!, dealloc)
            
            private fun toKotlinString(of: CValue<$cinteropPath.KString>, dealloc: Boolean): String =
                of.useContents { toKotlinString(data!!, dealloc) }
            
            private fun toKotlinString(of: CValue<$cinteropPath.KString>, arena: NativeArena, dealloc: Boolean): String =
                arena.toKotlinString(of.useContents { data }!!, dealloc)
            
        """.trimIndent())

        if(idl.dictionaries.isNotEmpty()) {
            printLabel(builder, "Dictionary")
            idl.dictionaries.values.forEach { printDictionaryCasts(builder, it) }
        }

        printLabel(builder, "Arrays")
        printArrayCasts(builder)
        idl.enums.values.forEach { printEnumCast(builder, it) }

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

        // to jvm
        append("\nprivate fun toKotlinDictionary")
        append(dictionary.name.capitalized())
        append("(of: CPointer<")
        append(cinteropPath)
        append(".")
        append(dictionary.name)
        append(">?, dealloc: Boolean) = of!!.pointed.run {\n\t")
        append(dictionary.name)
        append("(\n\t\t")

        dictionary.allFields().joinTo(builder, separator = ",\n\t\t") {
            castFromNative(it.type, it.name, dealloc = false, deallocContent = false, useArena = false)
        }
        append("\n\t)\n")
        append("}.also { if(dealloc) free(of) }\n")

        // to native
        append("\nprivate fun toNativeDictionary")
        append(dictionary.name)
        append("(of: ")
        append(dictionary.name)
        append(") = allocStruct<")
        append(cinteropPath)
        append(".")
        append(dictionary.name)
        append(">().apply {\n\t")
        append("val struct = pointed\n\t")

        dictionary.allFields().joinTo(builder, separator = "\n\t") {
            if(it.type.isArray() || it.type.isString())
                castToNative(it.type, "of.${it.name}", dealloc = false, useArena = false, struct = "struct.${it.name}")
            else
                "struct.${it.name} = ${castToNative(it.type, "of.${it.name}", dealloc = false, useArena = false)}"
        }
        append("\n}\n")
    }

    private fun printCallbackWrap(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        // header
        append("\nprivate fun ")
        append("toNativeCallback")
        append(callback.name)
        append("(of: ")
        append(callback.name)
        append(") =\n\t")

        // body
        append("allocStruct<")
        append(cinteropPath)
        append(".")
        append(callback.name)
        append(">().apply {\n\t\t")
        append("val struct = pointed\n\t\t")

        // m =
        append("struct.m = StableRef.create(of).asCPointer()\n\t\t")

        // invoke =
        val args = listOf("_callback") + callback.args.map { it.name }

        val castedArgs = callback.args.joinToString { castFromNative(it.type, it.name, it.isDealloc(), false, false) }

        val call = "_callback!!.pointed.m!!.asStableRef<${callback.name}>().get()($castedArgs)"

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

        val useArena = function.args.any { it.type.isString() || it.type.isArray() || it.isDealloc() }

        if(useArena)
            append("NativeArena.use { arena ->")
        append("\n\t")

        val args = function.args.joinToString { arg ->
            castToNative(arg.type, arg.name, arg.isDealloc(), useArena)
        }

        val call = "$cinteropPath.${function.name}($args)"
        append(castFromNative(function.type, call, function.isDealloc(), function.isDeallocContent(), useArena))

        if(useArena)
            append("\n}")

        append("\n")
    }

    private fun printArrayCasts(builder: StringBuilder) = builder.apply {
        arrayOf(
            "Byte", "Short", "Int", "Long", "Float", "Double"
        ).forEach { type ->
            append("""
                
                // Array: $type
    
                private fun toNative${type}Array(array: ${type}Array, arena: NativeArena) = cValue<$cinteropPath.K${type}Array> {
                    elements = arena.pin(array).addressOf(0)
                    size = array.size
                    arena.ptr(elements!!)
                }
    
                private fun toNative${type}Array(array: ${type}Array) = 
                    cValue<$cinteropPath.K${type}Array> { toNative${type}Array(array, this) }
                
                private fun toNative${type}Array(array: ${type}Array, struct: $cinteropPath.K${type}Array) = struct.apply {
                    val bytes = array.size * ${type}.SIZE_BYTES
                    elements = mallocExact(bytes.toUInt()).reinterpret()
                    size = array.size
                    array.usePinned { memcpy(elements, it.addressOf(0), bytes.${if(is32Bit) "toUInt" else "toULong"}()) }
                }
    
                private fun toKotlin${type}Array(struct: CValue<$cinteropPath.K${type}Array>, arena: NativeArena, dealloc: Boolean) = struct.useContents {
                    ${type}Array(size) { elements!![it] }.also {
                        if(dealloc) arena.freeMem(elements!!)
                    }
                }
                
                private fun toKotlin${type}Array(struct: CValue<$cinteropPath.K${type}Array>, dealloc: Boolean) = 
                    struct.useContents { toKotlin${type}Array(this, dealloc) }
    
                private fun toKotlin${type}Array(struct: $cinteropPath.K${type}Array, dealloc: Boolean) =
                    ${type}Array(struct.size) { struct.elements!![it] }
                        .also { if(dealloc) free(struct.elements!!) }
                
            """.trimIndent())
        }

        // Char needs some changes:
        // 1. .reinterpret() in toNativeCharArray
        // 2. .toInt().toChar() in toKotlinCharArray
        append("""
            
            // Array: Char

            private fun toNativeCharArray(array: CharArray, arena: NativeArena) = cValue<$cinteropPath.KCharArray> {
                elements = arena.pin(array).addressOf(0).reinterpret()
                size = array.size
                arena.ptr(elements!!)
            }
            
            private fun toNativeCharArray(array: CharArray) = 
                cValue<$cinteropPath.KCharArray> { toNativeCharArray(array, this) }

            private fun toNativeCharArray(array: CharArray, struct: $cinteropPath.KCharArray) = struct.apply {
            	val bytes = array.size * Char.SIZE_BYTES
            	elements = mallocExact(bytes.toUInt()).reinterpret()
            	size = array.size
            	array.usePinned { memcpy(elements, it.addressOf(0), bytes.${if (is32Bit) "toUInt" else "toULong"}()) }
            }

            private fun toKotlinCharArray(struct: CValue<$cinteropPath.KCharArray>, arena: NativeArena, dealloc: Boolean) = struct.useContents {
                CharArray(size) { elements!![it].toInt().toChar() }.also {
                    if(dealloc) arena.freeMem(elements!!)
                }
            }
            
            private fun toKotlinCharArray(struct: CValue<$cinteropPath.KCharArray>, dealloc: Boolean) = 
            	struct.useContents { toKotlinCharArray(this, dealloc) }

            private fun toKotlinCharArray(struct: $cinteropPath.KCharArray, dealloc: Boolean) =
                CharArray(struct.size) { struct.elements!![it].toInt().toChar() }
                    .also { if(dealloc) free(struct.elements!!) }
            
        """.trimIndent())

        // Booleans needs to be cast to bytes
        append("""
            
            // Array: Boolean

            private fun toNativeBooleanArray(array: BooleanArray, arena: NativeArena) = cValue<$cinteropPath.KBooleanArray> {
            	val byteArray = array.map { it.toByte() }.toByteArray()
            	elements = arena.pin(byteArray).addressOf(0).reinterpret()
            	size = array.size
            	arena.ptr(elements!!)
            }

            private fun toNativeBooleanArray(array: BooleanArray) = 
                cValue<$cinteropPath.KBooleanArray> { toNativeBooleanArray(array, this) }
            
            private fun toNativeBooleanArray(array: BooleanArray, struct: $cinteropPath.KBooleanArray) = struct.apply {
            	val bytes = array.size * Byte.SIZE_BYTES
            	elements = mallocExact(bytes.toUInt()).reinterpret()
            	size = array.size
            	val byteArray = array.map { it.toByte() }.toByteArray()
            	byteArray.usePinned { memcpy(elements, it.addressOf(0), bytes.${if (is32Bit) "toUInt" else "toULong"}()) }
            }

            private fun toKotlinBooleanArray(struct: CValue<$cinteropPath.KBooleanArray>, arena: NativeArena, dealloc: Boolean) = struct.useContents {
            	BooleanArray(size) { elements!![it].value }.also {
            		if(dealloc) arena.freeMem(elements!!)
            	}
            }

            private fun toKotlinBooleanArray(struct: CValue<$cinteropPath.KBooleanArray>, dealloc: Boolean) = 
                struct.useContents { toKotlinBooleanArray(this, dealloc) }
            
            private fun toKotlinBooleanArray(struct: $cinteropPath.KBooleanArray, dealloc: Boolean) =
            	BooleanArray(struct.size) { struct.elements!![it].value }
                    .also { if(dealloc) free(struct.elements!!) }
            
        """.trimIndent())

        // Enum casts
        append("""
            // Array: Enum

            private fun <T: Enum<T>, N: CPrimitiveVar> toNativeEnumArray(
                array: Array<T>,
                arena: NativeArena,
                typeSize: Long,
                setter: (from: Array<T>, to: CPointer<N>) -> Unit
            ) = cValue<$cinteropPath.KIntArray> {
                val bytes = array.size * typeSize
                elements = arena.ptr(mallocExact(bytes.toUInt()).reinterpret())
                size = array.size
                setter(array, elements!!.reinterpret())
            }
            
            private fun <T: Enum<T>, N: CPrimitiveVar> toNativeEnumArray(
                array: Array<T>,
                typeSize: Long,
                setter: (from: Array<T>, to: CPointer<N>) -> Unit
            ) = cValue<$cinteropPath.KIntArray> {
                toNativeEnumArray(array, typeSize, setter, this)
            }
            
            private fun <T: Enum<T>, N: CPrimitiveVar> toNativeEnumArray(
                array: Array<T>,
                typeSize: Long,
                setter: (from: Array<T>, to: CPointer<N>) -> Unit,
                struct: $cinteropPath.KIntArray
            ) = struct.apply {
                val bytes = array.size * typeSize
                elements = mallocExact(bytes.toUInt()).reinterpret()
                size = array.size
                setter(array, elements!!.reinterpret())
            }
            
            private fun <T: Enum<T>, N: CPrimitiveVar> toKotlinEnumArray(
                struct: CValue<$cinteropPath.KIntArray>,
                arena: NativeArena, 
                dealloc: Boolean,
                converter: (size: Int, elements: CPointer<N>) -> Array<T>
            ) = struct.useContents {
                converter(size, this.elements!!.reinterpret()).also {
                    if(dealloc) arena.freeMem(this.elements!!)
                }
            }
            
            private fun <T: Enum<T>, N: CPrimitiveVar> toKotlinEnumArray(
                struct: CValue<$cinteropPath.KIntArray>,
                dealloc: Boolean,
                converter: (size: Int, elements: CPointer<N>) -> Array<T>
            ) = struct.useContents { toKotlinEnumArray(this, dealloc, converter) }
            
            private fun <T: Enum<T>, N: CPrimitiveVar> toKotlinEnumArray(
                struct: $cinteropPath.KIntArray,
                dealloc: Boolean,
                converter: (size: Int, elements: CPointer<N>) -> Array<T>
            ) = converter(struct.size, struct.elements!!.reinterpret())
                    .also { if(dealloc) free(struct.elements!!) }
            
            // Object array
            
            private fun <T: Any, N: CPointed> toNativeArray(
                array: Array<T>,
                converter: (from: T) -> CPointer<N>,
                arena: NativeArena? = null
            ) = cValue<$cinteropPath.KArray> {
                toNativeArray(array, converter, arena, this)
            }
            
             private fun <T: Any, N: CPointed> toNativeArray(
                array: Array<T>,
                converter: (from: T) -> CPointer<N>,
                arena: NativeArena? = null,
                struct: $cinteropPath.KArray
            ) = struct.apply {
                elements = mallocExact((array.size * size_t.SIZE_BYTES).toUInt()).reinterpret()
                size = array.size
                for(i in array.indices)
                    elements!![i] = converter(array[i])
                arena?.ptr(elements!!.reinterpret())
            }
            
            private fun <T: Any, N: CPointed> toKotlinArray(
                struct: CValue<$cinteropPath.KArray>,
                converter: (from: CPointer<N>, dealloc: Boolean) -> T,
                dealloc: Boolean,
                deallocContent: Boolean,
                arena: NativeArena? = null
            ) = struct.useContents {
                toKotlinArray(this, converter, dealloc, deallocContent, arena)
            }
            
            @Suppress("unchecked_cast")
            private fun <T: Any, N: CPointed> toKotlinArray(
                struct: $cinteropPath.KArray,
                converter: (from: CPointer<N>, dealloc: Boolean) -> T,
                dealloc: Boolean,
                deallocContent: Boolean,
                arena: NativeArena? = null
            ): Array<T> {
                return Array<Any>(struct.size) { 
                    converter(struct.elements!![it]!!.reinterpret(), deallocContent) 
                }.also {
                    if(dealloc) arena?.freeMem(struct.elements!!) ?: free(struct.elements!!)
                } as Array<T>
            }
            
        """.trimIndent())
    }

    private fun printEnumCast(builder: StringBuilder, enum: ResolvedIdlEnum) = builder.apply {
        append("""
            
            private val _${enum.name}ToKt = { size: Int, elements: CPointer<${cinteropPath}.${enum.name}.Var> ->
                Array(size) { ${enum.name}.entries[elements[it].value.ordinal] }
            }
            
            private val _${enum.name}ToNative = { from: Array<${enum.name}>, to: CPointer<${cinteropPath}.${enum.name}.Var> ->
                for(i in from.indices)
                    to[i].value = ${cinteropPath}.${enum.name}.entries[from[i].ordinal]
            }
            
        """.trimIndent())
    }

    private fun castFromNative(type: ResolvedIdlType, content: String, dealloc: Boolean, deallocContent: Boolean, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.CHAR -> "$content.toInt().toChar()"
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "toKotlinString($content, arena, $dealloc)"
                    else "toKotlinString($content, $dealloc)"
                WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                    when (declaration) {
                        is BuiltinIdlDeclaration -> {
                            val name = declaration.kind.simpleName()
                            if (useArena) "toKotlin${name}Array($content, arena, $dealloc)"
                            else "toKotlin${name}Array($content, $dealloc)"
                        }
                        is ResolvedIdlEnum -> {
                            if (useArena) "toKotlinEnumArray($content, arena, $dealloc, _${declaration.name}ToKt)"
                            else "toKotlinEnumArray($content, $dealloc, _${declaration.name}ToKt)"
                        }
                        is ResolvedIdlDictionary -> "toKotlinArray($content, ::toKotlinDictionary${declaration.name}, $dealloc, $deallocContent${if(useArena) ", arena" else ""})"
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlCallbackFunction ->
                if(useArena) "arena.toKotlinCallback<${decl.name}>($content!!.reinterpret(), $dealloc)"
                else "toKotlinCallback<${decl.name}>($content!!.reinterpret(), $dealloc)"
            is ResolvedIdlEnum -> "${decl.name}.entries[${content}.ordinal]"
            is ResolvedIdlDictionary -> "toKotlinDictionary${decl.name}(${content}, $dealloc)"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }

    private fun castToNative(type: ResolvedIdlType, content: String, dealloc: Boolean, useArena: Boolean, struct: String? = null): String {
        val struct = if(struct != null) ", struct = $struct" else ""
        return when(type) {
            is ResolvedIdlType.Void -> content
            is ResolvedIdlType.Default -> when(val decl = type.declaration) {
                is BuiltinIdlDeclaration -> when(decl.kind) {
                    WebIDLBuiltinKind.STRING ->
                        if(useArena) "toNativeString($content, arena)"
                        else "toNativeString($content$struct)"
                    WebIDLBuiltinKind.CHAR -> "$content.code.toUShort()"
                    WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                        when (declaration) {
                            is BuiltinIdlDeclaration -> {
                                val name = declaration.kind.simpleName()
                                if (useArena) "toNative${name}Array($content, arena)"
                                else "toNative${name}Array($content$struct)"
                            }
                            is ResolvedIdlEnum -> {
                                if (useArena) "toNativeEnumArray($content, arena, sizeOf<${cinteropPath}.${declaration.name}.Var>(), _${declaration.name}ToNative)"
                                else "toNativeEnumArray($content, sizeOf<${cinteropPath}.${declaration.name}.Var>(), _${declaration.name}ToNative$struct)"
                            }
                            is ResolvedIdlDictionary -> "toNativeArray($content, ::toNativeDictionary${declaration.name}${if(useArena) ", arena" else ""}$struct)"
                            else -> throw UnsupportedOperationException(type.toString())
                        }
                    }
                    else -> content
                }
                is ResolvedIdlEnum -> "${cinteropPath}.${decl.name}.entries[${content}.ordinal]"
                is ResolvedIdlCallbackFunction ->
                    if(dealloc) "arena.callback(toNativeCallback${decl.name}($content))"
                    else "toNativeCallback${decl.name}($content)"
                is ResolvedIdlDictionary -> "toNativeDictionary${decl.name}(${content})"
                else -> throw UnsupportedOperationException(type.toString())
            }
            else -> throw UnsupportedOperationException(type.toString())
        }
    }
}