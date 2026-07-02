#include <api.h>
#include <string.h>

void returnVoid() {
}

KChar returnChar() {
    return 'a';
}

KBoolean returnBoolean() {
    return true;
}

KByte returnByte() {
    return 99;
}

KUByte returnUByte() {
    return 255u;
}

KShort returnShort() {
    return 99;
}

KUShort returnUShort() {
    return 65535u;
}

KInt returnInt() {
    return 99;
}

KUInt returnUInt() {
    return 4294967295u;
}

KLong returnLong() {
    return 9223372036854775805;
}

KULong returnULong() {
    return 18446744073709551615u;
}

KFloat returnFloat() {
    return 99;
}

KDouble returnDouble() {
    return 99.0;
}

KString* returnString() {
    return KString_new("test string", 11, 11, false);
}

KString* returnStringN() {
    return NULL;
}

MyEnum returnEnum() {
    return MyEnum_CASE2;
}

MyDictionary* returnDictionary() {
    return MyDictionary_new(1, 2, 3 ,4);
}

MyDictionary* returnDictionaryN() {
    return NULL;
}