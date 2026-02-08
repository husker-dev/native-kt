package com.huskerdev.nativekt.printers.jvm

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import org.gradle.internal.extensions.stdlib.capitalized

class KotlinJvmForeignPrinter(
    idl: IdlResolver,
    builder: StringBuilder,
    val classPath: String,
    name: String = "Foreign",
    parentClass: String? = null,
    val indent: String = ""
) {
    init {
        builder.append("${indent}private class ")
        builder.append(name)
        if(parentClass != null)
            builder.append(": $parentClass")
        builder.append(" {\n")

        printArena(builder, indent + "\t")
        builder.append("\n\n")

        builder.append($$"""
            private val lookup = SymbolLookup.loaderLookup()
            private val linker = Linker.nativeLinker()

            private fun lookup(name: String, isCritical: Boolean, retType: ValueLayout?, vararg argTypes: ValueLayout): MethodHandle {
                val function = if(retType == null)
                    FunctionDescriptor.ofVoid(*argTypes)
                else FunctionDescriptor.of(retType, *argTypes)
                
                val options = if(isCritical)
                    arrayOf(Linker.Option.critical(true))
                else emptyArray()
                
                val address = lookup.findOrThrow("Foreign_$${classPath.replace(".", "_")}_$${name}_${name}")
                
                return linker.downcallHandle(address, function, *options)
            }
            
            private fun MemorySegment.getString(arena: Arena, dealloc: Boolean) = if(dealloc) {
                reinterpret(Long.MAX_VALUE, arena) {
                     println(this@getString.isMapped)
                    if(this@getString.isMapped)
                        this@getString.unload()
                    else
                        freeHandle.invoke(this@getString)
                }.getString(0)
            } else
                reinterpret(Long.MAX_VALUE).getString(0)
        
            private val freeHandle = linker.downcallHandle(
                linker.defaultLookup().find("free").get(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
            )
            
            """.replaceIndent(indent + "\t"))

        builder.append("\n")
        idl.globalOperators().forEach {
            printFunctionHandle(builder, it)
        }
        idl.globalOperators().forEach {
            printFunctionCall(builder, it)
        }
        builder.append("${indent}}")
    }

    private fun printArena(builder: StringBuilder, indent: String){
        builder.append("""
            
            private class CustomArena: AutoCloseable {
            	companion object {
            		fun <T> use(block: CustomArena.() -> T) =
            			CustomArena().use { block(it) }
            	}

            	private val linker = Linker.nativeLinker()
            	private val freeHandle = linker.downcallHandle(
            		linker.defaultLookup().find("free").get(),
            		FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
            	)

            	private val arena = Arena.ofConfined()
            	private val allocated = hashMapOf<Long, MemorySegment>()

            	fun String.cstr(): MemorySegment =
            		arena.allocateFrom(this).also { allocated[it.address()] = it }

            	fun MemorySegment.asString(dealloc: Boolean): String {
            		val result = reinterpret(Long.MAX_VALUE).getString(0)
            		if(dealloc && address() !in allocated)
            			freeHandle.invoke(this)
            		return result
            	}

            	override fun close() = arena.close()
            }
        """.replaceIndent(indent))
    }

    private fun printFunctionHandle(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("${indent}\tprivate val handle")
        append(function.name.capitalized())
        append(" = lookup(\"")
        append(function.name)
        append("\", ")
        append(function.isCritical())
        append(", ")

        val args = arrayListOf(function.type.toForeignType())
        args += function.args.map {
            it.type.toForeignType()
        }
        args.joinTo(builder)
        append(")\n")
    }

    private fun printFunctionCall(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\n${indent}\t")
        printFunctionHeader(builder, function,
            isOverride = true,
            name = "_${function.name}",
            forcePrintVoid = true
        )
        append(" = ")

        val useArena = function.args.any { it.type.isString() } || function.type.isString()

        if(useArena)
            append("CustomArena.use { \n\t\t")
        else append("\n${indent}\t\t")

        val type = if(function.type.isString())
            "MemorySegment"
        else function.type.toKotlinType()

        val args = function.args.joinToString { castToNative(it.type, it.name) }
        val call = "(handle${function.name.capitalized()}.invokeExact($args) as $type)"
        append(castFromNative(function.type, call, function.isDealloc()))

        if(useArena)
            append("\n\t}")
        append("\n")
    }

    private fun castFromNative(type: ResolvedIdlType, content: String, dealloc: Boolean): String {
        return if(type.isString())
            "$content.asString($dealloc)"
        else content
    }

    private fun castToNative(type: ResolvedIdlType, content: String): String {
        return if(type.isString())
            "$content.cstr()"
        else content
    }

    fun ResolvedIdlType.toForeignType(): String = when(this) {
        is ResolvedIdlType.Union -> throw UnsupportedOperationException("Union type are not unsupported")
        is ResolvedIdlType.Void -> "null"
        is ResolvedIdlType.Default -> buildString {
            append(when(declaration) {
                is BuiltinIdlDeclaration -> when(val a = (declaration as BuiltinIdlDeclaration).kind) {
                    WebIDLBuiltinKind.CHAR -> "ValueLayout.JAVA_CHAR"
                    WebIDLBuiltinKind.BOOLEAN -> "ValueLayout.JAVA_BOOLEAN"
                    WebIDLBuiltinKind.BYTE,
                    WebIDLBuiltinKind.UNSIGNED_BYTE -> "ValueLayout.JAVA_BYTE"
                    WebIDLBuiltinKind.SHORT,
                    WebIDLBuiltinKind.UNSIGNED_SHORT -> "ValueLayout.JAVA_SHORT"
                    WebIDLBuiltinKind.INT,
                    WebIDLBuiltinKind.UNSIGNED_INT -> "ValueLayout.JAVA_INT"
                    WebIDLBuiltinKind.LONG,
                    WebIDLBuiltinKind.UNSIGNED_LONG -> "ValueLayout.JAVA_LONG"
                    WebIDLBuiltinKind.FLOAT,
                    WebIDLBuiltinKind.UNRESTRICTED_FLOAT -> "ValueLayout.JAVA_FLOAT"
                    WebIDLBuiltinKind.DOUBLE,
                    WebIDLBuiltinKind.UNRESTRICTED_DOUBLE -> "ValueLayout.JAVA_DOUBLE"
                    WebIDLBuiltinKind.STRING -> "ValueLayout.ADDRESS"
                    else -> throw UnsupportedOperationException(a.toString())
                }
                else -> declaration.name
            })
            if(parameters.isNotEmpty())
                throw UnsupportedOperationException("Parameters are not null")
        }
    }
}