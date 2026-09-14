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
unsafe extern "C" fn nativekt_natives_tc_6_rust_tc_6_rust__init() {}

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
    fn nativekt_natives_tc_6_rust_tc_6_rust__alloc(size: usize) -> *mut u8 as A_ {
        unsafe { alloc(Layout::from_size_align_unchecked(size, 1)) }
    }
    fn nativekt_natives_tc_6_rust_tc_6_rust__dealloc(ptr: *mut u8, size: usize) -> () as B_ {
        if ptr.is_null() { return }
        unsafe { dealloc(ptr, Layout::from_size_align_unchecked(size, 1)) }
    }
}

// ╔══════════════════════════╗
// ║     Primitive arrays     ║
// ╚══════════════════════════╝

macro_rules! impl_typed_array {
    ($rsType:ident, 
	$funcNew:ident, $funcNewJs:ident, 
	$funcData:ident, $funcDataJs:ident, 
	$funcLength:ident, $funcLengthJs:ident, 
	$funcFree:ident, $funcFreeJs:ident) => {
		export_fn! {
			fn $funcNew(elements: *mut $rsType, length: i32, make_copy: bool) -> *mut Vec<$rsType> as $funcNewJs {
				if(make_copy) {
                    unsafe { into_raw(std::slice::from_raw_parts(elements, length as usize).to_vec()) }
                } else {
                    unsafe { into_raw(Vec::from_raw_parts(elements, length as usize, length as usize)) }
                }
			}
			fn $funcData(of: *mut Vec<$rsType>) -> *mut $rsType as $funcDataJs {
				unsafe { (*of).as_mut_ptr() }
			}
			fn $funcLength(of: *mut Vec<$rsType>) -> i32 as $funcLengthJs {
				unsafe { (*of).len() as i32 }
			}
			fn $funcFree(of: *mut Vec<$rsType>) -> () as $funcFreeJs {
				drop(from_raw(of));
			}
		}
    };
}
impl_typed_array!(i32,
    nativekt_natives_tc_6_rust_tc_6_rust__intarray_new, C_,
    nativekt_natives_tc_6_rust_tc_6_rust__intarray_elements, D_,
    nativekt_natives_tc_6_rust_tc_6_rust__intarray_length, E_,
    nativekt_natives_tc_6_rust_tc_6_rust__intarray_free, F_
);
impl_typed_array!(i64,
    nativekt_natives_tc_6_rust_tc_6_rust__longarray_new, G_,
    nativekt_natives_tc_6_rust_tc_6_rust__longarray_elements, H_,
    nativekt_natives_tc_6_rust_tc_6_rust__longarray_length, I_,
    nativekt_natives_tc_6_rust_tc_6_rust__longarray_free, J_
);

// ╔═══════════════╗
// ║     Enums     ║
// ╚═══════════════╝

#[cfg_attr(target_arch = "wasm32", wasm_bindgen)]
#[repr(C)]
#[derive(PartialEq, Eq, Clone)]
pub enum MyEnum {
	A = 0
}

impl MyEnum {
    pub fn from_int(value: i32) -> Self {
        match value {
			0 => MyEnum::A,
            _ => panic!()
        }
    }
    pub fn to_int(&self) -> i32 {
        self.clone() as i32
    }
}

// ╔═══════════════════╗
// ║     Functions     ║
// ╚═══════════════════╝

export_fn!{ fn nativekt_natives_tc_6_rust_tc_6_rust_stub1(e: *mut Vec<i64>) -> () as K_ {
    crate::stub1(&from_raw(e))
}}
export_fn!{ fn nativekt_natives_tc_6_rust_tc_6_rust_stub2(e: *mut Vec<MyEnum>) -> () as L_ {
    crate::stub2(&from_raw(e))
}}