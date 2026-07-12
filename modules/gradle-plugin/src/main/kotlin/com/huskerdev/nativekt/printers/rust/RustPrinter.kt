package com.huskerdev.nativekt.printers.rust

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.*
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
        fun mangle(name: String) = mangle(classPath, moduleName, "_$name")

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

    private val ResolvedIdlDeclaration.rustName: String
        get() = when (this) {
            is ResolvedIdlEnum -> rustName
            is ResolvedIdlDictionary -> rustName
            is ResolvedIdlCallbackFunction -> rustName
            is ResolvedIdlInterface -> rustName
            else -> throw UnsupportedOperationException("${(this as BuiltinIdlDeclaration).kind}")
        }

    private val ResolvedIdlOperation.rustName: String
        get() = when {
            isInterfaceOperationConstructor() -> "new"
            isInterfaceOperationFn() -> interfaceFunctionName().snakeCase()
            else -> name.snakeCase()
        }

    private val ResolvedIdlDictionary.rustName: String
        get() = name.upperCamelCase()

    private val ResolvedIdlField.rustName: String
        get() = name.snakeCase()

    private val ResolvedIdlEnum.rustName: String
        get() = name.upperCamelCase()

    private val ResolvedIdlCallbackFunction.rustName: String
        get() = name.upperCamelCase()

    private val ResolvedIdlInterface.rustName: String
        get() = "crate::${name.upperCamelCase()}"

    private fun StringBuilder.printApi() {
        printLabel(this, "API")
        append("/*\n=============================================================== *\\")

        idl.interfaces.values.forEach { inter ->
            val name = inter.rustName

            append("\n\npub struct $name")
            if(inter.fields.isEmpty())
                append(";")

            if(inter.operations.isNotEmpty()) {
                append("\n\nimpl $name {")

                inter.toOperations().forEach { operation ->
                    val args = operation.args
                        .map { it.type.toRustType() }
                    val type = if(!operation.type.isVoid())
                        " -> ${operation.type.toRustType()}"
                    else ""

                    when {
                        operation.isInterfaceOperationConstructor() -> {
                            append("\n\tfn ${operation.rustName}(${args.joinToString()}) -> Self {}")
                        }
                        operation.isInterfaceOperationFn() -> {
                            val args = buildList {
                                add("&self")
                                addAll(args.drop(1))
                            }.joinToString()

                            append("\n\tfn ${operation.rustName}($args)$type {}")
                        }
                    }
                }
                append("\n}")
            }

            append("\n\n=============================================================== *\\")
        }

        idl.globalOperators().forEach { operation ->
            append("\n\npub fn ${operation.rustName}(")
            operation.args.joinTo(this, ",") {
                "\n\t${it.rustName}: ${it.type.toRustType()}"
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

        // Header
        append("\nextern \"C\" fn ${operation.cnameMangled(classPath, moduleName)}(")
        operation.args.joinTo(this, ", ") {
            "${it.rustName}: ${it.type.toNativeRustType()}"
        }
        append(") ")
        if(!operation.type.isVoid())
            append("-> ${operation.type.toNativeRustType()} ")
        append("{\n\t")

        // Call
        val call = buildString {
            append("crate::${operation.rustName}")
            operation.args.joinTo(this, prefix = "(", postfix = ")") {
                toRustType(it.type, it.rustName)
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
            val name = dictionary.rustName
            val args = dictionary.allFields().joinToString {
                "${it.rustName}: ${it.type.toNativeRustType()}"
            }
            append("""
                
                fn ${dictionary.subCFunc(classPath, moduleName, "new")}($args) -> *const _$name;
                fn ${dictionary.subCFunc(classPath, moduleName, "clone")}(self_: *const _$name) -> *const _$name;
                fn ${dictionary.subCFunc(classPath, moduleName, "free")}(self_: *const _$name);
            """.replaceIndent("\t"))
        }
        append("\n}\n")

        idl.dictionaries.values.forEach { dictionary ->
            val name = dictionary.rustName
            val fields = dictionary.allFields()

            val funcCNew = dictionary.subCFunc(classPath, moduleName, "new")
            val funcCClone = dictionary.subCFunc(classPath, moduleName, "clone")
            val funcCFree = dictionary.subCFunc(classPath, moduleName, "free")

            // Native struct
            append("""
                
                #[repr(C)]
                #[derive(Debug)]
                struct _$name {
            """.trimIndent())
            fields.forEach {
                append("\n\t${it.rustName}: ${it.type.toNativeRustType()},")
            }
            append("\n\t__flags: i8\n}\n\n")

            // Wrapper struct
            append("""
                pub struct $name {
                    ptr: *const _$name,
            """.trimIndent())
            fields.forEach {
                append("\n\tpub ${it.rustName}: ${it.type.toRustType()},")
            }
            append("\n}\n\n")

            // Impl
            append("""
                impl $name {
            """.trimIndent())

            // new
            append("\n\tpub fn new(")
            fields.joinTo(this) {
                "${it.rustName}: ${it.type.toRustType()}"
            }
            append(") -> Self {")
            append("\n\t\tSelf::wrap(unsafe { $funcCNew(")
            fields.joinTo(this) {
                toNativeType(it.type, it.rustName)
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
                    toRustType(it.type, "r.${it.rustName}") to it.rustName
                }
            }.joinTo(this) {
                if(it.first != it.second)
                    "${it.second}: ${it.first}"
                else it.first
            }
            append(" }\n\t}\n}\n")

            append("""
                
                impl_wrapper!($name, _$name);
                impl_drop_clone!($name, $funcCFree, $funcCClone);
                impl_ptr_holder!($name, _$name, $funcCFree, $funcCClone);
                
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
            val name = callback.rustName

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
                    "${it.rustName}: ${it.type.toRustType()}"
                }
            }.joinTo(this, prefix = "(", postfix = ")")
            if(!callback.type.isVoid())
                append(" -> ${callback.type.toRustType()}")

            val call = buildString {
                append("((&*self.ptr).invoke)")
                buildList {
                    add("self.ptr")
                    callback.args.mapTo(this) {
                        toNativeType(it.type, it.rustName)
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
            val name = enum.rustName

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
            inter.toOperations().forEach { operation ->
                val rustName = operation.rustName
                val cArgs = operation.args.joinToString {
                    "${it.rustName}: ${it.type.toNativeRustType()}"
                }
                val rustArgs = operation.args.map {
                    toRustType(it.type, it.rustName)
                }
                val type = if(!operation.type.isVoid())
                    " -> ${operation.type.toNativeRustType()}"
                else ""

                val body = when {
                    operation.isInterfaceOperationFree() ->
                        "unsafe { let _ = Arc::from_raw(${operation.args[0].rustName}); }"
                    operation.isInterfaceOperationConstructor() ->
                        "Arc::into_raw(Arc::new(${inter.rustName}::$rustName(${rustArgs.joinToString()})))"
                    operation.isInterfaceOperationFn() ->
                        toNativeType(operation.type, "clone_arc(${operation.args[0].rustName}).$rustName(${rustArgs.drop(1).joinToString()})")
                    else -> throw UnsupportedOperationException()
                }
                append("\n")
                append("""
                    #[no_mangle] extern "C" fn ${operation.cnameMangled(classPath, moduleName)}(${cArgs})$type {
                        $body
                    }
                    
                """.trimIndent())
            }
        }
    }

    private fun toNativeType(type: ResolvedIdlType, content: String): String {
        val nullable = if(type.isNullable) "_nullable" else ""
        return when {
            type.isVoid() || type.isPrimitive() -> content
            type.isEnum() -> "${type.declaration.rustName}::to_int($content)"
            type.isArray() && type.arrayTypeOrNull()!!.isNullable -> "KArrayOpt::unwrap$nullable($content)"
            type.isInterface() -> "Arc::into_raw($content)"
            type.isPrimitive() -> content
            type is ResolvedIdlType.Default -> "${type.toCType(ptr = false)}::unwrap$nullable($content)"
            else -> throw UnsupportedOperationException()
        }
    }

    private fun toRustType(type: ResolvedIdlType, content: String): String {
        val nullable = if(type.isNullable) "_nullable" else ""
        return when {
            type.isVoid() || type.isPrimitive() -> content
            type.isEnum() -> "${type.declaration.rustName}::from_int($content)"
            type.isArray() && type.arrayTypeOrNull()!!.isNullable -> "KArrayOpt::wrap$nullable($content)"
            type.isInterface() -> "clone_arc($content)"
            type is ResolvedIdlType.Default -> "${type.toCType(ptr = false)}::wrap$nullable($content)"
            else -> throw UnsupportedOperationException()
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
        isCallback() || isDictionary() -> "*const _${declaration.rustName}"
        isInterface() -> "*const ${declaration.rustName}"
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
            isEnum() -> declaration.rustName
            isString() -> "KString"
            isCallback() || isDictionary() -> declaration.rustName
            isInterface() -> "Arc<${declaration.rustName}>"
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