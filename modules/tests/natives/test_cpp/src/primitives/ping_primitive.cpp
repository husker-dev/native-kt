#include <nativekt.hpp>

std::uint16_t ping_char(std::uint16_t arg) {
    return arg;
}

bool ping_boolean(bool arg) {
    return arg;
}

std::int8_t ping_byte(std::int8_t arg) {
    return arg;
}

std::uint8_t ping_ubyte(std::uint8_t arg) {
    return arg;
}

std::int16_t ping_short(std::int16_t arg) {
    return arg;
}

std::uint16_t ping_ushort(std::uint16_t arg) {
    return arg;
}

std::int32_t ping_int(std::int32_t arg) {
    return arg;
}

std::uint32_t ping_uint(std::uint32_t arg) {
    return arg;
}

std::int64_t ping_long(std::int64_t arg) {
    return arg;
}

std::uint64_t ping_ulong(std::uint64_t arg) {
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

KString ping_string_empty(KString arg) {
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

EmptyDictionary ping_empty_dictionary(EmptyDictionary arg) {
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
