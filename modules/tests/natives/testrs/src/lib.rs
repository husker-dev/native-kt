use std::sync::Arc;
use crate::nativekt::*;

mod nativekt;

pub struct MyInterface;

impl MyInterface {
    fn new() -> Self {
        MyInterface {}
    }
    fn new_critical(_: i32) -> Self {
        MyInterface {}
    }
    fn test(&self) -> bool {
        println!("Hello from Rust interface!");
        true
    }
    fn test_critical(&self) -> bool {
        println!("Hello from Rust interface!");
        true
    }
    fn pass_interface(&self, _: &Arc<MyInterface>) {
    }
    fn return_interface(&self) -> Arc<MyInterface> {
        Arc::new(MyInterface::new())
    }
}

impl Drop for MyInterface {
    fn drop(&mut self) {
        println!("Drop MyInterface")
    }
}

fn f32_cmp(f1: f32, f2: f32) -> bool {
    (f1 - f2).abs() < 0.00001
}

fn f64_cmp(f1: f64, f2: f64) -> bool {
    (f1 - f2).abs() < 0.00001
}

pub fn pass_void() -> bool {
    true
}

pub fn pass_char(
    arg: u16
) -> bool {
    arg == 'a' as u16
}

pub fn pass_boolean(
    arg: bool
) -> bool {
    arg == true
}

pub fn pass_byte(
    arg: i8
) -> bool {
    arg == 1
}

pub fn pass_ubyte(
    arg: u8
) -> bool {
    arg == 255
}

pub fn pass_short(
    arg: i16
) -> bool {
    arg == 1
}

pub fn pass_ushort(
    arg: u16
) -> bool {
    arg == 65535
}

pub fn pass_int(
    arg: i32
) -> bool {
    arg == 99
}

pub fn pass_uint(
    arg: u32
) -> bool {
    arg == 4294967295
}

pub fn pass_long(
    arg: i64
) -> bool {
    arg == 9223372036854775805
}

pub fn pass_ulong(
    arg: u64
) -> bool {
    arg == 18446744073709551615
}

pub fn pass_float(
    arg: f32
) -> bool {
    f32_cmp(arg, 99.9)
}

pub fn pass_double(
    arg: f64
) -> bool {
    arg == 1.1
}

pub fn pass_string(
    arg: &String
) -> bool {
    arg == "test string"
}

pub fn pass_string_n(
    arg: &Option<String>
) -> bool {
    arg.is_none()
}

pub fn pass_enum(
    arg: MyEnum
) -> bool {
    arg == MyEnum::CASE2
}

pub fn pass_dictionary(
    arg: &MyDictionary
) -> bool {
    arg.a == 1 &&
    arg.b == 2 &&
    arg.c == 3 &&
    arg.d == 4
}

pub fn pass_dictionary_n(
    arg: &Option<MyDictionary>
) -> bool {
    arg.is_none()
}

pub fn pass_interface(
    arg: &Arc<MyInterface>
) -> bool {
    arg.test()
}

pub fn pass_interface_n(
    arg: &Option<Arc<MyInterface>>
) -> bool {
    arg.is_none()
}

pub fn return_void() {}

pub fn return_char() -> u16 {
    'a' as u16
}

pub fn return_boolean() -> bool {
    true
}

pub fn return_byte() -> i8 {
    99
}

pub fn return_ubyte() -> u8 {
    255
}

pub fn return_short() -> i16 {
    99
}

pub fn return_ushort() -> u16 {
    65535
}

pub fn return_int() -> i32 {
    99
}

pub fn return_uint() -> u32 {
    4294967295
}

pub fn return_long() -> i64 {
    9223372036854775805
}

pub fn return_ulong() -> u64 {
    18446744073709551615
}

pub fn return_float() -> f32 {
    99.0
}

pub fn return_double() -> f64 {
    99.0
}

pub fn return_string() -> String {
    "test string".to_string()
}

pub fn return_string_n() -> Option<String> {
    None
}

pub fn return_enum() -> MyEnum {
    MyEnum::CASE2
}

pub fn return_dictionary() -> MyDictionary {
    MyDictionary { a: 1, b: 2, c: 3, d: 4 }
}

pub fn return_dictionary_n() -> Option<MyDictionary> {
    None
}

pub fn ping_char(
    arg: u16
) -> u16 {
    arg
}

pub fn ping_boolean(
    arg: bool
) -> bool {
    arg
}

pub fn ping_byte(
    arg: i8
) -> i8 {
    arg
}

pub fn ping_ubyte(
    arg: u8
) -> u8 {
    arg
}

pub fn ping_short(
    arg: i16
) -> i16 {
    arg
}

pub fn ping_ushort(
    arg: u16
) -> u16 {
    arg
}

pub fn ping_int(
    arg: i32
) -> i32 {
    arg
}

pub fn ping_uint(
    arg: u32
) -> u32 {
    arg
}

pub fn ping_long(
    arg: i64
) -> i64 {
    arg
}

pub fn ping_ulong(
    arg: u64
) -> u64 {
    arg
}

pub fn ping_float(
    arg: f32
) -> f32 {
    arg
}

pub fn ping_double(
    arg: f64
) -> f64 {
    arg
}

pub fn ping_string(
    arg: &String
) -> String {
    arg.clone()
}

pub fn ping_string_n(
    arg: &Option<String>
) -> Option<String> {
    arg.clone()
}

pub fn ping_enum(
    arg: MyEnum
) -> MyEnum {
    arg
}

pub fn ping_dictionary(
    arg: &MyDictionary
) -> MyDictionary {
    arg.clone()
}

pub fn ping_dictionary_n(
    arg: &Option<MyDictionary>
) -> Option<MyDictionary> {
    arg.clone()
}

pub fn ping_interface(
    arg: &Arc<MyInterface>
) -> Arc<MyInterface> {
    arg.clone()
}

pub fn ping_interface_n(
    arg: &Option<Arc<MyInterface>>
) -> Option<Arc<MyInterface>> {
    arg.clone()
}

pub fn callback_void(
    arg: &Arc<VoidCallback>
) {
    arg.invoke()
}

pub fn callback_void_n(
    arg: &Option<Arc<VoidCallback>>
) -> bool {
    arg.is_none()
}

pub fn callback_arg_char(
    arg: &Arc<CallbackPassChar>
) -> bool {
    arg.invoke('a' as u16)
}

pub fn callback_arg_boolean(
    arg: &Arc<CallbackPassBoolean>
) -> bool {
    arg.invoke(true)
}

pub fn callback_arg_byte(
    arg: &Arc<CallbackPassByte>
) -> bool {
    arg.invoke(1)
}

pub fn callback_arg_ubyte(
    arg: &Arc<CallbackPassUByte>
) -> bool {
    arg.invoke(255)
}

pub fn callback_arg_short(
    arg: &Arc<CallbackPassShort>
) -> bool {
    arg.invoke(1)
}

pub fn callback_arg_ushort(
    arg: &Arc<CallbackPassUShort>
) -> bool {
    arg.invoke(65535)
}

pub fn callback_arg_int(
    arg: &Arc<CallbackPassInt>
) -> bool {
    arg.invoke(1)
}

pub fn callback_arg_uint(
    arg: &Arc<CallbackPassUInt>
) -> bool {
    arg.invoke(4294967295)
}

pub fn callback_arg_long(
    arg: &Arc<CallbackPassLong>
) -> bool {
    arg.invoke(1)
}

pub fn callback_arg_ulong(
    arg: &Arc<CallbackPassULong>
) -> bool {
    arg.invoke(18446744073709551615)
}

pub fn callback_arg_float(
    arg: &Arc<CallbackPassFloat>
) -> bool {
    arg.invoke(1.1)
}

pub fn callback_arg_double(
    arg: &Arc<CallbackPassDouble>
) -> bool {
    arg.invoke(1.1)
}

pub fn callback_arg_string(
    arg: &Arc<CallbackPassString>
) -> bool {
    arg.invoke("test string".to_string())
}

pub fn callback_arg_string_n(
    arg: &Arc<CallbackPassStringN>
) -> bool {
    arg.invoke(None)
}

pub fn callback_arg_callback(
    pass: &Arc<VoidCallback>,
    arg: &Arc<CallbackPassCallback>
) -> bool {
    arg.invoke(pass.clone())
}

pub fn callback_arg_callback_n(
    arg: &Arc<CallbackPassCallbackN>
) -> bool {
    arg.invoke(None)
}

pub fn callback_arg_enum(
    arg: &Arc<CallbackPassEnum>
) -> bool {
    arg.invoke(MyEnum::CASE2)
}

pub fn callback_arg_dictionary(
    arg: &Arc<CallbackPassDictionary>
) -> bool {
    arg.invoke(MyDictionary { a: 1, b: 2, c: 3, d: 4 })
}

pub fn callback_arg_dictionary_n(
    arg: &Arc<CallbackPassDictionaryN>
) -> bool {
    arg.invoke(None)
}

pub fn callback_arg_interface(
    pass: &Arc<MyInterface>,
    arg: &Arc<CallbackPassInterface>
) -> bool {
    arg.invoke(pass.clone())
}

pub fn callback_arg_interface_n(
    arg: &Arc<CallbackPassInterfaceN>
) -> bool {
    arg.invoke(None)
}

pub fn callback_return_char(
    arg: &Arc<CallbackReturnChar>
) -> bool {
    arg.invoke() == 'a' as u16
}

pub fn callback_return_boolean(
    arg: &Arc<CallbackReturnBoolean>
) -> bool {
    arg.invoke() == true
}

pub fn callback_return_byte(
    arg: &Arc<CallbackReturnByte>
) -> bool {
    arg.invoke() == 1
}

pub fn callback_return_ubyte(
    arg: &Arc<CallbackReturnUByte>
) -> bool {
    arg.invoke() == 255
}

pub fn callback_return_short(
    arg: &Arc<CallbackReturnShort>
) -> bool {
    arg.invoke() == 1
}

pub fn callback_return_ushort(
    arg: &Arc<CallbackReturnUShort>
) -> bool {
    arg.invoke() == 65535
}

pub fn callback_return_int(
    arg: &Arc<CallbackReturnInt>
) -> bool {
    arg.invoke() == 1
}

pub fn callback_return_uint(
    arg: &Arc<CallbackReturnUInt>
) -> bool {
    arg.invoke() == 4294967295
}

pub fn callback_return_long(
    arg: &Arc<CallbackReturnLong>
) -> bool {
    arg.invoke() == 1
}

pub fn callback_return_ulong(
    arg: &Arc<CallbackReturnULong>
) -> bool {
    arg.invoke() == 18446744073709551615
}

pub fn callback_return_float(
    arg: &Arc<CallbackReturnFloat>
) -> bool {
    arg.invoke() == 1.1
}

pub fn callback_return_double(
    arg: &Arc<CallbackReturnDouble>
) -> bool {
    arg.invoke() == 1.1
}

pub fn callback_return_string(
    arg: &Arc<CallbackReturnString>
) -> bool {
    arg.invoke().as_str() == "test string"
}

pub fn callback_return_string_n(
    arg: &Arc<CallbackReturnStringN>
) -> bool {
    arg.invoke().is_none()
}

pub fn callback_return_callback(
    arg: &Arc<CallbackReturnCallback>
) -> Arc<VoidCallback> {
    arg.invoke()
}

pub fn callback_return_callback_n(
    arg: &Arc<CallbackReturnCallbackN>
) -> bool {
    arg.invoke().is_none()
}

pub fn callback_return_enum(
    arg: &Arc<CallbackReturnEnum>
) -> bool {
    arg.invoke() == MyEnum::CASE2
}

pub fn callback_return_dictionary(
    arg: &Arc<CallbackReturnDictionary>
) -> bool {
    let result = arg.invoke();
    result.a == 1 &&
    result.b == 2 &&
    result.c == 3 &&
    result.d == 4
}

pub fn callback_return_dictionary_n(
    arg: &Arc<CallbackReturnDictionaryN>
) -> bool {
    arg.invoke().is_none()
}

pub fn callback_return_interface(
    arg: &Arc<CallbackReturnInterface>
) -> bool {
    arg.invoke().test()
}

pub fn callback_return_interface_n(
    arg: &Arc<CallbackReturnInterfaceN>
) -> bool {
    arg.invoke().is_none()
}

pub fn pass_char_array(
    arg: &Vec<u16>
) -> bool {
    arg.as_slice() == ['a' as u16, 'b' as u16]
}

pub fn pass_char_array_n(
    arg: &Option<Vec<u16>>
) -> bool {
    arg.is_none()
}

pub fn pass_boolean_array(
    arg: &Vec<bool>
) -> bool {
    arg.as_slice() == [true, false]
}

pub fn pass_byte_array(
    arg: &Vec<i8>
) -> bool {
    arg.as_slice() == [1, 2]
}

pub fn pass_ubyte_array(
    arg: &Vec<u8>
) -> bool {
    arg.as_slice() == [1, 255]
}

pub fn pass_short_array(
    arg: &Vec<i16>
) -> bool {
    arg.as_slice() == [1, 2]
}

pub fn pass_ushort_array(
    arg: &Vec<u16>
) -> bool {
    arg.as_slice() == [1, 65535]
}

pub fn pass_int_array(
    arg: &Vec<i32>
) -> bool {
    arg.as_slice() == [1, 2]
}

pub fn pass_uint_array(
    arg: &Vec<u32>
) -> bool {
    arg.as_slice() == [1, 4294967295]
}

pub fn pass_long_array(
    arg: &Vec<i64>
) -> bool {
    arg.as_slice() == [1, 2]
}

pub fn pass_ulong_array(
    arg: &Vec<u64>
) -> bool {
    arg.as_slice() == [1, 18446744073709551615]
}

pub fn pass_float_array(
    arg: &Vec<f32>
) -> bool {
    arg.as_slice() == [1.1, 2.2]
}

pub fn pass_double_array(
    arg: &Vec<f64>
) -> bool {
    arg.as_slice() == [1.1, 2.2]
}

pub fn pass_string_array(
    arg: &Vec<String>
) -> bool {
    arg.as_slice()[0].as_str() == "string1" &&
    arg.as_slice()[1].as_str() == "string2"
}

pub fn pass_string_array_n(
    arg: &Vec<Option<String>>
) -> bool {
    arg.as_slice()[0].is_none() &&
    arg.as_slice()[1].is_none()
}

pub fn pass_enum_array(
    arg: &Vec<MyEnum>
) -> bool {
    arg.as_slice()[0] == MyEnum::CASE1 &&
    arg.as_slice()[1] == MyEnum::CASE2
}

pub fn pass_dictionary_array(
    arg: &Vec<MyDictionary>
) -> bool {
    let elements = arg.as_slice();
    elements[0].a == 1 &&
    elements[0].b == 2 &&
    elements[0].c == 3 &&
    elements[0].d == 4 &&
    elements[1].a == 5 &&
    elements[1].b == 6 &&
    elements[1].c == 7 &&
    elements[1].d == 8
}

pub fn pass_dictionary_array_n(
    arg: &Vec<Option<MyDictionary>>
) -> bool {
    arg.as_slice()[0].is_none() && arg.as_slice()[1].is_none()
}

pub fn pass_interface_array(
    arg: &Vec<Arc<MyInterface>>
) -> bool {
    let elements = arg.as_slice();
    elements[0].test() && elements[1].test()
}

pub fn pass_interface_array_n(
    arg: &Vec<Option<Arc<MyInterface>>>
) -> bool {
    arg.as_slice()[0].is_none() && arg.as_slice()[1].is_none()
}

pub fn return_char_array() -> Vec<u16> {
    vec!('a' as u16, 'b' as u16)
}

pub fn return_char_array_n() -> Option<Vec<u16>> {
    None
}

pub fn return_boolean_array() -> Vec<bool> {
    vec!(true, false)
}

pub fn return_byte_array() -> Vec<i8> {
    vec!(1, 2)
}

pub fn return_ubyte_array() -> Vec<u8> {
    vec!(1, 255)
}

pub fn return_short_array() -> Vec<i16> {
    vec!(1, 2)
}

pub fn return_ushort_array() -> Vec<u16> {
    vec!(1, 65535)
}

pub fn return_int_array() -> Vec<i32> {
    vec!(1, 2)
}

pub fn return_uint_array() -> Vec<u32> {
    vec!(1, 4294967295)
}

pub fn return_long_array() -> Vec<i64> {
    vec!(1, 2)
}

pub fn return_ulong_array() -> Vec<u64> {
    vec!(1, 18446744073709551615)
}

pub fn return_float_array() -> Vec<f32> {
    vec!(1.1, 2.2)
}

pub fn return_double_array() -> Vec<f64> {
    vec!(1.1, 2.2)
}

pub fn return_string_array() -> Vec<String> {
    vec!("string1".to_string(), "string2".to_string())
}

pub fn return_string_array_n() -> Vec<Option<String>> {
    vec!(None, None)
}

pub fn return_enum_array() -> Vec<MyEnum> {
    vec!(MyEnum::CASE1, MyEnum::CASE2)
}

pub fn return_dictionary_array() -> Vec<MyDictionary> {
    vec!(
        MyDictionary { a: 1, b: 2, c: 3, d: 4 },
        MyDictionary { a: 5, b: 6, c: 7, d: 8 }
    )
}

pub fn return_dictionary_array_n() -> Vec<Option<MyDictionary>> {
    vec!(None, None)
}

pub fn ping_char_array(
    arg: &Vec<u16>
) -> Vec<u16> {
    arg.clone()
}

pub fn ping_char_array_n(
    arg: &Option<Vec<u16>>
) -> Option<Vec<u16>> {
    arg.clone()
}

pub fn ping_boolean_array(
    arg: &Vec<bool>
) -> Vec<bool> {
    arg.clone()
}

pub fn ping_byte_array(
    arg: &Vec<i8>
) -> Vec<i8> {
    arg.clone()
}

pub fn ping_ubyte_array(
    arg: &Vec<u8>
) -> Vec<u8> {
    arg.clone()
}

pub fn ping_short_array(
    arg: &Vec<i16>
) -> Vec<i16> {
    arg.clone()
}

pub fn ping_ushort_array(
    arg: &Vec<u16>
) -> Vec<u16> {
    arg.clone()
}

pub fn ping_int_array(
    arg: &Vec<i32>
) -> Vec<i32> {
    arg.clone()
}

pub fn ping_uint_array(
    arg: &Vec<u32>
) -> Vec<u32> {
    arg.clone()
}

pub fn ping_long_array(
    arg: &Vec<i64>
) -> Vec<i64> {
    arg.clone()
}

pub fn ping_ulong_array(
    arg: &Vec<u64>
) -> Vec<u64> {
    arg.clone()
}

pub fn ping_float_array(
    arg: &Vec<f32>
) -> Vec<f32> {
    arg.clone()
}

pub fn ping_double_array(
    arg: &Vec<f64>
) -> Vec<f64> {
    arg.clone()
}

pub fn ping_string_array(
    arg: &Vec<String>
) -> Vec<String> {
    arg.clone()
}

pub fn ping_string_array_n(
    arg: &Vec<Option<String>>
) -> Vec<Option<String>> {
    arg.clone()
}

pub fn ping_enum_array(
    arg: &Vec<MyEnum>
) -> Vec<MyEnum> {
    arg.clone()
}

pub fn ping_dictionary_array(
    arg: &Vec<MyDictionary>
) -> Vec<MyDictionary> {
    arg.clone()
}

pub fn ping_dictionary_array_n(
    arg: &Vec<Option<MyDictionary>>
) -> Vec<Option<MyDictionary>> {
    arg.clone()
}

pub fn ping_interface_array(
    arg: &Vec<Arc<MyInterface>>
) -> Vec<Arc<MyInterface>> {
    arg.clone()
}

pub fn ping_interface_array_n(
    arg: &Vec<Option<Arc<MyInterface>>>
) -> Vec<Option<Arc<MyInterface>>> {
    arg.clone()
}

pub fn callback_arg_char_array(
    arg: &Arc<CallbackPassCharArray>
) -> bool {
    arg.invoke(vec!('a' as u16, 'b' as u16))
}

pub fn callback_arg_char_array_n(
    arg: &Arc<CallbackPassCharArrayN>
) -> bool {
    arg.invoke(None)
}

pub fn callback_arg_boolean_array(
    arg: &Arc<CallbackPassBooleanArray>
) -> bool {
    arg.invoke(vec!(true, false))
}

pub fn callback_arg_byte_array(
    arg: &Arc<CallbackPassByteArray>
) -> bool {
    arg.invoke(vec!(1, 2))
}

pub fn callback_arg_ubyte_array(
    arg: &Arc<CallbackPassUByteArray>
) -> bool {
    arg.invoke(vec!(1, 255))
}

pub fn callback_arg_short_array(
    arg: &Arc<CallbackPassShortArray>
) -> bool {
    arg.invoke(vec!(1, 2))
}

pub fn callback_arg_ushort_array(
    arg: &Arc<CallbackPassUShortArray>
) -> bool {
    arg.invoke(vec!(1, 65535))
}

pub fn callback_arg_int_array(
    arg: &Arc<CallbackPassIntArray>
) -> bool {
    arg.invoke(vec!(1, 2))
}

pub fn callback_arg_uint_array(
    arg: &Arc<CallbackPassUIntArray>
) -> bool {
    arg.invoke(vec!(1, 4294967295))
}

pub fn callback_arg_long_array(
    arg: &Arc<CallbackPassLongArray>
) -> bool {
    arg.invoke(vec!(1, 2))
}

pub fn callback_arg_ulong_array(
    arg: &Arc<CallbackPassULongArray>
) -> bool {
    arg.invoke(vec!(1, 18446744073709551615))
}

pub fn callback_arg_float_array(
    arg: &Arc<CallbackPassFloatArray>
) -> bool {
    arg.invoke(vec!(1.1, 2.2))
}

pub fn callback_arg_double_array(
    arg: &Arc<CallbackPassDoubleArray>
) -> bool {
    arg.invoke(vec!(1.1, 2.2))
}

pub fn callback_arg_string_array(
    arg: &Arc<CallbackPassStringArray>
) -> bool {
    arg.invoke(vec!(
        "string1".to_string(),
        "string2".to_string()
    ))
}

pub fn callback_arg_string_array_n(
    arg: &Arc<CallbackPassStringArrayN>
) -> bool {
    arg.invoke(vec!(None, None))
}

pub fn callback_arg_enum_array(
    arg: &Arc<CallbackPassEnumArray>
) -> bool {
    arg.invoke(vec!(
        MyEnum::CASE1,
        MyEnum::CASE2
    ))
}

pub fn callback_arg_dictionary_array(
    arg: &Arc<CallbackPassDictionaryArray>
) -> bool {
    arg.invoke(vec!(
        MyDictionary { a: 1, b: 2, c: 3, d: 4 },
        MyDictionary { a: 5, b: 6, c: 7, d: 8 }
    ))
}

pub fn callback_arg_dictionary_array_n(
    arg: &Arc<CallbackPassDictionaryArrayN>
) -> bool {
    arg.invoke(vec!(None, None))
}

pub fn callback_arg_interface_array(
    arg: &Arc<CallbackPassInterfaceArray>
) -> bool {
    arg.invoke(vec!(
        Arc::new(MyInterface::new()),
        Arc::new(MyInterface::new())
    ))
}

pub fn callback_arg_interface_array_n(
    arg: &Arc<CallbackPassInterfaceArrayN>
) -> bool {
    arg.invoke(vec!(None, None))
}

pub fn callback_return_char_array(
    arg: &Arc<CallbackReturnCharArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 'a' as u16 && arr[1] == 'b' as u16
}

pub fn callback_return_char_array_n(
    arg: &Arc<CallbackReturnCharArrayN>
) -> bool {
    arg.invoke().is_none()
}

pub fn callback_return_boolean_array(
    arg: &Arc<CallbackReturnBooleanArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == true &&
        arr[1] == false
}

pub fn callback_return_byte_array(
    arg: &Arc<CallbackReturnByteArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 2
}

pub fn callback_return_ubyte_array(
    arg: &Arc<CallbackReturnUByteArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 255
}

pub fn callback_return_short_array(
    arg: &Arc<CallbackReturnShortArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 2
}

pub fn callback_return_ushort_array(
    arg: &Arc<CallbackReturnUShortArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 65535
}

pub fn callback_return_int_array(
    arg: &Arc<CallbackReturnIntArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 2
}

pub fn callback_return_uint_array(
    arg: &Arc<CallbackReturnUIntArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 4294967295
}

pub fn callback_return_long_array(
    arg: &Arc<CallbackReturnLongArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 2
}

pub fn callback_return_ulong_array(
    arg: &Arc<CallbackReturnULongArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 18446744073709551615
}

pub fn callback_return_float_array(
    arg: &Arc<CallbackReturnFloatArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1.1 &&
        arr[1] == 2.2
}

pub fn callback_return_double_array(
    arg: &Arc<CallbackReturnDoubleArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1.1 &&
        arr[1] == 2.2
}

pub fn callback_return_string_array(
    arg: &Arc<CallbackReturnStringArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0].as_str() == "string1" &&
        arr[1].as_str() == "string2"
}

pub fn callback_return_string_array_n(
    arg: &Arc<CallbackReturnStringArrayN>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0].is_none() &&
        arr[1].is_none()
}

pub fn callback_return_enum_array(
    arg: &Arc<CallbackReturnEnumArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == MyEnum::CASE1 &&
        arr[1] == MyEnum::CASE2
}

pub fn callback_return_dictionary_array(
    arg: &Arc<CallbackReturnDictionaryArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0].a == 1 &&
        arr[0].b == 2 &&
        arr[0].c == 3 &&
        arr[0].d == 4 &&
        arr[1].a == 5 &&
        arr[1].b == 6 &&
        arr[1].c == 7 &&
        arr[1].d == 8
}

pub fn callback_return_dictionary_array_n(
    arg: &Arc<CallbackReturnDictionaryArrayN>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0].is_none() &&
        arr[1].is_none()
}

pub fn callback_return_interface_array(
    arg: &Arc<CallbackReturnInterfaceArray>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0].test() && arr[1].test()
}

pub fn callback_return_interface_array_n(
    arg: &Arc<CallbackReturnInterfaceArrayN>
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0].is_none() && arr[1].is_none()
}

pub fn pass_big_dictionary(
    arg: &TypeDictionary
) -> bool {
    arg.a1 == 'a' as u16 &&
        arg.a2 == true &&
        arg.a3 == 123 &&
        arg.a4 == 123 &&
        arg.a5 == 123 &&
        arg.a6 == 123 &&
        arg.a7 == 123 &&
        arg.a8 == 123 &&
        arg.a9 == 9223372036854775807 &&
        arg.a10 == 9223372036854775807 &&
        arg.a11 == 123.0 &&
        arg.a12 == 123.4 &&
        arg.a13.as_str() == "test string" &&
        arg.a14 == MyEnum::CASE2 &&
        arg.a15.a == 1 && arg.a15.b == 2 && arg.a15.c == 3 && arg.a15.d == 4 &&
        arg.a16.is_none() &&
        arg.a17.test() &&
        arg.a18.is_none() &&
        
        // callback (a16) is skipped
        arg.a20.as_slice() == ['a' as u16, 'b' as u16] &&
        arg.a21.as_slice() == [true, false] &&
        arg.a22.as_slice() == [1, 2] &&
        arg.a23.as_slice() == [1, 2] &&
        arg.a24.as_slice() == [1, 2] &&
        arg.a25.as_slice() == [1, 2] &&
        arg.a26.as_slice() == [1, 2] &&
        arg.a27.as_slice() == [1, 2] &&
        arg.a28.as_slice() == [1, 2] &&
        arg.a29.as_slice() == [1, 2] &&
        arg.a30.as_slice() == [1.2, 3.4] &&
        arg.a31.as_slice() == [1.2, 3.4] &&
        arg.a32.as_slice()[0].as_str() == "string1" &&
        arg.a32.as_slice()[1].as_str() == "string2" &&
        arg.a33.as_slice() == [MyEnum::CASE1, MyEnum::CASE2] &&
        arg.a34.as_slice()[0].a == 1 &&
        arg.a34.as_slice()[0].b == 2 &&
        arg.a34.as_slice()[0].c == 3 &&
        arg.a34.as_slice()[0].d == 4 &&
        arg.a34.as_slice()[1].a == 5 &&
        arg.a34.as_slice()[1].b == 6 &&
        arg.a34.as_slice()[1].c == 7 &&
        arg.a34.as_slice()[1].d == 8 &&
        arg.a35.as_slice()[0].test() &&
        arg.a35.as_slice()[1].test()
}

pub fn return_big_dictionary(
    callback: &Arc<VoidCallback>,
    inter: &Arc<MyInterface>
) -> TypeDictionary {
    TypeDictionary {
        a1: 'a' as u16,
        a2: true,
        a3: 123,
        a4: 123,
        a5: 123,
        a6: 123,
        a7: 123,
        a8: 123,
        a9: 9223372036854775807,
        a10: 9223372036854775807,
        a11: 123.0,
        a12: 123.4,
        a13: "test string".to_string(),
        a14: MyEnum::CASE2,
        a15: MyDictionary { a: 1, b: 2, c: 3, d: 4 },
        a16: None,
        a17: inter.clone(),
        a18: None,
        a19: callback.clone(),
        a20: vec!('a' as u16, 'b' as u16),
        a21: vec!(true, false),
        a22: vec!(1, 2),
        a23: vec!(1, 2),
        a24: vec!(1, 2),
        a25: vec!(1, 2),
        a26: vec!(1, 2),
        a27: vec!(1, 2),
        a28: vec!(1, 2),
        a29: vec!(1, 2),
        a30: vec!(1.2, 3.4),
        a31: vec!(1.2, 3.4),
        a32: vec!(
            "string1".to_string(),
            "string2".to_string()
        ),
        a33: vec!(MyEnum::CASE1, MyEnum::CASE2),
        a34: vec!(
            MyDictionary { a: 1, b: 2, c: 3, d: 4 },
            MyDictionary { a: 5, b: 6, c: 7, d: 8 }
        ),
        a35: vec!(
            inter.clone(),
            inter.clone()
        )
    }
}

pub fn ping_big_dictionary(
    arg: &TypeDictionary
) -> TypeDictionary {
    arg.clone()
}

pub fn pass_big_dictionary_n(
    arg: &Option<TypeDictionary>
) -> bool {
    arg.is_none()
}

pub fn return_big_dictionary_n() -> Option<TypeDictionary> {
    None
}

pub fn ping_big_dictionary_n(
    arg: &Option<TypeDictionary>
) -> Option<TypeDictionary> {
    arg.clone()
}

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
) -> bool {
    println!();
    a1 == 'a' as u16 &&
        a2 == true &&
        a3 == 1 &&
        a4 == 255 &&
        a5 == 3 &&
        a6 == 65535 &&
        a7 == 5 &&
        a8 == 4294967295 &&
        a9 == 7 &&
        a10 == 18446744073709551615 &&
        f32_cmp(a11, 1.0) &&
        f64_cmp(a12, 2.0)
}

pub fn critical_enum(
    a1: MyEnum
) -> bool {
    a1 == MyEnum::CASE1
}

pub fn critical_string(
    a1: &String
) -> bool {
    a1.as_str() == "test string"
}

pub fn critical_string_n(
    a1: &Option<String>
) -> bool {
    a1.is_none()
}

pub fn critical_interface(
    a1: &Arc<MyInterface>
) -> bool {
    a1.test()
}

pub fn critical_interface_n(
    a1: &Option<Arc<MyInterface>>
) -> bool {
    a1.is_none()
}

pub fn critical_primitives_array(
    a1: &Vec<u16>,
    a2: &Vec<bool>,
    a3: &Vec<i8>,
    a4: &Vec<u8>,
    a5: &Vec<i16>,
    a6: &Vec<u16>,
    a7: &Vec<i32>,
    a8: &Vec<u32>,
    a9: &Vec<i64>,
    a10: &Vec<u64>,
    a11: &Vec<f32>,
    a12: &Vec<f64>
) -> bool {
    a1.as_slice() == ['a' as u16, 'b' as u16] &&
        a2.as_slice() == [true, false] &&
        a3.as_slice() == [1, 2] &&
        a4.as_slice() == [1, 255] &&
        a5.as_slice() == [1, 2] &&
        a6.as_slice() == [1, 65535] &&
        a7.as_slice() == [1, 2] &&
        a8.as_slice() == [1, 4294967295] &&
        a9.as_slice() == [1, 2] &&
        a10.as_slice() == [1, 18446744073709551615] &&
        a11.as_slice() == [1.1, 2.2] &&
        a12.as_slice() == [1.1, 2.2]
}

pub fn critical_enum_array(
    a1: &Vec<MyEnum>
) -> bool {
    a1.as_slice() == [MyEnum::CASE1, MyEnum::CASE2]
}

pub fn critical_primitives_array_n(
    a1: &Option<Vec<u16>>,
    a2: &Option<Vec<bool>>,
    a3: &Option<Vec<i8>>,
    a4: &Option<Vec<u8>>,
    a5: &Option<Vec<i16>>,
    a6: &Option<Vec<u16>>,
    a7: &Option<Vec<i32>>,
    a8: &Option<Vec<u32>>,
    a9: &Option<Vec<i64>>,
    a10: &Option<Vec<u64>>,
    a11: &Option<Vec<f32>>,
    a12: &Option<Vec<f64>>
) -> bool {
    a1.is_none() && a2.is_none() && a3.is_none() &&
        a4.is_none() && a5.is_none() && a6.is_none() &&
        a7.is_none() && a8.is_none() && a9.is_none() &&
        a10.is_none() && a11.is_none() && a12.is_none()
}

pub fn critical_enum_array_n(
    a1: &Option<Vec<MyEnum>>
) -> bool {
    a1.is_none()
}

pub fn critical_return_char() -> u16 {
    'a' as u16
}

pub fn critical_return_boolean() -> bool {
    true
}

pub fn critical_return_byte() -> i8 {
    1
}

pub fn critical_return_ubyte() -> u8 {
    255
}

pub fn critical_return_short() -> i16 {
    1
}

pub fn critical_return_ushort() -> u16 {
    65535
}

pub fn critical_return_int() -> i32 {
    1
}

pub fn critical_return_uint() -> u32 {
    4294967295
}

pub fn critical_return_long() -> i64 {
    1
}

pub fn critical_return_ulong() -> u64 {
    18446744073709551615
}

pub fn critical_return_float() -> f32 {
    1.0
}

pub fn critical_return_double() -> f64 {
    1.0
}

pub fn critical_return_enum() -> MyEnum {
    MyEnum::CASE1
}

pub fn critical_return_interface() -> Arc<MyInterface> {
    Arc::new(MyInterface::new())
}

pub fn jvmci1() -> bool {
    true
}

pub fn jvmci2(
    a1: i32
) -> bool {
    a1 == 1
}

pub fn jvmci3(
    a1: i32,
    a2: i32
) -> bool {
    a1 == 1 && a2 == 2
}

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
) -> bool {
    a1 == 1 && a2 == 2 && a3 == 3 &&
        a4 == 4 && a5 == 5 && a6 == 6 &&
        a7 == 7 && a8 == 8 && a9 == 9
}

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
) -> bool {
    a1 == 1 && a2 == 2 && a3 == 3 &&
        a4 == 4 && a5 == 5 && a6 == 6 &&
        a7 == 7 && a8 == 8 && a9 == 9
}

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
) -> bool {
    f32_cmp(a1, 1.0) && f32_cmp(a2, 2.0) && f32_cmp(a3, 3.0) &&
        f32_cmp(a4, 4.0) && f32_cmp(a5, 5.0) &&
        f32_cmp(a6, 6.0) && f32_cmp(a7, 7.0) && f32_cmp(a8, 8.0) &&
        f32_cmp(a9, 9.0) && a10 == 10 && a11 == 11 &&
        a12 == 12 && a13 == 13
}

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
) -> bool {
    f32_cmp(a1, 1.0) && f64_cmp(a2, 2.0) && f32_cmp(a3, 3.0) &&
        f64_cmp(a4, 4.0) && f32_cmp(a5, 5.0) && f64_cmp(a6, 6.0) &&
        f32_cmp(a7, 7.0) && f32_cmp(a8, 8.0) && f64_cmp(a9, 9.0)
}

pub fn jvmci8(
    a1: i32,
    a2: f64,
    a3: f32,
    a4: i64
) -> bool {
    a1 == 1 && f64_cmp(a2, 2.0) && f32_cmp(a3, 3.0) && a4 == 4
}

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
) -> bool {
    a1 == 1 && f64_cmp(a2, 2.0) && f32_cmp(a3, 3.0) &&
        a4 == 4 && a5 == 5 && f64_cmp(a6, 6.0) &&
        f32_cmp(a7, 7.0) && f32_cmp(a8, 8.0) && a9 == 9
}

pub fn jvmci10(
    a1: &String,
    a2: f64,
    a3: f32,
    a4: i64,
    a5: i64,
    a6: f64,
    a7: &String,
    a8: f32,
    a9: i32
) -> bool {
    a1.as_str() == "string1" && f64_cmp(a2, 2.0) && f32_cmp(a3, 3.0) &&
        a4 == 4 && a5 == 5 && f64_cmp(a6, 6.0) &&
        a7.as_str() == "string7" && f32_cmp(a8, 8.0) && a9 == 9
}

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
) -> bool {
    f32_cmp(a1, 1.0) && a2 == 2 && f32_cmp(a3, 3.0) &&
        a4 == 4 && f32_cmp(a5, 5.0) && a6 == 6 &&
        f32_cmp(a7, 7.0) && a8 == 8 && f32_cmp(a9 , 9.0) &&
        a10 == 10 && f32_cmp(a11, 11.0) && a12 == 12 &&
        f32_cmp(a13, 13.0) && a14 == 14 && f32_cmp(a15, 15.0) &&
        a16 == 16 && f32_cmp(a17, 17.0)
}

pub fn jvmci12() -> i32 {
    1
}

pub fn jvmci13() -> i64 {
    1
}

pub fn jvmci14() -> f32 {
    1.5
}

pub fn jvmci15() -> f64 {
    1.5
}

pub fn jvmci_array(
    array: &Vec<i32>
) -> bool {
    array.as_slice() == [1, 2, 3]
}

pub fn jvmci_some_arrays(
    array1: &Vec<i32>,
    array2: &Vec<f32>,
    array3: &Vec<f64>
) -> bool {
    array1.as_slice() == [1, 2, 3] &&
        array2.as_slice() == [4.0, 5.0, 6.0] &&
        array3.as_slice() == [7.0, 8.0, 9.0]
}

pub fn jvmci_enum(
    enum1: MyEnum,
    enum2: MyEnum,
    enum_array: &Vec<MyEnum>
) -> bool {
    enum1 == MyEnum::CASE1 &&
        enum2 == MyEnum::CASE2 &&
        enum_array.as_slice() == [MyEnum::CASE1, MyEnum::CASE2, MyEnum::CASE1]
}