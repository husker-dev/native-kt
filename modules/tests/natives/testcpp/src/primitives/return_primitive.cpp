#include <api.hpp>

void return_void() {
}

KChar return_char() {
    return 'a';
}

KBoolean return_boolean() {
    return true;
}

KByte return_byte() {
    return 99;
}

KUByte return_ubyte() {
    return 255u;
}

KShort return_short() {
    return 99;
}

KUShort return_ushort() {
    return 65535u;
}

KInt return_int() {
    return 99;
}

KUInt return_uint() {
    return 4294967295u;
}

KLong return_long() {
    return 9223372036854775805;
}

KULong return_ulong() {
    return 18446744073709551615u;
}

KFloat return_float() {
    return 99;
}

KDouble return_double() {
    return 99.0;
}

KString* return_string() {
    return new KString("test string", 11, 11, false);
}

KString* return_string_n() {
    return nullptr;
}

MyEnum return_enum() {
    return CASE2;
}

MyDictionary* return_dictionary() {
    return new MyDictionary(1, 2, 3, 4);
}

MyDictionary* return_dictionary_n() {
    return nullptr;
}