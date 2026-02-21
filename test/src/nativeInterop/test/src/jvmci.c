#include "api.h"
#include <stdio.h>
#include <string.h>

// JVMCI

bool jvmci1() {
    printf("jvmci1  |\n");
    return true;
}

bool jvmci2(int32_t a1) {
    printf("jvmci2  | a1: %d\n", a1);
    fflush(stdout);
    return a1 == 1;
}

bool jvmci3(int32_t a1, int32_t a2) {
    printf("jvmci3  | a1: %d, a2: %d\n", a1, a2);
    fflush(stdout);
    return a1 == 1 && a2 == 2;
}

bool jvmci4(int32_t a1, int32_t a2, int32_t a3, int32_t a4, int32_t a5, int32_t a6, int32_t a7, int32_t a8, int32_t a9) {
    printf("jvmci4  | a1: %d, a2: %d, a3: %d, a4: %d, a5: %d, a6: %d, a7: %d, a8: %d, a9: %d\n", a1, a2, a3, a4, a5, a6, a7, a8, a9);
    fflush(stdout);
    return a1 == 1 && a2 == 2 && a3 == 3 && a4 == 4 && a5 == 5 && a6 == 6 && a7 == 7 && a8 == 8 && a9 == 9;
}

bool jvmci5(int32_t a1, int64_t a2, int32_t a3, int64_t a4, int32_t a5, int64_t a6, int32_t a7, int32_t a8, int64_t a9) {
    printf("jvmci5  | a1: %d, a2: %lld, a3: %d, a4: %lld, a5: %d, a6: %lld, a7: %d, a8: %d, a9: %lld\n", a1, a2, a3, a4, a5, a6, a7, a8, a9);
    fflush(stdout);
    return a1 == 1 && a2 == 2 && a3 == 3 && a4 == 4 && a5 == 5 && a6 == 6 && a7 == 7 && a8 == 8 && a9 == 9;
}

bool jvmci6(float a1, float a2, float a3, float a4, float a5, float a6, float a7, float a8, float a9, int32_t a10, int32_t a11, int32_t a12, int32_t a13) {
    printf("jvmci6  | a1: %f, a2: %f, a3: %f, a4: %f, a5: %f, a6: %f, a7: %f, a8: %f, a9: %f\n", a1, a2, a3, a4, a5, a6, a7, a8, a9);
    fflush(stdout);
    return a1 == 1.0 && a2 == 2.0 && a3 == 3.0 && a4 == 4.0 && a5 == 5.0 && a6 == 6.0 && a7 == 7.0 && a8 == 8.0 && a9 == 9.0;
}

bool jvmci7(float a1, double a2, float a3, double a4, float a5, double a6, float a7, float a8, double a9) {
    printf("jvmci7  | a1: %f, a2: %f, a3: %f, a4: %f, a5: %f, a6: %f, a7: %f, a8: %f, a9: %f\n", a1, a2, a3, a4, a5, a6, a7, a8, a9);
    fflush(stdout);
    return a1 == 1.0 && a2 == 2.0 && a3 == 3.0 && a4 == 4.0 && a5 == 5.0 && a6 == 6.0 && a7 == 7.0 && a8 == 8.0 && a9 == 9.0;
}

bool jvmci8(int32_t a1, double a2, float a3, int64_t a4) {
    printf("jvmci8  | a1: %d, a2: %f, a3: %f, a4: %lld\n", a1, a2, a3, a4);
    fflush(stdout);
    return a1 == 1 && a2 == 2.0 && a3 == 3.0 && a4 == 4.0;
}

bool jvmci9(int32_t a1, double a2, float a3, int64_t a4, int64_t a5, double a6, float a7, float a8, int32_t a9) {
    printf("jvmci9  | a1: %d, a2: %f, a3: %f, a4: %lld, a5: %lld, a6: %f, a7: %f, a8: %f, a9: %d\n", a1, a2, a3, a4, a5, a6, a7, a8, a9);
    fflush(stdout);
    return a1 == 1 && a2 == 2.0 && a3 == 3.0 && a4 == 4 && a5 == 5 && a6 == 6.0 && a7 == 7.0 && a8 == 8.0 && a9 == 9;
}

bool jvmci10(const char* a1, double a2, float a3, int64_t a4, int64_t a5, double a6, const char* a7, float a8, int32_t a9) {
    printf("jvmci10 | a1: %s, a2: %f, a3: %f, a4: %lld, a5: %lld, a6: %f, a7: %s, a8: %f, a9: %d\n", a1, a2, a3, a4, a5, a6, a7, a8, a9);
    fflush(stdout);
    return strncmp(a1, "string1", 7) == 0 && a2 == 2.0 && a3 == 3.0 && a4 == 4 && a5 == 5 && a6 == 6.0 && strncmp(a7, "string7", 7) == 0 && a8 == 8.0 && a9 == 9;
}

bool jvmci11(float a1, int32_t a2, float a3, int32_t a4, float a5, int32_t a6, float a7, int32_t a8, float a9, int32_t a10, float a11, int32_t a12, float a13, int32_t a14, float a15, int32_t a16, float a17) {
    printf("jvmci11 | a1: %f, a2: %d, a3: %f, a4: %d, a5: %f, a6: %d, a7: %f, a8: %d, a9: %f, a10: %d, a11: %f, a12: %d, a13: %f, a14: %d, a15: %f, a16: %d, a17: %f\n", a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17);
    fflush(stdout);
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