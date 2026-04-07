package com.huskerdev.nativekt.printers

import com.huskerdev.nativekt.utils.allFields
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.printLabel
import com.huskerdev.nativekt.utils.toCDefType
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlCallbackFunction
import com.huskerdev.webidl.resolver.ResolvedIdlDictionary
import com.huskerdev.webidl.resolver.ResolvedIdlEnum
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import java.io.File
import kotlin.math.max

class HeaderPrinter(
    idl: IdlResolver,
    target: File,
    val guardName: String? = null
) {
    private val defName = "KOTLIN_NATIVE_${guardName}_H"

    init {
        target.parentFile.mkdirs()

        val builder = StringBuilder()
        printHeader(builder)

        printLabel(builder, "stdlib")
        printStdLib(builder)

        if(idl.callbacks.isNotEmpty()) {
            printLabel(builder, "Type defs")
            idl.dictionaries.values.forEach { printStructTypedef(builder, it) }
            idl.callbacks.values.forEach { printCallbackTypedef(builder, it) }
            builder.append("\n")
        }

        if(idl.enums.isNotEmpty()) {
            builder.append("\n")
            printLabel(builder, "Enums")
            idl.enums.values.forEach { printEnum(builder, it) }
        }

        if(idl.dictionaries.isNotEmpty()) {
            builder.append("\n")
            printLabel(builder, "Structs")
            idl.dictionaries.values.forEach { printStruct(builder, it) }
            idl.dictionaries.values.forEach { printStructNew(builder, it) }
        }

        printLabel(builder, "Functions")
        idl.globalOperators().forEach { printFunction(builder, it) }

        if(idl.callbacks.isNotEmpty()) {
            builder.append("\n")
            printLabel(builder, "Callbacks")
            printCallbacks(builder, idl.callbacks.values)
        }

        printFooter(builder)

        target.writeText(builder.toString().replace("\n", System.lineSeparator()))
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        append("\n")
        append(function.type.toCDefType())
        append(" ")
        append(function.name)
        append("(")

        function.args.joinTo(builder) {
            "${it.type.toCDefType()} ${it.name}"
        }

        append(");")
    }

    private fun printEnum(builder: StringBuilder, enum: ResolvedIdlEnum) = builder.apply {
        append("\ntypedef enum {\n\t")
        enum.elements.joinTo(builder, separator = ",\n\t") {
            "${enum.name}_${it}"
        }
        append("\n} ")
        append(enum.name)
        append(";\n")
    }

    private fun printStruct(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        append("\nstruct ")
        append(dictionary.name)
        append(" {")
        if(dictionary.implements != null)
            append(" // : ").append(dictionary.implements!!.name)
        append("\n\t")

        dictionary.allFields().joinTo(builder, separator = "\n\t") { field ->
            "${field.type.toCDefType()} ${field.name};"
        }
        append("\n};\n")
    }

    private fun printStructNew(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        append("\nstatic ")
        append(dictionary.name)
        append("* ")
        append(dictionary.name)
        append("_new(")
        dictionary.allFields().joinTo(builder) { field ->
            "${field.type.toCDefType()} ${field.name}"
        }
        append(") {\n\t")
        // malloc
        append(dictionary.name)
        append("* result = (")
        append(dictionary.name)
        append("*)malloc(sizeof(")
        append(dictionary.name)
        append("));\n\t")
        // set
        append("*result = (")
        append(dictionary.name)
        append("){ ")
        dictionary.allFields().joinTo(builder) { field ->
            field.name
        }
        append(" };\n\t")
        // return
        append("return result;\n}\n")
    }

    private fun printStructTypedef(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        append("\ntypedef struct ")
        append(dictionary.name)
        append(" ")
        append(dictionary.name)
        append(";")
    }

    private fun printCallbackTypedef(builder: StringBuilder, callback: ResolvedIdlCallbackFunction) = builder.apply {
        append("\ntypedef struct ")
        append(callback.name)
        append(" ")
        append(callback.name)
        append(";")
    }

    private fun printCallbacks(builder: StringBuilder, callbacks: Collection<ResolvedIdlCallbackFunction>) = builder.apply {
        val column1 = "Name"
        val column2 = "Type"
        val column3 = "Args"

        val names = arrayListOf<String>()
        val types = arrayListOf<String>()
        val args = arrayListOf<String>()

        callbacks.forEach { callback ->
            names += callback.name + ","
            types += callback.type.toCDefType() + if(callback.args.isNotEmpty()) "," else ""
            args += callback.args.joinToString { "${it.type.toCDefType()} ${it.name}" }
        }

        val width1 = max(column1.length, names.maxOf { it.length })
        val width2 = max(column2.length, types.maxOf { it.length })
        val width3 = max(column3.length, args.maxOf { it.length })

        // ┌───────┬─────────────────────┬────────────────┬─────────────────────────┐
        // │       │ Name                │ Type           │ Args                    │
        // └───────┴─────────────────────┴────────────────┴─────────────────────────┘
        // table
        append("// ┌───────┬")
        append("─".repeat(width1)).append("┬")
        append("─".repeat(width2)).append("┬")
        append("─".repeat(width3+1)).append("┐\n")
        append("// │  ...  │ ")
        append(column1).append(" ".repeat(width1 - column1.length - 1)).append("│ ")
        append(column2).append(" ".repeat(width2 - column2.length - 1)).append("│ ")
        append(column3).append(" ".repeat(width3 - column3.length)).append("│\n")
        append("// └───────┴")
        append("─".repeat(width1)).append("┴")
        append("─".repeat(width2)).append("┴")
        append("─".repeat(width3+1)).append("┘\n")

        for(i in callbacks.indices) {
            append("KCallbackDef(")

            // name
            append(names[i])
            append(" ".repeat(width1 - names[i].length))

            // type
            append(" ")
            append(types[i])
            append(" ".repeat(width2 - types[i].length ))

            // args
            if(args.isNotEmpty()) {
                append(" ")
                append(args[i])
                append(" ".repeat(width3 - args[i].length))
            }
            append(")\n")
        }
        append("#undef KCallbackDef\n")
    }

    private fun printHeader(builder: StringBuilder){
        builder.append("""
            /*
             * This file was automatically generated by Gradle.
             *
             * DO NOT EDIT THIS FILE MANUALLY.
             * Any changes made to this file will be overwritten the next time
             * the project is built.
             */
             

        """.trimIndent())

        if(guardName != null) {
            builder.append("#ifndef $defName\n")
            builder.append("#define $defName\n")
        }

        builder.append("""
            
            #include <stdlib.h>
            #include <stdarg.h>
            
            #include <stdint.h>
            #include <stdbool.h>
            
            #ifdef __cplusplus
            extern "C" {
            #endif
            
        """.trimIndent())
    }

    private fun printFooter(builder: StringBuilder){
        builder.append("""
            
            
            #ifdef __cplusplus
            }
            #endif
        """.trimIndent())

        if(guardName != null)
            builder.append("\n\n#endif // $defName")
    }

    private fun printStdLib(builder: StringBuilder){
        builder.append("""

            typedef int32_t  KInt;
            typedef int64_t  KLong;
            typedef float    KFloat;
            typedef double   KDouble;
            typedef int8_t   KByte;
            typedef int16_t  KShort;
            typedef bool     KBoolean;
            typedef uint16_t KChar;

            typedef struct KString {
                const char* data;
                KInt length;
            } KString;
            
            inline KString KString_new(const char* data, const KInt length) {
                return (KString) { data, length };
            }

            #define ARG_LENGTH(...) ARG_LENGTH__(__VA_ARGS__)
            #define ARG_LENGTH__(...) ARG_LENGTH_(,##__VA_ARGS__,                          \
                63, 62, 61, 60, 59, 58, 57, 56, 55, 54, 53, 52, 51, 50, 49, 48, 47, 46, 45,\
                44, 43, 42, 41, 40, 39, 38, 37, 36, 35, 34, 33, 32, 31, 30, 29, 28, 27, 26,\
                25, 24, 23, 22, 21, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6,\
                5, 4, 3, 2, 1, 0)
            #define ARG_LENGTH_(_, _63, _62, _61, _60, _59, _58, _57, _56, _55, _54, _53,  \
                _52, _51, _50, _49, _48, _47, _46, _45, _44, _43, _42, _41, _40, _39, _38, \
                _37, _36, _35, _34, _33, _32, _31, _30, _29, _28, _27, _26, _25, _24, _23, \
                _22, _21, _20, _19, _18, _17, _16, _15, _14, _13, _12, _11, _10, _9, _8,   \
                _7, _6, _5, _4, _3, _2, _1, Count, ...) Count
            
            #define KArrayDef(Name, Type, VarargType)	                    \
            typedef struct Name {			                                \
                const Type* elements;				                        \
                KInt size;				                                    \
            } Name;                                                         \
                                                                            \
            static Name Name##_new(const Type* elements, const KInt size) { \
                return (Name){ elements, size };                            \
            }																\
                                                                            \
            static Name _##Name##_of(const int n, ...) {                    \
                va_list args;                                               \
                va_start(args, n);                                          \
                Type* elements = (Type*)malloc(n * sizeof(Type));           \
                for (int i = 0; i < n; i++)                                 \
                    elements[i] = (Type)va_arg(args, VarargType);           \
                va_end(args);                                               \
                return (Name){ (const Type*) elements, n };                 \
            }                                                               \
            
            KArrayDef(KCharArray,	 KChar,    int32_t)
            KArrayDef(KBooleanArray, KBoolean, int32_t)
            KArrayDef(KByteArray,	 KByte,    int32_t)
            KArrayDef(KShortArray,	 KShort,   int32_t)
            KArrayDef(KIntArray,	 KInt,     int32_t)
            KArrayDef(KLongArray,	 KLong,    int64_t)
            KArrayDef(KFloatArray,	 KFloat,   double)
            KArrayDef(KDoubleArray,  KDouble,  double)
            KArrayDef(KArray,        void*,    void*)
            #undef KArrayDef
            
            #define KCharArray_of(...)    _KCharArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KBooleanArray_of(...) _KBooleanArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KByteArray_of(...)    _KByteArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KShortArray_of(...)   _KShortArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KIntArray_of(...)     _KIntArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            //#define KLongArray_of(...)    _KLongArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KFloatArray_of(...)   _KFloatArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KDoubleArray_of(...)  _KDoubleArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KArray_of(...)        _KArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)

            #define KCallbackDef(Name, Type, ...)		\
            struct Name {								\
                void *m;								\
                Type (*invoke)(Name* _, ##__VA_ARGS__);	\
                void (*free)(Name* _);					\
            };
            
        """.trimIndent())
    }
}