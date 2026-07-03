#include <api.h>

KBoolean callback_arg_char_array(CallbackPassCharArray* arg) {
    return arg->invoke(arg, KCharArray_of('a', 'b'));
}

KBoolean callback_arg_char_array_n(CallbackPassCharArrayN* arg) {
    return arg->invoke(arg, NULL);
}

KBoolean callback_arg_boolean_array(CallbackPassBooleanArray* arg) {
    return arg->invoke(arg, KBooleanArray_of(true, false));
}

KBoolean callback_arg_byte_array(CallbackPassByteArray* arg) {
    return arg->invoke(arg, KByteArray_of(1, 2));
}

KBoolean callback_arg_ubyte_array(CallbackPassUByteArray* arg) {
    return arg->invoke(arg, KUByteArray_of(1, 255u));
}

KBoolean callback_arg_short_array(CallbackPassShortArray* arg) {
    return arg->invoke(arg, KShortArray_of(1, 2));
}

KBoolean callback_arg_ushort_array(CallbackPassUShortArray* arg) {
    return arg->invoke(arg, KUShortArray_of(1, 65535u));
}

KBoolean callback_arg_int_array(CallbackPassIntArray* arg) {
    return arg->invoke(arg, KIntArray_of(1, 2));
}

KBoolean callback_arg_uint_array(CallbackPassUIntArray* arg) {
    return arg->invoke(arg, KUIntArray_of(1, 4294967295u));
}

KBoolean callback_arg_long_array(CallbackPassLongArray* arg) {
    KLong* elements = malloc(2 * sizeof(KLong));
    elements[0] = 1;
    elements[1] = 2;
    return arg->invoke(arg, KLongArray_new(elements, 2, true));
}

KBoolean callback_arg_ulong_array(CallbackPassULongArray* arg) {
    KULong* elements = malloc(2 * sizeof(KULong));
    elements[0] = 1;
    elements[1] = 18446744073709551615u;
    return arg->invoke(arg, KULongArray_new(elements, 2, true));
}

KBoolean callback_arg_float_array(CallbackPassFloatArray* arg) {
    return arg->invoke(arg, KFloatArray_of(1.1f, 2.2f));
}

KBoolean callback_arg_double_array(CallbackPassDoubleArray* arg) {
    return arg->invoke(arg, KDoubleArray_of(1.1, 2.2));
}

KBoolean callback_arg_string_array(CallbackPassStringArray* arg) {
    return arg->invoke(arg, KArray_of(
        KString_new("string1", 7, 7, false),
        KString_new("string2", 7, 7, false)
    ));
}

KBoolean callback_arg_string_array_n(CallbackPassStringArrayN* arg) {
    return arg->invoke(arg, KArray_of(NULL, NULL));
}

KBoolean callback_arg_enum_array(CallbackPassEnumArray* arg) {
    return arg->invoke(arg, KIntArray_of(MyEnum_CASE1, MyEnum_CASE2));
}

KBoolean callback_arg_dictionary_array(CallbackPassDictionaryArray* arg) {
    return arg->invoke(arg, KArray_of(
        MyDictionary_new(1, 2, 3, 4),
        MyDictionary_new(5, 6, 7, 8)
    ));
}

KBoolean callback_arg_dictionary_array_n(CallbackPassDictionaryArrayN* arg) {
    return arg->invoke(arg, KArray_of(NULL, NULL));
}