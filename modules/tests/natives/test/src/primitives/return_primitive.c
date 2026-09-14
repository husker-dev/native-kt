#include <api.h>

void return_void(void) {
}

uint16_t return_char(void) {
    return 'a';
}

bool return_boolean(void) {
    return true;
}

int8_t return_byte(void) {
    return 99;
}

uint8_t return_ubyte(void) {
    return 255u;
}

int16_t return_short(void) {
    return 99;
}

uint16_t return_ushort(void) {
    return 65535u;
}

int32_t return_int(void) {
    return 99;
}

uint32_t return_uint(void) {
    return 4294967295u;
}

int64_t return_long(void) {
    return 9223372036854775805;
}

uint64_t return_ulong(void) {
    return 18446744073709551615u;
}

float return_float(void) {
    return 99;
}

double return_double(void) {
    return 99.0;
}

KString* return_string(void) {
    return kstring_new("test string");
}

KString* return_string_n(void) {
    return NULL;
}

MyEnum return_enum(void) {
    return MyEnum_CASE2;
}

MyDictionary* return_dictionary(void) {
    return MyDictionary_new(1, 2, 3, 4);
}

MyDictionary* return_dictionary_n(void) {
    return NULL;
}