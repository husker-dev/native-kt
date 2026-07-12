package com.huskerdev.nativekt.printers.kotlin

import com.huskerdev.nativekt.plugin.Language
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File

class KotlinNativePrinter(
    idl: IdlResolver,
    target: File,
    language: Language,
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
            ${actual}fun ${syncLoadFunctionName(moduleName)}() {
                ${if(language == Language.CPP) 
                    "$cinteropPath.${mangle("init")}() // Init C++" 
                else "// Do nothing (statically linked)"}
            }
            
            ${actual}fun ${asyncLoadFunctionName(moduleName)}(onReady: () -> Unit) {
                ${syncLoadFunctionName(moduleName)}()
                onReady()
            }
            
        """.trimIndent())
        if(useCoroutines)
            builder.append("${actual}suspend fun ${asyncLoadFunctionName(moduleName)}() = ${syncLoadFunctionName(moduleName)}()\n")

        builder.append("""
            
            private val _handleKStringFree = staticCFunction<COpaquePointer?, Unit> {
            	if(it == null) return@staticCFunction
            	$cinteropPath.${mangle("kstring_free")}(it.reinterpret())
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

        if(idl.allOperators().isNotEmpty()) {
            printLabel(builder, "Functions")
            idl.allOperators().forEach { printFunction(builder, it) }
        }

        if(idl.interfaces.isNotEmpty()) {
            printLabel(builder, "Interfaces")
            idl.interfaces.values.forEach { printInterface(builder, it) }
        }

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun mangle(name: String) =
        mangle(classPath, moduleName, "_$name")

    private fun printInterface(builder: StringBuilder, inter: ResolvedIdlInterface) = builder.apply {
        val name = inter.name.upperCamelCase()
        append("""
            
            actual class $name(val _ptr: COpaquePointer): NativeKtResource() {
                companion object {
                    internal fun _wrap(ptr: COpaquePointer?): $name? = 
                        ptr?.run { $name(this) }
                }
        """.trimIndent())

        inter.toOperations().forEach { operation ->
            val args = operation.args.map {
                "${it.kname}: ${it.type.toKotlinType()}"
            }
            val argNames = operation.args.map { it.kname }

            append("\n\t")
            append(when {
                operation.isInterfaceOperationConstructor() ->
                    "actual constructor(${args.joinToString()}): this(${operation.kname}(${argNames.joinToString()}))"
                operation.isInterfaceOperationFn() -> {
                    val args = args.drop(1).joinToString()
                    val argNames = argNames.toMutableList()
                        .apply { set(0, "this") }
                        .joinToString()
                    val name = operation.interfaceFunctionName().camelCase()
                    "actual fun ${name}($args) = ${operation.kname}($argNames)"
                }
                operation.isInterfaceOperationFree() ->
                    "override fun _close(): Unit = ${operation.kname}(this)"
                else -> throw UnsupportedOperationException()
            })
        }
        append("\n}\n")
    }

    private fun printDictionaryCasts(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val name = dictionary.kname
        val cname = "$cinteropPath.${dictionary.cname}"

        // free handle
        append($$"""
            
            private val _handle$${name}Free = staticCFunction<COpaquePointer?, Unit> {
                if(it == null) return@staticCFunction
                $$cinteropPath.$${dictionary.subCFunc(classPath, moduleName, "free")}(it.reinterpret())
            }
            
        """.trimIndent())

        // native (arena)

        append($$"""
            
            private fun MemScope.toNative$${name}OnArena(of: $$name?): CPointer<$$cname>? {
                contract {
                    (of != null).implies(returnsNotNull())
                }
                if(of == null) return null
                val mem = alloc<$$cname>()
        """.trimIndent())

        dictionary.allFields().forEach {
            val value = castToNative(
                type = it.type,
                content = "of.${it.kname}",
                useArena = true,
                pin = false
            )
            append("\n\tmem.${it.cname} = $value")
        }
        append("""
            
                mem.__flags = 0
                return mem.ptr
            }
            
        """.trimIndent())

        // native

        append($$"""
            
            private fun toNative$$name(of: $$name?): CPointer<$$cname>? {
                contract {
                    (of != null).implies(returnsNotNull())
                }
                if(of == null) return null
                val mem = malloc(sizeOf<$$cname>().convert())!!.reinterpret<$$cname>().pointed
        """.trimIndent())

        dictionary.allFields().forEach {
            val value = castToNative(
                type = it.type,
                content = "of.${it.kname}",
                useArena = false,
                pin = false
            )
            append("\n\tmem.${it.cname} = $value")
        }
        append("""
            
                mem.__flags = FLAG_RELEASABLE.toByte()
                return mem.ptr
            }
            
        """.trimIndent())

        // kotlin

        append($$"""
            
            private fun toKotlin$$name(of: CPointer<$$cname>?): $$name? {
                contract {
                    (of != null).implies(returnsNotNull())
                }
                if(of == null) return null
                val mem = of.pointed
                return $$name(
        """.trimIndent())

        dictionary.allFields().forEach {
            append("\n\t\t${it.kname} = ${castFromNative(it.type, "mem.${it.cname}")},")
        }
        append("\n\t)\n}\n")

        // kotlin (not-null)

        append($$"""
            
            private fun toKotlin$$name(of: CPointer<$$cname>): $$name =
                toKotlin$$name(of as CPointer<$$cname>?)
            
        """.trimIndent())
    }

    private fun printCallbackWrap(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        val name = callback.kname
        val cname = "$cinteropPath.${callback.cname}"

        // Header
        append("\nprivate val _invoke$name: CPointer<CFunction<(")
        buildList {
            add("CPointer<$cname>")
            callback.args.mapTo(this) { it.type.toKnType() }
        }.joinTo(builder)
        append(") -> ${callback.type.toKnType()}>> =")

        // staticCFunction
        append("\n\tstaticCFunction { ")
        buildList {
            add("_callback")
            callback.args.mapTo(this) { it.kname }
        }.joinTo(builder)
        append(" ->")

        // Call
        val args = callback.args.joinToString {
            castFromNative(it.type, it.kname)
        }
        val call = "toKotlinCallback<$name>(_callback)($args)"
        append("\n\t\t${castToNative(callback.type, call, useArena = false, pin = false)}")

        // End
        append("\n\t}\n")
    }

    private fun printFunction(
        builder: StringBuilder,
        function: ResolvedIdlOperation
    ) = builder.apply {

        val isInterfaceFunction = function.isInterfaceOperation()
        val isInterfaceConstructor = function.isInterfaceOperationConstructor()

        val useArena = function.args.any {
            it.type.isString() ||
            it.type.isArray() ||
            it.type.isDictionary() ||
            it.type.isCallback()
        }

        val args = function.args.joinToString {
            castToNative(
                it.type,
                it.kname,
                useArena = useArena,
                pin = function.isCritical()
            )
        }

        val deallocFunc = if(function.type.isReleasable())
            freeFuncFor(function.type, "_result_native")
        else null

        val call = "$cinteropPath.${function.cnameMangled(classPath, moduleName)}($args)"

        // === Print ===

        append('\n')
        printFunctionHeader(builder, function,
            name = function.kname,
            printType = !isInterfaceConstructor,
            isActual = expectActual && !isInterfaceFunction,
            isPrivate = isInterfaceFunction
        )
        if(isInterfaceConstructor)
            append(": COpaquePointer")

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
        } else if(isInterfaceConstructor)
            append("$call!!")
        else
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
                type.isPrimitive() -> "${cinteropPath}.${mangle("${type.toCType().lowercase()}_array_free")}($content?.reinterpret())"
                type.isEnum() -> "${cinteropPath}.${mangle("kint_array_free")}($content?.reinterpret())"
                else -> "${cinteropPath}.${mangle("karray_free")}($content, _handle${type.toCType(ptr = false)}Free)"
            }
        }
        type.isCallback() -> "callbackFree($content?.reinterpret())"
        type.isDictionary() -> "${cinteropPath}.${mangle("${type.toCType(ptr = false).lowercase()}_free")}($content)"
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
            type.isEnum() -> "${type.declaration.kname}.entries[${content}.ordinal]"
            type.isDictionary() -> "toKotlin${type.declaration.kname}($content$nullable1)"
            type.isInterface() -> "${type.declaration.kname}._wrap($content)$nullable1"
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
            type.isEnum() -> "${cinteropPath}.${type.declaration.cname}.entries[$content.ordinal]"
            type.isString() ->
                if(useArena) "toNativeKStringOnArena($content, $pin)$nullable.reinterpret()"
                else "toNativeKString($content)$nullable.reinterpret()"
            type.isCallback() ->
                if(useArena) "toNativeCallbackOnArena($content, _invoke${type.declaration.kname})$nullable.reinterpret()"
                else "toNativeCallback($content, _invoke${type.declaration.kname})$nullable.reinterpret()"
            type.isDictionary() ->
                if (useArena) "toNative${type.declaration.kname}OnArena($content)"
                else "toNative${type.declaration.kname}($content)"
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