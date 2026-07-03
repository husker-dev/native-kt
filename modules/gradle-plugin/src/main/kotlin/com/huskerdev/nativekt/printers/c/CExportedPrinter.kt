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
            
            #define KArrayFreeDef(name, dname)                                   \
            NATIVEKT_EXPORT void ${exportedName("##dname##_free")}(name* self) { \
                name##_free(self);                                               \
            }
            KArrayFreeDef(KCharArray, ${"KCharArray".snakeCase()})
            KArrayFreeDef(KBooleanArray, ${"KBooleanArray".snakeCase()})
            KArrayFreeDef(KByteArray, ${"KByteArray".snakeCase()})
            KArrayFreeDef(KUByteArray, ${"KUByteArray".snakeCase()})
            KArrayFreeDef(KShortArray, ${"KShortArray".snakeCase()})
            KArrayFreeDef(KUShortArray, ${"KUShortArray".snakeCase()})
            KArrayFreeDef(KIntArray, ${"KIntArray".snakeCase()})
            KArrayFreeDef(KUIntArray, ${"KUIntArray".snakeCase()})
            KArrayFreeDef(KLongArray, ${"KLongArray".snakeCase()})
            KArrayFreeDef(KULongArray, ${"KULongArray".snakeCase()})
            KArrayFreeDef(KFloatArray, ${"KFloatArray".snakeCase()})
            KArrayFreeDef(KDoubleArray, ${"KDoubleArray".snakeCase()})
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
            "${it.type.toCType()} _arg_${it.name.snakeCase()}"
        }

        // == Function call ==
        append("\t")
        if(function.type !is ResolvedIdlType.Void)
            append("return ")

        val args = function.args.joinToString { "_arg_${it.name.snakeCase()}" }
        append("${function.name.snakeCase()}($args);\n}\n")
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
        "EXPORTED_${classPath.replace(".", "_")}_${name.snakeCase()}"
}

internal fun printCriticalNativeFunctionContent(builder: StringBuilder, name: String, function: ResolvedIdlOperation) = builder.apply {
    // == Type and name ==
    append(function.type.toCType(enumAsInt = true))
    append(" ")
    append(name)

    // == Function args ==
    function.args.flatMap {
        val name = it.name.snakeCase()
        when {
            it.type.isString() -> listOf("const char* _arr_$name", "int32_t _length_$name, size_t _size_$name")
            it.type.isArray() -> {
                val type = (it.type as ResolvedIdlType.Default).arrayType { type -> type.toCType(enumAsInt = true) }
                listOf("$type* _arr_$name", "int32_t _length_$name")
            }
            else -> listOf("${it.type.toCType(enumAsInt = true)} _arg_$name")
        }
    }.joinTo(this, prefix = "(", postfix = ") {")

    // == Casts ==
    function.args.forEach {
        val name = it.name.snakeCase()
        when {
            it.type.isString() -> append("\n\tKString _arg_$name = (KString) { _arr_$name, _size_$name,_length_$name, 0 };")
            it.type.isArray() -> {
                val type = it.type.toCType(enumAsInt = true, ptr = false)
                append("\n\t$type _arg_$name = ($type) { _arr_$name, sizeof(_arr_$name[0]) * _length_$name, _length_$name, 0 };")
            }
        }
    }

    // == Call args ==
    val args = function.args.joinToString {
        val name = it.name.snakeCase()
        if(it.type.isString() || it.type.isArray()) {
            if(it.type.isNullable)
                "_length_$name == -1 ? 0 : &_arg_$name"
            else "&_arg_$name"
        } else "_arg_$name"
    }

    // == Function call ==
    append("\n\t")
    if(function.type !is ResolvedIdlType.Void)
        append("return ")

    val call = "${function.name.snakeCase()}($args)"
    append(call)
    append(";\n}\n")
}