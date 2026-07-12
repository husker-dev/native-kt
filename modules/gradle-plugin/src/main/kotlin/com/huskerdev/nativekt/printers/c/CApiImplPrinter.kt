package com.huskerdev.nativekt.printers.c

import com.huskerdev.nativekt.plugin.Language
import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlDictionary
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import java.io.File

class CApiImplPrinter(
    idl: IdlResolver,
    target: File,
    val language: Language,
    val classPath: String,
    val moduleName: String
) {
    init {
        target.parentFile.mkdirs()
        val builder = StringBuilder()

        val headerExtension = language.headerExtension ?: "h"

        builder.append("""
            #include "api.$headerExtension"
            
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
                
                void ${mangle("abstract_callback_free")}(_AbstractCallback* self) {
                    if(self == NULL) return;
                    self->free(self);
                }
                
                void ${mangle("abstract_callback_free_forced")}(_AbstractCallback* self) {
                    if(self == NULL) return;
                    self->__flags |= K_FLAG_RELEASABLE;
                    self->free(self);
                }
                
            """.trimIndent())
        }

        printLabel(builder, "Functions")
        idl.allOperators().forEach {
            printFunctionProxy(builder, it)

            // Critical wrappers
            if(it.isCriticalCapable() && (it.hasString() || it.hasArray())) {
                builder.append("\n")
                printCriticalNativeFunctionContent(
                    builder, language, classPath, moduleName,
                    name = "c_${it.cnameMangled(classPath, moduleName)}",
                    function = it
                )
            }
        }

        target.writeText(builder.toString().replace("\n", System.lineSeparator()))
    }

    private fun mangle(name: String) =
        mangle(classPath, moduleName, "_$name")

    private fun printFunctionProxy(
        builder: StringBuilder,
        function: ResolvedIdlOperation
    ) = builder.apply {
        val name = function.cname
        val mangledName = function.cnameMangled(classPath, moduleName)
        val type = function.type.toCType(printNullable = true)
        val args = function.args.joinToString {
            "${it.type.toCType(printNullable = true)} ${it.cname}"
        }
        val argNames = function.args.map { it.cname }
        val ret = if(function.type.isVoid()) "" else "return "

        if(language == Language.CPP || language == Language.C) {
            append("\n")
            if(function.isInterfaceOperation()) {
                when (language) {
                    Language.CPP -> {
                        val interName = "I" + function.interfaceName().upperCamelCase()
                        append(when {
                            function.isInterfaceOperationConstructor() -> """
                                void* ${mangledName}($args) {
                                    return new std::shared_ptr<$interName>($interName::_create(${argNames.joinToString()}));
                                }
                            """.trimIndent()
                            function.isInterfaceOperationFn() -> {
                                val funcName = function.interfaceFunctionName().snakeCase()
                                val self = argNames[0]
                                val argNames = argNames.drop(1).joinToString()
                                """
                                    $type ${mangledName}($args) {
                                        (*static_cast<std::shared_ptr<$interName>*>($self))->$funcName($argNames);
                                    }
                                """.trimIndent()
                            }
                            function.isInterfaceOperationFree() -> """
                                void ${mangledName}($args) {
                                    delete static_cast<std::shared_ptr<$interName>*>(${argNames.joinToString()});
                                }
                            """.trimIndent()
                            else -> throw UnsupportedOperationException()
                        })
                    }
                    Language.C -> {
                        append("""
                            $type $mangledName($args) {
                                $ret$name(${argNames.joinToString()});
                            }
                        """.trimIndent())
                    }
                    else -> Unit
                }
            } else {
                append("""
                    $type $mangledName($args) {
                        $ret$name(${argNames.joinToString()});
                    }
                """.trimIndent())
            }
            append("\n")
        }
    }

    private fun printStructNew(
        builder: StringBuilder,
        dictionary: ResolvedIdlDictionary
    ) = builder.apply {
        val name = dictionary.cname
        val fields = dictionary.allFields()
        val args = fields.map { field ->
            val const = if(field.type.isPrimitive())
                "const " else ""
            "$const${field.type.toCType()} ${field.cname}"
        }
        val structFields = buildMap {
            putAll(fields.map { it.cname to it.cname })
            this["__flags"] = "K_FLAG_RELEASABLE"
        }

        val funcNew = dictionary.subCFunc(classPath, moduleName, "new")

        when (language) {
            Language.CPP -> {
                // c++ func

                append("\n$name::$name(")
                args.joinTo(builder, separator = ",") { "\n\t$it" }
                append("\n): ")
                structFields
                    .map { "${it.key}(${it.value})" }
                    .joinTo(builder)
                append(" {}\n")

                // mangled func
                append("""
                    
                    $name* $funcNew(${args.joinToString()}) {
                        return new $name(${fields.joinToString { it.cname }});
                    }
                    
                """.trimIndent())
            }
            else -> {
                append("""
            
                    $name* $funcNew(${args.joinToString()}) {
                        $name* result = ($name*) malloc(sizeof($name));
                        *result = ($name) { ${structFields.map { it.value }.joinToString()} };
                        return result;
                    }
                    
                """.trimIndent())
                if(language == Language.C) append("""
                    
                    $name* ${name}_new(${args.joinToString()}) {
                        return $funcNew(${fields.joinToString { it.cname }});
                    }
                    
                """.trimIndent())
            }
        }
    }

    private fun printStructClone(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val name = dictionary.cname
        val fields = dictionary.allFields()
        val structFields = fields.joinToString { field ->
            cloneFuncFor(field.type, "self->${field.cname}")
        }

        val funcClone = dictionary.subCFunc(classPath, moduleName, "clone")
        val funcNew = dictionary.subCFunc(classPath, moduleName, "new")

        when (language) {
            Language.CPP -> append("""
                
                $name* $name::clone() const {
                    return $funcClone(this);
                }
                
                template <> auto _clone_ptr<$name> = (void*)($funcClone);
                
                $name* $funcClone(const $name* self) {
                    if(self == nullptr) 
                        return nullptr;
                    return $funcNew($structFields);
                }
                
            """.trimIndent())
            else -> {
                append("""
                    
                    $name* $funcClone(const $name* self) {
                        if(self == NULL) return NULL;
                        return $funcNew($structFields);
                    }
                    
                """.trimIndent())
                if(language == Language.C) append("""
                    
                    $name* ${name}_clone(const $name* self) {
                        return $funcClone(self);
                    }
                    
                """.trimIndent())
            }
        }
    }

    private fun printStructFree(builder: StringBuilder, dictionary: ResolvedIdlDictionary) = builder.apply {
        val name = dictionary.cname
        val fields = dictionary.allFields()
        val funcFree = dictionary.subCFunc(classPath, moduleName, "free")
        val funcFreeForced = dictionary.subCFunc(classPath, moduleName, "free_forced")

        val freeFunctions = fields.mapNotNull { field ->
            freeFuncFor(
                classPath, moduleName,
                field.type,
                "self->${field.cname}"
            )
        }
        val forceFreeFunctions = fields.mapNotNull { field ->
            forceFreeFuncFor(
                classPath, moduleName,
                field.type,
                "self->${field.cname}"
            )
        }

        when (language) {
            Language.CPP -> {

                // c++ function
                append("""
                    
                    void $name::destroy() {
                        $funcFree(this);
                    }
                    
                    template <> auto _free_ptr<$name> = (void*)($funcFree);
                    
                """.trimIndent())

                // mangled function
                // free
                append("""
                    
                    void $funcFree($name* self) {
                        if (self == nullptr)
                            return;
                """.trimIndent())
                freeFunctions.joinTo(builder, separator = "") { "\n\t$it;" }
                append("""
            
                        if(K_OBJECT_IS_RELEASABLE(self->__flags))
                            free(self);
                    }
                    
                """.trimIndent())

                // force free
                append("""
                    
                    void $funcFreeForced($name* self) {
                        if (self == nullptr)
                            return;
                """.trimIndent())
                forceFreeFunctions.joinTo(builder, separator = "") { "\n\t$it;" }
                append("""
            
                        if(K_OBJECT_IS_RELEASABLE(self->__flags))
                            free(self);
                    }
                    
                """.trimIndent())
            }
            else -> {
                // free
                append("""
                    
                    void $funcFree($name* self) {
                        if (self == NULL)
                            return;
                """.trimIndent())
                freeFunctions.joinTo(builder, separator = "") { "\n\t$it;" }
                append("""
            
                        if(K_OBJECT_IS_RELEASABLE(self->__flags))
                            free((void*) self);
                    }
                """.trimIndent())

                // force free
                append("""
                    
                    void $funcFreeForced($name* self) {
                        if (self == NULL)
                            return;
                """.trimIndent())
                forceFreeFunctions.joinTo(builder, separator = "") { "\n\t$it;" }
                append("""
            
                        if(K_OBJECT_IS_RELEASABLE(self->__flags))
                            free(self);
                    }
                """.trimIndent())

                if (language == Language.C) append("""
                
                    void ${name}_free($name* self) {
                        $funcFree(self);
                    }
                    
                    void ${name}_free_forced($name* self) {
                        $funcFreeForced(self);
                    }
                    
                """.trimIndent())
            }
        }
    }

    private fun printStdLib(builder: StringBuilder) = builder.apply {
        printLabel(builder, "stdlib")

        append("\n// String\n")

        when (language) {
            Language.CPP -> append("""
                
                template <typename T> constexpr void (*_free_ptr)(void*) = nullptr;
                template <typename T> constexpr void* (*_clone_ptr)(void*) = nullptr;
                
                KString::KString(
                    const char* data,
                    const KInt length,
                    const size_t size,
                    const bool is_data_owner
                ): data(data), size(size), length(length), __flags(K_FLAG_RELEASABLE | (is_data_owner ? K_FLAG_DATA_OWNER : 0)) {}

                KString* KString::clone() const {
                    return nativekt_natives_testcpp_testcpp__kstring_clone(this);
                }

                void KString::destroy() {
                    ${mangle("kstring_free")}(this);
                }
                
                template <> auto _free_ptr<KString> = (void*)(${mangle("kstring_free")});
                template <> auto _clone_ptr<KString> = (void*)(${mangle("kstring_clone")});
                
                KString* ${mangle("kstring_new")}(const char* data, const KInt length, const size_t size, const bool is_data_owner) {
                    return new KString(data, length, size, is_data_owner);
                }

                KString* ${mangle("kstring_clone")}(const KString* self) {
                    if (self == nullptr) return nullptr;
                    const KInt size = self->size;
                    void* data = malloc(size);
                    memcpy(data, self->data, size);
                    return ${mangle("kstring_new")}(static_cast<const char*>(data), size, self->length, true);
                }

                void ${mangle("kstring_free")}(KString* self) {
                    if (self == nullptr)
                        return;
                    if (K_OBJECT_IS_DATA_OWNER(self->__flags))
                        free(const_cast<char*>(self->data));
                    if (K_OBJECT_IS_RELEASABLE(self->__flags))
                        free(self);
                }

                void ${mangle("kstring_free_forced")}(KString* self) {
                    if (self == nullptr) return;
                    self->__flags |= K_FLAG_RELEASABLE;
                    ${mangle("kstring_free")}(self);
                }
                
            """.trimIndent())
            else -> {
                append("""
                    
                    KString* ${mangle("kstring_new")}(const char* data, const KInt length, const size_t size, const bool is_data_owner) {
                        KString* result = (KString*) malloc(sizeof(KString));
                        *result = (KString) { data, size, length, K_FLAG_RELEASABLE | (is_data_owner ? K_FLAG_DATA_OWNER : 0) };
                        return result;
                    }
                    
                    KString* ${mangle("kstring_clone")}(const KString* of) {
                        if (of == NULL) return NULL;
                        const KInt size = of->size;
                        void* data = malloc(size);
                        memcpy(data, of->data, size);
                        return ${mangle("kstring_new")}((const char*) data, size, of->length, true);
                    }
                    
                    void ${mangle("kstring_free")}(KString* self) {
                        if (self == NULL)
                            return;
                        if (K_OBJECT_IS_DATA_OWNER(self->__flags))
                            free((void*) self->data);
                        if (K_OBJECT_IS_RELEASABLE(self->__flags))
                            free((void*) self);
                    }
                    
                    void ${mangle("kstring_free_forced")}(KString* self) {
                        if (self == NULL) return;
                        self->__flags |= K_FLAG_RELEASABLE;
                        ${mangle("kstring_free")}(self);
                    }
                    
                """.trimIndent())
                if(language == Language.C) append("""
                    KString* KString_new(const char* data, const KInt length, const size_t size, const bool is_data_owner) {
                        return ${mangle("kstring_new")}(data, length, size, is_data_owner);
                    }
                    
                    KString* KString_clone(const KString* of) {
                        return ${mangle("kstring_clone")}(of);
                    }
                    
                    void KString_free(KString* self) {
                        ${mangle("kstring_free")}(self);
                    }
                """.trimIndent())
            }
        }

        // Primitive arrays

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
            Triple("KDoubleArray", "KDouble", "double")
        ).forEach {
            val name = it.first
            val type = it.second
            val varargType = it.third
            val lowerName = name.snakeCase()

            when (language) {
                Language.CPP -> append("""
                    
                    // $name
                    
                    $name::$name(
                        const $type* elements,
                        const KInt length,
                        const bool is_data_owner
                    ): elements(elements), size(length * sizeof($type)), length(length), __flags(K_FLAG_RELEASABLE | (is_data_owner ? K_FLAG_DATA_OWNER : 0)) {}

                    $name* $name::clone() const {
                        return ${mangle("${lowerName}_clone")}(this);
                    }

                    $name* $name::of(const std::initializer_list<$type> elements) {
                        const size_t size = elements.size() * sizeof($type);
                        const auto data = malloc(size);
                        memcpy(data, elements.begin(), size);
                        return ${mangle("${lowerName}_new")}(static_cast<$type*>(data), elements.size(), true);
                    }

                    void $name::destroy() {
                        ${mangle("${lowerName}_free")}(this);
                    }
                    
                    $name* ${mangle("${lowerName}_new")}(
                        const $type* elements,
                        const KInt length,
                        const bool is_data_owner
                    ) {
                        return new $name(elements, length, is_data_owner);
                    }

                    $name* ${mangle("${lowerName}_clone")}(const $name* self) {
                        if(self == nullptr) return nullptr;
                        const KInt size = self->size;
                        void* elements = malloc(size);
                        memcpy(elements, self->elements, size);
                        return ${mangle("${lowerName}_new")}(static_cast<$type*>(elements), self->length, true);
                    }

                    void ${mangle("${lowerName}_free")}($name* self) {
                        if (self == nullptr)
                            return;
                        if (K_OBJECT_IS_DATA_OWNER(self->__flags))
                            free(const_cast<$type*>(self->elements));
                        if (K_OBJECT_IS_RELEASABLE(self->__flags))
                            free(self);
                    }

                    void ${mangle("${lowerName}_free_forced")}($name* self) {
                        if (self == nullptr) return;
                        self->__flags |= K_FLAG_RELEASABLE;
                        ${mangle("${lowerName}_free")}(self);
                    }
                    
                """.trimIndent())
                else -> {
                    append("""
                        
                        // $name
                
                        $name* ${mangle("${lowerName}_new")}(
                            const $type* elements,
                            const KInt length,
                            const bool is_data_owner
                        ) {
                            $name* result = ($name*) malloc(sizeof($name));
                            *result = ($name){
                                elements,
                                length * sizeof($type),
                                length,
                                K_FLAG_RELEASABLE | (is_data_owner ? K_FLAG_DATA_OWNER : 0)
                            };
                            return result;
                        }
                        
                        $name* ${mangle("${lowerName}_clone")}(const $name* of) {
                            if(of == NULL) return NULL;
                            const KInt size = of->size;
                            void** elements = malloc(size);
                            memcpy(elements, (void*) of->elements, size);
                            return ${mangle("${lowerName}_new")}(($type*) elements, of->length, true);
                        }
                        
                        void ${mangle("${lowerName}_free")}($name* self) {
                            if (self == NULL)
                                return;
                            if (K_OBJECT_IS_DATA_OWNER(self->__flags))
                                free((void*) self->elements);
                            if (K_OBJECT_IS_RELEASABLE(self->__flags))
                                free((void*) self);
                        }
                        
                        void ${mangle("${lowerName}_free_forced")}($name* self) {
                            if (self == NULL) return;
                            self->__flags |= K_FLAG_RELEASABLE;
                            ${mangle("${lowerName}_free")}(self);
                        }
                        
                    """.trimIndent())
                    if(language == Language.C) append("""
                        
                        $name* ${name}_new(const $type* elements, const KInt length, const bool is_data_owner) {
                            return ${mangle("${lowerName}_new")}(elements, length, is_data_owner);
                        }
                        
                        $name* ${name}_of_n(const int n, ...) {
                            va_list args;
                            va_start(args, n);
                            $type* elements = ($type*) malloc(n * sizeof($type));
                            for (int i = 0; i < n; i++)
                                elements[i] = ($type) va_arg(args, $varargType);
                            va_end(args);
                            return ${mangle("${lowerName}_new")}((const $type*) elements, (KInt) n, true);
                        }
                        
                        $name* ${name}_clone(const $name* of) {
                            return ${mangle("${lowerName}_clone")}(of);
                        }
                        
                        void ${name}_free($name* self) {
                            ${mangle("${lowerName}_free")}(self);
                        }
                        
                        void ${name}_free_forced($name* self) {
                            ${mangle("${lowerName}_free_forced")}(self);
                        }
                        
                    """.trimIndent())
                }
            }
        }

        // Object array
        when (language) {
            Language.CPP -> append("""
                
                KArray::KArray(
                    const void** elements,
                    const KInt length,
                    const bool is_data_owner
                ): elements(elements), size(length * sizeof(void*)), length(length), __flags(K_FLAG_RELEASABLE | (is_data_owner ? K_FLAG_DATA_OWNER : 0)) {}

                KArray* KArray::of(const std::initializer_list<void*> elements) {
                    const size_t size = elements.size() * sizeof(void*);
                    const auto data = static_cast<const void**>(malloc(size));
                    memcpy(data, elements.begin(), size);
                    return ${mangle("karray_new")}(data, elements.size(), true);
                }

                template <typename T>
                KArray* KArray::clone() const {
                    return ${mangle("karray_clone")}(this, _clone_ptr<T>);
                }

                template <typename T> 
                void KArray::destroy() {
                    return ${mangle("karray_free")}(this, _free_ptr<T>);
                }
                
                KArray* ${mangle("karray_new")}(
                    const void** elements,
                    const KInt length,
                    const bool is_data_owner
                ) {
                    return new KArray(elements, length, is_data_owner);
                }

                KArray* ${mangle("karray_clone")}(
                    const KArray* _Nullable self,
                    void* _Nullable (* _Nullable clone_op)(void* _Nullable)
                ) {
                    if(self == nullptr) 
                        return nullptr;
                    const auto elements = static_cast<const void**>(malloc(self->size));
                    for (int i = 0; i < self->length; i++) {
                        const auto element = const_cast<void*>(self->elements[i]);
                        elements[i] = element == nullptr ? nullptr : clone_op(element);
                    }
                    return ${mangle("karray_new")}(elements, self->length, true);
                }

                void ${mangle("karray_free")}(
                    KArray* _Nullable self,
                    void (* _Nonnull free_op)(void* _Nonnull)
                ) {
                    if (self == nullptr)
                        return;
                    if (K_OBJECT_IS_DATA_OWNER(self->__flags)) {
                        const void** elements = self->elements;
                        for (int i = 0; i < self->length; i++) {
                            const auto element = const_cast<void*>(elements[i]);
                            if(element == nullptr)
                                continue;
                            free_op(element);
                        }
                        free(elements);
                    }
                    if (K_OBJECT_IS_RELEASABLE(self->__flags))
                        free(self);
                }

                void ${mangle("karray_free_forced")}(
                    KArray* self, 
                    void (*free_op)(void*)
                ) {
                    if(self == nullptr) 
                        return;
                    self->__flags |= K_FLAG_RELEASABLE;
                    ${mangle("karray_free")}(self, free_op);
                }
                
            """.trimIndent())
            else -> {
                append("""
                    
                    KArray* ${mangle("karray_new")}(
                        const void** elements,
                        const KInt length,
                        const bool is_data_owner
                    ) {
                        KArray* result = (KArray*) malloc(sizeof(KArray));
                        *result = (KArray){
                            elements,
                            length * sizeof(KArray),
                            length,
                            K_FLAG_RELEASABLE | (is_data_owner ? K_FLAG_DATA_OWNER : 0)
                        };
                        return result;
                    }
                    
                    KArray* ${mangle("karray_clone")}(const KArray* _Nullable self, void* _Nullable (* _Nullable clone_op)(void* _Nullable)) {
                        if(self == NULL) return NULL;
                        const KInt size = self->size;
                        void** elements = malloc(size);
                        for (int i = 0; i < self->length; i++) {
                            void* element = (void*) self->elements[i];
                            elements[i] = element == NULL ? NULL : clone_op(element);
                        }
                        return ${mangle("karray_new")}((const void**) elements, self->length, true);
                    }
        
                    void ${mangle("karray_free")}(KArray* _Nullable self, void (* _Nonnull free_op)(void* _Nonnull)) {
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
                    
                    void ${mangle("karray_free_forced")}(KArray* self, void (*free_op)(void*)) {
                        if(self == NULL) return;
                        self->__flags |= K_FLAG_RELEASABLE;
                        ${mangle("karray_free")}(self, free_op);
                    }
                    
                """.trimIndent())
                if(language == Language.C) append("""
                    KArray* KArray_new(const void** elements, const KInt length, const bool is_data_owner) {
                        return ${mangle("karray_new")}(elements, length, is_data_owner);
                    }
                    
                    KArray* KArray_of_n(const int n, ...) {
                        va_list args;
                        va_start(args, n);
                        void** elements = (void**) malloc(n * sizeof(void*));
                        for (int i = 0; i < n; i++)
                            elements[i] = (void*) va_arg(args, void*);
                        va_end(args);
                        return ${mangle("karray_new")}((const void**) elements, (KInt) n, true);
                    }
                    
                    KArray* KArray_clone(const KArray* _Nullable self, void* _Nullable (* _Nullable clone_op)(void* _Nullable)) {
                        return ${mangle("karray_clone")}(self, clone_op);
                    }
                    
                    void KArray_free(KArray* _Nullable self, void (* _Nonnull free_op)(void* _Nonnull)) {
                        ${mangle("karray_free")}(self, free_op);
                    }
                    
                    void KArray_free_forced(KArray* self, void (*free_op)(void*)) {
                        ${mangle("karray_free_forced")}(self, free_op);
                    }
                """.trimIndent())
            }
        }
    }

    private fun cloneFuncFor(
        type: ResolvedIdlType,
        content: String
    ): String = when {
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() -> "${mangle("${type.toCType(ptr = false).lowercase()}_array_clone")}($content)"
                type.isEnum() -> "${mangle("kint_array_clone")}($content)"
                else -> "${mangle("karray_clone")}($content, (void*(*)(void*)) ${cloneFuncFor(type, "").dropLast(2)})"
            }
        }
        type.isCallback() -> "$content->clone($content)"
        type.isDictionary() || type.isString() -> "${mangle("${type.toCType(ptr = false).lowercase()}_clone")}($content)"
        else -> content
    }
}

internal fun freeFuncFor(
    classPath: String,
    moduleName: String,
    type: ResolvedIdlType,
    content: String
): String? {
    fun mangle(name: String) = mangle(classPath, moduleName, "_$name")
    return when {
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() -> "${mangle("${type.toCType(ptr = false).lowercase()}_array_free")}($content)"
                type.isEnum() -> "${mangle("kint_array_free")}($content)"
                else -> "${mangle("karray_free")}($content, (void(*)(void*)) ${freeFuncFor(classPath, moduleName, type, "")!!.dropLast(2)})"
            }
        }
        type.isCallback() -> "${mangle("abstract_callback_free")}((_AbstractCallback*) $content)"
        type.isDictionary() || type.isString() -> "${mangle("${type.toCType(ptr = false).lowercase()}_free")}($content)"
        else -> null
    }
}

internal fun forceFreeFuncFor(
    classPath: String,
    moduleName: String,
    type: ResolvedIdlType,
    content: String
): String? {
    fun mangle(name: String) = mangle(classPath, moduleName, "_$name")
    return when {
        type.isArray() -> type.arrayType { type ->
            when {
                type.isPrimitive() -> "${mangle("${type.toCType().lowercase()}_array_free_forced")}($content)"
                type.isEnum() -> "${mangle("kint_array_free_forced")}($content)"
                else -> "${mangle("karray_free_forced")}($content, (void(*)(void*)) ${forceFreeFuncFor(classPath, moduleName, type, "")!!.dropLast(2)})"
            }
        }
        type.isCallback() -> "${mangle("abstract_callback_free_forced")}((_AbstractCallback*) $content)"
        type.isDictionary() || type.isString() -> "${mangle("${type.toCType(ptr = false).lowercase()}_free_forced")}($content)"
        else -> null
    }
}

internal fun printCriticalNativeFunctionContent(
    builder: StringBuilder,
    language: Language,
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
        val name = it.cname
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
        val name = it.cname
        when {
            it.type.isString() -> append(when (language) {
                Language.CPP -> """
                    
                    const auto _arg_$name = static_cast<KString*>(alloca(sizeof(KString)));
                    _arg_$name->data = _arr_$name;
                    _arg_$name->size = _size_$name;
                    _arg_$name->length = _length_$name;
                    _arg_$name->__flags = 0;
                """.replaceIndent("\t")
                else -> """
                    
                    KString* _arg_$name = (KString*) alloca(sizeof(KString));
                    *_arg_$name = (KString) { _arr_$name, _size_$name, _length_$name, 0 };
                """.replaceIndent("\t")
            })
            it.type.isArray() -> {
                val type = it.type.toCType(enumAsInt = true, ptr = false)
                append(when (language) {
                    Language.CPP -> """
                        
                        const auto _arg_$name = static_cast<$type*>(alloca(sizeof($type)));
                        _arg_$name->elements = _arr_$name;
                        _arg_$name->size = sizeof(_arr_$name[0]) * _length_$name;
                        _arg_$name->length = _length_$name;
                        _arg_$name->__flags = 0;
                    """.replaceIndent("\t")
                    else -> """
                        
                        $type* _arg_$name = ($type*) alloca(sizeof($type));
                        *_arg_$name = ($type) { _arr_$name, sizeof(_arr_$name[0]) * _length_$name, _length_$name, 0 };
                    """.replaceIndent("\t")
                })
            }
        }
    }

    // == Call args ==
    val args = function.args.joinToString {
        val name = it.cname
        val type = it.type
        when {
            type.isNullable && (type.isString() || type.isArray()) ->
                "_length_$name == -1 ? 0 : _arg_$name"
            type.isEnum() && language == Language.CPP ->
                "static_cast<${type.declaration.cname}>(_arg_$name)"
            else -> "_arg_$name"
        }
    }

    // == Function call ==
    append("\n\t")
    if(function.type !is ResolvedIdlType.Void)
        append("return ")

    val call = "${function.cnameMangled(classPath, moduleName)}($args)"
    append(call)
    append(";\n}\n")
}