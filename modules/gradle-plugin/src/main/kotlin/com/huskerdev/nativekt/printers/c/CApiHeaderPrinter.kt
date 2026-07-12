package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.plugin.Language
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
import java.io.File
import kotlin.math.max

class CApiHeaderPrinter(
    val idl: IdlResolver,
    target: File,
    val language: Language?,
    val classPath: String,
    val moduleName: String,
    val isInternal: Boolean = false
) {
    init {
        target.parentFile.mkdirs()

        val builder = StringBuilder()
        printHeader(builder)

        printLabel(builder, "Types")
        printStdLib(builder)

        if(idl.callbacks.isNotEmpty() || idl.dictionaries.isNotEmpty() || idl.interfaces.isNotEmpty()) {
            builder.append("\n")
            printLabel(builder, "Type definitions")
            printTypeDefs(builder)
        }

        if(idl.enums.isNotEmpty()) {
            builder.append("\n")
            printLabel(builder, "Enums")
            idl.enums.values.forEach { printEnum(builder, it) }
        }

        if(idl.interfaces.isNotEmpty()) {
            builder.append("\n")
            printLabel(builder, "Interfaces")
            idl.interfaces.values.forEach { printInterface(builder, it) }
        }

        if(idl.dictionaries.isNotEmpty()) {
            builder.append("\n")
            printLabel(builder, "Structs")
            idl.dictionaries.values.forEach { printStruct(builder, it) }
        }

        if(idl.allOperators().isNotEmpty()) {
            builder.append("\n")
            printLabel(builder, "Functions")
            idl.allOperators().forEach {
                printFunction(builder, it)

                // Critical wrappers
                if (isInternal && it.isCriticalCapable() && (it.hasString() || it.hasArray()))
                    printCriticalFunction(builder, it)
            }
        }

        if(idl.callbacks.isNotEmpty()) {
            builder.append("\n")
            printLabel(builder, "Callbacks")
            printCallbacks(builder, idl.callbacks.values)
        }

        target.writeText(builder.toString().replace("\n", System.lineSeparator()))
    }

    private fun mangle(name: String) =
        mangle(classPath, moduleName, "_$name")

    private fun printTypeDefs(builder: StringBuilder) = builder.apply {
        buildList {
            idl.dictionaries.values.mapTo(this) { it.cname }
            idl.callbacks.values.mapTo(this) { it.cname }
            if(isInternal)
                add("_AbstractCallback")
            if(language == Language.CPP)
                idl.interfaces.values.mapTo(this) { "I${it.cname}" }
        }.joinTo(builder, separator = "") {
            when(language) {
                Language.CPP -> "\nstruct $it;"
                else -> "\ntypedef struct $it $it;"
            }
        }
        builder.append("\n")
    }

    private fun printInterface(builder: StringBuilder, inter: ResolvedIdlInterface) = builder.apply {
        when (language) {
            Language.CPP -> {
                val name = "I${inter.cname}"

                append("\nstruct $name {")
                inter.toOperations().forEach { operation ->
                    val args = operation.args.map {
                        "${it.type.toLangType()} ${it.cname}"
                    }
                    val type = operation.type.toLangType()

                    append("\n\t")
                    append(when {
                        operation.isInterfaceOperationConstructor() ->
                            "static $name* _Nonnull _create(${args.joinToString()});"
                        operation.isInterfaceOperationFn() -> {
                            val funcName = operation.interfaceFunctionName().snakeCase()
                            val args = args.drop(1).joinToString()
                            "virtual $type $funcName($args) = 0;"
                        }
                        operation.isInterfaceOperationFree() ->
                            "virtual ~$name() = default;"
                        else -> throw UnsupportedOperationException()
                    })
                }
                append("\n};\n")
            }
            Language.C -> Unit
            else -> Unit
        }
    }

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        val name = function.cname
        val mangledName = function.cnameMangled(classPath, moduleName)
        val type = function.type.toLangType()
        val args = function.args.joinToString {
            "${it.type.toLangType()} ${it.cname}"
        }

        if(language == Language.C || language == Language.CPP) {
            if(language == Language.C || !function.isInterfaceOperation())
                append("\n$type $name($args);")
        }
        if (isInternal)
            append("\nEXTERN_C DLL_EXPORT $type $mangledName($args);")
    }

    private fun printCriticalFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        val name = function.cnameMangled(classPath, moduleName)
        val type = function.type.toLangType()
        val args = function.args.flatMap {
            val name = it.cname
            val type = it.type
            when {
                type.isString() ->
                    listOf("const char* _Nonnull _arr_$name", "KInt _length_$name, KLong _size_$name")
                type.isArray() -> {
                    val type = type.arrayTypeOrNull()!!.toCType(enumAsInt = true)
                    listOf("$type* _Nonnull _arr_$name", "KInt _length_$name")
                }
                else -> listOf("${type.toCType(enumAsInt = true)} _arg_$name")
            }
        }.joinToString()

        append("\nEXTERN_C DLL_EXPORT $type c_$name($args);")
    }

    private fun printEnum(builder: StringBuilder, enum: ResolvedIdlEnum) = builder.apply {
        append("\n")
        when (language) {
            Language.CPP -> {
                append("enum ${enum.cname} {\n\t")
                enum.elements.joinTo(builder, separator = ",\n\t")
                append("\n};\n")
            }
            else -> {
                append("typedef enum {\n\t")
                enum.elements.joinTo(builder, separator = ",\n\t") {
                    "${enum.cname}_${it}"
                }
                append("\n} ${enum.cname};\n")
            }
        }
    }

    private fun printStruct(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val name = dictionary.cname
        val fields = dictionary.allFields()
        val args = fields.map { field ->
            "${field.type.toLangType()} ${field.cname}"
        }

        when (language) {
            Language.CPP -> {

                append("\nstruct $name {")
                if(dictionary.implements != null)
                    append(" // : ").append(dictionary.implements!!.cname)
                append("\n\t")

                // Fields
                buildList {
                    args.mapTo(this) { "$it;" }
                    add("char __flags;")
                }.joinTo(builder, separator = "\n\t")

                // Methods
                append("\n\n")
                append("""
                        $name(${args.joinToString()});
                        $name* _Nullable clone() const;
                        void destroy();
                    };
                    
                """.trimIndent())
            }
            else -> {
                append("\nstruct $name {")
                if(dictionary.implements != null)
                    append(" // : ").append(dictionary.implements!!.cname)
                append("\n\t")

                // Fields
                buildList {
                    args.mapTo(this) { "$it;" }
                    add("char __flags;")
                }.joinTo(builder, separator = "\n\t")
                append("\n};\n")

                // Methods
                if(language == Language.C) append("""
                    
                    $name* _Nonnull ${name}_new(${args.joinToString()});
                    $name* _Nullable ${name}_clone(const $name* _Nullable self);
                    void ${name}_free($name* _Nullable self);
                    
                """.trimIndent())
            }
        }

        if(isInternal) {
            val funcNew = dictionary.subCFunc(classPath, moduleName, "new")
            val funcClone = dictionary.subCFunc(classPath, moduleName, "clone")
            val funcFree = dictionary.subCFunc(classPath, moduleName, "free")
            val funcFreeForced = dictionary.subCFunc(classPath, moduleName, "free_forced")
            append("""
                
                EXTERN_C DLL_EXPORT $name* _Nonnull $funcNew(${args.joinToString()});
                EXTERN_C DLL_EXPORT $name* _Nullable $funcClone(const $name* _Nullable self);
                EXTERN_C DLL_EXPORT void $funcFree($name* _Nullable self);
                EXTERN_C DLL_EXPORT void $funcFreeForced($name* _Nullable self);
                
            """.trimIndent())
        }
    }

    private fun printCallbacks(builder: StringBuilder, callbacks: Collection<ResolvedIdlCallbackFunction>) = builder.apply {
        val column1 = "Name"
        val column2 = "Type"
        val column3 = "Args"

        val names = arrayListOf<String>()
        val types = arrayListOf<String>()
        val args = arrayListOf<String>()

        callbacks.forEach { callback ->
            names += callback.cname + ","
            types += callback.type.toLangType() + if(callback.args.isNotEmpty()) "," else ""
            args += callback.args.joinToString { "${it.type.toLangType()} ${it.cname}" }
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
            append("\nEXTERN_C DLL_EXPORT void ${mangle("abstract_callback_free")}(_AbstractCallback* _Nullable self);")
            append("\nEXTERN_C DLL_EXPORT void ${mangle("abstract_callback_free_forced")}(_AbstractCallback* _Nullable self);")
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
            
            #pragma once

            
        """.trimIndent())

        builder.append(when (language) {
            Language.CPP -> """
                #include <initializer_list>
                #include <cstdint>
                #include <memory>
            """.trimIndent()
            else -> """
                #include <stdlib.h>
                #include <stdint.h>
                #include <stdbool.h>
                
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
            """.trimIndent()
        })
        builder.append("\n\n")

        if(isInternal) {
            builder.append(when (language) {
                Language.CPP -> """
                    #include <cstdlib>
                    #include <cstring>
                    
                    #define EXTERN_C extern "C"
                    
                """.trimIndent()
                else -> """
                    #include <string.h>
                    #include <stdarg.h>
                    
                    #define EXTERN_C
                """.trimIndent()
            })
            builder.append("""
                
                
                #ifndef DLL_EXPORT
                    #if defined(_WIN32) || defined(__CYGWIN__)
                        #define DLL_EXPORT __declspec(dllexport)
                    #else
                        #define DLL_EXPORT __attribute__((visibility("default")))
                    #endif
                #endif
                
                #define K_FLAG_RELEASABLE 1
                #define K_FLAG_DATA_OWNER 2
                
                #define K_OBJECT_IS_RELEASABLE(flags) ((flags) & K_FLAG_RELEASABLE)
                #define K_OBJECT_IS_DATA_OWNER(flags) ((flags) & K_FLAG_DATA_OWNER)
                
                EXTERN_C DLL_EXPORT void ${mangle("init")}();
                
            """.trimIndent())
        }
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
            
            
        """.trimIndent())

        // String

        when (language) {
            Language.CPP -> append("""
                struct KString {
                    const char* _Nonnull data;
                    size_t size;
                    KInt length;
                    char __flags;
                    
                    KString(const char* _Nonnull data, KInt length, size_t size, bool is_data_owner);
                	KString* _Nonnull clone() const;
                	void destroy();
                };
                
            """.trimIndent())
            else -> {
                append("""
                    typedef struct KString {
                        const char* _Nonnull data;
                        size_t size;
                        KInt length;
                        char __flags;
                    } KString;
                    
                """.trimIndent())
                if(language == Language.C) append("""
                    
                    KString* _Nonnull KString_new(const char* _Nonnull data, KInt length, size_t size, bool is_data_owner);
                    KString* _Nullable KString_clone(const KString* _Nullable self);
                    void KString_free(KString* _Nullable self);
                    
                """.trimIndent())
            }
        }
        if(isInternal) {
            append("""
                
                EXTERN_C DLL_EXPORT KString* _Nonnull ${mangle("kstring_new")}(const char* _Nonnull data, KInt length, size_t size, bool is_data_owner);
                EXTERN_C DLL_EXPORT KString* _Nullable ${mangle("kstring_clone")}(const KString* _Nullable self);
                EXTERN_C DLL_EXPORT void ${mangle("kstring_free")}(KString* _Nullable self);
                
            """.trimIndent())
        }
        append("\n")

        // Primitive arrays

        mapOf(
            "KCharArray" to "KChar",
            "KBooleanArray" to "KBoolean",
            "KByteArray" to	"KByte",
            "KUByteArray" to "KUByte",
            "KShortArray" to "KShort",
            "KUShortArray" to "KUShort",
            "KIntArray" to "KInt",
            "KUIntArray" to "KUInt",
            "KLongArray" to "KLong",
            "KULongArray" to "KULong",
            "KFloatArray" to "KFloat",
            "KDoubleArray" to "KDouble"
        ).forEach {
            val name = it.key
            val type = it.value
            val lowerName = name.snakeCase()

            when (language) {
                Language.CPP -> append("""
                    struct $name {
                        const $type* _Nonnull elements;
                        size_t size;
                        KInt length;
                        char __flags;
                        
                        $name(const $type* _Nonnull elements, KInt length, bool is_data_owner);
                        static $name* _Nonnull of(std::initializer_list<$type> elements);
                        $name* _Nonnull clone() const;
                        void destroy();
                    };
                    
                """.trimIndent())
                else -> {
                    append("""
                        typedef struct $name {
                            const $type* _Nonnull elements;
                            size_t size;
                            KInt length;
                            char __flags;
                        } $name;
                        
                    """.trimIndent())
                    if(language == Language.C) append("""
                        
                        $name* _Nonnull ${name}_new(const $type* _Nonnull elements, KInt length, bool is_data_owner);
                        $name* _Nonnull ${name}_of_n(int n, ...);
                        #define ${name}_of(...) ${name}_of_n(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
                        $name* _Nullable ${name}_clone(const $name* _Nullable self);
                        void ${name}_free($name* _Nullable self);
                        
                    """.trimIndent())
                }
            }
            if(isInternal) append("""
                
                EXTERN_C DLL_EXPORT $name* _Nonnull ${mangle("${lowerName}_new")}(const $type* _Nonnull elements, KInt length, bool is_data_owner);
                EXTERN_C DLL_EXPORT $name* _Nonnull ${mangle("${lowerName}_of_n")}(int n, ...);
                EXTERN_C DLL_EXPORT $name* _Nullable ${mangle("${lowerName}_clone")}(const $name* _Nullable self);
                EXTERN_C DLL_EXPORT void ${mangle("${lowerName}_free")}($name* _Nullable self);
                EXTERN_C DLL_EXPORT void ${mangle("${lowerName}_free_forced")}($name* _Nullable self);

            """.trimIndent())

            append("\n")
        }

        // Object array

        when (language) {
            Language.CPP -> append("""
                struct KArray {
                    const void* _Nullable * _Nonnull elements;
                    size_t size;
                    KInt length;
                    char __flags;
                    
                    KArray(const void* _Nullable * _Nonnull elements, KInt length, bool is_data_owner);
                    static KArray* _Nonnull of(std::initializer_list<void* _Nullable> elements);
                    template <typename T> KArray* _Nullable clone() const;
                    template <typename T> void destroy();
                };
                
            """.trimIndent())
            else -> {
                append("""
                    typedef struct KArray {
                        const void* _Nullable* _Nonnull elements;
                        size_t size;
                        KInt length;
                        char __flags;
                    } KArray;
                    
                """.trimIndent())
                if(language == Language.C) append("""
                    
                    KArray* _Nonnull KArray_new(const void* _Nullable * _Nonnull elements, KInt length, bool is_data_owner);
                    KArray* _Nonnull KArray_of_n(int n, ...);
                    #define KArray_of(...) KArray_of_n(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
                    KArray* _Nullable KArray_clone(const KArray* _Nullable self, void* _Nullable (* _Nullable clone_op)(void* _Nullable));
                    void KArray_free(KArray* _Nullable self, void (* _Nonnull free_op)(void* _Nonnull));
                
                """.trimIndent())
            }
        }
        if(isInternal) append("""
            
            EXTERN_C DLL_EXPORT KArray* _Nonnull ${mangle("karray_new")}(const void* _Nullable * _Nonnull elements, KInt length, bool is_data_owner);
            EXTERN_C DLL_EXPORT KArray* _Nonnull ${mangle("karray_of_n")}(int n, ...);
            EXTERN_C DLL_EXPORT KArray* _Nullable ${mangle("karray_clone")}(const KArray* _Nullable self, void* _Nullable (* _Nullable clone_op)(void* _Nullable));
            EXTERN_C DLL_EXPORT void ${mangle("karray_free")}(KArray* _Nullable self, void (* _Nonnull free_op)(void* _Nonnull));
            EXTERN_C DLL_EXPORT void ${mangle("karray_free_forced")}(KArray* _Nullable self, void (* _Nonnull free_op)(void* _Nullable));
        
        """.trimIndent())

        append("""

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
    }

    private fun ResolvedIdlType.toLangType() =
        toCType(printNullable = true)
}

