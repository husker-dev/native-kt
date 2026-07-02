#include <api.h>
#include <string.h>

// Consume

KBoolean passVoid() {
    return true;
}

KBoolean passChar(const KChar arg) {
    return arg == 'a';
}

KBoolean passBoolean(const KBoolean arg) {
    return arg == true;
}

KBoolean passByte(const KByte arg) {
    return arg == 1;
}

KBoolean passUByte(const KUByte arg) {
    return arg == 255u;
}

KBoolean passShort(const KShort arg) {
    return arg == 1;
}

KBoolean passUShort(const KUShort arg) {
    return arg == 65535u;
}

KBoolean passInt(const KInt arg) {
    return arg == 99;
}

KBoolean passUInt(const KUInt arg) {
    return arg == 4294967295u;
}

KBoolean passLong(const KLong arg) {
    return arg == 9223372036854775805;
}

KBoolean passULong(const KULong arg) {
    return arg == 18446744073709551615u;
}

KBoolean passFloat(const KFloat arg) {
    return arg == 99.9f;
}

KBoolean passDouble(const KDouble arg) {
    return arg == 1.1;
}

KBoolean passString(KString* arg) {
    return !(arg->__flags & 1) && arg->length == 11 && strncmp(arg->data, "test string", arg->length) == 0;
}

KBoolean passStringN(KString* arg) {
    return arg == NULL;
}

KBoolean passEnum(const MyEnum arg) {
    return arg == MyEnum_CASE2;
}

KBoolean passDictionary(MyDictionary* arg) {
    return arg->a == 1 &&
        arg->b == 2 &&
        arg->c == 3 &&
        arg->d == 4;
}

KBoolean passDictionaryN(MyDictionary* arg) {
    return arg == NULL;
}