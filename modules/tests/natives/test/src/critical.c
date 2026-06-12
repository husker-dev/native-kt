#include <api.h>
#include <stdio.h>
#include <string.h>

KBoolean criticalPrimitives(KChar a1, KBoolean a2, KByte a3, KShort a4, KInt a5, KLong a6, KFloat a7, KDouble a8) {
    return a1 == 'a' &&
        a2 == true &&
        a3 == 1 &&
        a4 == 2 &&
        a5 == 3 &&
        a6 == 4 &&
        a7 == 1.0f &&
        a8 == 2.0;
}

KBoolean criticalEnum(MyEnum a1) {
    return a1 == MyEnum_CASE1;
}

KBoolean criticalString(KString* a1) {
    return a1->length == 11 && strncmp(a1->data, "test string", a1->length) == 0;
}

KBoolean criticalStringN(KString* a1) {
    return a1 == NULL;
}

KBoolean criticalPrimitivesArray(KCharArray* a1, KBooleanArray* a2, KByteArray* a3, KShortArray* a4, KIntArray* a5, KLongArray* a6, KFloatArray* a7, KDoubleArray* a8) {
    return a1->length == 2 &&
            a1->elements[0] == 'a' &&
            a1->elements[1] == 'b' &&
            a2->length == 2 &&
            a2->elements[0] == true &&
            a2->elements[1] == false &&
            a3->length == 2 &&
            a3->elements[0] == 1 &&
            a3->elements[1] == 2 &&
            a4->length == 2 &&
            a4->elements[0] == 1 &&
            a4->elements[1] == 2 &&
            a5->length == 2 &&
            a5->elements[0] == 1 &&
            a5->elements[1] == 2 &&
            a6->length == 2 &&
            a6->elements[0] == 1 &&
            a6->elements[1] == 2 &&
            a7->length == 2 &&
            a7->elements[0] == 1.1f &&
            a7->elements[1] == 2.2f &&
            a8->length == 2 &&
            a8->elements[0] == 1.1 &&
            a8->elements[1] == 2.2;
}

KBoolean criticalPrimitivesArrayN(KCharArray* a1, KBooleanArray* a2, KByteArray* a3, KShortArray* a4, KIntArray* a5, KLongArray* a6, KFloatArray* a7, KDoubleArray* a8) {
    return a1 == NULL && a2 == NULL && a3 == NULL && a4 == NULL && a5 == NULL && a6 == NULL && a7 == NULL && a8 == NULL;
}

KBoolean criticalEnumArray(KIntArray* a1) {
    const MyEnum* elements = (MyEnum*)a1->elements;

    return a1->length == 2 &&
           elements[0] == MyEnum_CASE1 &&
           elements[1] == MyEnum_CASE2;
}

KBoolean criticalEnumArrayN(KIntArray* a1) {
    return a1 == NULL;
}

KChar criticalReturnChar() {
    return 'a';
}

KBoolean criticalReturnBoolean() {
    return true;
}

KByte criticalReturnByte() {
    return 1;
}

KShort criticalReturnShort() {
    return 1;
}

KInt criticalReturnInt() {
    return 1;
}

KLong criticalReturnLong() {
    return 1;
}

KFloat criticalReturnFloat() {
    return 1.0f;
}

KDouble criticalReturnDouble() {
    return 1.0;
}

MyEnum criticalReturnEnum() {
    return MyEnum_CASE1;
}