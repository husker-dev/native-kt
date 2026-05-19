package com.huskerdev.nativekt.printers.rust

import com.huskerdev.webidl.resolver.IdlResolver
import java.io.File

class RustPrinter(
    val idl: IdlResolver,
    target: File
) {
    init {
        target.parentFile.mkdirs()

        val builder = StringBuilder()
        builder.append("""
            #![allow(unused)]
            
            use std::ffi::*;

            type KInt     = i32;
            type KLong    = i64;
            type KFloat   = f32;
            type KDouble  = f64;
            type KByte    = i8;
            type KShort   = i16;
            type KBoolean = bool;
            type KChar    = i16;
            
            #[repr(C)]
            #[derive(Debug)]
            pub struct KString {
                data: *mut c_char,
                length: KInt,
                releasable: bool
            }
            
            impl KString {
                pub fn from_string(s: String) -> KString {
                    let mut bytes = s.into_bytes();
                    let length = bytes.len() as KInt;
                    let ptr = bytes.as_mut_ptr();
                    std::mem::forget(bytes);
                    KString { data: ptr as *mut c_char, length, releasable: true }
                }
            
                pub fn to_str(&self) -> &str {
                    unsafe {
                        std::str::from_utf8_unchecked(
                            std::slice::from_raw_parts(self.data as *const u8, self.length as usize)
                        )
                    }
                }
            }
            
            impl Drop for KString {
                fn drop(&mut self) {
                    unsafe {
                        if self.releasable {
                            let _ = Vec::from_raw_parts(self.data, self.length as usize, self.length as usize);
                        }
                    }
                }
            }
            
            
        """.trimIndent())

        target.writeText(builder.toString())
    }
}