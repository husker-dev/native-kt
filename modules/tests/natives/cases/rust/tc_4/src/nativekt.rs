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
unsafe extern "C" fn nativekt_natives_tc_4_rust_tc_4_rust__init() {}

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
    fn nativekt_natives_tc_4_rust_tc_4_rust__alloc(size: usize) -> *mut u8 as A_ {
        unsafe { alloc(Layout::from_size_align_unchecked(size, 1)) }
    }
    fn nativekt_natives_tc_4_rust_tc_4_rust__dealloc(ptr: *mut u8, size: usize) -> () as B_ {
        if ptr.is_null() { return }
        unsafe { dealloc(ptr, Layout::from_size_align_unchecked(size, 1)) }
    }
}

// ╔═══════════════════╗
// ║     Functions     ║
// ╚═══════════════════╝

export_fn!{ fn nativekt_natives_tc_4_rust_tc_4_rust_stub1(e: *mut Arc<crate::EmptyInterface>) -> () as M_ {
    crate::stub1(&from_raw(e))
}}
export_fn!{ fn nativekt_natives_tc_4_rust_tc_4_rust_stub2(e: *mut Arc<crate::MyInterface>) -> () as N_ {
    crate::stub2(&from_raw(e))
}}
export_fn!{ fn nativekt_natives_tc_4_rust_tc_4_rust__interface_emptyinterface_free(_self: *mut Arc<crate::EmptyInterface>) -> () as O_ {
    from_raw(_self);
}}
export_fn!{ fn nativekt_natives_tc_4_rust_tc_4_rust__interface_emptyinterface_clone(_self: *mut Arc<crate::EmptyInterface>) -> *mut Arc<crate::EmptyInterface> as P_ {
    into_raw(unsafe { &*_self }.clone())
}}
export_fn!{ fn nativekt_natives_tc_4_rust_tc_4_rust__interface_emptyinterface_address(_self: *mut Arc<crate::EmptyInterface>) -> usize as Q_ {
    Arc::as_ptr(unsafe { &*_self }) as usize
}}
export_fn!{ fn nativekt_natives_tc_4_rust_tc_4_rust__interface_myinterface_fn_func(_self: *mut Arc<crate::MyInterface>) -> () as R_ {
    unsafe { &*_self }.func()
}}
export_fn!{ fn nativekt_natives_tc_4_rust_tc_4_rust__interface_myinterface_free(_self: *mut Arc<crate::MyInterface>) -> () as S_ {
    from_raw(_self);
}}
export_fn!{ fn nativekt_natives_tc_4_rust_tc_4_rust__interface_myinterface_clone(_self: *mut Arc<crate::MyInterface>) -> *mut Arc<crate::MyInterface> as T_ {
    into_raw(unsafe { &*_self }.clone())
}}
export_fn!{ fn nativekt_natives_tc_4_rust_tc_4_rust__interface_myinterface_address(_self: *mut Arc<crate::MyInterface>) -> usize as U_ {
    Arc::as_ptr(unsafe { &*_self }) as usize
}}