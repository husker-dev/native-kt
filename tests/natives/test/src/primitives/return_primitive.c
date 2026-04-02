#include <api.h>
#include <stdlib.h>
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

KString returnStringLiteral() {
    return makeKString("test string", 11);
}

KString returnString() {
    char* str = malloc(100);
    strcpy(str, "test string");
    return makeKString(str, 11);
}

MyEnum returnEnum() {
    return MyEnum_CASE2;
}

MyStruct returnStruct() {
    return (MyStruct){ 1, 2, 3, 4 };
}