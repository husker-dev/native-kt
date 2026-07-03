#include <api.h>
#include <stdio.h>
#include <string.h>

KBoolean critical_primitives(
    KChar a1, KBoolean a2,
    KByte a3, KUByte a4,
    KShort a5, KUShort a6,
    KInt a7, KUInt a8,
    KLong a9, KULong a10,
    KFloat a11, KDouble a12
) {
    return a1 == 'a' &&
        a2 == true &&
        a3 == 1 &&
        a4 == 255u &&
        a5 == 3 &&
        a6 == 65535u &&
        a7 == 5 &&
        a8 == 4294967295u &&
        a9 == 7 &&
        a10 == 18446744073709551615u &&
        a11 == 1.0f &&
        a12 == 2.0;
}

KBoolean critical_enum(MyEnum a1) {
    return a1 == MyEnum_CASE1;
}

KBoolean critical_string(KString* a1) {
    return a1->length == 11 && strncmp(a1->data, "test string", a1->length) == 0;
}

KBoolean critical_string_n(KString* a1) {
    return a1 == NULL;
}

KBoolean critical_primitives_array(
    KCharArray* a1, KBooleanArray* a2,
    KByteArray* a3, KUByteArray* a4,
    KShortArray* a5, KUShortArray* a6,
    KIntArray* a7,  KUIntArray* a8,
    KLongArray* a9, KULongArray* a10,
    KFloatArray* a11, KDoubleArray* a12
) {
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
            a4->elements[1] == 255u &&
            a5->length == 2 &&
            a5->elements[0] == 1 &&
            a5->elements[1] == 2 &&
            a6->length == 2 &&
            a6->elements[0] == 1 &&
            a6->elements[1] == 65535u &&
            a7->length == 2 &&
            a7->elements[0] == 1 &&
            a7->elements[1] == 2 &&
            a8->length == 2 &&
            a8->elements[0] == 1 &&
            a8->elements[1] == 4294967295u &&
            a9->length == 2 &&
            a9->elements[0] == 1 &&
            a9->elements[1] == 2 &&
            a10->length == 2 &&
            a10->elements[0] == 1 &&
            a10->elements[1] == 18446744073709551615u &&
            a11->length == 2 &&
            a11->elements[0] == 1.1f &&
            a11->elements[1] == 2.2f &&
            a12->length == 2 &&
            a12->elements[0] == 1.1 &&
            a12->elements[1] == 2.2;
}

KBoolean critical_primitives_array_n(
    KCharArray* a1, KBooleanArray* a2,
    KByteArray* a3, KUByteArray* a4,
    KShortArray* a5, KUShortArray* a6,
    KIntArray* a7, KUIntArray* a8,
    KLongArray* a9, KULongArray* a10,
    KFloatArray* a11, KDoubleArray* a12
) {
    return a1 == NULL && a2 == NULL &&
        a3 == NULL && a4 == NULL &&
        a5 == NULL && a6 == NULL &&
        a7 == NULL && a8 == NULL &&
        a9 == NULL && a10 == NULL &&
        a11 == NULL && a12 == NULL;
}

KBoolean critical_enum_array(KIntArray* a1) {
    const MyEnum* elements = (MyEnum*)a1->elements;

    return a1->length == 2 &&
           elements[0] == MyEnum_CASE1 &&
           elements[1] == MyEnum_CASE2;
}

KBoolean critical_enum_array_n(KIntArray* a1) {
    return a1 == NULL;
}

KChar critical_return_char() {
    return 'a';
}

KBoolean critical_return_boolean() {
    return true;
}

KByte critical_return_byte() {
    return 1;
}

KUByte critical_return_ubyte() {
    return 255u;
}

KShort critical_return_short() {
    return 1;
}

KUShort critical_return_ushort() {
    return 65535u;
}

KInt critical_return_int() {
    return 1;
}

KUInt critical_return_uint() {
    return 4294967295u;
}

KLong critical_return_long() {
    return 1;
}

KULong critical_return_ulong() {
    return 18446744073709551615u;
}

KFloat critical_return_float() {
    return 1.0f;
}

KDouble critical_return_double() {
    return 1.0;
}

MyEnum critical_return_enum() {
    return MyEnum_CASE1;
}