#include "api.h"
#include <stdio.h>

int callJni() {
    return 10;
}

int callJniAdd(int a, int b) {
    return a + b;
}

int callJniString(const char* arg) {
    return 10;
}

int callCriticalJni() {
    return 10;
}

int callCriticalJniString(const char* arg) {
    return 10;
}