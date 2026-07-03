package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import java.io.File
import kotlin.math.max

class CApiHeaderPrinter(
    idl: IdlResolver,
    target: File,
    val guardName: String? = null,
    val isInternal: Boolean = false
) {
    private val defName = "KOTLIN_NATIVE_${guardName}_H"

    init {
        target.parentFile.mkdirs()

        val builder = StringBuilder()
        printHeader(builder)

        printLabel(builder, "stdlib")
        printStdLib(builder)

        if(idl.callbacks.isNotEmpty() || idl.dictionaries.isNotEmpty()) {
            printLabel(builder, "Type defs")
            idl.dictionaries.values.forEach {
                builder.append("\ntypedef struct ${it.name.upperCamelCase()} ${it.name.upperCamelCase()};")
            }
            idl.callbacks.values.forEach {
                builder.append("\ntypedef struct ${it.name.upperCamelCase()} ${it.name.upperCamelCase()};")
            }
            if(isInternal)
                builder.append("\ntypedef struct _AbstractCallback _AbstractCallback;")
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
            idl.dictionaries.values.forEach {
                printStruct(builder, it)
                printStructFunctions(builder, it)
            }
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
        append(function.type.toCType(printNullable = true))
        append(" ")
        append(function.name.snakeCase())
        append("(")

        function.args.joinTo(builder) {
            "${it.type.toCType(printNullable = true)} ${it.name.snakeCase()}"
        }

        append(");")
    }

    private fun printEnum(builder: StringBuilder, enum: ResolvedIdlEnum) = builder.apply {
        append("\ntypedef enum {\n\t")
        enum.elements.joinTo(builder, separator = ",\n\t") {
            "${enum.name.upperCamelCase()}_${it}"
        }
        append("\n} ")
        append(enum.name.upperCamelCase())
        append(";\n")
    }

    private fun printStruct(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        append("\nstruct ")
        append(dictionary.name.upperCamelCase())
        append(" {")
        if(dictionary.implements != null)
            append(" // : ").append(dictionary.implements!!.name.upperCamelCase())
        append("\n\t")

        buildList {
            dictionary.allFields().mapTo(this) { field ->
                "${field.type.toCType(printNullable = true)} ${field.name.snakeCase()};"
            }
            add("char __flags;")
        }.joinTo(builder, separator = "\n\t")

        append("\n};\n")
    }

    private fun printStructFunctions(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val name = dictionary.name.upperCamelCase()

        append("\n$name* _Nonnull ${name}_new(")

        dictionary.allFields().joinTo(builder) { field ->
            "${field.type.toCType(printNullable = true)} ${field.name.snakeCase()}"
        }
        append(");")

        append("\n$name* _Nullable ${name}_clone(const $name* _Nullable self);")
        append("\nvoid ${name}_free($name* _Nullable self);")
        if(isInternal) {
            append("\nvoid _${name}_free_forced($name* _Nullable self);")
        }
        append("\n")
    }

    private fun printCallbacks(builder: StringBuilder, callbacks: Collection<ResolvedIdlCallbackFunction>) = builder.apply {
        val column1 = "Name"
        val column2 = "Type"
        val column3 = "Args"

        val names = arrayListOf<String>()
        val types = arrayListOf<String>()
        val args = arrayListOf<String>()

        callbacks.forEach { callback ->
            names += callback.name.upperCamelCase() + ","
            types += callback.type.toCType(printNullable = true) + if(callback.args.isNotEmpty()) "," else ""
            args += callback.args.joinToString { "${it.type.toCType(printNullable = true)} ${it.name.snakeCase()}" }
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
        if(isInternal)
            append("KCallbackDef(_AbstractCallback, void)\n")
        append("#undef KCallbackDef\n")

        if(isInternal) {
            append("\nvoid _AbstractCallback_free(_AbstractCallback* _Nullable self);")
            append("\nvoid __AbstractCallback_free_forced(_AbstractCallback* _Nullable self);\n")
        }
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
            #include <stdint.h>
            #include <stdbool.h>
            
            #ifdef __cplusplus
            extern "C" {
            #endif
            
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
            
        """.trimIndent())

        if(isInternal) {
            builder.append("""
                
                #define K_FLAG_RELEASABLE 1
                #define K_FLAG_DATA_OWNER 2
                
                #define K_OBJECT_IS_RELEASABLE(flags) ((flags) & K_FLAG_RELEASABLE)
                #define K_OBJECT_IS_DATA_OWNER(flags) ((flags) & K_FLAG_DATA_OWNER)
                
            """.trimIndent())
        }
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

    private fun printStdLib(builder: StringBuilder) = builder.apply {
        append("""
            
            typedef int32_t  KInt;
            typedef uint32_t KUInt;
            typedef int64_t  KLong;
            typedef uint64_t KULong;
            typedef float    KFloat;
            typedef double   KDouble;
            typedef int8_t   KByte;
            typedef uint8_t  KUByte;
            typedef int16_t  KShort;
            typedef uint16_t KUShort;
            typedef bool     KBoolean;
            typedef uint16_t KChar;

            typedef struct KString {
                const char* _Nonnull data;
                size_t size;
                KInt length;
                char __flags;
            } KString;
            
            KString* _Nonnull KString_new(
                const char* _Nonnull data, 
                KInt length, 
                size_t size, 
                bool is_data_owner
            );
            KString* _Nullable KString_clone(const KString* _Nullable self);
            void KString_free(KString* _Nullable self);
            
            #define KArrayDef(Name, Type)                                                \
            typedef struct Name {                                                        \
                const Type* _Nonnull elements;                                           \
                size_t size;				                                             \
                KInt length;				                                             \
                char __flags;                                                            \
            } Name;                                                                      \
                                                                                         \
            Name* _Nonnull Name##_new(                     \
                const Type* _Nonnull elements,             \
                const KInt length,                         \
                bool is_data_owner                         \
            );                                             \
            Name* _Nonnull _##Name##_of(const int n, ...);
            
            KArrayDef(KCharArray,	 KChar          )
            KArrayDef(KBooleanArray, KBoolean       )
            KArrayDef(KByteArray,	 KByte          )
            KArrayDef(KUByteArray,	 KUByte         )
            KArrayDef(KShortArray,	 KShort         )
            KArrayDef(KUShortArray,	 KUShort        )
            KArrayDef(KIntArray,	 KInt           )
            KArrayDef(KUIntArray,	 KUInt          )
            KArrayDef(KLongArray,	 KLong          )
            KArrayDef(KULongArray,	 KULong         )
            KArrayDef(KFloatArray,	 KFloat         )
            KArrayDef(KDoubleArray,  KDouble        )
            KArrayDef(KArray,        void* _Nullable)
            #undef KArrayDef
            
            #define KCharArray_of(...)    _KCharArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KBooleanArray_of(...) _KBooleanArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KByteArray_of(...)    _KByteArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KUByteArray_of(...)   _KUByteArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KShortArray_of(...)   _KShortArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KUShortArray_of(...)  _KUShortArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KIntArray_of(...)     _KIntArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KUIntArray_of(...)    _KUIntArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KFloatArray_of(...)   _KFloatArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KDoubleArray_of(...)  _KDoubleArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KArray_of(...)        _KArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            
            #define KArrayCloneFreeDef(Name, Type)                    \
            Name* _Nullable Name##_clone(const Name* _Nullable self); \
            void Name##_free(Name* _Nullable self);
            
            KArrayCloneFreeDef(KCharArray,    KChar)
            KArrayCloneFreeDef(KBooleanArray, KBoolean)
            KArrayCloneFreeDef(KByteArray,    KByte)
            KArrayCloneFreeDef(KUByteArray,   KUByte)
            KArrayCloneFreeDef(KShortArray,   KShort)
            KArrayCloneFreeDef(KUShortArray,  KUShort)
            KArrayCloneFreeDef(KIntArray,     KInt)
            KArrayCloneFreeDef(KUIntArray,    KUInt)
            KArrayCloneFreeDef(KLongArray,    KLong)
            KArrayCloneFreeDef(KULongArray,   KULong)
            KArrayCloneFreeDef(KFloatArray,   KFloat)
            KArrayCloneFreeDef(KDoubleArray,  KDouble)
            #undef KArrayCloneFreeDef
            
            KArray* _Nullable KArray_clone(const KArray* _Nullable self, void* _Nullable (* _Nullable clone_op)(void* _Nullable));
            void KArray_free(const KArray* _Nullable self, void (* _Nonnull free_op)(void* _Nonnull));

            #define KCallbackDef(Name, Type, ...)                                       \
            struct Name {                                                               \
                char __flags;                                                           \
                Type (* _Nonnull invoke)(Name* _Nonnull self, ##__VA_ARGS__);           \
                Name* _Nonnull (* _Nonnull clone)(Name* _Nonnull self);                 \
                KBoolean (* _Nonnull equals)(Name* _Nonnull self, Name* _Nullable obj); \
                KInt (* _Nonnull hash_code)(Name* _Nonnull self);                       \
                void (* _Nonnull free)(Name* _Nullable self);                           \
            };
            
        """.trimIndent())

        if(isInternal) {
            arrayOf(
                "KString",
                "KCharArray",
                "KBooleanArray",
                "KByteArray",
                "KUByteArray",
                "KShortArray",
                "KUShortArray",
                "KIntArray",
                "KUIntArray",
                "KLongArray",
                "KULongArray",
                "KFloatArray",
                "KDoubleArray"
            ).joinTo(builder, separator = "") {
                "\nvoid _${it}_free_forced($it* _Nullable self);"
            }
            append("\nvoid _KArray_free_forced(KArray* _Nullable self, void (* _Nonnull free_op)(void* _Nullable));")
            append("\n")
        }
    }
}

internal fun cloneFuncFor(
    type: ResolvedIdlType,
    content: String
): String = when {
    type.isArray() -> type.arrayType { type ->
        when {
            type.isPrimitive() -> "${type.toCType(ptr = false)}Array_clone($content)"
            type.isEnum() -> "KIntArray_clone($content)"
            else -> "KArray_clone($content, (void*) ${cloneFuncFor(type, "").dropLast(2)})"
        }
    }
    type.isCallback() -> "$content->clone($content)"
    type.isDictionary() || type.isString() -> "${type.toCType(ptr = false)}_clone($content)"
    else -> content
}

internal fun freeFuncFor(
    type: ResolvedIdlType,
    content: String
): String? = when {
    type.isArray() -> type.arrayType { type ->
        when {
            type.isPrimitive() -> "${type.toCType(ptr = false)}Array_free($content)"
            type.isEnum() -> "KIntArray_free($content)"
            else -> "KArray_free($content, (void*) ${freeFuncFor(type, "")!!.dropLast(2)})"
        }
    }
    type.isCallback() -> "_AbstractCallback_free((_AbstractCallback*) $content)"
    type.isDictionary() || type.isString() -> "${type.toCType(ptr = false)}_free($content)"
    else -> null
}

internal fun forceFreeFuncFor(
    type: ResolvedIdlType,
    content: String
): String? = when {
    type.isArray() -> type.arrayType { type ->
        when {
            type.isPrimitive() -> "_${type.toCType()}Array_free_forced($content)"
            type.isEnum() -> "_KIntArray_free_forced($content)"
            else -> "_KArray_free_forced($content, (void*) ${forceFreeFuncFor(type, "")!!.dropLast(2)})"
        }
    }
    type.isCallback() -> "__AbstractCallback_free_forced((_AbstractCallback*) $content)"
    type.isDictionary() || type.isString() -> "_${type.toCType(ptr = false)}_free_forced($content)"
    else -> null
}