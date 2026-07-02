#include <api.h>
#include <stdio.h>
#include <string.h>

KBoolean passCharArray(KCharArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 'a' &&
        arg->elements[1] == 'b';
}

KBoolean passCharArrayN(KCharArray* arg) {
    return arg == NULL;
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

KBoolean passUByteArray(KUByteArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 255u;
}

KBoolean passShortArray(KShortArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}

KBoolean passUShortArray(KUShortArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 65535u;
}

KBoolean passIntArray(KIntArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}

KBoolean passUIntArray(KUIntArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 4294967295u;
}

KBoolean passLongArray(KLongArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}

KBoolean passULongArray(KULongArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 18446744073709551615u;
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

KBoolean passStringArray(KArray* arg) {
    const KString* el1 = (KString*) arg->elements[0];
    const KString* el2 = (KString*) arg->elements[1];

    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        strncmp(el1->data, "string1", el1->length) == 0 &&
        strncmp(el2->data, "string2", el2->length) == 0;
}

KBoolean passStringArrayN(KArray* arg) {
    return arg->elements[0] == NULL && arg->elements[1] == NULL;
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

KBoolean passDictionaryArrayN(KArray* arg) {
    return arg->elements[0] == NULL && arg->elements[1] == NULL;
}