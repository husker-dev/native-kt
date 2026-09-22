#include <api.hpp>

// Consume

bool pass_void() {
    return true;
}

bool pass_char(uint16_t arg) {
    return arg == 'a';
}

bool pass_boolean(bool arg) {
    return arg == true;
}

bool pass_byte(int8_t arg) {
    return arg == 1;
}

bool pass_ubyte(uint8_t arg) {
    return arg == 255u;
}

bool pass_short(int16_t arg) {
    return arg == 1;
}

bool pass_ushort(uint16_t arg) {
    return arg == 65535u;
}

bool pass_int(int32_t arg) {
    return arg == 99;
}

bool pass_uint(uint32_t arg) {
    return arg == 4294967295u;
}

bool pass_long(int64_t arg) {
    return arg == 9223372036854775805;
}

bool pass_ulong(uint64_t arg) {
    return arg == 18446744073709551615u;
}

bool pass_float(float arg) {
    return arg == 99.9f;
}

bool pass_double(double arg) {
    return arg == 1.1;
}

bool pass_string(KString arg) {
    return std::string(arg.get_data(), arg.get_size()) == "test string";
}

bool pass_string_n(KOptional<KString> arg) {
    return arg.is_none();
}

bool pass_enum(MyEnum arg) {
    return arg == CASE2;
}

bool pass_dictionary(MyDictionary arg) {
    return arg.a == 1 &&
        arg.b == 2 &&
        arg.c == 3 &&
        arg.d == 4;
}

bool pass_dictionary_n(KOptional<MyDictionary> arg) {
    return arg.is_none();
}

bool pass_interface(std::shared_ptr<IMyInterface> arg) {
    return arg->test();
}

bool pass_interface_n(KOptional<std::shared_ptr<IMyInterface>> arg) {
    return arg.is_none();
}
