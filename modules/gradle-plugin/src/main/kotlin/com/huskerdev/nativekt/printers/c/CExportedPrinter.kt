package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import java.io.File

class CExportedPrinter(
    idl: IdlResolver,
    target: File,
    val classPath: String,
) {
    init {
        val builder = StringBuilder()
        builder.append("""
            #include "api.h"
            
            #ifndef NATIVEKT_EXPORT
                #if defined(_WIN32) || defined(__CYGWIN__)
                  #define NATIVEKT_EXPORT __declspec(dllexport)
                #else
                  #define NATIVEKT_EXPORT __attribute__((visibility("default")))
                #endif
            #endif
            
            
            NATIVEKT_EXPORT void ${exportedName("KString_free")}(KString* self) {
                KString_free(self);
            }
            
            NATIVEKT_EXPORT void ${exportedName("KArray_free")}(KArray* self, void (*freeOp)(void*)) {
                KArray_free(self, freeOp);
            }
            
            #define KArrayFreeDef(Name)                                         \
            NATIVEKT_EXPORT void ${exportedName("##Name##_free")}(Name* self) { \
                Name##_free(self);                                              \
            }
            KArrayFreeDef(KCharArray)
            KArrayFreeDef(KBooleanArray)
            KArrayFreeDef(KByteArray)
            KArrayFreeDef(KShortArray)
            KArrayFreeDef(KIntArray)
            KArrayFreeDef(KLongArray)
            KArrayFreeDef(KFloatArray)
            KArrayFreeDef(KDoubleArray)
            #undef KArrayFreeDef
            
        """.trimIndent())

        idl.dictionaries.values.forEach {
            builder.append("\n")
            builder.append("""
                NATIVEKT_EXPORT void ${exportedName("${it.name}_free")}(${it.name}* self) {
                    ${it.name}_free(self);
                }
            """.trimIndent())
            builder.append("\n")
        }

        idl.globalOperators().forEach {
            printFunction(builder, it)
            if(it.isCriticalCapable() && (it.hasString() || it.hasArray()))
                printFunctionCritical(builder, it)
        }

        target.writeText(builder.toString())
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        // == Type and name ==
        append("\nNATIVEKT_EXPORT ${function.type.toCType()} ${function.exportedName()}")

        // == Function args ==
        function.args.joinTo(this, prefix = "(", postfix = ") {\n") {
            "${it.type.toCType()} _arg_${it.name}"
        }

        // == Function call ==
        append("\t")
        if(function.type !is ResolvedIdlType.Void)
            append("return ")

        val args = function.args.joinToString { "_arg_${it.name}" }
        append("${function.name}($args);\n}\n")
    }

    private fun printFunctionCritical(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\nNATIVEKT_EXPORT ")
        printCriticalNativeFunctionContent(
            builder,
            name = "${function.exportedName()}_",
            function
        )
    }

    private fun ResolvedIdlOperation.exportedName() =
        exportedName(name)

    private fun exportedName(name: String) =
        "EXPORTED_${classPath.replace(".", "_")}_${name}"
}

internal fun printCriticalNativeFunctionContent(builder: StringBuilder, name: String, function: ResolvedIdlOperation) = builder.apply {
    // == Type and name ==
    append(function.type.toCType(enumAsInt = true))
    append(" ")
    append(name)

    // == Function args ==
    function.args.flatMap {
        when {
            it.type.isString() -> listOf("const char* _arr_${it.name}", "int32_t _length_${it.name}, size_t _size_${it.name}")
            it.type.isArray() -> {
                val type = (it.type as ResolvedIdlType.Default).arrayType { type -> type.toCType(enumAsInt = true) }
                listOf("$type* _arr_${it.name}", "int32_t _length_${it.name}")
            }
            else -> listOf("${it.type.toCType(enumAsInt = true)} _arg_${it.name}")
        }
    }.joinTo(this, prefix = "(", postfix = ") {")

    // == Casts ==
    function.args.forEach {
        val name = it.name
        when {
            it.type.isString() -> append("\n\tKString _arg_$name = (KString) { _arr_$name, _size_$name,_length_$name,  K_FLAG_ON_STACK };")
            it.type.isArray() -> {
                val type = it.type.toCType(enumAsInt = true, ptr = false)
                append("\n\t$type _arg_$name = ($type) { _arr_$name, sizeof(_arr_$name[0]) * _length_$name, _length_$name, K_FLAG_ON_STACK };")
            }
        }
    }

    // == Call args ==
    val args = function.args.joinToString {
        if(it.type.isString() || it.type.isArray()) {
            if(it.type.isNullable)
                "_length_${it.name} == -1 ? 0 : &_arg_${it.name}"
            else "&_arg_${it.name}"
        } else "_arg_${it.name}"
    }

    // == Function call ==
    append("\n\t")
    if(function.type !is ResolvedIdlType.Void)
        append("return ")

    val call = "${function.name}($args)"
    append(call)
    append(";\n}\n")
}