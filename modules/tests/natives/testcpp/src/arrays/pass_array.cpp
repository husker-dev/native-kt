#include <api.hpp>

bool pass_char_array(KArray<uint16_t> arg) {
    return arg.get_length() == 2 &&
        arg[0] == 'a' &&
        arg[1] == 'b';
}

bool pass_char_array_n(KOptional<KArray<uint16_t>> arg) {
    return arg.is_none();
}

bool pass_boolean_array(KArray<bool> arg) {
    return arg.get_length() == 2 &&
        arg[0] == true &&
        arg[1] == false;
}

bool pass_byte_array(KArray<int8_t> arg) {
    return arg.get_length() == 2 &&
        arg[0] == 1 &&
        arg[1] == 2;
}

bool pass_ubyte_array(KArray<uint8_t> arg) {
    return arg.get_length() == 2 &&
        arg[0] == 1 &&
        arg[1] == 255u;
}

bool pass_short_array(KArray<int16_t> arg) {
    return arg.get_length() == 2 &&
        arg[0] == 1 &&
        arg[1] == 2;
}

bool pass_ushort_array(KArray<uint16_t> arg) {
    return arg.get_length() == 2 &&
        arg[0] == 1 &&
        arg[1] == 65535u;
}

bool pass_int_array(KArray<int32_t> arg) {
    return arg.get_length() == 2 &&
        arg[0] == 1 &&
        arg[1] == 2;
}

bool pass_uint_array(KArray<uint32_t> arg) {
    return arg.get_length() == 2 &&
        arg[0] == 1 &&
        arg[1] == 4294967295u;
}

bool pass_long_array(KArray<int64_t> arg) {
    return arg.get_length() == 2 &&
        arg[0] == 1 &&
        arg[1] == 2;
}

bool pass_ulong_array(KArray<uint64_t> arg) {
    return arg.get_length() == 2 &&
        arg[0] == 1 &&
        arg[1] == 18446744073709551615u;
}

bool pass_float_array(KArray<float> arg) {
    return arg.get_length() == 2 &&
        arg[0] == 1.1f &&
        arg[1] == 2.2f;
}

bool pass_double_array(KArray<double> arg) {
    return arg.get_length() == 2 &&
        arg[0] == 1.1 &&
        arg[1] == 2.2;
}

bool pass_string_array(KArray<KString> arg) {
    return arg.get_length() == 2 &&
        std::string(arg[0].get_data(), arg[0].get_size()) == "string1" &&
        std::string(arg[1].get_data(), arg[1].get_size()) == "string2";
}

bool pass_string_array_n(KArray<KOptional<KString>> arg) {
    return arg[0].is_none() && arg[1].is_none();
}

bool pass_enum_array(KArray<MyEnum> arg) {
    return arg.get_length() == 2 &&
        arg[0] == CASE1 &&
        arg[1] == CASE2;
}

bool pass_dictionary_array(KArray<MyDictionary> arg) {
    return arg.get_length() == 2 &&
        arg[0].a == 1 &&
        arg[0].b == 2 &&
        arg[0].c == 3 &&
        arg[0].d == 4 &&
        arg[1].a == 5 &&
        arg[1].b == 6 &&
        arg[1].c == 7 &&
        arg[1].d == 8;
}

bool pass_dictionary_array_n(KArray<KOptional<MyDictionary>> arg) {
    return arg[0].is_none() && arg[1].is_none();
}

bool pass_interface_array(KArray<std::shared_ptr<IMyInterface>> arg) {
    return arg[0]->test() && arg[1]->test();
}

bool pass_interface_array_n(KArray<KOptional<std::shared_ptr<IMyInterface>>> arg) {
    return arg[0].is_none() && arg[1].is_none();
}
