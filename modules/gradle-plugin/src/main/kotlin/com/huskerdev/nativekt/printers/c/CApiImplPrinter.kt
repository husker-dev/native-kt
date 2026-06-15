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
    target: File,
    val classPath: String
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
                
                void __AbstractCallback_forceFree(_AbstractCallback* self) {
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

        // malloc
        append("${dictionary.name}* result = (${dictionary.name}*) malloc(sizeof(${dictionary.name}));\n\t")

        // fill
        append("*result = (${dictionary.name}) {\n\t\t")
        buildList {
            dictionary.allFields().mapTo(this) { field ->
                cloneFuncFor(field.type, "self->${field.name}")
            }
            add("K_FLAG_RELEASABLE")
        }.joinTo(builder, separator = ",\n\t\t")
        append("\n\t};\n\t")

        // return
        append("return result;\n}\n")
    }

    private fun printStructFree(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        // free
        append("""
            
            void ${dictionary.name}_free(${dictionary.name}* self) {
                if(self == NULL || !K_OBJECT_IS_RELEASABLE(self->__flags))
                    return;
            
        """.trimIndent())
        dictionary.allFields().forEach { field ->
            freeFuncFor(
                field.type,
                "self->${field.name}"
            )?.apply { append("\n\t$this;") }
        }
        append("""
            
            if(!K_OBJECT_IS_ON_STACK(self->__flags))
                free((void*) self);
        """.replaceIndent("\t"))
        append("\n}\n")

        // forceFree
        append("\nvoid _${dictionary.name}_forceFree(${dictionary.name}* self) {")
        append("\n\tif(self == NULL) return;")
        dictionary.allFields().forEach { field ->
            forceFreeFuncFor(
                field.type,
                "self->${field.name}"
            )?.apply { append("\n\t$this;") }
        }
        append("""
            
            if(!K_OBJECT_IS_ON_STACK(self->__flags))
                free((void*) self);
        """.replaceIndent("\t"))
        append("\n}\n")
    }

    private fun printStdLib(builder: StringBuilder) = builder.apply {
        printLabel(builder, "stdlib")

        builder.append("""
            
            // String
            
            KString* KString_new(const char* data, const KInt length, const size_t size) {
                KString* result = (KString*) malloc(sizeof(KString));
                *result = (KString) { data, size, length, K_FLAG_RELEASABLE };
                return result;
            }

            KString* KString_clone(const KString* of) {
                if(of == NULL) return NULL;
                const KInt size = of->size;
                void* data = malloc(size);
                memcpy(data, of->data, size);
                KString* result = (KString*) malloc(sizeof(KString));
                *result = (KString) { (const char*) data, size, of->length, K_FLAG_RELEASABLE };
                return result;
            }

            void KString_free(KString* self) {
                if(self == NULL || !K_OBJECT_IS_RELEASABLE(self->__flags))
                    return;
                free((void*) self->data);
                if(!K_OBJECT_IS_ON_STACK(self->__flags))
                    free((void*) self);
            }
            
            void _KString_forceFree(KString* self) {
                if(self == NULL) return;
                self->__flags |= K_FLAG_RELEASABLE;
                KString_free(self);
            }
            
            // Arrays
            
            #define KArrayDef(Name, Type, VarargType)                          \
            Name* Name##_new(const Type* elements, const KInt length) {        \
                Name* result = (Name*) malloc(sizeof(Name));                   \
                *result = (Name){                                              \
                    elements,                                                  \
                    length * sizeof(Name),                                     \
                    length,                                                    \
                    K_FLAG_RELEASABLE                                          \
                };                                                             \
                return result;                                                 \
            }                                                                  \
                                                                               \
            Name* _##Name##_of(const int n, ...) {                             \
                va_list args;                                                  \
                va_start(args, n);                                             \
                Type* elements = (Type*)malloc(n * sizeof(Type));              \
                for (int i = 0; i < n; i++)                                    \
                    elements[i] = (Type)va_arg(args, VarargType);              \
                va_end(args);                                                  \
                Name* result = (Name*) malloc(sizeof(Name));                   \
                *result = (Name){                                              \
                    (const Type*) elements,                                    \
                    n * sizeof(Name),                                          \
                    n,                                                         \
                    K_FLAG_RELEASABLE                                          \
                };                                                             \
                return result;                                                 \
            }

            #define KArrayCloneDef(Name, Type)                                     \
            Name* Name##_clone(const Name* of) {                                   \
                if(of == NULL) return NULL;                                        \
                const KInt size = of->size;                                        \
                void** elements = malloc(size);                                    \
                memcpy(elements, (void*) of->elements, size);                      \
                Name* result = (Name*) malloc(sizeof(Name));                       \
                *result = (Name) {                                                 \
                    (Type*) elements,                                              \
                    of->size,                                                      \
                    of->length,                                                    \
                    K_FLAG_RELEASABLE                                              \
                };                                                                 \
                return result;                                                     \
            }                                                                      \
                                                                                   \
            void Name##_free(Name* arr) {                                          \
                if(arr == NULL || !K_OBJECT_IS_RELEASABLE(arr->__flags))           \
                    return;                                                        \
                free((void*) arr->elements);                                       \
                if(!K_OBJECT_IS_ON_STACK(arr->__flags))                            \
                    free((void*) arr);                                             \
            }                                                                      \
                                                                                   \
            void _##Name##_forceFree(Name* self) {                                 \
                if(self == NULL) return;                                           \
                self->__flags |= K_FLAG_RELEASABLE;                                \
                Name##_free(self);                                                 \
            }
            
            KArray* KArray_clone(const KArray* of, void* (*cloneOp)(void*)) {
                if(of == NULL) return NULL;
            	const KInt size = of->size;
            	void** elements = malloc(size);
            	for (int i = 0; i < of->length; i++) {
                    void* element = (void*) of->elements[i];
                    if(element != NULL) 
                        elements[i] = cloneOp(element);
            		else elements[i] = NULL;
                }
            	KArray* result = (KArray*) malloc(sizeof(KArray));
            	*result = (KArray) { 
                    (const void**) elements, 
                    of->size,
                    of->length, 
                    K_FLAG_RELEASABLE
                }; 
            	return result;
            }
            
            void KArray_free(const KArray* self, void (*freeOp)(void*)) {
                if(self == NULL || !K_OBJECT_IS_RELEASABLE(self->__flags))
                    return;
                const void** elements = self->elements;
                for (int i = 0; i < self->length; i++) {
                    void* element = (void*) elements[i];
                    if(element == NULL) continue;
                    freeOp(element);
                }
                free((void*) elements);
                if(!K_OBJECT_IS_ON_STACK(self->__flags))
                    free((void*) self);
            }

            void _KArray_forceFree(KArray* self, void (*freeOp)(void*)) {
                if(self == NULL) return;
                self->__flags |= K_FLAG_RELEASABLE;
                KArray_free(self, freeOp);
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
            
            KArrayCloneDef(KCharArray,    KChar)
            KArrayCloneDef(KBooleanArray, KBoolean)
            KArrayCloneDef(KByteArray,    KByte)
            KArrayCloneDef(KShortArray,   KShort)
            KArrayCloneDef(KIntArray,     KInt)
            KArrayCloneDef(KLongArray,    KLong)
            KArrayCloneDef(KFloatArray,   KFloat)
            KArrayCloneDef(KDoubleArray,  KDouble)
            #undef KArrayCloneDef
            
        """.trimIndent())
    }
}