#include "api.h"
#include <stdio.h>


int32_t jvmOnlyFunc(int32_t arg, const char* arg2, int32_t __arg2Len) {
    printf("Hello, World aaa!\n");
    fflush(stdout);
    return 10;
}