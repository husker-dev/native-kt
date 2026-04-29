#include <api.h>
#include <stdio.h>
#include <string.h>

// JVMCI

KBoolean jvmci1() {
    printf("jvmci1  |\n");
    fflush(stdout);
    return true;
}

KBoolean jvmci2(const KInt a1) {
    printf("jvmci2  | a1: %d\n", a1);
    fflush(stdout);
    return a1 == 1;
}

KBoolean jvmci3(const KInt a1, const KInt a2) {
    printf("jvmci3  | a1: %d, a2: %d\n", a1, a2);
    fflush(stdout);
    return a1 == 1 && a2 == 2;
}

KBoolean jvmci4(
    const KInt a1, const KInt a2, const KInt a3, const KInt a4,
    const KInt a5, const KInt a6, const KInt a7, const KInt a8, const KInt a9
) {
    printf("jvmci4  | a1: %d, a2: %d, a3: %d, a4: %d, a5: %d, a6: %d, a7: %d, a8: %d, a9: %d\n", a1, a2, a3, a4, a5, a6, a7, a8, a9);
    fflush(stdout);
    return a1 == 1 && a2 == 2 && a3 == 3 && a4 == 4 && a5 == 5 && a6 == 6 && a7 == 7 && a8 == 8 && a9 == 9;
}

KBoolean jvmci5(
    const KInt a1, const KLong a2, const KInt a3, const KLong a4,
    const KInt a5, const KLong a6, const KInt a7, const KInt a8, const KLong a9
) {
    printf("jvmci5  | a1: %d, a2: %lld, a3: %d, a4: %lld, a5: %d, a6: %lld, a7: %d, a8: %d, a9: %lld\n", a1, a2, a3, a4, a5, a6, a7, a8, a9);
    fflush(stdout);
    return a1 == 1 && a2 == 2 && a3 == 3 && a4 == 4 && a5 == 5 && a6 == 6 && a7 == 7 && a8 == 8 && a9 == 9;
}

KBoolean jvmci6(
    const KFloat a1, const KFloat a2, const KFloat a3, const KFloat a4,
    const KFloat a5, const KFloat a6, const KFloat a7, const KFloat a8,
    const KFloat a9, const KInt a10, const KInt a11, const KInt a12, const KInt a13
) {
    printf("jvmci6  | a1: %f, a2: %f, a3: %f, a4: %f, a5: %f, a6: %f, a7: %f, a8: %f, a9: %f, a10: %d, a11: %d, a12: %d, a13: %d\n", a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13);
    fflush(stdout);
    return a1 == 1.0 && a2 == 2.0 && a3 == 3.0 && a4 == 4.0 && a5 == 5.0 && a6 == 6.0 && a7 == 7.0 && a8 == 8.0 && a9 == 9.0 && a10 == 10 && a11 == 11 && a12 == 12 && a13 == 13;
}

KBoolean jvmci7(
    const KFloat a1, const KDouble a2, const KFloat a3, const KDouble a4,
    const KFloat a5, const KDouble a6, const KFloat a7, const KFloat a8, const KDouble a9
) {
    printf("jvmci7  | a1: %f, a2: %f, a3: %f, a4: %f, a5: %f, a6: %f, a7: %f, a8: %f, a9: %f\n", a1, a2, a3, a4, a5, a6, a7, a8, a9);
    fflush(stdout);
    return a1 == 1.0 && a2 == 2.0 && a3 == 3.0 && a4 == 4.0 && a5 == 5.0 && a6 == 6.0 && a7 == 7.0 && a8 == 8.0 && a9 == 9.0;
}

KBoolean jvmci8(const KInt a1, const KDouble a2, const KFloat a3, const KLong a4) {
    printf("jvmci8  | a1: %d, a2: %f, a3: %f, a4: %lld\n", a1, a2, a3, a4);
    fflush(stdout);
    return a1 == 1 && a2 == 2.0 && a3 == 3.0 && a4 == 4.0;
}

KBoolean jvmci9(
    const KInt a1, const KDouble a2, const KFloat a3, const KLong a4,
    const KLong a5, const KDouble a6, const KFloat a7, const KFloat a8, const KInt a9
) {
    printf("jvmci9  | a1: %d, a2: %f, a3: %f, a4: %lld, a5: %lld, a6: %f, a7: %f, a8: %f, a9: %d\n", a1, a2, a3, a4, a5, a6, a7, a8, a9);
    fflush(stdout);
    return a1 == 1 && a2 == 2.0 && a3 == 3.0 && a4 == 4 && a5 == 5 && a6 == 6.0 && a7 == 7.0 && a8 == 8.0 && a9 == 9;
}

KBoolean jvmci10(
    const KString a1, const KDouble a2, const KFloat a3, const KLong a4,
    const KLong a5, const KDouble a6, const KString a7, const KFloat a8, const KInt a9
) {
    printf("jvmci10 | a1: %s, a2: %f, a3: %f, a4: %lld, a5: %lld, a6: %f, a7: %s, a8: %f, a9: %d\n", a1.data, a2, a3, a4, a5, a6, a7.data, a8, a9);
    fflush(stdout);
    return strncmp(a1.data, "string1", 7) == 0 && a2 == 2.0 && a3 == 3.0 && a4 == 4 && a5 == 5 && a6 == 6.0 && strncmp(a7.data, "string7", 7) == 0 && a8 == 8.0 && a9 == 9;
}

KBoolean jvmci11(
    const KFloat a1, const KInt a2, const KFloat a3, const KInt a4,
    const KFloat a5, const KInt a6, const KFloat a7, const KInt a8,
    const KFloat a9, const KInt a10, const KFloat a11, const KInt a12,
    const KFloat a13, const KInt a14, const KFloat a15, const KInt a16, const KFloat a17
) {
    printf("jvmci11 | a1: %f, a2: %d, a3: %f, a4: %d, a5: %f, a6: %d, a7: %f, a8: %d, a9: %f, a10: %d, a11: %f, a12: %d, a13: %f, a14: %d, a15: %f, a16: %d, a17: %f\n", a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17);
    fflush(stdout);
    return a1 == 1.0 && a2 == 2 && a3 == 3.0 && a4 == 4 && a5 == 5.0 && a6 == 6 && a7 == 7.0 && a8 == 8 && a9 == 9.0 && a10 == 10 && a11 == 11.0 && a12 == 12 && a13 == 13.0 && a14 == 14 && a15 == 15.0 && a16 == 16 && a17 == 17.0;
}

KInt jvmci12() {
    return 1;
}

KLong jvmci13() {
    return 1;
}

KFloat jvmci14() {
    return 1.5;
}

KDouble jvmci15() {
    return 1.5;
}

KBoolean jvmciArray(const KIntArray array) {
    return array.size == 3 &&
        array.elements[0] == 1 &&
        array.elements[1] == 2 &&
        array.elements[2] == 3;
}

KBoolean jvmciSomeArrays(const KIntArray array1, const KFloatArray array2, const KDoubleArray array3) {
    return array1.size == 3 &&
        array1.elements[0] == 1 &&
        array1.elements[1] == 2 &&
        array1.elements[2] == 3 &&
        array2.size == 3 &&
        array2.elements[0] == 4.0 &&
        array2.elements[1] == 5.0 &&
        array2.elements[2] == 6.0 &&
        array3.size == 3 &&
        array3.elements[0] == 7.0 &&
        array3.elements[1] == 8.0 &&
        array3.elements[2] == 9.0;
}

KBoolean jvmciEnum(const MyEnum enum1, const MyEnum enum2, const KIntArray enumArray){
    const MyEnum *elements = (MyEnum*)enumArray.elements;

    return enum1 == MyEnum_CASE1 && enum2 == MyEnum_CASE2 &&
        enumArray.size == 3 &&
            elements[0] == MyEnum_CASE1 &&
            elements[1] == MyEnum_CASE2 &&
            elements[2] == MyEnum_CASE1;
}