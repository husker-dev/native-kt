#include <api.hpp>

void return_void() {
}

uint16_t return_char() {
    return 'a';
}

KOptional<uint16_t> return_char_n() {
    return KOptional<uint16_t>();
}

bool return_boolean() {
    return true;
}

KOptional<bool> return_boolean_n() {
    return KOptional<bool>(true);
}

int8_t return_byte() {
    return 99;
}

KOptional<int8_t> return_byte_n() {
    return KOptional<int8_t>();
}

uint8_t return_ubyte() {
    return 255u;
}

KOptional<uint8_t> return_ubyte_n() {
    return KOptional<uint8_t>(255u);
}

int16_t return_short() {
    return 99;
}

KOptional<int16_t> return_short_n() {
    return KOptional<int16_t>();
}

uint16_t return_ushort() {
    return 65535u;
}

KOptional<uint16_t> return_ushort_n() {
    return KOptional<uint16_t>(65535u);
}

int32_t return_int() {
    return 99;
}

KOptional<int32_t> return_int_n() {
    return KOptional<int32_t>();
}

uint32_t return_uint() {
    return 4294967295u;
}

KOptional<uint32_t> return_uint_n() {
    return KOptional<uint32_t>(4294967295u);
}

int64_t return_long() {
    return 9223372036854775805;
}

KOptional<int64_t> return_long_n() {
    return KOptional<int64_t>();
}

uint64_t return_ulong() {
    return 18446744073709551615u;
}

KOptional<uint64_t> return_ulong_n() {
    return KOptional<uint64_t>(18446744073709551615u);
}

float return_float() {
    return 99;
}

KOptional<float> return_float_n() {
    return KOptional<float>();
}

double return_double() {
    return 99.0;
}

KOptional<double> return_double_n() {
    return KOptional<double>(99.0);
}

KString return_string() {
    return KString("test string");
}

KString return_string_empty() {
    return KString("");
}

KOptional<KString> return_string_n() {
    return KOptional<KString>();
}

MyEnum return_enum() {
    return CASE2;
}

KOptional<MyEnum> return_enum_n() {
    return KOptional<MyEnum>(CASE2);
}

MyDictionary return_dictionary() {
    return MyDictionary(1, 2, 3, 4);
}

KOptional<MyDictionary> return_dictionary_n() {
    return KOptional<MyDictionary>();
}
