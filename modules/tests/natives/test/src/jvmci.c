#include <api.h>
#include <stdio.h>
#include <string.h>
#include <inttypes.h>

// JVMCI

bool jvmci1(void) {
    printf("jvmci1  |\n");
    fflush(stdout);
    return true;
}

bool jvmci2(const int32_t a1) {
    printf("jvmci2  | a1: %d\n", a1);
    fflush(stdout);
    return a1 == 1;
}

bool jvmci3(const int32_t a1, const int32_t a2) {
    printf("jvmci3  | a1: %d, a2: %d\n", a1, a2);
    fflush(stdout);
    return a1 == 1 && a2 == 2;
}

bool jvmci4(
    const int32_t a1, const int32_t a2, const int32_t a3, const int32_t a4,
    const int32_t a5, const int32_t a6, const int32_t a7, const int32_t a8, const int32_t a9
) {
    printf("jvmci4  | a1: %d, a2: %d, a3: %d, a4: %d, a5: %d, a6: %d, a7: %d, a8: %d, a9: %d\n", a1, a2, a3, a4, a5, a6, a7, a8, a9);
    fflush(stdout);
    return a1 == 1 && a2 == 2 && a3 == 3 && a4 == 4 && a5 == 5 && a6 == 6 && a7 == 7 && a8 == 8 && a9 == 9;
}

bool jvmci5(
    const int32_t a1, const int64_t a2, const int32_t a3, const int64_t a4,
    const int32_t a5, const int64_t a6, const int32_t a7, const int32_t a8, const int64_t a9
) {
    printf("jvmci5  | a1: %d, a2: %" PRId64 ", a3: %d, a4: %" PRId64 ", a5: %d, a6: %" PRId64 ", a7: %d, a8: %d, a9: %" PRId64 "\n", a1, a2, a3, a4, a5, a6, a7, a8, a9);
    fflush(stdout);
    return a1 == 1 && a2 == 2 && a3 == 3 && a4 == 4 && a5 == 5 && a6 == 6 && a7 == 7 && a8 == 8 && a9 == 9;
}

bool jvmci6(
    const float a1, const float a2, const float a3, const float a4,
    const float a5, const float a6, const float a7, const float a8,
    const float a9, const int32_t a10, const int32_t a11, const int32_t a12, const int32_t a13
) {
    printf("jvmci6  | a1: %f, a2: %f, a3: %f, a4: %f, a5: %f, a6: %f, a7: %f, a8: %f, a9: %f, a10: %d, a11: %d, a12: %d, a13: %d\n", a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13);
    fflush(stdout);
    return a1 == 1.0 && a2 == 2.0 && a3 == 3.0 && a4 == 4.0 && a5 == 5.0 && a6 == 6.0 && a7 == 7.0 && a8 == 8.0 && a9 == 9.0 && a10 == 10 && a11 == 11 && a12 == 12 && a13 == 13;
}

bool jvmci7(
    const float a1, const double a2, const float a3, const double a4,
    const float a5, const double a6, const float a7, const float a8, const double a9
) {
    printf("jvmci7  | a1: %f, a2: %f, a3: %f, a4: %f, a5: %f, a6: %f, a7: %f, a8: %f, a9: %f\n", a1, a2, a3, a4, a5, a6, a7, a8, a9);
    fflush(stdout);
    return a1 == 1.0 && a2 == 2.0 && a3 == 3.0 && a4 == 4.0 && a5 == 5.0 && a6 == 6.0 && a7 == 7.0 && a8 == 8.0 && a9 == 9.0;
}

bool jvmci8(const int32_t a1, const double a2, const float a3, const int64_t a4) {
    printf("jvmci8  | a1: %d, a2: %f, a3: %f, a4: %" PRId64 "\n", a1, a2, a3, a4);
    fflush(stdout);
    return a1 == 1 && a2 == 2.0 && a3 == 3.0 && a4 == 4;
}

bool jvmci9(
    const int32_t a1, const double a2, const float a3, const int64_t a4,
    const int64_t a5, const double a6, const float a7, const float a8, const int32_t a9
) {
    printf("jvmci9  | a1: %d, a2: %f, a3: %f, a4: %" PRId64 ", a5: %" PRId64 ", a6: %f, a7: %f, a8: %f, a9: %d\n", a1, a2, a3, a4, a5, a6, a7, a8, a9);
    fflush(stdout);
    return a1 == 1 && a2 == 2.0 && a3 == 3.0 && a4 == 4 && a5 == 5 && a6 == 6.0 && a7 == 7.0 && a8 == 8.0 && a9 == 9;
}

bool jvmci10(
    KString* a1, const double a2, const float a3, const int64_t a4,
    const int64_t a5, const double a6, KString* a7, const float a8, const int32_t a9
) {
    printf("jvmci10 | a1: %s, a2: %f, a3: %f, a4: %" PRId64 ", a5: %" PRId64 ", a6: %f, a7: %s (%d), a8: %f, a9: %d\n", a1->data, a2, a3, a4, a5, a6, a7->data, a7->size, a8, a9);
    fflush(stdout);
    return strncmp(a1->data, "string1", 7) == 0 && a2 == 2.0 && a3 == 3.0 && a4 == 4 && a5 == 5 && a6 == 6.0 && strncmp(a7->data, "string7", 7) == 0 && a8 == 8.0 && a9 == 9;
}

bool jvmci11(
    const float a1, const int32_t a2, const float a3, const int32_t a4,
    const float a5, const int32_t a6, const float a7, const int32_t a8,
    const float a9, const int32_t a10, const float a11, const int32_t a12,
    const float a13, const int32_t a14, const float a15, const int32_t a16, const float a17
) {
    printf("jvmci11 | a1: %f, a2: %d, a3: %f, a4: %d, a5: %f, a6: %d, a7: %f, a8: %d, a9: %f, a10: %d, a11: %f, a12: %d, a13: %f, a14: %d, a15: %f, a16: %d, a17: %f\n", a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17);
    fflush(stdout);
    return a1 == 1.0 && a2 == 2 && a3 == 3.0 && a4 == 4 && a5 == 5.0 && a6 == 6 && a7 == 7.0 && a8 == 8 && a9 == 9.0 && a10 == 10 && a11 == 11.0 && a12 == 12 && a13 == 13.0 && a14 == 14 && a15 == 15.0 && a16 == 16 && a17 == 17.0;
}

int32_t jvmci12(void) {
    return 1;
}

int64_t jvmci13(void) {
    return 1;
}

float jvmci14(void) {
    return 1.5;
}

double jvmci15(void) {
    return 1.5;
}

bool jvmci_array(KIntArray* array) {
    return array->length == 3 &&
        array->elements[0] == 1 &&
        array->elements[1] == 2 &&
        array->elements[2] == 3;
}

bool jvmci_some_arrays(KIntArray* array1, KFloatArray* array2, KDoubleArray* array3) {
    return array1->length == 3 &&
        array1->elements[0] == 1 &&
        array1->elements[1] == 2 &&
        array1->elements[2] == 3 &&
        array2->length == 3 &&
        array2->elements[0] == 4.0 &&
        array2->elements[1] == 5.0 &&
        array2->elements[2] == 6.0 &&
        array3->length == 3 &&
        array3->elements[0] == 7.0 &&
        array3->elements[1] == 8.0 &&
        array3->elements[2] == 9.0;
}

bool jvmci_enum(const MyEnum enum1, const MyEnum enum2, KIntArray* enumArray){
    const MyEnum *elements = (MyEnum*)enumArray->elements;

    return enum1 == MyEnum_CASE1 && enum2 == MyEnum_CASE2 &&
        enumArray->length == 3 &&
            elements[0] == MyEnum_CASE1 &&
            elements[1] == MyEnum_CASE2 &&
            elements[2] == MyEnum_CASE1;
}