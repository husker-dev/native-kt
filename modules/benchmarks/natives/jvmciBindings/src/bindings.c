#include "api.h"


KInt call_critical_jvmci() {
    return 10;
}

KInt call_critical_jvmci_add(KInt a, KInt b) {
    return a + b;
}

KInt call_critical_jvmci_string(KString* arg) {
    return 10;
}