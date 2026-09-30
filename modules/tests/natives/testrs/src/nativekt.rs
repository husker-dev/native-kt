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
	fn nativekt_natives_testrs_testrs__string_new(data: *mut u8, size: i32, make_copy: bool) -> *mut String as S_ {
	    if make_copy {
	        unsafe { into_raw(String::from_utf8_unchecked(std::slice::from_raw_parts(data, size as usize).to_vec())) }
	    } else {
	        unsafe { into_raw(String::from_raw_parts(data, size as usize, size as usize)) }
	    }
	}
	
	fn nativekt_natives_testrs_testrs__string_data(str: *mut String) -> *mut u8 as T_ {
	    unsafe { (&*str).as_ptr().cast_mut() }
	}
	fn nativekt_natives_testrs_testrs__string_size(str: *mut String) -> i32 as U_ {
	    unsafe { (&*str).len() as i32 }
	}
	fn nativekt_natives_testrs_testrs__string_free(str: *mut String) -> () as W_ {
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
    nativekt_natives_testrs_testrs__chararray_new, X_,
    nativekt_natives_testrs_testrs__chararray_elements, Y_,
    nativekt_natives_testrs_testrs__chararray_length, Z_,
    nativekt_natives_testrs_testrs__chararray_free, a_
);
impl_typed_array!(bool,
    nativekt_natives_testrs_testrs__booleanarray_new, b_,
    nativekt_natives_testrs_testrs__booleanarray_elements, c_,
    nativekt_natives_testrs_testrs__booleanarray_length, d_,
    nativekt_natives_testrs_testrs__booleanarray_free, e_
);
impl_typed_array!(i8,
    nativekt_natives_testrs_testrs__bytearray_new, f_,
    nativekt_natives_testrs_testrs__bytearray_elements, g_,
    nativekt_natives_testrs_testrs__bytearray_length, h_,
    nativekt_natives_testrs_testrs__bytearray_free, i_
);
impl_typed_array!(i16,
    nativekt_natives_testrs_testrs__shortarray_new, j_,
    nativekt_natives_testrs_testrs__shortarray_elements, k_,
    nativekt_natives_testrs_testrs__shortarray_length, l_,
    nativekt_natives_testrs_testrs__shortarray_free, m_
);
impl_typed_array!(i32,
    nativekt_natives_testrs_testrs__intarray_new, n_,
    nativekt_natives_testrs_testrs__intarray_elements, o_,
    nativekt_natives_testrs_testrs__intarray_length, p_,
    nativekt_natives_testrs_testrs__intarray_free, q_
);
impl_typed_array!(i64,
    nativekt_natives_testrs_testrs__longarray_new, r_,
    nativekt_natives_testrs_testrs__longarray_elements, s_,
    nativekt_natives_testrs_testrs__longarray_length, t_,
    nativekt_natives_testrs_testrs__longarray_free, u_
);
impl_typed_array!(f32,
    nativekt_natives_testrs_testrs__floatarray_new, v_,
    nativekt_natives_testrs_testrs__floatarray_elements, w_,
    nativekt_natives_testrs_testrs__floatarray_length, x_,
    nativekt_natives_testrs_testrs__floatarray_free, y_
);
impl_typed_array!(f64,
    nativekt_natives_testrs_testrs__doublearray_new, z_,
    nativekt_natives_testrs_testrs__doublearray_elements, BA_,
    nativekt_natives_testrs_testrs__doublearray_length, BB_,
    nativekt_natives_testrs_testrs__doublearray_free, BC_
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

impl_object_array!(MyDictionary,
    nativekt_natives_testrs_testrs__array_mydictionary_new, BI_,
    nativekt_natives_testrs_testrs__array_mydictionary_length, BJ_,
    nativekt_natives_testrs_testrs__array_mydictionary_push, BK_,
    nativekt_natives_testrs_testrs__array_mydictionary_get, BL_,
    nativekt_natives_testrs_testrs__array_mydictionary_free, BM_
);
impl_object_array!(Arc<crate::MyInterface>,
    nativekt_natives_testrs_testrs__array_myinterface_new, BX_,
    nativekt_natives_testrs_testrs__array_myinterface_length, BY_,
    nativekt_natives_testrs_testrs__array_myinterface_push, BZ_,
    nativekt_natives_testrs_testrs__array_myinterface_get, Ba_,
    nativekt_natives_testrs_testrs__array_myinterface_free, Bb_
);
impl_object_array!(String,
    nativekt_natives_testrs_testrs__array_string_new, Bc_,
    nativekt_natives_testrs_testrs__array_string_length, Bd_,
    nativekt_natives_testrs_testrs__array_string_push, Be_,
    nativekt_natives_testrs_testrs__array_string_get, Bf_,
    nativekt_natives_testrs_testrs__array_string_free, Bg_
);
impl_object_array!(u16,
    nativekt_natives_testrs_testrs__array_char_new, Bh_,
    nativekt_natives_testrs_testrs__array_char_length, Bi_,
    nativekt_natives_testrs_testrs__array_char_push, Bj_,
    nativekt_natives_testrs_testrs__array_char_get, Bk_,
    nativekt_natives_testrs_testrs__array_char_free, Bl_
);
impl_object_array!(bool,
    nativekt_natives_testrs_testrs__array_boolean_new, Bm_,
    nativekt_natives_testrs_testrs__array_boolean_length, Bn_,
    nativekt_natives_testrs_testrs__array_boolean_push, Bo_,
    nativekt_natives_testrs_testrs__array_boolean_get, Bp_,
    nativekt_natives_testrs_testrs__array_boolean_free, Bq_
);
impl_object_array!(i8,
    nativekt_natives_testrs_testrs__array_byte_new, Br_,
    nativekt_natives_testrs_testrs__array_byte_length, Bs_,
    nativekt_natives_testrs_testrs__array_byte_push, Bt_,
    nativekt_natives_testrs_testrs__array_byte_get, Bu_,
    nativekt_natives_testrs_testrs__array_byte_free, Bv_
);
impl_object_array!(i16,
    nativekt_natives_testrs_testrs__array_short_new, Bw_,
    nativekt_natives_testrs_testrs__array_short_length, Bx_,
    nativekt_natives_testrs_testrs__array_short_push, By_,
    nativekt_natives_testrs_testrs__array_short_get, Bz_,
    nativekt_natives_testrs_testrs__array_short_free, CA_
);
impl_object_array!(i32,
    nativekt_natives_testrs_testrs__array_int_new, CB_,
    nativekt_natives_testrs_testrs__array_int_length, CC_,
    nativekt_natives_testrs_testrs__array_int_push, CD_,
    nativekt_natives_testrs_testrs__array_int_get, CE_,
    nativekt_natives_testrs_testrs__array_int_free, CF_
);
impl_object_array!(i64,
    nativekt_natives_testrs_testrs__array_long_new, CG_,
    nativekt_natives_testrs_testrs__array_long_length, CH_,
    nativekt_natives_testrs_testrs__array_long_push, CI_,
    nativekt_natives_testrs_testrs__array_long_get, CJ_,
    nativekt_natives_testrs_testrs__array_long_free, CK_
);
impl_object_array!(f32,
    nativekt_natives_testrs_testrs__array_float_new, CL_,
    nativekt_natives_testrs_testrs__array_float_length, CM_,
    nativekt_natives_testrs_testrs__array_float_push, CN_,
    nativekt_natives_testrs_testrs__array_float_get, CO_,
    nativekt_natives_testrs_testrs__array_float_free, CP_
);
impl_object_array!(f64,
    nativekt_natives_testrs_testrs__array_double_new, CQ_,
    nativekt_natives_testrs_testrs__array_double_length, CR_,
    nativekt_natives_testrs_testrs__array_double_push, CS_,
    nativekt_natives_testrs_testrs__array_double_get, CT_,
    nativekt_natives_testrs_testrs__array_double_free, CU_
);

// ╔══════════════════════════╗
// ║     Boxed primitives     ║
// ╚══════════════════════════╝

fn into_raw_primitive<T>(of: T) -> *mut T {
	unsafe {
		let result = alloc(Layout::from_size_align_unchecked(size_of::<T>(), align_of::<T>())) as *mut T;
		result.write(of);
		result
	}
}
fn from_raw_primitive<T>(of: *mut T) -> T {
	unsafe {
		let result = of.read();
		dealloc(of as *mut u8, Layout::from_size_align_unchecked(size_of::<T>(), align_of::<T>()));
		result
	}
}

macro_rules! impl_boxed_primitive {
    ($rsType:ident, 
    $funcNew:ident, $funcNewJs:ident, 
    $funcGet:ident, $funcGetJs:ident) => {
        export_fn! {
            fn $funcNew(value: $rsType) -> *mut $rsType as $funcNewJs {
                into_raw_primitive(value)
            }
            fn $funcGet(of: *mut $rsType, free: bool) -> $rsType as $funcGetJs {
                if(free) { from_raw_primitive(of) } 
                else { unsafe { of.read() } }
            }
        }
    };
}
impl_boxed_primitive!(
    u16,
    nativekt_natives_testrs_testrs__char_new, C_,
    nativekt_natives_testrs_testrs__char_get, D_
);
impl_boxed_primitive!(
    i8,
    nativekt_natives_testrs_testrs__boolean_new, E_,
    nativekt_natives_testrs_testrs__boolean_get, F_
);
impl_boxed_primitive!(
    i8,
    nativekt_natives_testrs_testrs__byte_new, G_,
    nativekt_natives_testrs_testrs__byte_get, H_
);
impl_boxed_primitive!(
    i16,
    nativekt_natives_testrs_testrs__short_new, I_,
    nativekt_natives_testrs_testrs__short_get, J_
);
impl_boxed_primitive!(
    i32,
    nativekt_natives_testrs_testrs__int_new, K_,
    nativekt_natives_testrs_testrs__int_get, L_
);
impl_boxed_primitive!(
    i64,
    nativekt_natives_testrs_testrs__long_new, M_,
    nativekt_natives_testrs_testrs__long_get, N_
);
impl_boxed_primitive!(
    f32,
    nativekt_natives_testrs_testrs__float_new, O_,
    nativekt_natives_testrs_testrs__float_get, P_
);
impl_boxed_primitive!(
    f64,
    nativekt_natives_testrs_testrs__double_new, Q_,
    nativekt_natives_testrs_testrs__double_get, R_
);

// ╔════════════════════════════╗
// ║     Critical functions     ║
// ╚════════════════════════════╝

fn critical_string(data: *mut u8, size: i32) -> ManuallyDrop<String> {
    unsafe { ManuallyDrop::new(String::from_raw_parts(data, size as usize, size as usize)) }
}

fn critical_string_opt(data: *mut u8, size: i32) -> ManuallyDrop<Option<String>> {
    ManuallyDrop::new(if size != -1 {
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
pub struct EmptyDictionary {
}
export_fn! {
	fn nativekt_natives_testrs_testrs__emptydictionary_new() -> *mut EmptyDictionary as CV_ {
		into_raw(EmptyDictionary {  })
	}
	fn nativekt_natives_testrs_testrs__emptydictionary_free(of: *mut EmptyDictionary) -> () as CW_ { from_raw(of); }
}

#[derive(Clone)]
pub struct ParentDictionary {
	pub a: i32, 
	pub b: i32
}
export_fn! {
	fn nativekt_natives_testrs_testrs__parentdictionary_new(a: i32, b: i32) -> *mut ParentDictionary as Cd_ {
		into_raw(ParentDictionary { a, b })
	}
	fn nativekt_natives_testrs_testrs__parentdictionary_free(of: *mut ParentDictionary) -> () as Ce_ { from_raw(of); }
	fn nativekt_natives_testrs_testrs__parentdictionary__a(of: *mut ParentDictionary) -> i32 as Cf_ { unsafe { (*of).a } }
	fn nativekt_natives_testrs_testrs__parentdictionary__b(of: *mut ParentDictionary) -> i32 as Cg_ { unsafe { (*of).b } }
}

#[derive(Clone)]
pub struct MyDictionary {
	pub a: i32, 
	pub b: i32, 
	pub c: i32, 
	pub d: i32
}
export_fn! {
	fn nativekt_natives_testrs_testrs__mydictionary_new(a: i32, b: i32, c: i32, d: i32) -> *mut MyDictionary as CX_ {
		into_raw(MyDictionary { a, b, c, d })
	}
	fn nativekt_natives_testrs_testrs__mydictionary_free(of: *mut MyDictionary) -> () as CY_ { from_raw(of); }
	fn nativekt_natives_testrs_testrs__mydictionary__a(of: *mut MyDictionary) -> i32 as CZ_ { unsafe { (*of).a } }
	fn nativekt_natives_testrs_testrs__mydictionary__b(of: *mut MyDictionary) -> i32 as Ca_ { unsafe { (*of).b } }
	fn nativekt_natives_testrs_testrs__mydictionary__c(of: *mut MyDictionary) -> i32 as Cb_ { unsafe { (*of).c } }
	fn nativekt_natives_testrs_testrs__mydictionary__d(of: *mut MyDictionary) -> i32 as Cc_ { unsafe { (*of).d } }
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
	pub a16: Option<MyDictionary>, 
	pub a17: Arc<crate::MyInterface>, 
	pub a18: Option<Arc<crate::MyInterface>>, 
	pub a19: Arc<VoidCallback>, 
	pub a20: Vec<u16>, 
	pub a21: Vec<bool>, 
	pub a22: Vec<i8>, 
	pub a23: Vec<u8>, 
	pub a24: Vec<i16>, 
	pub a25: Vec<u16>, 
	pub a26: Vec<i32>, 
	pub a27: Vec<u32>, 
	pub a28: Vec<i64>, 
	pub a29: Vec<u64>, 
	pub a30: Vec<f32>, 
	pub a31: Vec<f64>, 
	pub a32: Vec<String>, 
	pub a33: Vec<MyEnum>, 
	pub a34: Vec<MyDictionary>, 
	pub a35: Vec<Arc<crate::MyInterface>>
}
export_fn! {
	fn nativekt_natives_testrs_testrs__typedictionary_new(a1: u16, a2: bool, a3: i8, a4: u8, a5: i16, a6: u16, a7: i32, a8: u32, a9: i64, a10: u64, a11: f32, a12: f64, a13: *mut String, a14: MyEnum, a15: *mut MyDictionary, a16: *mut MyDictionary, a17: *mut Arc<crate::MyInterface>, a18: *mut Arc<crate::MyInterface>, a19: *mut Arc<VoidCallback>, a20: *mut Vec<u16>, a21: *mut Vec<bool>, a22: *mut Vec<i8>, a23: *mut Vec<u8>, a24: *mut Vec<i16>, a25: *mut Vec<u16>, a26: *mut Vec<i32>, a27: *mut Vec<u32>, a28: *mut Vec<i64>, a29: *mut Vec<u64>, a30: *mut Vec<f32>, a31: *mut Vec<f64>, a32: *mut Vec<String>, a33: *mut Vec<MyEnum>, a34: *mut Vec<MyDictionary>, a35: *mut Vec<Arc<crate::MyInterface>>) -> *mut TypeDictionary as Ch_ {
		into_raw(TypeDictionary { a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13: from_raw(a13), a14, a15: from_raw(a15), a16: ptr_opt(a16, from_raw), a17: from_raw(a17), a18: ptr_opt(a18, from_raw), a19: from_raw(a19), a20: from_raw(a20), a21: from_raw(a21), a22: from_raw(a22), a23: from_raw(a23), a24: from_raw(a24), a25: from_raw(a25), a26: from_raw(a26), a27: from_raw(a27), a28: from_raw(a28), a29: from_raw(a29), a30: from_raw(a30), a31: from_raw(a31), a32: from_raw(a32), a33: from_raw(a33), a34: from_raw(a34), a35: from_raw(a35) })
	}
	fn nativekt_natives_testrs_testrs__typedictionary_free(of: *mut TypeDictionary) -> () as Ci_ { from_raw(of); }
	fn nativekt_natives_testrs_testrs__typedictionary__a1(of: *mut TypeDictionary) -> u16 as Cj_ { unsafe { (*of).a1 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a2(of: *mut TypeDictionary) -> bool as Ck_ { unsafe { (*of).a2 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a3(of: *mut TypeDictionary) -> i8 as Cl_ { unsafe { (*of).a3 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a4(of: *mut TypeDictionary) -> u8 as Cm_ { unsafe { (*of).a4 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a5(of: *mut TypeDictionary) -> i16 as Cn_ { unsafe { (*of).a5 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a6(of: *mut TypeDictionary) -> u16 as Co_ { unsafe { (*of).a6 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a7(of: *mut TypeDictionary) -> i32 as Cp_ { unsafe { (*of).a7 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a8(of: *mut TypeDictionary) -> u32 as Cq_ { unsafe { (*of).a8 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a9(of: *mut TypeDictionary) -> i64 as Cr_ { unsafe { (*of).a9 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a10(of: *mut TypeDictionary) -> u64 as Cs_ { unsafe { (*of).a10 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a11(of: *mut TypeDictionary) -> f32 as Ct_ { unsafe { (*of).a11 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a12(of: *mut TypeDictionary) -> f64 as Cu_ { unsafe { (*of).a12 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a13(of: *mut TypeDictionary) -> *const String as Cv_ { unsafe { &(*of).a13 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a14(of: *mut TypeDictionary) -> MyEnum as Cw_ { unsafe { (*of).a14.clone() } }
	fn nativekt_natives_testrs_testrs__typedictionary__a15(of: *mut TypeDictionary) -> *const MyDictionary as Cx_ { unsafe { &(*of).a15 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a16(of: *mut TypeDictionary) -> *const MyDictionary as Cy_ { unsafe { (&(*of).a16).as_ref().map_or(core::ptr::null(), |it| it as *const MyDictionary) } }
	fn nativekt_natives_testrs_testrs__typedictionary__a17(of: *mut TypeDictionary) -> *const Arc<crate::MyInterface> as Cz_ { unsafe { &(*of).a17 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a18(of: *mut TypeDictionary) -> *const Arc<crate::MyInterface> as DA_ { unsafe { (&(*of).a18).as_ref().map_or(core::ptr::null(), |it| it as *const Arc<crate::MyInterface>) } }
	fn nativekt_natives_testrs_testrs__typedictionary__a19(of: *mut TypeDictionary) -> *const Arc<VoidCallback> as DB_ { unsafe { &(*of).a19 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a20(of: *mut TypeDictionary) -> *const Vec<u16> as DC_ { unsafe { &(*of).a20 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a21(of: *mut TypeDictionary) -> *const Vec<bool> as DD_ { unsafe { &(*of).a21 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a22(of: *mut TypeDictionary) -> *const Vec<i8> as DE_ { unsafe { &(*of).a22 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a23(of: *mut TypeDictionary) -> *const Vec<u8> as DF_ { unsafe { &(*of).a23 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a24(of: *mut TypeDictionary) -> *const Vec<i16> as DG_ { unsafe { &(*of).a24 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a25(of: *mut TypeDictionary) -> *const Vec<u16> as DH_ { unsafe { &(*of).a25 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a26(of: *mut TypeDictionary) -> *const Vec<i32> as DI_ { unsafe { &(*of).a26 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a27(of: *mut TypeDictionary) -> *const Vec<u32> as DJ_ { unsafe { &(*of).a27 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a28(of: *mut TypeDictionary) -> *const Vec<i64> as DK_ { unsafe { &(*of).a28 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a29(of: *mut TypeDictionary) -> *const Vec<u64> as DL_ { unsafe { &(*of).a29 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a30(of: *mut TypeDictionary) -> *const Vec<f32> as DM_ { unsafe { &(*of).a30 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a31(of: *mut TypeDictionary) -> *const Vec<f64> as DN_ { unsafe { &(*of).a31 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a32(of: *mut TypeDictionary) -> *const Vec<String> as DO_ { unsafe { &(*of).a32 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a33(of: *mut TypeDictionary) -> *const Vec<MyEnum> as DP_ { unsafe { &(*of).a33 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a34(of: *mut TypeDictionary) -> *const Vec<MyDictionary> as DQ_ { unsafe { &(*of).a34 } }
	fn nativekt_natives_testrs_testrs__typedictionary__a35(of: *mut TypeDictionary) -> *const Vec<Arc<crate::MyInterface>> as DR_ { unsafe { &(*of).a35 } }
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
	nativekt_natives_testrs_testrs__callbackpassboolean_new, DS_,
	nativekt_natives_testrs_testrs__callbackpassboolean_id, DT_,
	nativekt_natives_testrs_testrs__callbackpassboolean_free, DU_
);
impl_callback!(
    CallbackPassBooleanArray,
	external_fn_type!(bool; isize, *mut Vec<bool>),
	pub fn invoke(&self, arg: Vec<bool>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassbooleanarray_new, DV_,
	nativekt_natives_testrs_testrs__callbackpassbooleanarray_id, DW_,
	nativekt_natives_testrs_testrs__callbackpassbooleanarray_free, DX_
);
impl_callback!(
    CallbackPassByte,
	external_fn_type!(bool; isize, i8),
	pub fn invoke(&self, arg: i8) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassbyte_new, DY_,
	nativekt_natives_testrs_testrs__callbackpassbyte_id, DZ_,
	nativekt_natives_testrs_testrs__callbackpassbyte_free, Da_
);
impl_callback!(
    CallbackPassByteArray,
	external_fn_type!(bool; isize, *mut Vec<i8>),
	pub fn invoke(&self, arg: Vec<i8>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassbytearray_new, Db_,
	nativekt_natives_testrs_testrs__callbackpassbytearray_id, Dc_,
	nativekt_natives_testrs_testrs__callbackpassbytearray_free, Dd_
);
impl_callback!(
    CallbackPassCallback,
	external_fn_type!(bool; isize, *mut Arc<VoidCallback>),
	pub fn invoke(&self, arg: Arc<VoidCallback>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpasscallback_new, De_,
	nativekt_natives_testrs_testrs__callbackpasscallback_id, Df_,
	nativekt_natives_testrs_testrs__callbackpasscallback_free, Dg_
);
impl_callback!(
    CallbackPassCallbackN,
	external_fn_type!(bool; isize, *mut Arc<VoidCallback>),
	pub fn invoke(&self, arg: Option<Arc<VoidCallback>>) -> bool { external_fn_call!(bool; self.invoke; self.id, obj_opt(arg, into_raw)) },
	nativekt_natives_testrs_testrs__callbackpasscallbackn_new, Dh_,
	nativekt_natives_testrs_testrs__callbackpasscallbackn_id, Di_,
	nativekt_natives_testrs_testrs__callbackpasscallbackn_free, Dj_
);
impl_callback!(
    CallbackPassChar,
	external_fn_type!(bool; isize, u16),
	pub fn invoke(&self, arg: u16) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpasschar_new, Dk_,
	nativekt_natives_testrs_testrs__callbackpasschar_id, Dl_,
	nativekt_natives_testrs_testrs__callbackpasschar_free, Dm_
);
impl_callback!(
    CallbackPassCharArray,
	external_fn_type!(bool; isize, *mut Vec<u16>),
	pub fn invoke(&self, arg: Vec<u16>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpasschararray_new, Dn_,
	nativekt_natives_testrs_testrs__callbackpasschararray_id, Do_,
	nativekt_natives_testrs_testrs__callbackpasschararray_free, Dp_
);
impl_callback!(
    CallbackPassCharArrayEmpty,
	external_fn_type!(bool; isize, *mut Vec<u16>),
	pub fn invoke(&self, arg: Vec<u16>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpasschararrayempty_new, Dq_,
	nativekt_natives_testrs_testrs__callbackpasschararrayempty_id, Dr_,
	nativekt_natives_testrs_testrs__callbackpasschararrayempty_free, Ds_
);
impl_callback!(
    CallbackPassCharArrayN,
	external_fn_type!(bool; isize, *mut Vec<u16>),
	pub fn invoke(&self, arg: Option<Vec<u16>>) -> bool { external_fn_call!(bool; self.invoke; self.id, obj_opt(arg, into_raw)) },
	nativekt_natives_testrs_testrs__callbackpasschararrayn_new, Dt_,
	nativekt_natives_testrs_testrs__callbackpasschararrayn_id, Du_,
	nativekt_natives_testrs_testrs__callbackpasschararrayn_free, Dv_
);
impl_callback!(
    CallbackPassDictionary,
	external_fn_type!(bool; isize, *mut MyDictionary),
	pub fn invoke(&self, arg: MyDictionary) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassdictionary_new, Dw_,
	nativekt_natives_testrs_testrs__callbackpassdictionary_id, Dx_,
	nativekt_natives_testrs_testrs__callbackpassdictionary_free, Dy_
);
impl_callback!(
    CallbackPassDictionaryArray,
	external_fn_type!(bool; isize, *mut Vec<MyDictionary>),
	pub fn invoke(&self, arg: Vec<MyDictionary>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassdictionaryarray_new, Dz_,
	nativekt_natives_testrs_testrs__callbackpassdictionaryarray_id, EA_,
	nativekt_natives_testrs_testrs__callbackpassdictionaryarray_free, EB_
);
impl_callback!(
    CallbackPassDictionaryArrayN,
	external_fn_type!(bool; isize, *mut Vec<Option<MyDictionary>>),
	pub fn invoke(&self, arg: Vec<Option<MyDictionary>>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassdictionaryarrayn_new, EC_,
	nativekt_natives_testrs_testrs__callbackpassdictionaryarrayn_id, ED_,
	nativekt_natives_testrs_testrs__callbackpassdictionaryarrayn_free, EE_
);
impl_callback!(
    CallbackPassDictionaryN,
	external_fn_type!(bool; isize, *mut MyDictionary),
	pub fn invoke(&self, arg: Option<MyDictionary>) -> bool { external_fn_call!(bool; self.invoke; self.id, obj_opt(arg, into_raw)) },
	nativekt_natives_testrs_testrs__callbackpassdictionaryn_new, EF_,
	nativekt_natives_testrs_testrs__callbackpassdictionaryn_id, EG_,
	nativekt_natives_testrs_testrs__callbackpassdictionaryn_free, EH_
);
impl_callback!(
    CallbackPassDouble,
	external_fn_type!(bool; isize, f64),
	pub fn invoke(&self, arg: f64) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassdouble_new, EI_,
	nativekt_natives_testrs_testrs__callbackpassdouble_id, EJ_,
	nativekt_natives_testrs_testrs__callbackpassdouble_free, EK_
);
impl_callback!(
    CallbackPassDoubleArray,
	external_fn_type!(bool; isize, *mut Vec<f64>),
	pub fn invoke(&self, arg: Vec<f64>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassdoublearray_new, EL_,
	nativekt_natives_testrs_testrs__callbackpassdoublearray_id, EM_,
	nativekt_natives_testrs_testrs__callbackpassdoublearray_free, EN_
);
impl_callback!(
    CallbackPassEnum,
	external_fn_type!(bool; isize, MyEnum),
	pub fn invoke(&self, arg: MyEnum) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassenum_new, EO_,
	nativekt_natives_testrs_testrs__callbackpassenum_id, EP_,
	nativekt_natives_testrs_testrs__callbackpassenum_free, EQ_
);
impl_callback!(
    CallbackPassEnumArray,
	external_fn_type!(bool; isize, *mut Vec<MyEnum>),
	pub fn invoke(&self, arg: Vec<MyEnum>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassenumarray_new, ER_,
	nativekt_natives_testrs_testrs__callbackpassenumarray_id, ES_,
	nativekt_natives_testrs_testrs__callbackpassenumarray_free, ET_
);
impl_callback!(
    CallbackPassFloat,
	external_fn_type!(bool; isize, f32),
	pub fn invoke(&self, arg: f32) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassfloat_new, EU_,
	nativekt_natives_testrs_testrs__callbackpassfloat_id, EV_,
	nativekt_natives_testrs_testrs__callbackpassfloat_free, EW_
);
impl_callback!(
    CallbackPassFloatArray,
	external_fn_type!(bool; isize, *mut Vec<f32>),
	pub fn invoke(&self, arg: Vec<f32>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassfloatarray_new, EX_,
	nativekt_natives_testrs_testrs__callbackpassfloatarray_id, EY_,
	nativekt_natives_testrs_testrs__callbackpassfloatarray_free, EZ_
);
impl_callback!(
    CallbackPassInt,
	external_fn_type!(bool; isize, i32),
	pub fn invoke(&self, arg: i32) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassint_new, Ea_,
	nativekt_natives_testrs_testrs__callbackpassint_id, Eb_,
	nativekt_natives_testrs_testrs__callbackpassint_free, Ec_
);
impl_callback!(
    CallbackPassIntArray,
	external_fn_type!(bool; isize, *mut Vec<i32>),
	pub fn invoke(&self, arg: Vec<i32>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassintarray_new, Ed_,
	nativekt_natives_testrs_testrs__callbackpassintarray_id, Ee_,
	nativekt_natives_testrs_testrs__callbackpassintarray_free, Ef_
);
impl_callback!(
    CallbackPassInterface,
	external_fn_type!(bool; isize, *mut Arc<crate::MyInterface>),
	pub fn invoke(&self, arg: Arc<crate::MyInterface>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassinterface_new, Eg_,
	nativekt_natives_testrs_testrs__callbackpassinterface_id, Eh_,
	nativekt_natives_testrs_testrs__callbackpassinterface_free, Ei_
);
impl_callback!(
    CallbackPassInterfaceArray,
	external_fn_type!(bool; isize, *mut Vec<Arc<crate::MyInterface>>),
	pub fn invoke(&self, arg: Vec<Arc<crate::MyInterface>>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassinterfacearray_new, Ej_,
	nativekt_natives_testrs_testrs__callbackpassinterfacearray_id, Ek_,
	nativekt_natives_testrs_testrs__callbackpassinterfacearray_free, El_
);
impl_callback!(
    CallbackPassInterfaceArrayN,
	external_fn_type!(bool; isize, *mut Vec<Option<Arc<crate::MyInterface>>>),
	pub fn invoke(&self, arg: Vec<Option<Arc<crate::MyInterface>>>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassinterfacearrayn_new, Em_,
	nativekt_natives_testrs_testrs__callbackpassinterfacearrayn_id, En_,
	nativekt_natives_testrs_testrs__callbackpassinterfacearrayn_free, Eo_
);
impl_callback!(
    CallbackPassInterfaceN,
	external_fn_type!(bool; isize, *mut Arc<crate::MyInterface>),
	pub fn invoke(&self, arg: Option<Arc<crate::MyInterface>>) -> bool { external_fn_call!(bool; self.invoke; self.id, obj_opt(arg, into_raw)) },
	nativekt_natives_testrs_testrs__callbackpassinterfacen_new, Ep_,
	nativekt_natives_testrs_testrs__callbackpassinterfacen_id, Eq_,
	nativekt_natives_testrs_testrs__callbackpassinterfacen_free, Er_
);
impl_callback!(
    CallbackPassLong,
	external_fn_type!(bool; isize, i64),
	pub fn invoke(&self, arg: i64) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpasslong_new, Es_,
	nativekt_natives_testrs_testrs__callbackpasslong_id, Et_,
	nativekt_natives_testrs_testrs__callbackpasslong_free, Eu_
);
impl_callback!(
    CallbackPassLongArray,
	external_fn_type!(bool; isize, *mut Vec<i64>),
	pub fn invoke(&self, arg: Vec<i64>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpasslongarray_new, Ev_,
	nativekt_natives_testrs_testrs__callbackpasslongarray_id, Ew_,
	nativekt_natives_testrs_testrs__callbackpasslongarray_free, Ex_
);
impl_callback!(
    CallbackPassShort,
	external_fn_type!(bool; isize, i16),
	pub fn invoke(&self, arg: i16) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassshort_new, Ey_,
	nativekt_natives_testrs_testrs__callbackpassshort_id, Ez_,
	nativekt_natives_testrs_testrs__callbackpassshort_free, FA_
);
impl_callback!(
    CallbackPassShortArray,
	external_fn_type!(bool; isize, *mut Vec<i16>),
	pub fn invoke(&self, arg: Vec<i16>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassshortarray_new, FB_,
	nativekt_natives_testrs_testrs__callbackpassshortarray_id, FC_,
	nativekt_natives_testrs_testrs__callbackpassshortarray_free, FD_
);
impl_callback!(
    CallbackPassString,
	external_fn_type!(bool; isize, *mut String),
	pub fn invoke(&self, arg: String) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassstring_new, FE_,
	nativekt_natives_testrs_testrs__callbackpassstring_id, FF_,
	nativekt_natives_testrs_testrs__callbackpassstring_free, FG_
);
impl_callback!(
    CallbackPassStringArray,
	external_fn_type!(bool; isize, *mut Vec<String>),
	pub fn invoke(&self, arg: Vec<String>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassstringarray_new, FH_,
	nativekt_natives_testrs_testrs__callbackpassstringarray_id, FI_,
	nativekt_natives_testrs_testrs__callbackpassstringarray_free, FJ_
);
impl_callback!(
    CallbackPassStringArrayN,
	external_fn_type!(bool; isize, *mut Vec<Option<String>>),
	pub fn invoke(&self, arg: Vec<Option<String>>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassstringarrayn_new, FK_,
	nativekt_natives_testrs_testrs__callbackpassstringarrayn_id, FL_,
	nativekt_natives_testrs_testrs__callbackpassstringarrayn_free, FM_
);
impl_callback!(
    CallbackPassStringEmpty,
	external_fn_type!(bool; isize, *mut String),
	pub fn invoke(&self, arg: String) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassstringempty_new, FN_,
	nativekt_natives_testrs_testrs__callbackpassstringempty_id, FO_,
	nativekt_natives_testrs_testrs__callbackpassstringempty_free, FP_
);
impl_callback!(
    CallbackPassStringN,
	external_fn_type!(bool; isize, *mut String),
	pub fn invoke(&self, arg: Option<String>) -> bool { external_fn_call!(bool; self.invoke; self.id, obj_opt(arg, into_raw)) },
	nativekt_natives_testrs_testrs__callbackpassstringn_new, FQ_,
	nativekt_natives_testrs_testrs__callbackpassstringn_id, FR_,
	nativekt_natives_testrs_testrs__callbackpassstringn_free, FS_
);
impl_callback!(
    CallbackPassUByte,
	external_fn_type!(bool; isize, u8),
	pub fn invoke(&self, arg: u8) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassubyte_new, FT_,
	nativekt_natives_testrs_testrs__callbackpassubyte_id, FU_,
	nativekt_natives_testrs_testrs__callbackpassubyte_free, FV_
);
impl_callback!(
    CallbackPassUByteArray,
	external_fn_type!(bool; isize, *mut Vec<u8>),
	pub fn invoke(&self, arg: Vec<u8>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassubytearray_new, FW_,
	nativekt_natives_testrs_testrs__callbackpassubytearray_id, FX_,
	nativekt_natives_testrs_testrs__callbackpassubytearray_free, FY_
);
impl_callback!(
    CallbackPassUInt,
	external_fn_type!(bool; isize, u32),
	pub fn invoke(&self, arg: u32) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassuint_new, FZ_,
	nativekt_natives_testrs_testrs__callbackpassuint_id, Fa_,
	nativekt_natives_testrs_testrs__callbackpassuint_free, Fb_
);
impl_callback!(
    CallbackPassUIntArray,
	external_fn_type!(bool; isize, *mut Vec<u32>),
	pub fn invoke(&self, arg: Vec<u32>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassuintarray_new, Fc_,
	nativekt_natives_testrs_testrs__callbackpassuintarray_id, Fd_,
	nativekt_natives_testrs_testrs__callbackpassuintarray_free, Fe_
);
impl_callback!(
    CallbackPassULong,
	external_fn_type!(bool; isize, u64),
	pub fn invoke(&self, arg: u64) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassulong_new, Ff_,
	nativekt_natives_testrs_testrs__callbackpassulong_id, Fg_,
	nativekt_natives_testrs_testrs__callbackpassulong_free, Fh_
);
impl_callback!(
    CallbackPassULongArray,
	external_fn_type!(bool; isize, *mut Vec<u64>),
	pub fn invoke(&self, arg: Vec<u64>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassulongarray_new, Fi_,
	nativekt_natives_testrs_testrs__callbackpassulongarray_id, Fj_,
	nativekt_natives_testrs_testrs__callbackpassulongarray_free, Fk_
);
impl_callback!(
    CallbackPassUShort,
	external_fn_type!(bool; isize, u16),
	pub fn invoke(&self, arg: u16) -> bool { external_fn_call!(bool; self.invoke; self.id, arg) },
	nativekt_natives_testrs_testrs__callbackpassushort_new, Fl_,
	nativekt_natives_testrs_testrs__callbackpassushort_id, Fm_,
	nativekt_natives_testrs_testrs__callbackpassushort_free, Fn_
);
impl_callback!(
    CallbackPassUShortArray,
	external_fn_type!(bool; isize, *mut Vec<u16>),
	pub fn invoke(&self, arg: Vec<u16>) -> bool { external_fn_call!(bool; self.invoke; self.id, into_raw(arg)) },
	nativekt_natives_testrs_testrs__callbackpassushortarray_new, Fo_,
	nativekt_natives_testrs_testrs__callbackpassushortarray_id, Fp_,
	nativekt_natives_testrs_testrs__callbackpassushortarray_free, Fq_
);
impl_callback!(
    CallbackReturnBoolean,
	external_fn_type!(bool; isize),
	pub fn invoke(&self) -> bool { external_fn_call!(bool; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnboolean_new, Fr_,
	nativekt_natives_testrs_testrs__callbackreturnboolean_id, Fs_,
	nativekt_natives_testrs_testrs__callbackreturnboolean_free, Ft_
);
impl_callback!(
    CallbackReturnBooleanArray,
	external_fn_type!(*mut Vec<bool>; isize),
	pub fn invoke(&self) -> Vec<bool> { from_raw(external_fn_call!(*mut Vec<bool>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnbooleanarray_new, Fu_,
	nativekt_natives_testrs_testrs__callbackreturnbooleanarray_id, Fv_,
	nativekt_natives_testrs_testrs__callbackreturnbooleanarray_free, Fw_
);
impl_callback!(
    CallbackReturnByte,
	external_fn_type!(i8; isize),
	pub fn invoke(&self) -> i8 { external_fn_call!(i8; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnbyte_new, Fx_,
	nativekt_natives_testrs_testrs__callbackreturnbyte_id, Fy_,
	nativekt_natives_testrs_testrs__callbackreturnbyte_free, Fz_
);
impl_callback!(
    CallbackReturnByteArray,
	external_fn_type!(*mut Vec<i8>; isize),
	pub fn invoke(&self) -> Vec<i8> { from_raw(external_fn_call!(*mut Vec<i8>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnbytearray_new, GA_,
	nativekt_natives_testrs_testrs__callbackreturnbytearray_id, GB_,
	nativekt_natives_testrs_testrs__callbackreturnbytearray_free, GC_
);
impl_callback!(
    CallbackReturnCallback,
	external_fn_type!(*mut Arc<VoidCallback>; isize),
	pub fn invoke(&self) -> Arc<VoidCallback> { from_raw(external_fn_call!(*mut Arc<VoidCallback>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturncallback_new, GD_,
	nativekt_natives_testrs_testrs__callbackreturncallback_id, GE_,
	nativekt_natives_testrs_testrs__callbackreturncallback_free, GF_
);
impl_callback!(
    CallbackReturnCallbackN,
	external_fn_type!(*mut Arc<VoidCallback>; isize),
	pub fn invoke(&self) -> Option<Arc<VoidCallback>> { ptr_opt(external_fn_call!(*mut Arc<VoidCallback>; self.invoke; self.id), from_raw) },
	nativekt_natives_testrs_testrs__callbackreturncallbackn_new, GG_,
	nativekt_natives_testrs_testrs__callbackreturncallbackn_id, GH_,
	nativekt_natives_testrs_testrs__callbackreturncallbackn_free, GI_
);
impl_callback!(
    CallbackReturnChar,
	external_fn_type!(u16; isize),
	pub fn invoke(&self) -> u16 { external_fn_call!(u16; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnchar_new, GJ_,
	nativekt_natives_testrs_testrs__callbackreturnchar_id, GK_,
	nativekt_natives_testrs_testrs__callbackreturnchar_free, GL_
);
impl_callback!(
    CallbackReturnCharArray,
	external_fn_type!(*mut Vec<u16>; isize),
	pub fn invoke(&self) -> Vec<u16> { from_raw(external_fn_call!(*mut Vec<u16>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnchararray_new, GM_,
	nativekt_natives_testrs_testrs__callbackreturnchararray_id, GN_,
	nativekt_natives_testrs_testrs__callbackreturnchararray_free, GO_
);
impl_callback!(
    CallbackReturnCharArrayEmpty,
	external_fn_type!(*mut Vec<u16>; isize),
	pub fn invoke(&self) -> Vec<u16> { from_raw(external_fn_call!(*mut Vec<u16>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnchararrayempty_new, GP_,
	nativekt_natives_testrs_testrs__callbackreturnchararrayempty_id, GQ_,
	nativekt_natives_testrs_testrs__callbackreturnchararrayempty_free, GR_
);
impl_callback!(
    CallbackReturnCharArrayN,
	external_fn_type!(*mut Vec<u16>; isize),
	pub fn invoke(&self) -> Option<Vec<u16>> { ptr_opt(external_fn_call!(*mut Vec<u16>; self.invoke; self.id), from_raw) },
	nativekt_natives_testrs_testrs__callbackreturnchararrayn_new, GS_,
	nativekt_natives_testrs_testrs__callbackreturnchararrayn_id, GT_,
	nativekt_natives_testrs_testrs__callbackreturnchararrayn_free, GU_
);
impl_callback!(
    CallbackReturnDictionary,
	external_fn_type!(*mut MyDictionary; isize),
	pub fn invoke(&self) -> MyDictionary { from_raw(external_fn_call!(*mut MyDictionary; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturndictionary_new, GV_,
	nativekt_natives_testrs_testrs__callbackreturndictionary_id, GW_,
	nativekt_natives_testrs_testrs__callbackreturndictionary_free, GX_
);
impl_callback!(
    CallbackReturnDictionaryArray,
	external_fn_type!(*mut Vec<MyDictionary>; isize),
	pub fn invoke(&self) -> Vec<MyDictionary> { from_raw(external_fn_call!(*mut Vec<MyDictionary>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturndictionaryarray_new, GY_,
	nativekt_natives_testrs_testrs__callbackreturndictionaryarray_id, GZ_,
	nativekt_natives_testrs_testrs__callbackreturndictionaryarray_free, Ga_
);
impl_callback!(
    CallbackReturnDictionaryArrayN,
	external_fn_type!(*mut Vec<Option<MyDictionary>>; isize),
	pub fn invoke(&self) -> Vec<Option<MyDictionary>> { from_raw(external_fn_call!(*mut Vec<Option<MyDictionary>>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturndictionaryarrayn_new, Gb_,
	nativekt_natives_testrs_testrs__callbackreturndictionaryarrayn_id, Gc_,
	nativekt_natives_testrs_testrs__callbackreturndictionaryarrayn_free, Gd_
);
impl_callback!(
    CallbackReturnDictionaryN,
	external_fn_type!(*mut MyDictionary; isize),
	pub fn invoke(&self) -> Option<MyDictionary> { ptr_opt(external_fn_call!(*mut MyDictionary; self.invoke; self.id), from_raw) },
	nativekt_natives_testrs_testrs__callbackreturndictionaryn_new, Ge_,
	nativekt_natives_testrs_testrs__callbackreturndictionaryn_id, Gf_,
	nativekt_natives_testrs_testrs__callbackreturndictionaryn_free, Gg_
);
impl_callback!(
    CallbackReturnDouble,
	external_fn_type!(f64; isize),
	pub fn invoke(&self) -> f64 { external_fn_call!(f64; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturndouble_new, Gh_,
	nativekt_natives_testrs_testrs__callbackreturndouble_id, Gi_,
	nativekt_natives_testrs_testrs__callbackreturndouble_free, Gj_
);
impl_callback!(
    CallbackReturnDoubleArray,
	external_fn_type!(*mut Vec<f64>; isize),
	pub fn invoke(&self) -> Vec<f64> { from_raw(external_fn_call!(*mut Vec<f64>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturndoublearray_new, Gk_,
	nativekt_natives_testrs_testrs__callbackreturndoublearray_id, Gl_,
	nativekt_natives_testrs_testrs__callbackreturndoublearray_free, Gm_
);
impl_callback!(
    CallbackReturnEnum,
	external_fn_type!(MyEnum; isize),
	pub fn invoke(&self) -> MyEnum { external_fn_call!(enum MyEnum; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnenum_new, Gn_,
	nativekt_natives_testrs_testrs__callbackreturnenum_id, Go_,
	nativekt_natives_testrs_testrs__callbackreturnenum_free, Gp_
);
impl_callback!(
    CallbackReturnEnumArray,
	external_fn_type!(*mut Vec<MyEnum>; isize),
	pub fn invoke(&self) -> Vec<MyEnum> { from_raw(external_fn_call!(*mut Vec<MyEnum>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnenumarray_new, Gq_,
	nativekt_natives_testrs_testrs__callbackreturnenumarray_id, Gr_,
	nativekt_natives_testrs_testrs__callbackreturnenumarray_free, Gs_
);
impl_callback!(
    CallbackReturnFloat,
	external_fn_type!(f32; isize),
	pub fn invoke(&self) -> f32 { external_fn_call!(f32; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnfloat_new, Gt_,
	nativekt_natives_testrs_testrs__callbackreturnfloat_id, Gu_,
	nativekt_natives_testrs_testrs__callbackreturnfloat_free, Gv_
);
impl_callback!(
    CallbackReturnFloatArray,
	external_fn_type!(*mut Vec<f32>; isize),
	pub fn invoke(&self) -> Vec<f32> { from_raw(external_fn_call!(*mut Vec<f32>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnfloatarray_new, Gw_,
	nativekt_natives_testrs_testrs__callbackreturnfloatarray_id, Gx_,
	nativekt_natives_testrs_testrs__callbackreturnfloatarray_free, Gy_
);
impl_callback!(
    CallbackReturnInt,
	external_fn_type!(i32; isize),
	pub fn invoke(&self) -> i32 { external_fn_call!(i32; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnint_new, Gz_,
	nativekt_natives_testrs_testrs__callbackreturnint_id, HA_,
	nativekt_natives_testrs_testrs__callbackreturnint_free, HB_
);
impl_callback!(
    CallbackReturnIntArray,
	external_fn_type!(*mut Vec<i32>; isize),
	pub fn invoke(&self) -> Vec<i32> { from_raw(external_fn_call!(*mut Vec<i32>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnintarray_new, HC_,
	nativekt_natives_testrs_testrs__callbackreturnintarray_id, HD_,
	nativekt_natives_testrs_testrs__callbackreturnintarray_free, HE_
);
impl_callback!(
    CallbackReturnInterface,
	external_fn_type!(*mut Arc<crate::MyInterface>; isize),
	pub fn invoke(&self) -> Arc<crate::MyInterface> { from_raw(external_fn_call!(*mut Arc<crate::MyInterface>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturninterface_new, HF_,
	nativekt_natives_testrs_testrs__callbackreturninterface_id, HG_,
	nativekt_natives_testrs_testrs__callbackreturninterface_free, HH_
);
impl_callback!(
    CallbackReturnInterfaceArray,
	external_fn_type!(*mut Vec<Arc<crate::MyInterface>>; isize),
	pub fn invoke(&self) -> Vec<Arc<crate::MyInterface>> { from_raw(external_fn_call!(*mut Vec<Arc<crate::MyInterface>>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturninterfacearray_new, HI_,
	nativekt_natives_testrs_testrs__callbackreturninterfacearray_id, HJ_,
	nativekt_natives_testrs_testrs__callbackreturninterfacearray_free, HK_
);
impl_callback!(
    CallbackReturnInterfaceArrayN,
	external_fn_type!(*mut Vec<Option<Arc<crate::MyInterface>>>; isize),
	pub fn invoke(&self) -> Vec<Option<Arc<crate::MyInterface>>> { from_raw(external_fn_call!(*mut Vec<Option<Arc<crate::MyInterface>>>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturninterfacearrayn_new, HL_,
	nativekt_natives_testrs_testrs__callbackreturninterfacearrayn_id, HM_,
	nativekt_natives_testrs_testrs__callbackreturninterfacearrayn_free, HN_
);
impl_callback!(
    CallbackReturnInterfaceN,
	external_fn_type!(*mut Arc<crate::MyInterface>; isize),
	pub fn invoke(&self) -> Option<Arc<crate::MyInterface>> { ptr_opt(external_fn_call!(*mut Arc<crate::MyInterface>; self.invoke; self.id), from_raw) },
	nativekt_natives_testrs_testrs__callbackreturninterfacen_new, HO_,
	nativekt_natives_testrs_testrs__callbackreturninterfacen_id, HP_,
	nativekt_natives_testrs_testrs__callbackreturninterfacen_free, HQ_
);
impl_callback!(
    CallbackReturnLong,
	external_fn_type!(i64; isize),
	pub fn invoke(&self) -> i64 { external_fn_call!(i64; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnlong_new, HR_,
	nativekt_natives_testrs_testrs__callbackreturnlong_id, HS_,
	nativekt_natives_testrs_testrs__callbackreturnlong_free, HT_
);
impl_callback!(
    CallbackReturnLongArray,
	external_fn_type!(*mut Vec<i64>; isize),
	pub fn invoke(&self) -> Vec<i64> { from_raw(external_fn_call!(*mut Vec<i64>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnlongarray_new, HU_,
	nativekt_natives_testrs_testrs__callbackreturnlongarray_id, HV_,
	nativekt_natives_testrs_testrs__callbackreturnlongarray_free, HW_
);
impl_callback!(
    CallbackReturnShort,
	external_fn_type!(i16; isize),
	pub fn invoke(&self) -> i16 { external_fn_call!(i16; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnshort_new, HX_,
	nativekt_natives_testrs_testrs__callbackreturnshort_id, HY_,
	nativekt_natives_testrs_testrs__callbackreturnshort_free, HZ_
);
impl_callback!(
    CallbackReturnShortArray,
	external_fn_type!(*mut Vec<i16>; isize),
	pub fn invoke(&self) -> Vec<i16> { from_raw(external_fn_call!(*mut Vec<i16>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnshortarray_new, Ha_,
	nativekt_natives_testrs_testrs__callbackreturnshortarray_id, Hb_,
	nativekt_natives_testrs_testrs__callbackreturnshortarray_free, Hc_
);
impl_callback!(
    CallbackReturnString,
	external_fn_type!(*mut String; isize),
	pub fn invoke(&self) -> String { from_raw(external_fn_call!(*mut String; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnstring_new, Hd_,
	nativekt_natives_testrs_testrs__callbackreturnstring_id, He_,
	nativekt_natives_testrs_testrs__callbackreturnstring_free, Hf_
);
impl_callback!(
    CallbackReturnStringArray,
	external_fn_type!(*mut Vec<String>; isize),
	pub fn invoke(&self) -> Vec<String> { from_raw(external_fn_call!(*mut Vec<String>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnstringarray_new, Hg_,
	nativekt_natives_testrs_testrs__callbackreturnstringarray_id, Hh_,
	nativekt_natives_testrs_testrs__callbackreturnstringarray_free, Hi_
);
impl_callback!(
    CallbackReturnStringArrayN,
	external_fn_type!(*mut Vec<Option<String>>; isize),
	pub fn invoke(&self) -> Vec<Option<String>> { from_raw(external_fn_call!(*mut Vec<Option<String>>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnstringarrayn_new, Hj_,
	nativekt_natives_testrs_testrs__callbackreturnstringarrayn_id, Hk_,
	nativekt_natives_testrs_testrs__callbackreturnstringarrayn_free, Hl_
);
impl_callback!(
    CallbackReturnStringEmpty,
	external_fn_type!(*mut String; isize),
	pub fn invoke(&self) -> String { from_raw(external_fn_call!(*mut String; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnstringempty_new, Hm_,
	nativekt_natives_testrs_testrs__callbackreturnstringempty_id, Hn_,
	nativekt_natives_testrs_testrs__callbackreturnstringempty_free, Ho_
);
impl_callback!(
    CallbackReturnStringN,
	external_fn_type!(*mut String; isize),
	pub fn invoke(&self) -> Option<String> { ptr_opt(external_fn_call!(*mut String; self.invoke; self.id), from_raw) },
	nativekt_natives_testrs_testrs__callbackreturnstringn_new, Hp_,
	nativekt_natives_testrs_testrs__callbackreturnstringn_id, Hq_,
	nativekt_natives_testrs_testrs__callbackreturnstringn_free, Hr_
);
impl_callback!(
    CallbackReturnUByte,
	external_fn_type!(u8; isize),
	pub fn invoke(&self) -> u8 { external_fn_call!(u8; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnubyte_new, Hs_,
	nativekt_natives_testrs_testrs__callbackreturnubyte_id, Ht_,
	nativekt_natives_testrs_testrs__callbackreturnubyte_free, Hu_
);
impl_callback!(
    CallbackReturnUByteArray,
	external_fn_type!(*mut Vec<u8>; isize),
	pub fn invoke(&self) -> Vec<u8> { from_raw(external_fn_call!(*mut Vec<u8>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnubytearray_new, Hv_,
	nativekt_natives_testrs_testrs__callbackreturnubytearray_id, Hw_,
	nativekt_natives_testrs_testrs__callbackreturnubytearray_free, Hx_
);
impl_callback!(
    CallbackReturnUInt,
	external_fn_type!(u32; isize),
	pub fn invoke(&self) -> u32 { external_fn_call!(u32; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnuint_new, Hy_,
	nativekt_natives_testrs_testrs__callbackreturnuint_id, Hz_,
	nativekt_natives_testrs_testrs__callbackreturnuint_free, IA_
);
impl_callback!(
    CallbackReturnUIntArray,
	external_fn_type!(*mut Vec<u32>; isize),
	pub fn invoke(&self) -> Vec<u32> { from_raw(external_fn_call!(*mut Vec<u32>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnuintarray_new, IB_,
	nativekt_natives_testrs_testrs__callbackreturnuintarray_id, IC_,
	nativekt_natives_testrs_testrs__callbackreturnuintarray_free, ID_
);
impl_callback!(
    CallbackReturnULong,
	external_fn_type!(u64; isize),
	pub fn invoke(&self) -> u64 { external_fn_call!(u64; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnulong_new, IE_,
	nativekt_natives_testrs_testrs__callbackreturnulong_id, IF_,
	nativekt_natives_testrs_testrs__callbackreturnulong_free, IG_
);
impl_callback!(
    CallbackReturnULongArray,
	external_fn_type!(*mut Vec<u64>; isize),
	pub fn invoke(&self) -> Vec<u64> { from_raw(external_fn_call!(*mut Vec<u64>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnulongarray_new, IH_,
	nativekt_natives_testrs_testrs__callbackreturnulongarray_id, II_,
	nativekt_natives_testrs_testrs__callbackreturnulongarray_free, IJ_
);
impl_callback!(
    CallbackReturnUShort,
	external_fn_type!(u16; isize),
	pub fn invoke(&self) -> u16 { external_fn_call!(u16; self.invoke; self.id) },
	nativekt_natives_testrs_testrs__callbackreturnushort_new, IK_,
	nativekt_natives_testrs_testrs__callbackreturnushort_id, IL_,
	nativekt_natives_testrs_testrs__callbackreturnushort_free, IM_
);
impl_callback!(
    CallbackReturnUShortArray,
	external_fn_type!(*mut Vec<u16>; isize),
	pub fn invoke(&self) -> Vec<u16> { from_raw(external_fn_call!(*mut Vec<u16>; self.invoke; self.id)) },
	nativekt_natives_testrs_testrs__callbackreturnushortarray_new, IN_,
	nativekt_natives_testrs_testrs__callbackreturnushortarray_id, IO_,
	nativekt_natives_testrs_testrs__callbackreturnushortarray_free, IP_
);
impl_callback!(
    VoidCallback,
	external_fn_type!((); isize),
	pub fn invoke(&self) { external_fn_call!((); self.invoke; self.id) },
	nativekt_natives_testrs_testrs__voidcallback_new, IQ_,
	nativekt_natives_testrs_testrs__voidcallback_id, IR_,
	nativekt_natives_testrs_testrs__voidcallback_free, IS_
);

// ╔═══════════════╗
// ║     Enums     ║
// ╚═══════════════╝

#[cfg_attr(target_arch = "wasm32", wasm_bindgen)]
#[repr(i32)]
#[derive(PartialEq, Eq, Clone, Copy, Debug)]
pub enum MyEnum {
	CASE1 = 0,
	CASE2 = 1,	   
	   
	   #[doc(hidden)] __MinNiche = -2147483648, 
	#[doc(hidden)] __MaxNiche = 2147483647
}

// ╔═══════════════════╗
// ║     Functions     ║
// ╚═══════════════════╝

export_fn!{ fn nativekt_natives_testrs_testrs_pass_void() -> bool as IT_ {
    crate::pass_void()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_char(arg: u16) -> bool as IU_ {
    crate::pass_char(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_char_n(arg: *mut u16) -> bool as IV_ {
    crate::pass_char_n(ptr_opt(arg, from_raw_primitive))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_boolean(arg: bool) -> bool as IW_ {
    crate::pass_boolean(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_boolean_n(arg: *mut bool) -> bool as IX_ {
    crate::pass_boolean_n(ptr_opt(arg, from_raw_primitive))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_byte(arg: i8) -> bool as IY_ {
    crate::pass_byte(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_byte_n(arg: *mut i8) -> bool as IZ_ {
    crate::pass_byte_n(ptr_opt(arg, from_raw_primitive))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ubyte(arg: u8) -> bool as Ia_ {
    crate::pass_ubyte(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ubyte_n(arg: *mut u8) -> bool as Ib_ {
    crate::pass_ubyte_n(ptr_opt(arg, from_raw_primitive))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_short(arg: i16) -> bool as Ic_ {
    crate::pass_short(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_short_n(arg: *mut i16) -> bool as Id_ {
    crate::pass_short_n(ptr_opt(arg, from_raw_primitive))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ushort(arg: u16) -> bool as Ie_ {
    crate::pass_ushort(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ushort_n(arg: *mut u16) -> bool as If_ {
    crate::pass_ushort_n(ptr_opt(arg, from_raw_primitive))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_int(arg: i32) -> bool as Ig_ {
    crate::pass_int(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_int_n(arg: *mut i32) -> bool as Ih_ {
    crate::pass_int_n(ptr_opt(arg, from_raw_primitive))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_uint(arg: u32) -> bool as Ii_ {
    crate::pass_uint(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_uint_n(arg: *mut u32) -> bool as Ij_ {
    crate::pass_uint_n(ptr_opt(arg, from_raw_primitive))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_long(arg: i64) -> bool as Ik_ {
    crate::pass_long(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_long_n(arg: *mut i64) -> bool as Il_ {
    crate::pass_long_n(ptr_opt(arg, from_raw_primitive))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ulong(arg: u64) -> bool as Im_ {
    crate::pass_ulong(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ulong_n(arg: *mut u64) -> bool as In_ {
    crate::pass_ulong_n(ptr_opt(arg, from_raw_primitive))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_float(arg: f32) -> bool as Io_ {
    crate::pass_float(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_float_n(arg: *mut f32) -> bool as Ip_ {
    crate::pass_float_n(ptr_opt(arg, from_raw_primitive))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_double(arg: f64) -> bool as Iq_ {
    crate::pass_double(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_double_n(arg: *mut f64) -> bool as Ir_ {
    crate::pass_double_n(ptr_opt(arg, from_raw_primitive))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_string(arg: *mut String) -> bool as Is_ {
    crate::pass_string(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_string_empty(arg: *mut String) -> bool as It_ {
    crate::pass_string_empty(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_string_n(arg: *mut String) -> bool as Iu_ {
    crate::pass_string_n(ptr_opt(arg, from_raw))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_enum(arg: MyEnum) -> bool as Iv_ {
    crate::pass_enum(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_enum_n(arg: *mut MyEnum) -> bool as Iw_ {
    crate::pass_enum_n(ptr_opt(arg, from_raw_primitive))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_dictionary(arg: *mut MyDictionary) -> bool as Ix_ {
    crate::pass_dictionary(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_dictionary_n(arg: *mut MyDictionary) -> bool as Iy_ {
    crate::pass_dictionary_n(ptr_opt(arg, from_raw))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_interface(arg: *mut Arc<crate::MyInterface>) -> bool as Iz_ {
    crate::pass_interface(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_interface_n(arg: *mut Arc<crate::MyInterface>) -> bool as JA_ {
    crate::pass_interface_n(ptr_opt(arg, from_raw))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_void() -> () as JB_ {
    crate::return_void()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_char() -> u16 as JC_ {
    crate::return_char()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_char_n() -> *mut u16 as JD_ {
    obj_opt(crate::return_char_n(), into_raw_primitive)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_boolean() -> bool as JE_ {
    crate::return_boolean()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_boolean_n() -> *mut bool as JF_ {
    obj_opt(crate::return_boolean_n(), into_raw_primitive)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_byte() -> i8 as JG_ {
    crate::return_byte()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_byte_n() -> *mut i8 as JH_ {
    obj_opt(crate::return_byte_n(), into_raw_primitive)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ubyte() -> u8 as JI_ {
    crate::return_ubyte()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ubyte_n() -> *mut u8 as JJ_ {
    obj_opt(crate::return_ubyte_n(), into_raw_primitive)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_short() -> i16 as JK_ {
    crate::return_short()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_short_n() -> *mut i16 as JL_ {
    obj_opt(crate::return_short_n(), into_raw_primitive)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ushort() -> u16 as JM_ {
    crate::return_ushort()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ushort_n() -> *mut u16 as JN_ {
    obj_opt(crate::return_ushort_n(), into_raw_primitive)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_int() -> i32 as JO_ {
    crate::return_int()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_int_n() -> *mut i32 as JP_ {
    obj_opt(crate::return_int_n(), into_raw_primitive)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_uint() -> u32 as JQ_ {
    crate::return_uint()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_uint_n() -> *mut u32 as JR_ {
    obj_opt(crate::return_uint_n(), into_raw_primitive)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_long() -> i64 as JS_ {
    crate::return_long()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_long_n() -> *mut i64 as JT_ {
    obj_opt(crate::return_long_n(), into_raw_primitive)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ulong() -> u64 as JU_ {
    crate::return_ulong()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ulong_n() -> *mut u64 as JV_ {
    obj_opt(crate::return_ulong_n(), into_raw_primitive)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_float() -> f32 as JW_ {
    crate::return_float()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_float_n() -> *mut f32 as JX_ {
    obj_opt(crate::return_float_n(), into_raw_primitive)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_double() -> f64 as JY_ {
    crate::return_double()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_double_n() -> *mut f64 as JZ_ {
    obj_opt(crate::return_double_n(), into_raw_primitive)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_string() -> *mut String as Ja_ {
    into_raw(crate::return_string())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_string_empty() -> *mut String as Jb_ {
    into_raw(crate::return_string_empty())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_string_n() -> *mut String as Jc_ {
    obj_opt(crate::return_string_n(), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_enum() -> MyEnum as Jd_ {
    crate::return_enum()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_enum_n() -> *mut MyEnum as Je_ {
    obj_opt(crate::return_enum_n(), into_raw_primitive)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_dictionary() -> *mut MyDictionary as Jf_ {
    into_raw(crate::return_dictionary())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_dictionary_n() -> *mut MyDictionary as Jg_ {
    obj_opt(crate::return_dictionary_n(), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_char(arg: u16) -> u16 as Jh_ {
    crate::ping_char(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_boolean(arg: bool) -> bool as Ji_ {
    crate::ping_boolean(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_byte(arg: i8) -> i8 as Jj_ {
    crate::ping_byte(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_ubyte(arg: u8) -> u8 as Jk_ {
    crate::ping_ubyte(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_short(arg: i16) -> i16 as Jl_ {
    crate::ping_short(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_ushort(arg: u16) -> u16 as Jm_ {
    crate::ping_ushort(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_int(arg: i32) -> i32 as Jn_ {
    crate::ping_int(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_uint(arg: u32) -> u32 as Jo_ {
    crate::ping_uint(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_long(arg: i64) -> i64 as Jp_ {
    crate::ping_long(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_ulong(arg: u64) -> u64 as Jq_ {
    crate::ping_ulong(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_float(arg: f32) -> f32 as Jr_ {
    crate::ping_float(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_double(arg: f64) -> f64 as Js_ {
    crate::ping_double(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_string(arg: *mut String) -> *mut String as Jt_ {
    into_raw(crate::ping_string(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_string_empty(arg: *mut String) -> *mut String as Ju_ {
    into_raw(crate::ping_string_empty(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_string_n(arg: *mut String) -> *mut String as Jv_ {
    obj_opt(crate::ping_string_n(ptr_opt(arg, from_raw)), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_enum(arg: MyEnum) -> MyEnum as Jw_ {
    crate::ping_enum(arg)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_dictionary(arg: *mut MyDictionary) -> *mut MyDictionary as Jx_ {
    into_raw(crate::ping_dictionary(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_empty_dictionary(arg: *mut EmptyDictionary) -> *mut EmptyDictionary as Jy_ {
    into_raw(crate::ping_empty_dictionary(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_dictionary_n(arg: *mut MyDictionary) -> *mut MyDictionary as Jz_ {
    obj_opt(crate::ping_dictionary_n(ptr_opt(arg, from_raw)), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_interface(arg: *mut Arc<crate::MyInterface>) -> *mut Arc<crate::MyInterface> as KA_ {
    into_raw(crate::ping_interface(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_interface_n(arg: *mut Arc<crate::MyInterface>) -> *mut Arc<crate::MyInterface> as KB_ {
    obj_opt(crate::ping_interface_n(ptr_opt(arg, from_raw)), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_void(arg: *mut Arc<VoidCallback>) -> () as KC_ {
    crate::callback_void(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_void_n(arg: *mut Arc<VoidCallback>) -> bool as KD_ {
    crate::callback_void_n(ptr_opt(arg, from_raw))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_char(arg: *mut Arc<CallbackPassChar>) -> bool as KE_ {
    crate::callback_arg_char(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_boolean(arg: *mut Arc<CallbackPassBoolean>) -> bool as KF_ {
    crate::callback_arg_boolean(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_byte(arg: *mut Arc<CallbackPassByte>) -> bool as KG_ {
    crate::callback_arg_byte(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_ubyte(arg: *mut Arc<CallbackPassUByte>) -> bool as KH_ {
    crate::callback_arg_ubyte(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_short(arg: *mut Arc<CallbackPassShort>) -> bool as KI_ {
    crate::callback_arg_short(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_ushort(arg: *mut Arc<CallbackPassUShort>) -> bool as KJ_ {
    crate::callback_arg_ushort(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_int(arg: *mut Arc<CallbackPassInt>) -> bool as KK_ {
    crate::callback_arg_int(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_uint(arg: *mut Arc<CallbackPassUInt>) -> bool as KL_ {
    crate::callback_arg_uint(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_long(arg: *mut Arc<CallbackPassLong>) -> bool as KM_ {
    crate::callback_arg_long(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_ulong(arg: *mut Arc<CallbackPassULong>) -> bool as KN_ {
    crate::callback_arg_ulong(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_float(arg: *mut Arc<CallbackPassFloat>) -> bool as KO_ {
    crate::callback_arg_float(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_double(arg: *mut Arc<CallbackPassDouble>) -> bool as KP_ {
    crate::callback_arg_double(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_string(arg: *mut Arc<CallbackPassString>) -> bool as KQ_ {
    crate::callback_arg_string(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_string_empty(arg: *mut Arc<CallbackPassStringEmpty>) -> bool as KR_ {
    crate::callback_arg_string_empty(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_string_n(arg: *mut Arc<CallbackPassStringN>) -> bool as KS_ {
    crate::callback_arg_string_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_callback(pass: *mut Arc<VoidCallback>, arg: *mut Arc<CallbackPassCallback>) -> bool as KT_ {
    crate::callback_arg_callback(from_raw(pass), from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_callback_n(arg: *mut Arc<CallbackPassCallbackN>) -> bool as KU_ {
    crate::callback_arg_callback_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_enum(arg: *mut Arc<CallbackPassEnum>) -> bool as KV_ {
    crate::callback_arg_enum(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_dictionary(arg: *mut Arc<CallbackPassDictionary>) -> bool as KW_ {
    crate::callback_arg_dictionary(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_dictionary_n(arg: *mut Arc<CallbackPassDictionaryN>) -> bool as KX_ {
    crate::callback_arg_dictionary_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_interface(pass: *mut Arc<crate::MyInterface>, arg: *mut Arc<CallbackPassInterface>) -> bool as KY_ {
    crate::callback_arg_interface(from_raw(pass), from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_interface_n(arg: *mut Arc<CallbackPassInterfaceN>) -> bool as KZ_ {
    crate::callback_arg_interface_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_char(arg: *mut Arc<CallbackReturnChar>) -> bool as Ka_ {
    crate::callback_return_char(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_boolean(arg: *mut Arc<CallbackReturnBoolean>) -> bool as Kb_ {
    crate::callback_return_boolean(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_byte(arg: *mut Arc<CallbackReturnByte>) -> bool as Kc_ {
    crate::callback_return_byte(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_ubyte(arg: *mut Arc<CallbackReturnUByte>) -> bool as Kd_ {
    crate::callback_return_ubyte(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_short(arg: *mut Arc<CallbackReturnShort>) -> bool as Ke_ {
    crate::callback_return_short(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_ushort(arg: *mut Arc<CallbackReturnUShort>) -> bool as Kf_ {
    crate::callback_return_ushort(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_int(arg: *mut Arc<CallbackReturnInt>) -> bool as Kg_ {
    crate::callback_return_int(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_uint(arg: *mut Arc<CallbackReturnUInt>) -> bool as Kh_ {
    crate::callback_return_uint(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_long(arg: *mut Arc<CallbackReturnLong>) -> bool as Ki_ {
    crate::callback_return_long(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_ulong(arg: *mut Arc<CallbackReturnULong>) -> bool as Kj_ {
    crate::callback_return_ulong(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_float(arg: *mut Arc<CallbackReturnFloat>) -> bool as Kk_ {
    crate::callback_return_float(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_double(arg: *mut Arc<CallbackReturnDouble>) -> bool as Kl_ {
    crate::callback_return_double(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_string(arg: *mut Arc<CallbackReturnString>) -> bool as Km_ {
    crate::callback_return_string(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_string_empty(arg: *mut Arc<CallbackReturnStringEmpty>) -> bool as Kn_ {
    crate::callback_return_string_empty(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_string_n(arg: *mut Arc<CallbackReturnStringN>) -> bool as Ko_ {
    crate::callback_return_string_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_callback(arg: *mut Arc<CallbackReturnCallback>) -> *mut Arc<VoidCallback> as Kp_ {
    into_raw(crate::callback_return_callback(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_callback_n(arg: *mut Arc<CallbackReturnCallbackN>) -> bool as Kq_ {
    crate::callback_return_callback_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_enum(arg: *mut Arc<CallbackReturnEnum>) -> bool as Kr_ {
    crate::callback_return_enum(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_dictionary(arg: *mut Arc<CallbackReturnDictionary>) -> bool as Ks_ {
    crate::callback_return_dictionary(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_dictionary_n(arg: *mut Arc<CallbackReturnDictionaryN>) -> bool as Kt_ {
    crate::callback_return_dictionary_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_interface(arg: *mut Arc<CallbackReturnInterface>) -> bool as Ku_ {
    crate::callback_return_interface(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_interface_n(arg: *mut Arc<CallbackReturnInterfaceN>) -> bool as Kv_ {
    crate::callback_return_interface_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_char_array(arg: *mut Vec<u16>) -> bool as Kw_ {
    crate::pass_char_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_char_array_empty(arg: *mut Vec<u16>) -> bool as Kx_ {
    crate::pass_char_array_empty(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_char_array_n(arg: *mut Vec<u16>) -> bool as Ky_ {
    crate::pass_char_array_n(ptr_opt(arg, from_raw))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_char_narray(arg: *mut Vec<Option<u16>>) -> bool as Kz_ {
    crate::pass_char_narray(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_boolean_array(arg: *mut Vec<bool>) -> bool as LA_ {
    crate::pass_boolean_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_boolean_narray(arg: *mut Vec<Option<bool>>) -> bool as LB_ {
    crate::pass_boolean_narray(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_byte_array(arg: *mut Vec<i8>) -> bool as LC_ {
    crate::pass_byte_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_byte_narray(arg: *mut Vec<Option<i8>>) -> bool as LD_ {
    crate::pass_byte_narray(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ubyte_array(arg: *mut Vec<u8>) -> bool as LE_ {
    crate::pass_ubyte_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ubyte_narray(arg: *mut Vec<Option<u8>>) -> bool as LF_ {
    crate::pass_ubyte_narray(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_short_array(arg: *mut Vec<i16>) -> bool as LG_ {
    crate::pass_short_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_short_narray(arg: *mut Vec<Option<i16>>) -> bool as LH_ {
    crate::pass_short_narray(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ushort_array(arg: *mut Vec<u16>) -> bool as LI_ {
    crate::pass_ushort_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ushort_narray(arg: *mut Vec<Option<u16>>) -> bool as LJ_ {
    crate::pass_ushort_narray(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_int_array(arg: *mut Vec<i32>) -> bool as LK_ {
    crate::pass_int_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_int_narray(arg: *mut Vec<Option<i32>>) -> bool as LL_ {
    crate::pass_int_narray(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_uint_array(arg: *mut Vec<u32>) -> bool as LM_ {
    crate::pass_uint_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_uint_narray(arg: *mut Vec<Option<u32>>) -> bool as LN_ {
    crate::pass_uint_narray(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_long_array(arg: *mut Vec<i64>) -> bool as LO_ {
    crate::pass_long_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_long_narray(arg: *mut Vec<Option<i64>>) -> bool as LP_ {
    crate::pass_long_narray(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ulong_array(arg: *mut Vec<u64>) -> bool as LQ_ {
    crate::pass_ulong_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_ulong_narray(arg: *mut Vec<Option<u64>>) -> bool as LR_ {
    crate::pass_ulong_narray(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_float_array(arg: *mut Vec<f32>) -> bool as LS_ {
    crate::pass_float_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_float_narray(arg: *mut Vec<Option<f32>>) -> bool as LT_ {
    crate::pass_float_narray(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_double_array(arg: *mut Vec<f64>) -> bool as LU_ {
    crate::pass_double_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_double_narray(arg: *mut Vec<Option<f64>>) -> bool as LV_ {
    crate::pass_double_narray(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_string_array(arg: *mut Vec<String>) -> bool as LW_ {
    crate::pass_string_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_string_array_n(arg: *mut Vec<Option<String>>) -> bool as LX_ {
    crate::pass_string_array_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_enum_array(arg: *mut Vec<MyEnum>) -> bool as LY_ {
    crate::pass_enum_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_enum_narray(arg: *mut Vec<Option<MyEnum>>) -> bool as LZ_ {
    crate::pass_enum_narray(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_dictionary_array(arg: *mut Vec<MyDictionary>) -> bool as La_ {
    crate::pass_dictionary_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_dictionary_array_n(arg: *mut Vec<Option<MyDictionary>>) -> bool as Lb_ {
    crate::pass_dictionary_array_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_interface_array(arg: *mut Vec<Arc<crate::MyInterface>>) -> bool as Lc_ {
    crate::pass_interface_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_interface_array_n(arg: *mut Vec<Option<Arc<crate::MyInterface>>>) -> bool as Ld_ {
    crate::pass_interface_array_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_char_array() -> *mut Vec<u16> as Le_ {
    into_raw(crate::return_char_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_char_array_empty() -> *mut Vec<u16> as Lf_ {
    into_raw(crate::return_char_array_empty())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_char_array_n() -> *mut Vec<u16> as Lg_ {
    obj_opt(crate::return_char_array_n(), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_char_narray() -> *mut Vec<Option<u16>> as Lh_ {
    into_raw(crate::return_char_narray())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_boolean_array() -> *mut Vec<bool> as Li_ {
    into_raw(crate::return_boolean_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_boolean_narray() -> *mut Vec<Option<bool>> as Lj_ {
    into_raw(crate::return_boolean_narray())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_byte_array() -> *mut Vec<i8> as Lk_ {
    into_raw(crate::return_byte_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_byte_narray() -> *mut Vec<Option<i8>> as Ll_ {
    into_raw(crate::return_byte_narray())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ubyte_array() -> *mut Vec<u8> as Lm_ {
    into_raw(crate::return_ubyte_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ubyte_narray() -> *mut Vec<Option<u8>> as Ln_ {
    into_raw(crate::return_ubyte_narray())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_short_array() -> *mut Vec<i16> as Lo_ {
    into_raw(crate::return_short_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_short_narray() -> *mut Vec<Option<i16>> as Lp_ {
    into_raw(crate::return_short_narray())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ushort_array() -> *mut Vec<u16> as Lq_ {
    into_raw(crate::return_ushort_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ushort_narray() -> *mut Vec<Option<u16>> as Lr_ {
    into_raw(crate::return_ushort_narray())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_int_array() -> *mut Vec<i32> as Ls_ {
    into_raw(crate::return_int_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_int_narray() -> *mut Vec<Option<i32>> as Lt_ {
    into_raw(crate::return_int_narray())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_uint_array() -> *mut Vec<u32> as Lu_ {
    into_raw(crate::return_uint_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_uint_narray() -> *mut Vec<Option<u32>> as Lv_ {
    into_raw(crate::return_uint_narray())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_long_array() -> *mut Vec<i64> as Lw_ {
    into_raw(crate::return_long_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_long_narray() -> *mut Vec<Option<i64>> as Lx_ {
    into_raw(crate::return_long_narray())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ulong_array() -> *mut Vec<u64> as Ly_ {
    into_raw(crate::return_ulong_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_ulong_narray() -> *mut Vec<Option<u64>> as Lz_ {
    into_raw(crate::return_ulong_narray())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_float_array() -> *mut Vec<f32> as MA_ {
    into_raw(crate::return_float_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_float_narray() -> *mut Vec<Option<f32>> as MB_ {
    into_raw(crate::return_float_narray())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_double_array() -> *mut Vec<f64> as MC_ {
    into_raw(crate::return_double_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_double_narray() -> *mut Vec<Option<f64>> as MD_ {
    into_raw(crate::return_double_narray())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_string_array() -> *mut Vec<String> as ME_ {
    into_raw(crate::return_string_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_string_array_n() -> *mut Vec<Option<String>> as MF_ {
    into_raw(crate::return_string_array_n())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_enum_array() -> *mut Vec<MyEnum> as MG_ {
    into_raw(crate::return_enum_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_enum_narray() -> *mut Vec<Option<MyEnum>> as MH_ {
    into_raw(crate::return_enum_narray())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_dictionary_array() -> *mut Vec<MyDictionary> as MI_ {
    into_raw(crate::return_dictionary_array())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_dictionary_array_n() -> *mut Vec<Option<MyDictionary>> as MJ_ {
    into_raw(crate::return_dictionary_array_n())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_char_array(arg: *mut Vec<u16>) -> *mut Vec<u16> as MK_ {
    into_raw(crate::ping_char_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_char_array_empty(arg: *mut Vec<u16>) -> *mut Vec<u16> as ML_ {
    into_raw(crate::ping_char_array_empty(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_char_array_n(arg: *mut Vec<u16>) -> *mut Vec<u16> as MM_ {
    obj_opt(crate::ping_char_array_n(ptr_opt(arg, from_raw)), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_boolean_array(arg: *mut Vec<bool>) -> *mut Vec<bool> as MN_ {
    into_raw(crate::ping_boolean_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_byte_array(arg: *mut Vec<i8>) -> *mut Vec<i8> as MO_ {
    into_raw(crate::ping_byte_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_ubyte_array(arg: *mut Vec<u8>) -> *mut Vec<u8> as MP_ {
    into_raw(crate::ping_ubyte_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_short_array(arg: *mut Vec<i16>) -> *mut Vec<i16> as MQ_ {
    into_raw(crate::ping_short_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_ushort_array(arg: *mut Vec<u16>) -> *mut Vec<u16> as MR_ {
    into_raw(crate::ping_ushort_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_int_array(arg: *mut Vec<i32>) -> *mut Vec<i32> as MS_ {
    into_raw(crate::ping_int_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_uint_array(arg: *mut Vec<u32>) -> *mut Vec<u32> as MT_ {
    into_raw(crate::ping_uint_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_long_array(arg: *mut Vec<i64>) -> *mut Vec<i64> as MU_ {
    into_raw(crate::ping_long_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_ulong_array(arg: *mut Vec<u64>) -> *mut Vec<u64> as MV_ {
    into_raw(crate::ping_ulong_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_float_array(arg: *mut Vec<f32>) -> *mut Vec<f32> as MW_ {
    into_raw(crate::ping_float_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_double_array(arg: *mut Vec<f64>) -> *mut Vec<f64> as MX_ {
    into_raw(crate::ping_double_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_string_array(arg: *mut Vec<String>) -> *mut Vec<String> as MY_ {
    into_raw(crate::ping_string_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_string_array_n(arg: *mut Vec<Option<String>>) -> *mut Vec<Option<String>> as MZ_ {
    into_raw(crate::ping_string_array_n(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_enum_array(arg: *mut Vec<MyEnum>) -> *mut Vec<MyEnum> as Ma_ {
    into_raw(crate::ping_enum_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_dictionary_array(arg: *mut Vec<MyDictionary>) -> *mut Vec<MyDictionary> as Mb_ {
    into_raw(crate::ping_dictionary_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_dictionary_array_n(arg: *mut Vec<Option<MyDictionary>>) -> *mut Vec<Option<MyDictionary>> as Mc_ {
    into_raw(crate::ping_dictionary_array_n(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_interface_array(arg: *mut Vec<Arc<crate::MyInterface>>) -> *mut Vec<Arc<crate::MyInterface>> as Md_ {
    into_raw(crate::ping_interface_array(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_interface_array_n(arg: *mut Vec<Option<Arc<crate::MyInterface>>>) -> *mut Vec<Option<Arc<crate::MyInterface>>> as Me_ {
    into_raw(crate::ping_interface_array_n(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_char_array(arg: *mut Arc<CallbackPassCharArray>) -> bool as Mf_ {
    crate::callback_arg_char_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_char_array_empty(arg: *mut Arc<CallbackPassCharArrayEmpty>) -> bool as Mg_ {
    crate::callback_arg_char_array_empty(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_char_array_n(arg: *mut Arc<CallbackPassCharArrayN>) -> bool as Mh_ {
    crate::callback_arg_char_array_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_boolean_array(arg: *mut Arc<CallbackPassBooleanArray>) -> bool as Mi_ {
    crate::callback_arg_boolean_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_byte_array(arg: *mut Arc<CallbackPassByteArray>) -> bool as Mj_ {
    crate::callback_arg_byte_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_ubyte_array(arg: *mut Arc<CallbackPassUByteArray>) -> bool as Mk_ {
    crate::callback_arg_ubyte_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_short_array(arg: *mut Arc<CallbackPassShortArray>) -> bool as Ml_ {
    crate::callback_arg_short_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_ushort_array(arg: *mut Arc<CallbackPassUShortArray>) -> bool as Mm_ {
    crate::callback_arg_ushort_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_int_array(arg: *mut Arc<CallbackPassIntArray>) -> bool as Mn_ {
    crate::callback_arg_int_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_uint_array(arg: *mut Arc<CallbackPassUIntArray>) -> bool as Mo_ {
    crate::callback_arg_uint_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_long_array(arg: *mut Arc<CallbackPassLongArray>) -> bool as Mp_ {
    crate::callback_arg_long_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_ulong_array(arg: *mut Arc<CallbackPassULongArray>) -> bool as Mq_ {
    crate::callback_arg_ulong_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_float_array(arg: *mut Arc<CallbackPassFloatArray>) -> bool as Mr_ {
    crate::callback_arg_float_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_double_array(arg: *mut Arc<CallbackPassDoubleArray>) -> bool as Ms_ {
    crate::callback_arg_double_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_string_array(arg: *mut Arc<CallbackPassStringArray>) -> bool as Mt_ {
    crate::callback_arg_string_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_string_array_n(arg: *mut Arc<CallbackPassStringArrayN>) -> bool as Mu_ {
    crate::callback_arg_string_array_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_enum_array(arg: *mut Arc<CallbackPassEnumArray>) -> bool as Mv_ {
    crate::callback_arg_enum_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_dictionary_array(arg: *mut Arc<CallbackPassDictionaryArray>) -> bool as Mw_ {
    crate::callback_arg_dictionary_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_dictionary_array_n(arg: *mut Arc<CallbackPassDictionaryArrayN>) -> bool as Mx_ {
    crate::callback_arg_dictionary_array_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_interface_array(arg: *mut Arc<CallbackPassInterfaceArray>) -> bool as My_ {
    crate::callback_arg_interface_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_arg_interface_array_n(arg: *mut Arc<CallbackPassInterfaceArrayN>) -> bool as Mz_ {
    crate::callback_arg_interface_array_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_char_array(arg: *mut Arc<CallbackReturnCharArray>) -> bool as NA_ {
    crate::callback_return_char_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_char_array_empty(arg: *mut Arc<CallbackReturnCharArrayEmpty>) -> bool as NB_ {
    crate::callback_return_char_array_empty(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_char_array_n(arg: *mut Arc<CallbackReturnCharArrayN>) -> bool as NC_ {
    crate::callback_return_char_array_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_boolean_array(arg: *mut Arc<CallbackReturnBooleanArray>) -> bool as ND_ {
    crate::callback_return_boolean_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_byte_array(arg: *mut Arc<CallbackReturnByteArray>) -> bool as NE_ {
    crate::callback_return_byte_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_ubyte_array(arg: *mut Arc<CallbackReturnUByteArray>) -> bool as NF_ {
    crate::callback_return_ubyte_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_short_array(arg: *mut Arc<CallbackReturnShortArray>) -> bool as NG_ {
    crate::callback_return_short_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_ushort_array(arg: *mut Arc<CallbackReturnUShortArray>) -> bool as NH_ {
    crate::callback_return_ushort_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_int_array(arg: *mut Arc<CallbackReturnIntArray>) -> bool as NI_ {
    crate::callback_return_int_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_uint_array(arg: *mut Arc<CallbackReturnUIntArray>) -> bool as NJ_ {
    crate::callback_return_uint_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_long_array(arg: *mut Arc<CallbackReturnLongArray>) -> bool as NK_ {
    crate::callback_return_long_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_ulong_array(arg: *mut Arc<CallbackReturnULongArray>) -> bool as NL_ {
    crate::callback_return_ulong_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_float_array(arg: *mut Arc<CallbackReturnFloatArray>) -> bool as NM_ {
    crate::callback_return_float_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_double_array(arg: *mut Arc<CallbackReturnDoubleArray>) -> bool as NN_ {
    crate::callback_return_double_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_string_array(arg: *mut Arc<CallbackReturnStringArray>) -> bool as NO_ {
    crate::callback_return_string_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_string_array_n(arg: *mut Arc<CallbackReturnStringArrayN>) -> bool as NP_ {
    crate::callback_return_string_array_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_enum_array(arg: *mut Arc<CallbackReturnEnumArray>) -> bool as NQ_ {
    crate::callback_return_enum_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_dictionary_array(arg: *mut Arc<CallbackReturnDictionaryArray>) -> bool as NR_ {
    crate::callback_return_dictionary_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_dictionary_array_n(arg: *mut Arc<CallbackReturnDictionaryArrayN>) -> bool as NS_ {
    crate::callback_return_dictionary_array_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_interface_array(arg: *mut Arc<CallbackReturnInterfaceArray>) -> bool as NT_ {
    crate::callback_return_interface_array(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_callback_return_interface_array_n(arg: *mut Arc<CallbackReturnInterfaceArrayN>) -> bool as NU_ {
    crate::callback_return_interface_array_n(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_big_dictionary(arg: *mut TypeDictionary) -> bool as NV_ {
    crate::pass_big_dictionary(from_raw(arg))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_big_dictionary(callback: *mut Arc<VoidCallback>, inter: *mut Arc<crate::MyInterface>) -> *mut TypeDictionary as NW_ {
    into_raw(crate::return_big_dictionary(from_raw(callback), from_raw(inter)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_big_dictionary(arg: *mut TypeDictionary) -> *mut TypeDictionary as NX_ {
    into_raw(crate::ping_big_dictionary(from_raw(arg)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_pass_big_dictionary_n(arg: *mut TypeDictionary) -> bool as NY_ {
    crate::pass_big_dictionary_n(ptr_opt(arg, from_raw))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_return_big_dictionary_n() -> *mut TypeDictionary as NZ_ {
    obj_opt(crate::return_big_dictionary_n(), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_ping_big_dictionary_n(arg: *mut TypeDictionary) -> *mut TypeDictionary as Na_ {
    obj_opt(crate::ping_big_dictionary_n(ptr_opt(arg, from_raw)), into_raw)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_primitives(a1: u16, a2: bool, a3: i8, a4: u8, a5: i16, a6: u16, a7: i32, a8: u32, a9: i64, a10: u64, a11: f32, a12: f64) -> bool as Nb_ {
    crate::critical_primitives(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_enum(a1: MyEnum) -> bool as Nc_ {
    crate::critical_enum(a1)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_string(a1: *mut u8, _a1_size: i32) -> bool as Nd_ {
    crate::critical_string(&critical_string(a1, _a1_size))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_string_empty(a1: *mut u8, _a1_size: i32) -> bool as Ne_ {
    crate::critical_string_empty(&critical_string(a1, _a1_size))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_string_n(a1: *mut u8, _a1_size: i32) -> bool as Nf_ {
    crate::critical_string_n(&critical_string_opt(a1, _a1_size))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_interface(a1: *mut Arc<crate::MyInterface>) -> bool as Ng_ {
    crate::critical_interface(&from_raw(a1))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_interface_n(a1: *mut Arc<crate::MyInterface>) -> bool as Nh_ {
    crate::critical_interface_n(&ptr_opt(a1, from_raw))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_primitives_array(a1: *mut u16, _a1_length: i32, a2: *mut bool, _a2_length: i32, a3: *mut i8, _a3_length: i32, a4: *mut u8, _a4_length: i32, a5: *mut i16, _a5_length: i32, a6: *mut u16, _a6_length: i32, a7: *mut i32, _a7_length: i32, a8: *mut u32, _a8_length: i32, a9: *mut i64, _a9_length: i32, a10: *mut u64, _a10_length: i32, a11: *mut f32, _a11_length: i32, a12: *mut f64, _a12_length: i32) -> bool as Ni_ {
    crate::critical_primitives_array(&critical_array(a1, _a1_length), &critical_array(a2, _a2_length), &critical_array(a3, _a3_length), &critical_array(a4, _a4_length), &critical_array(a5, _a5_length), &critical_array(a6, _a6_length), &critical_array(a7, _a7_length), &critical_array(a8, _a8_length), &critical_array(a9, _a9_length), &critical_array(a10, _a10_length), &critical_array(a11, _a11_length), &critical_array(a12, _a12_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_enum_array(a1: *mut MyEnum, _a1_length: i32) -> bool as Nj_ {
    crate::critical_enum_array(&critical_array(a1, _a1_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_primitives_array_empty(a1: *mut u16, _a1_length: i32, a2: *mut bool, _a2_length: i32, a3: *mut i8, _a3_length: i32, a4: *mut u8, _a4_length: i32, a5: *mut i16, _a5_length: i32, a6: *mut u16, _a6_length: i32, a7: *mut i32, _a7_length: i32, a8: *mut u32, _a8_length: i32, a9: *mut i64, _a9_length: i32, a10: *mut u64, _a10_length: i32, a11: *mut f32, _a11_length: i32, a12: *mut f64, _a12_length: i32) -> bool as Nk_ {
    crate::critical_primitives_array_empty(&critical_array(a1, _a1_length), &critical_array(a2, _a2_length), &critical_array(a3, _a3_length), &critical_array(a4, _a4_length), &critical_array(a5, _a5_length), &critical_array(a6, _a6_length), &critical_array(a7, _a7_length), &critical_array(a8, _a8_length), &critical_array(a9, _a9_length), &critical_array(a10, _a10_length), &critical_array(a11, _a11_length), &critical_array(a12, _a12_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_enum_array_empty(a1: *mut MyEnum, _a1_length: i32) -> bool as Nl_ {
    crate::critical_enum_array_empty(&critical_array(a1, _a1_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_primitives_array_n(a1: *mut u16, _a1_length: i32, a2: *mut bool, _a2_length: i32, a3: *mut i8, _a3_length: i32, a4: *mut u8, _a4_length: i32, a5: *mut i16, _a5_length: i32, a6: *mut u16, _a6_length: i32, a7: *mut i32, _a7_length: i32, a8: *mut u32, _a8_length: i32, a9: *mut i64, _a9_length: i32, a10: *mut u64, _a10_length: i32, a11: *mut f32, _a11_length: i32, a12: *mut f64, _a12_length: i32) -> bool as Nm_ {
    crate::critical_primitives_array_n(&critical_array_opt(a1, _a1_length), &critical_array_opt(a2, _a2_length), &critical_array_opt(a3, _a3_length), &critical_array_opt(a4, _a4_length), &critical_array_opt(a5, _a5_length), &critical_array_opt(a6, _a6_length), &critical_array_opt(a7, _a7_length), &critical_array_opt(a8, _a8_length), &critical_array_opt(a9, _a9_length), &critical_array_opt(a10, _a10_length), &critical_array_opt(a11, _a11_length), &critical_array_opt(a12, _a12_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_enum_array_n(a1: *mut MyEnum, _a1_length: i32) -> bool as Nn_ {
    crate::critical_enum_array_n(&critical_array_opt(a1, _a1_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_char() -> u16 as No_ {
    crate::critical_return_char()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_boolean() -> bool as Np_ {
    crate::critical_return_boolean()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_byte() -> i8 as Nq_ {
    crate::critical_return_byte()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_ubyte() -> u8 as Nr_ {
    crate::critical_return_ubyte()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_short() -> i16 as Ns_ {
    crate::critical_return_short()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_ushort() -> u16 as Nt_ {
    crate::critical_return_ushort()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_int() -> i32 as Nu_ {
    crate::critical_return_int()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_uint() -> u32 as Nv_ {
    crate::critical_return_uint()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_long() -> i64 as Nw_ {
    crate::critical_return_long()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_ulong() -> u64 as Nx_ {
    crate::critical_return_ulong()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_float() -> f32 as Ny_ {
    crate::critical_return_float()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_double() -> f64 as Nz_ {
    crate::critical_return_double()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_enum() -> MyEnum as OA_ {
    crate::critical_return_enum()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_critical_return_interface() -> *mut Arc<crate::MyInterface> as OB_ {
    into_raw(crate::critical_return_interface())
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci1() -> bool as OC_ {
    crate::jvmci1()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci2(a1: i32) -> bool as OD_ {
    crate::jvmci2(a1)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci3(a1: i32, a2: i32) -> bool as OE_ {
    crate::jvmci3(a1, a2)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci4(a1: i32, a2: i32, a3: i32, a4: i32, a5: i32, a6: i32, a7: i32, a8: i32, a9: i32) -> bool as OF_ {
    crate::jvmci4(a1, a2, a3, a4, a5, a6, a7, a8, a9)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci5(a1: i32, a2: i64, a3: i32, a4: i64, a5: i32, a6: i64, a7: i32, a8: i32, a9: i64) -> bool as OG_ {
    crate::jvmci5(a1, a2, a3, a4, a5, a6, a7, a8, a9)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci6(a1: f32, a2: f32, a3: f32, a4: f32, a5: f32, a6: f32, a7: f32, a8: f32, a9: f32, a10: i32, a11: i32, a12: i32, a13: i32) -> bool as OH_ {
    crate::jvmci6(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci7(a1: f32, a2: f64, a3: f32, a4: f64, a5: f32, a6: f64, a7: f32, a8: f32, a9: f64) -> bool as OI_ {
    crate::jvmci7(a1, a2, a3, a4, a5, a6, a7, a8, a9)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci8(a1: i32, a2: f64, a3: f32, a4: i64) -> bool as OJ_ {
    crate::jvmci8(a1, a2, a3, a4)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci9(a1: i32, a2: f64, a3: f32, a4: i64, a5: i64, a6: f64, a7: f32, a8: f32, a9: i32) -> bool as OK_ {
    crate::jvmci9(a1, a2, a3, a4, a5, a6, a7, a8, a9)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci10(a1: *mut u8, _a1_size: i32, a2: f64, a3: f32, a4: i64, a5: i64, a6: f64, a7: *mut u8, _a7_size: i32, a8: f32, a9: i32) -> bool as OL_ {
    crate::jvmci10(&critical_string(a1, _a1_size), a2, a3, a4, a5, a6, &critical_string(a7, _a7_size), a8, a9)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci11(a1: f32, a2: i32, a3: f32, a4: i32, a5: f32, a6: i32, a7: f32, a8: i32, a9: f32, a10: i32, a11: f32, a12: i32, a13: f32, a14: i32, a15: f32, a16: i32, a17: f32) -> bool as OM_ {
    crate::jvmci11(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17)
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci12() -> i32 as ON_ {
    crate::jvmci12()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci13() -> i64 as OO_ {
    crate::jvmci13()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci14() -> f32 as OP_ {
    crate::jvmci14()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci15() -> f64 as OQ_ {
    crate::jvmci15()
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci_array(array: *mut i32, _array_length: i32) -> bool as OR_ {
    crate::jvmci_array(&critical_array(array, _array_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci_some_arrays(array1: *mut i32, _array1_length: i32, array2: *mut f32, _array2_length: i32, array3: *mut f64, _array3_length: i32) -> bool as OS_ {
    crate::jvmci_some_arrays(&critical_array(array1, _array1_length), &critical_array(array2, _array2_length), &critical_array(array3, _array3_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs_jvmci_enum(enum1: MyEnum, enum2: MyEnum, enum_array: *mut MyEnum, _enum_array_length: i32) -> bool as OT_ {
    crate::jvmci_enum(enum1, enum2, &critical_array(enum_array, _enum_array_length))
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_new_0() -> *mut Arc<crate::MyInterface> as OU_ {
    into_raw(Arc::new(crate::MyInterface::new()))
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_new_1(a: i32) -> *mut Arc<crate::MyInterface> as OV_ {
    into_raw(Arc::new(crate::MyInterface::new_critical(a)))
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_fn_test(_self: *mut Arc<crate::MyInterface>) -> bool as OW_ {
    unsafe { &*_self }.test()
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_fn_test_critical(_self: *mut Arc<crate::MyInterface>) -> bool as OX_ {
    unsafe { &*_self }.test_critical()
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_fn_pass_interface(_self: *mut Arc<crate::MyInterface>, a1: *mut Arc<crate::MyInterface>) -> () as OY_ {
    unsafe { &*_self }.pass_interface(&from_raw(a1))
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_fn_return_interface(_self: *mut Arc<crate::MyInterface>) -> *mut Arc<crate::MyInterface> as OZ_ {
    into_raw(unsafe { &*_self }.return_interface())
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_free(_self: *mut Arc<crate::MyInterface>) -> () as Oa_ {
    from_raw(_self);
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_clone(_self: *mut Arc<crate::MyInterface>) -> *mut Arc<crate::MyInterface> as Ob_ {
    into_raw(unsafe { &*_self }.clone())
}}
export_fn!{ fn nativekt_natives_testrs_testrs__interface_myinterface_address(_self: *mut Arc<crate::MyInterface>) -> usize as Oc_ {
    Arc::as_ptr(unsafe { &*_self }) as usize
}}