package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.kotlin.toKotlinType
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*
import io.github.vinceglb.filekit.*
import kotlinx.io.SystemLineSeparator

class CApiImplPrinter(
    val context: NativeModuleContext,
    target: PlatformFile
) {
    init {
        target.parent()!!.createDirectories()
        target.writeSync(buildString {
            printHeader()
            printStdLib()
            printStructs()
            printCallbacks()
            printFunctions()
        }.replace("\n", SystemLineSeparator))
    }

    private fun StringBuilder.printHeader() {
        append("""
            #include "api.h"
            
            
        """.trimIndent())
        if(context.needsAllocFunctions) append("""
            
            LIB_EXPORT void* ${context.mangle("alloc")}(size_t size) {
                return malloc(size);
            }
            LIB_EXPORT void ${context.mangle("dealloc")}(void* ptr, size_t size) {
                free(ptr);
            }
        """.trimIndent())
    }

    private fun StringBuilder.printStdLib() {
        if(context.callbacks.isNotEmpty() || context.interfaces.isNotEmpty()) {
            printLabel("RefCounted")
            append("""
                
                typedef struct RC__Abstract {
                    void* clone;
                    void* free;
                    void (* _Nonnull free_pointed)(void* _Nonnull);
                    void* pointed;
                    int32_t refs;
                } RC__Abstract;
    
                static void* _rc_clone(void* self) {
                    ((RC__Abstract*) self)->refs++;
                    return self;
                }
    
                static void _rc_free(void* self) {
                    RC__Abstract* _self = (RC__Abstract*) self;
                    if (self != NULL && --_self->refs == 0) {
                        _self->free_pointed(_self->pointed);
                        free(_self);
                    }
                }
                
            """.trimIndent())

            context.interfaces.forEach { inter ->
                val name = inter.cname
                val lower = inter.name.lowercase()
                append("""
                    
                    RC_$name* rc_${lower}_new(void* ptr) {
                        RC_$name* result = malloc(sizeof(RC_$name));
                        *result = (RC_$name) {
                            .clone = (RC_$name* (*)(RC_$name*)) _rc_clone,
                            .free = (void (*)(RC_$name*)) _rc_free,
                            .free_pointed = _interface_${lower}_free,
                            .pointed = ptr, 
                            .refs = 1
                        };
                        return result;
                    }
                    
                """.trimIndent())
            }

            context.callbacks.forEach { inter ->
                val name = inter.cname
                val lower = inter.name.lowercase()
                append("""
                    
                    RC_$name* rc_${lower}_new($name* ptr) {
                        RC_$name* result = malloc(sizeof(RC_$name));
                        *result = (RC_$name) {
                            .clone = (RC_$name* (*)(RC_$name*)) _rc_clone,
                            .free = (void (*)(RC_$name*)) _rc_free,
                            .free_pointed = ${lower}_free,
                            .pointed = ptr, 
                            .refs = 1
                        };
                        return result;
                    }
                    
                """.trimIndent())
            }
        }

        if(context.hasStringCast) {
            printLabel("String")
            append("""
                    
                // String
                
                static size_t utf8_strlen(const char *s) {
                    size_t count = 0;
                    while (*s) {
                        if ((*s & 0xC0) != 0x80)
                            count++;
                        s++;
                    }
                    return count;
                }
                
                KString* _kstring_new(const char* data, _KStringNewArgs args) {
                    const int32_t size = args.size != -1 ? args.size : strlen(data);
                    const int32_t length = args.length != -1 ? args.length : utf8_strlen(data);
                    const char* actual_data = data;
                    if (args.make_copy) {
                        actual_data = (const char*) malloc(size);
                        memcpy((void*) actual_data, data, size);
                    }
                    KString* result = (KString*) malloc(sizeof(KString));
                    *result = (KString) { kstring_clone, kstring_free, actual_data, length, size };
                    return result;
                }
                
                KString* kstring_clone(const KString* of) {
                    const size_t size = of->size;
                    void* data = malloc(size);
                    memcpy(data, of->data, size);
                    return kstring_new((const char*) data, .length = of->length, .size = size, .make_copy = false);
                }
                
                void kstring_free(KString* self) {
                    if(self == NULL) return;
                    free((void*) self->data);
                    free((void*) self);
                }
                
            """.trimIndent())
            if(context.hasStringCastToNative) append("""
                
                KString* ${context.mangle("string_new")}(const char* data, const int32_t length, const int32_t size, bool make_copy) {
                    return kstring_new(data, .length = length, .size = size, .make_copy = make_copy);
                }
            """.trimIndent())
            if(context.hasStringCastToKotlin) append("""
                
                const char* ${context.mangle("string_data")}(const KString* self) {
                    return self->data;
                }
                int32_t ${context.mangle("string_length")}(const KString* self) {
                    return self->length;
                }
                size_t ${context.mangle("string_size")}(const KString* self) {
                    return self->size;
                }
                
            """.trimIndent())
            append("""
                
                void ${context.mangle("string_free")}(KString* self) {
                    kstring_free(self);
                }
            """.trimIndent())
            append("\n")
        }

        if(context.hasPrimitiveArrayCast) {
            printLabel("Primitive arrays")
            listOf(
                Triple(Triple("Char", "uint16_t", "int32_t"),
                    context.hasCharArrayCastToNative,
                    context.hasCharArrayCastToKotlin),
                Triple(Triple("Boolean", "bool", "int32_t"),
                    context.hasBooleanArrayCastToNative,
                    context.hasBooleanArrayCastToKotlin),
                Triple(Triple("Byte", "int8_t", "int32_t"),
                    context.hasByteArrayCastToNative || context.hasUByteArrayCastToNative,
                    context.hasByteArrayCastToKotlin || context.hasUByteArrayCastToKotlin),
                Triple(Triple("UByte", "uint8_t", "int32_t"),
                    context.hasUByteArrayCastToNative,
                    context.hasUByteArrayCastToKotlin),
                Triple(Triple("Short", "int16_t", "int32_t"),
                    context.hasShortArrayCastToNative || context.hasUShortArrayCastToNative,
                    context.hasShortArrayCastToKotlin || context.hasUShortArrayCastToKotlin),
                Triple(Triple("UShort", "uint16_t", "int32_t"),
                    context.hasUShortArrayCastToNative,
                    context.hasUShortArrayCastToKotlin),
                Triple(Triple("Int", "int32_t", "int32_t"),
                    context.hasIntArrayCastToNative || context.hasEnumArrayCastToNative || context.hasUIntArrayCastToNative,
                    context.hasIntArrayCastToKotlin || context.hasEnumArrayCastToKotlin || context.hasUIntArrayCastToKotlin),
                Triple(Triple("UInt", "uint32_t", "int32_t"),
                    context.hasUIntArrayCastToNative,
                    context.hasUIntArrayCastToKotlin),
                Triple(Triple("Long", "int64_t", "int64_t"),
                    context.hasLongArrayCastToNative || context.hasULongArrayCastToNative,
                    context.hasLongArrayCastToKotlin || context.hasULongArrayCastToKotlin),
                Triple(Triple("ULong", "uint64_t", "int64_t"),
                    context.hasULongArrayCastToNative,
                    context.hasULongArrayCastToKotlin),
                Triple(Triple("Float", "float", "double"),
                    context.hasFloatArrayCastToNative,
                    context.hasFloatArrayCastToKotlin),
                Triple(Triple("Double", "double", "double"),
                    context.hasDoubleArrayCastToNative,
                    context.hasDoubleArrayCastToKotlin),
            ).forEach { (desc, hasToNativeCast, hasToKotlinCast) ->
                if(!hasToNativeCast && !hasToKotlinCast)
                    return@forEach

                val name = "K${desc.first}Array"
                val type = desc.second
                val varargType = desc.third
                val funcName = name.snakeCase()
                val lowerName = name.lowercase().drop(1)

                append("""
                    
                    // $name
                    
                    $name* ${funcName}_new(const $type* elements, const int32_t length, bool make_copy) {
                        $name* result = ($name*) malloc(sizeof($name));
                        const $type* actual_elements = elements;
                        if (make_copy) {
                            size_t size = sizeof($type) * length;
                            actual_elements = (const $type*) malloc(size);
                            memcpy((void*) actual_elements, (void*) elements, size);
                        }
                        *result = ($name) { ${funcName}_clone, ${funcName}_free, actual_elements, length };
                        return result;
                    }
                    
                    $name* ${funcName}_of_n(const int n, ...) {
                        va_list args;
                        va_start(args, n);
                        $type* elements = ($type*) malloc(n * sizeof($type));
                        for (int i = 0; i < n; i++)
                            elements[i] = ($type) va_arg(args, $varargType);
                        va_end(args);
                        return ${funcName}_new((const $type*) elements, (int32_t) n, false);
                    }
                    
                    $name* ${funcName}_clone(const $name* of) {
                        if(of == NULL) return NULL;
                        return ${funcName}_new(of->elements, of->length, true);
                    }
                    
                    void ${funcName}_free($name* self) {
                        if(self == NULL) return;
                        free((void*) self->elements);
                        free((void*) self);
                    }
                    
                """.trimIndent())

                // Skip for unsigned types
                if(desc.first in setOf("UByte", "UShort", "UInt", "ULong"))
                    return@forEach

                if(hasToNativeCast) append("""
                    
                    $name* ${context.mangle("${lowerName}_new")}(const $type* elements, const int32_t length, const bool make_copy) {
                        return ${funcName}_new(elements, length, make_copy);
                    }
                """.trimIndent())
                if(hasToKotlinCast) append("""
                    
                    const $type* ${context.mangle("${lowerName}_elements")}(const $name* self) {
                        return self->elements;
                    }
                    int32_t ${context.mangle("${lowerName}_length")}(const $name* self) {
                        return self->length;
                    }
                """.trimIndent())
                appendLine("""
                    
                    void ${context.mangle("${lowerName}_free")}($name* self) {
                        ${funcName}_free(self);
                    }
                """.trimIndent())
            }
        }

        if(context.hasObjectArraysCast) {
            printLabel("Object array")
            append("""
                
                typedef struct ArrayElement ArrayElement;
                struct ArrayElement {
                    void* _Nullable (* _Nullable clone)(ArrayElement* _Nullable);
                    void (* _Nullable free)(ArrayElement* _Nullable);
                };
                
                KArray* karray_with_capacity(const int32_t capacity) {
                    KArray* result = (KArray*) malloc(sizeof(KArray));
                    *result = (KArray) { karray_clone, karray_free, malloc(capacity * sizeof(void*)), 0, capacity };
                    return result;
                }
                
                KArray* karray_new(const void** elements, const int32_t length) {
                    KArray* result = (KArray*) malloc(sizeof(KArray));
                    *result = (KArray) { karray_clone, karray_free, elements, length, length };
                    return result;
                }
                
                KArray* karray_of_n(const int n, ...) {
                    va_list args;
                    va_start(args, n);
                    void** elements = (void**) malloc(n * sizeof(void*));
                    for (int i = 0; i < n; i++)
                        elements[i] = (void*) va_arg(args, void*);
                    va_end(args);
                    return karray_new((const void**) elements, (int32_t) n);
                }
                
                void karray_push(KArray* _Nullable self, const void* element) {
                    if(self == NULL) return;
                    if (self->length >= self->capacity) {
                        self->capacity = self->capacity == 0 ? 4 : self->capacity * 2;
                        self->elements = realloc(self->elements, self->capacity * sizeof(void*));
                    }
                    self->elements[self->length++] = element;
                }
                
                KArray* karray_clone(const KArray* _Nullable self) {
                    if(self == NULL) return NULL;
                    const int32_t size = self->length * sizeof(void*);
                    void** elements = malloc(size);
                    for (int i = 0; i < self->length; i++) {
                        ArrayElement* element = (ArrayElement*) self->elements[i];
                        elements[i] = element == NULL ? NULL : element->clone(element);
                    }
                    return karray_new((const void**) elements, self->length);
                }
                
                void karray_free(KArray* _Nullable self) {
                    if(self == NULL) return;
                    const void** elements = self->elements;
                    for (int i = 0; i < self->length; i++) {
                        ArrayElement* element = (ArrayElement*) elements[i];
                        if(element == NULL) continue;
                        element->free(element);
                    }
                    free((void*) elements);
                    free((void*) self);
                }
                
                #define IMPL_OBJECT_ARRAY(T, FUNC_NEW, FUNC_LENGTH, FUNC_PUSH, FUNC_GET, FUNC_FREE) \
                LIB_EXPORT KArray* FUNC_NEW(int32_t capacity, bool ne) {                     \
                    return karray_with_capacity(capacity);                                \
                }                                                                         \
                LIB_EXPORT int32_t FUNC_LENGTH(KArray* self, bool ne) {                      \
                    return ((KArray*) self)->length;                                      \
                }                                                                         \
                LIB_EXPORT void FUNC_PUSH(KArray* self, void* element, bool ne) {         \
                    karray_push((KArray*) self, element);                                 \
                }                                                                         \
                LIB_EXPORT T* FUNC_GET(KArray* self, int32_t index, bool ne) {               \
                    return (void*) ((KArray*) self)->elements[index];                     \
                }                                                                         \
                LIB_EXPORT void FUNC_FREE(KArray* self, bool ne) {                        \
                    karray_free((KArray*) self);                                          \
                }
                
            """.trimIndent())

            buildList {
                context.castedDictionaries.mapTo(this) {
                    Triple(it.cname, it.cname.lowercase(), it in context.usedObjectArrayCast)
                }
                context.castedInterfaces.mapTo(this) {
                    Triple("void", it.cname.lowercase(), it in context.usedObjectArrayCast)
                }
                add(Triple("KString", "string", context.hasStringArrayCast))
            }.filter { it.third }.joinTo(this, separator = "") { (type, name, _) ->
                """
        
                    IMPL_OBJECT_ARRAY($type,
                        ${context.mangle("array_${name}_new")},
                        ${context.mangle("array_${name}_length")},
                        ${context.mangle("array_${name}_push")},
                        ${context.mangle("array_${name}_get")},
                        ${context.mangle("array_${name}_free")}
                    )
                """.trimIndent()
            }
            append("\n")
        }
    }

    private fun StringBuilder.printStructs() {
        if(context.dictionaries.isEmpty())
            return
        printLabel("Struct functions")

        context.dictionaries.forEach { dictionary ->
            val name = dictionary.cname
            val fields = context.allFields[dictionary]!!

            val argNames = fields.map { it.cname }
            val args = fields.map {
                "${it.type.toCType(printNullable = true)} ${it.cname}"
            }

            val fieldsClone = fields.joinToString { field ->
                cloneFuncFor(field.type, "self->${field.cname}")
            }
            val fieldsFree = fields.mapNotNull { field ->
                freeFuncFor(context, field.type, "self->${field.cname}")
            }

            val funcNew = dictionary.subCFunc(context, "new")
            val funcFree = dictionary.subCFunc(context, "free")

            append("""
                
                $name* ${name}_clone(const $name* self) {
                    if(self == NULL) return NULL;
                    return ${name}_new($fieldsClone);
                }
                
                $name* ${name}_new(${args.joinToString()}) {
                    $name* result = ($name*) malloc(sizeof($name));
                    *result = ($name) { ${name}_clone, ${name}_free, ${argNames.joinToString()} };
                    return result;
                }
            
                void ${name}_free($name* self) {
                    if (self == NULL) return;
            """.trimIndent())
            fieldsFree.joinTo(this, separator = "") { "\n\t$it;" }
            append("""
        
                    free((void*) self);
                }
                
            """.trimIndent())

            if(dictionary in context.toNativeDeclarationCasts) append("""
                
                LIB_EXPORT $name* $funcNew(${args.joinToString()}) {
                    return ${name}_new(${argNames.joinToString()});
                }
            """.trimIndent())
            if(dictionary in context.castedDeclarations) append("""
                
                LIB_EXPORT void $funcFree($name* self) {
                    ${name}_free(self);
                }
            """.trimIndent())
            if(dictionary in context.toKotlinDeclarationCasts) {
                fields.forEach { field ->
                    val type = field.type.toCType(printNullable = true)
                    val func = dictionary.subFieldCFunc(context, field)
                    append("""
                        
                        LIB_EXPORT $type $func(const $name* self) {
                            return (($name*) self)->${field.cname};
                        }
                    """.trimIndent())
                }
            }
        }
    }

    private fun StringBuilder.printCallbacks() {
        if(context.callbacks.isEmpty())
            return
        printLabel("Callbacks")

        append("""
            
            #define KCallbackImpl(Name, Lower, FUNC_NEW, FUNC_ID, FUNC_FREE, FuncHeader) \
            LIB_EXPORT RC_##Name* FUNC_NEW(                                     \
                const size_t id,                                                \
                const int32_t hash_code,                                        \
                void* invoke,                                                   \
                void* equals,                                                   \
                void *free                                                      \
            ) {                                                                 \
                Name* callback = (Name*) malloc(sizeof(Name));                  \
            	*callback = (Name) { id, hash_code, invoke, equals, free };     \
            	return rc_##Lower##_new(callback);                              \
            }                                                                   \
            LIB_EXPORT size_t FUNC_ID(RC_##Name* _self) {                       \
                return _self->pointed->id;                                      \
            }                                                                   \
            LIB_EXPORT void FUNC_FREE(RC_##Name* _self) {                       \
                _self->free(_self);                                             \
            }                                                                   \
            FuncHeader
            
            
        """.trimIndent())

        context.callbacks.forEach { callback ->
            val name = callback.cname
            val lower = callback.name.camelCase().lowercase()
            append("KCallbackImpl(")
            append("\n\t$name, $lower,")
            append("\n\t${context.mangle("${lower}_new")},")
            append("\n\t${context.mangle("${lower}_id")},")
            append("\n\t${context.mangle("${lower}_free")},\n\t")

            // func
            append("${callback.type.toCType()} ${lower}_invoke")
            buildList {
                add("RC_$name* self")
                callback.args.mapTo(this) {
                    "${it.type.toCType()} ${it.cname}"
                }
            }.joinTo(this, prefix = "(", postfix = ") {")
            append("\n\t\t")
            if(!callback.type.isVoid())
                append("return ")
            append("self->pointed->invoke(")
            buildList {
                add("self->pointed->id")
                callback.args.mapTo(this) { it.cname }
            }.joinTo(this)
            append(");\n\t}\n)\n")
        }
    }

    private fun StringBuilder.printFunctions() {
        if(context.allOperations.isEmpty())
            return

        printLabel("Functions")
        context.allOperations.forEach { function ->
            val critical = function.isCritical()
            val name = function.cname
            val mangledName = function.cnameMangled(context)
            val type = function.type.toCType(printNullable = true)

            val args = function.args.joinToString {
                val name = it.cname
                when {
                    critical && it.type.isString() ->
                        "const char* _Nullable $name, int32_t _${name}_length, int32_t _${name}_size"
                    critical && it.type.isArray() ->
                        "const ${it.type.arrayTypeOrNull()!!.toCType()}* _Nullable $name, int32_t _${name}_length"
                    else -> "${it.type.toCType(printNullable = true)} ${it.cname}"
                }
            }.ifEmpty { "void" }

            val argNames = function.args.map {
                val name = it.cname
                when {
                    it.type.isRawInterface() ->
                        "$name->pointed"
                    critical && it.type.isString() ->
                        if(it.type.isNullable) "_${name}_length == -1 ? NULL : &(KString){ kstring_clone, kstring_free, $name, _${name}_length, _${name}_size }"
                        else "&(KString){ kstring_clone, kstring_free, $name, _${name}_length, _${name}_size }"
                    critical && it.type.isArray() -> {
                        val type = it.type.toCType(ptr = false)
                        val lower = type.snakeCase()
                        val cast = when {
                            it.type.isEnumArray() -> "(const int32_t*) "
                            else -> ""
                        }
                        if (it.type.isNullable) "_${name}_length == -1 ? NULL : &($type){ ${lower}_clone, ${lower}_free, $cast$name, _${name}_length }"
                        else "&($type){ ${lower}_clone, ${lower}_free, $cast$name, _${name}_length }"
                    }
                    else -> name
                }
            }

            val releases = function.args.mapNotNull {
                when {
                    it.type.isRawInterface() -> null
                    critical -> null
                    else -> freeFuncFor(context, it.type, it.cname)
                }
            }

            // Print

            var call = "$name(${argNames.joinToString()});"

            call = when {
                function.isInterfaceOperationConstructor() ->
                    "rc_${function.interfaceName().lowercase()}_new(${call.dropLast(1)});"
                function.isInterfaceOperationFree() ->
                    "if(_self != NULL) _self->free(_self);"
                function.isInterfaceOperationClone() ->
                    "_self == NULL ? NULL : _self->clone(_self);"
                function.isInterfaceOperationAddress() ->
                    "(int64_t) _self->pointed;"
                else -> call
            }

            append("\nLIB_EXPORT $type $mangledName($args) {")
            append("\n\t")
            if(releases.isNotEmpty()) {
                if(!function.type.isVoid())
                    append("${function.type.toCType()} _result = ")
                append(call)
                releases.joinTo(this, separator = "") { "\n\t$it;" }
                if(!function.type.isVoid())
                    append("\n\treturn _result;")
            } else {
                if(!function.type.isVoid())
                    append("return ")
                append(call)
            }
            append("\n}")
        }
        append("\n")
    }

    private fun cloneFuncFor(
        type: ResolvedIdlType,
        content: String
    ): String = when {
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() -> "k${type.toKotlinType().lowercase()}_array_clone($content)"
                type.isEnum() -> "kint_array_clone($content)"
                else -> "karray_clone($content)"
            }
        }
        type.isCallback() -> "$content->clone($content)"
        type.isString() -> "kstring_clone($content)"
        type.isDictionary() -> "${type.declaration.cname}_clone($content)"
        type.isInterface() ->
            (type.declaration as ResolvedIdlInterface)
                .toOperations()
                .first { it.isInterfaceOperationClone() }
                .cnameMangled(context) + "($content)"
        else -> content
    }
}

internal fun freeFuncFor(
    context: NativeModuleContext,
    type: ResolvedIdlType,
    content: String
): String? {
    return when {
        type.isArray() -> type.arrayType { arrType ->
            when {
                arrType.isPrimitive() -> {
                    val cast = if(arrType.isUnsigned())
                        "(${type.toCType(ignoreUnsigned = true)}) " else ""
                    "${context.mangle("${arrType.toKotlinType(ignoreUnsigned = true).lowercase()}array_free")}($cast$content)"
                }
                arrType.isEnum() -> "${context.mangle("intarray_free")}($content)"
                else -> "${context.mangle("array_${arrType.declaration.name.camelCase().lowercase()}_free")}($content, ${arrType.isNullable})"
            }
        }
        type.isCallback() -> "if($content != NULL) $content->free($content)"
        type.isDictionary() || type.isString() -> "${context.mangle("${type.toKotlinType(printNullable = false).lowercase()}_free")}($content)"
        type.isInterface() -> (type.declaration as ResolvedIdlInterface)
            .toOperations()
            .first { it.isInterfaceOperationFree() }
            .cnameMangled(context) + "($content)"
        else -> null
    }
}