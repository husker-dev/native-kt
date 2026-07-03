#include <api.h>
#include <stdlib.h>

KCharArray* return_char_array() {
    return KCharArray_of('a', 'b');
}

KCharArray* return_char_array_n() {
    return NULL;
}

KBooleanArray* return_boolean_array() {
    return KBooleanArray_of(true, false);
}

KByteArray* return_byte_array() {
    return KByteArray_of(1, 2);
}

KUByteArray* return_ubyte_array() {
    return KUByteArray_of(1, 255u);
}

KShortArray* return_short_array() {
    return KShortArray_of(1, 2);
}

KUShortArray* return_ushort_array() {
    return KUShortArray_of(1, 65535u);
}

KIntArray* return_int_array() {
    return KIntArray_of(1, 2);
}

KUIntArray* return_uint_array() {
    return KUIntArray_of(1, 4294967295u);
}

KLongArray* return_long_array() {
    KLong* elements = malloc(2 * sizeof(KLong));
    elements[0] = 1;
    elements[1] = 2;
    return KLongArray_new(elements, 2, true);
}

KULongArray* return_ulong_array() {
    KULong* elements = malloc(2 * sizeof(KULong));
    elements[0] = 1;
    elements[1] = 18446744073709551615u;
    return KULongArray_new(elements, 2, true);
}

KFloatArray* return_float_array() {
    return KFloatArray_of(1.1f, 2.2f);
}

KDoubleArray* return_double_array() {
    return KDoubleArray_of(1.1, 2.2);
}

KArray* return_string_array() {
    return KArray_of(
        KString_new("string1", 7, 7, false),
        KString_new("string2", 7, 7, false)
    );
}

KArray* return_string_array_n() {
    return KArray_of(NULL, NULL);
}

KIntArray* return_enum_array() {
    return KIntArray_of(MyEnum_CASE1, MyEnum_CASE2);
}

KArray* return_dictionary_array() {
    return KArray_of(
        MyDictionary_new(1, 2, 3, 4),
        MyDictionary_new(5, 6, 7, 8)
    );
}

KArray* return_dictionary_array_n() {
    return KArray_of(NULL, NULL);
}