#include "api.h"

KInt call_jni() {
    return 10;
}

KInt call_jni_add(KInt a, KInt b) {
    return a + b;
}

KInt call_jni_string(KString* arg) {
    return 10;
}

KInt call_critical_jni() {
    return 10;
}

KInt call_critical_jni_string(KString* arg) {
    return 10;
}