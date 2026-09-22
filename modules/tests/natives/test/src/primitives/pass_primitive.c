#include <api.h>
#include <string.h>

// Consume

bool pass_void(void) {
    return true;
}

bool pass_char(const uint16_t arg) {
    return arg == 'a';
}

bool pass_boolean(const bool arg) {
    return arg == true;
}

bool pass_byte(const int8_t arg) {
    return arg == 1;
}

bool pass_ubyte(const uint8_t arg) {
    return arg == 255u;
}

bool pass_short(const int16_t arg) {
    return arg == 1;
}

bool pass_ushort(const uint16_t arg) {
    return arg == 65535u;
}

bool pass_int(const int32_t arg) {
    return arg == 99;
}

bool pass_uint(const uint32_t arg) {
    return arg == 4294967295u;
}

bool pass_long(const int64_t arg) {
    return arg == 9223372036854775805;
}

bool pass_ulong(const uint64_t arg) {
    return arg == 18446744073709551615u;
}

bool pass_float(const float arg) {
    return arg == 99.9f;
}

bool pass_double(const double arg) {
    return arg == 1.1;
}

bool pass_string(KString* arg) {
    return arg->size == 11 && strncmp(arg->data, "test string", arg->size) == 0;
}

bool pass_string_n(KString* arg) {
    return arg == NULL;
}

bool pass_enum(const MyEnum arg) {
    return arg == MyEnum_CASE2;
}

bool pass_dictionary(MyDictionary* arg) {
    return arg->a == 1 &&
        arg->b == 2 &&
        arg->c == 3 &&
        arg->d == 4;
}

bool pass_dictionary_n(MyDictionary* arg) {
    return arg == NULL;
}

bool pass_interface(RC_MyInterface* arg) {
    return arg->pointed == (void*) 1;
}

bool pass_interface_n(RC_MyInterface* arg) {
    return arg == NULL;
}