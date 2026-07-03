
// ╔═════════════╗
// ║     API     ║
// ╚═════════════╝
/*
=============================================================== *\

pub fn pass_void() -> bool {}

pub fn pass_char(
	arg: u16
) -> bool {}

pub fn pass_boolean(
	arg: bool
) -> bool {}

pub fn pass_byte(
	arg: i8
) -> bool {}

pub fn pass_ubyte(
	arg: u8
) -> bool {}

pub fn pass_short(
	arg: i16
) -> bool {}

pub fn pass_ushort(
	arg: u16
) -> bool {}

pub fn pass_int(
	arg: i32
) -> bool {}

pub fn pass_uint(
	arg: u32
) -> bool {}

pub fn pass_long(
	arg: i64
) -> bool {}

pub fn pass_ulong(
	arg: u64
) -> bool {}

pub fn pass_float(
	arg: f32
) -> bool {}

pub fn pass_double(
	arg: f64
) -> bool {}

pub fn pass_string(
	arg: KString
) -> bool {}

pub fn pass_string_n(
	arg: Option<KString>
) -> bool {}

pub fn pass_enum(
	arg: MyEnum
) -> bool {}

pub fn pass_dictionary(
	arg: MyDictionary
) -> bool {}

pub fn pass_dictionary_n(
	arg: Option<MyDictionary>
) -> bool {}

pub fn return_void() {}

pub fn return_char() -> u16 {}

pub fn return_boolean() -> bool {}

pub fn return_byte() -> i8 {}

pub fn return_ubyte() -> u8 {}

pub fn return_short() -> i16 {}

pub fn return_ushort() -> u16 {}

pub fn return_int() -> i32 {}

pub fn return_uint() -> u32 {}

pub fn return_long() -> i64 {}

pub fn return_ulong() -> u64 {}

pub fn return_float() -> f32 {}

pub fn return_double() -> f64 {}

pub fn return_string() -> KString {}

pub fn return_string_n() -> Option<KString> {}

pub fn return_enum() -> MyEnum {}

pub fn return_dictionary() -> MyDictionary {}

pub fn return_dictionary_n() -> Option<MyDictionary> {}

pub fn ping_char(
	arg: u16
) -> u16 {}

pub fn ping_boolean(
	arg: bool
) -> bool {}

pub fn ping_byte(
	arg: i8
) -> i8 {}

pub fn ping_ubyte(
	arg: u8
) -> u8 {}

pub fn ping_short(
	arg: i16
) -> i16 {}

pub fn ping_ushort(
	arg: u16
) -> u16 {}

pub fn ping_int(
	arg: i32
) -> i32 {}

pub fn ping_uint(
	arg: u32
) -> u32 {}

pub fn ping_long(
	arg: i64
) -> i64 {}

pub fn ping_ulong(
	arg: u64
) -> u64 {}

pub fn ping_float(
	arg: f32
) -> f32 {}

pub fn ping_double(
	arg: f64
) -> f64 {}

pub fn ping_string(
	arg: KString
) -> KString {}

pub fn ping_string_n(
	arg: Option<KString>
) -> Option<KString> {}

pub fn ping_enum(
	arg: MyEnum
) -> MyEnum {}

pub fn ping_dictionary(
	arg: MyDictionary
) -> MyDictionary {}

pub fn ping_dictionary_n(
	arg: Option<MyDictionary>
) -> Option<MyDictionary> {}

pub fn callback_void(
	arg: VoidCallback
) {}

pub fn callback_void_n(
	arg: Option<VoidCallback>
) -> bool {}

pub fn callback_arg_char(
	arg: CallbackPassChar
) -> bool {}

pub fn callback_arg_boolean(
	arg: CallbackPassBoolean
) -> bool {}

pub fn callback_arg_byte(
	arg: CallbackPassByte
) -> bool {}

pub fn callback_arg_ubyte(
	arg: CallbackPassUByte
) -> bool {}

pub fn callback_arg_short(
	arg: CallbackPassShort
) -> bool {}

pub fn callback_arg_ushort(
	arg: CallbackPassUShort
) -> bool {}

pub fn callback_arg_int(
	arg: CallbackPassInt
) -> bool {}

pub fn callback_arg_uint(
	arg: CallbackPassUInt
) -> bool {}

pub fn callback_arg_long(
	arg: CallbackPassLong
) -> bool {}

pub fn callback_arg_ulong(
	arg: CallbackPassULong
) -> bool {}

pub fn callback_arg_float(
	arg: CallbackPassFloat
) -> bool {}

pub fn callback_arg_double(
	arg: CallbackPassDouble
) -> bool {}

pub fn callback_arg_string(
	arg: CallbackPassString
) -> bool {}

pub fn callback_arg_string_n(
	arg: CallbackPassStringN
) -> bool {}

pub fn callback_arg_callback(
	pass: VoidCallback,
	arg: CallbackPassCallback
) -> bool {}

pub fn callback_arg_callback_n(
	arg: CallbackPassCallbackN
) -> bool {}

pub fn callback_arg_enum(
	arg: CallbackPassEnum
) -> bool {}

pub fn callback_arg_dictionary(
	arg: CallbackPassDictionary
) -> bool {}

pub fn callback_arg_dictionary_n(
	arg: CallbackPassDictionaryN
) -> bool {}

pub fn callback_return_char(
	arg: CallbackReturnChar
) -> bool {}

pub fn callback_return_boolean(
	arg: CallbackReturnBoolean
) -> bool {}

pub fn callback_return_byte(
	arg: CallbackReturnByte
) -> bool {}

pub fn callback_return_ubyte(
	arg: CallbackReturnUByte
) -> bool {}

pub fn callback_return_short(
	arg: CallbackReturnShort
) -> bool {}

pub fn callback_return_ushort(
	arg: CallbackReturnUShort
) -> bool {}

pub fn callback_return_int(
	arg: CallbackReturnInt
) -> bool {}

pub fn callback_return_uint(
	arg: CallbackReturnUInt
) -> bool {}

pub fn callback_return_long(
	arg: CallbackReturnLong
) -> bool {}

pub fn callback_return_ulong(
	arg: CallbackReturnULong
) -> bool {}

pub fn callback_return_float(
	arg: CallbackReturnFloat
) -> bool {}

pub fn callback_return_double(
	arg: CallbackReturnDouble
) -> bool {}

pub fn callback_return_string(
	arg: CallbackReturnString
) -> bool {}

pub fn callback_return_string_n(
	arg: CallbackReturnStringN
) -> bool {}

pub fn callback_return_callback(
	arg: CallbackReturnCallback
) -> VoidCallback {}

pub fn callback_return_callback_n(
	arg: CallbackReturnCallbackN
) -> bool {}

pub fn callback_return_enum(
	arg: CallbackReturnEnum
) -> bool {}

pub fn callback_return_dictionary(
	arg: CallbackReturnDictionary
) -> bool {}

pub fn callback_return_dictionary_n(
	arg: CallbackReturnDictionaryN
) -> bool {}

pub fn pass_char_array(
	arg: KCharArray
) -> bool {}

pub fn pass_char_array_n(
	arg: Option<KCharArray>
) -> bool {}

pub fn pass_boolean_array(
	arg: KBooleanArray
) -> bool {}

pub fn pass_byte_array(
	arg: KByteArray
) -> bool {}

pub fn pass_ubyte_array(
	arg: KUByteArray
) -> bool {}

pub fn pass_short_array(
	arg: KShortArray
) -> bool {}

pub fn pass_ushort_array(
	arg: KUShortArray
) -> bool {}

pub fn pass_int_array(
	arg: KIntArray
) -> bool {}

pub fn pass_uint_array(
	arg: KUIntArray
) -> bool {}

pub fn pass_long_array(
	arg: KLongArray
) -> bool {}

pub fn pass_ulong_array(
	arg: KULongArray
) -> bool {}

pub fn pass_float_array(
	arg: KFloatArray
) -> bool {}

pub fn pass_double_array(
	arg: KDoubleArray
) -> bool {}

pub fn pass_string_array(
	arg: KArray<KString>
) -> bool {}

pub fn pass_string_array_n(
	arg: KArrayOpt<KString>
) -> bool {}

pub fn pass_enum_array(
	arg: KIntArray
) -> bool {}

pub fn pass_dictionary_array(
	arg: KArray<MyDictionary>
) -> bool {}

pub fn pass_dictionary_array_n(
	arg: KArrayOpt<MyDictionary>
) -> bool {}

pub fn return_char_array() -> KCharArray {}

pub fn return_char_array_n() -> Option<KCharArray> {}

pub fn return_boolean_array() -> KBooleanArray {}

pub fn return_byte_array() -> KByteArray {}

pub fn return_ubyte_array() -> KUByteArray {}

pub fn return_short_array() -> KShortArray {}

pub fn return_ushort_array() -> KUShortArray {}

pub fn return_int_array() -> KIntArray {}

pub fn return_uint_array() -> KUIntArray {}

pub fn return_long_array() -> KLongArray {}

pub fn return_ulong_array() -> KULongArray {}

pub fn return_float_array() -> KFloatArray {}

pub fn return_double_array() -> KDoubleArray {}

pub fn return_string_array() -> KArray<KString> {}

pub fn return_string_array_n() -> KArrayOpt<KString> {}

pub fn return_enum_array() -> KIntArray {}

pub fn return_dictionary_array() -> KArray<MyDictionary> {}

pub fn return_dictionary_array_n() -> KArrayOpt<MyDictionary> {}

pub fn ping_char_array(
	arg: KCharArray
) -> KCharArray {}

pub fn ping_char_array_n(
	arg: Option<KCharArray>
) -> Option<KCharArray> {}

pub fn ping_boolean_array(
	arg: KBooleanArray
) -> KBooleanArray {}

pub fn ping_byte_array(
	arg: KByteArray
) -> KByteArray {}

pub fn ping_ubyte_array(
	arg: KUByteArray
) -> KUByteArray {}

pub fn ping_short_array(
	arg: KShortArray
) -> KShortArray {}

pub fn ping_ushort_array(
	arg: KUShortArray
) -> KUShortArray {}

pub fn ping_int_array(
	arg: KIntArray
) -> KIntArray {}

pub fn ping_uint_array(
	arg: KUIntArray
) -> KUIntArray {}

pub fn ping_long_array(
	arg: KLongArray
) -> KLongArray {}

pub fn ping_ulong_array(
	arg: KULongArray
) -> KULongArray {}

pub fn ping_float_array(
	arg: KFloatArray
) -> KFloatArray {}

pub fn ping_double_array(
	arg: KDoubleArray
) -> KDoubleArray {}

pub fn ping_string_array(
	arg: KArray<KString>
) -> KArray<KString> {}

pub fn ping_string_array_n(
	arg: KArrayOpt<KString>
) -> KArrayOpt<KString> {}

pub fn ping_enum_array(
	arg: KIntArray
) -> KIntArray {}

pub fn ping_dictionary_array(
	arg: KArray<MyDictionary>
) -> KArray<MyDictionary> {}

pub fn ping_dictionary_array_n(
	arg: KArrayOpt<MyDictionary>
) -> KArrayOpt<MyDictionary> {}

pub fn callback_arg_char_array(
	arg: CallbackPassCharArray
) -> bool {}

pub fn callback_arg_char_array_n(
	arg: CallbackPassCharArrayN
) -> bool {}

pub fn callback_arg_boolean_array(
	arg: CallbackPassBooleanArray
) -> bool {}

pub fn callback_arg_byte_array(
	arg: CallbackPassByteArray
) -> bool {}

pub fn callback_arg_ubyte_array(
	arg: CallbackPassUByteArray
) -> bool {}

pub fn callback_arg_short_array(
	arg: CallbackPassShortArray
) -> bool {}

pub fn callback_arg_ushort_array(
	arg: CallbackPassUShortArray
) -> bool {}

pub fn callback_arg_int_array(
	arg: CallbackPassIntArray
) -> bool {}

pub fn callback_arg_uint_array(
	arg: CallbackPassUIntArray
) -> bool {}

pub fn callback_arg_long_array(
	arg: CallbackPassLongArray
) -> bool {}

pub fn callback_arg_ulong_array(
	arg: CallbackPassULongArray
) -> bool {}

pub fn callback_arg_float_array(
	arg: CallbackPassFloatArray
) -> bool {}

pub fn callback_arg_double_array(
	arg: CallbackPassDoubleArray
) -> bool {}

pub fn callback_arg_string_array(
	arg: CallbackPassStringArray
) -> bool {}

pub fn callback_arg_string_array_n(
	arg: CallbackPassStringArrayN
) -> bool {}

pub fn callback_arg_enum_array(
	arg: CallbackPassEnumArray
) -> bool {}

pub fn callback_arg_dictionary_array(
	arg: CallbackPassDictionaryArray
) -> bool {}

pub fn callback_arg_dictionary_array_n(
	arg: CallbackPassDictionaryArrayN
) -> bool {}

pub fn callback_return_char_array(
	arg: CallbackReturnCharArray
) -> bool {}

pub fn callback_return_char_array_n(
	arg: CallbackReturnCharArrayN
) -> bool {}

pub fn callback_return_boolean_array(
	arg: CallbackReturnBooleanArray
) -> bool {}

pub fn callback_return_byte_array(
	arg: CallbackReturnByteArray
) -> bool {}

pub fn callback_return_ubyte_array(
	arg: CallbackReturnUByteArray
) -> bool {}

pub fn callback_return_short_array(
	arg: CallbackReturnShortArray
) -> bool {}

pub fn callback_return_ushort_array(
	arg: CallbackReturnUShortArray
) -> bool {}

pub fn callback_return_int_array(
	arg: CallbackReturnIntArray
) -> bool {}

pub fn callback_return_uint_array(
	arg: CallbackReturnUIntArray
) -> bool {}

pub fn callback_return_long_array(
	arg: CallbackReturnLongArray
) -> bool {}

pub fn callback_return_ulong_array(
	arg: CallbackReturnULongArray
) -> bool {}

pub fn callback_return_float_array(
	arg: CallbackReturnFloatArray
) -> bool {}

pub fn callback_return_double_array(
	arg: CallbackReturnDoubleArray
) -> bool {}

pub fn callback_return_string_array(
	arg: CallbackReturnStringArray
) -> bool {}

pub fn callback_return_string_array_n(
	arg: CallbackReturnStringArrayN
) -> bool {}

pub fn callback_return_enum_array(
	arg: CallbackReturnEnumArray
) -> bool {}

pub fn callback_return_dictionary_array(
	arg: CallbackReturnDictionaryArray
) -> bool {}

pub fn callback_return_dictionary_array_n(
	arg: CallbackReturnDictionaryArrayN
) -> bool {}

pub fn pass_big_dictionary(
	arg: TypeDictionary
) -> bool {}

pub fn return_big_dictionary(
	callback: VoidCallback
) -> TypeDictionary {}

pub fn ping_big_dictionary(
	arg: TypeDictionary
) -> TypeDictionary {}

pub fn pass_big_dictionary_n(
	arg: Option<TypeDictionary>
) -> bool {}

pub fn return_big_dictionary_n() -> Option<TypeDictionary> {}

pub fn ping_big_dictionary_n(
	arg: Option<TypeDictionary>
) -> Option<TypeDictionary> {}

pub fn critical_primitives(
	a1: u16,
	a2: bool,
	a3: i8,
	a4: u8,
	a5: i16,
	a6: u16,
	a7: i32,
	a8: u32,
	a9: i64,
	a10: u64,
	a11: f32,
	a12: f64
) -> bool {}

pub fn critical_enum(
	a1: MyEnum
) -> bool {}

pub fn critical_string(
	a1: KString
) -> bool {}

pub fn critical_string_n(
	a1: Option<KString>
) -> bool {}

pub fn critical_primitives_array(
	a1: KCharArray,
	a2: KBooleanArray,
	a3: KByteArray,
	a4: KUByteArray,
	a5: KShortArray,
	a6: KUShortArray,
	a7: KIntArray,
	a8: KUIntArray,
	a9: KLongArray,
	a10: KULongArray,
	a11: KFloatArray,
	a12: KDoubleArray
) -> bool {}

pub fn critical_enum_array(
	a1: KIntArray
) -> bool {}

pub fn critical_primitives_array_n(
	a1: Option<KCharArray>,
	a2: Option<KBooleanArray>,
	a3: Option<KByteArray>,
	a4: Option<KUByteArray>,
	a5: Option<KShortArray>,
	a6: Option<KUShortArray>,
	a7: Option<KIntArray>,
	a8: Option<KUIntArray>,
	a9: Option<KLongArray>,
	a10: Option<KULongArray>,
	a11: Option<KFloatArray>,
	a12: Option<KDoubleArray>
) -> bool {}

pub fn critical_enum_array_n(
	a1: Option<KIntArray>
) -> bool {}

pub fn critical_return_char() -> u16 {}

pub fn critical_return_boolean() -> bool {}

pub fn critical_return_byte() -> i8 {}

pub fn critical_return_ubyte() -> u8 {}

pub fn critical_return_short() -> i16 {}

pub fn critical_return_ushort() -> u16 {}

pub fn critical_return_int() -> i32 {}

pub fn critical_return_uint() -> u32 {}

pub fn critical_return_long() -> i64 {}

pub fn critical_return_ulong() -> u64 {}

pub fn critical_return_float() -> f32 {}

pub fn critical_return_double() -> f64 {}

pub fn critical_return_enum() -> MyEnum {}

pub fn jvmci1() -> bool {}

pub fn jvmci2(
	a1: i32
) -> bool {}

pub fn jvmci3(
	a1: i32,
	a2: i32
) -> bool {}

pub fn jvmci4(
	a1: i32,
	a2: i32,
	a3: i32,
	a4: i32,
	a5: i32,
	a6: i32,
	a7: i32,
	a8: i32,
	a9: i32
) -> bool {}

pub fn jvmci5(
	a1: i32,
	a2: i64,
	a3: i32,
	a4: i64,
	a5: i32,
	a6: i64,
	a7: i32,
	a8: i32,
	a9: i64
) -> bool {}

pub fn jvmci6(
	a1: f32,
	a2: f32,
	a3: f32,
	a4: f32,
	a5: f32,
	a6: f32,
	a7: f32,
	a8: f32,
	a9: f32,
	a10: i32,
	a11: i32,
	a12: i32,
	a13: i32
) -> bool {}

pub fn jvmci7(
	a1: f32,
	a2: f64,
	a3: f32,
	a4: f64,
	a5: f32,
	a6: f64,
	a7: f32,
	a8: f32,
	a9: f64
) -> bool {}

pub fn jvmci8(
	a1: i32,
	a2: f64,
	a3: f32,
	a4: i64
) -> bool {}

pub fn jvmci9(
	a1: i32,
	a2: f64,
	a3: f32,
	a4: i64,
	a5: i64,
	a6: f64,
	a7: f32,
	a8: f32,
	a9: i32
) -> bool {}

pub fn jvmci10(
	a1: KString,
	a2: f64,
	a3: f32,
	a4: i64,
	a5: i64,
	a6: f64,
	a7: KString,
	a8: f32,
	a9: i32
) -> bool {}

pub fn jvmci11(
	a1: f32,
	a2: i32,
	a3: f32,
	a4: i32,
	a5: f32,
	a6: i32,
	a7: f32,
	a8: i32,
	a9: f32,
	a10: i32,
	a11: f32,
	a12: i32,
	a13: f32,
	a14: i32,
	a15: f32,
	a16: i32,
	a17: f32
) -> bool {}

pub fn jvmci12() -> i32 {}

pub fn jvmci13() -> i64 {}

pub fn jvmci14() -> f32 {}

pub fn jvmci15() -> f64 {}

pub fn jvmci_array(
	array: KIntArray
) -> bool {}

pub fn jvmci_some_arrays(
	array1: KIntArray,
	array2: KFloatArray,
	array3: KDoubleArray
) -> bool {}

pub fn jvmci_enum(
	enum1: MyEnum,
	enum2: MyEnum,
	enum_array: KIntArray
) -> bool {}

=============================================================== */

#![allow(unused)]
#![allow(private_bounds)]

use std::ffi::c_void;
use std::fmt::{Debug, Display, Formatter};
use std::ptr::null;

extern "C" {
    fn malloc(s: usize) -> *const c_void;
}

trait PtrHolder: Clone {
    fn ptr(&self) -> *const c_void;
    fn free_ptr() -> *const c_void;
    fn clone_ptr() -> *const c_void;
    fn wrap(of: *const c_void) -> Self;
}

macro_rules! impl_wrapper {
    ($name:ident, $inner:ident) => {
        impl $name {
            fn wrap_nullable(ptr: *const $inner) -> Option<$name> {
                if (ptr == std::ptr::null()) {
                    return None;
                }
                Some(Self::wrap(ptr))
            }
            fn unwrap(self) -> *const $inner {
                let ptr = self.ptr;
                std::mem::forget(self);
                ptr
            }
            fn unwrap_nullable(of: Option<Self>) -> *const $inner {
                if (of.is_none()) {
                    return std::ptr::null();
                }
                of.unwrap().unwrap()
            }
        }
    };
}

macro_rules! impl_drop_clone {
    ($name:ident, $free:ident, $clone:ident) => {
        impl Drop for $name {
            fn drop(&mut self) {
                unsafe { $free(self.ptr); }
            }
        }

        impl Clone for $name {
            fn clone(&self) -> Self {
                unsafe { $name::wrap($clone(self.ptr)) }
            }
        }
    };
}

macro_rules! impl_ptr_holder {
    ($name:ident, $inner:ident, $free:ident, $clone:ident) => {
        impl PtrHolder for $name {
            fn ptr(&self) -> *const c_void {
                self.ptr as *const c_void
            }
            fn free_ptr() -> *const c_void {
                $free as *const c_void
            }
            fn clone_ptr() -> *const c_void {
                $clone as *const c_void
            }
            fn wrap(of: *const c_void) -> Self {
                $name::wrap(of as *const $inner)
            }
        }
    };
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
    fn wrap(ptr: *const _KString) -> Self {
        Self { ptr }
    }
    pub fn from(value: String) -> Self {
        unsafe {
            let length = value.chars().count() as i32;
            let size = value.len();
            let data = malloc(size) as *mut u8;
            std::ptr::copy_nonoverlapping(value.as_ptr(), data, size);
            Self { ptr: KString_new(data, length, size, true) }
        }
    }
    pub fn from_str(value: &str) -> Self {
        unsafe {
            let length = value.chars().count() as i32;
            let size = value.len();
            let data = value.as_ptr();
            Self { ptr: KString_new(data, length, size, false) }
        }
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
        value.to_string()
    }
}

impl Display for KString {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        Debug::fmt(&self.as_str(), f)
    }
}

impl_wrapper!(KString, _KString);
impl_drop_clone!(KString, KString_free, KString_clone);
impl_ptr_holder!(KString, _KString, KString_free, KString_clone);

extern "C" {
    fn KString_new(data: *const u8, length: i32, size: usize, is_data_owner: bool) -> *const _KString;
    fn KString_free(self_: *const _KString);
    fn KString_clone(self_: *const _KString) -> *const _KString;
}

#[macro_export] macro_rules! k_string {
    ($str:expr) => (KString::from($str));
}

// ╔════════════════╗
// ║     Arrays     ║
// ╚════════════════╝

#[repr(C)]
#[derive(Debug)]
struct _KArray {
    elements: *const c_void,
    size: usize,
    length: i32,
    __flags: i8
}

extern "C" {
    fn KArray_new(elements: *const *const c_void, length: i32, is_data_owner: bool) -> *const _KArray;
    fn KArray_clone(self_: *const _KArray, clone_op: *const c_void) -> *const _KArray;
    fn KArray_free(self_: *const _KArray, free_op: *const c_void);
}

macro_rules! impl_typed_array {
    ($name:ident, $rsType:ident, $size:expr, $new:ident, $free:ident, $clone:ident) => {
        pub struct $name {
            ptr: *const _KArray
        }

        impl $name {
            fn wrap(ptr: *const _KArray) -> Self {
                Self { ptr }
            }
            pub fn as_slice(&self) -> &[$rsType] {
                unsafe {
                    let arr = self.ptr.read();
                    std::slice::from_raw_parts(arr.elements as *const $rsType, arr.length as usize)
                }
            }
            pub fn from(array: Vec<$rsType>) -> Self {
                unsafe {
                    let length = array.len();
                    let size = length * $size;
                    let elements = malloc(size) as *mut $rsType;
                    std::ptr::copy_nonoverlapping(array.as_ptr(), elements, length);
                    Self { ptr: $new(elements, length as i32, size) }
                }
            }
        }
        extern "C" {
            fn $new(elements: *const $rsType, length: i32, size: usize) -> *const _KArray;
            fn $free(self_: *const _KArray);
            fn $clone(self_: *const _KArray) -> *const _KArray;
        }

        impl_wrapper!($name, _KArray);
        impl_drop_clone!($name, $free, $clone);
        impl_ptr_holder!($name, _KArray, $free, $clone);
    };
}

impl_typed_array!(KCharArray, u16, 2, KCharArray_new, KCharArray_free, KCharArray_clone);
impl_typed_array!(KBooleanArray, bool, 1, KBooleanArray_new, KBooleanArray_free, KBooleanArray_clone);
impl_typed_array!(KByteArray, i8, 1, KByteArray_new, KByteArray_free, KByteArray_clone);
impl_typed_array!(KUByteArray, u8, 1, KUByteArray_new, KUByteArray_free, KUByteArray_clone);
impl_typed_array!(KShortArray, i16, 2, KShortArray_new, KShortArray_free, KShortArray_clone);
impl_typed_array!(KUShortArray, u16, 2, KUShortArray_new, KUShortArray_free, KUShortArray_clone);
impl_typed_array!(KIntArray, i32, 4, KIntArray_new, KIntArray_free, KIntArray_clone);
impl_typed_array!(KUIntArray, u32, 4, KUIntArray_new, KUIntArray_free, KUIntArray_clone);
impl_typed_array!(KLongArray, i64, 8, KLongArray_new, KLongArray_free, KLongArray_clone);
impl_typed_array!(KULongArray, u64, 8, KULongArray_new, KULongArray_free, KULongArray_clone);
impl_typed_array!(KFloatArray, f32, 4, KFloatArray_new, KFloatArray_free, KFloatArray_clone);
impl_typed_array!(KDoubleArray, f64, 8, KDoubleArray_new, KDoubleArray_free, KDoubleArray_clone);

#[macro_export] macro_rules! k_char_array {
    ($($x:expr),+ $(,)?) => (KCharArray::from(vec![$($x),+]));
}
#[macro_export] macro_rules! k_boolean_array {
    ($($x:expr),+ $(,)?) => (KBooleanArray::from(vec![$($x),+]));
}
#[macro_export] macro_rules! k_byte_array {
    ($($x:expr),+ $(,)?) => (KByteArray::from(vec![$($x),+]));
}
#[macro_export] macro_rules! k_ubyte_array {
    ($($x:expr),+ $(,)?) => (KUByteArray::from(vec![$($x),+]));
}
#[macro_export] macro_rules! k_short_array {
    ($($x:expr),+ $(,)?) => (KShortArray::from(vec![$($x),+]));
}
#[macro_export] macro_rules! k_ushort_array {
    ($($x:expr),+ $(,)?) => (KUShortArray::from(vec![$($x),+]));
}
#[macro_export] macro_rules! k_int_array {
    ($($x:expr),+ $(,)?) => (KIntArray::from(vec![$($x),+]));
}
#[macro_export] macro_rules! k_uint_array {
    ($($x:expr),+ $(,)?) => (KUIntArray::from(vec![$($x),+]));
}
#[macro_export] macro_rules! k_long_array {
    ($($x:expr),+ $(,)?) => (KLongArray::from(vec![$($x),+]));
}
#[macro_export] macro_rules! k_ulong_array {
    ($($x:expr),+ $(,)?) => (KULongArray::from(vec![$($x),+]));
}
#[macro_export] macro_rules! k_float_array {
    ($($x:expr),+ $(,)?) => (KFloatArray::from(vec![$($x),+]));
}
#[macro_export] macro_rules! k_double_array {
    ($($x:expr),+ $(,)?) => (KDoubleArray::from(vec![$($x),+]));
}

// KArray

pub struct KArray<T: PtrHolder> {
    ptr: *const _KArray,
    elements: Vec<T>
}

impl<T: PtrHolder> KArray<T> {
    fn wrap(of: *const _KArray) -> KArray<T> {
        unsafe {
            let _of = of.read();
            let _elements = _of.elements as *const *const c_void;
            let mut elements: Vec<T> = Vec::with_capacity(_of.length as usize);

            for i in 0.._of.length {
                let el = T::wrap(_elements.offset(i as isize).read());
                elements.push(el)
            }
            KArray { ptr: of, elements }
        }
    }
    fn wrap_nullable(ptr: *const _KArray) -> Option<KArray<T>> {
        if (ptr == null()) {
            return None
        }
        Some(KArray::wrap(ptr))
    }
    fn unwrap(self) -> *const _KArray {
        let ptr = self.ptr;
        std::mem::forget(self);
        ptr
    }
    fn unwrap_nullable(of: Option<KArray<T>>) -> *const _KArray {
        if(of.is_none()) {
            return null()
        }
        of.unwrap().unwrap()
    }
    pub fn new(elements: Vec<T>) -> KArray<T> {
        let mut ptrs: Vec<*const c_void> = Vec::with_capacity(elements.len());
        for element in &elements {
            ptrs.push(element.ptr());
        }
        let ptr = unsafe { KArray_new(ptrs.as_ptr(), elements.len() as i32, true) };
        std::mem::forget(ptrs);
        KArray { ptr, elements }
    }
    pub fn as_slice(&self) -> &[T] {
        self.elements.as_slice()
    }
}

impl<T: PtrHolder> Drop for KArray<T> {
    fn drop(&mut self) {
        unsafe { KArray_free(self.ptr, T::free_ptr()); }
    }
}

impl<T: PtrHolder> Clone for KArray<T> {
    fn clone(&self) -> Self {
        KArray::wrap(unsafe { KArray_clone(self.ptr, T::clone_ptr()) })
    }
}

#[macro_export] macro_rules! k_array {
    ($($x:expr),+ $(,)?) => (KArray::new(vec![$($x),+]));
}

// KArrayOpt

pub struct KArrayOpt<T: PtrHolder> {
    ptr: *const _KArray,
    elements: Vec<Option<T>>
}

impl<T: PtrHolder> KArrayOpt<T> {
    fn wrap(of: *const _KArray) -> KArrayOpt<T> {
        unsafe {
            let _of = of.read();
            let _elements = _of.elements as *const *const c_void;
            let mut elements: Vec<Option<T>> = Vec::with_capacity(_of.length as usize);

            for i in 0.._of.length {
                let p = _elements.offset(i as isize).read();
                if(p == null()) {
                    elements.push(None)
                } else {
                    let el = T::wrap(_elements.offset(i as isize).read());
                    elements.push(Some(el))
                }
            }
            KArrayOpt { ptr: of, elements }
        }
    }
    fn wrap_nullable(ptr: *const _KArray) -> Option<KArrayOpt<T>> {
        if (ptr == null()) {
            return None
        }
        Some(KArrayOpt::wrap(ptr))
    }
    fn unwrap(self) -> *const _KArray {
        let ptr = self.ptr;
        std::mem::forget(self);
        ptr
    }
    fn unwrap_nullable(of: Option<KArrayOpt<T>>) -> *const _KArray {
        if(of.is_none()) {
            return null()
        }
        of.unwrap().unwrap()
    }
    pub fn new(elements: Vec<Option<T>>) -> KArrayOpt<T> {
        let mut ptrs: Vec<*const c_void> = Vec::with_capacity(elements.len());
        for element in &elements {
            if(element.is_none()) {
                ptrs.push(null())
            } else {
                ptrs.push(element.clone().unwrap().ptr());
            }
        }
        let ptr = unsafe { KArray_new(ptrs.as_ptr(), elements.len() as i32, true) };
        std::mem::forget(ptrs);
        KArrayOpt { ptr, elements }
    }
    pub fn as_slice(&self) -> &[Option<T>] {
        self.elements.as_slice()
    }
}

impl<T: PtrHolder> Drop for KArrayOpt<T> {
    fn drop(&mut self) {
        unsafe { KArray_free(self.ptr, T::free_ptr()); }
    }
}

impl<T: PtrHolder> Clone for KArrayOpt<T> {
    fn clone(&self) -> Self {
        KArrayOpt::wrap(unsafe { KArray_clone(self.ptr, T::clone_ptr()) })
    }
}

#[macro_export] macro_rules! k_array_opt {
    ($($x:expr),+ $(,)?) => (KArrayOpt::new(vec![$($x),+]));
}

// ╔═════════════════╗
// ║     Structs     ║
// ╚═════════════════╝

extern "C" {	
	fn ParentDictionary_new(a: i32, b: i32) -> *const _ParentDictionary;
	fn ParentDictionary_clone(self_: *const _ParentDictionary) -> *const _ParentDictionary;
	fn ParentDictionary_free(self_: *const _ParentDictionary);	
	fn MyDictionary_new(a: i32, b: i32, c: i32, d: i32) -> *const _MyDictionary;
	fn MyDictionary_clone(self_: *const _MyDictionary) -> *const _MyDictionary;
	fn MyDictionary_free(self_: *const _MyDictionary);	
	fn TypeDictionary_new(a1: u16, a2: bool, a3: i8, a4: u8, a5: i16, a6: u16, a7: i32, a8: u32, a9: i64, a10: u64, a11: f32, a12: f64, a13: *const _KString, a14: i32, a15: *const _MyDictionary, a16: *const _VoidCallback, a17: *const _KArray, a18: *const _KArray, a19: *const _KArray, a20: *const _KArray, a21: *const _KArray, a22: *const _KArray, a23: *const _KArray, a24: *const _KArray, a25: *const _KArray, a26: *const _KArray, a27: *const _KArray, a28: *const _KArray, a29: *const _KArray, a30: *const _KArray, a31: *const _KArray) -> *const _TypeDictionary;
	fn TypeDictionary_clone(self_: *const _TypeDictionary) -> *const _TypeDictionary;
	fn TypeDictionary_free(self_: *const _TypeDictionary);
}

#[repr(C)]
#[derive(Debug)]
struct _ParentDictionary {
	a: i32,
	b: i32,
	__flags: i8
}

pub struct ParentDictionary {
    ptr: *const _ParentDictionary,
	pub a: i32,
	pub b: i32,
}

impl ParentDictionary {
	pub fn new(a: i32, b: i32) -> Self {
		Self::wrap(unsafe { ParentDictionary_new(a, b) })
	}
	fn wrap(ptr: *const _ParentDictionary) -> Self {
	    let r = unsafe { ptr.read() };
	    Self { ptr, a: r.a, b: r.b }
	}
}

impl_wrapper!(ParentDictionary, _ParentDictionary);
impl_drop_clone!(ParentDictionary, ParentDictionary_free, ParentDictionary_clone);
impl_ptr_holder!(ParentDictionary, _ParentDictionary, ParentDictionary_free, ParentDictionary_clone);

#[repr(C)]
#[derive(Debug)]
struct _MyDictionary {
	a: i32,
	b: i32,
	c: i32,
	d: i32,
	__flags: i8
}

pub struct MyDictionary {
    ptr: *const _MyDictionary,
	pub a: i32,
	pub b: i32,
	pub c: i32,
	pub d: i32,
}

impl MyDictionary {
	pub fn new(a: i32, b: i32, c: i32, d: i32) -> Self {
		Self::wrap(unsafe { MyDictionary_new(a, b, c, d) })
	}
	fn wrap(ptr: *const _MyDictionary) -> Self {
	    let r = unsafe { ptr.read() };
	    Self { ptr, a: r.a, b: r.b, c: r.c, d: r.d }
	}
}

impl_wrapper!(MyDictionary, _MyDictionary);
impl_drop_clone!(MyDictionary, MyDictionary_free, MyDictionary_clone);
impl_ptr_holder!(MyDictionary, _MyDictionary, MyDictionary_free, MyDictionary_clone);

#[repr(C)]
#[derive(Debug)]
struct _TypeDictionary {
	a1: u16,
	a2: bool,
	a3: i8,
	a4: u8,
	a5: i16,
	a6: u16,
	a7: i32,
	a8: u32,
	a9: i64,
	a10: u64,
	a11: f32,
	a12: f64,
	a13: *const _KString,
	a14: i32,
	a15: *const _MyDictionary,
	a16: *const _VoidCallback,
	a17: *const _KArray,
	a18: *const _KArray,
	a19: *const _KArray,
	a20: *const _KArray,
	a21: *const _KArray,
	a22: *const _KArray,
	a23: *const _KArray,
	a24: *const _KArray,
	a25: *const _KArray,
	a26: *const _KArray,
	a27: *const _KArray,
	a28: *const _KArray,
	a29: *const _KArray,
	a30: *const _KArray,
	a31: *const _KArray,
	__flags: i8
}

pub struct TypeDictionary {
    ptr: *const _TypeDictionary,
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
	pub a13: KString,
	pub a14: MyEnum,
	pub a15: MyDictionary,
	pub a16: VoidCallback,
	pub a17: KCharArray,
	pub a18: KBooleanArray,
	pub a19: KByteArray,
	pub a20: KUByteArray,
	pub a21: KShortArray,
	pub a22: KUShortArray,
	pub a23: KIntArray,
	pub a24: KUIntArray,
	pub a25: KLongArray,
	pub a26: KULongArray,
	pub a27: KFloatArray,
	pub a28: KDoubleArray,
	pub a29: KArray<KString>,
	pub a30: KIntArray,
	pub a31: KArray<MyDictionary>,
}

impl TypeDictionary {
	pub fn new(a1: u16, a2: bool, a3: i8, a4: u8, a5: i16, a6: u16, a7: i32, a8: u32, a9: i64, a10: u64, a11: f32, a12: f64, a13: KString, a14: MyEnum, a15: MyDictionary, a16: VoidCallback, a17: KCharArray, a18: KBooleanArray, a19: KByteArray, a20: KUByteArray, a21: KShortArray, a22: KUShortArray, a23: KIntArray, a24: KUIntArray, a25: KLongArray, a26: KULongArray, a27: KFloatArray, a28: KDoubleArray, a29: KArray<KString>, a30: KIntArray, a31: KArray<MyDictionary>) -> Self {
		Self::wrap(unsafe { TypeDictionary_new(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, KString::unwrap(a13), MyEnum::to_int(a14), MyDictionary::unwrap(a15), VoidCallback::unwrap(a16), KCharArray::unwrap(a17), KBooleanArray::unwrap(a18), KByteArray::unwrap(a19), KUByteArray::unwrap(a20), KShortArray::unwrap(a21), KUShortArray::unwrap(a22), KIntArray::unwrap(a23), KUIntArray::unwrap(a24), KLongArray::unwrap(a25), KULongArray::unwrap(a26), KFloatArray::unwrap(a27), KDoubleArray::unwrap(a28), KArray::unwrap(a29), KIntArray::unwrap(a30), KArray::unwrap(a31)) })
	}
	fn wrap(ptr: *const _TypeDictionary) -> Self {
	    let r = unsafe { ptr.read() };
	    Self { ptr, a1: r.a1, a2: r.a2, a3: r.a3, a4: r.a4, a5: r.a5, a6: r.a6, a7: r.a7, a8: r.a8, a9: r.a9, a10: r.a10, a11: r.a11, a12: r.a12, a13: KString::wrap(r.a13), a14: MyEnum::from_int(r.a14), a15: MyDictionary::wrap(r.a15), a16: VoidCallback::wrap(r.a16), a17: KCharArray::wrap(r.a17), a18: KBooleanArray::wrap(r.a18), a19: KByteArray::wrap(r.a19), a20: KUByteArray::wrap(r.a20), a21: KShortArray::wrap(r.a21), a22: KUShortArray::wrap(r.a22), a23: KIntArray::wrap(r.a23), a24: KUIntArray::wrap(r.a24), a25: KLongArray::wrap(r.a25), a26: KULongArray::wrap(r.a26), a27: KFloatArray::wrap(r.a27), a28: KDoubleArray::wrap(r.a28), a29: KArray::wrap(r.a29), a30: KIntArray::wrap(r.a30), a31: KArray::wrap(r.a31) }
	}
}

impl_wrapper!(TypeDictionary, _TypeDictionary);
impl_drop_clone!(TypeDictionary, TypeDictionary_free, TypeDictionary_clone);
impl_ptr_holder!(TypeDictionary, _TypeDictionary, TypeDictionary_free, TypeDictionary_clone);

// ╔═══════════════════╗
// ║     Callbacks     ║
// ╚═══════════════════╝

macro_rules! impl_callback_base {
    ($name:ident, $inner:ident) => {
        impl $name {
            fn wrap(ptr: *const $inner) -> Self {
                $name { ptr }
            }
            pub fn equals(&self, other: Self) -> bool {
                unsafe { (self.ptr.read().equals)(self.ptr, other.ptr) }
            }
            pub fn hash_code(&self) -> i32 {
                unsafe { (self.ptr.read().hash_code)(self.ptr) }
            }
        }
        
        impl_wrapper!($name, $inner);
        
        impl Drop for $name {
            fn drop(&mut self) {
                unsafe { (self.ptr.read().free)(self.ptr) }
            }
        }
        
        impl Clone for $name {
            fn clone(&self) -> Self {
                unsafe { $name::wrap((self.ptr.read().clone)(self.ptr)) }
            }
        }
    };
}

#[repr(C)]
#[derive(Debug)]
struct _VoidCallback {
    __flags: i8,
	invoke: extern "C" fn(*const Self),
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct VoidCallback {
    ptr: *const _VoidCallback
}

impl VoidCallback {
	pub fn invoke(&self) {
		unsafe { (self.ptr.read().invoke)(self.ptr) }
	}
}

impl_callback_base!(VoidCallback, _VoidCallback);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassChar {
    __flags: i8,
	invoke: extern "C" fn(*const Self, u16) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassChar {
    ptr: *const _CallbackPassChar
}

impl CallbackPassChar {
	pub fn invoke(&self, arg: u16) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, arg) }
	}
}

impl_callback_base!(CallbackPassChar, _CallbackPassChar);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassBoolean {
    __flags: i8,
	invoke: extern "C" fn(*const Self, bool) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassBoolean {
    ptr: *const _CallbackPassBoolean
}

impl CallbackPassBoolean {
	pub fn invoke(&self, arg: bool) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, arg) }
	}
}

impl_callback_base!(CallbackPassBoolean, _CallbackPassBoolean);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassByte {
    __flags: i8,
	invoke: extern "C" fn(*const Self, i8) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassByte {
    ptr: *const _CallbackPassByte
}

impl CallbackPassByte {
	pub fn invoke(&self, arg: i8) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, arg) }
	}
}

impl_callback_base!(CallbackPassByte, _CallbackPassByte);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassUByte {
    __flags: i8,
	invoke: extern "C" fn(*const Self, u8) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassUByte {
    ptr: *const _CallbackPassUByte
}

impl CallbackPassUByte {
	pub fn invoke(&self, arg: u8) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, arg) }
	}
}

impl_callback_base!(CallbackPassUByte, _CallbackPassUByte);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassShort {
    __flags: i8,
	invoke: extern "C" fn(*const Self, i16) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassShort {
    ptr: *const _CallbackPassShort
}

impl CallbackPassShort {
	pub fn invoke(&self, arg: i16) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, arg) }
	}
}

impl_callback_base!(CallbackPassShort, _CallbackPassShort);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassUShort {
    __flags: i8,
	invoke: extern "C" fn(*const Self, u16) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassUShort {
    ptr: *const _CallbackPassUShort
}

impl CallbackPassUShort {
	pub fn invoke(&self, arg: u16) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, arg) }
	}
}

impl_callback_base!(CallbackPassUShort, _CallbackPassUShort);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassInt {
    __flags: i8,
	invoke: extern "C" fn(*const Self, i32) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassInt {
    ptr: *const _CallbackPassInt
}

impl CallbackPassInt {
	pub fn invoke(&self, arg: i32) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, arg) }
	}
}

impl_callback_base!(CallbackPassInt, _CallbackPassInt);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassUInt {
    __flags: i8,
	invoke: extern "C" fn(*const Self, u32) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassUInt {
    ptr: *const _CallbackPassUInt
}

impl CallbackPassUInt {
	pub fn invoke(&self, arg: u32) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, arg) }
	}
}

impl_callback_base!(CallbackPassUInt, _CallbackPassUInt);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassLong {
    __flags: i8,
	invoke: extern "C" fn(*const Self, i64) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassLong {
    ptr: *const _CallbackPassLong
}

impl CallbackPassLong {
	pub fn invoke(&self, arg: i64) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, arg) }
	}
}

impl_callback_base!(CallbackPassLong, _CallbackPassLong);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassULong {
    __flags: i8,
	invoke: extern "C" fn(*const Self, u64) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassULong {
    ptr: *const _CallbackPassULong
}

impl CallbackPassULong {
	pub fn invoke(&self, arg: u64) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, arg) }
	}
}

impl_callback_base!(CallbackPassULong, _CallbackPassULong);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassFloat {
    __flags: i8,
	invoke: extern "C" fn(*const Self, f32) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassFloat {
    ptr: *const _CallbackPassFloat
}

impl CallbackPassFloat {
	pub fn invoke(&self, arg: f32) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, arg) }
	}
}

impl_callback_base!(CallbackPassFloat, _CallbackPassFloat);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassDouble {
    __flags: i8,
	invoke: extern "C" fn(*const Self, f64) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassDouble {
    ptr: *const _CallbackPassDouble
}

impl CallbackPassDouble {
	pub fn invoke(&self, arg: f64) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, arg) }
	}
}

impl_callback_base!(CallbackPassDouble, _CallbackPassDouble);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassString {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KString) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassString {
    ptr: *const _CallbackPassString
}

impl CallbackPassString {
	pub fn invoke(&self, arg: KString) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KString::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassString, _CallbackPassString);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassStringN {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KString) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassStringN {
    ptr: *const _CallbackPassStringN
}

impl CallbackPassStringN {
	pub fn invoke(&self, arg: Option<KString>) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KString::unwrap_nullable(arg)) }
	}
}

impl_callback_base!(CallbackPassStringN, _CallbackPassStringN);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassCallback {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _VoidCallback) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassCallback {
    ptr: *const _CallbackPassCallback
}

impl CallbackPassCallback {
	pub fn invoke(&self, arg: VoidCallback) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, VoidCallback::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassCallback, _CallbackPassCallback);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassCallbackN {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _VoidCallback) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassCallbackN {
    ptr: *const _CallbackPassCallbackN
}

impl CallbackPassCallbackN {
	pub fn invoke(&self, arg: Option<VoidCallback>) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, VoidCallback::unwrap_nullable(arg)) }
	}
}

impl_callback_base!(CallbackPassCallbackN, _CallbackPassCallbackN);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassEnum {
    __flags: i8,
	invoke: extern "C" fn(*const Self, i32) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassEnum {
    ptr: *const _CallbackPassEnum
}

impl CallbackPassEnum {
	pub fn invoke(&self, arg: MyEnum) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, MyEnum::to_int(arg)) }
	}
}

impl_callback_base!(CallbackPassEnum, _CallbackPassEnum);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassDictionary {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _MyDictionary) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassDictionary {
    ptr: *const _CallbackPassDictionary
}

impl CallbackPassDictionary {
	pub fn invoke(&self, arg: MyDictionary) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, MyDictionary::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassDictionary, _CallbackPassDictionary);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassDictionaryN {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _MyDictionary) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassDictionaryN {
    ptr: *const _CallbackPassDictionaryN
}

impl CallbackPassDictionaryN {
	pub fn invoke(&self, arg: Option<MyDictionary>) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, MyDictionary::unwrap_nullable(arg)) }
	}
}

impl_callback_base!(CallbackPassDictionaryN, _CallbackPassDictionaryN);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnChar {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> u16,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnChar {
    ptr: *const _CallbackReturnChar
}

impl CallbackReturnChar {
	pub fn invoke(&self) -> u16 {
		unsafe { (self.ptr.read().invoke)(self.ptr) }
	}
}

impl_callback_base!(CallbackReturnChar, _CallbackReturnChar);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnBoolean {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnBoolean {
    ptr: *const _CallbackReturnBoolean
}

impl CallbackReturnBoolean {
	pub fn invoke(&self) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr) }
	}
}

impl_callback_base!(CallbackReturnBoolean, _CallbackReturnBoolean);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnByte {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> i8,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnByte {
    ptr: *const _CallbackReturnByte
}

impl CallbackReturnByte {
	pub fn invoke(&self) -> i8 {
		unsafe { (self.ptr.read().invoke)(self.ptr) }
	}
}

impl_callback_base!(CallbackReturnByte, _CallbackReturnByte);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnUByte {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> u8,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnUByte {
    ptr: *const _CallbackReturnUByte
}

impl CallbackReturnUByte {
	pub fn invoke(&self) -> u8 {
		unsafe { (self.ptr.read().invoke)(self.ptr) }
	}
}

impl_callback_base!(CallbackReturnUByte, _CallbackReturnUByte);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnShort {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> i16,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnShort {
    ptr: *const _CallbackReturnShort
}

impl CallbackReturnShort {
	pub fn invoke(&self) -> i16 {
		unsafe { (self.ptr.read().invoke)(self.ptr) }
	}
}

impl_callback_base!(CallbackReturnShort, _CallbackReturnShort);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnUShort {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> u16,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnUShort {
    ptr: *const _CallbackReturnUShort
}

impl CallbackReturnUShort {
	pub fn invoke(&self) -> u16 {
		unsafe { (self.ptr.read().invoke)(self.ptr) }
	}
}

impl_callback_base!(CallbackReturnUShort, _CallbackReturnUShort);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnInt {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> i32,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnInt {
    ptr: *const _CallbackReturnInt
}

impl CallbackReturnInt {
	pub fn invoke(&self) -> i32 {
		unsafe { (self.ptr.read().invoke)(self.ptr) }
	}
}

impl_callback_base!(CallbackReturnInt, _CallbackReturnInt);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnUInt {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> u32,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnUInt {
    ptr: *const _CallbackReturnUInt
}

impl CallbackReturnUInt {
	pub fn invoke(&self) -> u32 {
		unsafe { (self.ptr.read().invoke)(self.ptr) }
	}
}

impl_callback_base!(CallbackReturnUInt, _CallbackReturnUInt);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnLong {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> i64,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnLong {
    ptr: *const _CallbackReturnLong
}

impl CallbackReturnLong {
	pub fn invoke(&self) -> i64 {
		unsafe { (self.ptr.read().invoke)(self.ptr) }
	}
}

impl_callback_base!(CallbackReturnLong, _CallbackReturnLong);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnULong {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> u64,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnULong {
    ptr: *const _CallbackReturnULong
}

impl CallbackReturnULong {
	pub fn invoke(&self) -> u64 {
		unsafe { (self.ptr.read().invoke)(self.ptr) }
	}
}

impl_callback_base!(CallbackReturnULong, _CallbackReturnULong);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnFloat {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> f32,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnFloat {
    ptr: *const _CallbackReturnFloat
}

impl CallbackReturnFloat {
	pub fn invoke(&self) -> f32 {
		unsafe { (self.ptr.read().invoke)(self.ptr) }
	}
}

impl_callback_base!(CallbackReturnFloat, _CallbackReturnFloat);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnDouble {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> f64,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnDouble {
    ptr: *const _CallbackReturnDouble
}

impl CallbackReturnDouble {
	pub fn invoke(&self) -> f64 {
		unsafe { (self.ptr.read().invoke)(self.ptr) }
	}
}

impl_callback_base!(CallbackReturnDouble, _CallbackReturnDouble);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnString {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KString,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnString {
    ptr: *const _CallbackReturnString
}

impl CallbackReturnString {
	pub fn invoke(&self) -> KString {
		unsafe { KString::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnString, _CallbackReturnString);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnStringN {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KString,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnStringN {
    ptr: *const _CallbackReturnStringN
}

impl CallbackReturnStringN {
	pub fn invoke(&self) -> Option<KString> {
		unsafe { KString::wrap_nullable((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnStringN, _CallbackReturnStringN);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnCallback {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _VoidCallback,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnCallback {
    ptr: *const _CallbackReturnCallback
}

impl CallbackReturnCallback {
	pub fn invoke(&self) -> VoidCallback {
		unsafe { VoidCallback::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnCallback, _CallbackReturnCallback);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnCallbackN {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _VoidCallback,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnCallbackN {
    ptr: *const _CallbackReturnCallbackN
}

impl CallbackReturnCallbackN {
	pub fn invoke(&self) -> Option<VoidCallback> {
		unsafe { VoidCallback::wrap_nullable((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnCallbackN, _CallbackReturnCallbackN);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnEnum {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> i32,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnEnum {
    ptr: *const _CallbackReturnEnum
}

impl CallbackReturnEnum {
	pub fn invoke(&self) -> MyEnum {
		unsafe { MyEnum::from_int((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnEnum, _CallbackReturnEnum);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnDictionary {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _MyDictionary,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnDictionary {
    ptr: *const _CallbackReturnDictionary
}

impl CallbackReturnDictionary {
	pub fn invoke(&self) -> MyDictionary {
		unsafe { MyDictionary::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnDictionary, _CallbackReturnDictionary);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnDictionaryN {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _MyDictionary,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnDictionaryN {
    ptr: *const _CallbackReturnDictionaryN
}

impl CallbackReturnDictionaryN {
	pub fn invoke(&self) -> Option<MyDictionary> {
		unsafe { MyDictionary::wrap_nullable((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnDictionaryN, _CallbackReturnDictionaryN);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassCharArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassCharArray {
    ptr: *const _CallbackPassCharArray
}

impl CallbackPassCharArray {
	pub fn invoke(&self, arg: KCharArray) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KCharArray::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassCharArray, _CallbackPassCharArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassCharArrayN {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassCharArrayN {
    ptr: *const _CallbackPassCharArrayN
}

impl CallbackPassCharArrayN {
	pub fn invoke(&self, arg: Option<KCharArray>) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KCharArray::unwrap_nullable(arg)) }
	}
}

impl_callback_base!(CallbackPassCharArrayN, _CallbackPassCharArrayN);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassBooleanArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassBooleanArray {
    ptr: *const _CallbackPassBooleanArray
}

impl CallbackPassBooleanArray {
	pub fn invoke(&self, arg: KBooleanArray) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KBooleanArray::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassBooleanArray, _CallbackPassBooleanArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassByteArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassByteArray {
    ptr: *const _CallbackPassByteArray
}

impl CallbackPassByteArray {
	pub fn invoke(&self, arg: KByteArray) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KByteArray::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassByteArray, _CallbackPassByteArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassUByteArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassUByteArray {
    ptr: *const _CallbackPassUByteArray
}

impl CallbackPassUByteArray {
	pub fn invoke(&self, arg: KUByteArray) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KUByteArray::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassUByteArray, _CallbackPassUByteArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassShortArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassShortArray {
    ptr: *const _CallbackPassShortArray
}

impl CallbackPassShortArray {
	pub fn invoke(&self, arg: KShortArray) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KShortArray::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassShortArray, _CallbackPassShortArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassUShortArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassUShortArray {
    ptr: *const _CallbackPassUShortArray
}

impl CallbackPassUShortArray {
	pub fn invoke(&self, arg: KUShortArray) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KUShortArray::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassUShortArray, _CallbackPassUShortArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassIntArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassIntArray {
    ptr: *const _CallbackPassIntArray
}

impl CallbackPassIntArray {
	pub fn invoke(&self, arg: KIntArray) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KIntArray::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassIntArray, _CallbackPassIntArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassUIntArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassUIntArray {
    ptr: *const _CallbackPassUIntArray
}

impl CallbackPassUIntArray {
	pub fn invoke(&self, arg: KUIntArray) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KUIntArray::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassUIntArray, _CallbackPassUIntArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassLongArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassLongArray {
    ptr: *const _CallbackPassLongArray
}

impl CallbackPassLongArray {
	pub fn invoke(&self, arg: KLongArray) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KLongArray::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassLongArray, _CallbackPassLongArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassULongArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassULongArray {
    ptr: *const _CallbackPassULongArray
}

impl CallbackPassULongArray {
	pub fn invoke(&self, arg: KULongArray) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KULongArray::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassULongArray, _CallbackPassULongArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassFloatArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassFloatArray {
    ptr: *const _CallbackPassFloatArray
}

impl CallbackPassFloatArray {
	pub fn invoke(&self, arg: KFloatArray) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KFloatArray::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassFloatArray, _CallbackPassFloatArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassDoubleArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassDoubleArray {
    ptr: *const _CallbackPassDoubleArray
}

impl CallbackPassDoubleArray {
	pub fn invoke(&self, arg: KDoubleArray) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KDoubleArray::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassDoubleArray, _CallbackPassDoubleArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassStringArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassStringArray {
    ptr: *const _CallbackPassStringArray
}

impl CallbackPassStringArray {
	pub fn invoke(&self, arg: KArray<KString>) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KArray::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassStringArray, _CallbackPassStringArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassStringArrayN {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassStringArrayN {
    ptr: *const _CallbackPassStringArrayN
}

impl CallbackPassStringArrayN {
	pub fn invoke(&self, arg: KArrayOpt<KString>) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KArrayOpt::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassStringArrayN, _CallbackPassStringArrayN);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassEnumArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassEnumArray {
    ptr: *const _CallbackPassEnumArray
}

impl CallbackPassEnumArray {
	pub fn invoke(&self, arg: KIntArray) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KIntArray::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassEnumArray, _CallbackPassEnumArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassDictionaryArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassDictionaryArray {
    ptr: *const _CallbackPassDictionaryArray
}

impl CallbackPassDictionaryArray {
	pub fn invoke(&self, arg: KArray<MyDictionary>) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KArray::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassDictionaryArray, _CallbackPassDictionaryArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackPassDictionaryArrayN {
    __flags: i8,
	invoke: extern "C" fn(*const Self, *const _KArray) -> bool,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackPassDictionaryArrayN {
    ptr: *const _CallbackPassDictionaryArrayN
}

impl CallbackPassDictionaryArrayN {
	pub fn invoke(&self, arg: KArrayOpt<MyDictionary>) -> bool {
		unsafe { (self.ptr.read().invoke)(self.ptr, KArrayOpt::unwrap(arg)) }
	}
}

impl_callback_base!(CallbackPassDictionaryArrayN, _CallbackPassDictionaryArrayN);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnCharArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnCharArray {
    ptr: *const _CallbackReturnCharArray
}

impl CallbackReturnCharArray {
	pub fn invoke(&self) -> KCharArray {
		unsafe { KCharArray::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnCharArray, _CallbackReturnCharArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnCharArrayN {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnCharArrayN {
    ptr: *const _CallbackReturnCharArrayN
}

impl CallbackReturnCharArrayN {
	pub fn invoke(&self) -> Option<KCharArray> {
		unsafe { KCharArray::wrap_nullable((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnCharArrayN, _CallbackReturnCharArrayN);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnBooleanArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnBooleanArray {
    ptr: *const _CallbackReturnBooleanArray
}

impl CallbackReturnBooleanArray {
	pub fn invoke(&self) -> KBooleanArray {
		unsafe { KBooleanArray::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnBooleanArray, _CallbackReturnBooleanArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnByteArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnByteArray {
    ptr: *const _CallbackReturnByteArray
}

impl CallbackReturnByteArray {
	pub fn invoke(&self) -> KByteArray {
		unsafe { KByteArray::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnByteArray, _CallbackReturnByteArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnUByteArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnUByteArray {
    ptr: *const _CallbackReturnUByteArray
}

impl CallbackReturnUByteArray {
	pub fn invoke(&self) -> KUByteArray {
		unsafe { KUByteArray::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnUByteArray, _CallbackReturnUByteArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnShortArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnShortArray {
    ptr: *const _CallbackReturnShortArray
}

impl CallbackReturnShortArray {
	pub fn invoke(&self) -> KShortArray {
		unsafe { KShortArray::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnShortArray, _CallbackReturnShortArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnUShortArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnUShortArray {
    ptr: *const _CallbackReturnUShortArray
}

impl CallbackReturnUShortArray {
	pub fn invoke(&self) -> KUShortArray {
		unsafe { KUShortArray::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnUShortArray, _CallbackReturnUShortArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnIntArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnIntArray {
    ptr: *const _CallbackReturnIntArray
}

impl CallbackReturnIntArray {
	pub fn invoke(&self) -> KIntArray {
		unsafe { KIntArray::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnIntArray, _CallbackReturnIntArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnUIntArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnUIntArray {
    ptr: *const _CallbackReturnUIntArray
}

impl CallbackReturnUIntArray {
	pub fn invoke(&self) -> KUIntArray {
		unsafe { KUIntArray::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnUIntArray, _CallbackReturnUIntArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnLongArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnLongArray {
    ptr: *const _CallbackReturnLongArray
}

impl CallbackReturnLongArray {
	pub fn invoke(&self) -> KLongArray {
		unsafe { KLongArray::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnLongArray, _CallbackReturnLongArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnULongArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnULongArray {
    ptr: *const _CallbackReturnULongArray
}

impl CallbackReturnULongArray {
	pub fn invoke(&self) -> KULongArray {
		unsafe { KULongArray::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnULongArray, _CallbackReturnULongArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnFloatArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnFloatArray {
    ptr: *const _CallbackReturnFloatArray
}

impl CallbackReturnFloatArray {
	pub fn invoke(&self) -> KFloatArray {
		unsafe { KFloatArray::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnFloatArray, _CallbackReturnFloatArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnDoubleArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnDoubleArray {
    ptr: *const _CallbackReturnDoubleArray
}

impl CallbackReturnDoubleArray {
	pub fn invoke(&self) -> KDoubleArray {
		unsafe { KDoubleArray::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnDoubleArray, _CallbackReturnDoubleArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnStringArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnStringArray {
    ptr: *const _CallbackReturnStringArray
}

impl CallbackReturnStringArray {
	pub fn invoke(&self) -> KArray<KString> {
		unsafe { KArray::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnStringArray, _CallbackReturnStringArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnStringArrayN {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnStringArrayN {
    ptr: *const _CallbackReturnStringArrayN
}

impl CallbackReturnStringArrayN {
	pub fn invoke(&self) -> KArrayOpt<KString> {
		unsafe { KArrayOpt::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnStringArrayN, _CallbackReturnStringArrayN);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnEnumArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnEnumArray {
    ptr: *const _CallbackReturnEnumArray
}

impl CallbackReturnEnumArray {
	pub fn invoke(&self) -> KIntArray {
		unsafe { KIntArray::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnEnumArray, _CallbackReturnEnumArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnDictionaryArray {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnDictionaryArray {
    ptr: *const _CallbackReturnDictionaryArray
}

impl CallbackReturnDictionaryArray {
	pub fn invoke(&self) -> KArray<MyDictionary> {
		unsafe { KArray::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnDictionaryArray, _CallbackReturnDictionaryArray);

#[repr(C)]
#[derive(Debug)]
struct _CallbackReturnDictionaryArrayN {
    __flags: i8,
	invoke: extern "C" fn(*const Self) -> *const _KArray,
    clone: extern "C" fn(*const Self) -> *const Self,
    equals: extern "C" fn(*const Self, *const Self) -> bool,
    hash_code: extern "C" fn(*const Self) -> i32,
    free: extern "C" fn(*const Self),
}

pub struct CallbackReturnDictionaryArrayN {
    ptr: *const _CallbackReturnDictionaryArrayN
}

impl CallbackReturnDictionaryArrayN {
	pub fn invoke(&self) -> KArrayOpt<MyDictionary> {
		unsafe { KArrayOpt::wrap((self.ptr.read().invoke)(self.ptr)) }
	}
}

impl_callback_base!(CallbackReturnDictionaryArrayN, _CallbackReturnDictionaryArrayN);

// ╔═══════════════╗
// ║     Enums     ║
// ╚═══════════════╝

#[derive(PartialEq, Eq)]
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
    pub fn to_int(self) -> i32 {
        self as i32
    }
}


// ╔═══════════════════╗
// ║     Functions     ║
// ╚═══════════════════╝

#[no_mangle]
extern "C" fn pass_void() -> bool {
	crate::pass_void()
}

#[no_mangle]
extern "C" fn pass_char(
	arg: u16
) -> bool {
	crate::pass_char(arg)
}

#[no_mangle]
extern "C" fn pass_boolean(
	arg: bool
) -> bool {
	crate::pass_boolean(arg)
}

#[no_mangle]
extern "C" fn pass_byte(
	arg: i8
) -> bool {
	crate::pass_byte(arg)
}

#[no_mangle]
extern "C" fn pass_ubyte(
	arg: u8
) -> bool {
	crate::pass_ubyte(arg)
}

#[no_mangle]
extern "C" fn pass_short(
	arg: i16
) -> bool {
	crate::pass_short(arg)
}

#[no_mangle]
extern "C" fn pass_ushort(
	arg: u16
) -> bool {
	crate::pass_ushort(arg)
}

#[no_mangle]
extern "C" fn pass_int(
	arg: i32
) -> bool {
	crate::pass_int(arg)
}

#[no_mangle]
extern "C" fn pass_uint(
	arg: u32
) -> bool {
	crate::pass_uint(arg)
}

#[no_mangle]
extern "C" fn pass_long(
	arg: i64
) -> bool {
	crate::pass_long(arg)
}

#[no_mangle]
extern "C" fn pass_ulong(
	arg: u64
) -> bool {
	crate::pass_ulong(arg)
}

#[no_mangle]
extern "C" fn pass_float(
	arg: f32
) -> bool {
	crate::pass_float(arg)
}

#[no_mangle]
extern "C" fn pass_double(
	arg: f64
) -> bool {
	crate::pass_double(arg)
}

#[no_mangle]
extern "C" fn pass_string(
	arg: *const _KString
) -> bool {
	crate::pass_string(KString::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_string_n(
	arg: *const _KString
) -> bool {
	crate::pass_string_n(KString::wrap_nullable(arg))
}

#[no_mangle]
extern "C" fn pass_enum(
	arg: i32
) -> bool {
	crate::pass_enum(MyEnum::from_int(arg))
}

#[no_mangle]
extern "C" fn pass_dictionary(
	arg: *const _MyDictionary
) -> bool {
	crate::pass_dictionary(MyDictionary::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_dictionary_n(
	arg: *const _MyDictionary
) -> bool {
	crate::pass_dictionary_n(MyDictionary::wrap_nullable(arg))
}

#[no_mangle]
extern "C" fn return_void() {
	crate::return_void()
}

#[no_mangle]
extern "C" fn return_char() -> u16 {
	crate::return_char()
}

#[no_mangle]
extern "C" fn return_boolean() -> bool {
	crate::return_boolean()
}

#[no_mangle]
extern "C" fn return_byte() -> i8 {
	crate::return_byte()
}

#[no_mangle]
extern "C" fn return_ubyte() -> u8 {
	crate::return_ubyte()
}

#[no_mangle]
extern "C" fn return_short() -> i16 {
	crate::return_short()
}

#[no_mangle]
extern "C" fn return_ushort() -> u16 {
	crate::return_ushort()
}

#[no_mangle]
extern "C" fn return_int() -> i32 {
	crate::return_int()
}

#[no_mangle]
extern "C" fn return_uint() -> u32 {
	crate::return_uint()
}

#[no_mangle]
extern "C" fn return_long() -> i64 {
	crate::return_long()
}

#[no_mangle]
extern "C" fn return_ulong() -> u64 {
	crate::return_ulong()
}

#[no_mangle]
extern "C" fn return_float() -> f32 {
	crate::return_float()
}

#[no_mangle]
extern "C" fn return_double() -> f64 {
	crate::return_double()
}

#[no_mangle]
extern "C" fn return_string() -> *const _KString {
	KString::unwrap(crate::return_string())
}

#[no_mangle]
extern "C" fn return_string_n() -> *const _KString {
	KString::unwrap_nullable(crate::return_string_n())
}

#[no_mangle]
extern "C" fn return_enum() -> i32 {
	MyEnum::to_int(crate::return_enum())
}

#[no_mangle]
extern "C" fn return_dictionary() -> *const _MyDictionary {
	MyDictionary::unwrap(crate::return_dictionary())
}

#[no_mangle]
extern "C" fn return_dictionary_n() -> *const _MyDictionary {
	MyDictionary::unwrap_nullable(crate::return_dictionary_n())
}

#[no_mangle]
extern "C" fn ping_char(
	arg: u16
) -> u16 {
	crate::ping_char(arg)
}

#[no_mangle]
extern "C" fn ping_boolean(
	arg: bool
) -> bool {
	crate::ping_boolean(arg)
}

#[no_mangle]
extern "C" fn ping_byte(
	arg: i8
) -> i8 {
	crate::ping_byte(arg)
}

#[no_mangle]
extern "C" fn ping_ubyte(
	arg: u8
) -> u8 {
	crate::ping_ubyte(arg)
}

#[no_mangle]
extern "C" fn ping_short(
	arg: i16
) -> i16 {
	crate::ping_short(arg)
}

#[no_mangle]
extern "C" fn ping_ushort(
	arg: u16
) -> u16 {
	crate::ping_ushort(arg)
}

#[no_mangle]
extern "C" fn ping_int(
	arg: i32
) -> i32 {
	crate::ping_int(arg)
}

#[no_mangle]
extern "C" fn ping_uint(
	arg: u32
) -> u32 {
	crate::ping_uint(arg)
}

#[no_mangle]
extern "C" fn ping_long(
	arg: i64
) -> i64 {
	crate::ping_long(arg)
}

#[no_mangle]
extern "C" fn ping_ulong(
	arg: u64
) -> u64 {
	crate::ping_ulong(arg)
}

#[no_mangle]
extern "C" fn ping_float(
	arg: f32
) -> f32 {
	crate::ping_float(arg)
}

#[no_mangle]
extern "C" fn ping_double(
	arg: f64
) -> f64 {
	crate::ping_double(arg)
}

#[no_mangle]
extern "C" fn ping_string(
	arg: *const _KString
) -> *const _KString {
	KString::unwrap(crate::ping_string(KString::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_string_n(
	arg: *const _KString
) -> *const _KString {
	KString::unwrap_nullable(crate::ping_string_n(KString::wrap_nullable(arg)))
}

#[no_mangle]
extern "C" fn ping_enum(
	arg: i32
) -> i32 {
	MyEnum::to_int(crate::ping_enum(MyEnum::from_int(arg)))
}

#[no_mangle]
extern "C" fn ping_dictionary(
	arg: *const _MyDictionary
) -> *const _MyDictionary {
	MyDictionary::unwrap(crate::ping_dictionary(MyDictionary::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_dictionary_n(
	arg: *const _MyDictionary
) -> *const _MyDictionary {
	MyDictionary::unwrap_nullable(crate::ping_dictionary_n(MyDictionary::wrap_nullable(arg)))
}

#[no_mangle]
extern "C" fn callback_void(
	arg: *const _VoidCallback
) {
	crate::callback_void(VoidCallback::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_void_n(
	arg: *const _VoidCallback
) -> bool {
	crate::callback_void_n(VoidCallback::wrap_nullable(arg))
}

#[no_mangle]
extern "C" fn callback_arg_char(
	arg: *const _CallbackPassChar
) -> bool {
	crate::callback_arg_char(CallbackPassChar::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_boolean(
	arg: *const _CallbackPassBoolean
) -> bool {
	crate::callback_arg_boolean(CallbackPassBoolean::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_byte(
	arg: *const _CallbackPassByte
) -> bool {
	crate::callback_arg_byte(CallbackPassByte::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_ubyte(
	arg: *const _CallbackPassUByte
) -> bool {
	crate::callback_arg_ubyte(CallbackPassUByte::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_short(
	arg: *const _CallbackPassShort
) -> bool {
	crate::callback_arg_short(CallbackPassShort::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_ushort(
	arg: *const _CallbackPassUShort
) -> bool {
	crate::callback_arg_ushort(CallbackPassUShort::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_int(
	arg: *const _CallbackPassInt
) -> bool {
	crate::callback_arg_int(CallbackPassInt::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_uint(
	arg: *const _CallbackPassUInt
) -> bool {
	crate::callback_arg_uint(CallbackPassUInt::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_long(
	arg: *const _CallbackPassLong
) -> bool {
	crate::callback_arg_long(CallbackPassLong::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_ulong(
	arg: *const _CallbackPassULong
) -> bool {
	crate::callback_arg_ulong(CallbackPassULong::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_float(
	arg: *const _CallbackPassFloat
) -> bool {
	crate::callback_arg_float(CallbackPassFloat::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_double(
	arg: *const _CallbackPassDouble
) -> bool {
	crate::callback_arg_double(CallbackPassDouble::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_string(
	arg: *const _CallbackPassString
) -> bool {
	crate::callback_arg_string(CallbackPassString::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_string_n(
	arg: *const _CallbackPassStringN
) -> bool {
	crate::callback_arg_string_n(CallbackPassStringN::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_callback(
	pass: *const _VoidCallback,
	arg: *const _CallbackPassCallback
) -> bool {
	crate::callback_arg_callback(VoidCallback::wrap(pass), CallbackPassCallback::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_callback_n(
	arg: *const _CallbackPassCallbackN
) -> bool {
	crate::callback_arg_callback_n(CallbackPassCallbackN::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_enum(
	arg: *const _CallbackPassEnum
) -> bool {
	crate::callback_arg_enum(CallbackPassEnum::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_dictionary(
	arg: *const _CallbackPassDictionary
) -> bool {
	crate::callback_arg_dictionary(CallbackPassDictionary::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_dictionary_n(
	arg: *const _CallbackPassDictionaryN
) -> bool {
	crate::callback_arg_dictionary_n(CallbackPassDictionaryN::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_char(
	arg: *const _CallbackReturnChar
) -> bool {
	crate::callback_return_char(CallbackReturnChar::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_boolean(
	arg: *const _CallbackReturnBoolean
) -> bool {
	crate::callback_return_boolean(CallbackReturnBoolean::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_byte(
	arg: *const _CallbackReturnByte
) -> bool {
	crate::callback_return_byte(CallbackReturnByte::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_ubyte(
	arg: *const _CallbackReturnUByte
) -> bool {
	crate::callback_return_ubyte(CallbackReturnUByte::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_short(
	arg: *const _CallbackReturnShort
) -> bool {
	crate::callback_return_short(CallbackReturnShort::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_ushort(
	arg: *const _CallbackReturnUShort
) -> bool {
	crate::callback_return_ushort(CallbackReturnUShort::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_int(
	arg: *const _CallbackReturnInt
) -> bool {
	crate::callback_return_int(CallbackReturnInt::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_uint(
	arg: *const _CallbackReturnUInt
) -> bool {
	crate::callback_return_uint(CallbackReturnUInt::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_long(
	arg: *const _CallbackReturnLong
) -> bool {
	crate::callback_return_long(CallbackReturnLong::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_ulong(
	arg: *const _CallbackReturnULong
) -> bool {
	crate::callback_return_ulong(CallbackReturnULong::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_float(
	arg: *const _CallbackReturnFloat
) -> bool {
	crate::callback_return_float(CallbackReturnFloat::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_double(
	arg: *const _CallbackReturnDouble
) -> bool {
	crate::callback_return_double(CallbackReturnDouble::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_string(
	arg: *const _CallbackReturnString
) -> bool {
	crate::callback_return_string(CallbackReturnString::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_string_n(
	arg: *const _CallbackReturnStringN
) -> bool {
	crate::callback_return_string_n(CallbackReturnStringN::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_callback(
	arg: *const _CallbackReturnCallback
) -> *const _VoidCallback {
	VoidCallback::unwrap(crate::callback_return_callback(CallbackReturnCallback::wrap(arg)))
}

#[no_mangle]
extern "C" fn callback_return_callback_n(
	arg: *const _CallbackReturnCallbackN
) -> bool {
	crate::callback_return_callback_n(CallbackReturnCallbackN::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_enum(
	arg: *const _CallbackReturnEnum
) -> bool {
	crate::callback_return_enum(CallbackReturnEnum::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_dictionary(
	arg: *const _CallbackReturnDictionary
) -> bool {
	crate::callback_return_dictionary(CallbackReturnDictionary::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_dictionary_n(
	arg: *const _CallbackReturnDictionaryN
) -> bool {
	crate::callback_return_dictionary_n(CallbackReturnDictionaryN::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_char_array(
	arg: *const _KArray
) -> bool {
	crate::pass_char_array(KCharArray::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_char_array_n(
	arg: *const _KArray
) -> bool {
	crate::pass_char_array_n(KCharArray::wrap_nullable(arg))
}

#[no_mangle]
extern "C" fn pass_boolean_array(
	arg: *const _KArray
) -> bool {
	crate::pass_boolean_array(KBooleanArray::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_byte_array(
	arg: *const _KArray
) -> bool {
	crate::pass_byte_array(KByteArray::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_ubyte_array(
	arg: *const _KArray
) -> bool {
	crate::pass_ubyte_array(KUByteArray::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_short_array(
	arg: *const _KArray
) -> bool {
	crate::pass_short_array(KShortArray::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_ushort_array(
	arg: *const _KArray
) -> bool {
	crate::pass_ushort_array(KUShortArray::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_int_array(
	arg: *const _KArray
) -> bool {
	crate::pass_int_array(KIntArray::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_uint_array(
	arg: *const _KArray
) -> bool {
	crate::pass_uint_array(KUIntArray::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_long_array(
	arg: *const _KArray
) -> bool {
	crate::pass_long_array(KLongArray::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_ulong_array(
	arg: *const _KArray
) -> bool {
	crate::pass_ulong_array(KULongArray::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_float_array(
	arg: *const _KArray
) -> bool {
	crate::pass_float_array(KFloatArray::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_double_array(
	arg: *const _KArray
) -> bool {
	crate::pass_double_array(KDoubleArray::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_string_array(
	arg: *const _KArray
) -> bool {
	crate::pass_string_array(KArray::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_string_array_n(
	arg: *const _KArray
) -> bool {
	crate::pass_string_array_n(KArrayOpt::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_enum_array(
	arg: *const _KArray
) -> bool {
	crate::pass_enum_array(KIntArray::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_dictionary_array(
	arg: *const _KArray
) -> bool {
	crate::pass_dictionary_array(KArray::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_dictionary_array_n(
	arg: *const _KArray
) -> bool {
	crate::pass_dictionary_array_n(KArrayOpt::wrap(arg))
}

#[no_mangle]
extern "C" fn return_char_array() -> *const _KArray {
	KCharArray::unwrap(crate::return_char_array())
}

#[no_mangle]
extern "C" fn return_char_array_n() -> *const _KArray {
	KCharArray::unwrap_nullable(crate::return_char_array_n())
}

#[no_mangle]
extern "C" fn return_boolean_array() -> *const _KArray {
	KBooleanArray::unwrap(crate::return_boolean_array())
}

#[no_mangle]
extern "C" fn return_byte_array() -> *const _KArray {
	KByteArray::unwrap(crate::return_byte_array())
}

#[no_mangle]
extern "C" fn return_ubyte_array() -> *const _KArray {
	KUByteArray::unwrap(crate::return_ubyte_array())
}

#[no_mangle]
extern "C" fn return_short_array() -> *const _KArray {
	KShortArray::unwrap(crate::return_short_array())
}

#[no_mangle]
extern "C" fn return_ushort_array() -> *const _KArray {
	KUShortArray::unwrap(crate::return_ushort_array())
}

#[no_mangle]
extern "C" fn return_int_array() -> *const _KArray {
	KIntArray::unwrap(crate::return_int_array())
}

#[no_mangle]
extern "C" fn return_uint_array() -> *const _KArray {
	KUIntArray::unwrap(crate::return_uint_array())
}

#[no_mangle]
extern "C" fn return_long_array() -> *const _KArray {
	KLongArray::unwrap(crate::return_long_array())
}

#[no_mangle]
extern "C" fn return_ulong_array() -> *const _KArray {
	KULongArray::unwrap(crate::return_ulong_array())
}

#[no_mangle]
extern "C" fn return_float_array() -> *const _KArray {
	KFloatArray::unwrap(crate::return_float_array())
}

#[no_mangle]
extern "C" fn return_double_array() -> *const _KArray {
	KDoubleArray::unwrap(crate::return_double_array())
}

#[no_mangle]
extern "C" fn return_string_array() -> *const _KArray {
	KArray::unwrap(crate::return_string_array())
}

#[no_mangle]
extern "C" fn return_string_array_n() -> *const _KArray {
	KArrayOpt::unwrap(crate::return_string_array_n())
}

#[no_mangle]
extern "C" fn return_enum_array() -> *const _KArray {
	KIntArray::unwrap(crate::return_enum_array())
}

#[no_mangle]
extern "C" fn return_dictionary_array() -> *const _KArray {
	KArray::unwrap(crate::return_dictionary_array())
}

#[no_mangle]
extern "C" fn return_dictionary_array_n() -> *const _KArray {
	KArrayOpt::unwrap(crate::return_dictionary_array_n())
}

#[no_mangle]
extern "C" fn ping_char_array(
	arg: *const _KArray
) -> *const _KArray {
	KCharArray::unwrap(crate::ping_char_array(KCharArray::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_char_array_n(
	arg: *const _KArray
) -> *const _KArray {
	KCharArray::unwrap_nullable(crate::ping_char_array_n(KCharArray::wrap_nullable(arg)))
}

#[no_mangle]
extern "C" fn ping_boolean_array(
	arg: *const _KArray
) -> *const _KArray {
	KBooleanArray::unwrap(crate::ping_boolean_array(KBooleanArray::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_byte_array(
	arg: *const _KArray
) -> *const _KArray {
	KByteArray::unwrap(crate::ping_byte_array(KByteArray::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_ubyte_array(
	arg: *const _KArray
) -> *const _KArray {
	KUByteArray::unwrap(crate::ping_ubyte_array(KUByteArray::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_short_array(
	arg: *const _KArray
) -> *const _KArray {
	KShortArray::unwrap(crate::ping_short_array(KShortArray::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_ushort_array(
	arg: *const _KArray
) -> *const _KArray {
	KUShortArray::unwrap(crate::ping_ushort_array(KUShortArray::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_int_array(
	arg: *const _KArray
) -> *const _KArray {
	KIntArray::unwrap(crate::ping_int_array(KIntArray::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_uint_array(
	arg: *const _KArray
) -> *const _KArray {
	KUIntArray::unwrap(crate::ping_uint_array(KUIntArray::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_long_array(
	arg: *const _KArray
) -> *const _KArray {
	KLongArray::unwrap(crate::ping_long_array(KLongArray::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_ulong_array(
	arg: *const _KArray
) -> *const _KArray {
	KULongArray::unwrap(crate::ping_ulong_array(KULongArray::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_float_array(
	arg: *const _KArray
) -> *const _KArray {
	KFloatArray::unwrap(crate::ping_float_array(KFloatArray::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_double_array(
	arg: *const _KArray
) -> *const _KArray {
	KDoubleArray::unwrap(crate::ping_double_array(KDoubleArray::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_string_array(
	arg: *const _KArray
) -> *const _KArray {
	KArray::unwrap(crate::ping_string_array(KArray::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_string_array_n(
	arg: *const _KArray
) -> *const _KArray {
	KArrayOpt::unwrap(crate::ping_string_array_n(KArrayOpt::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_enum_array(
	arg: *const _KArray
) -> *const _KArray {
	KIntArray::unwrap(crate::ping_enum_array(KIntArray::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_dictionary_array(
	arg: *const _KArray
) -> *const _KArray {
	KArray::unwrap(crate::ping_dictionary_array(KArray::wrap(arg)))
}

#[no_mangle]
extern "C" fn ping_dictionary_array_n(
	arg: *const _KArray
) -> *const _KArray {
	KArrayOpt::unwrap(crate::ping_dictionary_array_n(KArrayOpt::wrap(arg)))
}

#[no_mangle]
extern "C" fn callback_arg_char_array(
	arg: *const _CallbackPassCharArray
) -> bool {
	crate::callback_arg_char_array(CallbackPassCharArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_char_array_n(
	arg: *const _CallbackPassCharArrayN
) -> bool {
	crate::callback_arg_char_array_n(CallbackPassCharArrayN::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_boolean_array(
	arg: *const _CallbackPassBooleanArray
) -> bool {
	crate::callback_arg_boolean_array(CallbackPassBooleanArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_byte_array(
	arg: *const _CallbackPassByteArray
) -> bool {
	crate::callback_arg_byte_array(CallbackPassByteArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_ubyte_array(
	arg: *const _CallbackPassUByteArray
) -> bool {
	crate::callback_arg_ubyte_array(CallbackPassUByteArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_short_array(
	arg: *const _CallbackPassShortArray
) -> bool {
	crate::callback_arg_short_array(CallbackPassShortArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_ushort_array(
	arg: *const _CallbackPassUShortArray
) -> bool {
	crate::callback_arg_ushort_array(CallbackPassUShortArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_int_array(
	arg: *const _CallbackPassIntArray
) -> bool {
	crate::callback_arg_int_array(CallbackPassIntArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_uint_array(
	arg: *const _CallbackPassUIntArray
) -> bool {
	crate::callback_arg_uint_array(CallbackPassUIntArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_long_array(
	arg: *const _CallbackPassLongArray
) -> bool {
	crate::callback_arg_long_array(CallbackPassLongArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_ulong_array(
	arg: *const _CallbackPassULongArray
) -> bool {
	crate::callback_arg_ulong_array(CallbackPassULongArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_float_array(
	arg: *const _CallbackPassFloatArray
) -> bool {
	crate::callback_arg_float_array(CallbackPassFloatArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_double_array(
	arg: *const _CallbackPassDoubleArray
) -> bool {
	crate::callback_arg_double_array(CallbackPassDoubleArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_string_array(
	arg: *const _CallbackPassStringArray
) -> bool {
	crate::callback_arg_string_array(CallbackPassStringArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_string_array_n(
	arg: *const _CallbackPassStringArrayN
) -> bool {
	crate::callback_arg_string_array_n(CallbackPassStringArrayN::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_enum_array(
	arg: *const _CallbackPassEnumArray
) -> bool {
	crate::callback_arg_enum_array(CallbackPassEnumArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_dictionary_array(
	arg: *const _CallbackPassDictionaryArray
) -> bool {
	crate::callback_arg_dictionary_array(CallbackPassDictionaryArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_arg_dictionary_array_n(
	arg: *const _CallbackPassDictionaryArrayN
) -> bool {
	crate::callback_arg_dictionary_array_n(CallbackPassDictionaryArrayN::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_char_array(
	arg: *const _CallbackReturnCharArray
) -> bool {
	crate::callback_return_char_array(CallbackReturnCharArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_char_array_n(
	arg: *const _CallbackReturnCharArrayN
) -> bool {
	crate::callback_return_char_array_n(CallbackReturnCharArrayN::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_boolean_array(
	arg: *const _CallbackReturnBooleanArray
) -> bool {
	crate::callback_return_boolean_array(CallbackReturnBooleanArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_byte_array(
	arg: *const _CallbackReturnByteArray
) -> bool {
	crate::callback_return_byte_array(CallbackReturnByteArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_ubyte_array(
	arg: *const _CallbackReturnUByteArray
) -> bool {
	crate::callback_return_ubyte_array(CallbackReturnUByteArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_short_array(
	arg: *const _CallbackReturnShortArray
) -> bool {
	crate::callback_return_short_array(CallbackReturnShortArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_ushort_array(
	arg: *const _CallbackReturnUShortArray
) -> bool {
	crate::callback_return_ushort_array(CallbackReturnUShortArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_int_array(
	arg: *const _CallbackReturnIntArray
) -> bool {
	crate::callback_return_int_array(CallbackReturnIntArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_uint_array(
	arg: *const _CallbackReturnUIntArray
) -> bool {
	crate::callback_return_uint_array(CallbackReturnUIntArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_long_array(
	arg: *const _CallbackReturnLongArray
) -> bool {
	crate::callback_return_long_array(CallbackReturnLongArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_ulong_array(
	arg: *const _CallbackReturnULongArray
) -> bool {
	crate::callback_return_ulong_array(CallbackReturnULongArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_float_array(
	arg: *const _CallbackReturnFloatArray
) -> bool {
	crate::callback_return_float_array(CallbackReturnFloatArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_double_array(
	arg: *const _CallbackReturnDoubleArray
) -> bool {
	crate::callback_return_double_array(CallbackReturnDoubleArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_string_array(
	arg: *const _CallbackReturnStringArray
) -> bool {
	crate::callback_return_string_array(CallbackReturnStringArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_string_array_n(
	arg: *const _CallbackReturnStringArrayN
) -> bool {
	crate::callback_return_string_array_n(CallbackReturnStringArrayN::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_enum_array(
	arg: *const _CallbackReturnEnumArray
) -> bool {
	crate::callback_return_enum_array(CallbackReturnEnumArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_dictionary_array(
	arg: *const _CallbackReturnDictionaryArray
) -> bool {
	crate::callback_return_dictionary_array(CallbackReturnDictionaryArray::wrap(arg))
}

#[no_mangle]
extern "C" fn callback_return_dictionary_array_n(
	arg: *const _CallbackReturnDictionaryArrayN
) -> bool {
	crate::callback_return_dictionary_array_n(CallbackReturnDictionaryArrayN::wrap(arg))
}

#[no_mangle]
extern "C" fn pass_big_dictionary(
	arg: *const _TypeDictionary
) -> bool {
	crate::pass_big_dictionary(TypeDictionary::wrap(arg))
}

#[no_mangle]
extern "C" fn return_big_dictionary(
	callback: *const _VoidCallback
) -> *const _TypeDictionary {
	TypeDictionary::unwrap(crate::return_big_dictionary(VoidCallback::wrap(callback)))
}

#[no_mangle]
extern "C" fn ping_big_dictionary(
	arg: *const _TypeDictionary
) -> *const _TypeDictionary {
	TypeDictionary::unwrap(crate::ping_big_dictionary(TypeDictionary::wrap(arg)))
}

#[no_mangle]
extern "C" fn pass_big_dictionary_n(
	arg: *const _TypeDictionary
) -> bool {
	crate::pass_big_dictionary_n(TypeDictionary::wrap_nullable(arg))
}

#[no_mangle]
extern "C" fn return_big_dictionary_n() -> *const _TypeDictionary {
	TypeDictionary::unwrap_nullable(crate::return_big_dictionary_n())
}

#[no_mangle]
extern "C" fn ping_big_dictionary_n(
	arg: *const _TypeDictionary
) -> *const _TypeDictionary {
	TypeDictionary::unwrap_nullable(crate::ping_big_dictionary_n(TypeDictionary::wrap_nullable(arg)))
}

#[no_mangle]
extern "C" fn critical_primitives(
	a1: u16,
	a2: bool,
	a3: i8,
	a4: u8,
	a5: i16,
	a6: u16,
	a7: i32,
	a8: u32,
	a9: i64,
	a10: u64,
	a11: f32,
	a12: f64
) -> bool {
	crate::critical_primitives(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12)
}

#[no_mangle]
extern "C" fn critical_enum(
	a1: i32
) -> bool {
	crate::critical_enum(MyEnum::from_int(a1))
}

#[no_mangle]
extern "C" fn critical_string(
	a1: *const _KString
) -> bool {
	crate::critical_string(KString::wrap(a1))
}

#[no_mangle]
extern "C" fn critical_string_n(
	a1: *const _KString
) -> bool {
	crate::critical_string_n(KString::wrap_nullable(a1))
}

#[no_mangle]
extern "C" fn critical_primitives_array(
	a1: *const _KArray,
	a2: *const _KArray,
	a3: *const _KArray,
	a4: *const _KArray,
	a5: *const _KArray,
	a6: *const _KArray,
	a7: *const _KArray,
	a8: *const _KArray,
	a9: *const _KArray,
	a10: *const _KArray,
	a11: *const _KArray,
	a12: *const _KArray
) -> bool {
	crate::critical_primitives_array(KCharArray::wrap(a1), KBooleanArray::wrap(a2), KByteArray::wrap(a3), KUByteArray::wrap(a4), KShortArray::wrap(a5), KUShortArray::wrap(a6), KIntArray::wrap(a7), KUIntArray::wrap(a8), KLongArray::wrap(a9), KULongArray::wrap(a10), KFloatArray::wrap(a11), KDoubleArray::wrap(a12))
}

#[no_mangle]
extern "C" fn critical_enum_array(
	a1: *const _KArray
) -> bool {
	crate::critical_enum_array(KIntArray::wrap(a1))
}

#[no_mangle]
extern "C" fn critical_primitives_array_n(
	a1: *const _KArray,
	a2: *const _KArray,
	a3: *const _KArray,
	a4: *const _KArray,
	a5: *const _KArray,
	a6: *const _KArray,
	a7: *const _KArray,
	a8: *const _KArray,
	a9: *const _KArray,
	a10: *const _KArray,
	a11: *const _KArray,
	a12: *const _KArray
) -> bool {
	crate::critical_primitives_array_n(KCharArray::wrap_nullable(a1), KBooleanArray::wrap_nullable(a2), KByteArray::wrap_nullable(a3), KUByteArray::wrap_nullable(a4), KShortArray::wrap_nullable(a5), KUShortArray::wrap_nullable(a6), KIntArray::wrap_nullable(a7), KUIntArray::wrap_nullable(a8), KLongArray::wrap_nullable(a9), KULongArray::wrap_nullable(a10), KFloatArray::wrap_nullable(a11), KDoubleArray::wrap_nullable(a12))
}

#[no_mangle]
extern "C" fn critical_enum_array_n(
	a1: *const _KArray
) -> bool {
	crate::critical_enum_array_n(KIntArray::wrap_nullable(a1))
}

#[no_mangle]
extern "C" fn critical_return_char() -> u16 {
	crate::critical_return_char()
}

#[no_mangle]
extern "C" fn critical_return_boolean() -> bool {
	crate::critical_return_boolean()
}

#[no_mangle]
extern "C" fn critical_return_byte() -> i8 {
	crate::critical_return_byte()
}

#[no_mangle]
extern "C" fn critical_return_ubyte() -> u8 {
	crate::critical_return_ubyte()
}

#[no_mangle]
extern "C" fn critical_return_short() -> i16 {
	crate::critical_return_short()
}

#[no_mangle]
extern "C" fn critical_return_ushort() -> u16 {
	crate::critical_return_ushort()
}

#[no_mangle]
extern "C" fn critical_return_int() -> i32 {
	crate::critical_return_int()
}

#[no_mangle]
extern "C" fn critical_return_uint() -> u32 {
	crate::critical_return_uint()
}

#[no_mangle]
extern "C" fn critical_return_long() -> i64 {
	crate::critical_return_long()
}

#[no_mangle]
extern "C" fn critical_return_ulong() -> u64 {
	crate::critical_return_ulong()
}

#[no_mangle]
extern "C" fn critical_return_float() -> f32 {
	crate::critical_return_float()
}

#[no_mangle]
extern "C" fn critical_return_double() -> f64 {
	crate::critical_return_double()
}

#[no_mangle]
extern "C" fn critical_return_enum() -> i32 {
	MyEnum::to_int(crate::critical_return_enum())
}

#[no_mangle]
extern "C" fn jvmci1() -> bool {
	crate::jvmci1()
}

#[no_mangle]
extern "C" fn jvmci2(
	a1: i32
) -> bool {
	crate::jvmci2(a1)
}

#[no_mangle]
extern "C" fn jvmci3(
	a1: i32,
	a2: i32
) -> bool {
	crate::jvmci3(a1, a2)
}

#[no_mangle]
extern "C" fn jvmci4(
	a1: i32,
	a2: i32,
	a3: i32,
	a4: i32,
	a5: i32,
	a6: i32,
	a7: i32,
	a8: i32,
	a9: i32
) -> bool {
	crate::jvmci4(a1, a2, a3, a4, a5, a6, a7, a8, a9)
}

#[no_mangle]
extern "C" fn jvmci5(
	a1: i32,
	a2: i64,
	a3: i32,
	a4: i64,
	a5: i32,
	a6: i64,
	a7: i32,
	a8: i32,
	a9: i64
) -> bool {
	crate::jvmci5(a1, a2, a3, a4, a5, a6, a7, a8, a9)
}

#[no_mangle]
extern "C" fn jvmci6(
	a1: f32,
	a2: f32,
	a3: f32,
	a4: f32,
	a5: f32,
	a6: f32,
	a7: f32,
	a8: f32,
	a9: f32,
	a10: i32,
	a11: i32,
	a12: i32,
	a13: i32
) -> bool {
	crate::jvmci6(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13)
}

#[no_mangle]
extern "C" fn jvmci7(
	a1: f32,
	a2: f64,
	a3: f32,
	a4: f64,
	a5: f32,
	a6: f64,
	a7: f32,
	a8: f32,
	a9: f64
) -> bool {
	crate::jvmci7(a1, a2, a3, a4, a5, a6, a7, a8, a9)
}

#[no_mangle]
extern "C" fn jvmci8(
	a1: i32,
	a2: f64,
	a3: f32,
	a4: i64
) -> bool {
	crate::jvmci8(a1, a2, a3, a4)
}

#[no_mangle]
extern "C" fn jvmci9(
	a1: i32,
	a2: f64,
	a3: f32,
	a4: i64,
	a5: i64,
	a6: f64,
	a7: f32,
	a8: f32,
	a9: i32
) -> bool {
	crate::jvmci9(a1, a2, a3, a4, a5, a6, a7, a8, a9)
}

#[no_mangle]
extern "C" fn jvmci10(
	a1: *const _KString,
	a2: f64,
	a3: f32,
	a4: i64,
	a5: i64,
	a6: f64,
	a7: *const _KString,
	a8: f32,
	a9: i32
) -> bool {
	crate::jvmci10(KString::wrap(a1), a2, a3, a4, a5, a6, KString::wrap(a7), a8, a9)
}

#[no_mangle]
extern "C" fn jvmci11(
	a1: f32,
	a2: i32,
	a3: f32,
	a4: i32,
	a5: f32,
	a6: i32,
	a7: f32,
	a8: i32,
	a9: f32,
	a10: i32,
	a11: f32,
	a12: i32,
	a13: f32,
	a14: i32,
	a15: f32,
	a16: i32,
	a17: f32
) -> bool {
	crate::jvmci11(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17)
}

#[no_mangle]
extern "C" fn jvmci12() -> i32 {
	crate::jvmci12()
}

#[no_mangle]
extern "C" fn jvmci13() -> i64 {
	crate::jvmci13()
}

#[no_mangle]
extern "C" fn jvmci14() -> f32 {
	crate::jvmci14()
}

#[no_mangle]
extern "C" fn jvmci15() -> f64 {
	crate::jvmci15()
}

#[no_mangle]
extern "C" fn jvmci_array(
	array: *const _KArray
) -> bool {
	crate::jvmci_array(KIntArray::wrap(array))
}

#[no_mangle]
extern "C" fn jvmci_some_arrays(
	array1: *const _KArray,
	array2: *const _KArray,
	array3: *const _KArray
) -> bool {
	crate::jvmci_some_arrays(KIntArray::wrap(array1), KFloatArray::wrap(array2), KDoubleArray::wrap(array3))
}

#[no_mangle]
extern "C" fn jvmci_enum(
	enum1: i32,
	enum2: i32,
	enum_array: *const _KArray
) -> bool {
	crate::jvmci_enum(MyEnum::from_int(enum1), MyEnum::from_int(enum2), KIntArray::wrap(enum_array))
}
