package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.isString
import com.huskerdev.nativekt.utils.printFunctionHeader
import com.huskerdev.nativekt.utils.toKotlinType
import com.huskerdev.webidl.resolver.BuiltinIdlDeclaration
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlField
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind
import org.gradle.internal.extensions.stdlib.capitalized

class KotlinJvmForeignPrinter(idl: IdlResolver, builder: StringBuilder) {
    init {
        builder.append("""
            private class Foreign: NativeInvoker {
                private val lookup = SymbolLookup.loaderLookup()
                
                private val _char = type("char")
                private val _int = type("int")
                private val _longlong = type("long long")
                private val _pointer = (type("void*") as AddressLayout)
                    .withTargetLayout(MemoryLayout.sequenceLayout(Long.MAX_VALUE, _char))
            
                private fun type(name: String) =
                    Linker.nativeLinker().canonicalLayouts()[name] as ValueLayout
            
                private fun _lookup(name: String, retType: ValueLayout, vararg argTypes: ValueLayout): MethodHandle {
                    return Linker.nativeLinker().downcallHandle(
                        lookup.findOrThrow(name),
                        FunctionDescriptor.of(retType, *argTypes)
                    )
                }
                
            
            """.trimIndent())
        idl.globalOperators().forEach {
            printFunctionHandle(builder, it)
        }
        builder.append("\n")
        idl.globalOperators().forEach {
            printFunctionCall(builder, it)
        }
        builder.append("}")
    }

    private fun printFunctionHandle(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\tprivate val handle")
        append(function.name.capitalized())
        append(" = _lookup(\"")
        append(function.name)
        append("\", ")
        append(function.type.toForeignType())
        append(", ")
        function.args.flatMap {
            if(it.type.isString())
                listOf("_pointer", "_longlong")
            else listOf(it.type.toForeignType())
        }.joinTo(builder)
        append(")\n")
    }

    private fun printFunctionCall(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\t")
        printFunctionHeader(builder, function, isOverride = true, name = "_${function.name}")
        append(" = ")

        val useArena = function.args.any {
            it.type.isString()
        }
        if(useArena)
            append("Arena.ofConfined().use {\n\t\t")

        val type = if(function.type.isString())
            ""
        else function.type.toKotlinType()

        val call = "handle${function.name.capitalized()}.invokeExact(${castArgs(function.args)}) as $type"
        append(call)

        if(useArena)
            append("\n\t}")
        append("\n")
    }

    private fun castArgs(args: List<ResolvedIdlField.Argument>): String {
        return args.flatMap { arg ->
            if(arg.type.isString()) {
                listOf("it.allocateFrom(${arg.name})", "${arg.name}.length.toLong()")
            } else
                listOf(arg.name)
        }.joinToString()
    }

    fun ResolvedIdlType.toForeignType(): String = when(this) {
        is ResolvedIdlType.Union -> throw UnsupportedOperationException("Union type are not unsupported")
        is ResolvedIdlType.Void -> "void"
        is ResolvedIdlType.Default -> buildString {
            append(when(declaration) {
                is BuiltinIdlDeclaration -> when((declaration as BuiltinIdlDeclaration).kind) {
                    WebIDLBuiltinKind.BOOLEAN -> "bool"
                    WebIDLBuiltinKind.BYTE -> "int8_t"
                    WebIDLBuiltinKind.UNSIGNED_BYTE -> "uint8_t"
                    WebIDLBuiltinKind.SHORT -> "int16_t"
                    WebIDLBuiltinKind.UNSIGNED_SHORT -> "uint16_t"
                    WebIDLBuiltinKind.INT -> "_int"
                    WebIDLBuiltinKind.UNSIGNED_INT -> "uint32_t"
                    WebIDLBuiltinKind.LONG -> "int64_t"
                    WebIDLBuiltinKind.UNSIGNED_LONG -> "uint32_t"
                    WebIDLBuiltinKind.UNRESTRICTED_FLOAT -> "float"
                    WebIDLBuiltinKind.UNRESTRICTED_DOUBLE -> "double"
                    WebIDLBuiltinKind.STRING -> "char*"
                    else -> throw UnsupportedOperationException()
                }
                else -> declaration.name
            })
            if(parameters.isNotEmpty())
                throw UnsupportedOperationException("Parameters are not null")
        }
    }
}