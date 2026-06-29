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

KShort returnShort() {
    return 99;
}

KInt returnInt() {
    return 99;
}

KLong returnLong() {
    return 9223372036854775805;
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