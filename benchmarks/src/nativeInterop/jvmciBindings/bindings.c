#include "api.h"


int callCriticalJVMCI() {
    return 10;
}

int callCriticalJVMCIAdd(int a, int b) {
    return a + b;
}

int callCriticalJVMCIString(const char* arg) {
    return 10;
}