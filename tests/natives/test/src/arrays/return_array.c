#include <api.h>
#include <stdlib.h>

KCharArray returnCharArray() {
    return KCharArray_of('a', 'b');
}

KBooleanArray returnBooleanArray() {
    return KBooleanArray_of(true, false);
}

KByteArray returnByteArray() {
    return KByteArray_of(1, 2);
}

KShortArray returnShortArray() {
    return KShortArray_of(1, 2);
}

KIntArray returnIntArray() {
    return KIntArray_of(1, 2);
}

KLongArray returnLongArray() {
    return KLongArray_of(1, 2);
}

KFloatArray returnFloatArray() {
    return KFloatArray_of(1.1f, 2.2f);
}

KDoubleArray returnDoubleArray() {
    return KDoubleArray_of(1.1, 2.2);
}

KIntArray returnEnumArray() {
    return KIntArray_of(MyEnum_CASE1, MyEnum_CASE2);
}

KArray returnDictionaryArray() {
    return KArray_of(
        MyDictionary_new(1, 2, 3, 4),
        MyDictionary_new(5, 6, 7, 8)
    );
}