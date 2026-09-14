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
unsafe extern "C" fn nativekt_natives_tc_3_rust_tc_3_rust__init() {}

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

// ╔═════════════════╗
// ║     Structs     ║
// ╚═════════════════╝

#[derive(Clone)]
pub struct EmptyDictionary {
}
export_fn! {
	fn nativekt_natives_tc_3_rust_tc_3_rust__emptydictionary_new() -> *mut EmptyDictionary as P_ {
		into_raw(EmptyDictionary {  })
	}
}

#[derive(Clone)]
pub struct EmptyDictionary1 {
}
export_fn! {
	fn nativekt_natives_tc_3_rust_tc_3_rust__emptydictionary1_new() -> *mut EmptyDictionary1 as Q_ {
		into_raw(EmptyDictionary1 {  })
	}
}

#[derive(Clone)]
pub struct MyDictionary {
	pub a: i32
}
export_fn! {
	fn nativekt_natives_tc_3_rust_tc_3_rust__mydictionary_new(a: i32) -> *mut MyDictionary as R_ {
		into_raw(MyDictionary { a })
	}
}

// ╔═══════════════════╗
// ║     Functions     ║
// ╚═══════════════════╝

export_fn!{ fn nativekt_natives_tc_3_rust_tc_3_rust_stub1(e: *mut EmptyDictionary1) -> () as S_ {
    crate::stub1(&from_raw(e))
}}
export_fn!{ fn nativekt_natives_tc_3_rust_tc_3_rust_stub2(e: *mut MyDictionary) -> () as T_ {
    crate::stub2(&from_raw(e))
}}