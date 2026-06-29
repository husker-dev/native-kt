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

KShortArray* returnShortArray() {
    return KShortArray_of(1, 2);
}

KIntArray* returnIntArray() {
    return KIntArray_of(1, 2);
}

KLongArray* returnLongArray() {
    KLong* elements = malloc(2 * sizeof(int64_t));
    elements[0] = 1;
    elements[1] = 2;
    return KLongArray_new(elements, 2, true);
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