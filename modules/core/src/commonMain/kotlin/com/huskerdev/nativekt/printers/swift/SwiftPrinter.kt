package com.huskerdev.nativekt.printers.swift

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.printers.c.*
import com.huskerdev.nativekt.utils.*
import com.huskerdev.stdlibx.fs.*
import com.huskerdev.webidl.*
import com.huskerdev.webidl.resolver.*
import kotlin.collections.drop

class SwiftPrinter(
    val context: NativeModuleContext,
    target: SyncPath,
) {
    init {
        target.parent!!.createDirectories()
        target.writeString(buildString {
            printHeader()
            printStdLib()
            printDictionaries()
            printCallbacks()
            printEnums()
            printFunctions()
        })
    }

    private fun StringBuilder.printHeader() {
        appendLine("""
            
            @_cdecl("${context.mangle("init")}")
            public func ___init() { }
        """.trimIndent())

        if(context.usedTypes.any { it.isReleasable() }
            || context.hasPrimitiveNullable
            || context.hasEnumNullable
        ) appendLine("""
            
            func fromRaw<T>(
                _ raw: UnsafeMutableRawPointer,
                as type: T.Type
            ) -> T {
                let pointer = raw.assumingMemoryBound(to: T.self)
                let value = pointer.move()
                pointer.deallocate()
                return value
            }
            
            func intoRaw<T>(_ value: T) -> UnsafeMutableRawPointer {
                let pointer = UnsafeMutablePointer<T>.allocate(capacity: 1)
                pointer.initialize(to: value)
                return UnsafeMutableRawPointer(pointer)
            }
        """.trimIndent())

        if(context.hasNullableToNative) appendLine("""
            
            func fromRawOpt<T>(
                _ raw: UnsafeMutableRawPointer?,
                as type: T.Type
            ) -> T? {
                guard let raw else { return nil }
                return fromRaw(raw, as: type)
            }
        """.trimIndent())
        if(context.hasNullableToKotlin) appendLine("""
            
            func intoRawOpt<T>(_ value: T?) -> UnsafeMutableRawPointer? {
                guard let value else { return nil }
                return intoRaw(value)
            }
        """.trimIndent())

        if(context.needsAllocFunctions) appendLine("""
            
            @_cdecl("${context.mangle("alloc")}")
            public func ___alloc(size: Int) -> UnsafeMutableRawPointer {
                return UnsafeMutableRawPointer.allocate(byteCount: size, alignment: 1)
            }

            @_cdecl("${context.mangle("dealloc")}")
            public func ___dealloc(of: UnsafeMutableRawPointer) {
                of.deallocate()
            }
        """.trimIndent())
    }

    private fun StringBuilder.printStdLib() {
        if(context.hasString) {
            printLabel("String")

            if(context.hasStringToNative) appendLine("""
                
                @_cdecl("${context.mangle("string_new")}")
                public func ___stringNew(data: UnsafePointer<UInt8>, size: Int32, makeCopy: Bool) -> UnsafeMutableRawPointer {
                    defer { if(!makeCopy) { data.deallocate() } }
                    return intoRaw(String(decoding: UnsafeBufferPointer(start: data, count: Int(size)), as: UTF8.self))
                }
            """.trimIndent())
            if(context.hasStringToKotlin) appendLine("""
                
                @_cdecl("${context.mangle("string_data")}")
                public func ___string_data(self: UnsafeMutableRawPointer) -> UnsafePointer<UInt8>? {
                    let handle = self.assumingMemoryBound(to: String.self)
                    handle.pointee.reserveCapacity(16)
                    return handle.pointee.withUTF8 { $0.baseAddress }
                }
                
                @_cdecl("${context.mangle("string_size")}")
                public func ___string_size(self: UnsafeMutableRawPointer) -> Int {
                    return self.assumingMemoryBound(to: String.self).pointee.utf8.count
                }
            """.trimIndent())
            appendLine("""
                
                @_cdecl("${context.mangle("string_free")}")
                public func ___string_free(self: UnsafeMutableRawPointer) {
                    let _ = fromRaw(self, as: String.self)
                }
            """.trimIndent())
        }

        if(context.hasPrimitiveArray) {
            printLabel("Primitive arrays")

            listOf(
                Triple("char" to "UInt16",
                    context.hasCharArrayToNative,
                    context.hasCharArrayToKotlin),
                Triple("boolean" to "Bool",
                    context.hasBooleanArrayToNative,
                    context.hasBooleanArrayToKotlin),
                Triple("byte" to "Int8",
                    context.hasByteArrayToNative || context.hasUByteArrayToNative,
                    context.hasByteArrayToKotlin || context.hasUByteArrayToKotlin),
                Triple("ubyte" to "UInt8",
                    context.hasUByteArrayToNative,
                    context.hasUByteArrayToKotlin),
                Triple("short" to "Int16",
                    context.hasShortArrayToNative || context.hasUShortArrayToNative,
                    context.hasShortArrayToKotlin || context.hasUShortArrayToKotlin),
                Triple("ushort" to "UInt16",
                    context.hasUShortArrayToNative,
                    context.hasUShortArrayToKotlin),
                Triple("int" to "Int32",
                    context.hasIntArrayToNative || context.hasEnumArrayToNative || context.hasUIntArrayToNative,
                    context.hasIntArrayToKotlin || context.hasEnumArrayToKotlin || context.hasUIntArrayToKotlin),
                Triple("uint" to "UInt32",
                    context.hasUIntArrayToNative,
                    context.hasUIntArrayToKotlin),
                Triple("long" to "Int64",
                    context.hasLongArrayToNative || context.hasULongArrayToNative,
                    context.hasLongArrayToKotlin || context.hasULongArrayToKotlin),
                Triple("ulong" to "UInt64",
                    context.hasULongArrayToNative,
                    context.hasULongArrayToKotlin),
                Triple("float" to "Float",
                    context.hasFloatArrayToNative,
                    context.hasFloatArrayToKotlin),
                Triple("double" to "Double",
                    context.hasDoubleArrayToNative,
                    context.hasDoubleArrayToKotlin),
            ).forEach { (names, hasToNative, hasToKotlin) ->
                if(!hasToKotlin && !hasToNative) return@forEach

                val swiftType = names.second
                val lowerName = "${names.first}array"

                appendLine("\n// $swiftType")
                if(hasToNative) appendLine("""
                    
                    @_cdecl("${context.mangle("${lowerName}_new")}")
                    public func ___${lowerName}_new(elements: UnsafePointer<$swiftType>, length: Int32, makeCopy: Bool) -> UnsafeMutableRawPointer {
                        defer { if(!makeCopy) { elements.deallocate() } }
                        return intoRaw(Array(UnsafeBufferPointer(start: elements, count: Int(length))))
                    }
                """.trimIndent())
                if(hasToKotlin) appendLine("""
                    
                    @_cdecl("${context.mangle("${lowerName}_elements")}")
                    public func ___${lowerName}_elements(self: UnsafeMutableRawPointer) -> UnsafePointer<$swiftType>? {
                        return self.assumingMemoryBound(to: [$swiftType].self).pointee.withUnsafeBufferPointer { $0.baseAddress }
                    }
                    
                    @_cdecl("${context.mangle("${lowerName}_length")}")
                    public func ___${lowerName}_length(self: UnsafeMutableRawPointer) -> Int {
                        return self.assumingMemoryBound(to: [$swiftType].self).pointee.count
                    }
                """.trimIndent())
                appendLine("""
                    
                    @_cdecl("${context.mangle("${lowerName}_free")}")
                    public func ___${lowerName}_free(self: UnsafeMutableRawPointer) {
                        let _ = fromRaw(self, as: [$swiftType].self)
                    }
                """.trimIndent())
            }
        }

        if(context.hasObjectArrays || context.hasPrimitiveNullableArray || context.hasEnumNullableArray) {
            printLabel("Object array")

            buildList {
                context.castedDictionaries.mapTo(this) {
                    Triple(it.swiftName, it.cname.lowercase(), it in context.usedObjectArray)
                }
                context.castedInterfaces.mapTo(this) {
                    Triple(it.swiftName, it.cname.lowercase(), it in context.usedObjectArray)
                }
                add(Triple("String", "string", context.hasStringArray))
                add(Triple("UInt16", "char", context.hasCharNullableArray))
                add(Triple("Bool", "boolean", context.hasBooleanNullableArray))
                add(Triple("Int8", "byte", context.hasByteNullableArray || context.hasUByteNullableArray))
                add(Triple("Int16", "short", context.hasShortNullableArray || context.hasUShortNullableArray))
                add(Triple("Int32", "int", context.hasIntNullableArray || context.hasUIntNullableArray || context.hasEnumNullableArray))
                add(Triple("Int64", "long", context.hasLongNullableArray || context.hasULongNullableArray))
                add(Triple("Float", "float", context.hasFloatNullableArray))
                add(Triple("Double", "double", context.hasDoubleNullableArray))
            }.filter { it.third }.joinTo(this, separator = "") { (type, name, _) ->
                """
                    
                    // $type
                    
                    @_cdecl("${context.mangle("array_${name}_new")}")
                    public func ___array_${name}_new(capacity: Int32, ne: Bool) -> UnsafeMutableRawPointer {
                        if(ne) {
                            var arr = Array<$type?>();
                            arr.reserveCapacity(Int(capacity))
                            return intoRaw(arr)
                        } else {
                            var arr = Array<$type>();
                            arr.reserveCapacity(Int(capacity))
                            return intoRaw(arr)
                        }
                    }
                    
                    @_cdecl("${context.mangle("array_${name}_length")}")
                    public func ___array_${name}_length(self: UnsafeMutableRawPointer, ne: Bool) -> Int32 {
                        if(ne) {
                            return Int32(self.assumingMemoryBound(to: [$type?].self).pointee.count)
                        } else {
                            return Int32(self.assumingMemoryBound(to: [$type].self).pointee.count)
                        }
                    }
                    
                    @_cdecl("${context.mangle("array_${name}_push")}")
                    public func ___array_${name}_push(self: UnsafeMutableRawPointer, element: UnsafeMutableRawPointer?, ne: Bool) {
                        if(ne) {
                            self.assumingMemoryBound(to: [$type?].self).pointee.append(fromRawOpt(element, as: $type.self))
                        } else {
                            self.assumingMemoryBound(to: [$type].self).pointee.append(fromRaw(element!, as: $type.self))
                        }
                    }
                    
                    @_cdecl("${context.mangle("array_${name}_get")}")
                    public func ___array_${name}_get(self: UnsafeMutableRawPointer, index: Int32, ne: Bool) -> UnsafeRawPointer? {
                        if(ne) {
                            return self.assumingMemoryBound(to: [$type?].self).pointee.withUnsafeBufferPointer { 
                                guard let slot = $0.baseAddress?.advanced(by: Int(index)), slot.pointee != nil else { return nil }
                                return UnsafeRawPointer(slot)
                            }
                        } else {
                            return UnsafeRawPointer(self.assumingMemoryBound(to: [$type].self).pointee.withUnsafeBufferPointer { $0.baseAddress?.advanced(by: Int(index)) }!)
                        }
                    }
                    
                    @_cdecl("${context.mangle("array_${name}_free")}")
                    public func ___array_${name}_free(self: UnsafeMutableRawPointer, ne: Bool) {
                        if(ne) {
                            let _ = fromRaw(self, as: [$type?].self)
                        } else {
                            let _ = fromRaw(self, as: [$type].self)
                        }
                    }
                    
                """.trimIndent()
            }
            append("\n")
        }

        if(context.hasPrimitiveNullable || context.hasEnumNullable) {
            printLabel("Boxed primitives")

            listOf(
                Triple("UInt16", "char", context.hasCharNullable),
                Triple("Bool", "boolean", context.hasBooleanNullable),
                Triple("Int8", "byte", context.hasByteNullable || context.hasUByteNullable),
                Triple("UInt16", "short", context.hasShortNullable || context.hasUShortNullable),
                Triple("Int32", "int", context.hasIntNullable || context.hasUIntNullable || context.hasEnumNullable),
                Triple("Int64", "long", context.hasLongNullable || context.hasULongNullable),
                Triple("Float", "float", context.hasFloatNullable),
                Triple("Double", "double", context.hasDoubleNullable)
            ).forEach { (type, name, hasCast) ->
                if(!hasCast) return@forEach
                appendLine("""
                    
                    @_cdecl("${context.mangle("${name}_new")}")
                    public func ___${name}_new(value: $type) -> UnsafeMutableRawPointer {
                        return intoRaw(value);
                    }
                    
                    @_cdecl("${context.mangle("${name}_get")}")
                    public func ___${name}_get(of: UnsafeMutableRawPointer, free: Bool) -> $type {
                        if free { return fromRaw(of, as: $type.self) }
                        return of.assumingMemoryBound(to: $type.self).pointee
                    }
                """.trimIndent())
            }
        }
    }

    private fun StringBuilder.printDictionaries() {
        if(context.dictionaries.isEmpty())
            return
        printLabel("Structs")

        context.dictionaries.forEach { dictionary ->
            val name = dictionary.swiftName
            val fields = context.allFields[dictionary]!!

            val funcNew = dictionary.subCFuncMangled(context, "new")
            val funcFree = dictionary.subCFuncMangled(context, "free")

            append("\npublic struct $name {")
            fields.forEach { field ->
                append("\n\tvar ${field.swiftName}: ${field.type.toSwiftType(printNullable = true)}")
            }
            append("\n}")

            if(dictionary in context.castedDeclarations) {
                if (dictionary in context.toNativeDeclaration) {
                    // new
                    append("\n\n@_cdecl(\"$funcNew\")")
                    append("\npublic func ___${name}_New(")
                    fields.joinTo(this) {
                        "${it.swiftName}: ${it.type.toNativeSwiftType()}"
                    }

                    append(") -> UnsafeMutableRawPointer {")
                    append("\n\tintoRaw($name(")

                    fields.joinTo(this) {
                        "${it.swiftName}: ${castToSwift(it.type, it.swiftName)}"
                    }
                    append("))\n}")
                }
                if (dictionary in context.toKotlinDeclaration) {
                    // free
                    append("\n\n@_cdecl(\"$funcFree\")")
                    append("\npublic func ___${name}_Free(of: UnsafeMutableRawPointer) { let _ = fromRaw(of, as: $name.self); }")

                    // field getters
                    fields.joinTo(this, separator = "") {
                        val funcName = dictionary.subFieldCFuncMangled(context, it)

                        val type = it.type.toNativeSwiftType()
                        val castedCall = when {
                            it.type.isEnumArray() -> "intoRaw(of.assumingMemoryBound(to: $name.self).pointee.${it.swiftName}.map { \$0.rawValue })"
                            it.type.isReleasable() -> "of + MemoryLayout<$name>.offset(of: \\.${it.swiftName})!"
                            it.type.isEnum() -> "of.assumingMemoryBound(to: $name.self).pointee.${it.swiftName}.rawValue"
                            else -> "of.assumingMemoryBound(to: $name.self).pointee.${it.swiftName}"
                        }
                        val nullCheck = if(it.type.isNullable) "of.assumingMemoryBound(to: $name.self).pointee.${it.swiftName} == nil ? nil : " else ""
                        """
                            
                            
                            @_cdecl("$funcName")
                            public func ___${name}__${it.swiftName}(of: UnsafeMutableRawPointer) -> $type { $nullCheck$castedCall }
                        """.trimIndent()
                    }
                }
            }
            append("\n")
        }
    }

    private fun StringBuilder.printCallbacks() {
        if(context.callbacks.isEmpty())
            return
        printLabel("Callbacks")

        appendLine("""
            
            public class AbstractCallback: Hashable {
                internal let id: Int
                private let hashCode: Int32
                private let equalsFn: @convention(c) (Int, Int) -> Bool
                private let freeFn: @convention(c) (Int) -> Void

                internal init(
                    id: Int,
                    hashCode: Int32,
                    equals: @escaping @convention(c) (Int, Int) -> Bool,
                    free: @escaping @convention(c) (Int) -> Void
                ) {
                    self.id = id
                    self.hashCode = hashCode
                    self.equalsFn = equals
                    self.freeFn = free
                }

                public static func == (lhs: AbstractCallback, rhs: AbstractCallback) -> Bool {
                    if lhs === rhs { return true }
                    guard type(of: lhs) == type(of: rhs) else { return false }
                    return lhs.equalsFn(lhs.id, rhs.id)
                }

                public func hash(into hasher: inout Hasher) { 
                    hasher.combine(hashCode) 
                }
                
                deinit { freeFn(id) }
            }
        """.trimIndent())

        context.callbacks.forEach { callback ->
            val name = callback.swiftName
            val lower = callback.name.camelCase().lowercase()

            val swiftType = callback.type.toSwiftType(printNullable = true)
            val nativeType = callback.type.toNativeSwiftType()
            val ret = if(callback.type.isVoid()) "" else "return "

            val types = buildList {
                add("Int")
                callback.args.mapTo(this) {
                    it.type.toNativeSwiftType()
                }
            }.joinToString()
            val args = callback.args.joinToString {
                "_ ${it.swiftName}: ${it.type.toSwiftType(printNullable = true)}"
            }

            val castedArgs = buildList {
                add("id")
                callback.args.mapTo(this) {
                    castToNative(it.type, it.swiftName)
                }
            }.joinToString()

            val call = castToSwift(callback.type, "invokeFn($castedArgs)")
            appendLine("""
                
                public final class $name: AbstractCallback {
                    private let invokeFn: @convention(c) ($types) -> $nativeType
                    internal init(
                        id: Int,
                        hashCode: Int32,
                        invoke: @escaping @convention(c) ($types) -> $nativeType,
                        equals: @escaping @convention(c) (Int, Int) -> Bool,
                        free: @escaping @convention(c) (Int) -> Void
                    ) {
                        self.invokeFn = invoke
                        super.init(id: id, hashCode: hashCode, equals: equals, free: free)
                    }
                    public func callAsFunction($args) -> $swiftType {
                        $ret$call
                    }
                }
                
                @_cdecl("${context.mangle("${lower}_new")}")
                public func ___${name}_New(
                    id: Int,
                    hashCode: Int32,
                    invoke: @escaping @convention(c) ($types) -> $nativeType,
                    equals: @escaping @convention(c) (Int, Int) -> Bool,
                    free: @escaping @convention(c) (Int) -> Void
                ) -> UnsafeMutableRawPointer { intoRaw($name(id: id, hashCode: hashCode, invoke: invoke, equals: equals, free: free)) }
                
                @_cdecl("${context.mangle("${lower}_id")}")
                public func ___${name}_Id(of: UnsafeMutableRawPointer) -> Int { of.assumingMemoryBound(to: $name.self).pointee.id }
                
                @_cdecl("${context.mangle("${lower}_free")}")
                public func ___${name}_Free(of: UnsafeMutableRawPointer) { let _ = fromRaw(of, as: $name.self) }
            """.trimIndent())
        }
    }

    private fun StringBuilder.printEnums() {
        if(context.enums.isEmpty())
            return
        printLabel("Enums")

        context.enums.forEach { enum ->
            append("\npublic enum ${enum.swiftName}: Int32 {")
            if(enum.elements.isNotEmpty())
                append("\n\tcase ")
            enum.elements.joinTo(this) { it.camelCase() }
            appendLine("\n}")
        }
    }

    private fun StringBuilder.printFunctions() {
        if(context.allOperations.isEmpty())
            return
        printLabel("Functions")

        context.allOperations.forEach { operation ->
            val critical = operation.isCritical()
            val name = operation.swiftName
            val cName = operation.cnameMangled(context)

            val type = if(!operation.isInterfaceOperationAddress())
                operation.type.toNativeSwiftType()
            else "UnsafeMutableRawPointer"

            val args = operation.args.joinToString {
                val name = it.swiftName
                when {
                    critical && it.type.isString() ->
                        "$name: UnsafePointer<UInt8>?, _${name}_size: Int32"
                    critical && it.type.isArray() ->
                        "$name: UnsafePointer<${it.type.arrayTypeOrNull()!!.toNativeSwiftType()}>?, _${name}_length: Int32"
                    else -> "${it.cname}: ${it.type.toNativeSwiftType()}"
                }
            }

            val castedArgs = operation.args.map {
                val name = it.swiftName
                when {
                    critical && it.type.isString() ->
                        if(it.type.isNullable) "$name: _${name}_size < 0 ? nil : String(decoding: UnsafeBufferPointer(start: $name, count: Int(_${name}_size)), as: UTF8.self)"
                        else "$name: String(decoding: UnsafeBufferPointer(start: $name, count: Int(_${name}_size)), as: UTF8.self)"
                    critical && it.type.isEnumArray() -> {
                        val enumName = it.type.arrayTypeOrNull()!!.declaration.swiftName
                        if (it.type.isNullable) "$name: _${name}_length < 0 ? nil : Array(UnsafeBufferPointer(start: $name, count: Int(_${name}_length))).map { $enumName(rawValue: $0)! }"
                        else $$"$$name: Array(UnsafeBufferPointer(start: $$name, count: Int(_$${name}_length))).map { $$enumName(rawValue: $0)! }"
                    }
                    critical && it.type.isArray() ->
                        if(it.type.isNullable) "$name: _${name}_length < 0 ? nil : Array(UnsafeBufferPointer(start: $name, count: Int(_${name}_length)))"
                        else "$name: Array(UnsafeBufferPointer(start: $name, count: Int(_${name}_length)))"
                    else -> "$name: ${castToSwift(it.type, it.swiftName)}"
                }
            }

            val ret = if(!operation.type.isVoid())
                "return " else ""

            val call = if (operation.isInterfaceOperation()) {
                val inter = operation.getInterface(context)!!
                val interName = inter.swiftName
                val self = operation.args.getOrNull(0)?.swiftName
                when {
                    operation.isInterfaceOperationConstructor() ->
                        "intoRaw($interName(${castedArgs.joinToString()}))"
                    operation.isInterfaceOperationFn() ->
                        castToNative(operation.type, "$self.assumingMemoryBound(to: $interName.self).pointee.$name(${castedArgs.drop(1).joinToString()})")
                    operation.isInterfaceOperationFree() ->
                        "let _ = fromRaw($self, as: $interName.self);"
                    operation.isInterfaceOperationClone() ->
                        "intoRaw($self.assumingMemoryBound(to: $interName.self).pointee)"
                    operation.isInterfaceOperationAddress() ->
                        "Unmanaged.passUnretained($self.assumingMemoryBound(to: $interName.self).pointee).toOpaque()"
                    else -> throw UnsupportedOperationException()
                }
            } else castToNative(operation.type, "$name(${castedArgs.joinToString()})")

            appendLine("""
                
                @_cdecl("$cName")
                public func __$name($args) -> $type {
                    $ret$call
                }
            """.trimIndent())
        }
        append("\n")
    }

    private fun castToNative(type: ResolvedIdlType, content: String): String = when {
        type.isEnumArray() -> {
            val enum = type.arrayTypeOrNull()!!
            val enumCast = "$0.rawValue"
            val cast = if(enum.isNullable) $$"$0.map { $$enumCast }" else enumCast

            if (type.isNullable) "intoRawOpt($content.map { $cast })"
            else "intoRaw($content.map { $cast })"
        }
        type.isEnum() ->
            if(type.isNullable) "intoRawOpt($content?.rawValue)"
            else "$content.rawValue"
        type.isNullable -> "intoRawOpt($content)"
        type.isReleasable() -> "intoRaw($content)"
        else -> content
    }

    private fun castToSwift(type: ResolvedIdlType, content: String): String = when {
        type.isEnumArray() -> {
            val enum = type.arrayTypeOrNull()!!
            val enumCast = "${enum.declaration.swiftName}(rawValue: $0)!"
            val int = if(enum.isNullable) "Int32?" else "Int32"
            val cast = if(enum.isNullable) "$0.map { $enumCast }" else enumCast

            if (type.isNullable) "fromRawOpt($content, as: [$int].self).map { $cast }"
            else "fromRaw($content, as: [$int].self).map { $cast }"
        }
        type.isEnum() ->
            if(type.isNullable) $$"fromRawOpt($$content, as: Int32.self).map { $${type.declaration.swiftName}(rawValue: $0)! }"
            else "${type.declaration.swiftName}(rawValue: $content)!"
        type.isNullable -> "fromRawOpt($content, as: ${type.toSwiftType()}.self)"
        type.isReleasable() -> "fromRaw($content, as: ${type.toSwiftType()}.self)"
        else -> content
    }
}