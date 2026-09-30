package com.huskerdev.nativekt.printers.cpp

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*
import io.github.vinceglb.filekit.*
import kotlinx.io.SystemLineSeparator


class CppApiImplPrinter(
    val context: NativeModuleContext,
    target: PlatformFile
) {
    init {
        target.parent()!!.createDirectories()
        target.writeSync(buildString {
            printHeader()
            printStdLib()
            printBoxedPrimitives()
            printPreDefs()
            printCallbacks()
            printStructs()
            printFunctions()
        }.replace("\n", SystemLineSeparator))
    }

    private fun StringBuilder.printHeader() {
        appendLine("""
            #include "api.hpp"
            
            #ifndef LIB_EXPORT
                #if defined(_WIN32) || defined(__CYGWIN__)
                    #define LIB_EXPORT extern "C" __declspec(dllexport)
                #else
                    #define LIB_EXPORT extern "C" __attribute__((visibility("default")))
                #endif
            #endif
            
            LIB_EXPORT void ${context.mangle("init")}();
        """.trimIndent())
        if(context.needsAllocFunctions) appendLine("""
            
            LIB_EXPORT void* ${context.mangle("alloc")}(size_t size) {
                return size == 0 ? nullptr : malloc(size);
            }
            
            LIB_EXPORT void ${context.mangle("dealloc")}(void* ptr, size_t size) {
                if(ptr != nullptr) free(ptr);
            }
        """.trimIndent())
    }

    private fun StringBuilder.printStdLib() {
        if(!context.hasInterface &&
            !context.hasCallback &&
            !context.hasString &&
            !context.hasPrimitiveArray &&
            !context.hasObjectArrays
        ) return

        printLabel("Types")

        if(context.hasInterface || context.hasCallback) appendLine("""
            
            template <typename T>
            std::shared_ptr<T> arc_unwrap(std::shared_ptr<T>* _Nonnull arg) {
                auto result = std::move(*arg);
                delete arg;
                return result;
            }
            
            template <typename T>
            std::shared_ptr<T>* _Nonnull arc_wrap(std::shared_ptr<T> value) { 
                return new std::shared_ptr(value); 
            }
        """.trimIndent())
        if(context.hasPrimitiveNullable || context.hasEnumNullable) appendLine("""
            
            template <typename T>
            T primitive_unwrap(T* _Nonnull arg) {
                T result = *arg;
                free(arg);
                return result;
            }
            
            template <typename T>
            T* _Nonnull primitive_wrap(T value) { 
                T* result = (T*) malloc(sizeof(T));
                *result = value;
                return result;
            }
        """.trimIndent())

        // String
        if(context.hasString) {
            append("\n// String\n")

            if(context.hasStringToNative) appendLine("""
                
                KString kstring_unwrap(KString* _Nonnull arg) {
                    KString result = std::move(*arg);
                    delete arg;
                    return result;
                }
            """.trimIndent())
            if(context.hasStringToKotlin) appendLine("""
                
                KString* _Nonnull kstring_wrap(KString value) { 
                    return new KString(std::move(value)); 
                }
            """.trimIndent())

            appendLine("""
                
                KString::KString(const char *data, bool copy) {
                    const size_t length = strlen(data);
                    if (copy) {
                        const size_t size = length * sizeof(char);
                        this->data = new char[size];
                        std::memcpy(this->data, data, size);
                    } else 
                        this->data = const_cast<char*>(data);
                    this->size = length;
                }
    
                KString::KString(const char *data, size_t size, bool copy) {
                    if (copy) {
                        this->data = new char[size];
                        std::memcpy(this->data, data, size);
                    } else 
                        this->data = const_cast<char*>(data);
                    this->size = size;
                }
    
                KString::KString(const KString& other): size(other.size) {
                    this->data = new char[other.size];
                    std::memcpy(this->data, other.data, other.size);
                }
    
                KString::KString(KString&& other) noexcept
                    : data(other.data), size(other.size) {
                    other.data = nullptr;
                    other.size = 0;
                }
    
                KString& KString::operator=(KString&& other) noexcept {
                    if (this != &other) {
                        delete[] data;
                        data = other.data;
                        size = other.size;
                        other.data = nullptr;
                        other.size = 0;
                    }
                    return *this;
                }
    
                KString::~KString() {
                    delete[] this->data;
                }
    
                KString::operator const char*() const {
                    return data;
                }
    
                std::string *KString::to_string() const {
                    return new std::string(data, size);
                }
    
                const char *KString::get_data() const {
                    return data;
                }
    
                int32_t KString::get_length() const {
                    if(size == 0)
                        return 0;
                    const char* s = data;
                    size_t count = 0;
                    while (*s) {
                        if ((*s & 0xC0) != 0x80)
                            count++;
                        s++;
                    }
                    return count;
                }
    
                size_t KString::get_size() const {
                    return size;
                }
            """.trimIndent())

            if(context.hasStringToNative) appendLine("""
                
                LIB_EXPORT void* _Nonnull ${context.mangle("string_new")}(char* _Nonnull data, size_t size, bool make_copy) {
                    return new KString(data, size, make_copy);
                }
            """.trimIndent())
            if(context.hasStringToKotlin) appendLine("""
                
                LIB_EXPORT const char* _Nonnull ${context.mangle("string_data")}(void* _Nonnull self) {
                    return static_cast<KString*>(self)->get_data();
                }
                
                LIB_EXPORT size_t ${context.mangle("string_size")}(void* _Nonnull self) {
                    return static_cast<KString*>(self)->get_size();
                }
                
                LIB_EXPORT void ${context.mangle("string_free")}(void* _Nonnull self) {
                    if(self == NULL) return;
                    delete static_cast<KString*>(self);
                }
            """.trimIndent())
            append("\n")
        }

        // Primitive arrays
        if(context.hasPrimitiveArray) {
            appendLine("""
                
                // Primitive arrays
                
                #define IMPL_TYPED_ARRAY(T, FUNC_NEW, FUNC_ELEMENTS, FUNC_LENGTH, FUNC_FREE) \
                LIB_EXPORT void* FUNC_NEW(T* elements, int32_t length, bool make_copy) {  \
                    return new KArray<T>(elements, length, make_copy);                    \
                }                                                                         \
                LIB_EXPORT T* FUNC_ELEMENTS(void* self) {                                 \
                    return const_cast<T*>(static_cast<KArray<T>*>(self)->get_elements()); \
                }                                                                         \
                LIB_EXPORT int32_t FUNC_LENGTH(void* self) {                              \
                    return static_cast<KArray<T>*>(self)->get_length();                   \
                }                                                                         \
                LIB_EXPORT void FUNC_FREE(void* self) {                                   \
                    delete static_cast<KArray<T>*>(self);                                 \
                }
            """.trimIndent())
            listOf(
                Triple("Char", "uint16_t", context.hasCharArray),
                Triple("Boolean", "bool", context.hasBooleanArray),
                Triple("Byte", "int8_t", context.hasByteArray || context.hasUByteArray),
                Triple("Short", "int16_t", context.hasShortArray || context.hasUShortArray),
                Triple("Int", "int32_t", context.hasIntArray || context.hasEnums || context.hasUIntArray),
                Triple("Long", "int64_t", context.hasLongArray || context.hasULongArray),
                Triple("Float", "float", context.hasFloatArray),
                Triple("Double", "double", context.hasDoubleArray)
            ).filter { it.third }
                .forEach {
                    val name = "${it.first.lowercase()}array"
                    val type = it.second
                    appendLine("""
                        
                        IMPL_TYPED_ARRAY($type,
                            ${context.mangle("${name}_new")},
                            ${context.mangle("${name}_elements")},
                            ${context.mangle("${name}_length")},
                            ${context.mangle("${name}_free")}
                        );
                    """.trimIndent())
                }
        }

        // Typed arrays
        if(context.hasObjectArrays || context.hasPrimitiveNullableArray || context.hasEnumNullableArray) {
            appendLine("""
    
                // Typed array macros
                
                #define IMPL_OBJECT_ARRAY(T, FUNC_NEW, FUNC_LENGTH, FUNC_PUSH, FUNC_GET, FUNC_FREE) \
                LIB_EXPORT void* FUNC_NEW(int32_t capacity, bool ne) {                    \
                    if (ne) return new KArray<KOptional<T>>(capacity);                    \
                    return new KArray<T>(capacity);                                       \
                }                                                                         \
                LIB_EXPORT int32_t FUNC_LENGTH(void* arr, bool ne) {                      \
                    if (ne) return static_cast<KArray<KOptional<T>>*>(arr)->get_length(); \
                    return static_cast<KArray<T>*>(arr)->get_length();                    \
                }                                                                         \
                LIB_EXPORT void FUNC_PUSH(void* array, void* element, bool ne) {          \
                    const auto el = static_cast<T*>(element);                             \
                    if (ne) {                                                             \
                        const auto arr = static_cast<KArray<KOptional<T>>*>(array);       \
                        if (el == nullptr) arr->push(KOptional<T>());                     \
                        else { arr->push(KOptional<T>(std::move(*el))); delete el; }      \
                    } else {                                                              \
                        const auto arr = static_cast<KArray<T>*>(array);                  \
                        arr->push(std::move(*el)); delete el;                             \
                    }                                                                     \
                }                                                                         \
                LIB_EXPORT void* FUNC_GET(void* array, int32_t index, bool ne) {          \
                    if (ne) {                                                             \
                        const auto arr = static_cast<KArray<KOptional<T>>*>(array);       \
                        const KOptional<T>* opt = arr->get_elements() + index;            \
                        if (opt->is_none()) return nullptr;                               \
                        return const_cast<T*>(opt->get_ptr());                            \
                    }                                                                     \
                    const auto arr = static_cast<KArray<T>*>(array);                      \
                    return const_cast<T*>(arr->get_elements() + index);                   \
                }                                                                         \
                LIB_EXPORT void FUNC_FREE(void* array, bool ne) {                         \
                    if (ne) delete static_cast<KArray<KOptional<T>>*>(array);             \
                    else delete static_cast<KArray<T>*>(array);                           \
                }
            """.trimIndent())
            buildList {
                context.dictionaries
                    .filter { it in context.usedObjectArray }
                    .mapTo(this) { it.cppName to it.cname.lowercase() }
                context.castedInterfaces
                    .filter { it in context.usedObjectArray }
                    .mapTo(this) { "std::shared_ptr<${it.cppName}>" to it.cname.lowercase() }

                if(context.hasStringArray) add("KString" to "string")
                if(context.hasCharNullableArray)    add("uint16_t" to "char")
                if(context.hasBooleanNullableArray) add("int8_t" to "boolean")
                if(context.hasByteNullableArray || context.hasUByteNullableArray)   add("int8_t" to "byte")
                if(context.hasShortNullableArray || context.hasUShortNullableArray) add("int16_t" to "short")
                if(context.hasIntNullableArray || context.hasUIntNullableArray || context.hasEnumNullableArray) add("int32_t" to "int")
                if(context.hasLongNullableArray || context.hasULongNullableArray)   add("int64_t" to "long")
                if(context.hasFloatNullableArray)   add("float" to "float")
                if(context.hasDoubleNullableArray)  add("double" to "double")
            }.forEach {
                appendLine("""
        
                    IMPL_OBJECT_ARRAY(${it.first},
                        ${context.mangle("array_${it.second}_new")},
                        ${context.mangle("array_${it.second}_length")},
                        ${context.mangle("array_${it.second}_push")},
                        ${context.mangle("array_${it.second}_get")},
                        ${context.mangle("array_${it.second}_free")}
                    )
                """.trimIndent())
            }
        }
        append("\n")
    }

    private fun StringBuilder.printBoxedPrimitives() {
        printLabel("Boxed primitives")

        appendLine("""
            
            #define IMPL_BOXED_PRIMITIVE(T, FUNC_NEW, FUNC_GET, FUNC_FREE) \
            LIB_EXPORT T* FUNC_NEW(T value) {            \
                T* result = (T*) malloc(sizeof(T));      \
                result[0] = value;                       \
                return result;                           \
            }                                            \
            LIB_EXPORT T FUNC_GET(T* self, bool _free) { \
                T value = self[0];                       \
                if(_free) free((void*) self);            \
                return value;                            \
            }
        """.trimIndent())

        listOf(
            Triple("uint16_t", "char", context.hasCharNullable),
            Triple("int8_t", "boolean", context.hasBooleanNullable),
            Triple("int8_t", "byte", context.hasByteNullable || context.hasUByteNullable),
            Triple("int16_t", "short", context.hasShortNullable || context.hasUShortNullable),
            Triple("int32_t", "int", context.hasIntNullable || context.hasUIntNullable || context.hasEnumNullable),
            Triple("int64_t", "long", context.hasLongNullable || context.hasULongNullable),
            Triple("float", "float", context.hasFloatNullable),
            Triple("double", "double", context.hasDoubleNullable)
        ).forEach { (type, name, hasCast) ->
            if(!hasCast) return@forEach
            appendLine("""
                IMPL_BOXED_PRIMITIVE(
                    $type,
                    ${context.mangle("${name}_new")},
                    ${context.mangle("${name}_get")},
                    ${context.mangle("${name}_free")}
                )
            """.trimIndent())
        }
    }

    private fun StringBuilder.printPreDefs() {
        if(!context.hasDictionary)
            return
        printLabel("Pre-definitions")

        context.castedDictionaries.forEach { dictionary ->
            val name = dictionary.cppName
            val lower = dictionary.name.camelCase().lowercase()

            if(dictionary in context.toNativeDeclaration)
                append("\n$name ${lower}_unwrap($name* _Nonnull arg);")
            if(dictionary in context.toKotlinDeclaration)
                append("\n$name* _Nonnull ${lower}_wrap($name value);")
        }
        append("\n")
    }

    private fun StringBuilder.printCallbacks() {
        if(context.callbacks.isEmpty())
            return
        printLabel("Callbacks")

        appendLine("""
            
            #define KCallbackImpl(Name, InvokeBlock, FUNC_NEW, FUNC_ID, FUNC_FREE)                  \
            Name::Name(                                                                             \
                const size_t id,                                                                    \
                const int32_t hash_code,                                                               \
                void* _Nonnull invoke,                                                              \
                bool (* _Nonnull equals)(size_t, size_t),                                           \
                void (* _Nonnull free)(size_t)                                                      \
            ) : id(id), _invoke(invoke), _equals(equals), _free(free), hash_code(hash_code) {}      \
            Name::~Name() {                                                                         \
                _free(id);                                                                          \
            }                                                                                       \
            bool Name::operator==(const Name &other) const {                                        \
                return _equals(id, other.id);                                                       \
            }                                                                                       \
            template <>                                                                             \
            struct std::hash<Name> {                                                                \
                size_t operator()(const Name& u) const noexcept {                                   \
                    return u.hash_code;                                                             \
                }                                                                                   \
            };                                                                                      \
            InvokeBlock                                                                             \
                                                                                                    \
            LIB_EXPORT std::shared_ptr<Name>* FUNC_NEW(                                             \
                const size_t id,                                                                    \
                const int32_t hash_code,                                                            \
                void* _Nonnull invoke,                                                              \
                bool (* _Nonnull equals)(size_t, size_t),                                           \
                void (* _Nonnull free)(size_t)                                                      \
            ) {                                                                                     \
                return new std::shared_ptr<Name>(new Name(id, hash_code, invoke, equals, free));    \
            }                                                                                       \
            LIB_EXPORT size_t FUNC_ID(std::shared_ptr<Name>* _self) {                               \
                return (*_self)->id;                                                                \
            }                                                                                       \
            LIB_EXPORT void FUNC_FREE(std::shared_ptr<Name>* _self) {                               \
                delete _self;                                                                       \
            }
            
        """.trimIndent())

        context.callbacks.forEach { callback ->
            val name = callback.cppName
            val lower = callback.name.camelCase().lowercase()
            val type = callback.type.toCppType()
            val ret = if(callback.type.isVoid()) "" else "return "

            val args = callback.args.joinToString {
                "${it.type.toCppType()} ${it.cppName}"
            }

            val convertedArgs = buildList {
                add("this->id")
                callback.args.mapTo(this) { toNativeType(it.type, it.cppName) }
            }.joinToString()

            val nativeArgs = buildList {
                add("size_t id")
                callback.args.mapTo(this) { it.type.toCppType(ptr = true, printOption = false) }
            }.joinToString()

            val nativeType = callback.type.toCppType(ptr = true, printOption = false)

            val call = "reinterpret_cast<$nativeType (*)(${nativeArgs})>(_invoke)($convertedArgs)"

            appendLine("""
                KCallbackImpl(
                    $name, 
                    $type $name::invoke($args) const { ${ret}${toCppType(callback.type, call)}; },
                    ${context.mangle("${lower}_new")},
                    ${context.mangle("${lower}_id")},
                    ${context.mangle("${lower}_free")}
                );
            """.trimIndent())
        }
    }

    private fun StringBuilder.printStructs() {
        if(context.dictionaries.isEmpty())
            return

        context.dictionaries.forEach { dictionary ->
            val name = dictionary.cppName
            val allFields = context.allFields[dictionary]!!
            val localFields = dictionary.fields
            val parentArgs = allFields.filter { it !in localFields }

            val allArgs = allFields.joinToString {
                "${it.type.toCppType()} ${it.cppName}"
            }
            val allNativeArgs = allFields.joinToString {
                "${it.type.toCppType(printOption = false, ptr = true)} ${it.cppName}"
            }
            val castedArgs = allFields.joinToString {
                toCppType(it.type, it.cppName)
            }

            var constructorInit = buildList {
                fun ResolvedIdlField.Declaration.move() =
                    if(type.isReleasable()) "std::move(${cppName})" else cppName

                if(dictionary.implements != null)
                    add("${dictionary.implements!!.cppName}(${parentArgs.joinToString { it.move() }})")
                localFields.mapTo(this) { "${it.cppName}(${it.move()})" }
            }.joinToString()

            var copyInit = buildList {
                if(dictionary.implements != null)
                    add("${dictionary.implements!!.cppName}(other)")
                localFields.mapTo(this) { "${it.cppName}(other.${it.cppName})" }
            }.joinToString()

            if(constructorInit.isNotEmpty())
                constructorInit = " : $constructorInit"
            if(copyInit.isNotEmpty())
                copyInit = " : $copyInit"

            appendLine("""
                
                // $name
                
                $name::$name($allArgs)$constructorInit {}
                $name::$name(const $name& other)$copyInit {}
            """.trimIndent())

            // Unwrap
            if(dictionary in context.toNativeDeclaration) appendLine("""
                
                $name ${dictionary.name.camelCase().lowercase()}_unwrap($name* _Nonnull arg) {
                    $name result = std::move(*arg);
                    delete arg;
                    return result;
                }
            """.trimIndent())

            // Wrap
            if(dictionary in context.toKotlinDeclaration) appendLine("""
                
                $name* _Nonnull ${dictionary.name.camelCase().lowercase()}_wrap($name value) {
                    return new $name(std::move(value));
                }
            """.trimIndent())

            // Default
            if(dictionary in context.toNativeDeclaration) appendLine("""
                
                LIB_EXPORT void* ${dictionary.subCFunc(context, "new")}($allNativeArgs) {
                    return new $name($castedArgs);
                }
            """.trimIndent())
            if(dictionary in context.toKotlinDeclaration) {
                appendLine("""
                    
                    LIB_EXPORT void ${dictionary.subCFunc(context, "free")}(void* self) {
                        delete static_cast<$name*>(self);
                    }
                """.trimIndent())
                allFields.forEach { field ->
                    val nativeType = if(field.type.isReleasable()) "void*" else field.type.toCType()
                    val funcName = dictionary.subFieldCFunc(context, field)

                    val ref = "static_cast<$name*>(self)->${field.cppName}"

                    val call = when {
                        field.type.isNullable -> "$ref.get_ptr()"
                        field.type.isReleasable() -> "&$ref"
                        else -> ref
                    }
                    append("\nLIB_EXPORT $nativeType $funcName(void* self) { return $call; }")
                }
                append("\n")
            }
        }
    }

    private fun StringBuilder.printFunctions() {
        if(context.allOperations.isEmpty())
            return
        printLabel("Functions")

        context.allOperations.forEach { function ->
            val critical = function.isCritical()
            val name = function.cppName
            val mangledName = function.cnameMangled(context)
            val type = function.type.toCppType(printOption = false, ptr = true)
            val args = function.args.joinToString {
                val name = it.cppName
                when {
                    critical && it.type.isString() -> "const char* $name, int32_t _${name}_size"
                    critical && it.type.isArray() -> "const ${it.type.arrayTypeOrNull()!!.toCppType(ptr = false)}* $name, int32_t _${name}_length"
                    else -> "${it.type.toCppType(printOption = false, ptr = true)} $name"
                }
            }
            val castedNames = function.args.map {
                val name = it.cppName
                when {
                    critical && it.type.isString() -> {
                        val newString = "*new (alloca(sizeof(KString))) KString"
                        val newOptional = "*new (alloca(sizeof(KOptional<KString>))) KOptional"

                        if (it.type.isNullable) "_${name}_size == -1 ? $newOptional<KString>() : $newOptional($newString($name, _${name}_size, false))"
                        else "$newString($name, _${name}_size, false)"
                    }
                    critical && it.type.isArray() -> {
                        val type = it.type.arrayTypeOrNull()!!.toCppType(ptr = false)
                        val newArray = "*new (alloca(sizeof(KArray<$type>))) KArray"
                        val newOptional = "*new (alloca(sizeof(KOptional<KArray<$type>>))) KOptional"

                        if(it.type.isNullable) "_${name}_length == -1 ? $newOptional<KArray<$type>>() : $newOptional($newArray(const_cast<$type*>($name), _${name}_length, false))"
                        else "$newArray(const_cast<$type*>($name), _${name}_length, false)"
                    }
                    it.type.isReleasable() && !function.isCritical() ->
                        "std::move(${toCppType(it.type, it.cppName)})"
                    else -> toCppType(it.type, it.cppName)
                }
            }

            if(function.isInterfaceOperation()) {
                val interName = "I" + function.interfaceName().upperCamelCase()
                append("\nLIB_EXPORT $type ${mangledName}($args) {\n\t")
                append(when {
                    function.isInterfaceOperationConstructor() ->
                        "return new std::shared_ptr<$interName>($interName::$name(${castedNames.joinToString()}));"
                    function.isInterfaceOperationFn() -> {
                        val funcName = function.interfaceFunctionName().snakeCase()
                        val self = function.args[0].cppName
                        val argNames = castedNames.drop(1).joinToString()
                        val call = "(*$self)->$funcName($argNames)"
                        if(function.type.isVoid())
                            "$call;"
                        else "return ${toNativeType(function.type, call)};"
                    }
                    function.isInterfaceOperationFree() ->
                        "delete ${function.args[0].cppName};"
                    function.isInterfaceOperationClone() ->
                        "return new std::shared_ptr<$interName>(*${function.args[0].cppName});"
                    function.isInterfaceOperationAddress() ->
                        "return (int64_t) ${function.args[0].cppName}->get();"
                    else -> throw UnsupportedOperationException()
                })
                append("\n}")
            } else {
                val call = "$name(${castedNames.joinToString()})"
                val expr = if(function.type.isVoid())
                    call
                else "return ${toNativeType(function.type, call)}"

                append("""
                    
                    LIB_EXPORT $type $mangledName($args) {
                        $expr;
                    }
                """.trimIndent())
            }
        }
        append("\n")
    }

    private fun toNativeType(type: ResolvedIdlType, content: String): String = when {
        type.isVoid() -> content
        type.isPrimitive() || type.isEnum() ->
            if(type.isNullable) "obj_opt($content, primitive_wrap)"
            else content
        type.isArray() ->
            if (type.isNullable) "obj_opt<${type.toCppType(printOption = false)}>($content, karray_wrap<${type.arrayTypeOrNull()!!.toCppType()}>)"
            else "karray_wrap<${type.arrayTypeOrNull()!!.toCppType()}>($content)"
        type.isInterface() || type.isCallback() ->
            if(type.isNullable) "obj_opt($content, arc_wrap)"
            else "arc_wrap($content)"
        type.isDictionary() ->
            if(type.isNullable) "obj_opt($content, ${type.declaration.name.camelCase().lowercase()}_wrap)"
            else "${type.declaration.name.camelCase().lowercase()}_wrap($content)"
        type.isString() ->
            if(type.isNullable) "obj_opt($content, kstring_wrap)"
            else "kstring_wrap($content)"
        else -> throw UnsupportedOperationException()
    }

    private fun toCppType(type: ResolvedIdlType, content: String): String = when {
        type.isVoid() -> content
        type.isPrimitive() || type.isEnum() ->
            if(type.isNullable) "ptr_opt($content, primitive_unwrap)"
            else content
        type.isArray() ->
            if (type.isNullable) "ptr_opt<${type.toCppType(printOption = false)}>($content, karray_unwrap<${type.arrayTypeOrNull()!!.toCppType()}>)"
            else "karray_unwrap<${type.arrayTypeOrNull()!!.toCppType()}>($content)"
        type.isInterface() || type.isCallback() ->
            if(type.isNullable) "ptr_opt($content, arc_unwrap)"
            else "arc_unwrap($content)"
        type.isDictionary() ->
            if(type.isNullable) "ptr_opt($content, ${type.declaration.name.camelCase().lowercase()}_unwrap)"
            else "${type.declaration.name.camelCase().lowercase()}_unwrap($content)"
        type.isString() ->
            if(type.isNullable) "ptr_opt<KString>($content, kstring_unwrap)"
            else "kstring_unwrap($content)"

        else -> throw UnsupportedOperationException()
    }
}