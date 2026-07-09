package com.huskerdev.nativekt.printers.rust

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.io.File

class RustPrinter(
    val idl: IdlResolver,
    target: File,
    val classPath: String,
    val moduleName: String
) {
    init {
        target.parentFile.mkdirs()

        val builder = StringBuilder()
        builder.printApi()
        builder.printHeaderDef()
        builder.printStringDef(::mangle)
        builder.printArraysDef(::mangle)
        builder.printStructs()
        builder.printCallbacks()
        builder.printEnums()
        builder.printInterfaces()

        builder.append("\n")
        printLabel(builder, "Functions")
        idl.globalOperators().forEach {
            builder.printFunction(it)
        }

        target.writeText(builder.toString().replace("\n", System.lineSeparator()))
    }

    private fun mangle(name: String) =
        mangle(classPath, moduleName, name)

    private fun StringBuilder.printApi() {
        printLabel(this, "API")
        append("/*\n=============================================================== *\\")

        idl.interfaces.values.forEach { inter ->
            val name = inter.name.upperCamelCase()

            append("\n\npub struct $name")
            if(inter.fields.isEmpty())
                append(";")

            if(inter.operations.isNotEmpty()) {
                append("\n\nimpl $name {")

                inter.operations.forEach { op ->
                    append("\n\tfn ${op.name.camelCase()}(")
                    buildList {
                        add("&self")
                    }.joinTo(this)
                    append(")")

                    if(!op.type.isVoid())
                        append(" -> ${op.type.toRustType()}")
                    append(" {}")
                }
                append("\n}")
            }

            append("\n\n=============================================================== *\\")
        }

        idl.globalOperators().forEach { operation ->
            append("\n\npub fn ${operation.name.snakeCase()}(")
            operation.args.joinTo(this, ",") {
                "\n\t${it.name.snakeCase()}: ${it.type.toRustType()}"
            }
            if(operation.args.isNotEmpty())
                append("\n")
            append(")")

            if(!operation.type.isVoid())
                append(" -> ${operation.type.toRustType()}")
            append(" {}")
        }
        append("\n\n=============================================================== */\n\n")
    }

    private fun StringBuilder.printFunction(operation: ResolvedIdlOperation) {
        append("\n#[no_mangle]")

        val cName = mangle(operation.name)

        // Header
        append("\nextern \"C\" fn $cName(")
        operation.args.joinTo(this, ", ") {
            "${it.name.snakeCase()}: ${it.type.toNativeRustType()}"
        }
        append(") ")
        if(!operation.type.isVoid())
            append("-> ${operation.type.toNativeRustType()} ")
        append("{\n\t")

        // Call
        val call = buildString {
            append("crate::${operation.name.snakeCase()}")
            operation.args.joinTo(this, prefix = "(", postfix = ")") {
                toRustType(it.type, it.name.snakeCase())
            }
        }
        append(toNativeType(operation.type, call))
        append("\n}\n")
    }

    private fun StringBuilder.printStructs() {
        if(idl.dictionaries.isEmpty())
            return

        printLabel(this, "Structs")

        append("\nextern \"C\" {")
        idl.dictionaries.values.forEach { dictionary ->
            val name = dictionary.name
            val args = dictionary.allFields().joinToString {
                "${it.name}: ${it.type.toNativeRustType()}"
            }
            append("""
                
                fn ${mangle("${name}_new")}($args) -> *const _${name};
                fn ${mangle("${name}_clone")}(self_: *const _${name}) -> *const _${name};
                fn ${mangle("${name}_free")}(self_: *const _${name});
            """.replaceIndent("\t"))
        }
        append("\n}\n")

        idl.dictionaries.values.forEach { dictionary ->
            val name = dictionary.name.upperCamelCase()
            val fields = dictionary.allFields()

            // Native struct
            append("""
                
                #[repr(C)]
                #[derive(Debug)]
                struct _$name {
            """.trimIndent())
            fields.forEach {
                append("\n\t${it.name.snakeCase()}: ${it.type.toNativeRustType()},")
            }
            append("\n\t__flags: i8\n}\n\n")

            // Wrapper struct
            append("""
                pub struct $name {
                    ptr: *const _$name,
            """.trimIndent())
            fields.forEach {
                append("\n\tpub ${it.name.snakeCase()}: ${it.type.toRustType()},")
            }
            append("\n}\n\n")

            // Impl
            append("""
                impl $name {
            """.trimIndent())

            // new
            append("\n\tpub fn new(")
            fields.joinTo(this) {
                "${it.name.snakeCase()}: ${it.type.toRustType()}"
            }
            append(") -> Self {")
            append("\n\t\tSelf::wrap(unsafe { ${mangle("${name}_new")}(")
            fields.joinTo(this) {
                toNativeType(it.type, it.name.snakeCase())
            }
            append(") })\n\t}\n")

            // wraps
            append("""
                fn wrap(ptr: *const _$name) -> Self {
                    let r = unsafe { &*ptr };
                    Self { 
            """.replaceIndent("\t"))
            buildList {
                add("ptr" to "ptr")
                fields.mapTo(this) {
                    toRustType(it.type, "r.${it.name.snakeCase()}") to it.name.snakeCase()
                }
            }.joinTo(this) {
                if(it.first != it.second)
                    "${it.second}: ${it.first}"
                else it.first
            }
            append(" }\n\t}\n}\n")

            append("""
                
                impl_wrapper!($name, _$name);
                impl_drop_clone!($name, ${mangle("${name}_free")}, ${mangle("${name}_clone")});
                impl_ptr_holder!($name, _$name, ${mangle("${name}_free")}, ${mangle("${name}_clone")});
                
            """.trimIndent())
        }
    }

    private fun StringBuilder.printCallbacks() {
        if(idl.callbacks.isEmpty())
            return

        printLabel(this, "Callbacks")
        append($$"""
            
            macro_rules! impl_callback_base {
                ($name:ident, $inner:ident) => {
                    impl $name {
                        fn wrap(ptr: *const $inner) -> Self {
                            $name { ptr }
                        }
                        pub fn equals(&self, other: Self) -> bool {
                            unsafe { ((&*self.ptr).equals)(self.ptr, other.ptr) }
                        }
                        pub fn hash_code(&self) -> i32 {
                            unsafe { ((&*self.ptr).hash_code)(self.ptr) }
                        }
                    }
                    
                    impl_wrapper!($name, $inner);
                    
                    impl Drop for $name {
                        fn drop(&mut self) {
                            unsafe { ((&*self.ptr).free)(self.ptr) }
                        }
                    }
                    
                    impl Clone for $name {
                        fn clone(&self) -> Self {
                            unsafe { $name::wrap(((&*self.ptr).clone)(self.ptr)) }
                        }
                    }
                };
            }
            
        """.trimIndent())

        idl.callbacks.values.forEach { callback ->
            val name = callback.name.uppercaseFirstChar()

            // Struct
            append("""
                
                #[repr(C)]
                #[derive(Debug)]
                struct _$name {
                    __flags: i8,
                
            """.trimIndent())
            append("\tinvoke: extern \"C\" fn")
            buildList {
                add("*const Self")
                callback.args.mapTo(this) {
                    it.type.toNativeRustType()
                }
            }.joinTo(this, prefix = "(", postfix = ")")
            if(!callback.type.isVoid())
                append(" -> ${callback.type.toNativeRustType()}")
            append(",")
            append("""
                
                    clone: extern "C" fn(*const Self) -> *const Self,
                    equals: extern "C" fn(*const Self, *const Self) -> bool,
                    hash_code: extern "C" fn(*const Self) -> i32,
                    free: extern "C" fn(*const Self),
                }
                
                pub struct $name {
                    ptr: *const _$name
                }
                
                impl $name {
                
            """.trimIndent())

            // invoke
            append("\tpub fn invoke")
            buildList {
                add("&self")
                callback.args.mapTo(this) {
                    "${it.name}: ${it.type.toRustType()}"
                }
            }.joinTo(this, prefix = "(", postfix = ")")
            if(!callback.type.isVoid())
                append(" -> ${callback.type.toRustType()}")

            val call = buildString {
                append("((&*self.ptr).invoke)")
                buildList {
                    add("self.ptr")
                    callback.args.mapTo(this) {
                        toNativeType(it.type, it.name)
                    }
                }.joinTo(this, prefix = "(", postfix = ")")
            }
            append(" {\n\t\tunsafe { ${toRustType(callback.type, call)} }\n\t}")

            // Other
            append("""
                
                }
                
                impl_callback_base!($name, _$name);
                
            """.trimIndent())
        }
    }

    private fun StringBuilder.printEnums() {
        if(idl.enums.isEmpty())
            return

        printLabel(this, "Enums")

        idl.enums.values.forEach { enum ->
            val name = enum.name.upperCamelCase()

            append("""
                
                #[derive(PartialEq, Eq)]
                pub enum $name {
            """.trimIndent())

            enum.elements.mapIndexed { index, value ->
                "\n\t$value = $index"
            }.joinTo(this, ",")
            append("\n}\n")

            append("""
                
                impl $name {
                    pub fn from_int(value: i32) -> Self {
                        match value {
            """.trimIndent())
            enum.elements.mapIndexed { index, value ->
                "\n\t\t\t$index => $name::$value,"
            }.joinTo(this, "")
            append("""
            
                        _ => panic!()
                    }
                }
                pub fn to_int(self) -> i32 {
                    self as i32
                }
            }
            
            """.trimIndent())
        }
    }

    private fun StringBuilder.printInterfaces() {
        if(idl.interfaces.isEmpty())
            return

        printLabel(this, "Interfaces")

        append("""

            fn clone_arc<T>(arc: *const T) -> Arc<T> {
                unsafe {
                    let _self = Arc::from_raw(arc);
                    let result = _self.clone();
                    std::mem::forget(_self);
                    result
                }
            }
        """.trimIndent())

        idl.interfaces.values.forEach { inter ->
            val name = "crate::${inter.name.upperCamelCase()}"

            append("""
                
                
                #[no_mangle]
                extern "C" fn ${mangle(interfaceFreeCName(inter))}(ptr: *const $name) {
                    unsafe { let _ = Arc::from_raw(ptr); }
                }
            """.trimIndent())

            inter.constructors.forEach { constructor ->
                val constructorName = "new"
                val cArgs = constructor.args.joinToString {
                    "${it.name.snakeCase()}: ${it.type.toNativeRustType()}"
                }
                val rustArgs = constructor.args.joinToString {
                    toRustType(it.type, it.name.snakeCase())
                }

                append("""
                    
                    
                    #[no_mangle]
                    extern "C" fn ${mangle(interfaceConstructorCName(inter, constructor))}($cArgs) -> *const $name {
                        Arc::into_raw(Arc::new($name::$constructorName($rustArgs)))
                    }
                """.trimIndent())
            }

            inter.operations.forEach { operation ->
                val operationName = operation.name.snakeCase()

                val cArgs = buildList {
                    add("ptr: *const $name")
                    operation.args.mapTo(this) {
                        "${it.name.snakeCase()}: ${it.type.toNativeRustType()}"
                    }
                }.joinToString()

                val rustArgs = operation.args.joinToString {
                    toRustType(it.type, it.name.snakeCase())
                }

                val call = toNativeType(operation.type, "clone_arc(ptr).$operationName($rustArgs)")

                append("""
                    
                    
                    #[no_mangle]
                    extern "C" fn ${mangle(interfaceOperationCName(inter, operation))}($cArgs) {
                        $call
                    }
                """.trimIndent())
            }
        }
    }

    private fun toNativeType(type: ResolvedIdlType, content: String): String {
        val nullable = if(type.isNullable) "_nullable" else ""
        return when {
            type.isVoid() || type.isPrimitive() -> content
            type.isEnum() -> "${type.declaration.name}::to_int($content)"
            type.isArray() && type.arrayTypeOrNull()!!.isNullable -> "KArrayOpt::unwrap$nullable($content)"
            type.isInterface() -> "Arc::into_raw($content)"
            else -> "${type.toCType(ptr = false)}::unwrap$nullable($content)"
        }
    }

    private fun toRustType(type: ResolvedIdlType, content: String): String {
        val nullable = if(type.isNullable) "_nullable" else ""
        return when {
            type.isVoid() || type.isPrimitive() -> content
            type.isEnum() -> "${type.declaration.name}::from_int($content)"
            type.isArray() && type.arrayTypeOrNull()!!.isNullable -> "KArrayOpt::wrap$nullable($content)"
            type.isInterface() -> "clone_arc($content)"
            else -> "${type.toCType(ptr = false)}::wrap$nullable($content)"
        }
    }

    private fun ResolvedIdlType.toNativeRustType(): String = when {
        isChar() -> "u16"
        isBoolean() -> "bool"
        isByte() -> "i8"
        isUByte() -> "u8"
        isShort() -> "i16"
        isUShort() -> "u16"
        isInt() -> "i32"
        isUInt() -> "u32"
        isLong() -> "i64"
        isULong() -> "u64"
        isFloat() -> "f32"
        isDouble() -> "f64"
        isArray() -> "*const _KArray"
        isEnum() -> "i32"
        isString() -> "*const _KString"
        isCallback() || isDictionary() -> "*const _${declaration.name.upperCamelCase()}"
        isInterface() -> "*const crate::${declaration.name.upperCamelCase()}"
        else -> "UNKNOWN"
    }

    private fun ResolvedIdlType.toRustType(): String {
        val result = when {
            isChar() -> "u16"
            isBoolean() -> "bool"
            isByte() -> "i8"
            isUByte() -> "u8"
            isShort() -> "i16"
            isUShort() -> "u16"
            isInt() -> "i32"
            isUInt() -> "u32"
            isLong() -> "i64"
            isULong() -> "u64"
            isFloat() -> "f32"
            isDouble() -> "f64"
            isEnum() -> declaration.name.upperCamelCase()
            isString() -> "KString"
            isCallback() || isDictionary() -> declaration.name.upperCamelCase()
            isInterface() -> "Arc<crate::${declaration.name.upperCamelCase()}>"
            isArray() -> arrayType { type ->
                when {
                    type.isPrimitive() -> "${type.toCType(ptr = false)}Array"
                    type.isEnum() -> "KIntArray"
                    else -> if(type.isNullable) "KArrayOpt<${type.toCType(ptr = false)}>"
                    else "KArray<${type.toRustType()}>"
                }
            }
            else -> "UNKNOWN"
        }
        return if(isNullable)
            "Option<$result>"
        else result
    }

}