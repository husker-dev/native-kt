#include "api.h"


KInt callCriticalJVMCI() {
    return 10;
}

KInt callCriticalJVMCIAdd(KInt a, KInt b) {
    return a + b;
}

KInt callCriticalJVMCIString(KString* arg) {
    return 10;
}