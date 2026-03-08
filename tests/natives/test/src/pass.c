#include <api.h>
#include <string.h>

// Consume

KBoolean consume() {
    return true;
}

KBoolean consumeInt(KInt arg) {
    return arg == 99;
}

KBoolean consumeLong(KLong arg) {
    return arg == 9223372036854775805;
}

KBoolean consumeFloat(KFloat arg) {
    return arg == 99.9f;
}

KBoolean consumeDouble(KDouble arg) {
    return arg == 1.1;
}

KBoolean consumeByte(KByte arg) {
    return arg == 1;
}

KBoolean consumeBoolean(KBoolean arg) {
    return arg == true;
}

KBoolean consumeChar(KChar arg) {
    return arg == 'a';
}

KBoolean consumeString(KString arg) {
    return arg.length == 11 && strncmp(arg.data, "test string", arg.length) == 0;
}