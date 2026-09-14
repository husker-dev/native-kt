#include <api.hpp>

void return_void() {
}

uint16_t return_char() {
    return 'a';
}

bool return_boolean() {
    return true;
}

int8_t return_byte() {
    return 99;
}

uint8_t return_ubyte() {
    return 255u;
}

int16_t return_short() {
    return 99;
}

uint16_t return_ushort() {
    return 65535u;
}

int32_t return_int() {
    return 99;
}

uint32_t return_uint() {
    return 4294967295u;
}

int64_t return_long() {
    return 9223372036854775805;
}

uint64_t return_ulong() {
    return 18446744073709551615u;
}

float return_float() {
    return 99;
}

double return_double() {
    return 99.0;
}

KString return_string() {
    return KString("test string");
}

KOptional<KString> return_string_n() {
    return KOptional<KString>();
}

MyEnum return_enum() {
    return CASE2;
}

MyDictionary return_dictionary() {
    return MyDictionary(1, 2, 3, 4);
}

KOptional<MyDictionary> return_dictionary_n() {
    return KOptional<MyDictionary>();
}