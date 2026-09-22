#include <api.hpp>

uint16_t ping_char(uint16_t arg) {
    return arg;
}

bool ping_boolean(bool arg) {
    return arg;
}

int8_t ping_byte(int8_t arg) {
    return arg;
}

uint8_t ping_ubyte(uint8_t arg) {
    return arg;
}

int16_t ping_short(int16_t arg) {
    return arg;
}

uint16_t ping_ushort(uint16_t arg) {
    return arg;
}

int32_t ping_int(int32_t arg) {
    return arg;
}

uint32_t ping_uint(uint32_t arg) {
    return arg;
}

int64_t ping_long(int64_t arg) {
    return arg;
}

uint64_t ping_ulong(uint64_t arg) {
    return arg;
}

float ping_float(float arg) {
    return arg;
}

double ping_double(double arg) {
    return arg;
}

KString ping_string(KString arg) {
    return arg;
}

KOptional<KString> ping_string_n(KOptional<KString> arg) {
    return arg;
}

MyEnum ping_enum(MyEnum arg) {
    return arg;
}

MyDictionary ping_dictionary(MyDictionary arg) {
    return arg;
}

KOptional<MyDictionary> ping_dictionary_n(KOptional<MyDictionary> arg) {
    return arg;
}

std::shared_ptr<IMyInterface> ping_interface(std::shared_ptr<IMyInterface> arg) {
    return arg;
}

KOptional<std::shared_ptr<IMyInterface>> ping_interface_n(KOptional<std::shared_ptr<IMyInterface>> arg) {
    return arg;
}
