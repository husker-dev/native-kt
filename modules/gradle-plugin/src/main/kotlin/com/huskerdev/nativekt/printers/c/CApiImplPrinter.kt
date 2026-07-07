package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlDictionary
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import java.io.File

class CApiImplPrinter(
    idl: IdlResolver,
    target: File,
    val classPath: String,
    val moduleName: String,
    val cFunctions: Boolean = true
) {
    init {
        target.parentFile.mkdirs()

        val builder = StringBuilder()
        builder.append("""
            #include "api.h"
            
            #include <stdarg.h>
            #include <string.h>
            
            
            #ifdef __cplusplus
            extern "C" {
            #endif
            
        """.trimIndent())
        printStdLib(builder)

        if(idl.dictionaries.isNotEmpty()) {
            printLabel(builder, "Struct functions")
            idl.dictionaries.values.forEach {
                printStructNew(builder, it)
                printStructClone(builder, it)
                printStructFree(builder, it)
            }
        }

        if(idl.callbacks.isNotEmpty()) {
            printLabel(builder, "Callback free")
            builder.append("""
                
                void ${mangle("_AbstractCallback_free")}(_AbstractCallback* self) {
                    if(self == NULL) return;
                    self->free(self);
                }
                
                void ${mangle("_AbstractCallback_free_forced")}(_AbstractCallback* self) {
                    if(self == NULL) return;
                    self->__flags |= K_FLAG_RELEASABLE;
                    self->free(self);
                }
                
            """.trimIndent())
        }

        if(cFunctions) {
            printLabel(builder, "Functions")
            idl.globalOperators().forEach {
                printFunction(builder, it)
            }
        }

        // Critical wrappers
        idl.globalOperators().forEach {
            if(it.isCriticalCapable() && (it.hasString() || it.hasArray())) {
                builder.append("\n")
                printCriticalNativeFunctionContent(
                    builder, classPath, moduleName,
                    name = "${mangle(it.name)}_",
                    function = it
                )
            }
        }

        target.writeText(builder.toString().replace("\n", System.lineSeparator()))
    }

    private fun mangle(name: String) =
        mangle(classPath, moduleName, name)

    private fun printFunction(builder: StringBuilder, function: ResolvedIdlOperation) = builder.apply {
        if(cFunctions) {
            val name = function.name.snakeCase()
            val type = function.type.toCType(printNullable = true)
            val args = function.args.joinToString {
                "${it.type.toCType(printNullable = true)} ${it.name.snakeCase()}"
            }
            val argNames = function.args.joinToString { it.name.snakeCase() }

            append("""
                
                $type ${mangle(name)}($args) {
                    ${if(function.type.isVoid()) "" else "return "}$name($argNames);
                }
                
            """.trimIndent())
        }
    }

    private fun printStructNew(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val name = dictionary.name.upperCamelCase()
        val fields = dictionary.allFields()
        val args = fields.joinToString { field ->
            val const = if(field.type.isPrimitive())
                "const " else ""
            "$const${field.type.toCType()} ${field.name.snakeCase()}"
        }

        append("""
            
            $name* ${mangle("${name}_new")}($args) {
                $name* result = ($name*) malloc(sizeof($name));
                *result = ($name) { 
        """.trimIndent())
        buildList {
            fields.mapTo(this) { it.name.snakeCase() }
            add("K_FLAG_RELEASABLE")
        }.joinTo(builder)

        append(" };\n\t")
        append("return result;\n}\n")

        if(cFunctions) {
            append("""
                
                $name* ${name}_new($args) {
                    return ${mangle("${name}_new")}(${fields.joinToString { it.name }});
                }
                
            """.trimIndent())
        }
    }

    private fun printStructClone(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val name = dictionary.name.upperCamelCase()
        val fields = dictionary.allFields()

        append("""
            
            $name* ${mangle("${name}_clone")}(const $name* self) {
                if(self == NULL) return NULL;
                return ${mangle("${name}_new")}(
                    
        """.trimIndent())
        fields.joinTo(builder, separator = ",\n\t\t") { field ->
            cloneFuncFor(field.type, "self->${field.name.snakeCase()}")
        }
        append("\n\t);\n}\n")

        if(cFunctions) {
            append("""
                
                $name* ${name}_clone(const $name* self) {
                    return ${mangle("${name}_clone")}(self);
                }
                
            """.trimIndent())
        }
    }

    private fun printStructFree(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val name = dictionary.name.upperCamelCase()
        val fields = dictionary.allFields()

        // free
        append("""
            
            void ${mangle("${name}_free")}($name* self) {
                if (self == NULL)
                    return;
        """.trimIndent())
        fields.forEach { field ->
            freeFuncFor(
                classPath, moduleName,
                field.type,
                "self->${field.name.snakeCase()}"
            )?.apply { append("\n\t$this;") }
        }
        append("""
            
            if(K_OBJECT_IS_RELEASABLE(self->__flags))
                free((void*) self);
        """.replaceIndent("\t"))
        append("\n}\n")

        // forceFree
        append("\nvoid ${mangle("${name}_free_forced")}($name* self) {")
        append("\n\tif(self == NULL) return;")
        fields.forEach { field ->
            forceFreeFuncFor(
                classPath, moduleName,
                field.type,
                "self->${field.name.snakeCase()}"
            )?.apply { append("\n\t$this;") }
        }
        append("""
            
            if(K_OBJECT_IS_RELEASABLE(self->__flags))
                free((void*) self);
        """.replaceIndent("\t"))
        append("\n}\n")

        if(cFunctions) {
            append("""
                
                void ${name}_free($name* self) {
                    ${mangle("${name}_free")}(self);
                }
                
                void ${name}_free_forced($name* self) {
                    ${mangle("${name}_free_forced")}(self);
                }
                
            """.trimIndent())
        }
    }

    private fun printStdLib(builder: StringBuilder) = builder.apply {
        printLabel(builder, "stdlib")

        append("\n// String\n")

        if(cFunctions) {
            append("""
                
                KString* KString_new(const char* data, const KInt length, const size_t size, const bool is_data_owner) {
                    return ${mangle("KString_new")}(data, length, size, is_data_owner);
                }
                
                KString* KString_clone(const KString* of) {
                    return ${mangle("KString_clone")}(of);
                }
                
                void KString_free(KString* self) {
                    ${mangle("KString_free")}(self);
                }
                
            """.trimIndent())
        }

        builder.append("""
            
            KString* ${mangle("KString_new")}(const char* data, const KInt length, const size_t size, const bool is_data_owner) {
                KString* result = (KString*) malloc(sizeof(KString));
                *result = (KString) { data, size, length, K_FLAG_RELEASABLE | (is_data_owner ? K_FLAG_DATA_OWNER : 0) };
                return result;
            }
            
            KString* ${mangle("KString_clone")}(const KString* of) {
                if (of == NULL) return NULL;
                const KInt size = of->size;
                void* data = malloc(size);
                memcpy(data, of->data, size);
                return ${mangle("KString_new")}((const char*) data, size, of->length, true);
            }
            
            void ${mangle("KString_free")}(KString* self) {
                if (self == NULL)
                    return;
                if (K_OBJECT_IS_DATA_OWNER(self->__flags))
                    free((void*) self->data);
                if (K_OBJECT_IS_RELEASABLE(self->__flags))
                    free((void*) self);
            }
            
            void ${mangle("KString_free_forced")}(KString* self) {
                if (self == NULL) return;
                self->__flags |= K_FLAG_RELEASABLE;
                ${mangle("KString_free")}(self);
            }
            
        """.trimIndent())

        listOf(
            Triple("KCharArray", "KChar", "int32_t"),
            Triple("KBooleanArray", "KBoolean", "int32_t"),
            Triple("KByteArray", "KByte", "int32_t"),
            Triple("KUByteArray", "KUByte", "int32_t"),
            Triple("KShortArray", "KShort", "int32_t"),
            Triple("KUShortArray", "KUShort", "int32_t"),
            Triple("KIntArray", "KInt", "int32_t"),
            Triple("KUIntArray", "KUInt", "int32_t"),
            Triple("KLongArray", "KLong", "int64_t"),
            Triple("KULongArray", "KULong", "int64_t"),
            Triple("KFloatArray", "KFloat",  "double"),
            Triple("KDoubleArray", "KDouble", "double"),
            Triple("KArray", "void*", "void*")
        ).forEach {
            val name = it.first
            val type = it.second
            val varargType = it.third

            append("""
                
                // $name
                
                $name* ${mangle("${name}_new")}(
                    const $type* elements,
                    const KInt length,
                    const bool is_data_owner
                ) {
                    $name* result = ($name*) malloc(sizeof($name));
                    *result = ($name){
                        elements,
                        length * sizeof($name),
                        length,
                        K_FLAG_RELEASABLE | (is_data_owner ? K_FLAG_DATA_OWNER : 0)
                    };
                    return result;
                }
                
            """.trimIndent())
            if(name != "KArray") {
                append("""
                    
                    $name* ${mangle("${name}_clone")}(const $name* of) {
                        if(of == NULL) return NULL;
                        const KInt size = of->size;
                        void** elements = malloc(size);
                        memcpy(elements, (void*) of->elements, size);
                        return ${mangle("${name}_new")}(($type*) elements, of->length, true);
                    }
                    
                    void ${mangle("${name}_free")}($name* self) {
                        if (self == NULL)
                            return;
                        if (K_OBJECT_IS_DATA_OWNER(self->__flags))
                            free((void*) self->elements);
                        if (K_OBJECT_IS_RELEASABLE(self->__flags))
                            free((void*) self);
                    }
                    
                    void ${mangle("${name}_free_forced")}($name* self) {
                        if (self == NULL) return;
                        self->__flags |= K_FLAG_RELEASABLE;
                        ${mangle("${name}_free")}(self);
                    }
                    
                """.trimIndent())
            } else {
                append("""
                    KArray* ${mangle("KArray_clone")}(const KArray* _Nullable self, void* _Nullable (* _Nullable clone_op)(void* _Nullable)) {
                        if(self == NULL) return NULL;
                        const KInt size = self->size;
                        void** elements = malloc(size);
                        for (int i = 0; i < self->length; i++) {
                            void* element = (void*) self->elements[i];
                            elements[i] = element == NULL ? NULL : clone_op(element);
                        }
                        return ${mangle("KArray_new")}((const void**) elements, self->length, true);
                    }
        
                    void ${mangle("KArray_free")}(const KArray* _Nullable self, void (* _Nonnull free_op)(void* _Nonnull)) {
                        if (self == NULL)
                            return;
                        if (K_OBJECT_IS_DATA_OWNER(self->__flags)) {
                            const void** elements = self->elements;
                            for (int i = 0; i < self->length; i++) {
                                void* element = (void*) elements[i];
                                if(element == NULL) continue;
                                free_op(element);
                            }
                            free((void*) elements);
                        }
                        if (K_OBJECT_IS_RELEASABLE(self->__flags))
                            free((void*) self);
                    }
                    
                    void ${mangle("KArray_free_forced")}(KArray* self, void (*free_op)(void*)) {
                        if(self == NULL) return;
                        self->__flags |= K_FLAG_RELEASABLE;
                        ${mangle("KArray_free")}(self, free_op);
                    }
                    
                """.trimIndent())
            }
            if(cFunctions) {
                append("""
                    
                    $name* ${name}_new(const $type* elements, const KInt length, const bool is_data_owner) {
                        return ${mangle("${name}_new")}(elements, length, is_data_owner);
                    }
                    
                    $name* ${name}_of_n(const int n, ...) {
                        va_list args;
                        va_start(args, n);
                        $type* elements = ($type*) malloc(n * sizeof($type));
                        for (int i = 0; i < n; i++)
                            elements[i] = ($type) va_arg(args, $varargType);
                        va_end(args);
                        return ${mangle("${name}_new")}((const $type*) elements, (KInt) n, true);
                    }
                    
                """.trimIndent())
                if(name != "KArray") {
                    append("""
                        
                        $name* ${name}_clone(const $name* of) {
                            return ${mangle("${name}_clone")}(of);
                        }
                        
                        void ${name}_free($name* self) {
                            ${mangle("${name}_free")}(self);
                        }
                        
                        void ${name}_free_forced($name* self) {
                            ${mangle("${name}_free_forced")}(self);
                        }
                        
                    """.trimIndent())
                } else {
                    append("""
                        
                        $name* KArray_clone(const KArray* _Nullable self, void* _Nullable (* _Nullable clone_op)(void* _Nullable)) {
                            return ${mangle("KArray_clone")}(self, clone_op);
                        }
                        
                        void KArray_free(const KArray* _Nullable self, void (* _Nonnull free_op)(void* _Nonnull)) {
                            ${mangle("KArray_free")}(self, free_op);
                        }
                        
                        void KArray_free_forced(KArray* self, void (*free_op)(void*)) {
                            ${mangle("KArray_free_forced")}(self, free_op);
                        }
                        
                    """.trimIndent())
                }
            }
        }
    }

    private fun cloneFuncFor(
        type: ResolvedIdlType,
        content: String
    ): String = when {
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() -> "${mangle("${type.toCType(ptr = false)}Array_clone")}($content)"
                type.isEnum() -> "${mangle("KIntArray_clone")}($content)"
                else -> "${mangle("KArray_clone")}($content, (void*) ${cloneFuncFor(type, "").dropLast(2)})"
            }
        }
        type.isCallback() -> "$content->clone($content)"
        type.isDictionary() || type.isString() -> "${mangle("${type.toCType(ptr = false)}_clone")}($content)"
        else -> content
    }
}

internal fun freeFuncFor(
    classPath: String,
    moduleName: String,
    type: ResolvedIdlType,
    content: String
): String? {
    fun mangle(name: String) = mangle(classPath, moduleName, name)
    return when {
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() -> "${mangle("${type.toCType(ptr = false)}Array_free")}($content)"
                type.isEnum() -> "${mangle("KIntArray_free")}($content)"
                else -> "${mangle("KArray_free")}($content, (void*) ${freeFuncFor(classPath, moduleName, type, "")!!.dropLast(2)})"
            }
        }
        type.isCallback() -> "${mangle("_AbstractCallback_free")}((_AbstractCallback*) $content)"
        type.isDictionary() || type.isString() -> "${mangle("${type.toCType(ptr = false)}_free")}($content)"
        else -> null
    }
}

internal fun forceFreeFuncFor(
    classPath: String,
    moduleName: String,
    type: ResolvedIdlType,
    content: String
): String? {
    fun mangle(name: String) = mangle(classPath, moduleName, name)
    return when {
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() -> "${mangle("${type.toCType()}Array_free_forced")}($content)"
                type.isEnum() -> "${mangle("KIntArray_free_forced")}($content)"
                else -> "${mangle("KArray_free_forced")}($content, (void*) ${forceFreeFuncFor(classPath, moduleName, type, "")!!.dropLast(2)})"
            }
        }
        type.isCallback() -> "${mangle("_AbstractCallback_free_forced")}((_AbstractCallback*) $content)"
        type.isDictionary() || type.isString() -> "${mangle("${type.toCType(ptr = false)}_free_forced")}($content)"
        else -> null
    }
}

internal fun printCriticalNativeFunctionContent(
    builder: StringBuilder,
    classPath: String,
    moduleName: String,
    name: String,
    function: ResolvedIdlOperation
) = builder.apply {
    // == Type and name ==
    append(function.type.toCType(enumAsInt = true))
    append(" ")
    append(name)

    // == Function args ==
    function.args.flatMap {
        val name = it.name.snakeCase()
        when {
            it.type.isString() -> listOf("const char* _arr_$name", "KInt _length_$name, KLong _size_$name")
            it.type.isArray() -> {
                val type = (it.type as ResolvedIdlType.Default).arrayType { type -> type.toCType(enumAsInt = true) }
                listOf("$type* _arr_$name", "KInt _length_$name")
            }
            else -> listOf("${it.type.toCType(enumAsInt = true)} _arg_$name")
        }
    }.joinTo(this, prefix = "(", postfix = ") {")

    // == Casts ==
    function.args.forEach {
        val name = it.name.snakeCase()
        when {
            it.type.isString() -> append("\n\tKString _arg_$name = (KString) { _arr_$name, _size_$name, _length_$name, 0 };")
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

    val call = "${mangle(classPath, moduleName, function.name)}($args)"
    append(call)
    append(";\n}\n")
}