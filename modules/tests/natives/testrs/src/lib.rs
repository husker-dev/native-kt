#![allow(unused)]

use std::ffi::*;
use std::ops::Deref;
use std::ptr::null;

const FLAG_RELEASABLE: i8 = 1;

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
    size: usize,
    length: KInt,
    __flags: i8
}

extern "C" {
    fn KString_new(data: *mut c_char, length: KInt, size: usize) -> *mut KString;
    fn KString_free(self_: *const KString);
    fn KString_clone(self_: *const KString) -> *mut KString;
}

impl KString {
    pub fn read(ptr: *const KString) -> Option<String> {
        unsafe {
            if(ptr == null()) {
                return None;
            }
            let str_struct = ptr.read();
            let str = std::str::from_utf8_unchecked(
                std::slice::from_raw_parts(str_struct.data as *const u8, str_struct.size)
            );
            Some(str.to_string())
        }
    }
}
/*
impl From<String> for KString {
    fn from(value: String) -> Self {
        let length = value.len() as KInt;
        let mut bytes = value.into_bytes();
        let size = bytes.len();
        let data = bytes.as_mut_ptr() as *mut c_char;
        std::mem::forget(bytes);
        KString_new(data, length, size)
    }
}

impl Drop for KString {
    fn drop(&mut self) {
        unsafe {
            KString_free(self);
        }
    }
}

impl Clone for KString {
    fn clone(&self) -> Self {
        unsafe {
            KString_clone(self).to_owned()
        }
    }
}

 */


#[no_mangle]
extern "C" fn test_func(
    k: *const KString
) {
    // Take &str from 'k' ('k' won't be released)
    let str_var = KString::read(k);
    println!("rust: {}", str_var.unwrap_or("null".to_string()));

    // Create string inside function (will be released)
    //let some_string = KString::from("Some KString".to_string());

    // Create and return string (won't be released)
   // "return string".to_string().into()
}