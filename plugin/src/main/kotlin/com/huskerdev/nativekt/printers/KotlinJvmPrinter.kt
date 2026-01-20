package com.huskerdev.nativekt.printers

import com.huskerdev.webidl.resolver.BuiltinIdlDeclaration
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlCallbackFunction
import com.huskerdev.webidl.resolver.ResolvedIdlDictionary
import com.huskerdev.webidl.resolver.ResolvedIdlEnum
import com.huskerdev.webidl.resolver.ResolvedIdlField
import com.huskerdev.webidl.resolver.ResolvedIdlInterface
import com.huskerdev.webidl.resolver.ResolvedIdlNamespace
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import com.huskerdev.webidl.resolver.ResolvedIdlTypeDef
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind
import org.gradle.internal.extensions.stdlib.capitalized
import java.io.File

class KotlinJvmPrinter(
    idl: IdlResolver,
    target: File,
    classPath: String,
    moduleName: String
) {

    init {
        val builder = StringBuilder()
        builder.append("package $classPath\n\n")
        builder.append("import java.lang.foreign.*\n")
        builder.append("import natives.util.NativesUtil\n")
        listOf(
            "lookup", "_int", "_longlong", "_pointer"
        ).joinTo(builder, "\n") {
            "import natives.util.ForeignUtils.$it"
        }

        builder.append("\n\n")
        builder.append("""
            |actual fun loadLib${moduleName.capitalized()}() {
            |    NativesUtil.loadLib("$moduleName")
            |    impl = if(NativesUtil.supportsForeign) Foreign() else JNI()
            |}
            |
            |actual suspend fun loadLib${moduleName.capitalized()}Async() = 
            |    loadLib${moduleName.capitalized()}()
            |    
            |private lateinit var impl: NativeInvoker
            |
            |private sealed interface NativeInvoker {
            |    ${idl.globalOperators().joinToString("\n\t", transform = ::functionHeader)}
            |}
            |
            |// === Functions ===
            |
        """.trimMargin("|"))

        idl.globalOperators().forEach { printFunctionProxy(builder, it) }

        builder.append("\n\n// === Implementation ===\n\n")

        ForeignPrinter(idl, builder)
        builder.append("\n\n")
        JNIPrinter(idl, builder)

        target.parentFile.mkdirs()
        target.writeText(builder.toString())
    }

    private fun printFunctionProxy(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append('\n')
        printFunctionHeader(builder, function, isActual = true)
        append(" = impl.")
        append(function.name)
        function.args.joinTo(this, prefix = "(", postfix = ")\n") { it.name }
    }

    class JNIPrinter(idl: IdlResolver, builder: StringBuilder) {
        init {
            builder.append("private class JNI: NativeInvoker {\n")
            idl.globalOperators().forEach {
                builder.append("\t")
                printFunctionHeader(builder, it, isExternal = true, isOverride = true)
                builder.append("\n")
            }
            builder.append("}")
        }
    }

    class ForeignPrinter(idl: IdlResolver, builder: StringBuilder) {
        init {
            builder.append("private class Foreign: NativeInvoker {\n\n")
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
            append(" = lookup(\"")
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
            printFunctionHeader(builder, function, isOverride = true)
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

}