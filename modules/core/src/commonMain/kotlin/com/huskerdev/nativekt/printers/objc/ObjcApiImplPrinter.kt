package com.huskerdev.nativekt.printers.objc

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.stdlibx.fs.SyncPath
import com.huskerdev.stdlibx.systemLineSeparator
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*

class ObjcApiImplPrinter(
    val context: NativeModuleContext,
    target: SyncPath,
    val includePath: String = "nativekt.h"
) {
    private fun export(name: String) = "__attribute__((visibility(\"default\"))) ${context.mangle(name)}"

    init {
        target.parent!!.createDirectories()
        target.writeString(buildString {
            printHeader()
            printStdLib()
            printBoxedPrimitives()
            printPreDefs()
            printCallbacks()
            printStructs()
            printFunctions()
        }.replace("\n", systemLineSeparator))
    }

    private fun StringBuilder.printHeader() {
        appendLine("""
            #import "$includePath"
            #include <stdlib.h>
            #include <string.h>

            void ${export("init")}(void) {}
        """.trimIndent())
        if (context.operations.isNotEmpty()) appendLine("""

            @implementation NativeKt
            @end
        """.trimIndent())
        if (context.needsAllocFunctions) appendLine("""

            void* ${export("alloc")}(size_t size) {
                return size == 0 ? NULL : malloc(size);
            }

            void ${export("dealloc")}(void* ptr, size_t size) {
                (void)size;
                free(ptr);
            }
        """.trimIndent())
    }

    private fun StringBuilder.printStdLib() {
        if (!context.hasPrimitiveNullable && !context.hasEnumNullable &&
            !context.hasString && !context.hasPrimitiveArray && !context.hasEnumArray &&
            !context.hasObjectArrays && !context.hasPrimitiveNullableArray && !context.hasEnumNullableArray &&
            !context.hasDictionary && !context.hasInterface && !context.hasCallback
        ) return
        printLabel("Types")

        appendLine("""

            static void* arc_wrap(id value) {
                return (__bridge_retained void*) value;
            }

            static id arc_unwrap(void* value) {
                return (__bridge_transfer id) value;
            }
        """.trimIndent())
        if (context.toKotlinTypes.any { it.isReleasable() } || context.hasInterface) appendLine("""

            static void arc_free(void* value) {
                (void) arc_unwrap(value);
            }
        """.trimIndent())

        if (context.hasString) {
            appendLine("""

                // String

                @implementation KTString {
                    char* _data;
                    size_t _size;
                }

                - (instancetype)init {
                    return [self initWithBytes:NULL size:0];
                }

                - (instancetype)initWithBytes:(const char*)bytes size:(size_t)size {
                    return [self initWithBytes:bytes size:size makeCopy:YES];
                }

                - (instancetype)initWithBytes:(const char*)bytes size:(size_t)size makeCopy:(BOOL)makeCopy {
                    NSParameterAssert(size == 0 || bytes != NULL);
                    self = [super init];
                    if (!self) return nil;
                    _size = size;
                    if (makeCopy && size > 0) {
                        _data = malloc(size);
                        if (_data == NULL)
                            [NSException raise:NSMallocException format:@"Cannot allocate string"];
                        memcpy(_data, bytes, size);
                    } else if (!makeCopy) {
                        _data = (char*)bytes;
                    }
                    return self;
                }

                - (instancetype)initWithNSString:(NSString*)string {
                    NSData* data = [string dataUsingEncoding:NSUTF8StringEncoding];
                    if (data == nil)
                        [NSException raise:NSInvalidArgumentException format:@"String cannot be encoded as UTF-8"];
                    return [self initWithBytes:data.bytes size:data.length];
                }

                - (const char*)data {
                    return _data;
                }

                - (size_t)size {
                    return _size;
                }

                - (NSString*)toNSString {
                    return [[NSString alloc] initWithBytes:_data length:_size encoding:NSUTF8StringEncoding];
                }

                - (id)copyWithZone:(NSZone*)zone {
                    (void)zone;
                    return self;
                }

                - (BOOL)isEqual:(id)other {
                    if (self == other)
                        return YES;
                    if (![other isKindOfClass:[KTString class]])
                        return NO;
                    KTString* string = other;
                    return _size == string.size && (_size == 0 || memcmp(_data, string.data, _size) == 0);
                }

                - (NSUInteger)hash {
                    return [self toNSString].hash;
                }

                - (void)dealloc {
                    free(_data);
                }

                @end
            """.trimIndent())
            if (context.hasStringToNative) appendLine("""

                void* ${export("string_new")}(const char* data, int32_t size, bool makeCopy) {
                    @autoreleasepool {
                        return arc_wrap([[KTString alloc] initWithBytes:data size:(size_t)size makeCopy:makeCopy]);
                    }
                }
            """.trimIndent())
            if (context.hasStringToKotlin) appendLine("""

                const char* ${export("string_data")}(void* value) {
                    return ((__bridge KTString*) value).data;
                }

                size_t ${export("string_size")}(void* value) {
                    return ((__bridge KTString*) value).size;
                }

                void ${export("string_free")}(void* value) {
                    @autoreleasepool { arc_free(value); }
                }
            """.trimIndent())
        }

        if (context.hasPrimitiveArray || context.hasEnumArray) {
            appendLine("""

                // Primitive arrays

                @interface KTPrimitiveArray ()
                - (instancetype)initWithBytes:(const void*)bytes
                                       length:(int32_t)length
                                  elementSize:(size_t)elementSize
                                     makeCopy:(BOOL)makeCopy;
                @end

                @implementation KTPrimitiveArray {
                    void* _data;
                    int32_t _length;
                    size_t _elementSize;
                }

                - (instancetype)initWithBytes:(const void*)bytes
                                       length:(int32_t)length
                                  elementSize:(size_t)elementSize
                                     makeCopy:(BOOL)makeCopy {
                    NSParameterAssert(length >= 0 && (length == 0 || bytes != NULL));
                    if ((size_t)length > SIZE_MAX / elementSize) {
                        [NSException raise:NSInvalidArgumentException format:@"Array is too large"];
                    }
                    self = [super init];
                    if (!self) return nil;
                    _length = length;
                    _elementSize = elementSize;
                    size_t size = (size_t)length * elementSize;
                    if (makeCopy && size > 0) {
                        _data = malloc(size);
                        if (_data == NULL)
                            [NSException raise:NSMallocException format:@"Cannot allocate array"];
                        memcpy(_data, bytes, size);
                    } else if (!makeCopy)
                        _data = (void*)bytes;
                    return self;
                }

                - (const void*)data {
                    return _data;
                }

                - (int32_t)length {
                    return _length;
                }

                - (NSArray<NSNumber*>*)toNSArray {
                    [self doesNotRecognizeSelector:_cmd];
                    return nil;
                }

                - (id)copyWithZone:(NSZone*)zone {
                    (void)zone;
                    return [(KTPrimitiveArray*)[[self class] alloc] initWithBytes:_data
                                                                           length:_length
                                                                      elementSize:_elementSize
                                                                         makeCopy:YES];
                }

                - (void)dealloc {
                    free(_data);
                }

                @end
            """.trimIndent())
            context.objcPrimitiveArrayTypes().forEach { (name, type, accessor, toNative, toKotlin) ->
                val lower = name.lowercase()
                appendLine("""

                    @implementation KT${name}Array

                    - (instancetype)init {
                        return [self initWithElements:NULL length:0];
                    }

                    - (instancetype)initWithElements:(const $type*)elements length:(int32_t)length {
                        return [self initWithElements:elements length:length makeCopy:YES];
                    }

                    - (instancetype)initWithElements:(const $type*)elements length:(int32_t)length makeCopy:(BOOL)makeCopy {
                        return [super initWithBytes:elements length:length elementSize:sizeof($type) makeCopy:makeCopy];
                    }

                    - (instancetype)initWithNSArray:(NSArray<NSNumber*>*)array {
                        if (array.count > INT32_MAX || array.count > SIZE_MAX / sizeof($type))
                            [NSException raise:NSInvalidArgumentException format:@"Array is too large"];
                        $type* elements = array.count == 0 ? NULL : malloc(array.count * sizeof($type));
                        if (array.count > 0 && elements == NULL)
                            [NSException raise:NSMallocException format:@"Cannot allocate array"];
                        for (NSUInteger i = 0; i < array.count; ++i)
                            elements[i] = [array[i] $accessor];
                        return [self initWithElements:elements length:(int32_t)array.count makeCopy:NO];
                    }

                    - (const $type*)elements {
                        return self.data;
                    }

                    - (NSArray<NSNumber*>*)toNSArray {
                        NSMutableArray<NSNumber*>* array = [NSMutableArray arrayWithCapacity:(NSUInteger)self.length];
                        const $type* elements = self.elements;
                        for (int32_t i = 0; i < self.length; ++i)
                            [array addObject:@(elements[i])];
                        return [array copy];
                    }

                    @end
                """.trimIndent())
                if (toNative) appendLine("""

                    void* ${export("${lower}array_new")}(const $type* elements, int32_t length, bool makeCopy) {
                        @autoreleasepool { return arc_wrap([[KT${name}Array alloc] initWithElements:elements length:length makeCopy:makeCopy]); }
                    }
                """.trimIndent())
                if (toKotlin) appendLine("""

                    const $type* ${export("${lower}array_elements")}(void* value) {
                        return ((__bridge KT${name}Array*) value).elements;
                    }

                    int32_t ${export("${lower}array_length")}(void* value) {
                        return ((__bridge KT${name}Array*) value).length;
                    }

                    void ${export("${lower}array_free")}(void* value) {
                        @autoreleasepool { arc_free(value); }
                    }
                """.trimIndent())
            }
        }

        if (context.hasObjectArrays || context.hasPrimitiveNullableArray || context.hasEnumNullableArray) {
            appendLine("""

                // Object arrays

                @implementation KTArray {
                    id __strong* _elements;
                    int32_t _length;
                    int32_t _capacity;
                }

                - (instancetype)init {
                    return [self initWithCapacity:0];
                }

                - (instancetype)initWithCapacity:(int32_t)capacity {
                    NSParameterAssert(capacity >= 0);
                    self = [super init];
                    if (!self) return nil;
                    _capacity = capacity;
                    _elements = capacity == 0 ? NULL : (id __strong*) calloc((size_t) capacity, sizeof(id));
                    if (capacity > 0 && _elements == NULL)
                        [NSException raise:NSMallocException format:@"Cannot allocate array"];
                    return self;
                }

                - (instancetype)initWithNSArray:(NSArray*)array {
                    if (array.count > INT32_MAX)
                        [NSException raise:NSInvalidArgumentException format:@"Array is too large"];
                    self = [self initWithCapacity:(int32_t)array.count];
                    if (!self) return nil;
                    for (id source in array) {
                        id element = source;
            """.trimIndent())
            if (context.hasString) appendLine("""
                if ([element isKindOfClass:[NSString class]]) {
                    element = [[KTString alloc] initWithNSString:element];
                }
            """.replaceIndent("\t\t"))
            appendLine("""
                        [self addObject:element];
                    }
                    return self;
                }

                - (int32_t)length {
                    return _length;
                }

                - (NSArray*)elements {
                    NSMutableArray* array = [NSMutableArray arrayWithCapacity:(NSUInteger)_length];
                    for (int32_t i = 0; i < _length; ++i)
                        [array addObject:_elements[i]];
                    return [array copy];
                }

                - (id)objectAtIndexedSubscript:(NSUInteger)index {
                    if (index >= (NSUInteger)_length)
                        [NSException raise:NSRangeException format:@"Array index out of range"];
                    return _elements[index];
                }

                - (void)addObject:(id)object {
                    NSParameterAssert(object != nil);
                    if (_length == INT32_MAX)
                        [NSException raise:NSInvalidArgumentException format:@"Array is too large"];
                    if (_length == _capacity) {
                        int32_t capacity = _capacity < 4 ? 4 : (_capacity > INT32_MAX / 2 ? INT32_MAX : _capacity * 2);
                        id __strong* elements = (id __strong*) calloc((size_t)capacity, sizeof(id));
                        if (elements == NULL)
                            [NSException raise:NSMallocException format:@"Cannot allocate array"];
                        for (int32_t i = 0; i < _length; ++i) {
                            elements[i] = _elements[i];
                            _elements[i] = nil;
                        }
                        free(_elements);
                        _elements = elements;
                        _capacity = capacity;
                    }
                    _elements[_length++] = object;
                }

                - (NSArray*)toNSArray {
                    NSMutableArray* array = [NSMutableArray arrayWithCapacity:(NSUInteger)_length];
                    for (int32_t i = 0; i < _length; ++i) {
                        id element = _elements[i];
            """.trimIndent())
            if (context.hasString) appendLine("""
                if ([element isKindOfClass:[KTString class]])
                element = [element toNSString];
            """.trimIndent().prependIndent("\t\t"))
            appendLine("""
                        [array addObject:element];
                    }
                    return [array copy];
                }

                - (id)copyWithZone:(NSZone*)zone {
                    (void)zone;
                    KTArray* array = [(KTArray*)[[self class] alloc] initWithCapacity:_length];
                    for (int32_t i = 0; i < _length; ++i)
                        [array addObject:_elements[i]];
                    return array;
                }

                - (void)dealloc {
                    for (int32_t i = 0; i < _length; ++i)
                        _elements[i] = nil;
                    free(_elements);
                }

                @end
            """.trimIndent())

            buildList {
                (context.castedDictionaries + context.castedInterfaces)
                    .filter { it in context.usedObjectArray }
                    .mapTo(this) {
                        Triple(it.cname.lowercase(), it in context.usedObjectArrayToNative, it in context.usedObjectArrayToKotlin)
                    }
                add(Triple("string",
                    context.hasStringArrayToNative, context.hasStringArrayToKotlin))
                add(Triple("char",
                    context.hasCharNullableArrayToNative, context.hasCharNullableArrayToKotlin))
                add(Triple("boolean",
                    context.hasBooleanNullableArrayToNative, context.hasBooleanNullableArrayToKotlin))
                add(Triple("byte",
                    context.hasByteNullableArrayToNative || context.hasUByteNullableArrayToNative,
                    context.hasByteNullableArrayToKotlin || context.hasUByteNullableArrayToKotlin))
                add(Triple("short",
                    context.hasShortNullableArrayToNative || context.hasUShortNullableArrayToNative,
                    context.hasShortNullableArrayToKotlin || context.hasUShortNullableArrayToKotlin))
                add(Triple("int",
                    context.hasIntNullableArrayToNative || context.hasUIntNullableArrayToNative || context.hasEnumNullableArrayToNative,
                    context.hasIntNullableArrayToKotlin || context.hasUIntNullableArrayToKotlin || context.hasEnumNullableArrayToKotlin))
                add(Triple("long",
                    context.hasLongNullableArrayToNative || context.hasULongNullableArrayToNative,
                    context.hasLongNullableArrayToKotlin || context.hasULongNullableArrayToKotlin))
                add(Triple("float",
                    context.hasFloatNullableArrayToNative, context.hasFloatNullableArrayToKotlin))
                add(Triple("double",
                    context.hasDoubleNullableArrayToNative, context.hasDoubleNullableArrayToKotlin))
            }.filter { it.second || it.third }.forEach { (name, toNative, toKotlin) ->
                if (toNative) appendLine("""

                    void* ${export("array_${name}_new")}(int32_t capacity, bool nullableElements) {
                        @autoreleasepool {
                            (void) nullableElements;
                            return arc_wrap([[KTArray alloc] initWithCapacity:capacity]);
                        }
                    }
                """.trimIndent())
                if (toKotlin) appendLine("""

                    int32_t ${export("array_${name}_length")}(void* value, bool nullableElements) {
                        (void) nullableElements;
                        return ((__bridge KTArray*) value).length;
                    }
                """.trimIndent())
                if (toNative) appendLine("""

                    void ${export("array_${name}_push")}(void* value, void* element, bool nullableElements) {
                        @autoreleasepool {
                            (void) nullableElements;
                            [(__bridge KTArray*) value addObject:arc_unwrap(element) ?: [NSNull null]];
                        }
                    }
                """.trimIndent())
                if (toKotlin) appendLine("""

                    void* ${export("array_${name}_get")}(void* value, int32_t index, bool nullableElements) {
                        @autoreleasepool {
                            (void) nullableElements;
                            id element = ((__bridge KTArray*) value)[(NSUInteger) index];
                            return element == [NSNull null] ? NULL : (__bridge void*) element;
                        }
                    }

                    void ${export("array_${name}_free")}(void* value, bool nullableElements) {
                        @autoreleasepool {
                            (void) nullableElements;
                            arc_free(value);
                        }
                    }
                """.trimIndent())
            }
        }
    }

    private fun StringBuilder.printBoxedPrimitives() {
        if (!context.hasPrimitiveNullable && !context.hasEnumNullable) return
        printLabel("Boxed primitives")

        context.objcBoxedPrimitiveTypes().forEach { (name, type, accessor, toNative, toKotlin) ->
            if (toNative) appendLine("""

                void* ${export("${name.lowercase()}_new")}($type value) {
                    @autoreleasepool { return arc_wrap(@(value)); }
                }
            """.trimIndent())
            if (toKotlin) appendLine("""

                $type ${export("${name.lowercase()}_get")}(void* raw, bool release) {
                    @autoreleasepool {
                        NSNumber* value = release ? arc_unwrap(raw) : (__bridge NSNumber*) raw;
                        return [value $accessor];
                    }
                }
            """.trimIndent())
        }
    }

    private fun StringBuilder.printPreDefs() {
        if (!context.hasUByteNullableToNative && !context.hasUShortNullableToNative &&
            !context.hasUIntNullableToNative && !context.hasULongNullableToNative
        ) return

        appendLine("""

            static id primitive_unsigned(id value, unsigned bits) {
                if (value == nil || value == [NSNull null]) {
                    return value;
                }
        """.trimIndent())

        // For primitive arrays
        if (context.hasUByteNullableArrayToNative || context.hasUShortNullableArrayToNative ||
            context.hasUIntNullableArrayToNative || context.hasULongNullableArrayToNative
        ) appendLine("""
            if ([value isKindOfClass:[KTArray class]]) {
                KTArray* array = value;
                KTArray* result = [[KTArray alloc] initWithCapacity:array.length];
                for (id element in array.elements) {
                    [result addObject:primitive_unsigned(element, bits)];
                }
                return result;
            }
        """.replaceIndent("\t"))

        // Cast to unsigned
        appendLine("""
                uint64_t number = [value unsignedLongLongValue];
                switch (bits) {
                    case 8: return @((uint8_t) number);
                    case 16: return @((uint16_t) number);
                    case 32: return @((uint32_t) number);
                    default: return @(number);
                }
            }
        """.trimIndent())
    }

    private fun StringBuilder.printCallbacks() {
        if (!context.hasCallback) return
        printLabel("Callbacks")

        context.castedCallbacks.forEach { callback ->
            val name = callback.objcName
            val lower = callback.name.camelCase().lowercase()
            val nativeType = callback.type.toCommonNativeType()
            val nativeArgs = (listOf("size_t") + callback.args.map { it.type.toCommonNativeType() }).joinToString()

            appendLine("""

                @interface $name () {
                    size_t _nktId;
                    int32_t _nktHash;
                    $nativeType (*_nktInvoke)($nativeArgs);
                    bool (*_nktEquals)(size_t, size_t);
                    void (*_nktFree)(size_t);
                }
                - (instancetype)initWithId:(size_t)identifier
                                      hash:(int32_t)hash
                                    invoke:(void*)invoke
                                    equals:(void*)equals
                                      free:(void*)release;
                - (size_t)nktId;
                @end

                @implementation $name

                - (instancetype)initWithId:(size_t)identifier
                                      hash:(int32_t)hash
                                    invoke:(void*)invoke
                                    equals:(void*)equals
                                      free:(void*)release {
                    self = [super init];
                    if (!self) return nil;
                    _nktId = identifier;
                    _nktHash = hash;
                    _nktInvoke = invoke;
                    _nktEquals = equals;
                    _nktFree = release;
                    return self;
                }

                - (size_t)nktId {
                    return _nktId;
                }

                - (NSUInteger)hash {
                    return (NSUInteger) _nktHash;
                }

                - (BOOL)isEqual:(id)other {
                    if (self == other)
                        return YES;
                    if ([other class] != [self class])
                        return NO;
                    return _nktEquals(_nktId, (($name*) other)->_nktId);
                }

                - (void)dealloc {
                    _nktFree(_nktId);
                }
            """.trimIndent())

            val args = (listOf("_nktId") + callback.args.map { toNativeType(it.type, it.objcName) }).joinToString()
            val call = toObjcType(callback.type, "_nktInvoke($args)")
            val ret = if (callback.type.isVoid()) "" else "return "
            appendLine("""

                - (${callback.type.toObjcType()})${objcMethod("invoke", callback.args.map { it.objcName to it.type.toObjcType() })} {
                    $ret$call;
                }

                @end
            """.trimIndent())

            // ABI
            if (callback in context.toNativeDeclaration) appendLine("""

                void* ${export("${lower}_new")}(size_t identifier, int32_t hash, void* invoke, void* equals, void* release) {
                    @autoreleasepool { return arc_wrap([[$name alloc] initWithId:identifier hash:hash invoke:invoke equals:equals free:release]); }
                }
            """.trimIndent())
            if (callback in context.toKotlinDeclaration) appendLine("""

                size_t ${export("${lower}_id")}(void* value) {
                    return [(__bridge $name*)value nktId];
                }

                void ${export("${lower}_free")}(void* value) {
                    @autoreleasepool { arc_free(value); }
                }
            """.trimIndent())
        }
    }

    private fun StringBuilder.printStructs() {
        if (!context.hasDictionary) return
        printLabel("Dictionaries")

        context.castedDictionaries.forEach { dictionary ->
            val name = dictionary.objcName
            val fields = context.allFields[dictionary]!!

            appendLine("\n@implementation $name")

            if (fields.isNotEmpty()) {
                val args = fields.map { it.objcName to it.type.toObjcType() }
                append("""

                    - (instancetype)${objcMethod("initWith${fields.first().objcName.uppercaseFirstChar()}", args)} {
                        self = [super init];
                        if (!self) return nil;
                """.trimIndent())
                fields.joinTo(this@printStructs, separator = "") {
                    "\n\tself.${it.objcName} = ${it.objcName};"
                }
                appendLine("\n\treturn self;\n}\n")
            }
            appendLine("@end")

            // ABI

            val args = fields.joinToString {
                "${it.type.toCommonNativeType()} ${it.objcName}"
            }.ifEmpty { "void" }

            val call = if (fields.isEmpty()) {
                "[$name new]"
            } else {
                val values = fields.map { it.objcName to toObjcType(it.type, it.objcName) }
                objcCall("[$name alloc]", "initWith${fields.first().objcName.uppercaseFirstChar()}", values)
            }

            if (dictionary in context.toNativeDeclaration) appendLine("""

                void* ${export(dictionary.subCFunc("new"))}($args) {
                    @autoreleasepool { return arc_wrap($call); }
                }
            """.trimIndent())
            if (dictionary in context.toKotlinDeclaration) {
                appendLine("""

                    void ${export(dictionary.subCFunc("free"))}(void* value) {
                        @autoreleasepool { arc_free(value); }
                    }
                """.trimIndent())

                fields.forEach { field ->
                    val ref = "((__bridge $name*) value).${field.objcName}"
                    val getter = if (field.type.isObjcObject())
                        "id fieldValue = $ref; return fieldValue == [NSNull null] ? NULL : (__bridge void*)fieldValue;"
                    else "return $ref;"

                    appendLine("""

                        ${field.type.toCommonNativeType()} ${export(dictionary.subFieldCFunc(field))}(void* value) {
                            @autoreleasepool { $getter }
                        }
                    """.trimIndent())
                }
            }
        }
    }

    private fun StringBuilder.printFunctions() {
        if (context.allOperations.isEmpty()) return
        printLabel("Functions")

        context.allOperations.forEach { operation ->
            val type = if (operation.isInterfaceOperationAddress())
                "intptr_t" else operation.type.toCommonNativeType()

            val args = operation.args.joinToString {
                val name = it.objcName
                when {
                    operation.isCritical() && it.type.isString() -> "const char* $name, int32_t _${name}_size"
                    operation.isCritical() && it.type.isArray() -> "const ${it.type.arrayTypeOrNull()!!.toCommonNativeType()}* $name, int32_t _${name}_length"
                    else -> "${it.type.toCommonNativeType()} $name"
                }
            }.ifEmpty { "void" }

            val values = operation.args.map {
                val name = it.objcName
                val value = when {
                    operation.isCritical() && it.type.isString() ->
                        "_${name}_size < 0 ? nil : [[KTString alloc] initWithBytes:$name size:(size_t)_${name}_size]"
                    operation.isCritical() && it.type.isArray() -> {
                        val element = it.type.arrayTypeOrNull()!!
                        val arrayClass = element.objcArrayClassName
                        val elementType = element.toCommonNativeType(ignoreUnsigned = true)
                        "_${name}_length < 0 ? nil : [[$arrayClass alloc] initWithElements:(const $elementType*)$name length:_${name}_length]"
                    }
                    else -> toObjcType(it.type, name)
                }
                name to value
            }

            val self = operation.args.firstOrNull()?.objcName
            val call = when {
                operation.isInterfaceOperationFree() -> "arc_free($self)"
                operation.isInterfaceOperationClone() -> "arc_wrap((__bridge id)$self)"
                operation.isInterfaceOperationAddress() -> "(intptr_t)$self"
                operation.isInterfaceOperationConstructor() -> toNativeType(operation.type, objcCall(operation.getInterface(context)!!.objcName, operation.objcName, values))
                operation.isInterfaceOperationFn() -> toNativeType(operation.type, objcCall("(__bridge ${operation.getInterface(context)!!.objcName}*)$self", operation.objcName, values.drop(1)))
                else -> toNativeType(operation.type, objcCall("NativeKt", operation.objcName, values))
            }

            val ret = if (operation.type.isVoid()) "" else "return "
            val body = if(operation.isInterfaceOperationAddress())
                "$ret$call;"
            else "@autoreleasepool { $ret$call; }"
            appendLine("""

                $type __attribute__((visibility("default"))) ${operation.cnameMangled(context)}($args) {
                    $body
                }
            """.trimIndent())
        }
    }

    private fun toNativeType(type: ResolvedIdlType, content: String) =
        if (type.isObjcObject()) "arc_wrap($content)" else content

    private fun toObjcType(type: ResolvedIdlType, content: String): String {
        if (!type.isObjcObject()) return content

        val ownedObject = "arc_unwrap($content)"
        val convertedObject = when {
            type.isNullablePrimitive() -> normalizeUnsigned(type, ownedObject)
            type.isArray() -> type.arrayType { arrayType ->
                if (arrayType.isNullablePrimitive())
                    normalizeUnsigned(arrayType, ownedObject)
                else ownedObject
            }
            else -> ownedObject
        }

        val objcType = type.toObjcType(printNullable = false)
        return "($objcType) $convertedObject"
    }

    private fun normalizeUnsigned(type: ResolvedIdlType, content: String): String = when {
        type.isUByte() -> "primitive_unsigned($content, 8)"
        type.isUShort() -> "primitive_unsigned($content, 16)"
        type.isUInt() -> "primitive_unsigned($content, 32)"
        type.isULong() -> "primitive_unsigned($content, 64)"
        else -> content
    }
}
