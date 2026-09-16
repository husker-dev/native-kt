package com.huskerdev.nativekt.printers.rust

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.utils.camelCase
import com.huskerdev.nativekt.utils.isReleasable
import com.huskerdev.nativekt.utils.printLabel


internal fun StringBuilder.printHeaderDef(context: NativeModuleContext) {
    appendLine($$"""
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
        unsafe extern "C" fn $${context.mangle("init")}() {}
        
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
    """.trimIndent())

    if(context.usedTypes.any { it.isReleasable() }) appendLine("""
        
        fn from_raw<T>(of: *mut T) -> T {
            unsafe { *Box::from_raw(of) }
        }
        
        fn into_raw<T>(of: T) -> *mut T {
            Box::into_raw(Box::new(of))
        }
    """.trimIndent())
    if(context.hasNullableCastToNative) appendLine("""
        
        fn ptr_opt<T, R>(ptr: *mut T, f: fn(*mut T) -> R) -> Option<R> {
            if ptr.is_null() { None } else { Some(f(ptr)) }
        }
    """.trimIndent())
    if(context.hasNullableCastToKotlin) appendLine("""
        
        fn obj_opt<T, R>(ptr: Option<R>, f: fn(R) -> *mut T) -> *mut T {
            if ptr.is_none() { null_mut() } else { f(ptr.unwrap()) }
        }
    """.trimIndent())
    if(context.needsAllocFunctions) appendLine("""
        
        export_fn! {
            fn ${context.mangle("alloc")}(size: usize) -> *mut u8 as ${context.jsMangle["alloc"]} {
                unsafe { alloc(Layout::from_size_align_unchecked(size, 1)) }
            }
            fn ${context.mangle("dealloc")}(ptr: *mut u8, size: usize) -> () as ${context.jsMangle["dealloc"]} {
                if ptr.is_null() { return }
                unsafe { dealloc(ptr, Layout::from_size_align_unchecked(size, 1)) }
            }
        }
    """.trimIndent())

    if(context.callbacks.isNotEmpty()) appendLine($$"""
        
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
    """.trimIndent())
}

internal fun StringBuilder.printStringDef(context: NativeModuleContext) {
    if(!context.hasStringCast)
        return
    printLabel("String")

    append("\nexport_fn! {")
    if(context.hasStringCastToNative) appendLine($$"""
        
        fn $${context.mangle("string_new")}(data: *mut u8, _length: i32, size: i32, make_copy: bool) -> *mut String as $${context.jsMangle["string_new"]} {
            if make_copy {
                unsafe { into_raw(String::from_utf8_unchecked(std::slice::from_raw_parts(data, size as usize).to_vec())) }
            } else {
                unsafe { into_raw(String::from_raw_parts(data, size as usize, size as usize)) }
            }
        }
    """.replaceIndent("\t"))
    if(context.hasStringCastToKotlin) appendLine("""
        
        fn ${context.mangle("string_data")}(str: *mut String) -> *mut u8 as ${context.jsMangle["string_data"]} {
            unsafe { (&*str).as_ptr().cast_mut() }
        }
        fn ${context.mangle("string_length")}(str: *mut String) -> i32 as ${context.jsMangle["string_length"]} {
            unsafe { (&*str).chars().count() as i32 }
        }
        fn ${context.mangle("string_size")}(str: *mut String) -> i32 as ${context.jsMangle["string_size"]} {
            unsafe { (&*str).len() as i32 }
        }
        fn ${context.mangle("string_free")}(str: *mut String) -> () as ${context.jsMangle["string_free"]} {
            from_raw(str);
        }
    """.replaceIndent("\t"))
    appendLine("\n}")
}

internal fun StringBuilder.printArraysDef(context: NativeModuleContext) {
    if(context.hasPrimitiveArrayCast) {
        printLabel("Primitive arrays")
        append($$"""
            
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
        """.trimIndent())

        listOf(
            Triple("char", "u16", context.hasCharArrayCast),
            Triple("boolean", "bool", context.hasBooleanArrayCast),
            Triple("byte", "i8", context.hasByteArrayCast || context.hasUByteArrayCast),
            Triple("short", "i16", context.hasShortArrayCast || context.hasUShortArrayCast),
            Triple("int", "i32", context.hasIntArrayCast || context.hasEnumsCast || context.hasUIntArrayCast),
            Triple("long", "i64", context.hasLongArrayCast || context.hasULongArrayCast),
            Triple("float", "f32", context.hasFloatArrayCast),
            Triple("double", "f64", context.hasDoubleArrayCast)
        ).forEach { (name, type, has) ->
            if(!has)
                return@forEach
            append("""
                
                impl_typed_array!($type,
                    ${context.mangle("${name}array_new")}, ${context.jsMangle["${name}array_new"]},
                    ${context.mangle("${name}array_elements")}, ${context.jsMangle["${name}array_elements"]},
                    ${context.mangle("${name}array_length")}, ${context.jsMangle["${name}array_length"]},
                    ${context.mangle("${name}array_free")}, ${context.jsMangle["${name}array_free"]}
                );
            """.trimIndent())
        }
        append("\n")
    }

    if(context.hasObjectArraysCast) {
        printLabel("Object array")
        append($$"""
            
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
            
        """.trimIndent())

        buildList {
            context.dictionaries
                .filter { it in context.usedObjectArrayCast }
                .mapTo(this) {
                    it.rustName to it.name.camelCase().lowercase()
                }
            context.interfaces
                .filter { it in context.usedObjectArrayCast }
                .mapTo(this) {
                    "Arc<${it.rustName}>" to it.name.camelCase().lowercase()
                }
            if(context.hasStringArrayCast)
                add("String" to "string")
        }.joinTo(this, separator = "") {
            val lower = it.second
            """
            
            impl_object_array!(${it.first},
            	${context.mangle("array_${lower}_new")}, ${context.jsMangle["array_${lower}_new"]},
                ${context.mangle("array_${lower}_length")}, ${context.jsMangle["array_${lower}_length"]},
                ${context.mangle("array_${lower}_push")}, ${context.jsMangle["array_${lower}_push"]},
                ${context.mangle("array_${lower}_get")}, ${context.jsMangle["array_${lower}_get"]},
                ${context.mangle("array_${lower}_free")}, ${context.jsMangle["array_${lower}_free"]}
            );
        """.trimIndent()
        }
        append("\n")
    }
}

internal fun StringBuilder.printCriticalFuncDef(context: NativeModuleContext) {
    if(!context.hasCriticalStringCast &&
        !context.hasCriticalStringOptCast &&
        !context.hasCriticalArrayCast && !
        context.hasCriticalArrayOptCast
    ) return

    printLabel("Critical functions")

    if(context.hasCriticalStringCast) appendLine("""
        
        fn critical_string(data: *mut u8, size: i32) -> ManuallyDrop<String> {
            unsafe { ManuallyDrop::new(String::from_raw_parts(data, size as usize, size as usize)) }
        }
    """.trimIndent())
    if(context.hasCriticalStringOptCast) appendLine("""
        
        fn critical_string_opt(data: *mut u8, length: i32, size: i32) -> ManuallyDrop<Option<String>> {
            ManuallyDrop::new(if length != -1 {
                Some(unsafe { String::from_raw_parts(data, size as usize, size as usize) })
            } else { None })
        }
    """.trimIndent())
    if(context.hasCriticalArrayCast) appendLine("""
        
        fn critical_array<T>(data: *mut T, size: i32) -> ManuallyDrop<Vec<T>> {
            unsafe { ManuallyDrop::new(Vec::from_raw_parts(data, size as usize, size as usize)) }
        }
    """.trimIndent())
    if(context.hasCriticalArrayOptCast) appendLine("""
        
        fn critical_array_opt<T>(data: *mut T, size: i32) -> ManuallyDrop<Option<Vec<T>>> {
            ManuallyDrop::new(if size != -1 {
                Some(unsafe { Vec::from_raw_parts(data, size as usize, size as usize) })
            } else { None })
        }
    """.trimIndent())
}