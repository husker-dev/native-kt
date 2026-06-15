#![allow(unused)]

use std::ffi::c_void;
use std::ptr::null;

extern "C" {
    fn malloc(s: usize) -> *const c_void;
}

// ╔════════════════╗
// ║     String     ║
// ╚════════════════╝

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

// ╔═══════════════════╗
// ║     Functions     ║
// ╚═══════════════════╝

#[no_mangle]
extern "C" fn test_func(
	arg: *const _KString,
	arg2: i32
) -> *const _KString {
	KString::unwrap(crate::test_func(KString::wrap_nullable(arg), arg2))
}
