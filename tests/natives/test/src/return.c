#include <api.h>
#include <stdlib.h>
#include <string.h>

// Get

void get() {
}

KInt getInt() {
    return 99;
}

KLong getLong() {
    return 9223372036854775805;
}

KFloat getFloat() {
    return 99;
}

KDouble getDouble() {
    return 99.0;
}

KByte getByte() {
    return 99;
}

KBoolean getBoolean() {
    return true;
}

KChar getChar() {
    return 'a';
}

KString getStringLiteral() {
    return makeKString("test string", 11);
}

KString getString() {
    char* str = malloc(100);
    strcpy(str, "test string");
    return makeKString(str, 11);
}