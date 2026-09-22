#include <api.h>
#include <stdio.h>
#include <string.h>

bool critical_primitives(
    uint16_t a1, bool a2,
    int8_t a3, uint8_t a4,
    int16_t a5, uint16_t a6,
    int32_t a7, uint32_t a8,
    int64_t a9, uint64_t a10,
    float a11, double a12
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

bool critical_enum(MyEnum a1) {
    return a1 == MyEnum_CASE1;
}

bool critical_string(KString* a1) {
    return a1->size == 11 && strncmp(a1->data, "test string", a1->size) == 0;
}

bool critical_string_n(KString* a1) {
    return a1 == NULL;
}

bool critical_interface(RC_MyInterface* a1) {
    return a1->pointed == (void*) 1;
}

bool critical_interface_n(RC_MyInterface* a1) {
    return a1 == NULL;
}

bool critical_primitives_array(
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

bool critical_primitives_array_n(
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

bool critical_enum_array(KIntArray* a1) {
    const MyEnum* elements = (MyEnum*)a1->elements;

    return a1->length == 2 &&
           elements[0] == MyEnum_CASE1 &&
           elements[1] == MyEnum_CASE2;
}

bool critical_enum_array_n(KIntArray* a1) {
    return a1 == NULL;
}

uint16_t critical_return_char(void) {
    return 'a';
}

bool critical_return_boolean(void) {
    return true;
}

int8_t critical_return_byte(void) {
    return 1;
}

uint8_t critical_return_ubyte(void) {
    return 255u;
}

int16_t critical_return_short(void) {
    return 1;
}

uint16_t critical_return_ushort(void) {
    return 65535u;
}

int32_t critical_return_int(void) {
    return 1;
}

uint32_t critical_return_uint(void) {
    return 4294967295u;
}

int64_t critical_return_long(void) {
    return 1;
}

uint64_t critical_return_ulong(void) {
    return 18446744073709551615u;
}

float critical_return_float(void) {
    return 1.0f;
}

double critical_return_double(void) {
    return 1.0;
}

MyEnum critical_return_enum(void) {
    return MyEnum_CASE1;
}

RC_MyInterface* critical_return_interface(void) {
    return rc_myinterface_new((void*) 1);
}