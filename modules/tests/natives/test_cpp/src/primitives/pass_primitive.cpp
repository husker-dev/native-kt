#include <nativekt.hpp>

// Consume

bool pass_void() {
    return true;
}

bool pass_char(std::uint16_t arg) {
    return arg == 'a';
}

bool pass_char_n(KOptional<std::uint16_t> arg) {
    return arg.is_none() || *arg == 'a';
}

bool pass_boolean(bool arg) {
    return arg == true;
}

bool pass_boolean_n(KOptional<bool> arg) {
    return arg.is_none() || *arg == true;
}

bool pass_byte(std::int8_t arg) {
    return arg == 1;
}

bool pass_byte_n(KOptional<std::int8_t> arg) {
    return arg.is_none() || *arg == 1;
}

bool pass_ubyte(std::uint8_t arg) {
    return arg == 255u;
}

bool pass_ubyte_n(KOptional<std::uint8_t> arg) {
    return arg.is_none() || *arg == 255u;
}

bool pass_short(std::int16_t arg) {
    return arg == 1;
}

bool pass_short_n(KOptional<std::int16_t> arg) {
    return arg.is_none() || *arg == 1;
}

bool pass_ushort(std::uint16_t arg) {
    return arg == 65535u;
}

bool pass_ushort_n(KOptional<std::uint16_t> arg) {
    return arg.is_none() || *arg == 65535u;
}

bool pass_int(std::int32_t arg) {
    return arg == 99;
}

bool pass_int_n(KOptional<std::int32_t> arg) {
    return arg.is_none() || *arg == 99;
}

bool pass_uint(std::uint32_t arg) {
    return arg == 4294967295u;
}

bool pass_uint_n(KOptional<std::uint32_t> arg) {
    return arg.is_none() || *arg == 4294967295u;
}

bool pass_long(std::int64_t arg) {
    return arg == 9223372036854775805;
}

bool pass_long_n(KOptional<std::int64_t> arg) {
    return arg.is_none() || *arg == 9223372036854775805;
}

bool pass_ulong(std::uint64_t arg) {
    return arg == 18446744073709551615u;
}

bool pass_ulong_n(KOptional<std::uint64_t> arg) {
    return arg.is_none() || *arg == 18446744073709551615u;
}

bool pass_float(float arg) {
    return arg == 99.9f;
}

bool pass_float_n(KOptional<float> arg) {
    return arg.is_none() || *arg == 99.9f;
}

bool pass_double(double arg) {
    return arg == 1.1;
}

bool pass_double_n(KOptional<double> arg) {
    return arg.is_none() || *arg == 1.1;
}

bool pass_string(KString arg) {
    return std::string(arg.get_data(), arg.get_size()) == "test string";
}

bool pass_string_empty(KString arg) {
    return arg.get_size() == 0;
}

bool pass_string_n(KOptional<KString> arg) {
    return arg.is_none();
}

bool pass_enum(MyEnum arg) {
    return arg == CASE2;
}

bool pass_enum_n(KOptional<MyEnum> arg) {
    return arg.is_none() || *arg == CASE2;
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
