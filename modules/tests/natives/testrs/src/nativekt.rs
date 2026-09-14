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
unsafe extern "C" fn nativekt_natives_testrs_testrs__init() {}

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

fn ptr_opt<T, R>(ptr: *mut T, f: fn(*mut T) -> R) -> Option<R> {
    if ptr.is_null() { None } else { Some(f(ptr)) }
}

fn obj_opt<T, R>(ptr: Option<R>, f: fn(R) -> *mut T) -> *mut T {
    if ptr.is_none() { null_mut() } else { f(ptr.unwrap()) }
}

export_fn! {
    fn nativekt_natives_testrs_testrs__alloc(size: usize) -> *mut u8 as A_ {
        unsafe { alloc(Layout::from_size_align_unchecked(size, 1)) }
    }
    fn nativekt_natives_testrs_testrs__dealloc(ptr: *mut u8, size: usize) -> () as B_ {
        if ptr.is_null() { return }
        unsafe { dealloc(ptr, Layout::from_size_align_unchecked(size, 1)) }
    }
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

// ╔════════════════╗
// ║     String     ║
// ╚════════════════╝

export_fn! {	
	fn nativekt_natives_testrs_testrs__string_new(data: *mut u8, _length: i32, size: i32, make_copy: bool) -> *mut String as C_ {
	    if make_copy {
	        unsafe { into_raw(String::from_utf8_unchecked(std::slice::from_raw_parts(data, size as usize).to_vec())) }
	    } else {
	        unsafe { into_raw(String::from_raw_parts(data, size as usize, size as usize)) }
	    }
	}
	
	fn nativekt_natives_testrs_testrs__string_data(str: *mut String) -> *mut u8 as D_ {
	    unsafe { (&*str).as_ptr().cast_mut() }
	}
	fn nativekt_natives_testrs_testrs__string_length(str: *mut String) -> i32 as F_ {
	    unsafe { (&*str).chars().count() as i32 }
	}
	fn nativekt_natives_testrs_testrs__string_size(str: *mut String) -> i32 as E_ {
	    unsafe { (&*str).len() as i32 }
	}
	fn nativekt_natives_testrs_testrs__string_free(str: *mut String) -> () as G_ {
	    from_raw(str);
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
impl_typed_array!(u16,
    nativekt_natives_testrs_testrs__chararray_new, H_,
    nativekt_natives_testrs_testrs__chararray_elements, I_,
    nativekt_natives_testrs_testrs__chararray_length, J_,
    nativekt_natives_testrs_testrs__chararray_free, K_
);
impl_typed_array!(bool,
    nativekt_natives_testrs_testrs__booleanarray_new, L_,
    nativekt_natives_testrs_testrs__booleanarray_elements, M_,
    nativekt_natives_testrs_testrs__booleanarray_length, N_,
    nativekt_natives_testrs_testrs__booleanarray_free, O_
);
impl_typed_array!(i8,
    nativekt_natives_testrs_testrs__bytearray_new, P_,
    nativekt_natives_testrs_testrs__bytearray_elements, Q_,
    nativekt_natives_testrs_testrs__bytearray_length, R_,
    nativekt_natives_testrs_testrs__bytearray_free, S_
);
impl_typed_array!(i16,
    nativekt_natives_testrs_testrs__shortarray_new, T_,
    nativekt_natives_testrs_testrs__shortarray_elements, U_,
    nativekt_natives_testrs_testrs__shortarray_length, V_,
    nativekt_natives_testrs_testrs__shortarray_free, W_
);
impl_typed_array!(i32,
    nativekt_natives_testrs_testrs__intarray_new, X_,
    nativekt_natives_testrs_testrs__intarray_elements, Y_,
    nativekt_natives_testrs_testrs__intarray_length, Z_,
    nativekt_natives_testrs_testrs__intarray_free, a_
);
impl_typed_array!(i64,
    nativekt_natives_testrs_testrs__longarray_new, b_,
    nativekt_natives_testrs_testrs__longarray_elements, c_,
    nativekt_natives_testrs_testrs__longarray_length, d_,
    nativekt_natives_testrs_testrs__longarray_free, e_
);
impl_typed_array!(f32,
    nativekt_natives_testrs_testrs__floatarray_new, f_,
    nativekt_natives_testrs_testrs__floatarray_elements, g_,
    nativekt_natives_testrs_testrs__floatarray_length, h_,
    nativekt_natives_testrs_testrs__floatarray_free, i_
);
impl_typed_array!(f64,
    nativekt_natives_testrs_testrs__doublearray_new, j_,
    nativekt_natives_testrs_testrs__doublearray_elements, k_,
    nativekt_natives_testrs_testrs__doublearray_length, l_,
    nativekt_natives_testrs_testrs__doublearray_free, m_
);

// ╔══════════════════════╗
// ║     Object array     ║
// ╚══════════════════════╝

macro_rules! impl_object_array {
    ($T:ty, 
    $funcNew:ident, $funcNewJs:ident, 
    $funcLength:ident, $funcLengthJs:ident,
    $funcPush:ident, $funcPushJs:ident, 
    $funcGet:ident, $funcGetJs:ident, 
    $funcFree:ident, $funcFreeJs:ident) => {
        export_fn! {
            fn $funcNew(capacity: i32, nullable_elements: bool) -> *mut c_void as $funcNewJs {
                match nullable_elements {
                    true => into_raw(Vec::<Option<$T>>::with_capacity(capacity as usize)) as *mut c_void,
                    false => into_raw(Vec::<$T>::with_capacity(capacity as usize)) as *mut c_void,
                }
            }
            fn $funcLength(arr: *mut c_void, nullable_elements: bool) -> i32 as $funcLengthJs {
                unsafe { match nullable_elements {
                    true => (&mut *(arr as *mut Vec<Option<$T>>)).len() as i32,
                    false => (&mut *(arr as *mut Vec<$T>)).len() as i32
                } }
            }
            fn $funcPush(arr: *mut c_void, element: *mut c_void, nullable_elements: bool) -> () as $funcPushJs {
                unsafe { if(nullable_elements) {
                    let arr = &mut *(arr as *mut Vec<Option<$T>>);
                    arr.push(match element.is_null() {
                        true => None,
                        false => Some(*Box::from_raw(element as *mut $T)),
                    });
                } else {
                    let arr = &mut *(arr as *mut Vec<$T>);
                    arr.push(*Box::from_raw(element as *mut $T));
                } }
            }
            fn $funcGet(arr: *mut c_void, index: i32, nullable_elements: bool) -> *mut c_void as $funcGetJs {
                unsafe { if(nullable_elements) {
                    let arr = &mut *(arr as *mut Vec<Option<$T>>);
                    let element = arr.get_unchecked_mut(index as usize);
                    match element {
                        Some(obj) => obj as *mut $T as *mut c_void,
                        None => null_mut(),
                    }
                } else {
                    let arr = &mut *(arr as *mut Vec<$T>);
                    arr.get_unchecked_mut(index as usize) as *mut $T as *mut c_void
                } }
            }
            fn $funcFree(arr: *mut c_void, nullable_elements: bool) -> () as $funcFreeJs {
                if(nullable_elements) {
                    drop(from_raw(arr as *mut Vec<Option<$T>>));
                } else {
                    drop(from_raw(arr as *mut Vec<$T>));
                }
            }
        }
    };
}

impl_object_array!(ParentDictionary,
	nativekt_natives_testrs_testrs__array_parentdictionary_new, n_,
    nativekt_natives_testrs_testrs__array_parentdictionary_length, o_,
    nativekt_natives_testrs_testrs__array_parentdictionary_push, p_,
    nativekt_natives_testrs_testrs__array_parentdictionary_get, q_,
    nativekt_natives_testrs_testrs__array_parentdictionary_free, r_
);
impl_object_array!(MyDictionary,
	nativekt_natives_testrs_testrs__array_mydictionary_new, s_,
    nativekt_natives_testrs_testrs__array_mydictionary_length, t_,
    nativekt_natives_testrs_testrs__array_mydictionary_push, u_,
    nativekt_natives_testrs_testrs__array_mydictionary_get, v_,
    nativekt_natives_testrs_testrs__array_mydictionary_free, w_
);
impl_object_array!(TypeDictionary,
	nativekt_natives_testrs_testrs__array_typedictionary_new, x_,
    nativekt_natives_testrs_testrs__array_typedictionary_length, y_,
    nativekt_natives_testrs_testrs__array_typedictionary_push, z_,
    nativekt_natives_testrs_testrs__array_typedictionary_get, BA_,
    nativekt_natives_testrs_testrs__array_typedictionary_free, BB_
);
impl_object_array!(Arc<crate::MyInterface>,
	nativekt_natives_testrs_testrs__array_myinterface_new, BC_,
    nativekt_natives_testrs_testrs__array_myinterface_length, BD_,
    nativekt_natives_testrs_testrs__array_myinterface_push, BE_,
    nativekt_natives_testrs_testrs__array_myinterface_get, BF_,
    nativekt_natives_testrs_testrs__array_myinterface_free, BG_
);
impl_object_array!(String,
	nativekt_natives_testrs_testrs__array_string_new, BH_,
    nativekt_natives_testrs_testrs__array_string_length, BI_,
    nativekt_natives_testrs_testrs__array_string_push, BJ_,
    nativekt_natives_testrs_testrs__array_string_get, BK_,
    nativekt_natives_testrs_testrs__array_string_free, BL_
);

// ╔════════════════════════════╗
// ║     Critical functions     ║
// ╚════════════════════════════╝

fn critical_string(data: *mut u8, size: i32) -> ManuallyDrop<String> {
    unsafe { ManuallyDrop::new(String::from_raw_parts(data, size as usize, size as usize)) }
}

fn critical_string_opt(data: *mut u8, length: i32, size: i32) -> ManuallyDrop<Option<String>> {
    ManuallyDrop::new(if length != -1 {
        Some(unsafe { String::from_raw_parts(data, size as usize, size as usize) })
    } else { None })
}

fn critical_array<T>(data: *mut T, size: i32) -> ManuallyDrop<Vec<T>> {
    unsafe { ManuallyDrop::new(Vec::from_raw_parts(data, size as usize, size as usize)) }
}

fn critical_array_opt<T>(data: *mut T, size: i32) -> ManuallyDrop<Option<Vec<T>>> {
    ManuallyDrop::new(if size != -1 {
        Some(unsafe { Vec::from_raw_parts(data, size as usize, size as usize) })
    } else { None })
}

// ╔═════════════════╗
// ║     Structs     ║
// ╚═════════════════╝

#[derive(Clone)]
pub struct ParentDictionary {
	pub a: i32, 
	pub b: i32
}
export_fn! {
	fn nativekt_natives_testrs_testrs__parentdictionary_new(a: i32, b: i32) -> *mut ParentDictionary as BM_ {
		into_raw(ParentDictionary { a, b })
	}
	fn nativekt_natives_testrs_testrs__parentdictionary_free(of: *mut ParentDictionary) -> () as BN_ { from_raw(of); }
	fn nativekt_natives_testrs_testrs__parentdictionary__a(of: *mut ParentDictionary) -> i32 as BO_ { unsafe { (*of).a } }
	fn nativekt_natives_testrs_testrs__parentdictionary__b(of: *mut ParentDictionary) -> i32 as BP_ { unsafe { (*of).b } }
}

#[derive(Clone)]
pub struct MyDictionary {
	pub a: i32, 
	pub b: i32, 
	pub c: i32, 
	pub d: i32
}
export_fn! {
	fn nativekt_natives_testrs_testrs__mydictionary_new(a: i32, b: i32, c: i32, d: i32) -> *mut MyDictionary as BQ_ {
		into_raw(MyDictionary { a, b, c, d })
	}
	fn nativekt_natives_testrs_testrs__mydictionary_free(of: *mut MyDictionary) -> () as BR_ { from_raw(of); }
	fn nativekt_natives_testrs_testrs__mydictionary__a(of: *mut MyDictionary) -> i32 as BS_ { unsafe { (*of).a } }
	fn nativekt_natives_testrs_testrs__mydictionary__b(of: *mut MyDictionary) -> i32 as BT_ { unsafe { (*of).b } }
	fn nativekt_natives_testrs_testrs__mydictionary__c(of: *mut MyDictionary) -> i32 as BU_ { unsafe { (*of).c } }
	fn nativekt_natives_testrs_testrs__mydictionary__d(of: *mut MyDictionary) -> i32 as BV_ { unsafe { (*of).d } }
}

#[derive(Clone)]
pub struct TypeDictionary {
	pub a1: u16, 
	pub a2: bool, 
	pub a3: i8, 
	pub a4: u8, 
	pub a5: i16, 
	pub a6: u16, 
	pub a7: i32, 
	pub a8: u32, 
	pub a9: i64, 
	pub a10: u64, 
	pub a11: f32, 
	pub a12: f64, 
	pub a13: String, 
	pub a14: MyEnum, 
	pub a15: MyDictionary, 
	pub a16: Arc<crate::MyInterface>, 
	pub a17: Arc<VoidCallback>, 
	pub a18: Vec<u16>, 
	pub a19: Vec<bool>, 
	pub a20: Vec<i8>, 
	pub a21: Vec<u8>, 
	pub a22: Vec<i16>, 
	pub a23: Vec<u16>, 
	pub a24: Vec<i32>, 
	pub a25: Vec<u32>, 
	pub a26: Vec<i64>, 
	pub a27: Vec<u64>, 
	pub a28: Vec<f32>, 
	pub a29: Vec<f64>, 
	pub a30: Vec<String>, 
	pub a31: Vec<MyEnum>, 
	pub a32: Vec<MyDictionary>, 
	pub a33: Vec<Arc<crate::MyInterface>>
}
export_fn! {
	fn nativekt_natives_testrs_testrs__typedictionary_new(a1: u16, a2: bool, a3: i8, a4: u8, a5: i16, a6: u16, a7: i32, a8: u32, a9: i64, a10: u64, a11: f32, a12: f64, a13: *mut String, a14: MyEnum, a15: *mut MyDictionary, a16: *mut Arc<crate::MyInterface>, a17: *mut Arc<VoidCallback>, a18: *mut Vec<u16>, a19: *mut Vec<bool>, a20: *mut Vec<i8>, a21: *mut Vec<u8>, a22: *mut Vec<i16>, a23: *mut Vec<u16>, a24: *mut Vec<i32>, a25: *mut Vec<u32>, a26: *mut Vec<i64>, a27: *mut Vec<u64>, a28: *mut Vec<f32>, a29: *mut Vec<f64>, a30: *mut Vec<String>, a31: *mut Vec<MyEnum>, a32: *mut Vec<MyDictionary>, a33: *mut Vec<Arc<crate::MyInterface>>) -> *mut TypeDictionary as BW_ {
		into_raw(TypeDictionary { a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13: from_raw(a13), a14, a15: from_raw(a15), a16: from_raw(a16), a17: from_raw(a17), a18: from_raw(a18), a19: from_raw(a19), a20: from_raw(a20), a21: from_raw(a21), a22: from_raw(a22), a23: from_raw(a23), a24: from_raw(a24), a25: from_raw(a25), a26: from_raw(a26), a27: from_raw(a27), a28: from_raw(a28), a29: from_raw(a29), a30: from_raw(a30), a31: from_raw(a31), a32: from_raw(a32), a33: from_raw(a33) })
	}
	fn nativekt_natives_testrs_testrs__typedictionary_free(of: *mut TypeDictionary) -> () as BX_ { from_raw(of); }
	fn nativekt_natives_testrs_testrs__typedictionary__a1(of: *mut TypeDictionary) -> u16 as BY_ { unsafe { (*of).a1 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a2(of: *mut TypeDictionary) -> bool as BZ_ { unsafe { (*of).a2 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a3(of: *mut TypeDictionary) -> i8 as Ba_ { unsafe { (*of).a3 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a4(of: *mut TypeDictionary) -> u8 as Bb_ { unsafe { (*of).a4 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a5(of: *mut TypeDictionary) -> i16 as Bc_ { unsafe { (*of).a5 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a6(of: *mut TypeDictionary) -> u16 as Bd_ { unsafe { (*of).a6 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a7(of: *mut TypeDictionary) -> i32 as Be_ { unsafe { (*of).a7 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a8(of: *mut TypeDictionary) -> u32 as Bf_ { unsafe { (*of).a8 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a9(of: *mut TypeDictionary) -> i64 as Bg_ { unsafe { (*of).a9 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a10(of: *mut TypeDictionary) -> u64 as Bh_ { unsafe { (*of).a10 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a11(of: *mut TypeDictionary) -> f32 as Bi_ { unsafe { (*of).a11 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a12(of: *mut TypeDictionary) -> f64 as Bj_ { unsafe { (*of).a12 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a13(of: *mut TypeDictionary) -> *const String as Bk_ { unsafe { &(*of).a13 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a14(of: *mut TypeDictionary) -> MyEnum as Bl_ { unsafe { (*of).a14.clone() } }
	fn nativekt_natives_testrs_testrs__typedictionary__a15(of: *mut TypeDictionary) -> *const MyDictionary as Bm_ { unsafe { &(*of).a15 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a16(of: *mut TypeDictionary) -> *const Arc<crate::MyInterface> as Bn_ { unsafe { &(*of).a16 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a17(of: *mut TypeDictionary) -> *const Arc<VoidCallback> as Bo_ { unsafe { &(*of).a17 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a18(of: *mut TypeDictionary) -> *const Vec<u16> as Bp_ { unsafe { &(*of).a18 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a19(of: *mut TypeDictionary) -> *const Vec<bool> as Bq_ { unsafe { &(*of).a19 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a20(of: *mut TypeDictionary) -> *const Vec<i8> as Br_ { unsafe { &(*of).a20 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a21(of: *mut TypeDictionary) -> *const Vec<u8> as Bs_ { unsafe { &(*of).a21 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a22(of: *mut TypeDictionary) -> *const Vec<i16> as Bt_ { unsafe { &(*of).a22 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a23(of: *mut TypeDictionary) -> *const Vec<u16> as Bu_ { unsafe { &(*of).a23 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a24(of: *mut TypeDictionary) -> *const Vec<i32> as Bv_ { unsafe { &(*of).a24 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a25(of: *mut TypeDictionary) -> *const Vec<u32> as Bw_ { unsafe { &(*of).a25 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a26(of: *mut TypeDictionary) -> *const Vec<i64> as Bx_ { unsafe { &(*of).a26 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a27(of: *mut TypeDictionary) -> *const Vec<u64> as By_ { unsafe { &(*of).a27 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a28(of: *mut TypeDictionary) -> *const Vec<f32> as Bz_ { unsafe { &(*of).a28 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a29(of: *mut TypeDictionary) -> *const Vec<f64> as CA_ { unsafe { &(*of).a29 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a30(of: *mut TypeDictionary) -> *const Vec<String> as CB_ { unsafe { &(*of).a30 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a31(of: *mut TypeDictionary) -> *const Vec<MyEnum> as CC_ { unsafe { &(*of).a31 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a32(of: *mut TypeDictionary) -> *const Vec<MyDictionary> as CD_ { unsafe { &(*of).a32 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a33(of: *mut TypeDictionary) -> *const Vec<Arc<crate::MyInterface>> as CE_ { unsafe { &(*of).a33 } }
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
    CallbackPassBoolean,
	external_fn_type!(bool; isize, bool),
	pub fn invoke(&self, arg: bool) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassboolean_new, CF_,
	nativekt_natives_testrs_testrs__callbackpassboolean_id, CG_,
	nativekt_natives_testrs_testrs__callbackpassboolean_free, CH_
);
impl_callback!(
    CallbackPassBooleanArray,
	external_fn_type!(bool; isize, *mut Vec<bool>),
	pub fn invoke(&self, arg: Vec<bool>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassbooleanarray_new, CI_,
	nativekt_natives_testrs_testrs__callbackpassbooleanarray_id, CJ_,
	nativekt_natives_testrs_testrs__callbackpassbooleanarray_free, CK_
);
impl_callback!(
    CallbackPassByte,
	external_fn_type!(bool; isize, i8),
	pub fn invoke(&self, arg: i8) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassbyte_new, CL_,
	nativekt_natives_testrs_testrs__callbackpassbyte_id, CM_,
	nativekt_natives_testrs_testrs__callbackpassbyte_free, CN_
);
impl_callback!(
    CallbackPassByteArray,
	external_fn_type!(bool; isize, *mut Vec<i8>),
	pub fn invoke(&self, arg: Vec<i8>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassbytearray_new, CO_,
	nativekt_natives_testrs_testrs__callbackpassbytearray_id, CP_,
	nativekt_natives_testrs_testrs__callbackpassbytearray_free, CQ_
);
impl_callback!(
    CallbackPassCallback,
	external_fn_type!(bool; isize, *mut Arc<VoidCallback>),
	pub fn invoke(&self, arg: Arc<VoidCallback>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpasscallback_new, CR_,
	nativekt_natives_testrs_testrs__callbackpasscallback_id, CS_,
	nativekt_natives_testrs_testrs__callbackpasscallback_free, CT_
);
impl_callback!(
    CallbackPassCallbackN,
	external_fn_type!(bool; isize, *mut Arc<VoidCallback>),
	pub fn invoke(&self, arg: Option<Arc<VoidCallback>>) -> bool { external_fn_call!(bool; self.invoke; self.id, obj_opt(arg, into_raw)) },
	nativekt_natives_testrs_testrs__callbackpasscallbackn_new, CU_,
	nativekt_natives_testrs_testrs__callbackpasscallbackn_id, CV_,
	nativekt_natives_testrs_testrs__callbackpasscallbackn_free, CW_
);
impl_callback!(
    CallbackPassChar,
	external_fn_type!(bool; isize, u16),
	pub fn invoke(&self, arg: u16) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpasschar_new, CX_,
	nativekt_natives_testrs_testrs__callbackpasschar_id, CY_,
	nativekt_natives_testrs_testrs__callbackpasschar_free, CZ_
);
impl_callback!(
    CallbackPassCharArray,
	external_fn_type!(bool; isize, *mut Vec<u16>),
	pub fn invoke(&self, arg: Vec<u16>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpasschararray_new, Ca_,
	nativekt_natives_testrs_testrs__callbackpasschararray_id, Cb_,
	nativekt_natives_testrs_testrs__callbackpasschararray_free, Cc_
);
impl_callback!(
    CallbackPassCharArrayN,
	external_fn_type!(bool; isize, *mut Vec<u16>),
	pub fn invoke(&self, arg: Option<Vec<u16>>) -> bool { external_fn_call!(bool; self.invoke; self.id, obj_opt(arg, into_raw)) },
	nativekt_natives_testrs_testrs__callbackpasschararrayn_new, Cd_,
	nativekt_natives_testrs_testrs__callbackpasschararrayn_id, Ce_,
	nativekt_natives_testrs_testrs__callbackpasschararrayn_free, Cf_
);
impl_callback!(
    CallbackPassDictionary,
	external_fn_type!(bool; isize, *mut MyDictionary),
	pub fn invoke(&self, arg: MyDictionary) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassdictionary_new, Cg_,
	nativekt_natives_testrs_testrs__callbackpassdictionary_id, Ch_,
	nativekt_natives_testrs_testrs__callbackpassdictionary_free, Ci_
);
impl_callback!(
    CallbackPassDictionaryArray,
	external_fn_type!(bool; isize, *mut Vec<MyDictionary>),
	pub fn invoke(&self, arg: Vec<MyDictionary>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassdictionaryarray_new, Cj_,
	nativekt_natives_testrs_testrs__callbackpassdictionaryarray_id, Ck_,
	nativekt_natives_testrs_testrs__callbackpassdictionaryarray_free, Cl_
);
impl_callback!(
    CallbackPassDictionaryArrayN,
	external_fn_type!(bool; isize, *mut Vec<Option<MyDictionary>>),
	pub fn invoke(&self, arg: Vec<Option<MyDictionary>>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassdictionaryarrayn_new, Cm_,
	nativekt_natives_testrs_testrs__callbackpassdictionaryarrayn_id, Cn_,
	nativekt_natives_testrs_testrs__callbackpassdictionaryarrayn_free, Co_
);
impl_callback!(
    CallbackPassDictionaryN,
	external_fn_type!(bool; isize, *mut MyDictionary),
	pub fn invoke(&self, arg: Option<MyDictionary>) -> bool { external_fn_call!(bool; self.invoke; self.id, obj_opt(arg, into_raw)) },
	nativekt_natives_testrs_testrs__callbackpassdictionaryn_new, Cp_,
	nativekt_natives_testrs_testrs__callbackpassdictionaryn_id, Cq_,
	nativekt_natives_testrs_testrs__callbackpassdictionaryn_free, Cr_
);
impl_callback!(
    CallbackPassDouble,
	external_fn_type!(bool; isize, f64),
	pub fn invoke(&self, arg: f64) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassdouble_new, Cs_,
	nativekt_natives_testrs_testrs__callbackpassdouble_id, Ct_,
	nativekt_natives_testrs_testrs__callbackpassdouble_free, Cu_
);
impl_callback!(
    CallbackPassDoubleArray,
	external_fn_type!(bool; isize, *mut Vec<f64>),
	pub fn invoke(&self, arg: Vec<f64>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassdoublearray_new, Cv_,
	nativekt_natives_testrs_testrs__callbackpassdoublearray_id, Cw_,
	nativekt_natives_testrs_testrs__callbackpassdoublearray_free, Cx_
);
impl_callback!(
    CallbackPassEnum,
	external_fn_type!(bool; isize, MyEnum),
	pub fn invoke(&self, arg: MyEnum) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassenum_new, Cy_,
	nativekt_natives_testrs_testrs__callbackpassenum_id, Cz_,
	nativekt_natives_testrs_testrs__callbackpassenum_free, DA_
);
impl_callback!(
    CallbackPassEnumArray,
	external_fn_type!(bool; isize, *mut Vec<MyEnum>),
	pub fn invoke(&self, arg: Vec<MyEnum>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassenumarray_new, DB_,
	nativekt_natives_testrs_testrs__callbackpassenumarray_id, DC_,
	nativekt_natives_testrs_testrs__callbackpassenumarray_free, DD_
);
impl_callback!(
    CallbackPassFloat,
	external_fn_type!(bool; isize, f32),
	pub fn invoke(&self, arg: f32) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassfloat_new, DE_,
	nativekt_natives_testrs_testrs__callbackpassfloat_id, DF_,
	nativekt_natives_testrs_testrs__callbackpassfloat_free, DG_
);
impl_callback!(
    CallbackPassFloatArray,
	external_fn_type!(bool; isize, *mut Vec<f32>),
	pub fn invoke(&self, arg: Vec<f32>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassfloatarray_new, DH_,
	nativekt_natives_testrs_testrs__callbackpassfloatarray_id, DI_,
	nativekt_natives_testrs_testrs__callbackpassfloatarray_free, DJ_
);
impl_callback!(
    CallbackPassInt,
	external_fn_type!(bool; isize, i32),
	pub fn invoke(&self, arg: i32) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassint_new, DK_,
	nativekt_natives_testrs_testrs__callbackpassint_id, DL_,
	nativekt_natives_testrs_testrs__callbackpassint_free, DM_
);
impl_callback!(
    CallbackPassIntArray,
	external_fn_type!(bool; isize, *mut Vec<i32>),
	pub fn invoke(&self, arg: Vec<i32>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassintarray_new, DN_,
	nativekt_natives_testrs_testrs__callbackpassintarray_id, DO_,
	nativekt_natives_testrs_testrs__callbackpassintarray_free, DP_
);
impl_callback!(
    CallbackPassInterface,
	external_fn_type!(bool; isize, *mut Arc<crate::MyInterface>),
	pub fn invoke(&self, arg: Arc<crate::MyInterface>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassinterface_new, DQ_,
	nativekt_natives_testrs_testrs__callbackpassinterface_id, DR_,
	nativekt_natives_testrs_testrs__callbackpassinterface_free, DS_
);
impl_callback!(
    CallbackPassInterfaceArray,
	external_fn_type!(bool; isize, *mut Vec<Arc<crate::MyInterface>>),
	pub fn invoke(&self, arg: Vec<Arc<crate::MyInterface>>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassinterfacearray_new, DT_,
	nativekt_natives_testrs_testrs__callbackpassinterfacearray_id, DU_,
	nativekt_natives_testrs_testrs__callbackpassinterfacearray_free, DV_
);
impl_callback!(
    CallbackPassInterfaceArrayN,
	external_fn_type!(bool; isize, *mut Vec<Option<Arc<crate::MyInterface>>>),
	pub fn invoke(&self, arg: Vec<Option<Arc<crate::MyInterface>>>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassinterfacearrayn_new, DW_,
	nativekt_natives_testrs_testrs__callbackpassinterfacearrayn_id, DX_,
	nativekt_natives_testrs_testrs__callbackpassinterfacearrayn_free, DY_
);
impl_callback!(
    CallbackPassInterfaceN,
	external_fn_type!(bool; isize, *mut Arc<crate::MyInterface>),
	pub fn invoke(&self, arg: Option<Arc<crate::MyInterface>>) -> bool { external_fn_call!(bool; self.invoke; self.id, obj_opt(arg, into_raw)) },
	nativekt_natives_testrs_testrs__callbackpassinterfacen_new, DZ_,
	nativekt_natives_testrs_testrs__callbackpassinterfacen_id, Da_,
	nativekt_natives_testrs_testrs__callbackpassinterfacen_free, Db_
);
impl_callback!(
    CallbackPassLong,
	external_fn_type!(bool; isize, i64),
	pub fn invoke(&self, arg: i64) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpasslong_new, Dc_,
	nativekt_natives_testrs_testrs__callbackpasslong_id, Dd_,
	nativekt_natives_testrs_testrs__callbackpasslong_free, De_
);
impl_callback!(
    CallbackPassLongArray,
	external_fn_type!(bool; isize, *mut Vec<i64>),
	pub fn invoke(&self, arg: Vec<i64>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpasslongarray_new, Df_,
	nativekt_natives_testrs_testrs__callbackpasslongarray_id, Dg_,
	nativekt_natives_testrs_testrs__callbackpasslongarray_free, Dh_
);
impl_callback!(
    CallbackPassShort,
	external_fn_type!(bool; isize, i16),
	pub fn invoke(&self, arg: i16) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassshort_new, Di_,
	nativekt_natives_testrs_testrs__callbackpassshort_id, Dj_,
	nativekt_natives_testrs_testrs__callbackpassshort_free, Dk_
);
impl_callback!(
    CallbackPassShortArray,
	external_fn_type!(bool; isize, *mut Vec<i16>),
	pub fn invoke(&self, arg: Vec<i16>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassshortarray_new, Dl_,
	nativekt_natives_testrs_testrs__callbackpassshortarray_id, Dm_,
	nativekt_natives_testrs_testrs__callbackpassshortarray_free, Dn_
);
impl_callback!(
    CallbackPassString,
	external_fn_type!(bool; isize, *mut String),
	pub fn invoke(&self, arg: String) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassstring_new, Do_,
	nativekt_natives_testrs_testrs__callbackpassstring_id, Dp_,
	nativekt_natives_testrs_testrs__callbackpassstring_free, Dq_
);
impl_callback!(
    CallbackPassStringArray,
	external_fn_type!(bool; isize, *mut Vec<String>),
	pub fn invoke(&self, arg: Vec<String>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassstringarray_new, Dr_,
	nativekt_natives_testrs_testrs__callbackpassstringarray_id, Ds_,
	nativekt_natives_testrs_testrs__callbackpassstringarray_free, Dt_
);
impl_callback!(
    CallbackPassStringArrayN,
	external_fn_type!(bool; isize, *mut Vec<Option<String>>),
	pub fn invoke(&self, arg: Vec<Option<String>>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassstringarrayn_new, Du_,
	nativekt_natives_testrs_testrs__callbackpassstringarrayn_id, Dv_,
	nativekt_natives_testrs_testrs__callbackpassstringarrayn_free, Dw_
);
impl_callback!(
    CallbackPassStringN,
	external_fn_type!(bool; isize, *mut String),
	pub fn invoke(&self, arg: Option<String>) -> bool { external_fn_call!(bool; self.invoke; self.id, obj_opt(arg, into_raw)) },
	nativekt_natives_testrs_testrs__callbackpassstringn_new, Dx_,
	nativekt_natives_testrs_testrs__callbackpassstringn_id, Dy_,
	nativekt_natives_testrs_testrs__callbackpassstringn_free, Dz_
);
impl_callback!(
    CallbackPassUByte,
	external_fn_type!(bool; isize, u8),
	pub fn invoke(&self, arg: u8) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassubyte_new, EA_,
	nativekt_natives_testrs_testrs__callbackpassubyte_id, EB_,
	nativekt_natives_testrs_testrs__callbackpassubyte_free, EC_
);
impl_callback!(
    CallbackPassUByteArray,
	external_fn_type!(bool; isize, *mut Vec<u8>),
	pub fn invoke(&self, arg: Vec<u8>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassubytearray_new, ED_,
	nativekt_natives_testrs_testrs__callbackpassubytearray_id, EE_,
	nativekt_natives_testrs_testrs__callbackpassubytearray_free, EF_
);
impl_callback!(
    CallbackPassUInt,
	external_fn_type!(bool; isize, u32),
	pub fn invoke(&self, arg: u32) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassuint_new, EG_,
	nativekt_natives_testrs_testrs__callbackpassuint_id, EH_,
	nativekt_natives_testrs_testrs__callbackpassuint_free, EI_
);
impl_callback!(
    CallbackPassUIntArray,
	external_fn_type!(bool; isize, *mut Vec<u32>),
	pub fn invoke(&self, arg: Vec<u32>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassuintarray_new, EJ_,
	nativekt_natives_testrs_testrs__callbackpassuintarray_id, EK_,
	nativekt_natives_testrs_testrs__callbackpassuintarray_free, EL_
);
impl_callback!(
    CallbackPassULong,
	external_fn_type!(bool; isize, u64),
	pub fn invoke(&self, arg: u64) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassulong_new, EM_,
	nativekt_natives_testrs_testrs__callbackpassulong_id, EN_,
	nativekt_natives_testrs_testrs__callbackpassulong_free, EO_
);
impl_callback!(
    CallbackPassULongArray,
	external_fn_type!(bool; isize, *mut Vec<u64>),
	pub fn invoke(&self, arg: Vec<u64>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassulongarray_new, EP_,
	nativekt_natives_testrs_testrs__callbackpassulongarray_id, EQ_,
	nativekt_natives_testrs_testrs__callbackpassulongarray_free, ER_
);
impl_callback!(
    CallbackPassUShort,
	external_fn_type!(bool; isize, u16),
	pub fn invoke(&self, arg: u16) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassushort_new, ES_,
	nativekt_natives_testrs_testrs__callbackpassushort_id, ET_,
	nativekt_natives_testrs_testrs__callbackpassushort_free, EU_
);
impl_callback!(
    CallbackPassUShortArray,
	external_fn_type!(bool; isize, *mut Vec<u16>),
	pub fn invoke(&self, arg: Vec<u16>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassushortarray_new, EV_,
	nativekt_natives_testrs_testrs__callbackpassushortarray_id, EW_,
	nativekt_natives_testrs_testrs__callbackpassushortarray_free, EX_
);
impl_callback!(
    CallbackReturnBoolean,
	external_fn_type!(bool; isize),
	pub fn invoke(&self) -> bool { external_fn_call!(bool; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnboolean_new, EY_,
	nativekt_natives_testrs_testrs__callbackreturnboolean_id, EZ_,
	nativekt_natives_testrs_testrs__callbackreturnboolean_free, Ea_
);
impl_callback!(
    CallbackReturnBooleanArray,
	external_fn_type!(*mut Vec<bool>; isize),
	pub fn invoke(&self) -> Vec<bool> { from_raw(external_fn_call!(*mut Vec<bool>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnbooleanarray_new, Eb_,
	nativekt_natives_testrs_testrs__callbackreturnbooleanarray_id, Ec_,
	nativekt_natives_testrs_testrs__callbackreturnbooleanarray_free, Ed_
);
impl_callback!(
    CallbackReturnByte,
	external_fn_type!(i8; isize),
	pub fn invoke(&self) -> i8 { external_fn_call!(i8; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnbyte_new, Ee_,
	nativekt_natives_testrs_testrs__callbackreturnbyte_id, Ef_,
	nativekt_natives_testrs_testrs__callbackreturnbyte_free, Eg_
);
impl_callback!(
    CallbackReturnByteArray,
	external_fn_type!(*mut Vec<i8>; isize),
	pub fn invoke(&self) -> Vec<i8> { from_raw(external_fn_call!(*mut Vec<i8>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnbytearray_new, Eh_,
	nativekt_natives_testrs_testrs__callbackreturnbytearray_id, Ei_,
	nativekt_natives_testrs_testrs__callbackreturnbytearray_free, Ej_
);
impl_callback!(
    CallbackReturnCallback,
	external_fn_type!(*mut Arc<VoidCallback>; isize),
	pub fn invoke(&self) -> Arc<VoidCallback> { from_raw(external_fn_call!(*mut Arc<VoidCallback>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturncallback_new, Ek_,
	nativekt_natives_testrs_testrs__callbackreturncallback_id, El_,
	nativekt_natives_testrs_testrs__callbackreturncallback_free, Em_
);
impl_callback!(
    CallbackReturnCallbackN,
	external_fn_type!(*mut Arc<VoidCallback>; isize),
	pub fn invoke(&self) -> Option<Arc<VoidCallback>> { ptr_opt(external_fn_call!(*mut Arc<VoidCallback>; self.invoke; self.id), from_raw) },
	nativekt_natives_testrs_testrs__callbackreturncallbackn_new, En_,
	nativekt_natives_testrs_testrs__callbackreturncallbackn_id, Eo_,
	nativekt_natives_testrs_testrs__callbackreturncallbackn_free, Ep_
);
impl_callback!(
    CallbackReturnChar,
	external_fn_type!(u16; isize),
	pub fn invoke(&self) -> u16 { external_fn_call!(u16; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnchar_new, Eq_,
	nativekt_natives_testrs_testrs__callbackreturnchar_id, Er_,
	nativekt_natives_testrs_testrs__callbackreturnchar_free, Es_
);
impl_callback!(
    CallbackReturnCharArray,
	external_fn_type!(*mut Vec<u16>; isize),
	pub fn invoke(&self) -> Vec<u16> { from_raw(external_fn_call!(*mut Vec<u16>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnchararray_new, Et_,
	nativekt_natives_testrs_testrs__callbackreturnchararray_id, Eu_,
	nativekt_natives_testrs_testrs__callbackreturnchararray_free, Ev_
);
impl_callback!(
    CallbackReturnCharArrayN,
	external_fn_type!(*mut Vec<u16>; isize),
	pub fn invoke(&self) -> Option<Vec<u16>> { ptr_opt(external_fn_call!(*mut Vec<u16>; self.invoke; self.id), from_raw) },
	nativekt_natives_testrs_testrs__callbackreturnchararrayn_new, Ew_,
	nativekt_natives_testrs_testrs__callbackreturnchararrayn_id, Ex_,
	nativekt_natives_testrs_testrs__callbackreturnchararrayn_free, Ey_
);
impl_callback!(
    CallbackReturnDictionary,
	external_fn_type!(*mut MyDictionary; isize),
	pub fn invoke(&self) -> MyDictionary { from_raw(external_fn_call!(*mut MyDictionary; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturndictionary_new, Ez_,
	nativekt_natives_testrs_testrs__callbackreturndictionary_id, FA_,
	nativekt_natives_testrs_testrs__callbackreturndictionary_free, FB_
);
impl_callback!(
    CallbackReturnDictionaryArray,
	external_fn_type!(*mut Vec<MyDictionary>; isize),
	pub fn invoke(&self) -> Vec<MyDictionary> { from_raw(external_fn_call!(*mut Vec<MyDictionary>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturndictionaryarray_new, FC_,
	nativekt_natives_testrs_testrs__callbackreturndictionaryarray_id, FD_,
	nativekt_natives_testrs_testrs__callbackreturndictionaryarray_free, FE_
);
impl_callback!(
    CallbackReturnDictionaryArrayN,
	external_fn_type!(*mut Vec<Option<MyDictionary>>; isize),
	pub fn invoke(&self) -> Vec<Option<MyDictionary>> { from_raw(external_fn_call!(*mut Vec<Option<MyDictionary>>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturndictionaryarrayn_new, FF_,
	nativekt_natives_testrs_testrs__callbackreturndictionaryarrayn_id, FG_,
	nativekt_natives_testrs_testrs__callbackreturndictionaryarrayn_free, FH_
);
impl_callback!(
    CallbackReturnDictionaryN,
	external_fn_type!(*mut MyDictionary; isize),
	pub fn invoke(&self) -> Option<MyDictionary> { ptr_opt(external_fn_call!(*mut MyDictionary; self.invoke; self.id), from_raw) },
	nativekt_natives_testrs_testrs__callbackreturndictionaryn_new, FI_,
	nativekt_natives_testrs_testrs__callbackreturndictionaryn_id, FJ_,
	nativekt_natives_testrs_testrs__callbackreturndictionaryn_free, FK_
);
impl_callback!(
    CallbackReturnDouble,
	external_fn_type!(f64; isize),
	pub fn invoke(&self) -> f64 { external_fn_call!(f64; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturndouble_new, FL_,
	nativekt_natives_testrs_testrs__callbackreturndouble_id, FM_,
	nativekt_natives_testrs_testrs__callbackreturndouble_free, FN_
);
impl_callback!(
    CallbackReturnDoubleArray,
	external_fn_type!(*mut Vec<f64>; isize),
	pub fn invoke(&self) -> Vec<f64> { from_raw(external_fn_call!(*mut Vec<f64>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturndoublearray_new, FO_,
	nativekt_natives_testrs_testrs__callbackreturndoublearray_id, FP_,
	nativekt_natives_testrs_testrs__callbackreturndoublearray_free, FQ_
);
impl_callback!(
    CallbackReturnEnum,
	external_fn_type!(MyEnum; isize),
	pub fn invoke(&self) -> MyEnum { external_fn_call!(enum MyEnum; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnenum_new, FR_,
	nativekt_natives_testrs_testrs__callbackreturnenum_id, FS_,
	nativekt_natives_testrs_testrs__callbackreturnenum_free, FT_
);
impl_callback!(
    CallbackReturnEnumArray,
	external_fn_type!(*mut Vec<MyEnum>; isize),
	pub fn invoke(&self) -> Vec<MyEnum> { from_raw(external_fn_call!(*mut Vec<MyEnum>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnenumarray_new, FU_,
	nativekt_natives_testrs_testrs__callbackreturnenumarray_id, FV_,
	nativekt_natives_testrs_testrs__callbackreturnenumarray_free, FW_
);
impl_callback!(
    CallbackReturnFloat,
	external_fn_type!(f32; isize),
	pub fn invoke(&self) -> f32 { external_fn_call!(f32; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnfloat_new, FX_,
	nativekt_natives_testrs_testrs__callbackreturnfloat_id, FY_,
	nativekt_natives_testrs_testrs__callbackreturnfloat_free, FZ_
);
impl_callback!(
    CallbackReturnFloatArray,
	external_fn_type!(*mut Vec<f32>; isize),
	pub fn invoke(&self) -> Vec<f32> { from_raw(external_fn_call!(*mut Vec<f32>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnfloatarray_new, Fa_,
	nativekt_natives_testrs_testrs__callbackreturnfloatarray_id, Fb_,
	nativekt_natives_testrs_testrs__callbackreturnfloatarray_free, Fc_
);
impl_callback!(
    CallbackReturnInt,
	external_fn_type!(i32; isize),
	pub fn invoke(&self) -> i32 { external_fn_call!(i32; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnint_new, Fd_,
	nativekt_natives_testrs_testrs__callbackreturnint_id, Fe_,
	nativekt_natives_testrs_testrs__callbackreturnint_free, Ff_
);
impl_callback!(
    CallbackReturnIntArray,
	external_fn_type!(*mut Vec<i32>; isize),
	pub fn invoke(&self) -> Vec<i32> { from_raw(external_fn_call!(*mut Vec<i32>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnintarray_new, Fg_,
	nativekt_natives_testrs_testrs__callbackreturnintarray_id, Fh_,
	nativekt_natives_testrs_testrs__callbackreturnintarray_free, Fi_
);
impl_callback!(
    CallbackReturnInterface,
	external_fn_type!(*mut Arc<crate::MyInterface>; isize),
	pub fn invoke(&self) -> Arc<crate::MyInterface> { from_raw(external_fn_call!(*mut Arc<crate::MyInterface>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturninterface_new, Fj_,
	nativekt_natives_testrs_testrs__callbackreturninterface_id, Fk_,
	nativekt_natives_testrs_testrs__callbackreturninterface_free, Fl_
);
impl_callback!(
    CallbackReturnInterfaceArray,
	external_fn_type!(*mut Vec<Arc<crate::MyInterface>>; isize),
	pub fn invoke(&self) -> Vec<Arc<crate::MyInterface>> { from_raw(external_fn_call!(*mut Vec<Arc<crate::MyInterface>>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturninterfacearray_new, Fm_,
	nativekt_natives_testrs_testrs__callbackreturninterfacearray_id, Fn_,
	nativekt_natives_testrs_testrs__callbackreturninterfacearray_free, Fo_
);
impl_callback!(
    CallbackReturnInterfaceArrayN,
	external_fn_type!(*mut Vec<Option<Arc<crate::MyInterface>>>; isize),
	pub fn invoke(&self) -> Vec<Option<Arc<crate::MyInterface>>> { from_raw(external_fn_call!(*mut Vec<Option<Arc<crate::MyInterface>>>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturninterfacearrayn_new, Fp_,
	nativekt_natives_testrs_testrs__callbackreturninterfacearrayn_id, Fq_,
	nativekt_natives_testrs_testrs__callbackreturninterfacearrayn_free, Fr_
);
impl_callback!(
    CallbackReturnInterfaceN,
	external_fn_type!(*mut Arc<crate::MyInterface>; isize),
	pub fn invoke(&self) -> Option<Arc<crate::MyInterface>> { ptr_opt(external_fn_call!(*mut Arc<crate::MyInterface>; self.invoke; self.id), from_raw) },
	nativekt_natives_testrs_testrs__callbackreturninterfacen_new, Fs_,
	nativekt_natives_testrs_testrs__callbackreturninterfacen_id, Ft_,
	nativekt_natives_testrs_testrs__callbackreturninterfacen_free, Fu_
);
impl_callback!(
    CallbackReturnLong,
	external_fn_type!(i64; isize),
	pub fn invoke(&self) -> i64 { external_fn_call!(i64; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnlong_new, Fv_,
	nativekt_natives_testrs_testrs__callbackreturnlong_id, Fw_,
	nativekt_natives_testrs_testrs__callbackreturnlong_free, Fx_
);
impl_callback!(
    CallbackReturnLongArray,
	external_fn_type!(*mut Vec<i64>; isize),
	pub fn invoke(&self) -> Vec<i64> { from_raw(external_fn_call!(*mut Vec<i64>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnlongarray_new, Fy_,
	nativekt_natives_testrs_testrs__callbackreturnlongarray_id, Fz_,
	nativekt_natives_testrs_testrs__callbackreturnlongarray_free, GA_
);
impl_callback!(
    CallbackReturnShort,
	external_fn_type!(i16; isize),
	pub fn invoke(&self) -> i16 { external_fn_call!(i16; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnshort_new, GB_,
	nativekt_natives_testrs_testrs__callbackreturnshort_id, GC_,
	nativekt_natives_testrs_testrs__callbackreturnshort_free, GD_
);
impl_callback!(
    CallbackReturnShortArray,
	external_fn_type!(*mut Vec<i16>; isize),
	pub fn invoke(&self) -> Vec<i16> { from_raw(external_fn_call!(*mut Vec<i16>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnshortarray_new, GE_,
	nativekt_natives_testrs_testrs__callbackreturnshortarray_id, GF_,
	nativekt_natives_testrs_testrs__callbackreturnshortarray_free, GG_
);
impl_callback!(
    CallbackReturnString,
	external_fn_type!(*mut String; isize),
	pub fn invoke(&self) -> String { from_raw(external_fn_call!(*mut String; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnstring_new, GH_,
	nativekt_natives_testrs_testrs__callbackreturnstring_id, GI_,
	nativekt_natives_testrs_testrs__callbackreturnstring_free, GJ_
);
impl_callback!(
    CallbackReturnStringArray,
	external_fn_type!(*mut Vec<String>; isize),
	pub fn invoke(&self) -> Vec<String> { from_raw(external_fn_call!(*mut Vec<String>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnstringarray_new, GK_,
	nativekt_natives_testrs_testrs__callbackreturnstringarray_id, GL_,
	nativekt_natives_testrs_testrs__callbackreturnstringarray_free, GM_
);
impl_callback!(
    CallbackReturnStringArrayN,
	external_fn_type!(*mut Vec<Option<String>>; isize),
	pub fn invoke(&self) -> Vec<Option<String>> { from_raw(external_fn_call!(*mut Vec<Option<String>>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnstringarrayn_new, GN_,
	nativekt_natives_testrs_testrs__callbackreturnstringarrayn_id, GO_,
	nativekt_natives_testrs_testrs__callbackreturnstringarrayn_free, GP_
);
impl_callback!(
    CallbackReturnStringN,
	external_fn_type!(*mut String; isize),
	pub fn invoke(&self) -> Option<String> { ptr_opt(external_fn_call!(*mut String; self.invoke; self.id), from_raw) },
	nativekt_natives_testrs_testrs__callbackreturnstringn_new, GQ_,
	nativekt_natives_testrs_testrs__callbackreturnstringn_id, GR_,
	nativekt_natives_testrs_testrs__callbackreturnstringn_free, GS_
);
impl_callback!(
    CallbackReturnUByte,
	external_fn_type!(u8; isize),
	pub fn invoke(&self) -> u8 { external_fn_call!(u8; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnubyte_new, GT_,
	nativekt_natives_testrs_testrs__callbackreturnubyte_id, GU_,
	nativekt_natives_testrs_testrs__callbackreturnubyte_free, GV_
);
impl_callback!(
    CallbackReturnUByteArray,
	external_fn_type!(*mut Vec<u8>; isize),
	pub fn invoke(&self) -> Vec<u8> { from_raw(external_fn_call!(*mut Vec<u8>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnubytearray_new, GW_,
	nativekt_natives_testrs_testrs__callbackreturnubytearray_id, GX_,
	nativekt_natives_testrs_testrs__callbackreturnubytearray_free, GY_
);
impl_callback!(
    CallbackReturnUInt,
	external_fn_type!(u32; isize),
	pub fn invoke(&self) -> u32 { external_fn_call!(u32; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnuint_new, GZ_,
	nativekt_natives_testrs_testrs__callbackreturnuint_id, Ga_,
	nativekt_natives_testrs_testrs__callbackreturnuint_free, Gb_
);
impl_callback!(
    CallbackReturnUIntArray,
	external_fn_type!(*mut Vec<u32>; isize),
	pub fn invoke(&self) -> Vec<u32> { from_raw(external_fn_call!(*mut Vec<u32>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnuintarray_new, Gc_,
	nativekt_natives_testrs_testrs__callbackreturnuintarray_id, Gd_,
	nativekt_natives_testrs_testrs__callbackreturnuintarray_free, Ge_
);
impl_callback!(
    CallbackReturnULong,
	external_fn_type!(u64; isize),
	pub fn invoke(&self) -> u64 { external_fn_call!(u64; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnulong_new, Gf_,
	nativekt_natives_testrs_testrs__callbackreturnulong_id, Gg_,
	nativekt_natives_testrs_testrs__callbackreturnulong_free, Gh_
);
impl_callback!(
    CallbackReturnULongArray,
	external_fn_type!(*mut Vec<u64>; isize),
	pub fn invoke(&self) -> Vec<u64> { from_raw(external_fn_call!(*mut Vec<u64>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnulongarray_new, Gi_,
	nativekt_natives_testrs_testrs__callbackreturnulongarray_id, Gj_,
	nativekt_natives_testrs_testrs__callbackreturnulongarray_free, Gk_
);
impl_callback!(
    CallbackReturnUShort,
	external_fn_type!(u16; isize),
	pub fn invoke(&self) -> u16 { external_fn_call!(u16; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnushort_new, Gl_,
	nativekt_natives_testrs_testrs__callbackreturnushort_id, Gm_,
	nativekt_natives_testrs_testrs__callbackreturnushort_free, Gn_
);
impl_callback!(
    CallbackReturnUShortArray,
	external_fn_type!(*mut Vec<u16>; isize),
	pub fn invoke(&self) -> Vec<u16> { from_raw(external_fn_call!(*mut Vec<u16>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnushortarray_new, Go_,
	nativekt_natives_testrs_testrs__callbackreturnushortarray_id, Gp_,
	nativekt_natives_testrs_testrs__callbackreturnushortarray_free, Gq_
);
impl_callback!(
    VoidCallback,
	external_fn_type!((); isize),
	pub fn invoke(&self) { external_fn_call!((); self.invoke; self.id) },
	nativekt_natives_testrs_testrs__voidcallback_new, Gr_,
	nativekt_natives_testrs_testrs__voidcallback_id, Gs_,
	nativekt_natives_testrs_testrs__voidcallback_free, Gt_
);

// ╔═══════════════╗
// ║     Enums     ║
// ╚═══════════════╝

#[cfg_attr(target_arch = "wasm32", wasm_bindgen)]
#[repr(C)]
#[derive(PartialEq, Eq, Clone)]
pub enum MyEnum {
	CASE1 = 0,
	CASE2 = 1
}

impl MyEnum {
    pub fn from_int(value: i32) -> Self {
        match value {
			0 => MyEnum::CASE1,
			1 => MyEnum::CASE2,
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

export_fn!{ fn nativekt_natives_testrs_testrs_pass_void() -> bool as Gu_ {
    crate::pass_void()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_char(arg: u16) -> bool as Gv_ {
    crate::pass_char(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_boolean(arg: bool) -> bool as Gw_ {
    crate::pass_boolean(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_byte(arg: i8) -> bool as Gx_ {
    crate::pass_byte(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ubyte(arg: u8) -> bool as Gy_ {
    crate::pass_ubyte(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_short(arg: i16) -> bool as Gz_ {
    crate::pass_short(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ushort(arg: u16) -> bool as HA_ {
    crate::pass_ushort(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_int(arg: i32) -> bool as HB_ {
    crate::pass_int(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_uint(arg: u32) -> bool as HC_ {
    crate::pass_uint(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_long(arg: i64) -> bool as HD_ {
    crate::pass_long(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ulong(arg: u64) -> bool as HE_ {
    crate::pass_ulong(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_float(arg: f32) -> bool as HF_ {
    crate::pass_float(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_double(arg: f64) -> bool as HG_ {
    crate::pass_double(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_string(arg: *mut String) -> bool as HH_ {
    crate::pass_string(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_string_n(arg: *mut String) -> bool as HI_ {
    crate::pass_string_n(&ptr_opt(arg, from_raw))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_enum(arg: MyEnum) -> bool as HJ_ {
    crate::pass_enum(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_dictionary(arg: *mut MyDictionary) -> bool as HK_ {
    crate::pass_dictionary(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_dictionary_n(arg: *mut MyDictionary) -> bool as HL_ {
    crate::pass_dictionary_n(&ptr_opt(arg, from_raw))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_interface(arg: *mut Arc<crate::MyInterface>) -> bool as HM_ {
    crate::pass_interface(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_interface_n(arg: *mut Arc<crate::MyInterface>) -> bool as HN_ {
    crate::pass_interface_n(&ptr_opt(arg, from_raw))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_void() -> () as HO_ {
    crate::return_void()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_char() -> u16 as HP_ {
    crate::return_char()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_boolean() -> bool as HQ_ {
    crate::return_boolean()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_byte() -> i8 as HR_ {
    crate::return_byte()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ubyte() -> u8 as HS_ {
    crate::return_ubyte()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_short() -> i16 as HT_ {
    crate::return_short()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ushort() -> u16 as HU_ {
    crate::return_ushort()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_int() -> i32 as HV_ {
    crate::return_int()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_uint() -> u32 as HW_ {
    crate::return_uint()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_long() -> i64 as HX_ {
    crate::return_long()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ulong() -> u64 as HY_ {
    crate::return_ulong()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_float() -> f32 as HZ_ {
    crate::return_float()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_double() -> f64 as Ha_ {
    crate::return_double()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_string() -> *mut String as Hb_ {
    into_raw(crate::return_string())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_string_n() -> *mut String as Hc_ {
    obj_opt(crate::return_string_n(), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_enum() -> MyEnum as Hd_ {
    crate::return_enum()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_dictionary() -> *mut MyDictionary as He_ {
    into_raw(crate::return_dictionary())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_dictionary_n() -> *mut MyDictionary as Hf_ {
    obj_opt(crate::return_dictionary_n(), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_char(arg: u16) -> u16 as Hg_ {
    crate::ping_char(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_boolean(arg: bool) -> bool as Hh_ {
    crate::ping_boolean(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_byte(arg: i8) -> i8 as Hi_ {
    crate::ping_byte(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_ubyte(arg: u8) -> u8 as Hj_ {
    crate::ping_ubyte(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_short(arg: i16) -> i16 as Hk_ {
    crate::ping_short(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_ushort(arg: u16) -> u16 as Hl_ {
    crate::ping_ushort(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_int(arg: i32) -> i32 as Hm_ {
    crate::ping_int(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_uint(arg: u32) -> u32 as Hn_ {
    crate::ping_uint(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_long(arg: i64) -> i64 as Ho_ {
    crate::ping_long(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_ulong(arg: u64) -> u64 as Hp_ {
    crate::ping_ulong(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_float(arg: f32) -> f32 as Hq_ {
    crate::ping_float(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_double(arg: f64) -> f64 as Hr_ {
    crate::ping_double(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_string(arg: *mut String) -> *mut String as Hs_ {
    into_raw(crate::ping_string(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_string_n(arg: *mut String) -> *mut String as Ht_ {
    obj_opt(crate::ping_string_n(&ptr_opt(arg, from_raw)), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_enum(arg: MyEnum) -> MyEnum as Hu_ {
    crate::ping_enum(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_dictionary(arg: *mut MyDictionary) -> *mut MyDictionary as Hv_ {
    into_raw(crate::ping_dictionary(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_dictionary_n(arg: *mut MyDictionary) -> *mut MyDictionary as Hw_ {
    obj_opt(crate::ping_dictionary_n(&ptr_opt(arg, from_raw)), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_interface(arg: *mut Arc<crate::MyInterface>) -> *mut Arc<crate::MyInterface> as Hx_ {
    into_raw(crate::ping_interface(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_interface_n(arg: *mut Arc<crate::MyInterface>) -> *mut Arc<crate::MyInterface> as Hy_ {
    obj_opt(crate::ping_interface_n(&ptr_opt(arg, from_raw)), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_void(arg: *mut Arc<VoidCallback>) -> () as Hz_ {
    crate::callback_void(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_void_n(arg: *mut Arc<VoidCallback>) -> bool as IA_ {
    crate::callback_void_n(&ptr_opt(arg, from_raw))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_char(arg: *mut Arc<CallbackPassChar>) -> bool as IB_ {
    crate::callback_arg_char(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_boolean(arg: *mut Arc<CallbackPassBoolean>) -> bool as IC_ {
    crate::callback_arg_boolean(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_byte(arg: *mut Arc<CallbackPassByte>) -> bool as ID_ {
    crate::callback_arg_byte(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_ubyte(arg: *mut Arc<CallbackPassUByte>) -> bool as IE_ {
    crate::callback_arg_ubyte(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_short(arg: *mut Arc<CallbackPassShort>) -> bool as IF_ {
    crate::callback_arg_short(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_ushort(arg: *mut Arc<CallbackPassUShort>) -> bool as IG_ {
    crate::callback_arg_ushort(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_int(arg: *mut Arc<CallbackPassInt>) -> bool as IH_ {
    crate::callback_arg_int(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_uint(arg: *mut Arc<CallbackPassUInt>) -> bool as II_ {
    crate::callback_arg_uint(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_long(arg: *mut Arc<CallbackPassLong>) -> bool as IJ_ {
    crate::callback_arg_long(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_ulong(arg: *mut Arc<CallbackPassULong>) -> bool as IK_ {
    crate::callback_arg_ulong(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_float(arg: *mut Arc<CallbackPassFloat>) -> bool as IL_ {
    crate::callback_arg_float(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_double(arg: *mut Arc<CallbackPassDouble>) -> bool as IM_ {
    crate::callback_arg_double(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_string(arg: *mut Arc<CallbackPassString>) -> bool as IN_ {
    crate::callback_arg_string(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_string_n(arg: *mut Arc<CallbackPassStringN>) -> bool as IO_ {
    crate::callback_arg_string_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_callback(pass: *mut Arc<VoidCallback>, arg: *mut Arc<CallbackPassCallback>) -> bool as IP_ {
    crate::callback_arg_callback(&from_raw(pass), &from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_callback_n(arg: *mut Arc<CallbackPassCallbackN>) -> bool as IQ_ {
    crate::callback_arg_callback_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_enum(arg: *mut Arc<CallbackPassEnum>) -> bool as IR_ {
    crate::callback_arg_enum(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_dictionary(arg: *mut Arc<CallbackPassDictionary>) -> bool as IS_ {
    crate::callback_arg_dictionary(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_dictionary_n(arg: *mut Arc<CallbackPassDictionaryN>) -> bool as IT_ {
    crate::callback_arg_dictionary_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_interface(pass: *mut Arc<crate::MyInterface>, arg: *mut Arc<CallbackPassInterface>) -> bool as IU_ {
    crate::callback_arg_interface(&from_raw(pass), &from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_interface_n(arg: *mut Arc<CallbackPassInterfaceN>) -> bool as IV_ {
    crate::callback_arg_interface_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_char(arg: *mut Arc<CallbackReturnChar>) -> bool as IW_ {
    crate::callback_return_char(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_boolean(arg: *mut Arc<CallbackReturnBoolean>) -> bool as IX_ {
    crate::callback_return_boolean(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_byte(arg: *mut Arc<CallbackReturnByte>) -> bool as IY_ {
    crate::callback_return_byte(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_ubyte(arg: *mut Arc<CallbackReturnUByte>) -> bool as IZ_ {
    crate::callback_return_ubyte(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_short(arg: *mut Arc<CallbackReturnShort>) -> bool as Ia_ {
    crate::callback_return_short(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_ushort(arg: *mut Arc<CallbackReturnUShort>) -> bool as Ib_ {
    crate::callback_return_ushort(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_int(arg: *mut Arc<CallbackReturnInt>) -> bool as Ic_ {
    crate::callback_return_int(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_uint(arg: *mut Arc<CallbackReturnUInt>) -> bool as Id_ {
    crate::callback_return_uint(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_long(arg: *mut Arc<CallbackReturnLong>) -> bool as Ie_ {
    crate::callback_return_long(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_ulong(arg: *mut Arc<CallbackReturnULong>) -> bool as If_ {
    crate::callback_return_ulong(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_float(arg: *mut Arc<CallbackReturnFloat>) -> bool as Ig_ {
    crate::callback_return_float(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_double(arg: *mut Arc<CallbackReturnDouble>) -> bool as Ih_ {
    crate::callback_return_double(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_string(arg: *mut Arc<CallbackReturnString>) -> bool as Ii_ {
    crate::callback_return_string(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_string_n(arg: *mut Arc<CallbackReturnStringN>) -> bool as Ij_ {
    crate::callback_return_string_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_callback(arg: *mut Arc<CallbackReturnCallback>) -> *mut Arc<VoidCallback> as Ik_ {
    into_raw(crate::callback_return_callback(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_callback_n(arg: *mut Arc<CallbackReturnCallbackN>) -> bool as Il_ {
    crate::callback_return_callback_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_enum(arg: *mut Arc<CallbackReturnEnum>) -> bool as Im_ {
    crate::callback_return_enum(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_dictionary(arg: *mut Arc<CallbackReturnDictionary>) -> bool as In_ {
    crate::callback_return_dictionary(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_dictionary_n(arg: *mut Arc<CallbackReturnDictionaryN>) -> bool as Io_ {
    crate::callback_return_dictionary_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_interface(arg: *mut Arc<CallbackReturnInterface>) -> bool as Ip_ {
    crate::callback_return_interface(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_interface_n(arg: *mut Arc<CallbackReturnInterfaceN>) -> bool as Iq_ {
    crate::callback_return_interface_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_char_array(arg: *mut Vec<u16>) -> bool as Ir_ {
    crate::pass_char_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_char_array_n(arg: *mut Vec<u16>) -> bool as Is_ {
    crate::pass_char_array_n(&ptr_opt(arg, from_raw))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_boolean_array(arg: *mut Vec<bool>) -> bool as It_ {
    crate::pass_boolean_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_byte_array(arg: *mut Vec<i8>) -> bool as Iu_ {
    crate::pass_byte_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ubyte_array(arg: *mut Vec<u8>) -> bool as Iv_ {
    crate::pass_ubyte_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_short_array(arg: *mut Vec<i16>) -> bool as Iw_ {
    crate::pass_short_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ushort_array(arg: *mut Vec<u16>) -> bool as Ix_ {
    crate::pass_ushort_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_int_array(arg: *mut Vec<i32>) -> bool as Iy_ {
    crate::pass_int_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_uint_array(arg: *mut Vec<u32>) -> bool as Iz_ {
    crate::pass_uint_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_long_array(arg: *mut Vec<i64>) -> bool as JA_ {
    crate::pass_long_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ulong_array(arg: *mut Vec<u64>) -> bool as JB_ {
    crate::pass_ulong_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_float_array(arg: *mut Vec<f32>) -> bool as JC_ {
    crate::pass_float_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_double_array(arg: *mut Vec<f64>) -> bool as JD_ {
    crate::pass_double_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_string_array(arg: *mut Vec<String>) -> bool as JE_ {
    crate::pass_string_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_string_array_n(arg: *mut Vec<Option<String>>) -> bool as JF_ {
    crate::pass_string_array_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_enum_array(arg: *mut Vec<MyEnum>) -> bool as JG_ {
    crate::pass_enum_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_dictionary_array(arg: *mut Vec<MyDictionary>) -> bool as JH_ {
    crate::pass_dictionary_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_dictionary_array_n(arg: *mut Vec<Option<MyDictionary>>) -> bool as JI_ {
    crate::pass_dictionary_array_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_interface_array(arg: *mut Vec<Arc<crate::MyInterface>>) -> bool as JJ_ {
    crate::pass_interface_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_interface_array_n(arg: *mut Vec<Option<Arc<crate::MyInterface>>>) -> bool as JK_ {
    crate::pass_interface_array_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_char_array() -> *mut Vec<u16> as JL_ {
    into_raw(crate::return_char_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_char_array_n() -> *mut Vec<u16> as JM_ {
    obj_opt(crate::return_char_array_n(), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_boolean_array() -> *mut Vec<bool> as JN_ {
    into_raw(crate::return_boolean_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_byte_array() -> *mut Vec<i8> as JO_ {
    into_raw(crate::return_byte_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ubyte_array() -> *mut Vec<u8> as JP_ {
    into_raw(crate::return_ubyte_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_short_array() -> *mut Vec<i16> as JQ_ {
    into_raw(crate::return_short_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ushort_array() -> *mut Vec<u16> as JR_ {
    into_raw(crate::return_ushort_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_int_array() -> *mut Vec<i32> as JS_ {
    into_raw(crate::return_int_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_uint_array() -> *mut Vec<u32> as JT_ {
    into_raw(crate::return_uint_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_long_array() -> *mut Vec<i64> as JU_ {
    into_raw(crate::return_long_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ulong_array() -> *mut Vec<u64> as JV_ {
    into_raw(crate::return_ulong_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_float_array() -> *mut Vec<f32> as JW_ {
    into_raw(crate::return_float_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_double_array() -> *mut Vec<f64> as JX_ {
    into_raw(crate::return_double_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_string_array() -> *mut Vec<String> as JY_ {
    into_raw(crate::return_string_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_string_array_n() -> *mut Vec<Option<String>> as JZ_ {
    into_raw(crate::return_string_array_n())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_enum_array() -> *mut Vec<MyEnum> as Ja_ {
    into_raw(crate::return_enum_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_dictionary_array() -> *mut Vec<MyDictionary> as Jb_ {
    into_raw(crate::return_dictionary_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_dictionary_array_n() -> *mut Vec<Option<MyDictionary>> as Jc_ {
    into_raw(crate::return_dictionary_array_n())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_char_array(arg: *mut Vec<u16>) -> *mut Vec<u16> as Jd_ {
    into_raw(crate::ping_char_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_char_array_n(arg: *mut Vec<u16>) -> *mut Vec<u16> as Je_ {
    obj_opt(crate::ping_char_array_n(&ptr_opt(arg, from_raw)), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_boolean_array(arg: *mut Vec<bool>) -> *mut Vec<bool> as Jf_ {
    into_raw(crate::ping_boolean_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_byte_array(arg: *mut Vec<i8>) -> *mut Vec<i8> as Jg_ {
    into_raw(crate::ping_byte_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_ubyte_array(arg: *mut Vec<u8>) -> *mut Vec<u8> as Jh_ {
    into_raw(crate::ping_ubyte_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_short_array(arg: *mut Vec<i16>) -> *mut Vec<i16> as Ji_ {
    into_raw(crate::ping_short_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_ushort_array(arg: *mut Vec<u16>) -> *mut Vec<u16> as Jj_ {
    into_raw(crate::ping_ushort_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_int_array(arg: *mut Vec<i32>) -> *mut Vec<i32> as Jk_ {
    into_raw(crate::ping_int_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_uint_array(arg: *mut Vec<u32>) -> *mut Vec<u32> as Jl_ {
    into_raw(crate::ping_uint_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_long_array(arg: *mut Vec<i64>) -> *mut Vec<i64> as Jm_ {
    into_raw(crate::ping_long_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_ulong_array(arg: *mut Vec<u64>) -> *mut Vec<u64> as Jn_ {
    into_raw(crate::ping_ulong_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_float_array(arg: *mut Vec<f32>) -> *mut Vec<f32> as Jo_ {
    into_raw(crate::ping_float_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_double_array(arg: *mut Vec<f64>) -> *mut Vec<f64> as Jp_ {
    into_raw(crate::ping_double_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_string_array(arg: *mut Vec<String>) -> *mut Vec<String> as Jq_ {
    into_raw(crate::ping_string_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_string_array_n(arg: *mut Vec<Option<String>>) -> *mut Vec<Option<String>> as Jr_ {
    into_raw(crate::ping_string_array_n(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_enum_array(arg: *mut Vec<MyEnum>) -> *mut Vec<MyEnum> as Js_ {
    into_raw(crate::ping_enum_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_dictionary_array(arg: *mut Vec<MyDictionary>) -> *mut Vec<MyDictionary> as Jt_ {
    into_raw(crate::ping_dictionary_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_dictionary_array_n(arg: *mut Vec<Option<MyDictionary>>) -> *mut Vec<Option<MyDictionary>> as Ju_ {
    into_raw(crate::ping_dictionary_array_n(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_interface_array(arg: *mut Vec<Arc<crate::MyInterface>>) -> *mut Vec<Arc<crate::MyInterface>> as Jv_ {
    into_raw(crate::ping_interface_array(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_interface_array_n(arg: *mut Vec<Option<Arc<crate::MyInterface>>>) -> *mut Vec<Option<Arc<crate::MyInterface>>> as Jw_ {
    into_raw(crate::ping_interface_array_n(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_char_array(arg: *mut Arc<CallbackPassCharArray>) -> bool as Jx_ {
    crate::callback_arg_char_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_char_array_n(arg: *mut Arc<CallbackPassCharArrayN>) -> bool as Jy_ {
    crate::callback_arg_char_array_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_boolean_array(arg: *mut Arc<CallbackPassBooleanArray>) -> bool as Jz_ {
    crate::callback_arg_boolean_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_byte_array(arg: *mut Arc<CallbackPassByteArray>) -> bool as KA_ {
    crate::callback_arg_byte_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_ubyte_array(arg: *mut Arc<CallbackPassUByteArray>) -> bool as KB_ {
    crate::callback_arg_ubyte_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_short_array(arg: *mut Arc<CallbackPassShortArray>) -> bool as KC_ {
    crate::callback_arg_short_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_ushort_array(arg: *mut Arc<CallbackPassUShortArray>) -> bool as KD_ {
    crate::callback_arg_ushort_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_int_array(arg: *mut Arc<CallbackPassIntArray>) -> bool as KE_ {
    crate::callback_arg_int_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_uint_array(arg: *mut Arc<CallbackPassUIntArray>) -> bool as KF_ {
    crate::callback_arg_uint_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_long_array(arg: *mut Arc<CallbackPassLongArray>) -> bool as KG_ {
    crate::callback_arg_long_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_ulong_array(arg: *mut Arc<CallbackPassULongArray>) -> bool as KH_ {
    crate::callback_arg_ulong_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_float_array(arg: *mut Arc<CallbackPassFloatArray>) -> bool as KI_ {
    crate::callback_arg_float_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_double_array(arg: *mut Arc<CallbackPassDoubleArray>) -> bool as KJ_ {
    crate::callback_arg_double_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_string_array(arg: *mut Arc<CallbackPassStringArray>) -> bool as KK_ {
    crate::callback_arg_string_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_string_array_n(arg: *mut Arc<CallbackPassStringArrayN>) -> bool as KL_ {
    crate::callback_arg_string_array_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_enum_array(arg: *mut Arc<CallbackPassEnumArray>) -> bool as KM_ {
    crate::callback_arg_enum_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_dictionary_array(arg: *mut Arc<CallbackPassDictionaryArray>) -> bool as KN_ {
    crate::callback_arg_dictionary_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_dictionary_array_n(arg: *mut Arc<CallbackPassDictionaryArrayN>) -> bool as KO_ {
    crate::callback_arg_dictionary_array_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_interface_array(arg: *mut Arc<CallbackPassInterfaceArray>) -> bool as KP_ {
    crate::callback_arg_interface_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_interface_array_n(arg: *mut Arc<CallbackPassInterfaceArrayN>) -> bool as KQ_ {
    crate::callback_arg_interface_array_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_char_array(arg: *mut Arc<CallbackReturnCharArray>) -> bool as KR_ {
    crate::callback_return_char_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_char_array_n(arg: *mut Arc<CallbackReturnCharArrayN>) -> bool as KS_ {
    crate::callback_return_char_array_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_boolean_array(arg: *mut Arc<CallbackReturnBooleanArray>) -> bool as KT_ {
    crate::callback_return_boolean_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_byte_array(arg: *mut Arc<CallbackReturnByteArray>) -> bool as KU_ {
    crate::callback_return_byte_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_ubyte_array(arg: *mut Arc<CallbackReturnUByteArray>) -> bool as KV_ {
    crate::callback_return_ubyte_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_short_array(arg: *mut Arc<CallbackReturnShortArray>) -> bool as KW_ {
    crate::callback_return_short_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_ushort_array(arg: *mut Arc<CallbackReturnUShortArray>) -> bool as KX_ {
    crate::callback_return_ushort_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_int_array(arg: *mut Arc<CallbackReturnIntArray>) -> bool as KY_ {
    crate::callback_return_int_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_uint_array(arg: *mut Arc<CallbackReturnUIntArray>) -> bool as KZ_ {
    crate::callback_return_uint_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_long_array(arg: *mut Arc<CallbackReturnLongArray>) -> bool as Ka_ {
    crate::callback_return_long_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_ulong_array(arg: *mut Arc<CallbackReturnULongArray>) -> bool as Kb_ {
    crate::callback_return_ulong_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_float_array(arg: *mut Arc<CallbackReturnFloatArray>) -> bool as Kc_ {
    crate::callback_return_float_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_double_array(arg: *mut Arc<CallbackReturnDoubleArray>) -> bool as Kd_ {
    crate::callback_return_double_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_string_array(arg: *mut Arc<CallbackReturnStringArray>) -> bool as Ke_ {
    crate::callback_return_string_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_string_array_n(arg: *mut Arc<CallbackReturnStringArrayN>) -> bool as Kf_ {
    crate::callback_return_string_array_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_enum_array(arg: *mut Arc<CallbackReturnEnumArray>) -> bool as Kg_ {
    crate::callback_return_enum_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_dictionary_array(arg: *mut Arc<CallbackReturnDictionaryArray>) -> bool as Kh_ {
    crate::callback_return_dictionary_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_dictionary_array_n(arg: *mut Arc<CallbackReturnDictionaryArrayN>) -> bool as Ki_ {
    crate::callback_return_dictionary_array_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_interface_array(arg: *mut Arc<CallbackReturnInterfaceArray>) -> bool as Kj_ {
    crate::callback_return_interface_array(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_interface_array_n(arg: *mut Arc<CallbackReturnInterfaceArrayN>) -> bool as Kk_ {
    crate::callback_return_interface_array_n(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_big_dictionary(arg: *mut TypeDictionary) -> bool as Kl_ {
    crate::pass_big_dictionary(&from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_big_dictionary(callback: *mut Arc<VoidCallback>, inter: *mut Arc<crate::MyInterface>) -> *mut TypeDictionary as Km_ {
    into_raw(crate::return_big_dictionary(&from_raw(callback), &from_raw(inter)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_big_dictionary(arg: *mut TypeDictionary) -> *mut TypeDictionary as Kn_ {
    into_raw(crate::ping_big_dictionary(&from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_big_dictionary_n(arg: *mut TypeDictionary) -> bool as Ko_ {
    crate::pass_big_dictionary_n(&ptr_opt(arg, from_raw))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_big_dictionary_n() -> *mut TypeDictionary as Kp_ {
    obj_opt(crate::return_big_dictionary_n(), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_big_dictionary_n(arg: *mut TypeDictionary) -> *mut TypeDictionary as Kq_ {
    obj_opt(crate::ping_big_dictionary_n(&ptr_opt(arg, from_raw)), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_primitives(a1: u16, a2: bool, a3: i8, a4: u8, a5: i16, a6: u16, a7: i32, a8: u32, a9: i64, a10: u64, a11: f32, a12: f64) -> bool as Kr_ {
    crate::critical_primitives(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_enum(a1: MyEnum) -> bool as Ks_ {
    crate::critical_enum(a1)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_string(a1: *mut u8, _a1_length: i32, _a1_size: i32) -> bool as Kt_ {
    crate::critical_string(&critical_string(a1, _a1_size))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_string_n(a1: *mut u8, _a1_length: i32, _a1_size: i32) -> bool as Ku_ {
    crate::critical_string_n(&critical_string_opt(a1, _a1_length, _a1_size))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_interface(a1: *mut Arc<crate::MyInterface>) -> bool as Kv_ {
    crate::critical_interface(&from_raw(a1))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_interface_n(a1: *mut Arc<crate::MyInterface>) -> bool as Kw_ {
    crate::critical_interface_n(&ptr_opt(a1, from_raw))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_primitives_array(a1: *mut u16, _a1_length: i32, a2: *mut bool, _a2_length: i32, a3: *mut i8, _a3_length: i32, a4: *mut u8, _a4_length: i32, a5: *mut i16, _a5_length: i32, a6: *mut u16, _a6_length: i32, a7: *mut i32, _a7_length: i32, a8: *mut u32, _a8_length: i32, a9: *mut i64, _a9_length: i32, a10: *mut u64, _a10_length: i32, a11: *mut f32, _a11_length: i32, a12: *mut f64, _a12_length: i32) -> bool as Kx_ {
    crate::critical_primitives_array(&critical_array(a1, _a1_length), &critical_array(a2, _a2_length), &critical_array(a3, _a3_length), &critical_array(a4, _a4_length), &critical_array(a5, _a5_length), &critical_array(a6, _a6_length), &critical_array(a7, _a7_length), &critical_array(a8, _a8_length), &critical_array(a9, _a9_length), &critical_array(a10, _a10_length), &critical_array(a11, _a11_length), &critical_array(a12, _a12_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_enum_array(a1: *mut MyEnum, _a1_length: i32) -> bool as Ky_ {
    crate::critical_enum_array(&critical_array(a1, _a1_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_primitives_array_n(a1: *mut u16, _a1_length: i32, a2: *mut bool, _a2_length: i32, a3: *mut i8, _a3_length: i32, a4: *mut u8, _a4_length: i32, a5: *mut i16, _a5_length: i32, a6: *mut u16, _a6_length: i32, a7: *mut i32, _a7_length: i32, a8: *mut u32, _a8_length: i32, a9: *mut i64, _a9_length: i32, a10: *mut u64, _a10_length: i32, a11: *mut f32, _a11_length: i32, a12: *mut f64, _a12_length: i32) -> bool as Kz_ {
    crate::critical_primitives_array_n(&critical_array_opt(a1, _a1_length), &critical_array_opt(a2, _a2_length), &critical_array_opt(a3, _a3_length), &critical_array_opt(a4, _a4_length), &critical_array_opt(a5, _a5_length), &critical_array_opt(a6, _a6_length), &critical_array_opt(a7, _a7_length), &critical_array_opt(a8, _a8_length), &critical_array_opt(a9, _a9_length), &critical_array_opt(a10, _a10_length), &critical_array_opt(a11, _a11_length), &critical_array_opt(a12, _a12_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_enum_array_n(a1: *mut MyEnum, _a1_length: i32) -> bool as LA_ {
    crate::critical_enum_array_n(&critical_array_opt(a1, _a1_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_char() -> u16 as LB_ {
    crate::critical_return_char()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_boolean() -> bool as LC_ {
    crate::critical_return_boolean()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_byte() -> i8 as LD_ {
    crate::critical_return_byte()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_ubyte() -> u8 as LE_ {
    crate::critical_return_ubyte()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_short() -> i16 as LF_ {
    crate::critical_return_short()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_ushort() -> u16 as LG_ {
    crate::critical_return_ushort()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_int() -> i32 as LH_ {
    crate::critical_return_int()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_uint() -> u32 as LI_ {
    crate::critical_return_uint()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_long() -> i64 as LJ_ {
    crate::critical_return_long()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_ulong() -> u64 as LK_ {
    crate::critical_return_ulong()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_float() -> f32 as LL_ {
    crate::critical_return_float()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_double() -> f64 as LM_ {
    crate::critical_return_double()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_enum() -> MyEnum as LN_ {
    crate::critical_return_enum()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_interface() -> *mut Arc<crate::MyInterface> as LO_ {
    into_raw(crate::critical_return_interface())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci1() -> bool as LP_ {
    crate::jvmci1()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci2(a1: i32) -> bool as LQ_ {
    crate::jvmci2(a1)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci3(a1: i32, a2: i32) -> bool as LR_ {
    crate::jvmci3(a1, a2)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci4(a1: i32, a2: i32, a3: i32, a4: i32, a5: i32, a6: i32, a7: i32, a8: i32, a9: i32) -> bool as LS_ {
    crate::jvmci4(a1, a2, a3, a4, a5, a6, a7, a8, a9)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci5(a1: i32, a2: i64, a3: i32, a4: i64, a5: i32, a6: i64, a7: i32, a8: i32, a9: i64) -> bool as LT_ {
    crate::jvmci5(a1, a2, a3, a4, a5, a6, a7, a8, a9)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci6(a1: f32, a2: f32, a3: f32, a4: f32, a5: f32, a6: f32, a7: f32, a8: f32, a9: f32, a10: i32, a11: i32, a12: i32, a13: i32) -> bool as LU_ {
    crate::jvmci6(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci7(a1: f32, a2: f64, a3: f32, a4: f64, a5: f32, a6: f64, a7: f32, a8: f32, a9: f64) -> bool as LV_ {
    crate::jvmci7(a1, a2, a3, a4, a5, a6, a7, a8, a9)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci8(a1: i32, a2: f64, a3: f32, a4: i64) -> bool as LW_ {
    crate::jvmci8(a1, a2, a3, a4)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci9(a1: i32, a2: f64, a3: f32, a4: i64, a5: i64, a6: f64, a7: f32, a8: f32, a9: i32) -> bool as LX_ {
    crate::jvmci9(a1, a2, a3, a4, a5, a6, a7, a8, a9)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci10(a1: *mut u8, _a1_length: i32, _a1_size: i32, a2: f64, a3: f32, a4: i64, a5: i64, a6: f64, a7: *mut u8, _a7_length: i32, _a7_size: i32, a8: f32, a9: i32) -> bool as LY_ {
    crate::jvmci10(&critical_string(a1, _a1_size), a2, a3, a4, a5, a6, &critical_string(a7, _a7_size), a8, a9)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci11(a1: f32, a2: i32, a3: f32, a4: i32, a5: f32, a6: i32, a7: f32, a8: i32, a9: f32, a10: i32, a11: f32, a12: i32, a13: f32, a14: i32, a15: f32, a16: i32, a17: f32) -> bool as LZ_ {
    crate::jvmci11(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci12() -> i32 as La_ {
    crate::jvmci12()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci13() -> i64 as Lb_ {
    crate::jvmci13()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci14() -> f32 as Lc_ {
    crate::jvmci14()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci15() -> f64 as Ld_ {
    crate::jvmci15()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci_array(array: *mut i32, _array_length: i32) -> bool as Le_ {
    crate::jvmci_array(&critical_array(array, _array_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci_some_arrays(array1: *mut i32, _array1_length: i32, array2: *mut f32, _array2_length: i32, array3: *mut f64, _array3_length: i32) -> bool as Lf_ {
    crate::jvmci_some_arrays(&critical_array(array1, _array1_length), &critical_array(array2, _array2_length), &critical_array(array3, _array3_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci_enum(enum1: MyEnum, enum2: MyEnum, enum_array: *mut MyEnum, _enum_array_length: i32) -> bool as Lg_ {
    crate::jvmci_enum(enum1, enum2, &critical_array(enum_array, _enum_array_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_new_0() -> *mut Arc<crate::MyInterface> as Lh_ {
    into_raw(Arc::new(crate::MyInterface::new()))
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_new_1(a: i32) -> *mut Arc<crate::MyInterface> as Li_ {
    into_raw(Arc::new(crate::MyInterface::new_critical(a)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_fn_test(_self: *mut Arc<crate::MyInterface>) -> bool as Lj_ {
    unsafe { &*_self }.test()
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_fn_test_critical(_self: *mut Arc<crate::MyInterface>) -> bool as Lk_ {
    unsafe { &*_self }.test_critical()
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_fn_pass_interface(_self: *mut Arc<crate::MyInterface>, a1: *mut Arc<crate::MyInterface>) -> () as Ll_ {
    unsafe { &*_self }.pass_interface(&from_raw(a1))
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_fn_return_interface(_self: *mut Arc<crate::MyInterface>) -> *mut Arc<crate::MyInterface> as Lm_ {
    into_raw(unsafe { &*_self }.return_interface())
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_free(_self: *mut Arc<crate::MyInterface>) -> () as Ln_ {
    from_raw(_self);
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_clone(_self: *mut Arc<crate::MyInterface>) -> *mut Arc<crate::MyInterface> as Lo_ {
    into_raw(unsafe { &*_self }.clone())
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_address(_self: *mut Arc<crate::MyInterface>) -> usize as Lp_ {
    Arc::as_ptr(unsafe { &*_self }) as usize
}}