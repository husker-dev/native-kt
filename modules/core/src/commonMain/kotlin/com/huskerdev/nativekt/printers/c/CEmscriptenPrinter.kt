package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*
import io.github.vinceglb.filekit.*

class CEmscriptenPrinter(
    private val context: NativeModuleContext,
    target: PlatformFile
) {
    init {
        target.parent()!!.createDirectories()
        target.writeSync(buildString {
            printHeader()
            printBoxedPrimitives()
            printString()
            printPrimitiveArrays()
            printTypedArrays()
            printDictionaries()
            printCallbacks()
            printOperations()
        })
    }

    private fun StringBuilder.printHeader() {
        appendLine("""
            #include "api.h"
            #include <emscripten.h>
            
        """.trimIndent())
        if(context.needsAllocFunctions) appendLine("""
            
            EMSCRIPTEN_KEEPALIVE void* ${context.jsMangle["alloc"]}(const size_t size) {
                return malloc(size);
            }
            
            EMSCRIPTEN_KEEPALIVE void ${context.jsMangle["dealloc"]}(void* ptr, const size_t size) {
                free(ptr);
            }
        """.trimIndent())
    }

    private fun StringBuilder.printBoxedPrimitives() {
        if(!context.hasPrimitiveNullable && !context.hasEnumNullable)
            return
        appendLine("\n// Boxed primitives")

        listOf(
            Triple("uint16_t" to "char", context.hasCharNullableToNative, context.hasCharNullableToKotlin),
            Triple("bool" to "boolean", context.hasBooleanNullableToNative, context.hasBooleanNullableToKotlin),
            Triple("int8_t" to "byte",
                context.hasByteNullableToNative || context.hasUByteNullableToNative,
                context.hasByteNullableToKotlin || context.hasUByteNullableToKotlin),
            Triple("int16_t" to "short",
                context.hasShortNullableToNative || context.hasUShortNullableToNative,
                context.hasShortNullableToKotlin || context.hasUShortNullableToKotlin),
            Triple("int32_t" to "int",
                context.hasIntNullableToNative || context.hasUIntNullableToNative || context.hasEnumNullableToNative,
                context.hasIntNullableToKotlin || context.hasUIntNullableToKotlin || context.hasEnumNullableToKotlin),
            Triple("int64_t" to "long",
                context.hasLongNullableToNative || context.hasULongNullableToNative,
                context.hasLongNullableToKotlin || context.hasULongNullableToKotlin),
            Triple("float" to "float", context.hasFloatNullableToNative, context.hasFloatNullableToKotlin),
            Triple("double" to "double", context.hasDoubleNullableToNative, context.hasDoubleNullableToKotlin)
        ).forEach { (names, hasToNativeCast, hasToKotlinCast) ->
            if(!hasToNativeCast && !hasToKotlinCast) return@forEach
            val (type, name) = names

            if(hasToNativeCast) appendLine("""
                
                EMSCRIPTEN_KEEPALIVE void* ${context.jsMangle["${name}_new"]}($type value) {
                    return ${context.mangle("${name}_new")}(value);
                }
            """.trimIndent())
            if(hasToKotlinCast) appendLine("""
                
                EMSCRIPTEN_KEEPALIVE $type ${context.jsMangle["${name}_get"]}(void* _Nullable self, bool free) {
                    return ${context.mangle("${name}_get")}(self, free);
                }
            """.trimIndent())
        }
    }

    private fun StringBuilder.printString() {
        if(!context.hasString)
            return
        appendLine("\n// String")

        if(context.hasStringToNative) appendLine("""
            
            EMSCRIPTEN_KEEPALIVE void* ${context.jsMangle["string_new"]}(const char* _Nullable data, const int32_t size, const bool make_copy) {
                return ${context.mangle("string_new")}(data, size, make_copy);
            }
        """.trimIndent())
        if(context.hasStringToKotlin) appendLine("""
            
            EMSCRIPTEN_KEEPALIVE const char* ${context.jsMangle["string_data"]}(const void* _Nullable self) {
                return ${context.mangle("string_data")}(self);
            }
            EMSCRIPTEN_KEEPALIVE size_t ${context.jsMangle["string_size"]}(const void* _Nullable self) {
                return ${context.mangle("string_size")}(self);
            }
            EMSCRIPTEN_KEEPALIVE void ${context.jsMangle["string_free"]}(void* _Nullable self) {
                ${context.mangle("string_free")}(self);
            }
        """.trimIndent())
    }

    private fun StringBuilder.printPrimitiveArrays() {
        if(!context.hasPrimitiveArray)
            return
        listOf(
            Triple("Char" to "uint16_t", context.hasCharArrayToNative, context.hasCharArrayToKotlin),
            Triple("Boolean" to "bool", context.hasBooleanArrayToNative, context.hasBooleanArrayToKotlin),
            Triple("Byte" to "int8_t",
                context.hasByteArrayToNative || context.hasUByteArrayToNative,
                context.hasByteArrayToKotlin || context.hasUByteArrayToKotlin),
            Triple("Short" to "int16_t",
                context.hasShortArrayToNative || context.hasUShortArrayToNative,
                context.hasShortArrayToKotlin || context.hasUShortArrayToKotlin),
            Triple("Int" to "int32_t",
                context.hasIntArrayToNative || context.hasUIntArrayToNative || context.hasEnumArrayToNative,
                context.hasIntArrayToKotlin || context.hasUIntArrayToKotlin || context.hasEnumArrayToKotlin),
            Triple("Long" to "int64_t",
                context.hasLongArrayToNative || context.hasULongArrayToNative,
                context.hasLongArrayToKotlin || context.hasULongArrayToKotlin),
            Triple("Float" to "float", context.hasFloatArrayToNative, context.hasFloatArrayToKotlin),
            Triple("Double" to "double", context.hasDoubleArrayToNative, context.hasDoubleArrayToKotlin),
        ).forEach { (names, hasToNativeCast, hasToKotlinCast) ->
            if(!hasToNativeCast && !hasToKotlinCast)
                return@forEach
            val name = "${names.first.lowercase()}array"
            val type = names.second

            appendLine("\n// Array: $name")
            if(hasToNativeCast) appendLine("""
                
                EMSCRIPTEN_KEEPALIVE void* ${context.jsMangle["${name}_new"]}(const $type* _Nullable elements, const int32_t length, const bool make_copy) {
                    return ${context.mangle("${name}_new")}(elements, length, make_copy);
                }
            """.trimIndent())
            if(hasToKotlinCast) appendLine("""
                
                EMSCRIPTEN_KEEPALIVE const $type* ${context.jsMangle["${name}_elements"]}(const void* _Nullable self) {
                    return ${context.mangle("${name}_elements")}(self);
                }
                EMSCRIPTEN_KEEPALIVE int32_t ${context.jsMangle["${name}_length"]}(const void* _Nullable self) {
                    return ${context.mangle("${name}_length")}(self);
                }
                EMSCRIPTEN_KEEPALIVE void ${context.jsMangle["${name}_free"]}(void* _Nullable self) {
                    ${context.mangle("${name}_free")}(self);
                }
            """.trimIndent())
        }
    }

    private fun StringBuilder.printTypedArrays() {
        if(!context.hasObjectArrays && !context.hasPrimitiveNullableArray && !context.hasEnumNullableArray)
            return

        append("""
            
            #define OBJECT_ARRAY(FUNC_NEW, FUNC_LENGTH, FUNC_PUSH, FUNC_GET, FUNC_FREE,  \
                JS_FUNC_NEW, JS_FUNC_LENGTH, JS_FUNC_PUSH, JS_FUNC_GET, JS_FUNC_FREE)    \
            EMSCRIPTEN_KEEPALIVE void* _Nullable JS_FUNC_NEW(int32_t capacity, bool ne) {             \
                return FUNC_NEW(capacity, ne);                                                     \
            }                                                                                      \
            EMSCRIPTEN_KEEPALIVE int32_t JS_FUNC_LENGTH(void* _Nullable self, bool ne) {              \
                return FUNC_LENGTH(self, ne);                                                      \
            }                                                                                      \
            EMSCRIPTEN_KEEPALIVE void JS_FUNC_PUSH(void* _Nullable self, void* element, bool ne) { \
                FUNC_PUSH(self, element, ne);                                                      \
            }                                                                                      \
            EMSCRIPTEN_KEEPALIVE void* JS_FUNC_GET(void* _Nullable self, int32_t index, bool ne) {    \
                return FUNC_GET(self, index, ne);                                                  \
            }                                                                                      \
            EMSCRIPTEN_KEEPALIVE void JS_FUNC_FREE(void* _Nullable self, bool ne) {                \
                FUNC_FREE(self, ne);                                                               \
            }
            
        """.trimIndent())

        buildList {
            (context.castedDictionaries + context.castedInterfaces)
                .filter { it in context.usedObjectArray }
                .mapTo(this) { it.name.camelCase().lowercase() }
            if(context.hasStringArray) add("string")
            if(context.hasCharNullableArray) add("char")
            if(context.hasBooleanNullableArray) add("boolean")
            if(context.hasByteNullableArray || context.hasUByteNullableArray) add("byte")
            if(context.hasShortNullableArray || context.hasUShortNullableArray) add("short")
            if(context.hasIntNullableArray || context.hasUIntNullableArray || context.hasEnumNullableArray) add("int")
            if(context.hasLongNullableArray || context.hasULongNullableArray) add("long")
            if(context.hasFloatNullableArray) add("float")
            if(context.hasDoubleNullableArray) add("double")
        }.forEach {
            append("""
                
                OBJECT_ARRAY(
                    ${context.mangle("array_${it}_new")},
                    ${context.mangle("array_${it}_length")},
                    ${context.mangle("array_${it}_push")},
                    ${context.mangle("array_${it}_get")},
                    ${context.mangle("array_${it}_free")},
                    ${context.jsMangle["array_${it}_new"]}, ${context.jsMangle["array_${it}_length"]}, ${context.jsMangle["array_${it}_push"]}, ${context.jsMangle["array_${it}_get"]}, ${context.jsMangle["array_${it}_free"]}
                )
            """.trimIndent())
        }
        append("\n")
    }

    private fun StringBuilder.printDictionaries() {
        context.castedDictionaries.forEach { dictionary ->
            val name = dictionary.name.camelCase().lowercase()
            val fields = context.allFields[dictionary]!!
            val args = fields.joinToString {
                "${it.type.toLangType()} ${it.cname}"
            }

            appendLine("\n// ${dictionary.cname}")
            if(dictionary in context.toNativeDeclaration) appendLine("""
                
                EMSCRIPTEN_KEEPALIVE void* _Nullable ${context.jsMangle["${name}_new"]}($args) {
                    return ${context.mangle("${name}_new")}(${fields.joinToString { it.cname }});
                }
            """.trimIndent())
            if(dictionary in context.toKotlinDeclaration) {
                appendLine("""
                    
                    EMSCRIPTEN_KEEPALIVE void ${context.jsMangle["${name}_free"]}(void* _Nullable self) {
                        ${context.mangle("${name}_free")}(self);
                    }
                """.trimIndent())
                fields.forEach {
                    val fieldName = it.name.camelCase().lowercase()
                    val type = it.type.toLangType()
                    appendLine("""
                        
                        EMSCRIPTEN_KEEPALIVE $type ${context.jsMangle["${name}__$fieldName"]}(void* _Nullable self) {
                            return ${context.mangle("${name}__$fieldName")}(self);
                        }
                    """.trimIndent())
                }
            }
        }
    }

    private fun StringBuilder.printCallbacks() {
        context.castedCallbacks.forEach { callback ->
            val name = callback.name.camelCase().lowercase()

            appendLine("\n// ${callback.cname}")
            if(callback in context.toNativeDeclaration) append("""
                
                EMSCRIPTEN_KEEPALIVE void* _Nullable ${context.jsMangle["${name}_new"]}(const size_t id, const int32_t hash_code, void* _Nullable invoke, void* _Nullable equals, void* _Nullable free) {
                	return ${context.mangle("${name}_new")}(id, hash_code, invoke, equals, free);
                }
            """.trimIndent())
            if(callback in context.toKotlinDeclaration) append("""
                
                EMSCRIPTEN_KEEPALIVE size_t ${context.jsMangle["${name}_id"]}(void* _Nullable _self) {
                    return ${context.mangle("${name}_id")}(_self);
                }
                EMSCRIPTEN_KEEPALIVE void ${context.jsMangle["${name}_free"]}(void* _Nullable _self) {
                    ${context.mangle("${name}_free")}(_self);
                }
            """.trimIndent())
            append("\n")
        }
    }

    private fun StringBuilder.printOperations() {
        if(context.allOperations.isEmpty())
            return
        printLabel("Operations")

        context.allOperations.forEach { operation ->
            val critical = operation.isCritical()
            val type = if(operation.isInterfaceOperationAddress())
                "size_t"
            else operation.type.toLangType()
            val jsName = context.jsMangle[operation.cname]
            val cname = operation.cnameMangled(context)

            val args = operation.args.joinToString {
                val name = it.cname
                when {
                    critical && it.type.isString() ->
                        "const char* _Nullable $name, int32_t _${name}_size"
                    critical && it.type.isArray() ->
                        "const ${it.type.arrayTypeOrNull()!!.toLangType()}* _Nullable $name, int32_t _${name}_length"
                    else -> "${it.type.toLangType()} $name"
                }
            }
            val argNames = operation.args.joinToString {
                val name = it.cname
                when {
                    critical && it.type.isString() ->
                        "$name, _${name}_size"
                    critical && it.type.isArray() ->
                        "$name, _${name}_length"
                    else -> name
                }
            }
            val returns = if(operation.type.isVoid())
                "" else "return "

            append("""
                
                EMSCRIPTEN_KEEPALIVE $type $jsName($args) {
                    $returns$cname($argNames);
                }
            """.trimIndent())
        }
        append("\n")
    }

    private fun ResolvedIdlType.toLangType(): String = when (context.language) {
        Language.C -> toCType(printNullable = true)
        else -> toCommonNativeType(printNullable = true)
    }
}