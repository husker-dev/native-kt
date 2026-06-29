
// ╔═════════════╗
// ║     API     ║
// ╚═════════════╝
/*
=============================================================== *\

pub fn test_func(
	arg: Option<KString>,
	arg2: i32
) -> KString {}

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
impl_typed_array!(KBooleanArray, i8, 1, KBooleanArray_new, KBooleanArray_free, KBooleanArray_clone);
impl_typed_array!(KByteArray, i8, 1, KByteArray_new, KByteArray_free, KByteArray_clone);
impl_typed_array!(KShortArray, i16, 2, KShortArray_new, KShortArray_free, KShortArray_clone);
impl_typed_array!(KIntArray, i32, 4, KIntArray_new, KIntArray_free, KIntArray_clone);
impl_typed_array!(KLongArray, i64, 8, KLongArray_new, KLongArray_free, KLongArray_clone);
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
#[macro_export] macro_rules! k_short_array {
    ($($x:expr),+ $(,)?) => (KShortArray::from(vec![$($x),+]));
}
#[macro_export] macro_rules! k_int_array {
    ($($x:expr),+ $(,)?) => (KIntArray::from(vec![$($x),+]));
}
#[macro_export] macro_rules! k_long_array {
    ($($x:expr),+ $(,)?) => (KLongArray::from(vec![$($x),+]));
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
