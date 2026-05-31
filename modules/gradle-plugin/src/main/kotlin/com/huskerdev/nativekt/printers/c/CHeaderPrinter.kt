package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.utils.allFields
import com.huskerdev.nativekt.utils.firstParam
import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.isArray
import com.huskerdev.nativekt.utils.isCallback
import com.huskerdev.nativekt.utils.isDictionary
import com.huskerdev.nativekt.utils.isPrimitive
import com.huskerdev.nativekt.utils.isString
import com.huskerdev.nativekt.utils.printLabel
import com.huskerdev.nativekt.utils.simpleName
import com.huskerdev.nativekt.utils.toCDefType
import com.huskerdev.webidl.resolver.BuiltinIdlDeclaration
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlCallbackFunction
import com.huskerdev.webidl.resolver.ResolvedIdlDictionary
import com.huskerdev.webidl.resolver.ResolvedIdlEnum
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import java.io.File
import kotlin.math.max

class CHeaderPrinter(
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

        if(idl.dictionaries.isNotEmpty()) {
            printLabel(builder, "Struct functions")

            idl.dictionaries.values.forEach {
                builder.append("\nstatic ${it.name}* ${it.name}_clone(const ${it.name}* of);")
            }
            idl.dictionaries.values.forEach {
                builder.append("\nstatic void ${it.name}_free(${it.name}* of);")
            }
            builder.append("\n")
            idl.dictionaries.values.forEach {
                printStructClone(builder, it)
                printStructFree(builder, it)
            }
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

        buildList {
            add("char __flags;")
            dictionary.allFields().mapTo(this) { field ->
                "${field.type.toCDefType()} ${field.name};"
            }
        }.joinTo(builder, separator = "\n\t")

        append("\n};\n")
    }

    private fun printStructNew(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        append("\nstatic ${dictionary.name}* ${dictionary.name}_new(")

        dictionary.allFields().joinTo(builder) { field ->
            val const = if(field.type.isPrimitive())
                "const " else ""
            "$const${field.type.toCDefType()} ${field.name}"
        }
        append(") {\n\t")

        // malloc
        append("${dictionary.name}* result = (${dictionary.name}*) malloc(sizeof(${dictionary.name}));\n\t")

        // fill
        append("*result = (${dictionary.name}) { ")
        buildList {
            add("K_FLAG_RELEASABLE")
            dictionary.allFields().mapTo(this) { it.name }
        }.joinTo(builder)
        append(" };\n\t")

        // return
        append("return result;\n}\n")
    }

    private fun printStructClone(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        append("\nstatic ${dictionary.name}* ${dictionary.name}_clone(const ${dictionary.name}* of) {\n\t")

        // malloc
        append("${dictionary.name}* result = (${dictionary.name}*) malloc(sizeof(${dictionary.name}));\n\t")

        // fill
        append("*result = (${dictionary.name}) {\n\t\t")
        buildList {
            add("K_FLAG_RELEASABLE")
            dictionary.allFields().mapTo(this) { field ->
                when {
                    field.type.isString() -> "KString_clone(of->${field.name})"
                    field.type.isArray() -> {
                        (field.type as ResolvedIdlType.Default).firstParam { _, declaration ->
                            when (declaration) {
                                is BuiltinIdlDeclaration -> {
                                    val name = declaration.kind.simpleName()
                                    "K${name}Array_clone(of->${field.name})"
                                }
                                is ResolvedIdlEnum -> "KIntArray_clone(of->${field.name})"
                                is ResolvedIdlDictionary -> "KArray_clone(of->${field.name}, (void*) ${declaration.name}_clone)"
                                else -> throw UnsupportedOperationException(field.type.toString())
                            }
                        }
                    }
                    field.type.isCallback() -> "of->${field.name}->clone(of->${field.name})"
                    field.type.isDictionary() -> "${(field.type as ResolvedIdlType.Default).declaration.name}_clone(of->${field.name})"
                    else -> "of->${field.name}"
                }
            }
        }.joinTo(builder, separator = ",\n\t\t")
        append("\n\t};\n\t")

        // return
        append("return result;\n}\n")
    }

    private fun printStructFree(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        append("\nstatic void ${dictionary.name}_free(${dictionary.name}* of) {\n")
        append("""
            if(!K_OBJECT_IS_RELEASABLE(of->__flags))
                return;
        """.replaceIndent("\t"))

        dictionary.allFields().forEach { field ->
            freeFuncFor(
                field.type,
                "of->${field.name}"
            )?.apply { append("\n\t$this;") }
        }
        append("""
            
            if(!K_OBJECT_IS_ON_STACK(of->__flags))
                free((void*) of);
        """.replaceIndent("\t"))

        append("\n}\n")
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
            #include <string.h>
            
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
            
            #define K_FLAG_RELEASABLE 1
            #define K_FLAG_ON_STACK 2
            
            #define K_OBJECT_IS_RELEASABLE(flags) ((flags) & K_FLAG_RELEASABLE)
            #define K_OBJECT_IS_ON_STACK(flags) ((flags) & K_FLAG_ON_STACK)

            typedef int32_t  KInt;
            typedef int64_t  KLong;
            typedef float    KFloat;
            typedef double   KDouble;
            typedef int8_t   KByte;
            typedef int16_t  KShort;
            typedef bool     KBoolean;
            typedef uint16_t KChar;

            typedef struct KString {
                char __flags;
                const char* data;
                KInt length;
                size_t size;
            } KString;
            
            static KString* KString_new(const char* data, const KInt length, const KInt size) {
                KString* result = (KString*) malloc(sizeof(KString));
                *result = (KString) { K_FLAG_RELEASABLE, data, length, size };
                return result;
            }
            
            static KString* KString_clone(const KString* of) {
                const KInt size = of->size;
                void* data = malloc(size);
                memcpy(data, of->data, size);
                KString* result = (KString*) malloc(sizeof(KString));
                *result = (KString) { K_FLAG_RELEASABLE, (const char*) data, of->length, size };
                return result;
            }
            
            static void KString_free(KString* str) {
                if(!K_OBJECT_IS_RELEASABLE(str->__flags))
                    return;
                free((void*) str->data);
                if(!K_OBJECT_IS_ON_STACK(str->__flags))
                    free((void*) str);
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
            
            #define KArrayDef(Name, Type, VarargType)                          \
            typedef struct Name {                                              \
                char __flags;                                                  \
                const Type* elements;                                          \
                KInt length;				                                   \
                size_t size;				                                   \
            } Name;                                                            \
                                                                               \
            static Name* Name##_new(const Type* elements, const KInt length) { \
                Name* result = (Name*) malloc(sizeof(Name));                   \
                *result = (Name){                                              \
                    K_FLAG_RELEASABLE,                                         \
                    elements,                                                  \
                    length,                                                    \
                    length * sizeof(Name)                                      \
                };                                                             \
                return result;                                                 \
            }                                                                  \
                                                                               \
            static Name* _##Name##_of(const int n, ...) {                      \
                va_list args;                                                  \
                va_start(args, n);                                             \
                Type* elements = (Type*)malloc(n * sizeof(Type));              \
                for (int i = 0; i < n; i++)                                    \
                    elements[i] = (Type)va_arg(args, VarargType);              \
                va_end(args);                                                  \
                Name* result = (Name*) malloc(sizeof(Name));                   \
                *result = (Name){                                              \
                    K_FLAG_RELEASABLE,                                         \
                    (const Type*) elements,                                    \
                    n,                                                         \
                    n * sizeof(Name)                                           \
                };                                                             \
                return result;                                                 \
            }
            
            #define KArrayCloneDef(Name, Type)                                     \
            static Name* Name##_clone(const Name* of) {                            \
                const KInt size = of->size;                                        \
                void** elements = malloc(size);                                    \
                memcpy(elements, (void*) of->elements, size);                      \
                Name* result = (Name*) malloc(sizeof(Name));                       \
                *result = (Name) {                                                 \
                    K_FLAG_RELEASABLE,                                             \
                    (Type*) elements,                                              \
                    of->length,                                                    \
                    of->size                                                       \
                };                                                                 \
                return result;                                                     \
            }                                                                      \
                                                                                   \
            static void Name##_free(Name* arr) {                                   \
                if(!K_OBJECT_IS_RELEASABLE(arr->__flags))                          \
                    return;                                                        \
                free((void*) arr->elements);                                       \
                if(!K_OBJECT_IS_ON_STACK(arr->__flags))                            \
                    free((void*) arr);                                             \
            }
            
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
            #define KFloatArray_of(...)   _KFloatArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KDoubleArray_of(...)  _KDoubleArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            #define KArray_of(...)        _KArray_of(ARG_LENGTH(__VA_ARGS__), __VA_ARGS__)
            
            KArrayCloneDef(KCharArray,    KChar)
            KArrayCloneDef(KBooleanArray, KBoolean)
            KArrayCloneDef(KByteArray,    KByte)
            KArrayCloneDef(KShortArray,   KShort)
            KArrayCloneDef(KIntArray,     KInt)
            KArrayCloneDef(KLongArray,    KLong)
            KArrayCloneDef(KFloatArray,   KFloat)
            KArrayCloneDef(KDoubleArray,  KDouble)
            
            static KArray* KArray_clone(const KArray* of, void* (*cloneOp)(void*)) {
            	const KInt size = of->size;
            	void** elements = malloc(size);
            	for (int i = 0; i < of->length; i++)
            		elements[i] = cloneOp((void*)of->elements[i]);
            	KArray* result = (KArray*) malloc(sizeof(KArray));
            	*result = (KArray) { 
                    K_FLAG_RELEASABLE, 
                    (const void**) elements, 
                    of->length, 
                    of->size
                }; 
            	return result;
            }
            
            static void KArray_free(const KArray* arr, void* (*freeOp)(void*)) {
                if(!K_OBJECT_IS_RELEASABLE(arr->__flags))
                    return;
                const void** elements = arr->elements;
                for (int i = 0; i < arr->length; i++)
                    freeOp((void*) elements[i]);
                free((void*) elements);
                if(!K_OBJECT_IS_ON_STACK(arr->__flags))
                    free((void*) arr);
            }

            #define KCallbackDef(Name, Type, ...)       \
            struct Name {                               \
                char __flags;                           \
                Type (*invoke)(Name* _, ##__VA_ARGS__); \
                Name* (*clone)(Name* _);                \
                KBoolean (*equals)(Name* _, Name* obj); \
                KInt (*hashCode)(Name* _);              \
                void (*free)(Name* _);                  \
            };
            
        """.trimIndent())
    }
}