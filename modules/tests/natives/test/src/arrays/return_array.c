#include <api.h>
#include <stdlib.h>
#include <string.h>

KCharArray* return_char_array(void) {
    return kchar_array_of('a', 'b');
}

KCharArray* return_char_array_n(void) {
    return NULL;
}

KBooleanArray* return_boolean_array(void) {
    return kboolean_array_of(true, false);
}

KByteArray* return_byte_array(void) {
    return kbyte_array_of(1, 2);
}

KUByteArray* return_ubyte_array(void) {
    return kubyte_array_of(1, 255u);
}

KShortArray* return_short_array(void) {
    return kshort_array_of(1, 2);
}

KUShortArray* return_ushort_array(void) {
    return kushort_array_of(1, 65535u);
}

KIntArray* return_int_array(void) {
    return kint_array_of(1, 2);
}

KUIntArray* return_uint_array(void) {
    return kuint_array_of(1, 4294967295u);
}

KLongArray* return_long_array(void) {
    return klong_array_new((int64_t[]){ 1, 2 }, 2, true);
}

KULongArray* return_ulong_array(void) {
    return kulong_array_new((uint64_t[]){ 1, 18446744073709551615u }, 2, true);
}

KFloatArray* return_float_array(void) {
    return kfloat_array_of(1.1f, 2.2f);
}

KDoubleArray* return_double_array(void) {
    return kdouble_array_of(1.1, 2.2);
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

KArray* return_dictionary_array(void) {
    return karray_of(
        MyDictionary_new(1, 2, 3, 4),
        MyDictionary_new(5, 6, 7, 8)
    );
}

KArray* return_dictionary_array_n(void) {
    return karray_of(NULL, NULL);
}