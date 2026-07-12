#include <api.hpp>

KCharArray* return_char_array() {
    return KCharArray::of({'a', 'b'});
}

KCharArray* return_char_array_n() {
    return nullptr;
}

KBooleanArray* return_boolean_array() {
    return KBooleanArray::of({true, false});
}

KByteArray* return_byte_array() {
    return KByteArray::of({1, 2});
}

KUByteArray* return_ubyte_array() {
    return KUByteArray::of({1, 255u});
}

KShortArray* return_short_array() {
    return KShortArray::of({1, 2});
}

KUShortArray* return_ushort_array() {
    return KUShortArray::of({1, 65535u});
}

KIntArray* return_int_array() {
    return KIntArray::of({1, 2});
}

KUIntArray* return_uint_array() {
    return KUIntArray::of({1, 4294967295u});
}

KLongArray* return_long_array() {
    return KLongArray::of({1, 2});
}

KULongArray* return_ulong_array() {
    return KULongArray::of({1, 18446744073709551615u});
}

KFloatArray* return_float_array() {
    return KFloatArray::of({1.1f, 2.2f});
}

KDoubleArray* return_double_array() {
    return KDoubleArray::of({1.1, 2.2});
}

KArray* return_string_array() {
    return KArray::of({
        new KString("string1", 7, 7, false),
        new KString("string2", 7, 7, false)
    });
}

KArray* return_string_array_n() {
    return KArray::of({nullptr, nullptr});
}

KIntArray* return_enum_array() {
    return KIntArray::of({CASE1, CASE2});
}

KArray* return_dictionary_array() {
    return KArray::of({
        new MyDictionary(1, 2, 3, 4),
        new MyDictionary(5, 6, 7, 8)
    });
}

KArray* return_dictionary_array_n() {
    return KArray::of({nullptr, nullptr});
}