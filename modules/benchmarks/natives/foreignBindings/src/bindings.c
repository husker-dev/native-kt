#include "api.h"

KInt call_foreign() {
    return 10;
}

KInt call_foreign_add(KInt a, KInt b) {
    return a + b;
}

KInt call_foreign_string(KString* arg) {
    return 10;
}


KInt call_critical_foreign() {
    return 10;
}

KInt call_critical_foreign_add(KInt a, KInt b) {
    return a + b;
}

KInt call_critical_foreign_string(KString* arg) {
    return 10;
}