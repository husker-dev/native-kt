package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.utils.allFields
import com.huskerdev.nativekt.utils.isPrimitive
import com.huskerdev.nativekt.utils.printLabel
import com.huskerdev.nativekt.utils.toCType
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlDictionary
import java.io.File

class CApiImplPrinter(
    idl: IdlResolver,
    target: File
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
                
                void _AbstractCallback_free(_AbstractCallback* self) {
                    if(self == NULL) return;
                    self->free(self);
                }
                
                void __AbstractCallback_free_forced(_AbstractCallback* self) {
                    if(self == NULL) return;
                    self->__flags |= K_FLAG_RELEASABLE;
                    self->free(self);
                }
                
            """.trimIndent())
        }

        target.writeText(builder.toString().replace("\n", System.lineSeparator()))
    }

    private fun printStructNew(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        append("\n${dictionary.name}* ${dictionary.name}_new(")

        dictionary.allFields().joinTo(builder) { field ->
            val const = if(field.type.isPrimitive())
                "const " else ""
            "$const${field.type.toCType()} ${field.name}"
        }
        append(") {\n\t")

        // malloc
        append("${dictionary.name}* result = (${dictionary.name}*) malloc(sizeof(${dictionary.name}));\n\t")

        // fill
        append("*result = (${dictionary.name}) { ")
        buildList {
            dictionary.allFields().mapTo(this) { it.name }
            add("K_FLAG_RELEASABLE")
        }.joinTo(builder)
        append(" };\n\t")

        // return
        append("return result;\n}\n")
    }

    private fun printStructClone(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        append("\n${dictionary.name}* ${dictionary.name}_clone(const ${dictionary.name}* self) {\n\t")
        append("if(self == NULL) return NULL;\n\t")

        // new
        append("return ${dictionary.name}_new(\n\t\t")
        dictionary.allFields().joinTo(builder, separator = ",\n\t\t") { field ->
            cloneFuncFor(field.type, "self->${field.name}")
        }
        append("\n\t);\n}\n")
    }

    private fun printStructFree(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        // free
        append("""
            
            void ${dictionary.name}_free(${dictionary.name}* self) {
                if (self == NULL)
                    return;
        """.trimIndent())
        dictionary.allFields().forEach { field ->
            freeFuncFor(
                field.type,
                "self->${field.name}"
            )?.apply { append("\n\t$this;") }
        }
        append("""
            
            if(K_OBJECT_IS_RELEASABLE(self->__flags))
                free((void*) self);
        """.replaceIndent("\t"))
        append("\n}\n")

        // forceFree
        append("\nvoid _${dictionary.name}_free_forced(${dictionary.name}* self) {")
        append("\n\tif(self == NULL) return;")
        dictionary.allFields().forEach { field ->
            forceFreeFuncFor(
                field.type,
                "self->${field.name}"
            )?.apply { append("\n\t$this;") }
        }
        append("""
            
            if(K_OBJECT_IS_RELEASABLE(self->__flags))
                free((void*) self);
        """.replaceIndent("\t"))
        append("\n}\n")
    }

    private fun printStdLib(builder: StringBuilder) = builder.apply {
        printLabel(builder, "stdlib")

        builder.append("""
            
            // String
            
            KString* KString_new(const char* data, const KInt length, const size_t size, const bool is_data_owner) {
                KString* result = (KString*) malloc(sizeof(KString));
                *result = (KString) { data, size, length, K_FLAG_RELEASABLE | (is_data_owner ? K_FLAG_DATA_OWNER : 0) };
                return result;
            }
            
            KString* KString_clone(const KString* of) {
                if (of == NULL) return NULL;
                const KInt size = of->size;
                void* data = malloc(size);
                memcpy(data, of->data, size);
                return KString_new((const char*) data, size, of->length, true);
            }
            
            void KString_free(KString* self) {
                if (self == NULL)
                    return;
                if (K_OBJECT_IS_DATA_OWNER(self->__flags))
                    free((void*) self->data);
                if (K_OBJECT_IS_RELEASABLE(self->__flags))
                    free((void*) self);
            }
            
            void _KString_free_forced(KString* self) {
                if (self == NULL) return;
                self->__flags |= K_FLAG_RELEASABLE;
                KString_free(self);
            }
            
            // Arrays
            
            #define KArrayDef(Name, Type, VarargType)                           \
            Name* Name##_new(                                                   \
            	const Type* elements,                                           \
            	const KInt length,                                              \
            	const bool is_data_owner                                        \
            ) {                                                                 \
                Name* result = (Name*) malloc(sizeof(Name));                    \
                *result = (Name){                                               \
                    elements,                                                   \
                    length * sizeof(Name),                                      \
                    length,                                                     \
                    K_FLAG_RELEASABLE | (is_data_owner ? K_FLAG_DATA_OWNER : 0) \
                };                                                              \
                return result;                                                  \
            }                                                                   \
                                                                                \
            Name* _##Name##_of(const int n, ...) {                              \
                va_list args;                                                   \
                va_start(args, n);                                              \
                Type* elements = (Type*) malloc(n * sizeof(Type));              \
                for (int i = 0; i < n; i++)                                     \
                    elements[i] = (Type) va_arg(args, VarargType);              \
                va_end(args);                                                   \
                return Name##_new((const Type*) elements, (KInt) n, true);      \
            }

            #define KArrayCloneDef(Name, Type)                                     \
            Name* Name##_clone(const Name* of) {                                   \
                if(of == NULL) return NULL;                                        \
                const KInt size = of->size;                                        \
                void** elements = malloc(size);                                    \
                memcpy(elements, (void*) of->elements, size);                      \
                return Name##_new((Type*) elements, of->length, true);             \
            }                                                                      \
                                                                                   \
            void Name##_free(Name* self) {                                         \
            	if (self == NULL)			                                       \
            		return;                                                        \
            	if (K_OBJECT_IS_DATA_OWNER(self->__flags))                         \
            		free((void*) self->elements);                                  \
            	if (K_OBJECT_IS_RELEASABLE(self->__flags))                         \
            		free((void*) self);                                            \
            }                                                                      \
                                                                                   \
            void _##Name##_free_forced(Name* self) {                               \
                if (self == NULL) return;                                          \
                self->__flags |= K_FLAG_RELEASABLE;                                \
                Name##_free(self);                                                 \
            }

            KArray* KArray_clone(const KArray* of, void* (*clone_op)(void*)) {
                if(of == NULL) return NULL;
            	const KInt size = of->size;
            	void** elements = malloc(size);
            	for (int i = 0; i < of->length; i++) {
                    void* element = (void*) of->elements[i];
                    elements[i] = element == NULL ? NULL : clone_op(element);
                }
            	return KArray_new((const void**) elements, of->length, true);
            }

            void KArray_free(const KArray* self, void (*free_op)(void*)) {
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

            void _KArray_free_forced(KArray* self, void (*free_op)(void*)) {
                if(self == NULL) return;
                self->__flags |= K_FLAG_RELEASABLE;
                KArray_free(self, free_op);
            }
            
            KArrayDef(KCharArray,	 KChar,    int32_t)
            KArrayDef(KBooleanArray, KBoolean, int32_t)
            KArrayDef(KByteArray,	 KByte,    int32_t)
            KArrayDef(KUByteArray,	 KUByte,   int32_t)
            KArrayDef(KShortArray,	 KShort,   int32_t)
            KArrayDef(KUShortArray,	 KUShort,  int32_t)
            KArrayDef(KIntArray,	 KInt,     int32_t)
            KArrayDef(KUIntArray,	 KUInt,    int32_t)
            KArrayDef(KLongArray,	 KLong,    int64_t)
            KArrayDef(KULongArray,	 KULong,   int64_t)
            KArrayDef(KFloatArray,	 KFloat,   double)
            KArrayDef(KDoubleArray,  KDouble,  double)
            KArrayDef(KArray,        void*,    void*)
            #undef KArrayDef
            
            KArrayCloneDef(KCharArray,    KChar)
            KArrayCloneDef(KBooleanArray, KBoolean)
            KArrayCloneDef(KByteArray,    KByte)
            KArrayCloneDef(KUByteArray,   KUByte)
            KArrayCloneDef(KShortArray,   KShort)
            KArrayCloneDef(KUShortArray,  KUShort)
            KArrayCloneDef(KIntArray,     KInt)
            KArrayCloneDef(KUIntArray,    KUInt)
            KArrayCloneDef(KLongArray,    KLong)
            KArrayCloneDef(KULongArray,   KULong)
            KArrayCloneDef(KFloatArray,   KFloat)
            KArrayCloneDef(KDoubleArray,  KDouble)
            #undef KArrayCloneDef
            
        """.trimIndent())
    }
}