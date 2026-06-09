package com.huskerdev.nativekt.printers.kotlin.jvm

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized

class KotlinJvmForeignPrinter(
    idl: IdlResolver,
    builder: StringBuilder,
    val classPath: String,
    val name: String = "Foreign",
    parentClass: String? = null,
    val indent: String = ""
) {
    init {
        builder.append("${indent}private class ")
        builder.append(name)
        builder.append("(libraryPath: String, prefix: String = \"EXPORTED_")
        builder.append(classPath.replace(".", "_"))
        builder.append("_\")")
        if(parentClass != null)
            builder.append(": $parentClass")
        builder.append(" {\n\n")

        if(idl.callbacks.isNotEmpty()) {
            builder.append("\tcompanion object {\n")

            idl.dictionaries.values.forEach { printDictionaryLayout(builder, it) }
            idl.dictionaries.values.forEach { printDictionaryCasts(builder, it) }
            idl.callbacks.values.forEach { printCallbackInvoke(builder, it) }

            builder.append("\n$indent\t\tprivate val lookup = MethodHandles.lookup()\n")
            idl.callbacks.values.forEach { printCallbackUpcall(builder, it) }

            builder.append("\t}\n\n")
        }

        builder.append($$"""
            private val handle = SymbolLookup.libraryLookup(java.nio.file.Paths.get(libraryPath), Arena.global())
            
            private val addressKArrayFree = ForeignUtils.address(handle, "${prefix}KArray_free")
            private val handleKArrayFree = ForeignUtils.handle(addressKArrayFree, false, null, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            
        """.replaceIndent("\t"))

        buildList {
            addAll(listOf(
                "KString", "KCharArray", "KBooleanArray",
                "KByteArray", "KShortArray", "KIntArray",
                "KLongArray", "KFloatArray", "KDoubleArray"
            ))
            idl.dictionaries.values.mapTo(this) { it.name }
        }.forEach {
            builder.append("\n\tprivate val address${it.capitalized()}Free = ForeignUtils.address(handle, \"\${prefix}${it}_free\")")
            builder.append("\n\tprivate val handle${it.capitalized()}Free = ForeignUtils.handle(address${it.capitalized()}Free, false, null, ValueLayout.ADDRESS)\n")
        }
        builder.append("\n")

        idl.globalOperators().forEach {
            printFunctionHandle(builder, it)
        }

        builder.append("""
            
            override fun _address(name: String): Long =
                ForeignUtils.address(handle, name).address()
            
        """.replaceIndent("$indent\t"))

        idl.globalOperators().forEach {
            printFunctionCall(builder, it)
        }
        builder.append("${indent}}")
    }

    private fun printDictionaryLayout(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        append("$indent\t\tprivate val layout${dictionary.name.capitalized()} = CStructLayout(")
        buildList {
            dictionary.allFields().mapTo(this) { it.type.toForeignType() }
            add("ValueLayout.JAVA_BYTE")
        }.joinTo(builder)
        append(")\n")
    }

    private fun printDictionaryCasts(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val layout = "layout${dictionary.name.capitalized()}"
        val name = dictionary.name
        val fields = dictionary.allFields()

        // to native (heap)
        append("\n\t\tprivate fun toNativeDictionary${name.capitalized()}(of: $name) = ")
        append("ForeignUtils.malloc($layout.size).apply {")

        fields.forEachIndexed { i, it ->
            val value = castToNative(it.type, "of.${it.name}", useArena = false)
            val set = "set(${it.type.toForeignType()}, $layout[$i], ${value})"
            append("\n\t\t\t$set")
        }
        append("\n\t\t\tset(ValueLayout.JAVA_BYTE, $layout[${fields.size}], ForeignUtils.FLAG_RELEASABLE)")

        append("\n\t\t}\n")

        // to native (arena)
        append("\n\t\tprivate fun toNativeDictionary${name.capitalized()}OnArena(arena: Arena, of: $name) = ")
        append("arena.allocate($layout.size).apply {")

        fields.forEachIndexed { i, it ->
            val value = castToNative(it.type, "of.${it.name}", useArena = true)
            val set = "set(${it.type.toForeignType()}, $layout[$i], ${value})"
            append("\n\t\t\t$set")
        }
        append("\n\t\t\tset(ValueLayout.JAVA_BYTE, $layout[${fields.size}], ForeignUtils.FLAG_ON_STACK)")

        append("\n\t\t}\n")

        // to jvm
        append("\n\t\tprivate fun toJvmDictionary${dictionary.name.capitalized()}(of: MemorySegment)")
        append(" = of.reinterpret($layout.size).run { ${dictionary.name}(")

        fields.forEachIndexed { i, it ->
            val get = "get(${it.type.toForeignType()}, $layout[$i])"
            val value = castFromNative(it.type, get)
            append("\n\t\t\t$value,")
        }
        append("\n\t\t) }\n")
    }


    private fun printFunctionHandle(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        val isCriticalAlt = function.isCritical() && function.isCriticalCapable() && (function.hasString() || function.hasArray())

        append("${indent}\tprivate val handle")
        append(function.name.capitalized())
        append($$" = ForeignUtils.lookup(handle, \"${prefix}")
        append(function.name)
        if(isCriticalAlt)
            append("_")
        append("\", ")
        append(function.isCritical())
        append(", ")

        val args = arrayListOf(function.type.toForeignType())
        args += function.args.flatMap {
            when {
                isCriticalAlt && it.type.isString() -> listOf("ValueLayout.ADDRESS", "ValueLayout.JAVA_INT", "ValueLayout.JAVA_INT")
                isCriticalAlt && it.type.isArray() -> listOf("ValueLayout.ADDRESS", "ValueLayout.JAVA_INT")
                else -> listOf(it.type.toForeignType())
            }
        }
        args.joinTo(builder)
        append(")\n")
    }

    private fun printFunctionCall(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {

        val useArena = function.args.any {
            (it.type.isString() && !function.isCritical()) ||
            (it.type.isArray() && !function.isCritical()) ||
            it.type.isDictionary() ||
            it.type.isCallback()
        }

        val transforms = function.args.mapNotNull {
            when {
                function.isCritical() && it.type.isString() ->
                    "\n${indent}\t\tval _bytes_${it.name} = ${it.name}.toByteArray()"
                function.isCritical() && it.type.isBooleanArray() ->
                    "\n${indent}\t\tval _bytes_${it.name} = ByteArray(${it.name}.size) { if(${it.name}[it]) 1 else 0 }"
                function.isCritical() && it.type.isEnumArray() ->
                    "\n${indent}\t\tval _ints_${it.name} = IntArray(${it.name}.size) { ${it.name}[it].ordinal }"
                else -> null
            }
        }

        val args = function.args.flatMap {
            when {
                function.isCritical() && it.type.isString() ->
                    listOf("MemorySegment.ofArray(_bytes_${it.name})", "${it.name}.length", "_bytes_${it.name}.size")
                function.isCritical() && it.type.isBooleanArray() ->
                    listOf("MemorySegment.ofArray(_bytes_${it.name})", "${it.name}.size")
                function.isCritical() && it.type.isEnumArray() ->
                    listOf("MemorySegment.ofArray(_ints_${it.name})", "${it.name}.size")
                function.isCritical() && it.type.isArray() ->
                    listOf("MemorySegment.ofArray(${it.name})", "${it.name}.size")
                else ->
                    listOf(castToNative(it.type, it.name, useArena = useArena))
            }
        }.joinToString()

        val deallocFunc = if(function.isDealloc())
            freeFuncFor(function.type, "_result_native")
        else null

        val call = "(handle${function.name.capitalized()}.invokeExact($args) as ${function.type.toKotlinForeignType()})"

        // === Print ===

        append("\n${indent}\t")
        printFunctionHeader(builder, function, isOverride = true)

        when {
            useArena -> append(" = Arena.ofConfined().use { arena ->")
            function.isDealloc() || transforms.isNotEmpty() -> append(" {")
            else -> append(" = ")
        }

        transforms.joinTo(builder, separator = "")
        append("\n$indent\t\t")

        if(deallocFunc != null) {
            append("val _result_native = $call")
            append("\n$indent\t\t")
            append("val _result_jvm = ${castFromNative(function.type, "_result_native")}")
            append("\n$indent\t\t")
            append(deallocFunc)

            if(function.type !is ResolvedIdlType.Void) {
                append("\n$indent\t\t")
                if(useArena) append("_result_jvm")
                else append("return _result_jvm")
            }
        } else {
            if(!useArena && transforms.isNotEmpty())
                append("return ")
            append(castFromNative(function.type, call))
        }

        if(useArena || deallocFunc != null || transforms.isNotEmpty())
            append("\n$indent\t}")
        append("\n")
    }

    private fun printCallbackInvoke(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val args = buildList {
            add("_callback: MemorySegment")
            callback.args.mapTo(this) { "${it.name}: ${it.type.toKotlinForeignType()}" }
        }.joinToString()

        val lambdaArgs = callback.args.joinToString { castFromNative(it.type, it.name) }
        val type = callback.type.toKotlinForeignType()

        append("\n\t\t@JvmStatic fun invoke${callback.name}($args): $type =\n\t\t\t")

        val call = "(ForeignUtils.callbacks[_callback.address()] as ${callback.name})($lambdaArgs)"

        append(castToNative(callback.type, call, useArena = false))
        append("\n")
    }

    private fun printCallbackUpcall(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        append("\n\t\tprivate val upcall")
        append(callback.name)
        append(" = ForeignUtils.upcall(\n\t\t\t")

        // lookup
        append("lookup, \"invoke${callback.name}\",\n\t\t\t")
        append("MethodType.methodType(")

        val returnType = if(callback.type is ResolvedIdlType.Void)
            "Void::class.javaPrimitiveType"
        else "${callback.type.toKotlinForeignType()}::class.java"

        val argClasses = buildList {
            add(returnType)
            add("MemorySegment::class.java")
            callback.args.mapTo(this) { "${it.type.toKotlinForeignType()}::class.java" }
        }

        argClasses.joinTo(builder)
        append("),\n\t\t\t")

        // desc

        if(callback.type is ResolvedIdlType.Void)
            append("FunctionDescriptor.ofVoid(")
        else
            append("FunctionDescriptor.of(").append(callback.type.toForeignType()).append(", ")

        buildList {
            add("ValueLayout.ADDRESS")
            callback.args.mapTo(this) { it.type.toForeignType() }
        }.joinTo(builder)
        append(")\n\t\t)\n")
    }

    private fun freeFuncFor(
        type: ResolvedIdlType,
        content: String
    ) = when {
        type.isString() -> "handleKStringFree.invoke($content)"
        type.isArray() -> (type as ResolvedIdlType.Default).firstParam { _, declaration ->
            when (declaration) {
                is BuiltinIdlDeclaration -> "handleK${declaration.kind.simpleName()}ArrayFree.invoke($content)"
                is ResolvedIdlEnum -> "handleKIntArrayFree.invoke($content)"
                is ResolvedIdlDictionary -> "handleKArrayFree.invoke($content, address${declaration.name}Free)"
                else -> throw UnsupportedOperationException(type.toString())
            }
        }
        type.isCallback() -> "ForeignUtils.callbackFree($content)"
        type.isDictionary() -> "handle${(type as ResolvedIdlType.Default).declaration.name.capitalized()}Free.invoke($content)"
        else -> null
    }

    private fun castFromNative(type: ResolvedIdlType, content: String): String = when(type) {
        is ResolvedIdlType.Void -> content
        is ResolvedIdlType.Default -> when(type.declaration) {
            is BuiltinIdlDeclaration -> when((type.declaration as BuiltinIdlDeclaration).kind) {
                WebIDLBuiltinKind.STRING -> "ForeignUtils.toJvmString($content)"
                WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                    when (declaration) {
                        is BuiltinIdlDeclaration -> "ForeignUtils.toJvm${declaration.kind.simpleName()}Array($content)"
                        is ResolvedIdlEnum -> "ForeignUtils.toJvmEnumArray($content, ${declaration.name}::class.java)"
                        is ResolvedIdlDictionary -> "ForeignUtils.toJvmArray($content, ::toJvmDictionary${declaration.name}, ${declaration.name}::class.java)"
                        else -> throw UnsupportedOperationException(type.toString())
                    }
                }
                else -> content
            }
            is ResolvedIdlCallbackFunction -> "ForeignUtils.toJvmCallback($content)"
            is ResolvedIdlEnum -> "${type.declaration.name}.entries[$content]"
            is ResolvedIdlDictionary -> "toJvmDictionary${type.declaration.name}(${content})"
            else -> throw UnsupportedOperationException(type.toString())
        }
        else -> throw UnsupportedOperationException(type.toString())
    }

    private fun castToNative(
        type: ResolvedIdlType,
        content: String,
        useArena: Boolean,
    ): String {
        return when (type) {
            is ResolvedIdlType.Void -> content
            is ResolvedIdlType.Default -> when (type.declaration) {
                is BuiltinIdlDeclaration -> when ((type.declaration as BuiltinIdlDeclaration).kind) {
                    WebIDLBuiltinKind.STRING ->
                        if (useArena) "ForeignUtils.toNativeStringOnArena(arena, $content)"
                        else "ForeignUtils.toNativeString($content)"
                    WebIDLBuiltinKind.LIST -> type.firstParam { _, declaration ->
                        when (declaration) {
                            is BuiltinIdlDeclaration -> {
                                val name = declaration.kind.simpleName()
                                if (useArena) "ForeignUtils.toNative${name}ArrayOnArena(arena, $content)"
                                else "ForeignUtils.toNative${name}Array($content)"
                            }
                            is ResolvedIdlEnum ->
                                if (useArena) "ForeignUtils.toNativeEnumArrayOnArena(arena, $content)"
                                else "ForeignUtils.toNativeEnumArray($content)"
                            is ResolvedIdlDictionary ->
                                if (useArena) "ForeignUtils.toNativeArrayOnArena(arena, $content, ::toNativeDictionary${declaration.name}OnArena)"
                                else "ForeignUtils.toNativeArray($content, ::toNativeDictionary${declaration.name})"
                            else -> throw UnsupportedOperationException(type.toString())
                        }
                    }
                    else -> content
                }
                is ResolvedIdlCallbackFunction ->
                    if (useArena) "ForeignUtils.createCallbackOnArena(arena, $content, upcall${type.declaration.name})"
                    else "ForeignUtils.createCallback($content, upcall${type.declaration.name})"
                is ResolvedIdlEnum -> "$content.ordinal"
                is ResolvedIdlDictionary ->
                    if (useArena) "toNativeDictionary${type.declaration.name}OnArena(arena, $content)"
                    else "toNativeDictionary${type.declaration.name}($content)"
                else -> throw UnsupportedOperationException(type.toString())
            }
            else -> throw UnsupportedOperationException(type.toString())
        }
    }

    fun ResolvedIdlType.toForeignType(): String = when(this) {
        is ResolvedIdlType.Union -> throw UnsupportedOperationException("Union type are not unsupported")
        is ResolvedIdlType.Void -> "null"
        is ResolvedIdlType.Default -> when(declaration) {
            is BuiltinIdlDeclaration -> when(val a = (declaration as BuiltinIdlDeclaration).kind) {
                WebIDLBuiltinKind.CHAR -> "ValueLayout.JAVA_CHAR"
                WebIDLBuiltinKind.BOOLEAN -> "ValueLayout.JAVA_BOOLEAN"
                WebIDLBuiltinKind.BYTE -> "ValueLayout.JAVA_BYTE"
                WebIDLBuiltinKind.SHORT -> "ValueLayout.JAVA_SHORT"
                WebIDLBuiltinKind.INT -> "ValueLayout.JAVA_INT"
                WebIDLBuiltinKind.LONG -> "ValueLayout.JAVA_LONG"
                WebIDLBuiltinKind.FLOAT -> "ValueLayout.JAVA_FLOAT"
                WebIDLBuiltinKind.DOUBLE -> "ValueLayout.JAVA_DOUBLE"
                WebIDLBuiltinKind.STRING -> "ValueLayout.ADDRESS"
                WebIDLBuiltinKind.LIST -> "ValueLayout.ADDRESS"
                else -> throw UnsupportedOperationException(a.toString())
            }
            is ResolvedIdlEnum -> "ValueLayout.JAVA_INT"
            else -> "ValueLayout.ADDRESS"
        }
    }
}