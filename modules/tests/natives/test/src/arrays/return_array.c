#include <api.h>
#include <stdlib.h>
#include <string.h>

KCharArray* return_char_array(void) {
    return kchar_array_of('a', 'b');
}

KCharArray* return_char_array_empty(void) {
    return kchar_array_of_n(0);
}

KCharArray* return_char_array_n(void) {
    return NULL;
}

KArray* return_char_narray(void) {
    return karray_of(NULL, boxed_char_new('a'));
}

KBooleanArray* return_boolean_array(void) {
    return kboolean_array_of(true, false);
}

KArray* return_boolean_narray(void) {
    return karray_of(NULL, boxed_boolean_new(true));
}

KByteArray* return_byte_array(void) {
    return kbyte_array_of(1, 2);
}

KArray* return_byte_narray(void) {
    return karray_of(NULL, boxed_byte_new(1));
}

KUByteArray* return_ubyte_array(void) {
    return kubyte_array_of(1, 255u);
}

KArray* return_ubyte_narray(void) {
    return karray_of(NULL, boxed_byte_new(255u));
}

KShortArray* return_short_array(void) {
    return kshort_array_of(1, 2);
}

KArray* return_short_narray(void) {
    return karray_of(NULL, boxed_short_new(1));
}

KUShortArray* return_ushort_array(void) {
    return kushort_array_of(1, 65535u);
}

KArray* return_ushort_narray(void) {
    return karray_of(NULL, boxed_short_new(65535u));
}

KIntArray* return_int_array(void) {
    return kint_array_of(1, 2);
}

KArray* return_int_narray(void) {
    return karray_of(NULL, boxed_int_new(1));
}

KUIntArray* return_uint_array(void) {
    return kuint_array_of(1, 4294967295u);
}

KArray* return_uint_narray(void) {
    return karray_of(NULL, boxed_int_new(4294967295u));
}

KLongArray* return_long_array(void) {
    return klong_array_new((int64_t[]){ 1, 2 }, 2, true);
}

KArray* return_long_narray(void) {
    return karray_of(NULL, boxed_long_new(1));
}

KULongArray* return_ulong_array(void) {
    return kulong_array_new((uint64_t[]){ 1, 18446744073709551615u }, 2, true);
}

KArray* return_ulong_narray(void) {
    return karray_of(NULL, boxed_long_new(18446744073709551615u));
}

KFloatArray* return_float_array(void) {
    return kfloat_array_of(1.1f, 2.2f);
}

KArray* return_float_narray(void) {
    return karray_of(NULL, boxed_float_new(1.1f));
}

KDoubleArray* return_double_array(void) {
    return kdouble_array_of(1.1, 2.2);
}

KArray* return_double_narray(void) {
    return karray_of(NULL, boxed_double_new(1.1));
}

KArray* return_string_array(void) {
    return karray_of(
        kstring_new("string1"),
        kstring_new("string2")
    );
}

KArray* return_string_array_n(void) {
    return karray_of(NULL, NULL);
}

KIntArray* return_enum_array(void) {
    return kint_array_of(MyEnum_CASE1, MyEnum_CASE2);
}

KArray* return_enum_narray(void) {
    return karray_of(NULL, boxed_int_new(MyEnum_CASE2));
}

KArray* return_dictionary_array(void) {
    return karray_of(
        MyDictionary_new(1, 2, 3, 4),
        MyDictionary_new(5, 6, 7, 8)
    );
}

KArray* return_dictionary_array_n(void) {
    return karray_of(NULL, NULL);
}