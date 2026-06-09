#include "api.h"

KInt callForeign() {
    return 10;
}

KInt callForeignAdd(KInt a, KInt b) {
    return a + b;
}

KInt callForeignString(KString* arg) {
    return 10;
}


KInt callCriticalForeign() {
    return 10;
}

KInt callCriticalForeignAdd(KInt a, KInt b) {
    return a + b;
}

KInt callCriticalForeignString(KString* arg) {
    return 10;
}