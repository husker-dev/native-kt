#include <api.hpp>
#include <iostream>

// JVMCI

bool jvmci1() {
    std::cout << "jvmci1  |" << std::endl;
    return true;
}

bool jvmci2(const int32_t a1) {
    std::cout << "jvmci2  | a1: " << a1 << std::endl;
    return a1 == 1;
}

bool jvmci3(const int32_t a1, const int32_t a2) {
    std::cout << "jvmci3  | a1: " << a1 << ", a2" << a2 << std::endl;
    return a1 == 1 && a2 == 2;
}

bool jvmci4(
    const int32_t a1, const int32_t a2, const int32_t a3, const int32_t a4,
    const int32_t a5, const int32_t a6, const int32_t a7, const int32_t a8, const int32_t a9
) {
    std::cout << "jvmci4  | a1: " << a1 << ", a2: " << a2 << ", a3: " << a3 <<
        ", a4: " << a4 << ", a5: " << a5 << ", a6: " << a6 << ", a7: " << a7 <<
        ", a8: " << a8 << ", a9: " << a9 << std::endl;
    return a1 == 1 && a2 == 2 && a3 == 3 && a4 == 4 && a5 == 5 && a6 == 6 && a7 == 7 && a8 == 8 && a9 == 9;
}

bool jvmci5(
    const int32_t a1, const int64_t a2, const int32_t a3, const int64_t a4,
    const int32_t a5, const int64_t a6, const int32_t a7, const int32_t a8, const int64_t a9
) {
    std::cout << "jvmci5  | a1: " << a1 << ", a2: " << a2 << ", a3: " << a3 <<
        ", a4: " << a4 << ", a5: " << a5 << ", a6: " << a6 << ", a7: " << a7 <<
        ", a8: " << a8 << ", a9: " << a9 << std::endl;
    return a1 == 1 && a2 == 2 && a3 == 3 && a4 == 4 && a5 == 5 && a6 == 6 && a7 == 7 && a8 == 8 && a9 == 9;
}

bool jvmci6(
    const float a1, const float a2, const float a3, const float a4,
    const float a5, const float a6, const float a7, const float a8,
    const float a9, const int32_t a10, const int32_t a11, const int32_t a12, const int32_t a13
) {
    std::cout << "jvmci6  | a1: " << a1 << ", a2: " << a2 << ", a3: " << a3 <<
        ", a4: " << a4 << ", a5: " << a5 << ", a6: " << a6 << ", a7: " << a7 <<
        ", a8: " << a8 << ", a9: " << a9 << ", a10: " << a10 << ", a11: " << a11 <<
        ", a12: " << a12 << std::endl;
    return a1 == 1.0 && a2 == 2.0 && a3 == 3.0 && a4 == 4.0 && a5 == 5.0 && a6 == 6.0 && a7 == 7.0 && a8 == 8.0 && a9 == 9.0 && a10 == 10 && a11 == 11 && a12 == 12 && a13 == 13;
}

bool jvmci7(
    const float a1, const double a2, const float a3, const double a4,
    const float a5, const double a6, const float a7, const float a8, const double a9
) {
    std::cout << "jvmci7  | a1: " << a1 << ", a2: " << a2 << ", a3: " << a3 <<
        ", a4: " << a4 << ", a5: " << a5 << ", a6: " << a6 << ", a7: " << a7 <<
        ", a8: " << a8 << ", a9: " << a9 << std::endl;
    return a1 == 1.0 && a2 == 2.0 && a3 == 3.0 && a4 == 4.0 && a5 == 5.0 && a6 == 6.0 && a7 == 7.0 && a8 == 8.0 && a9 == 9.0;
}

bool jvmci8(const int32_t a1, const double a2, const float a3, const int64_t a4) {
    std::cout << "jvmci8  | a1: " << a1 << ", a2: " << a2 << ", a3: " << a3 << std::endl;
    return a1 == 1 && a2 == 2.0 && a3 == 3.0 && a4 == 4;
}

bool jvmci9(
    const int32_t a1, const double a2, const float a3, const int64_t a4,
    const int64_t a5, const double a6, const float a7, const float a8, const int32_t a9
) {
    std::cout << "jvmci9  | a1: " << a1 << ", a2: " << a2 << ", a3: " << a3 <<
        ", a4: " << a4 << ", a5: " << a5 << ", a6: " << a6 << ", a7: " << a7 <<
        ", a8: " << a8 << ", a9: " << a9 << std::endl;
    return a1 == 1 && a2 == 2.0 && a3 == 3.0 && a4 == 4 && a5 == 5 && a6 == 6.0 && a7 == 7.0 && a8 == 8.0 && a9 == 9;
}

bool jvmci10(
    const KString& a1, const double a2, const float a3, const int64_t a4,
    const int64_t a5, const double a6, const KString& a7, const float a8, const int32_t a9
) {
    std::cout << "jvmci10  | a1: " << a1.get_data() << ", a2: " << a2 << ", a3: " << a3 <<
        ", a4: " << a4 << ", a5: " << a5 << ", a6: " << a6 << ", a7: " << a7.get_data() <<
        ", a8: " << a8 << ", a9: " << a9 << std::endl;
    return std::string(a1.get_data(), a1.get_size()) == "string1" && a2 == 2.0 && a3 == 3.0 && a4 == 4 && a5 == 5 && a6 == 6.0 && std::string(a7.get_data(), a7.get_size()) == "string7" && a8 == 8.0 && a9 == 9;
}

bool jvmci11(
    const float a1, const int32_t a2, const float a3, const int32_t a4,
    const float a5, const int32_t a6, const float a7, const int32_t a8,
    const float a9, const int32_t a10, const float a11, const int32_t a12,
    const float a13, const int32_t a14, const float a15, const int32_t a16, const float a17
) {
    std::cout << "jvmci11  | a1: " << a1 << ", a2: " << a2 << ", a3: " << a3 <<
        ", a4: " << a4 << ", a5: " << a5 << ", a6: " << a6 << ", a7: " << a7 <<
        ", a8: " << a8 << ", a9: " << a9 << ", a10: " << a10 << ", a11: " << a11 <<
        ", a12: " << a12 << ", a13: " << a13 << ", a14: " << a14 << ", a15: " << a15 <<
        ", a16: " << a16 << std::endl;
    return a1 == 1.0 && a2 == 2 && a3 == 3.0 && a4 == 4 && a5 == 5.0 && a6 == 6 && a7 == 7.0 && a8 == 8 && a9 == 9.0 && a10 == 10 && a11 == 11.0 && a12 == 12 && a13 == 13.0 && a14 == 14 && a15 == 15.0 && a16 == 16 && a17 == 17.0;
}

int32_t jvmci12() {
    return 1;
}

int64_t jvmci13() {
    return 1;
}

float jvmci14() {
    return 1.5;
}

double jvmci15() {
    return 1.5;
}

bool jvmci_array(const KArray<int32_t>& array) {
    return array.get_length() == 3 &&
        array[0] == 1 &&
        array[1] == 2 &&
        array[2] == 3;
}

bool jvmci_some_arrays(const KArray<int32_t>& array1, const KArray<float>& array2, const KArray<double>& array3) {
    return array1.get_length() == 3 &&
        array1[0] == 1 &&
        array1[1] == 2 &&
        array1[2] == 3 &&
        array2.get_length() == 3 &&
        array2[0] == 4.0 &&
        array2[1] == 5.0 &&
        array2[2] == 6.0 &&
        array3.get_length() == 3 &&
        array3[0] == 7.0 &&
        array3[1] == 8.0 &&
        array3[2] == 9.0;
}

bool jvmci_enum(const MyEnum enum1, const MyEnum enum2, const KArray<MyEnum>& enumArray){
    return enum1 == CASE1 && enum2 == CASE2 &&
        enumArray.get_length() == 3 &&
            enumArray[0] == CASE1 &&
            enumArray[1] == CASE2 &&
            enumArray[2] == CASE1;
}