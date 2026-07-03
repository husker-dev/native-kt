package com.huskerdev.nativekt.printers.rust

import com.huskerdev.nativekt.utils.*
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.io.File

class RustPrinter(
    val idl: IdlResolver,
    target: File
) {
    init {
        target.parentFile.mkdirs()

        val builder = StringBuilder()
        builder.printApi()
        builder.printHeaderDef()
        builder.printStringDef()
        builder.printArraysDef()
        builder.printStructs()
        builder.printCallbacks()
        builder.printEnums()

        builder.append("\n")
        printLabel(builder, "Functions")
        idl.globalOperators().forEach {
            builder.printFunction(it)
        }

        target.writeText(builder.toString().replace("\n", System.lineSeparator()))
    }

    private fun StringBuilder.printFunction(operation: ResolvedIdlOperation) {
        append("\n#[no_mangle]")

        // Header
        append("\nextern \"C\" fn ${operation.name.snakeCase()}(")
        operation.args.joinTo(this, ",") {
            "\n\t${it.name.snakeCase()}: ${it.type.toNativeRustType()}"
        }
        if(operation.args.isNotEmpty())
            append("\n")
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

    private fun toNativeType(type: ResolvedIdlType, content: String): String {
        val nullable = if(type.isNullable) "_nullable" else ""
        return when {
            type.isVoid() || type.isPrimitive() -> content
            type.isEnum() -> "${type.declaration.name}::to_int($content)"
            type.isArray() && type.arrayTypeOrNull()!!.isNullable -> "KArrayOpt::unwrap$nullable($content)"
            else -> "${type.toCType(ptr = false)}::unwrap$nullable($content)"
        }
    }

    private fun toRustType(type: ResolvedIdlType, content: String): String {
        val nullable = if(type.isNullable) "_nullable" else ""
        return when {
            type.isVoid() || type.isPrimitive() -> content
            type.isEnum() -> "${type.declaration.name}::from_int($content)"
            type.isArray() && type.arrayTypeOrNull()!!.isNullable -> "KArrayOpt::wrap$nullable($content)"
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
        else -> "*const _${this.toCType(ptr = false)}"
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
            isArray() -> arrayType { type ->
                when {
                    type.isPrimitive() -> "${type.toCType(ptr = false)}Array"
                    type.isEnum() -> "KIntArray"
                    else -> if(type.isNullable) "KArrayOpt<${type.toCType(ptr = false)}>"
                        else "KArray<${type.toRustType()}>"
                }
            }
            else -> this.toCType(ptr = false)
        }
        return if(isNullable)
            "Option<$result>"
        else result
    }

    private fun StringBuilder.printHeaderDef() {
        append($$"""
            #![allow(unused)]
            #![allow(private_bounds)]
            
            use std::ffi::c_void;
            use std::fmt::{Debug, Display, Formatter};
            use std::ptr::null;
            
            extern "C" {
                fn malloc(s: usize) -> *const c_void;
            }
            
            trait PtrHolder: Clone {
                fn ptr(&self) -> *const c_void;
                fn free_ptr() -> *const c_void;
                fn clone_ptr() -> *const c_void;
                fn wrap(of: *const c_void) -> Self;
            }
            
            macro_rules! impl_wrapper {
                ($name:ident, $inner:ident) => {
                    impl $name {
                        fn wrap_nullable(ptr: *const $inner) -> Option<$name> {
                            if (ptr == std::ptr::null()) {
                                return None;
                            }
                            Some(Self::wrap(ptr))
                        }
                        fn unwrap(self) -> *const $inner {
                            let ptr = self.ptr;
                            std::mem::forget(self);
                            ptr
                        }
                        fn unwrap_nullable(of: Option<Self>) -> *const $inner {
                            if (of.is_none()) {
                                return std::ptr::null();
                            }
                            of.unwrap().unwrap()
                        }
                    }
                };
            }
            
            macro_rules! impl_drop_clone {
                ($name:ident, $free:ident, $clone:ident) => {
                    impl Drop for $name {
                        fn drop(&mut self) {
                            unsafe { $free(self.ptr); }
                        }
                    }

                    impl Clone for $name {
                        fn clone(&self) -> Self {
                            unsafe { $name::wrap($clone(self.ptr)) }
                        }
                    }
                };
            }
            
            macro_rules! impl_ptr_holder {
                ($name:ident, $inner:ident, $free:ident, $clone:ident) => {
                    impl PtrHolder for $name {
                        fn ptr(&self) -> *const c_void {
                            self.ptr as *const c_void
                        }
                        fn free_ptr() -> *const c_void {
                            $free as *const c_void
                        }
                        fn clone_ptr() -> *const c_void {
                            $clone as *const c_void
                        }
                        fn wrap(of: *const c_void) -> Self {
                            $name::wrap(of as *const $inner)
                        }
                    }
                };
            }
            
        """.trimIndent())
    }

    private fun StringBuilder.printApi() {
        printLabel(this, "API")
        append("/*\n=============================================================== *\\")
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

    private fun StringBuilder.printStringDef() {
        printLabel(this, "String")
        append($$"""
            
            #[repr(C)]
            #[derive(Debug)]
            struct _KString {
                data: *const u8,
                size: usize,
                length: i32,
                __flags: i8
            }
            
            pub struct KString {
                ptr: *const _KString
            }
            
            impl KString {
                fn wrap(ptr: *const _KString) -> Self {
                    Self { ptr }
                }
                pub fn from(value: String) -> Self {
                    unsafe {
                        let length = value.chars().count() as i32;
                        let size = value.len();
                        let data = malloc(size) as *mut u8;
                        std::ptr::copy_nonoverlapping(value.as_ptr(), data, size);
                        Self { ptr: KString_new(data, length, size, true) }
                    }
                }
                pub fn from_str(value: &str) -> Self {
                    unsafe {
                        let length = value.chars().count() as i32;
                        let size = value.len();
                        let data = value.as_ptr();
                        Self { ptr: KString_new(data, length, size, false) }
                    }
                }
                pub fn as_str(&self) -> &str {
                    unsafe {
                        let str = self.ptr.read();
                        std::str::from_utf8_unchecked(
                            std::slice::from_raw_parts(str.data, str.size)
                        )
                    }
                }
            }

            impl From<KString> for String {
                fn from(value: KString) -> Self {
                    value.to_string()
                }
            }
            
            impl Display for KString {
                fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
                    Debug::fmt(&self.as_str(), f)
                }
            }

            impl_wrapper!(KString, _KString);
            impl_drop_clone!(KString, KString_free, KString_clone);
            impl_ptr_holder!(KString, _KString, KString_free, KString_clone);

            extern "C" {
                fn KString_new(data: *const u8, length: i32, size: usize, is_data_owner: bool) -> *const _KString;
                fn KString_free(self_: *const _KString);
                fn KString_clone(self_: *const _KString) -> *const _KString;
            }

            #[macro_export] macro_rules! k_string {
                ($str:expr) => (KString::from($str));
            }
            
        """.trimIndent())
    }

    private fun StringBuilder.printArraysDef() {
        printLabel(this, "Arrays")

        append($$"""
            
            #[repr(C)]
            #[derive(Debug)]
            struct _KArray {
                elements: *const c_void,
                size: usize,
                length: i32,
                __flags: i8
            }
            
            extern "C" {
                fn KArray_new(elements: *const *const c_void, length: i32, is_data_owner: bool) -> *const _KArray;
                fn KArray_clone(self_: *const _KArray, clone_op: *const c_void) -> *const _KArray;
                fn KArray_free(self_: *const _KArray, free_op: *const c_void);
            }
            
            macro_rules! impl_typed_array {
                ($name:ident, $rsType:ident, $size:expr, $new:ident, $free:ident, $clone:ident) => {
                    pub struct $name {
                        ptr: *const _KArray
                    }
            
                    impl $name {
                        fn wrap(ptr: *const _KArray) -> Self {
                            Self { ptr }
                        }
                        pub fn as_slice(&self) -> &[$rsType] {
                            unsafe {
                                let arr = self.ptr.read();
                                std::slice::from_raw_parts(arr.elements as *const $rsType, arr.length as usize)
                            }
                        }
                        pub fn from(array: Vec<$rsType>) -> Self {
                            unsafe {
                                let length = array.len();
                                let size = length * $size;
                                let elements = malloc(size) as *mut $rsType;
                                std::ptr::copy_nonoverlapping(array.as_ptr(), elements, length);
                                Self { ptr: $new(elements, length as i32, size) }
                            }
                        }
                    }
                    extern "C" {
                        fn $new(elements: *const $rsType, length: i32, size: usize) -> *const _KArray;
                        fn $free(self_: *const _KArray);
                        fn $clone(self_: *const _KArray) -> *const _KArray;
                    }
            
                    impl_wrapper!($name, _KArray);
                    impl_drop_clone!($name, $free, $clone);
                    impl_ptr_holder!($name, _KArray, $free, $clone);
                };
            }
            
            impl_typed_array!(KCharArray, u16, 2, KCharArray_new, KCharArray_free, KCharArray_clone);
            impl_typed_array!(KBooleanArray, bool, 1, KBooleanArray_new, KBooleanArray_free, KBooleanArray_clone);
            impl_typed_array!(KByteArray, i8, 1, KByteArray_new, KByteArray_free, KByteArray_clone);
            impl_typed_array!(KUByteArray, u8, 1, KUByteArray_new, KUByteArray_free, KUByteArray_clone);
            impl_typed_array!(KShortArray, i16, 2, KShortArray_new, KShortArray_free, KShortArray_clone);
            impl_typed_array!(KUShortArray, u16, 2, KUShortArray_new, KUShortArray_free, KUShortArray_clone);
            impl_typed_array!(KIntArray, i32, 4, KIntArray_new, KIntArray_free, KIntArray_clone);
            impl_typed_array!(KUIntArray, u32, 4, KUIntArray_new, KUIntArray_free, KUIntArray_clone);
            impl_typed_array!(KLongArray, i64, 8, KLongArray_new, KLongArray_free, KLongArray_clone);
            impl_typed_array!(KULongArray, u64, 8, KULongArray_new, KULongArray_free, KULongArray_clone);
            impl_typed_array!(KFloatArray, f32, 4, KFloatArray_new, KFloatArray_free, KFloatArray_clone);
            impl_typed_array!(KDoubleArray, f64, 8, KDoubleArray_new, KDoubleArray_free, KDoubleArray_clone);
            
            #[macro_export] macro_rules! k_char_array {
                ($($x:expr),+ $(,)?) => (KCharArray::from(vec![$($x),+]));
            }
            #[macro_export] macro_rules! k_boolean_array {
                ($($x:expr),+ $(,)?) => (KBooleanArray::from(vec![$($x),+]));
            }
            #[macro_export] macro_rules! k_byte_array {
                ($($x:expr),+ $(,)?) => (KByteArray::from(vec![$($x),+]));
            }
            #[macro_export] macro_rules! k_ubyte_array {
                ($($x:expr),+ $(,)?) => (KUByteArray::from(vec![$($x),+]));
            }
            #[macro_export] macro_rules! k_short_array {
                ($($x:expr),+ $(,)?) => (KShortArray::from(vec![$($x),+]));
            }
            #[macro_export] macro_rules! k_ushort_array {
                ($($x:expr),+ $(,)?) => (KUShortArray::from(vec![$($x),+]));
            }
            #[macro_export] macro_rules! k_int_array {
                ($($x:expr),+ $(,)?) => (KIntArray::from(vec![$($x),+]));
            }
            #[macro_export] macro_rules! k_uint_array {
                ($($x:expr),+ $(,)?) => (KUIntArray::from(vec![$($x),+]));
            }
            #[macro_export] macro_rules! k_long_array {
                ($($x:expr),+ $(,)?) => (KLongArray::from(vec![$($x),+]));
            }
            #[macro_export] macro_rules! k_ulong_array {
                ($($x:expr),+ $(,)?) => (KULongArray::from(vec![$($x),+]));
            }
            #[macro_export] macro_rules! k_float_array {
                ($($x:expr),+ $(,)?) => (KFloatArray::from(vec![$($x),+]));
            }
            #[macro_export] macro_rules! k_double_array {
                ($($x:expr),+ $(,)?) => (KDoubleArray::from(vec![$($x),+]));
            }
            
        """.trimIndent())
        append($$"""
            
            // KArray
            
            pub struct KArray<T: PtrHolder> {
                ptr: *const _KArray,
                elements: Vec<T>
            }
            
            impl<T: PtrHolder> KArray<T> {
                fn wrap(of: *const _KArray) -> KArray<T> {
                    unsafe {
                        let _of = of.read();
                        let _elements = _of.elements as *const *const c_void;
                        let mut elements: Vec<T> = Vec::with_capacity(_of.length as usize);
            
                        for i in 0.._of.length {
                            let el = T::wrap(_elements.offset(i as isize).read());
                            elements.push(el)
                        }
                        KArray { ptr: of, elements }
                    }
                }
                fn wrap_nullable(ptr: *const _KArray) -> Option<KArray<T>> {
                    if (ptr == null()) {
                        return None
                    }
                    Some(KArray::wrap(ptr))
                }
                fn unwrap(self) -> *const _KArray {
                    let ptr = self.ptr;
                    std::mem::forget(self);
                    ptr
                }
                fn unwrap_nullable(of: Option<KArray<T>>) -> *const _KArray {
                    if(of.is_none()) {
                        return null()
                    }
                    of.unwrap().unwrap()
                }
                pub fn new(elements: Vec<T>) -> KArray<T> {
                    let mut ptrs: Vec<*const c_void> = Vec::with_capacity(elements.len());
                    for element in &elements {
                        ptrs.push(element.ptr());
                    }
                    let ptr = unsafe { KArray_new(ptrs.as_ptr(), elements.len() as i32, true) };
                    std::mem::forget(ptrs);
                    KArray { ptr, elements }
                }
                pub fn as_slice(&self) -> &[T] {
                    self.elements.as_slice()
                }
            }
            
            impl<T: PtrHolder> Drop for KArray<T> {
                fn drop(&mut self) {
                    unsafe { 
                        self.elements.set_len(0);
                        KArray_free(self.ptr, T::free_ptr()); 
                    }
                }
            }
            
            impl<T: PtrHolder> Clone for KArray<T> {
                fn clone(&self) -> Self {
                    KArray::wrap(unsafe { KArray_clone(self.ptr, T::clone_ptr()) })
                }
            }
            
            #[macro_export] macro_rules! k_array {
                ($($x:expr),+ $(,)?) => (KArray::new(vec![$($x),+]));
            }
            
            // KArrayOpt

            pub struct KArrayOpt<T: PtrHolder> {
                ptr: *const _KArray,
                elements: Vec<Option<T>>
            }

            impl<T: PtrHolder> KArrayOpt<T> {
                fn wrap(of: *const _KArray) -> KArrayOpt<T> {
                    unsafe {
                        let _of = of.read();
                        let _elements = _of.elements as *const *const c_void;
                        let mut elements: Vec<Option<T>> = Vec::with_capacity(_of.length as usize);

                        for i in 0.._of.length {
                            let p = _elements.offset(i as isize).read();
                            if(p == null()) {
                                elements.push(None)
                            } else {
                                let el = T::wrap(_elements.offset(i as isize).read());
                                elements.push(Some(el))
                            }
                        }
                        KArrayOpt { ptr: of, elements }
                    }
                }
                fn wrap_nullable(ptr: *const _KArray) -> Option<KArrayOpt<T>> {
                    if (ptr == null()) {
                        return None
                    }
                    Some(KArrayOpt::wrap(ptr))
                }
                fn unwrap(self) -> *const _KArray {
                    let ptr = self.ptr;
                    std::mem::forget(self);
                    ptr
                }
                fn unwrap_nullable(of: Option<KArrayOpt<T>>) -> *const _KArray {
                    if(of.is_none()) {
                        return null()
                    }
                    of.unwrap().unwrap()
                }
                pub fn new(elements: Vec<Option<T>>) -> KArrayOpt<T> {
                    let mut ptrs: Vec<*const c_void> = Vec::with_capacity(elements.len());
                    for element in &elements {
                        if(element.is_none()) {
                            ptrs.push(null())
                        } else {
                            ptrs.push(element.clone().unwrap().ptr());
                        }
                    }
                    let ptr = unsafe { KArray_new(ptrs.as_ptr(), elements.len() as i32, true) };
                    std::mem::forget(ptrs);
                    KArrayOpt { ptr, elements }
                }
                pub fn as_slice(&self) -> &[Option<T>] {
                    self.elements.as_slice()
                }
            }

            impl<T: PtrHolder> Drop for KArrayOpt<T> {
                fn drop(&mut self) {
                    unsafe {
                        self.elements.set_len(0);
                        KArray_free(self.ptr, T::free_ptr()); 
                    }
                }
            }

            impl<T: PtrHolder> Clone for KArrayOpt<T> {
                fn clone(&self) -> Self {
                    KArrayOpt::wrap(unsafe { KArray_clone(self.ptr, T::clone_ptr()) })
                }
            }
            
            #[macro_export] macro_rules! k_array_opt {
                ($($x:expr),+ $(,)?) => (KArrayOpt::new(vec![$($x),+]));
            }
            
        """.trimIndent())
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
                
                fn ${name}_new($args) -> *const _${name};
                fn ${name}_clone(self_: *const _${name}) -> *const _${name};
                fn ${name}_free(self_: *const _${name});
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
            append("\n\t\tSelf::wrap(unsafe { ${name}_new(")
            fields.joinTo(this) {
                toNativeType(it.type, it.name.snakeCase())
            }
            append(") })\n\t}\n")

            // wraps
            append("""
                fn wrap(ptr: *const _$name) -> Self {
                    let r = unsafe { ptr.read() };
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
                impl_drop_clone!($name, ${name}_free, ${name}_clone);
                impl_ptr_holder!($name, _$name, ${name}_free, ${name}_clone);
                
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
                            unsafe { (self.ptr.read().equals)(self.ptr, other.ptr) }
                        }
                        pub fn hash_code(&self) -> i32 {
                            unsafe { (self.ptr.read().hash_code)(self.ptr) }
                        }
                    }
                    
                    impl_wrapper!($name, $inner);
                    
                    impl Drop for $name {
                        fn drop(&mut self) {
                            unsafe { (self.ptr.read().free)(self.ptr) }
                        }
                    }
                    
                    impl Clone for $name {
                        fn clone(&self) -> Self {
                            unsafe { $name::wrap((self.ptr.read().clone)(self.ptr)) }
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
                append("(self.ptr.read().invoke)")
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

}