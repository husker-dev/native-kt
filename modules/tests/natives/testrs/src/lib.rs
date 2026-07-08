use std::sync::Arc;
use crate::nativekt::*;

mod nativekt;

pub struct B;

impl B {
    fn new() -> Self { 
        B {}
    }
}

pub struct A;

impl A {
    fn new() -> Self {
        A {}
    }
    fn test(&self, _arg: Arc<B>) {
        println!("Hello from interface!")
    }
}

fn f32_cmp(f1: f32, f2: f32) -> bool {
    (f1 - f2).abs() < 0.00001
}

fn f64_cmp(f1: f64, f2: f64) -> bool {
    (f1 - f2).abs() < 0.00001
}

pub fn test_func(
    arg1: Option<KString>,
    arg2: i32
) -> KString {
    let arg1 = arg1.unwrap_or(KString::from_str("null"));

    println!("rust: {}, {}", arg1.as_str(), arg2);

    KString::from_str("Bye! Пока!")
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
    arg: KString
) -> bool {
    arg.as_str() == "test string"
}

pub fn pass_string_n(
    arg: Option<KString>
) -> bool {
    arg.is_none()
}

pub fn pass_enum(
    arg: MyEnum
) -> bool {
    arg == MyEnum::CASE2
}

pub fn pass_dictionary(
    arg: MyDictionary
) -> bool {
    arg.a == 1 &&
    arg.b == 2 &&
    arg.c == 3 &&
    arg.d == 4
}

pub fn pass_dictionary_n(
    arg: Option<MyDictionary>
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

pub fn return_string() -> KString {
    k_string!("test string".to_string())
}

pub fn return_string_n() -> Option<KString> {
    None
}

pub fn return_enum() -> MyEnum {
    MyEnum::CASE2
}

pub fn return_dictionary() -> MyDictionary {
    MyDictionary::new(1, 2, 3, 4)
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
    arg: KString
) -> KString {
    arg
}

pub fn ping_string_n(
    arg: Option<KString>
) -> Option<KString> {
    arg
}

pub fn ping_enum(
    arg: MyEnum
) -> MyEnum {
    arg
}

pub fn ping_dictionary(
    arg: MyDictionary
) -> MyDictionary {
    arg
}

pub fn ping_dictionary_n(
    arg: Option<MyDictionary>
) -> Option<MyDictionary> {
    arg
}

pub fn callback_void(
    arg: VoidCallback
) {
    arg.invoke()
}

pub fn callback_void_n(
    arg: Option<VoidCallback>
) -> bool {
    arg.is_none()
}

pub fn callback_arg_char(
    arg: CallbackPassChar
) -> bool {
    arg.invoke('a' as u16)
}

pub fn callback_arg_boolean(
    arg: CallbackPassBoolean
) -> bool {
    arg.invoke(true)
}

pub fn callback_arg_byte(
    arg: CallbackPassByte
) -> bool {
    arg.invoke(1)
}

pub fn callback_arg_ubyte(
    arg: CallbackPassUByte
) -> bool {
    arg.invoke(255)
}

pub fn callback_arg_short(
    arg: CallbackPassShort
) -> bool {
    arg.invoke(1)
}

pub fn callback_arg_ushort(
    arg: CallbackPassUShort
) -> bool {
    arg.invoke(65535)
}

pub fn callback_arg_int(
    arg: CallbackPassInt
) -> bool {
    arg.invoke(1)
}

pub fn callback_arg_uint(
    arg: CallbackPassUInt
) -> bool {
    arg.invoke(4294967295)
}

pub fn callback_arg_long(
    arg: CallbackPassLong
) -> bool {
    arg.invoke(1)
}

pub fn callback_arg_ulong(
    arg: CallbackPassULong
) -> bool {
    arg.invoke(18446744073709551615)
}

pub fn callback_arg_float(
    arg: CallbackPassFloat
) -> bool {
    arg.invoke(1.1)
}

pub fn callback_arg_double(
    arg: CallbackPassDouble
) -> bool {
    arg.invoke(1.1)
}

pub fn callback_arg_string(
    arg: CallbackPassString
) -> bool {
    arg.invoke(k_string!("test string".to_string()))
}

pub fn callback_arg_string_n(
    arg: CallbackPassStringN
) -> bool {
    arg.invoke(None)
}

pub fn callback_arg_callback(
    pass: VoidCallback,
    arg: CallbackPassCallback
) -> bool {
    arg.invoke(pass)
}

pub fn callback_arg_callback_n(
    arg: CallbackPassCallbackN
) -> bool {
    arg.invoke(None)
}

pub fn callback_arg_enum(
    arg: CallbackPassEnum
) -> bool {
    arg.invoke(MyEnum::CASE2)
}

pub fn callback_arg_dictionary(
    arg: CallbackPassDictionary
) -> bool {
    arg.invoke(MyDictionary::new(1, 2, 3, 4))
}

pub fn callback_arg_dictionary_n(
    arg: CallbackPassDictionaryN
) -> bool {
    arg.invoke(None)
}

pub fn callback_return_char(
    arg: CallbackReturnChar
) -> bool {
    arg.invoke() == 'a' as u16
}

pub fn callback_return_boolean(
    arg: CallbackReturnBoolean
) -> bool {
    arg.invoke() == true
}

pub fn callback_return_byte(
    arg: CallbackReturnByte
) -> bool {
    arg.invoke() == 1
}

pub fn callback_return_ubyte(
    arg: CallbackReturnUByte
) -> bool {
    arg.invoke() == 255
}

pub fn callback_return_short(
    arg: CallbackReturnShort
) -> bool {
    arg.invoke() == 1
}

pub fn callback_return_ushort(
    arg: CallbackReturnUShort
) -> bool {
    arg.invoke() == 65535
}

pub fn callback_return_int(
    arg: CallbackReturnInt
) -> bool {
    arg.invoke() == 1
}

pub fn callback_return_uint(
    arg: CallbackReturnUInt
) -> bool {
    arg.invoke() == 4294967295
}

pub fn callback_return_long(
    arg: CallbackReturnLong
) -> bool {
    arg.invoke() == 1
}

pub fn callback_return_ulong(
    arg: CallbackReturnULong
) -> bool {
    arg.invoke() == 18446744073709551615
}

pub fn callback_return_float(
    arg: CallbackReturnFloat
) -> bool {
    arg.invoke() == 1.1
}

pub fn callback_return_double(
    arg: CallbackReturnDouble
) -> bool {
    arg.invoke() == 1.1
}

pub fn callback_return_string(
    arg: CallbackReturnString
) -> bool {
    arg.invoke().as_str() == "test string"
}

pub fn callback_return_string_n(
    arg: CallbackReturnStringN
) -> bool {
    arg.invoke().is_none()
}

pub fn callback_return_callback(
    arg: CallbackReturnCallback
) -> VoidCallback {
    arg.invoke()
}

pub fn callback_return_callback_n(
    arg: CallbackReturnCallbackN
) -> bool {
    arg.invoke().is_none()
}

pub fn callback_return_enum(
    arg: CallbackReturnEnum
) -> bool {
    arg.invoke() == MyEnum::CASE2
}

pub fn callback_return_dictionary(
    arg: CallbackReturnDictionary
) -> bool {
    let result = arg.invoke();
    result.a == 1 &&
    result.b == 2 &&
    result.c == 3 &&
    result.d == 4
}

pub fn callback_return_dictionary_n(
    arg: CallbackReturnDictionaryN
) -> bool {
    arg.invoke().is_none()
}

pub fn pass_char_array(
    arg: KCharArray
) -> bool {
    arg.as_slice() == ['a' as u16, 'b' as u16]
}

pub fn pass_char_array_n(
    arg: Option<KCharArray>
) -> bool {
    arg.is_none()
}

pub fn pass_boolean_array(
    arg: KBooleanArray
) -> bool {
    arg.as_slice() == [true, false]
}

pub fn pass_byte_array(
    arg: KByteArray
) -> bool {
    arg.as_slice() == [1, 2]
}

pub fn pass_ubyte_array(
    arg: KUByteArray
) -> bool {
    arg.as_slice() == [1, 255]
}

pub fn pass_short_array(
    arg: KShortArray
) -> bool {
    arg.as_slice() == [1, 2]
}

pub fn pass_ushort_array(
    arg: KUShortArray
) -> bool {
    arg.as_slice() == [1, 65535]
}

pub fn pass_int_array(
    arg: KIntArray
) -> bool {
    arg.as_slice() == [1, 2]
}

pub fn pass_uint_array(
    arg: KUIntArray
) -> bool {
    arg.as_slice() == [1, 4294967295]
}

pub fn pass_long_array(
    arg: KLongArray
) -> bool {
    arg.as_slice() == [1, 2]
}

pub fn pass_ulong_array(
    arg: KULongArray
) -> bool {
    arg.as_slice() == [1, 18446744073709551615]
}

pub fn pass_float_array(
    arg: KFloatArray
) -> bool {
    arg.as_slice() == [1.1, 2.2]
}

pub fn pass_double_array(
    arg: KDoubleArray
) -> bool {
    arg.as_slice() == [1.1, 2.2]
}

pub fn pass_string_array(
    arg: KArray<KString>
) -> bool {
    arg.as_slice()[0].as_str() == "string1" &&
    arg.as_slice()[1].as_str() == "string2"
}

pub fn pass_string_array_n(
    arg: KArrayOpt<KString>
) -> bool {
    arg.as_slice()[0].is_none() &&
    arg.as_slice()[1].is_none()
}

pub fn pass_enum_array(
    arg: KIntArray
) -> bool {
    arg.as_slice()[0] == MyEnum::CASE1.to_int() &&
    arg.as_slice()[1] == MyEnum::CASE2.to_int()
}

pub fn pass_dictionary_array(
    arg: KArray<MyDictionary>
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
    arg: KArrayOpt<MyDictionary>
) -> bool {
    arg.as_slice()[0].is_none() && arg.as_slice()[1].is_none()
}

pub fn return_char_array() -> KCharArray {
    k_char_array!('a' as u16, 'b' as u16)
}

pub fn return_char_array_n() -> Option<KCharArray> {
    None
}

pub fn return_boolean_array() -> KBooleanArray {
    k_boolean_array!(true, false)
}

pub fn return_byte_array() -> KByteArray {
    k_byte_array!(1, 2)
}

pub fn return_ubyte_array() -> KUByteArray {
    k_ubyte_array!(1, 255)
}

pub fn return_short_array() -> KShortArray {
    k_short_array!(1, 2)
}

pub fn return_ushort_array() -> KUShortArray {
    k_ushort_array!(1, 65535)
}

pub fn return_int_array() -> KIntArray {
    k_int_array!(1, 2)
}

pub fn return_uint_array() -> KUIntArray {
    k_uint_array!(1, 4294967295)
}

pub fn return_long_array() -> KLongArray {
    k_long_array!(1, 2)
}

pub fn return_ulong_array() -> KULongArray {
    k_ulong_array!(1, 18446744073709551615)
}

pub fn return_float_array() -> KFloatArray {
    k_float_array!(1.1, 2.2)
}

pub fn return_double_array() -> KDoubleArray {
    k_double_array!(1.1, 2.2)
}

pub fn return_string_array() -> KArray<KString> {
    k_array!(KString::from_str("string1"), KString::from_str("string2"))
}

pub fn return_string_array_n() -> KArrayOpt<KString> {
    k_array_opt!(None, None)
}

pub fn return_enum_array() -> KIntArray {
    k_int_array!(MyEnum::CASE1.to_int(), MyEnum::CASE2.to_int())
}

pub fn return_dictionary_array() -> KArray<MyDictionary> {
    k_array!(
        MyDictionary::new(1, 2, 3, 4),
        MyDictionary::new(5, 6, 7, 8)
    )
}

pub fn return_dictionary_array_n() -> KArrayOpt<MyDictionary> {
    k_array_opt!(None, None)
}

pub fn ping_char_array(
    arg: KCharArray
) -> KCharArray {
    arg
}

pub fn ping_char_array_n(
    arg: Option<KCharArray>
) -> Option<KCharArray> {
    arg
}

pub fn ping_boolean_array(
    arg: KBooleanArray
) -> KBooleanArray {
    arg
}

pub fn ping_byte_array(
    arg: KByteArray
) -> KByteArray {
    arg
}

pub fn ping_ubyte_array(
    arg: KUByteArray
) -> KUByteArray {
    arg
}

pub fn ping_short_array(
    arg: KShortArray
) -> KShortArray {
    arg
}

pub fn ping_ushort_array(
    arg: KUShortArray
) -> KUShortArray {
    arg
}

pub fn ping_int_array(
    arg: KIntArray
) -> KIntArray {
    arg
}

pub fn ping_uint_array(
    arg: KUIntArray
) -> KUIntArray {
    arg
}

pub fn ping_long_array(
    arg: KLongArray
) -> KLongArray {
    arg
}

pub fn ping_ulong_array(
    arg: KULongArray
) -> KULongArray {
    arg
}

pub fn ping_float_array(
    arg: KFloatArray
) -> KFloatArray {
    arg
}

pub fn ping_double_array(
    arg: KDoubleArray
) -> KDoubleArray {
    arg
}

pub fn ping_string_array(
    arg: KArray<KString>
) -> KArray<KString> {
    arg
}

pub fn ping_string_array_n(
    arg: KArrayOpt<KString>
) -> KArrayOpt<KString> {
    arg
}

pub fn ping_enum_array(
    arg: KIntArray
) -> KIntArray {
    arg
}

pub fn ping_dictionary_array(
    arg: KArray<MyDictionary>
) -> KArray<MyDictionary> {
    arg
}

pub fn ping_dictionary_array_n(
    arg: KArrayOpt<MyDictionary>
) -> KArrayOpt<MyDictionary> {
    arg
}

pub fn callback_arg_char_array(
    arg: CallbackPassCharArray
) -> bool {
    arg.invoke(k_char_array!('a' as u16, 'b' as u16))
}

pub fn callback_arg_char_array_n(
    arg: CallbackPassCharArrayN
) -> bool {
    arg.invoke(None)
}

pub fn callback_arg_boolean_array(
    arg: CallbackPassBooleanArray
) -> bool {
    arg.invoke(k_boolean_array!(true, false))
}

pub fn callback_arg_byte_array(
    arg: CallbackPassByteArray
) -> bool {
    arg.invoke(k_byte_array!(1, 2))
}

pub fn callback_arg_ubyte_array(
    arg: CallbackPassUByteArray
) -> bool {
    arg.invoke(k_ubyte_array!(1, 255))
}

pub fn callback_arg_short_array(
    arg: CallbackPassShortArray
) -> bool {
    arg.invoke(k_short_array!(1, 2))
}

pub fn callback_arg_ushort_array(
    arg: CallbackPassUShortArray
) -> bool {
    arg.invoke(k_ushort_array!(1, 65535))
}

pub fn callback_arg_int_array(
    arg: CallbackPassIntArray
) -> bool {
    arg.invoke(k_int_array!(1, 2))
}

pub fn callback_arg_uint_array(
    arg: CallbackPassUIntArray
) -> bool {
    arg.invoke(k_uint_array!(1, 4294967295))
}

pub fn callback_arg_long_array(
    arg: CallbackPassLongArray
) -> bool {
    arg.invoke(k_long_array!(1, 2))
}

pub fn callback_arg_ulong_array(
    arg: CallbackPassULongArray
) -> bool {
    arg.invoke(k_ulong_array!(1, 18446744073709551615))
}

pub fn callback_arg_float_array(
    arg: CallbackPassFloatArray
) -> bool {
    arg.invoke(k_float_array!(1.1, 2.2))
}

pub fn callback_arg_double_array(
    arg: CallbackPassDoubleArray
) -> bool {
    arg.invoke(k_double_array!(1.1, 2.2))
}

pub fn callback_arg_string_array(
    arg: CallbackPassStringArray
) -> bool {
    arg.invoke(k_array!(
        KString::from_str("string1"),
        KString::from_str("string2")
    ))
}

pub fn callback_arg_string_array_n(
    arg: CallbackPassStringArrayN
) -> bool {
    arg.invoke(k_array_opt!(None, None))
}

pub fn callback_arg_enum_array(
    arg: CallbackPassEnumArray
) -> bool {
    arg.invoke(k_int_array!(
        MyEnum::CASE1.to_int(),
        MyEnum::CASE2.to_int()
    ))
}

pub fn callback_arg_dictionary_array(
    arg: CallbackPassDictionaryArray
) -> bool {
    arg.invoke(k_array!(
        MyDictionary::new(1, 2, 3, 4),
        MyDictionary::new(5, 6, 7, 8)
    ))
}

pub fn callback_arg_dictionary_array_n(
    arg: CallbackPassDictionaryArrayN
) -> bool {
    arg.invoke(k_array_opt!(None, None))
}

pub fn callback_return_char_array(
    arg: CallbackReturnCharArray
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 'a' as u16 &&
        arr[1] == 'b' as u16
}

pub fn callback_return_char_array_n(
    arg: CallbackReturnCharArrayN
) -> bool {
    arg.invoke().is_none()
}

pub fn callback_return_boolean_array(
    arg: CallbackReturnBooleanArray
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == true &&
        arr[1] == false
}

pub fn callback_return_byte_array(
    arg: CallbackReturnByteArray
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 2
}

pub fn callback_return_ubyte_array(
    arg: CallbackReturnUByteArray
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 255
}

pub fn callback_return_short_array(
    arg: CallbackReturnShortArray
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 2
}

pub fn callback_return_ushort_array(
    arg: CallbackReturnUShortArray
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 65535
}

pub fn callback_return_int_array(
    arg: CallbackReturnIntArray
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 2
}

pub fn callback_return_uint_array(
    arg: CallbackReturnUIntArray
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 4294967295
}

pub fn callback_return_long_array(
    arg: CallbackReturnLongArray
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 2
}

pub fn callback_return_ulong_array(
    arg: CallbackReturnULongArray
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1 &&
        arr[1] == 18446744073709551615
}

pub fn callback_return_float_array(
    arg: CallbackReturnFloatArray
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1.1 &&
        arr[1] == 2.2
}

pub fn callback_return_double_array(
    arg: CallbackReturnDoubleArray
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == 1.1 &&
        arr[1] == 2.2
}

pub fn callback_return_string_array(
    arg: CallbackReturnStringArray
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0].as_str() == "string1" &&
        arr[1].as_str() == "string2"
}

pub fn callback_return_string_array_n(
    arg: CallbackReturnStringArrayN
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0].is_none() &&
        arr[1].is_none()
}

pub fn callback_return_enum_array(
    arg: CallbackReturnEnumArray
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0] == MyEnum::CASE1.to_int() &&
        arr[1] == MyEnum::CASE2.to_int()
}

pub fn callback_return_dictionary_array(
    arg: CallbackReturnDictionaryArray
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
    arg: CallbackReturnDictionaryArrayN
) -> bool {
    let result = arg.invoke();
    let arr = result.as_slice();
    arr[0].is_none() &&
        arr[1].is_none()
}

pub fn pass_big_dictionary(
    arg: TypeDictionary
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
        // callback (a16) is skipped
        arg.a17.as_slice() == ['a' as u16, 'b' as u16] &&
        arg.a18.as_slice() == [true, false] &&
        arg.a19.as_slice() == [1, 2] &&
        arg.a20.as_slice() == [1, 2] &&
        arg.a21.as_slice() == [1, 2] &&
        arg.a22.as_slice() == [1, 2] &&
        arg.a23.as_slice() == [1, 2] &&
        arg.a24.as_slice() == [1, 2] &&
        arg.a25.as_slice() == [1, 2] &&
        arg.a26.as_slice() == [1, 2] &&
        arg.a27.as_slice() == [1.2, 3.4] &&
        arg.a28.as_slice() == [1.2, 3.4] &&
        arg.a29.as_slice()[0].as_str() == "string1" &&
        arg.a29.as_slice()[1].as_str() == "string2" &&
        arg.a30.as_slice() == [MyEnum::CASE1.to_int(), MyEnum::CASE2.to_int()] &&
        arg.a31.as_slice()[0].a == 1 &&
        arg.a31.as_slice()[0].b == 2 &&
        arg.a31.as_slice()[0].c == 3 &&
        arg.a31.as_slice()[0].d == 4 &&
        arg.a31.as_slice()[1].a == 5 &&
        arg.a31.as_slice()[1].b == 6 &&
        arg.a31.as_slice()[1].c == 7 &&
        arg.a31.as_slice()[1].d == 8
}

pub fn return_big_dictionary(
    callback: VoidCallback
) -> TypeDictionary {
    TypeDictionary::new(
        'a' as u16,
        true,
        123,
        123,
        123,
        123,
        123,
        123,
        9223372036854775807,
        9223372036854775807,
        123.0,
        123.4,
        KString::from_str("test string"),
        MyEnum::CASE2,
        MyDictionary::new(1, 2, 3, 4),
        callback,
        k_char_array!('a' as u16, 'b' as u16),
        k_boolean_array!(true, false),
        k_byte_array!(1, 2),
        k_ubyte_array!(1, 2),
        k_short_array!(1, 2),
        k_ushort_array!(1, 2),
        k_int_array!(1, 2),
        k_uint_array!(1, 2),
        k_long_array!(1, 2),
        k_ulong_array!(1, 2),
        k_float_array!(1.2, 3.4),
        k_double_array!(1.2, 3.4),
        k_array!(
            KString::from_str("string1"),
            KString::from_str("string2")
        ),
        k_int_array!(MyEnum::CASE1.to_int(), MyEnum::CASE2.to_int()),
        k_array!(
            MyDictionary::new(1, 2, 3, 4),
            MyDictionary::new(5, 6, 7, 8)
        )
    )
}

pub fn ping_big_dictionary(
    arg: TypeDictionary
) -> TypeDictionary {
    arg
}

pub fn pass_big_dictionary_n(
    arg: Option<TypeDictionary>
) -> bool {
    arg.is_none()
}

pub fn return_big_dictionary_n() -> Option<TypeDictionary> {
    None
}

pub fn ping_big_dictionary_n(
    arg: Option<TypeDictionary>
) -> Option<TypeDictionary> {
    arg
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
    a1: KString
) -> bool {
    a1.as_str() == "test string"
}

pub fn critical_string_n(
    a1: Option<KString>
) -> bool {
    a1.is_none()
}

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
    a1: KIntArray
) -> bool {
    a1.as_slice() == [MyEnum::CASE1.to_int(), MyEnum::CASE2.to_int()]
}

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
) -> bool {
    a1.is_none() && a2.is_none() && a3.is_none() &&
        a4.is_none() && a5.is_none() && a6.is_none() &&
        a7.is_none() && a8.is_none() && a9.is_none() &&
        a10.is_none() && a11.is_none() && a12.is_none()
}

pub fn critical_enum_array_n(
    a1: Option<KIntArray>
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
    a1: KString,
    a2: f64,
    a3: f32,
    a4: i64,
    a5: i64,
    a6: f64,
    a7: KString,
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
    array: KIntArray
) -> bool {
    array.as_slice() == [1, 2, 3]
}

pub fn jvmci_some_arrays(
    array1: KIntArray,
    array2: KFloatArray,
    array3: KDoubleArray
) -> bool {
    array1.as_slice() == [1, 2, 3] &&
        array2.as_slice() == [4.0, 5.0, 6.0] &&
        array3.as_slice() == [7.0, 8.0, 9.0]
}

pub fn jvmci_enum(
    enum1: MyEnum,
    enum2: MyEnum,
    enum_array: KIntArray
) -> bool {
    enum1 == MyEnum::CASE1 &&
        enum2 == MyEnum::CASE2 &&
        enum_array.as_slice() == [MyEnum::CASE1.to_int(), MyEnum::CASE2.to_int(), MyEnum::CASE1.to_int()]
}