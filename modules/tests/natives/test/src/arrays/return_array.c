#include <api.h>
#include <stdlib.h>
#include <string.h>

KCharArray* returnCharArray() {
    return KCharArray_of('a', 'b');
}

KCharArray* returnCharArrayN() {
    return NULL;
}

KBooleanArray* returnBooleanArray() {
    return KBooleanArray_of(true, false);
}

KByteArray* returnByteArray() {
    return KByteArray_of(1, 2);
}

KUByteArray* returnUByteArray() {
    return KUByteArray_of(1, 255u);
}

KShortArray* returnShortArray() {
    return KShortArray_of(1, 2);
}

KUShortArray* returnUShortArray() {
    return KUShortArray_of(1, 65535u);
}

KIntArray* returnIntArray() {
    return KIntArray_of(1, 2);
}

KUIntArray* returnUIntArray() {
    return KUIntArray_of(1, 4294967295u);
}

KLongArray* returnLongArray() {
    KLong* elements = malloc(2 * sizeof(KLong));
    elements[0] = 1;
    elements[1] = 2;
    return KLongArray_new(elements, 2, true);
}

KULongArray* returnULongArray() {
    KULong* elements = malloc(2 * sizeof(KULong));
    elements[0] = 1;
    elements[1] = 18446744073709551615u;
    return KULongArray_new(elements, 2, true);
}

KFloatArray* returnFloatArray() {
    return KFloatArray_of(1.1f, 2.2f);
}

KDoubleArray* returnDoubleArray() {
    return KDoubleArray_of(1.1, 2.2);
}

KArray* returnStringArray() {
    return KArray_of(
        KString_new("string1", 7, 7, false),
        KString_new("string2", 7, 7, false)
    );
}

KArray* returnStringArrayN() {
    return KArray_of(NULL, NULL);
}

KIntArray* returnEnumArray() {
    return KIntArray_of(MyEnum_CASE1, MyEnum_CASE2);
}

KArray* returnDictionaryArray() {
    return KArray_of(
        MyDictionary_new(1, 2, 3, 4),
        MyDictionary_new(5, 6, 7, 8)
    );
}

KArray* returnDictionaryArrayN() {
    return KArray_of(NULL, NULL);
}