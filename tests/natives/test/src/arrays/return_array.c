#include <api.h>
#include <stdlib.h>

KCharArray returnCharArray() {
    KChar* elements = malloc(2 * sizeof(KChar));
    elements[0] = 'a';
    elements[1] = 'b';
    return makeKCharArray(elements, 2);
}

KBooleanArray returnBooleanArray() {
    KBoolean* elements = malloc(2 * sizeof(KBoolean));
    elements[0] = true;
    elements[1] = false;
    return makeKBooleanArray(elements, 2);
}

KByteArray returnByteArray() {
    KByte* elements = malloc(2 * sizeof(KBoolean));
    elements[0] = 1;
    elements[1] = 2;
    return makeKByteArray(elements, 2);
}

KShortArray returnShortArray() {
    KShort* elements = malloc(2 * sizeof(KShort));
    elements[0] = 1;
    elements[1] = 2;
    return makeKShortArray(elements, 2);
}

KIntArray returnIntArray() {
    KInt* elements = malloc(2 * sizeof(KInt));
    elements[0] = 1;
    elements[1] = 2;
    return makeKIntArray(elements, 2);
}

KLongArray returnLongArray() {
    KLong* elements = malloc(2 * sizeof(KLong));
    elements[0] = 1;
    elements[1] = 2;
    return makeKLongArray(elements, 2);
}

KFloatArray returnFloatArray() {
    KFloat* elements = malloc(2 * sizeof(KFloat));
    elements[0] = 1.1;
    elements[1] = 2.2;
    return makeKFloatArray(elements, 2);
}

KDoubleArray returnDoubleArray() {
    KDouble* elements = malloc(2 * sizeof(KDouble));
    elements[0] = 1.1;
    elements[1] = 2.2;
    return makeKDoubleArray(elements, 2);
}

KArray returnEnumArray() {
    MyEnum* elements = malloc(2 * sizeof(MyEnum));
    elements[0] = MyEnum_CASE1;
    elements[1] = MyEnum_CASE2;
    return makeKArray(elements, 2);
}

KArray returnStructArray() {
    MyStruct* elements = malloc(2 * sizeof(MyStruct));
    elements[0] = (MyStruct){ 1, 2, 3, 4 };
    elements[1] = (MyStruct){ 5, 6, 7, 8 };
    return makeKArray(elements, 2);
}