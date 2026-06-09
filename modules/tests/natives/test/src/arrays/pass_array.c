#include <api.h>

KBoolean passArray(KIntArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}

KBoolean passCharArray(KCharArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 'a' &&
        arg->elements[1] == 'b';
}

KBoolean passBooleanArray(KBooleanArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == true &&
        arg->elements[1] == false;
}

KBoolean passByteArray(KByteArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}

KBoolean passShortArray(KShortArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}

KBoolean passIntArray(KIntArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}
KBoolean passLongArray(KLongArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}

KBoolean passFloatArray(KFloatArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1.1f &&
        arg->elements[1] == 2.2f;
}

KBoolean passDoubleArray(KDoubleArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1.1 &&
        arg->elements[1] == 2.2;
}

KBoolean passEnumArray(KIntArray* arg) {
    const MyEnum* elements = (MyEnum*)arg->elements;

    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        elements[0] == MyEnum_CASE1 &&
        elements[1] == MyEnum_CASE2;
}

KBoolean passDictionaryArray(KArray* arg) {
    MyDictionary** elements = (MyDictionary**)arg->elements;

    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        elements[0]->a == 1 &&
        elements[0]->b == 2 &&
        elements[0]->c == 3 &&
        elements[0]->d == 4 &&
        elements[1]->a == 5 &&
        elements[1]->b == 6 &&
        elements[1]->c == 7 &&
        elements[1]->d == 8;
}