package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File

class KotlinNativePrinter(
    idl: IdlResolver,
    target: File,
    val classPath: String,
    val moduleName: String,
    useCoroutines: Boolean,
    val expectActual: Boolean
) {
    val cinteropPath = "cinterop.$classPath"

    init {
        val actual = if(expectActual) "actual " else ""

        val builder = StringBuilder()
        builder.append("""
            @file:OptIn(ExperimentalForeignApi::class, ExperimentalContracts::class, ExperimentalExtendedContracts::class)
            @file:Suppress("unused", "UNNECESSARY_SAFE_CALL")
            
            package $classPath
            
            import kotlinx.cinterop.*
            import kotlin.contracts.*
            import com.huskerdev.nativekt.*
            import com.huskerdev.nativekt.kn.*
            import platform.posix.*
            
            ${actual}val isLib${moduleName.capitalized()}Loaded: Boolean = true
            
            @Throws(UnsupportedOperationException::class)
            ${actual}fun ${syncLoadFunctionName(moduleName)}() = Unit
            ${actual}fun ${asyncLoadFunctionName(moduleName)}(onReady: () -> Unit) = onReady()
            
        """.trimIndent())
        if(useCoroutines)
            builder.append("${actual}suspend fun ${asyncLoadFunctionName(moduleName)}() = Unit\n")

        builder.append("""
            
            private val _handleKStringFree = staticCFunction<COpaquePointer?, Unit> {
            	if(it == null) return@staticCFunction
            	$cinteropPath.${mangle("KString_free")}(it.reinterpret())
            }
            
        """.trimIndent())


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

        if(idl.interfaces.isNotEmpty()) {
            printLabel(builder, "Interfaces")
            idl.interfaces.values.forEach { printInterface(builder, it) }
        }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun mangle(name: String) =
        mangle(classPath, moduleName, name)

    private fun printInterface(builder: StringBuilder, inter: ResolvedIdlInterface) = builder.apply {
        val name = inter.name.upperCamelCase()
        append("""
            
            actual class $name(val _ptr: COpaquePointer): NativeKtResource() {
                companion object {
                    internal fun _wrap(ptr: COpaquePointer?): $name? = 
                        ptr?.run { $name(this) }
                }
                
        """.trimIndent())

        if(inter.constructors.size == 1) {
            val constructor = inter.constructors[0]
            val nativeFunc = "_" + interfaceConstructorCName(inter, constructor).camelCase()

            append("\n\tactual constructor(")
            constructor.args.joinTo(this) {
                "${it.name.camelCase()}: ${it.type.toKotlinType()}"
            }
            append("): this($nativeFunc(")
            constructor.args.joinTo(this) { it.name }
            append(")._ptr)")
        }
        inter.operations.forEach { operation ->
            val nativeFunc = "_" + interfaceOperationCName(inter, operation).camelCase()

            append("\n\tactual fun ${operation.name.camelCase()}(")
            operation.args.joinTo(this) {
                "${it.name.camelCase()}: ${it.type.toKotlinType()}"
            }
            append(") = $nativeFunc(")
            buildList {
                add("this")
                operation.args.mapTo(this) { it.name }
            }.joinTo(this)
            append(")")
        }
        append("""
            
            
                override fun _close() = _${interfaceFreeCName(inter).camelCase()}(this)
            }
            
        """.trimIndent())

        inter.toOperations().forEach {
            printFunction(builder, it, true)
        }
    }

    private fun printDictionaryCasts(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val name = dictionary.name.upperCamelCase()

        // free handle
        append($$"""
            
            private val _handle$${name}Free = staticCFunction<COpaquePointer?, Unit> {
                if(it == null) return@staticCFunction
                $$cinteropPath.$${mangle("${name}_free")}(it.reinterpret())
            }
            
        """.trimIndent())

        // native (arena)

        append($$"""
            
            private fun MemScope.toNative$${name}OnArena(of: $$name?): CPointer<$$cinteropPath.$$name>? {
                contract {
                    (of != null).implies(returnsNotNull())
                }
                if(of == null) return null
                val mem = alloc<$$cinteropPath.$$name>()
        """.trimIndent())

        dictionary.allFields().forEach {
            val value = castToNative(
                type = it.type,
                content = "of.${it.name.camelCase()}",
                useArena = true,
                pin = false
            )
            append("\n\tmem.${it.name} = $value")
        }
        append("""
            
                mem.__flags = 0
                return mem.ptr
            }
            
        """.trimIndent())

        // native

        append($$"""
            
            private fun toNative$$name(of: $$name?): CPointer<$$cinteropPath.$$name>? {
                contract {
                    (of != null).implies(returnsNotNull())
                }
                if(of == null) return null
                val mem = malloc(sizeOf<$$cinteropPath.$$name>().convert())!!.reinterpret<$$cinteropPath.$$name>().pointed
        """.trimIndent())

        dictionary.allFields().forEach {
            val value = castToNative(
                type = it.type,
                content = "of.${it.name.camelCase()}",
                useArena = false,
                pin = false
            )
            append("\n\tmem.${it.name} = $value")
        }
        append("""
            
                mem.__flags = FLAG_RELEASABLE.toByte()
                return mem.ptr
            }
            
        """.trimIndent())

        // kotlin

        append($$"""
            
            private fun toKotlin$$name(of: CPointer<$$cinteropPath.$$name>?): $$name? {
                contract {
                    (of != null).implies(returnsNotNull())
                }
                if(of == null) return null
                val mem = of.pointed
                return $$name(
        """.trimIndent())

        dictionary.allFields().forEach {
            append("\n\t\t${it.name} = ${castFromNative(it.type, "mem.${it.name.camelCase()}")},")
        }
        append("\n\t)\n}\n")

        // kotlin (not-null)

        append($$"""
            
            private fun toKotlin$$name(of: CPointer<$$cinteropPath.$$name>): $$name =
                toKotlin$$name(of as CPointer<$$cinteropPath.$$name>?)
            
        """.trimIndent())
    }

    private fun printCallbackWrap(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val name = callback.name.upperCamelCase()

        // Header
        append("\nprivate val _invoke$name: CPointer<CFunction<(")
        buildList {
            add("CPointer<$cinteropPath.$name>")
            callback.args.mapTo(this) { it.type.toKnType() }
        }.joinTo(builder)
        append(") -> ${callback.type.toKnType()}>> =")

        // staticCFunction
        append("\n\tstaticCFunction { ")
        buildList {
            add("_callback")
            callback.args.mapTo(this) { it.name.camelCase() }
        }.joinTo(builder)
        append(" ->")

        // Call
        val args = callback.args.joinToString {
            castFromNative(it.type, it.name.camelCase())
        }
        val call = "toKotlinCallback<$name>(_callback)($args)"
        append("\n\t\t${castToNative(callback.type, call, useArena = false, pin = false)}")

        // End
        append("\n\t}\n")
    }

    private fun printFunction(
        builder: StringBuilder,
        function: ResolvedIdlOperation,
        isInterfaceFunction: Boolean = false
    ) = builder.apply {

        val useArena = function.args.any {
            it.type.isString() ||
            it.type.isArray() ||
            it.type.isDictionary() ||
            it.type.isCallback()
        }

        val args = function.args.joinToString {
            castToNative(
                it.type,
                it.name.camelCase(),
                useArena = useArena,
                pin = function.isCritical()
            )
        }

        val deallocFunc = if(function.type.isReleasable())
            freeFuncFor(function.type, "_result_native")
        else null

        val call = "$cinteropPath.${mangle(function.name)}($args)"

        // === Print ===

        append('\n')
        printFunctionHeader(builder, function,
            name = (if(isInterfaceFunction) "_" else "") + function.name.camelCase(),
            isActual = expectActual && !isInterfaceFunction,
            isPrivate = isInterfaceFunction
        )

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
    ): String? = when {
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() -> "${cinteropPath}.${mangle("${type.toCType()}Array_free")}($content?.reinterpret())"
                type.isEnum() -> "${cinteropPath}.${mangle("KIntArray_free")}($content?.reinterpret())"
                else -> "${cinteropPath}.${mangle("KArray_free")}($content, _handle${type.toCType(ptr = false)}Free)"
            }
        }
        type.isCallback() -> "callbackFree($content?.reinterpret())"
        type.isDictionary() -> "${cinteropPath}.${mangle("${type.toCType(ptr = false)}_free")}($content)"
        else -> null
    }

    private fun castFromNative(
        type: ResolvedIdlType,
        content: String
    ): String {
        val nullable = if(type.isNullable) "?" else "!!"
        val nullable1 = if(type.isNullable) "" else "!!"
        return when {
            type.isArray() && type.isUnsigned() -> castToUnsigned(type, castFromNative(type.toSignedType(), content))
            type.isChar() -> "$content.toInt().toChar()"
            type.isString() -> "toKotlinKString($content$nullable.reinterpret())"
            type.isCallback() -> "toKotlinCallback<${type.toKotlinType()}>($content$nullable1)"
            type.isEnum() -> "${type.declaration.name}.entries[${content}.ordinal]"
            type.isDictionary() -> "toKotlin${type.declaration.name}($content$nullable1)"
            type.isInterface() -> "${type.declaration.name.upperCamelCase()}._wrap($content)$nullable1"
            type.isArray() -> type.arrayType { type ->
                when {
                    type.isPrimitive() -> "toKotlin${type.toCType()}Array($content$nullable.reinterpret())"
                    type.isEnum() -> "toKotlinEnumArray<${type.declaration.name}>($content$nullable.reinterpret())"
                    else -> {
                        val fn = castFromNative(type, "").split("(")[0]
                        if(type.isNullable) {
                            val nType = if(type.isString())
                                 "nativekt.internals.KString"
                            else "$cinteropPath.${type.toCType(ptr = false)}"

                            "toKotlinKArray<${type.toKotlinType()}, CPointer<$nType>>($content?.reinterpret(), ::$fn)"
                        } else "toKotlinKArray($content!!.reinterpret(), ::$fn)"
                    }
                }
            }
            else -> content
        }
    }

    private fun castToNative(
        type: ResolvedIdlType,
        content: String,
        useArena: Boolean,
        pin: Boolean
    ): String {
        val nullable = if(type.isNullable) "?" else ""
        return when {
            type.isArray() && type.isUnsigned() -> castToNative(type.toSignedType(), castToSigned(type, content), useArena, pin)
            type.isChar() -> "$content.code.toUShort()"
            type.isEnum() -> "${cinteropPath}.${type.declaration.name}.entries[$content.ordinal]"
            type.isString() ->
                if(useArena) "toNativeKStringOnArena($content, $pin)$nullable.reinterpret()"
                else "toNativeKString($content)$nullable.reinterpret()"
            type.isCallback() ->
                if(useArena) "toNativeCallbackOnArena($content, _invoke${type.declaration.name.capitalized()})$nullable.reinterpret()"
                else "toNativeCallback($content, _invoke${type.declaration.name.capitalized()})$nullable.reinterpret()"
            type.isDictionary() ->
                if (useArena) "toNative${type.declaration.name}OnArena($content)"
                else "toNative${type.declaration.name}($content)"
            type.isInterface() -> "$content._ptr"
            type.isArray() -> type.arrayType { type ->
                when {
                    type.isPrimitive() ->
                        if (useArena) "toNative${type.toCType()}ArrayOnArena($content, $pin)$nullable.reinterpret()"
                        else "toNative${type.toCType()}Array($content)$nullable.reinterpret()"
                    type.isEnum() ->
                        if (useArena) "toNativeEnumArrayOnArena($content, $pin)$nullable.reinterpret()"
                        else "toNativeEnumArray($content)$nullable.reinterpret()"
                    else -> {
                        val fn = castToNative(type, "", useArena, false).split("(")[0]
                        if (useArena) "toNativeKArrayOnArena($content, ::$fn)$nullable.reinterpret()"
                        else "toNativeKArray($content, ::$fn)$nullable.reinterpret()"
                    }
                }
            }
            else -> content
        }
    }

    private fun ResolvedIdlType.toKnType(): String = when {
        isVoid() -> "Unit"
        else -> {
            val ref = "${cinteropPath}.${toCType(ptr = false)}"
            if(isString() || isArray() || isCallback() || isDictionary())
                "CPointer<$ref>?" else ref
        }
    }
}