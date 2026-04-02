#include <api.h>
#include <string.h>

// Consume

KBoolean passVoid() {
    return true;
}

KBoolean passChar(KChar arg) {
    return arg == 'a';
}

KBoolean passBoolean(KBoolean arg) {
    return arg == true;
}

KBoolean passByte(KByte arg) {
    return arg == 1;
}

KBoolean passShort(KShort arg) {
    return arg == 1;
}

KBoolean passInt(KInt arg) {
    return arg == 99;
}

KBoolean passLong(KLong arg) {
    return arg == 9223372036854775805;
}

KBoolean passFloat(KFloat arg) {
    return arg == 99.9f;
}

KBoolean passDouble(KDouble arg) {
    return arg == 1.1;
}

KBoolean passString(KString arg) {
    return arg.length == 11 && strncmp(arg.data, "test string", arg.length) == 0;
}

KBoolean passEnum(MyEnum arg) {
    return arg == MyEnum_CASE2;
}

KBoolean passStruct(MyStruct arg) {
    return arg.a == 1 &&
        arg.b == 2 &&
        arg.c == 3 &&
        arg.d == 4;
}