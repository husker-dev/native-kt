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
unsafe extern "C" fn nativekt_natives_tc_5_rust_tc_5_rust__init() {}

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

// Macro: External function type declaration

#[cfg(target_arch = "wasm32")]
use wasm_bindgen::JsValue;

#[cfg(target_arch = "wasm32")]
macro_rules! external_fn_type {
    ($ret:ty; $($arg:ty),* $(,)?) => {
        wasm_bindgen::JsValue
    };
}

#[cfg(not(target_arch = "wasm32"))]
macro_rules! external_fn_type {
    ($ret:ty; $($arg:ty),* $(,)?) => {
        extern "C" fn($($arg),*) -> $ret
    };
}

// Macro: External function call

#[cfg(target_arch = "wasm32")]
#[wasm_bindgen]
extern "C" {
	#[wasm_bindgen(js_namespace = Reflect, js_name = apply)]
	fn reflect_apply(target: &JsValue, this_arg: &JsValue, args: &JsValue) -> JsValue;
}

#[cfg(target_arch = "wasm32")]
macro_rules! external_fn_call {
    ((); $cb:expr; $($arg:expr),* $(,)?) => {{
        let __args = JsValue::from(vec![$(JsValue::from($arg)),+]);
        reflect_apply(&$cb, &JsValue::UNDEFINED, &__args);
    }};
    (bool; $cb:expr; $($arg:expr),* $(,)?) => {{
        let __args = JsValue::from(vec![$(JsValue::from($arg)),+]);
        reflect_apply(&$cb, &JsValue::UNDEFINED, &__args).as_bool().unwrap()
    }};
    (i64; $cb:expr; $($arg:expr),* $(,)?) => {{
        let __args = JsValue::from(vec![$(JsValue::from($arg)),+]);
        i64::try_from(reflect_apply(&$cb, &JsValue::UNDEFINED, &__args)).unwrap()
    }};
    (u64; $cb:expr; $($arg:expr),* $(,)?) => {{
        let __args = JsValue::from(vec![$(JsValue::from($arg)),+]);
        i64::try_from(reflect_apply(&$cb, &JsValue::UNDEFINED, &__args)).unwrap() as u64
    }};
    (enum $t:ty; $cb:expr; $($arg:expr),* $(,)?) => {{
        let __args = JsValue::from(vec![$(JsValue::from($arg)),+]);
        let __r = reflect_apply(&$cb, &JsValue::UNDEFINED, &__args).as_f64().unwrap() as i32;
        unsafe { std::mem::transmute::<i32, $t>(__r) }
    }};
    (*mut $t:ty; $cb:expr; $($arg:expr),* $(,)?) => {{
        let __args = JsValue::from(vec![$(JsValue::from($arg)),+]);
        let __r = reflect_apply(&$cb, &JsValue::UNDEFINED, &__args);
        (__r.as_f64().unwrap_or(0.0) as usize) as *mut $t
    }};
    ($ret:ty; $cb:expr; $($arg:expr),* $(,)?) => {{
        let __args = JsValue::from(vec![$(JsValue::from($arg)),+]);
        reflect_apply(&$cb, &JsValue::UNDEFINED, &__args).as_f64().unwrap() as $ret
    }};
}

#[cfg(not(target_arch = "wasm32"))]
macro_rules! external_fn_call {
	(enum $ret:ty; $cb:expr; $($arg:expr),* $(,)?) => { $cb($($arg),*) };
    ($ret:ty; $cb:expr; $($arg:expr),* $(,)?) => { $cb($($arg),*) };
}

// ╔═══════════════════╗
// ║     Callbacks     ║
// ╚═══════════════════╝

macro_rules! impl_callback {
    ($name:ident, $invoke_type:ty, $invoke_body:item, 
	$funcNew:ident, $funcNewJs:ident, 
	$funcId:ident, $funcIdJs:ident,
	$funcFree:ident, $funcFreeJs:ident) => {
        pub struct $name {
			id: isize,
			invoke: $invoke_type,
			equals: external_fn_type!(bool; isize, isize),
			hash_code: i32,
			free: external_fn_type!((); isize)
		}
		impl $name {
			$invoke_body
		}
		impl Drop for $name {
            fn drop(&mut self) { external_fn_call!((); self.free; self.id) }
        }
        impl Hash for $name {
            fn hash<H: Hasher>(&self, state: &mut H) { self.hash_code.hash(state); }
        }
        impl PartialEq for $name {
            fn eq(&self, other: &Self) -> bool { external_fn_call!(bool; self.equals; self.id, other.id) }
        }
        impl Eq for $name {}
		
		export_fn! {
			fn $funcNew(
				id: isize,
				hash_code: i32,
				invoke: $invoke_type,
				equals: external_fn_type!(bool; isize, isize),
				free: external_fn_type!((); isize)
			) -> *mut Arc<$name> as $funcNewJs {
				into_raw(Arc::new($name { id, invoke, equals, hash_code, free }))
			}
			fn $funcId(_self: *mut Arc<$name>) -> isize as $funcIdJs {
				unsafe { (&*_self).id }
			}
			fn $funcFree(_self: *mut Arc<$name>) -> () as $funcFreeJs {
				from_raw(_self);
			}
		}
    };
}
impl_callback!(
    VoidCallback,
	external_fn_type!((); isize),
	pub fn invoke(&self) { external_fn_call!((); self.invoke; self.id) },
	nativekt_natives_tc_5_rust_tc_5_rust__voidcallback_new, A_,
	nativekt_natives_tc_5_rust_tc_5_rust__voidcallback_id, B_,
	nativekt_natives_tc_5_rust_tc_5_rust__voidcallback_free, C_
);

// ╔═══════════════════╗
// ║     Functions     ║
// ╚═══════════════════╝

export_fn!{ fn nativekt_natives_tc_5_rust_tc_5_rust_stub(e: *mut Arc<VoidCallback>) -> () as D_ {
    crate::stub(from_raw(e))
}}