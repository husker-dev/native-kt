#include <api.h>

uint16_t ping_char(const uint16_t arg) {
    return arg;
}

bool ping_boolean(const bool arg) {
    return arg;
}

int8_t ping_byte(const int8_t arg) {
    return arg;
}

uint8_t ping_ubyte(const uint8_t arg) {
    return arg;
}

int16_t ping_short(const int16_t arg) {
    return arg;
}

uint16_t ping_ushort(const uint16_t arg) {
    return arg;
}

int32_t ping_int(const int32_t arg) {
    return arg;
}

uint32_t ping_uint(const uint32_t arg) {
    return arg;
}

int64_t ping_long(const int64_t arg) {
    return arg;
}

uint64_t ping_ulong(const uint64_t arg) {
    return arg;
}

float ping_float(const float arg) {
    return arg;
}

double ping_double(const double arg) {
    return arg;
}

KString* ping_string(KString* arg) {
    return arg->clone(arg);
}

KString* ping_string_n(KString* arg) {
    return arg;
}

MyEnum ping_enum(const MyEnum arg) {
    return arg;
}

MyDictionary* ping_dictionary(MyDictionary* arg) {
    return arg->clone(arg);
}

MyDictionary* ping_dictionary_n(MyDictionary* arg) {
    return arg;
}

RC_MyInterface* ping_interface(RC_MyInterface* arg) {
    return arg->clone(arg);
}

RC_MyInterface* ping_interface_n(RC_MyInterface* arg) {
    return arg;
}