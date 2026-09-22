#include <api.h>
#include <stdio.h>
#include <string.h>

bool pass_char_array(KCharArray* arg) {
    return arg->length == 2 &&
        arg->elements[0] == 'a' &&
        arg->elements[1] == 'b';
}

bool pass_char_array_n(KCharArray* arg) {
    return arg == NULL;
}

bool pass_boolean_array(KBooleanArray* arg) {
    return arg->length == 2 &&
        arg->elements[0] == true &&
        arg->elements[1] == false;
}

bool pass_byte_array(KByteArray* arg) {
    return arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}

bool pass_ubyte_array(KUByteArray* arg) {
    return arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 255u;
}

bool pass_short_array(KShortArray* arg) {
    return arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}

bool pass_ushort_array(KUShortArray* arg) {
    return arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 65535u;
}

bool pass_int_array(KIntArray* arg) {
    return arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}

bool pass_uint_array(KUIntArray* arg) {
    return arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 4294967295u;
}

bool pass_long_array(KLongArray* arg) {
    return arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}

bool pass_ulong_array(KULongArray* arg) {
    return arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 18446744073709551615u;
}

bool pass_float_array(KFloatArray* arg) {
    return arg->length == 2 &&
        arg->elements[0] == 1.1f &&
        arg->elements[1] == 2.2f;
}

bool pass_double_array(KDoubleArray* arg) {
    return arg->length == 2 &&
        arg->elements[0] == 1.1 &&
        arg->elements[1] == 2.2;
}

bool pass_string_array(KArray* arg) {
    const KString* el1 = (KString*) arg->elements[0];
    const KString* el2 = (KString*) arg->elements[1];

    return arg->length == 2 &&
        strncmp(el1->data, "string1", el1->size) == 0 &&
        strncmp(el2->data, "string2", el2->size) == 0;
}

bool pass_string_array_n(KArray* arg) {
    return arg->elements[0] == NULL && arg->elements[1] == NULL;
}

bool pass_enum_array(KIntArray* arg) {
    const MyEnum* elements = (MyEnum*)arg->elements;

    return arg->length == 2 &&
        elements[0] == MyEnum_CASE1 &&
        elements[1] == MyEnum_CASE2;
}

bool pass_dictionary_array(KArray* arg) {
    MyDictionary** elements = (MyDictionary**)arg->elements;

    return arg->length == 2 &&
        elements[0]->a == 1 &&
        elements[0]->b == 2 &&
        elements[0]->c == 3 &&
        elements[0]->d == 4 &&
        elements[1]->a == 5 &&
        elements[1]->b == 6 &&
        elements[1]->c == 7 &&
        elements[1]->d == 8;
}

bool pass_dictionary_array_n(KArray* arg) {
    return arg->elements[0] == NULL && arg->elements[1] == NULL;
}

bool pass_interface_array(KArray* arg) {
    RC_MyInterface** elements = (RC_MyInterface**) arg->elements;
    return elements[0]->pointed == (void*) 1 &&
           elements[1]->pointed == (void*) 1;
}

bool pass_interface_array_n(KArray* arg) {
    return arg->elements[0] == NULL && arg->elements[1] == NULL;
}