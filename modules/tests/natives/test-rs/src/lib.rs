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
    pub fn to_str(&self) -> &str {
        unsafe {
            std::str::from_utf8_unchecked(
                std::slice::from_raw_parts(self.data as *const u8, self.length as usize)
            )
        }
    }
}

impl From<String> for KString {
    fn from(value: String) -> Self {
        let mut bytes = value.into_bytes();
        let length = bytes.len() as KInt;
        let ptr = bytes.as_mut_ptr();
        std::mem::forget(bytes);
        KString { data: ptr as *mut c_char, length, releasable: true }
    }
}

impl Drop for KString {
    fn drop(&mut self) {
        unsafe {
            if self.reasable {
                let _ = Vec::from_raw_parts(self.data, self.length as usize, self.length as usize);
            }
        }
    }
}


#[no_mangle]
extern "C" fn test_func(
    k: KString
) -> KString {
    // Take &str from 'k' ('k' won't be released)
    let str_var = k.to_str();

    // Create string inside function (will be released)
    let some_string = KString::from("Some KString".to_string());

    // Create and return string (won't be released)
    "return string".to_string().into()
}