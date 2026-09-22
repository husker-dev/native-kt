#![allow(unused)]

use std::ffi::c_void;
use std::ptr::null_mut;
use std::hash::{Hasher, Hash};
use std::mem::ManuallyDrop;
use std::sync::Arc;
use std::alloc::{alloc, dealloc, Layout};

#[cfg(target_arch = "wasm32")]
use wasm_bindgen::prelude::*;

#[cfg(not(target_arch = "wasm32"))]
#[no_mangle] 
unsafe extern "C" fn nativekt_natives_jvmcibindings_jvmci_bindings__init() {}

// Macro: Function export
macro_rules! export_fn {
    (@parse
        fn $native:ident($($arg:ident : $ty:ty),* $(,)?) -> $ret:ty as $js:ident $body:block
        $($rest:tt)*
    ) => {
        #[cfg(target_arch = "wasm32")]
        #[wasm_bindgen]
        #[allow(non_snake_case)]
        pub fn $js($($arg: $ty),*) -> $ret $body

        #[cfg(not(target_arch = "wasm32"))]
        #[no_mangle]
        extern "C" fn $native($($arg: $ty),*) -> $ret $body

        export_fn!(@parse $($rest)*);
    };
	(@parse) => {};
	($($item:tt)*) => {
        export_fn!(@parse $($item)*);
    };
}

fn from_raw<T>(of: *mut T) -> T {
    unsafe { *Box::from_raw(of) }
}

fn into_raw<T>(of: T) -> *mut T {
    Box::into_raw(Box::new(of))
}

export_fn! {
    fn nativekt_natives_jvmcibindings_jvmci_bindings__alloc(size: usize) -> *mut u8 as A_ {
        unsafe { alloc(Layout::from_size_align_unchecked(size, 1)) }
    }
    fn nativekt_natives_jvmcibindings_jvmci_bindings__dealloc(ptr: *mut u8, size: usize) -> () as B_ {
        if ptr.is_null() { return }
        unsafe { dealloc(ptr, Layout::from_size_align_unchecked(size, 1)) }
    }
}

// ╔════════════════╗
// ║     String     ║
// ╚════════════════╝

export_fn! {	
	fn nativekt_natives_jvmcibindings_jvmci_bindings__string_new(data: *mut u8, size: i32, make_copy: bool) -> *mut String as C_ {
	    if make_copy {
	        unsafe { into_raw(String::from_utf8_unchecked(std::slice::from_raw_parts(data, size as usize).to_vec())) }
	    } else {
	        unsafe { into_raw(String::from_raw_parts(data, size as usize, size as usize)) }
	    }
	}

}

// ╔════════════════════════════╗
// ║     Critical functions     ║
// ╚════════════════════════════╝

fn critical_string(data: *mut u8, size: i32) -> ManuallyDrop<String> {
    unsafe { ManuallyDrop::new(String::from_raw_parts(data, size as usize, size as usize)) }
}

// ╔═══════════════════╗
// ║     Functions     ║
// ╚═══════════════════╝

export_fn!{ fn nativekt_natives_jvmcibindings_jvmci_bindings_call_critical_jvmci() -> () as L_ {
    crate::call_critical_jvmci()
}}
export_fn!{ fn nativekt_natives_jvmcibindings_jvmci_bindings_call_critical_jvmci_add(a: i32, b: i32) -> i32 as M_ {
    crate::call_critical_jvmci_add(a, b)
}}
export_fn!{ fn nativekt_natives_jvmcibindings_jvmci_bindings_call_critical_jvmci_string(arg: *mut u8, _arg_size: i32) -> () as N_ {
    crate::call_critical_jvmci_string(&critical_string(arg, _arg_size))
}}