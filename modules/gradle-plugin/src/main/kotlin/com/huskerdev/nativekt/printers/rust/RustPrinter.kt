package com.huskerdev.nativekt.printers.rust

import com.huskerdev.nativekt.utils.globalOperators
import com.huskerdev.nativekt.utils.isBoolean
import com.huskerdev.nativekt.utils.isByte
import com.huskerdev.nativekt.utils.isChar
import com.huskerdev.nativekt.utils.isDouble
import com.huskerdev.nativekt.utils.isFloat
import com.huskerdev.nativekt.utils.isInt
import com.huskerdev.nativekt.utils.isLong
import com.huskerdev.nativekt.utils.isPrimitive
import com.huskerdev.nativekt.utils.isShort
import com.huskerdev.nativekt.utils.isString
import com.huskerdev.nativekt.utils.isVoid
import com.huskerdev.nativekt.utils.printLabel
import com.huskerdev.nativekt.utils.toCType
import com.huskerdev.webidl.resolver.IdlResolver
import com.huskerdev.webidl.resolver.ResolvedIdlOperation
import com.huskerdev.webidl.resolver.ResolvedIdlType
import java.io.File

class RustPrinter(
    val idl: IdlResolver,
    target: File
) {
    init {
        target.parentFile.mkdirs()

        val builder = StringBuilder()
        builder.printHeaderDef()
        builder.printStringDef()

        builder.append("\n")
        printLabel(builder, "Functions")
        idl.globalOperators().forEach {
            builder.printFunction(it)
        }

        target.writeText(builder.toString())
    }

    private fun StringBuilder.printFunction(operation: ResolvedIdlOperation) {
        append("\n#[no_mangle]")

        // Header
        append("\nextern \"C\" fn ${operation.name}(")
        operation.args.joinTo(this, ",") {
            "\n\t${it.name}: ${it.type.toNativeRustType()}"
        }
        if(operation.args.isNotEmpty())
            append("\n")
        append(") ")
        if(!operation.type.isVoid())
            append("-> ${operation.type.toNativeRustType()} ")
        append("{\n\t")

        // Call
        val call = buildString {
            append("crate::${operation.name}")
            operation.args.joinTo(this, prefix = "(", postfix = ")") {
                toRustType(it.type, it.name)
            }
        }
        append(toNativeType(operation.type, call))
        append("\n}\n")
    }

    private fun toNativeType(type: ResolvedIdlType, content: String): String {
        val nullable = if(type.isNullable) "_nullable" else ""
        return when {
            type.isString() -> "KString::unwrap$nullable($content)"
            else -> content
        }
    }

    private fun toRustType(type: ResolvedIdlType, content: String): String {
        val nullable = if(type.isNullable) "_nullable" else ""
        return when {
            type.isString() -> "KString::wrap$nullable($content)"
            else -> content
        }
    }

    private fun ResolvedIdlType.toNativeRustType(): String = when {
        isChar() -> "i16"
        isBoolean() -> "bool"
        isByte() -> "i8"
        isShort() -> "i16"
        isInt() -> "i32"
        isLong() -> "i64"
        isFloat() -> "f32"
        isDouble() -> "f64"
        isString() -> "*const _KString"
        isPrimitive() -> this.toCType(ptr = false)
        else -> throw UnsupportedOperationException()
    }

    private fun StringBuilder.printHeaderDef() {
        append("""
            #![allow(unused)]
            
            use std::ffi::c_void;
            use std::ptr::null;
            
            extern "C" {
                fn malloc(s: usize) -> *const c_void;
            }
            
        """.trimIndent())
    }

    private fun StringBuilder.printStringDef() {
        printLabel(this, "String")
        append("""
            
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
            
                fn wrap(ptr: *const _KString) -> KString {
                    KString { ptr }
                }
            
                fn wrap_nullable(ptr: *const _KString) -> Option<KString> {
                    if(ptr == null()) {
                        return None
                    }
                    Some(KString { ptr })
                }
            
                fn unwrap(self) -> *const _KString {
                    let ptr = self.ptr;
                    std::mem::forget(self);
                    ptr
                }
            
                fn unwrap_nullable(of: Option<KString>) -> *const _KString {
                    if(of.is_none()) {
                        return null()
                    }
                    of.unwrap().ptr
                }
            
                pub fn from(value: &str) -> KString {
                    unsafe {
                        let length = value.chars().count() as i32;
                        let size = value.len();
                        let data = malloc(size) as *mut u8;
                        std::ptr::copy_nonoverlapping(value.as_ptr(), data, size);
                        KString { ptr: KString_new(data, length, size) }
                    }
                }
            
                pub fn from_string(value: &String) -> KString {
                    KString::from(value.as_str())
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
                    value.as_str().to_string()
                }
            }
            
            impl Drop for KString {
                fn drop(&mut self) {
                    unsafe {
                        KString_free(self.ptr);
                    }
                }
            }
            
            impl Clone for KString {
                fn clone(&self) -> Self {
                    unsafe {
                        KString { ptr: KString_clone(self.ptr) }
                    }
                }
            }
            
            extern "C" {
                fn KString_new(data: *const u8, length: i32, size: usize) -> *const _KString;
                fn KString_free(self_: *const _KString);
                fn KString_clone(self_: *const _KString) -> *const _KString;
            }
        """.trimIndent())
    }
}