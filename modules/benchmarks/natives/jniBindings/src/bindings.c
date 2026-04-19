#include "api.h"

KInt callJni() {
    return 10;
}

KInt callJniAdd(KInt a, KInt b) {
    return a + b;
}

KInt callJniString(KString arg) {
    return 10;
}

KInt callCriticalJni() {
    return 10;
}

KInt callCriticalJniString(KString arg) {
    return 10;
}