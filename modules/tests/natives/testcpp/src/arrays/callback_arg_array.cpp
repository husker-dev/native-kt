#include <api.hpp>

KBoolean callback_arg_char_array(CallbackPassCharArray* arg) {
    return arg->invoke(arg, KCharArray::of({'a', 'b'}));
}

KBoolean callback_arg_char_array_n(CallbackPassCharArrayN* arg) {
    return arg->invoke(arg, nullptr);
}

KBoolean callback_arg_boolean_array(CallbackPassBooleanArray* arg) {
    return arg->invoke(arg, KBooleanArray::of({true, false}));
}

KBoolean callback_arg_byte_array(CallbackPassByteArray* arg) {
    return arg->invoke(arg, KByteArray::of({1, 2}));
}

KBoolean callback_arg_ubyte_array(CallbackPassUByteArray* arg) {
    return arg->invoke(arg, KUByteArray::of({1, 255u}));
}

KBoolean callback_arg_short_array(CallbackPassShortArray* arg) {
    return arg->invoke(arg, KShortArray::of({1, 2}));
}

KBoolean callback_arg_ushort_array(CallbackPassUShortArray* arg) {
    return arg->invoke(arg, KUShortArray::of({1, 65535u}));
}

KBoolean callback_arg_int_array(CallbackPassIntArray* arg) {
    return arg->invoke(arg, KIntArray::of({1, 2}));
}

KBoolean callback_arg_uint_array(CallbackPassUIntArray* arg) {
    return arg->invoke(arg, KUIntArray::of({1, 4294967295u}));
}

KBoolean callback_arg_long_array(CallbackPassLongArray* arg) {
    return arg->invoke(arg, KLongArray::of({1, 2}));
}

KBoolean callback_arg_ulong_array(CallbackPassULongArray* arg) {
    return arg->invoke(arg, KULongArray::of({1, 18446744073709551615u}));
}

KBoolean callback_arg_float_array(CallbackPassFloatArray* arg) {
    return arg->invoke(arg, KFloatArray::of({1.1f, 2.2f}));
}

KBoolean callback_arg_double_array(CallbackPassDoubleArray* arg) {
    return arg->invoke(arg, KDoubleArray::of({1.1, 2.2}));
}

KBoolean callback_arg_string_array(CallbackPassStringArray* arg) {
    return arg->invoke(arg, KArray::of({
        new KString("string1", 7, 7, false),
        new KString("string2", 7, 7, false)
    }));
}

KBoolean callback_arg_string_array_n(CallbackPassStringArrayN* arg) {
    return arg->invoke(arg, KArray::of({nullptr, nullptr}));
}

KBoolean callback_arg_enum_array(CallbackPassEnumArray* arg) {
    return arg->invoke(arg, KIntArray::of({CASE1, CASE2}));
}

KBoolean callback_arg_dictionary_array(CallbackPassDictionaryArray* arg) {
    return arg->invoke(arg, KArray::of({
        new MyDictionary(1, 2, 3, 4),
        new MyDictionary(5, 6, 7, 8)
    }));
}

KBoolean callback_arg_dictionary_array_n(CallbackPassDictionaryArrayN* arg) {
    return arg->invoke(arg, KArray::of({nullptr, nullptr}));
}