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
            
            private fun String.makeKString(arena: NativeArena) = cValue<$cinteropPath.KString> {
                data = this@makeKString.cstr.getPointer(arena.scope)
                length = this@makeKString.length
                arena.ptr(data!!)
            }
            
            private fun String.makeKString() = cValue<$cinteropPath.KString> {
                data = strdup(this@makeKString)
                length = this@makeKString.length
            }
            
            private fun CValue<$cinteropPath.KString>.unwrapKString(dealloc: Boolean): String =
                useContents { data }!!.unwrapCStr(dealloc)
            
            private fun CValue<$cinteropPath.KString>.unwrapKString(arena: NativeArena, dealloc: Boolean): String =
                arena.unwrapCStr(useContents { data }!!, dealloc)
            
        """.trimIndent())

        printLabel(builder, "Arrays")
        printArrayCasts(builder)
        idl.enums.values.forEach { printEnumCast(builder, it) }

        printLabel(builder, "Callbacks")

        idl.callbacks.values.forEach { printCallbackWrap(builder, it) }

        printLabel(builder, "Functions")

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
        val args = listOf("_callback") + callback.args.map { it.name }

        val castedArgs = callback.args.joinToString { castFromNative(it.type, it.name, it.isDealloc(), false) }

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
        append(castFromNative(function.type, call, function.isDealloc(), useArena))

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
    
                private fun toNative${type}Array(array: ${type}Array) = cValue<$cinteropPath.K${type}Array> {
                    val bytes = array.size * ${type}.SIZE_BYTES
                    elements = mallocExact(bytes.toUInt()).reinterpret()
                    size = array.size
                    array.usePinned { memcpy(elements, it.addressOf(0), bytes.toULong()) }
                }
    
                private fun toKotlin${type}Array(struct: CValue<$cinteropPath.K${type}Array>, arena: NativeArena, dealloc: Boolean) = struct.useContents {
                    ${type}Array(size) { elements!![it] }.also {
                        if(dealloc) arena.freeMem(elements!!)
                    }
                }
    
                private fun toKotlin${type}Array(struct: CValue<$cinteropPath.K${type}Array>, dealloc: Boolean) = struct.useContents {
                    ${type}Array(size) { elements!![it] }.also {
                        if(dealloc) free(elements!!)
                    }
                }
                
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

            private fun toNativeCharArray(array: CharArray) = cValue<$cinteropPath.KCharArray> {
                val bytes = array.size * Char.SIZE_BYTES
                elements = mallocExact(bytes.toUInt()).reinterpret()
                size = array.size
                array.usePinned { memcpy(elements, it.addressOf(0), bytes.toULong()) }
            }

            private fun toKotlinCharArray(struct: CValue<$cinteropPath.KCharArray>, arena: NativeArena, dealloc: Boolean) = struct.useContents {
                CharArray(size) { elements!![it].toInt().toChar() }.also {
                    if(dealloc) arena.freeMem(elements!!)
                }
            }

            private fun toKotlinCharArray(struct: CValue<$cinteropPath.KCharArray>, dealloc: Boolean) = struct.useContents {
                CharArray(size) { elements!![it].toInt().toChar() }.also {
                    if(dealloc) free(elements!!)
                }
            }
            
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

            private fun toNativeBooleanArray(array: BooleanArray) = cValue<$cinteropPath.KBooleanArray> {
            	val bytes = array.size * Byte.SIZE_BYTES
            	elements = mallocExact(bytes.toUInt()).reinterpret()
            	size = array.size
            	val byteArray = array.map { it.toByte() }.toByteArray()
            	byteArray.usePinned { memcpy(elements, it.addressOf(0), bytes.toULong()) }
            }

            private fun toKotlinBooleanArray(struct: CValue<$cinteropPath.KBooleanArray>, arena: NativeArena, dealloc: Boolean) = struct.useContents {
            	BooleanArray(size) { elements!![it].value }.also {
            		if(dealloc) arena.freeMem(elements!!)
            	}
            }

            private fun toKotlinBooleanArray(struct: CValue<$cinteropPath.KBooleanArray>, dealloc: Boolean) = struct.useContents {
            	BooleanArray(size) { elements!![it].value }.also {
            		if(dealloc) free(elements!!)
            	}
            }
            
        """.trimIndent())

        // Enum casts
        append("""
            // Array: Enum

            private fun <T: Enum<T>, N: CPrimitiveVar> toNativeEnumArray(
                array: Array<T>,
                arena: NativeArena,
                typeSize: Long,
                setter: (from: Array<T>, to: CPointer<N>) -> Unit
            ) = cValue<cinterop.natives.test.KArray> {
                val bytes = array.size * typeSize
                elements = arena.ptr(mallocExact(bytes.toUInt()).reinterpret())
                size = array.size
                setter(array, elements!!.reinterpret())
            }
            
            private fun <T: Enum<T>, N: CPrimitiveVar> toNativeEnumArray(
                array: Array<T>,
                typeSize: Long,
                setter: (from: Array<T>, to: CPointer<N>) -> Unit
            ) = cValue<cinterop.natives.test.KArray> {
                val bytes = array.size * typeSize
                elements = mallocExact(bytes.toUInt()).reinterpret()
                size = array.size
                setter(array, elements!!.reinterpret())
            }
            
            private fun <T: Enum<T>, N: CPrimitiveVar> toKotlinEnumArray(
                struct: CValue<cinterop.natives.test.KArray>,
                arena: NativeArena, 
                dealloc: Boolean,
                converter: (size: Int, elements: CPointer<N>) -> Array<T>
            ) = struct.useContents {
                converter(size, this.elements!!.reinterpret()).also {
                    if(dealloc) arena.freeMem(this.elements!!)
                }
            }
            
            private fun <T: Enum<T>, N: CPrimitiveVar> toKotlinEnumArray(
                struct: CValue<cinterop.natives.test.KArray>,
                dealloc: Boolean,
                converter: (size: Int, elements: CPointer<N>) -> Array<T>
            ) = struct.useContents {
                converter(size, this.elements!!.reinterpret()).also {
                    if(dealloc) free(this.elements!!)
                }
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

    private fun castFromNative(type: ResolvedIdlType, content: String, dealloc: Boolean, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.CHAR -> "$content.toInt().toChar()"
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "$content.unwrapKString(arena, $dealloc)"
                    else "$content.unwrapKString($dealloc)"
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
                        is ResolvedIdlDictionary -> content
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlCallbackFunction ->
                if(useArena) "arena.unwrapCallback<${decl.name}>($content!!.reinterpret(), $dealloc)"
                else "unwrapCallback<${decl.name}>($content!!.reinterpret(), $dealloc)"
            is ResolvedIdlEnum -> "${decl.name}.entries[${content}.ordinal]"
            is ResolvedIdlDictionary -> content
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }

    private fun castToNative(type: ResolvedIdlType, content: String, dealloc: Boolean, useArena: Boolean): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(val decl = type.declaration) {
            is BuiltinIdlDeclaration -> when(decl.kind) {
                WebIDLBuiltinKind.STRING ->
                    if(useArena) "$content.makeKString(arena)"
                    else "$content.makeKString()"
                WebIDLBuiltinKind.CHAR -> "$content.code.toUShort()"
                WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                    when (declaration) {
                        is BuiltinIdlDeclaration -> {
                            val name = declaration.kind.simpleName()
                            if (useArena) "toNative${name}Array($content, arena)"
                            else "toNative${name}Array($content)"
                        }
                        is ResolvedIdlEnum -> {
                            if (useArena) "toNativeEnumArray($content, arena, sizeOf<${cinteropPath}.${declaration.name}.Var>(), _${declaration.name}ToNative)"
                            else "toNativeEnumArray($content, sizeOf<${cinteropPath}.${declaration.name}.Var>(), _${declaration.name}ToNative)"
                        }
                        is ResolvedIdlDictionary -> content
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlEnum -> "${cinteropPath}.${decl.name}.entries[${content}.ordinal]"
            is ResolvedIdlCallbackFunction ->
                if(dealloc) "arena.callback($content.wrap${decl.name}())"
                else "$content.wrap${decl.name}()"
            is ResolvedIdlDictionary -> content
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }
}