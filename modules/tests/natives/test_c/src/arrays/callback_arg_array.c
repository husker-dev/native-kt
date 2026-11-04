#include <nativekt.h>
#include <string.h>

bool callback_arg_char_array(RC_CallbackPassCharArray* arg) {
    return callbackpasschararray_invoke(arg, k_char_array_of('a', 'b'));
}

bool callback_arg_char_array_empty(RC_CallbackPassCharArrayEmpty* arg) {
    return callbackpasschararrayempty_invoke(arg, k_char_array_of_n(0));
}

bool callback_arg_char_array_n(RC_CallbackPassCharArrayN* arg) {
    return callbackpasschararrayn_invoke(arg, NULL);
}

bool callback_arg_boolean_array(RC_CallbackPassBooleanArray* arg) {
    return callbackpassbooleanarray_invoke(arg, k_boolean_array_of(true, false));
}

bool callback_arg_byte_array(RC_CallbackPassByteArray* arg) {
    return callbackpassbytearray_invoke(arg, k_byte_array_of(1, 2));
}

bool callback_arg_ubyte_array(RC_CallbackPassUByteArray* arg) {
    return callbackpassubytearray_invoke(arg, k_ubyte_array_of(1, 255u));
}

bool callback_arg_short_array(RC_CallbackPassShortArray* arg) {
    return callbackpassshortarray_invoke(arg, k_short_array_of(1, 2));
}

bool callback_arg_ushort_array(RC_CallbackPassUShortArray* arg) {
    return callbackpassushortarray_invoke(arg, k_ushort_array_of(1, 65535u));
}

bool callback_arg_int_array(RC_CallbackPassIntArray* arg) {
    return callbackpassintarray_invoke(arg, k_int_array_of(1, 2));
}

bool callback_arg_uint_array(RC_CallbackPassUIntArray* arg) {
    return callbackpassuintarray_invoke(arg, k_uint_array_of(1, 4294967295u));
}

bool callback_arg_long_array(RC_CallbackPassLongArray* arg) {
    return callbackpasslongarray_invoke(arg, k_long_array_new((int64_t[]){ 1, 2 }, 2, true));
}

bool callback_arg_ulong_array(RC_CallbackPassULongArray* arg) {
    return callbackpassulongarray_invoke(arg, k_ulong_array_new((uint64_t[]){ 1, 18446744073709551615u }, 2, true));
}

bool callback_arg_float_array(RC_CallbackPassFloatArray* arg) {
    return callbackpassfloatarray_invoke(arg, k_float_array_of(1.1f, 2.2f));
}

bool callback_arg_double_array(RC_CallbackPassDoubleArray* arg) {
    return callbackpassdoublearray_invoke(arg, k_double_array_of(1.1, 2.2));
}

bool callback_arg_string_array(RC_CallbackPassStringArray* arg) {
    return callbackpassstringarray_invoke(arg, k_array_of(
        k_string_new("string1"),
        k_string_new("string2")
    ));
}

bool callback_arg_string_array_n(RC_CallbackPassStringArrayN* arg) {
    return callbackpassstringarrayn_invoke(arg, k_array_of(NULL, NULL));
}

bool callback_arg_enum_array(RC_CallbackPassEnumArray* arg) {
    return callbackpassenumarray_invoke(arg, k_int_array_of(MyEnum_CASE1, MyEnum_CASE2));
}

bool callback_arg_dictionary_array(RC_CallbackPassDictionaryArray* arg) {
    return callbackpassdictionaryarray_invoke(arg, k_array_of(
        MyDictionary_new(1, 2, 3, 4),
        MyDictionary_new(5, 6, 7, 8)
    ));
}

bool callback_arg_dictionary_array_n(RC_CallbackPassDictionaryArrayN* arg) {
    return callbackpassdictionaryarrayn_invoke(arg, k_array_of(NULL, NULL));
}

bool callback_arg_interface_array(RC_CallbackPassInterfaceArray* arg) {
    return callbackpassinterfacearray_invoke(arg, k_array_of(
        rc_myinterface_new(_interface_myinterface_new_0()),
        rc_myinterface_new(_interface_myinterface_new_0())
    ));
}

bool callback_arg_interface_array_n(RC_CallbackPassInterfaceArrayN* arg) {
    return callbackpassinterfacearrayn_invoke(arg, k_array_of(NULL, NULL));
}