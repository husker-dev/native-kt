#include <api.h>

KBoolean callbackArgCharArray(CallbackPassCharArray* arg) {
    return arg->invoke(arg, makeKCharArray((KChar[]) { 'a', 'b' }, 2));
}

KBoolean callbackArgBooleanArray(CallbackPassBooleanArray* arg) {
    return arg->invoke(arg, makeKBooleanArray((KBoolean[]) { true, false }, 2));
}

KBoolean callbackArgByteArray(CallbackPassByteArray* arg) {
    return arg->invoke(arg, makeKByteArray((KByte[]) { 1, 2 }, 2));
}

KBoolean callbackArgShortArray(CallbackPassShortArray* arg) {
    return arg->invoke(arg, makeKShortArray((KShort[]) { 1, 2 }, 2));
}

KBoolean callbackArgIntArray(CallbackPassIntArray* arg) {
    return arg->invoke(arg, makeKIntArray((KInt[]) { 1, 2 }, 2));
}

KBoolean callbackArgLongArray(CallbackPassLongArray* arg) {
    return arg->invoke(arg, makeKLongArray((KLong[]) { 1, 2 }, 2));
}

KBoolean callbackArgFloatArray(CallbackPassFloatArray* arg) {
    return arg->invoke(arg, makeKFloatArray((KFloat[]) { 1.1f, 2.2f }, 2));
}

KBoolean callbackArgDoubleArray(CallbackPassDoubleArray* arg) {
    return arg->invoke(arg, makeKDoubleArray((KDouble[]) { 1.1, 2.2 }, 2));
}
