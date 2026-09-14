#include <api.hpp>

bool critical_primitives(
    const uint16_t a1, const bool a2,
    const int8_t a3, const uint8_t a4,
    const int16_t a5, const uint16_t a6,
    const int32_t a7, const uint32_t a8,
    const int64_t a9, const uint64_t a10,
    const float a11, const double a12
) {
    return a1 == 'a' &&
        a2 == true &&
        a3 == 1 &&
        a4 == 255u &&
        a5 == 3 &&
        a6 == 65535u &&
        a7 == 5 &&
        a8 == 4294967295u &&
        a9 == 7 &&
        a10 == 18446744073709551615u &&
        a11 == 1.0f &&
        a12 == 2.0;
}

bool critical_enum(const MyEnum a1) {
    return a1 == CASE1;
}

bool critical_interface(const std::shared_ptr<IMyInterface>& a1) {
    return a1->test();
}

bool critical_interface_n(const KOptional<std::shared_ptr<IMyInterface>>& a1) {
    return a1.is_none();
}

bool critical_string(const KString& a1) {
    return std::string(a1.get_data(), a1.get_size()) == "test string";
}

bool critical_string_n(const KOptional<KString>& a1) {
    return a1.is_none();
}

bool critical_primitives_array(
    const KArray<uint16_t>& a1, const KArray<bool>& a2,
    const KArray<int8_t>& a3, const KArray<uint8_t>& a4,
    const KArray<int16_t>& a5, const KArray<uint16_t>& a6,
    const KArray<int32_t>& a7,  const KArray<uint32_t>& a8,
    const KArray<int64_t>& a9, const KArray<uint64_t>& a10,
    const KArray<float>& a11, const KArray<double>& a12
) {
    return a1.get_length() == 2 &&
            a1[0] == 'a' &&
            a1[1] == 'b' &&
            a2.get_length() == 2 &&
            a2[0] == true &&
            a2[1] == false &&
            a3.get_length() == 2 &&
            a3[0] == 1 &&
            a3[1] == 2 &&
            a4.get_length() == 2 &&
            a4[0] == 1 &&
            a4[1] == 255u &&
            a5.get_length() == 2 &&
            a5[0] == 1 &&
            a5[1] == 2 &&
            a6.get_length() == 2 &&
            a6[0] == 1 &&
            a6[1] == 65535u &&
            a7.get_length() == 2 &&
            a7[0] == 1 &&
            a7[1] == 2 &&
            a8.get_length() == 2 &&
            a8[0] == 1 &&
            a8[1] == 4294967295u &&
            a9.get_length() == 2 &&
            a9[0] == 1 &&
            a9[1] == 2 &&
            a10.get_length() == 2 &&
            a10[0] == 1 &&
            a10[1] == 18446744073709551615u &&
            a11.get_length() == 2 &&
            a11[0] == 1.1f &&
            a11[1] == 2.2f &&
            a12.get_length() == 2 &&
            a12[0] == 1.1 &&
            a12[1] == 2.2;
}

bool critical_primitives_array_n(
    const KOptional<KArray<uint16_t>>& a1, const KOptional<KArray<bool>>& a2,
    const KOptional<KArray<int8_t>>& a3, const KOptional<KArray<uint8_t>>& a4,
    const KOptional<KArray<int16_t>>& a5, const KOptional<KArray<uint16_t>>& a6,
    const KOptional<KArray<int32_t>>& a7, const KOptional<KArray<uint32_t>>& a8,
    const KOptional<KArray<int64_t>>& a9, const KOptional<KArray<uint64_t>>& a10,
    const KOptional<KArray<float>>& a11, const KOptional<KArray<double>>& a12
) {
    return a1.is_none() && a2.is_none() &&
        a3.is_none() && a4.is_none() &&
        a5.is_none() && a6.is_none() &&
        a7.is_none() && a8.is_none() &&
        a9.is_none() && a10.is_none() &&
        a11.is_none() && a12.is_none();
}

bool critical_enum_array(const KArray<MyEnum>& a1) {
    return a1.get_length() == 2 &&
           a1[0] == CASE1 &&
           a1[1] == CASE2;
}

bool critical_enum_array_n(const KOptional<KArray<MyEnum>>& a1) {
    return a1.is_none();
}

uint16_t critical_return_char() {
    return 'a';
}

bool critical_return_boolean() {
    return true;
}

int8_t critical_return_byte() {
    return 1;
}

uint8_t critical_return_ubyte() {
    return 255u;
}

int16_t critical_return_short() {
    return 1;
}

uint16_t critical_return_ushort() {
    return 65535u;
}

int32_t critical_return_int() {
    return 1;
}

uint32_t critical_return_uint() {
    return 4294967295u;
}

int64_t critical_return_long() {
    return 1;
}

uint64_t critical_return_ulong() {
    return 18446744073709551615u;
}

float critical_return_float() {
    return 1.0f;
}

double critical_return_double() {
    return 1.0;
}

MyEnum critical_return_enum() {
    return CASE1;
}

std::shared_ptr<IMyInterface> critical_return_interface() {
    return std::shared_ptr<IMyInterface>(IMyInterface::_create());
}
