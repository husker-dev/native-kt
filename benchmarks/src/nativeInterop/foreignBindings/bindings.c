#include "api.h"
#include <stdio.h>

int callForeign() {
    return 10;
}

int callForeignAdd(int a, int b) {
    return a + b;
}

int callForeignString(const char* arg) {
    return 10;
}


int callCriticalForeign() {
    return 10;
}

int callCriticalForeignAdd(int a, int b) {
    return a + b;
}

int callCriticalForeignString(const char* arg) {
    return 10;
}