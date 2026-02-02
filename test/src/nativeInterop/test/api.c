#include "api.h"
#include <stdio.h>


int32_t testFunc(int32_t arg, const char* arg2, size_t arg2_len) {
    printf("Hello, World aaa!\n");
    fflush(stdout);
    return 10;
}