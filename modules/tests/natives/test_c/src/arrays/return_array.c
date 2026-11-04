#include <nativekt.h>
#include <stdlib.h>
#include <string.h>

KCharArray* return_char_array(void) {
    return k_char_array_of('a', 'b');
}

KCharArray* return_char_array_empty(void) {
    return k_char_array_of_n(0);
}

KCharArray* return_char_array_n(void) {
    return NULL;
}

KArray* return_char_n_array(void) {
    return k_array_of(NULL, boxed_char_new('a'));
}

KBooleanArray* return_boolean_array(void) {
    return k_boolean_array_of(true, false);
}

KArray* return_boolean_n_array(void) {
    return k_array_of(NULL, boxed_boolean_new(true));
}

KByteArray* return_byte_array(void) {
    return k_byte_array_of(1, 2);
}

KArray* return_byte_n_array(void) {
    return k_array_of(NULL, boxed_byte_new(1));
}

KUByteArray* return_ubyte_array(void) {
    return k_ubyte_array_of(1, 255u);
}

KArray* return_ubyte_n_array(void) {
    return k_array_of(NULL, boxed_byte_new(255u));
}

KShortArray* return_short_array(void) {
    return k_short_array_of(1, 2);
}

KArray* return_short_n_array(void) {
    return k_array_of(NULL, boxed_short_new(1));
}

KUShortArray* return_ushort_array(void) {
    return k_ushort_array_of(1, 65535u);
}

KArray* return_ushort_n_array(void) {
    return k_array_of(NULL, boxed_short_new(65535u));
}

KIntArray* return_int_array(void) {
    return k_int_array_of(1, 2);
}

KArray* return_int_n_array(void) {
    return k_array_of(NULL, boxed_int_new(1));
}

KUIntArray* return_uint_array(void) {
    return k_uint_array_of(1, 4294967295u);
}

KArray* return_uint_n_array(void) {
    return k_array_of(NULL, boxed_int_new(4294967295u));
}

KLongArray* return_long_array(void) {
    return k_long_array_new((int64_t[]){ 1, 2 }, 2, true);
}

KArray* return_long_n_array(void) {
    return k_array_of(NULL, boxed_long_new(1));
}

KULongArray* return_ulong_array(void) {
    return k_ulong_array_new((uint64_t[]){ 1, 18446744073709551615u }, 2, true);
}

KArray* return_ulong_n_array(void) {
    return k_array_of(NULL, boxed_long_new(18446744073709551615u));
}

KFloatArray* return_float_array(void) {
    return k_float_array_of(1.1f, 2.2f);
}

KArray* return_float_n_array(void) {
    return k_array_of(NULL, boxed_float_new(1.1f));
}

KDoubleArray* return_double_array(void) {
    return k_double_array_of(1.1, 2.2);
}

KArray* return_double_n_array(void) {
    return k_array_of(NULL, boxed_double_new(1.1));
}

KArray* return_string_array(void) {
    return k_array_of(
        k_string_new("string1"),
        k_string_new("string2")
    );
}

KArray* return_string_array_n(void) {
    return k_array_of(NULL, NULL);
}

KIntArray* return_enum_array(void) {
    return k_int_array_of(MyEnum_CASE1, MyEnum_CASE2);
}

KArray* return_enum_n_array(void) {
    return k_array_of(NULL, boxed_int_new(MyEnum_CASE2));
}

KArray* return_dictionary_array(void) {
    return k_array_of(
        MyDictionary_new(1, 2, 3, 4),
        MyDictionary_new(5, 6, 7, 8)
    );
}

KArray* return_dictionary_array_n(void) {
    return k_array_of(NULL, NULL);
}