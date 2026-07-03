#include <api.h>
#include <stdio.h>
#include <string.h>

KBoolean pass_char_array(KCharArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 'a' &&
        arg->elements[1] == 'b';
}

KBoolean pass_char_array_n(KCharArray* arg) {
    return arg == NULL;
}

KBoolean pass_boolean_array(KBooleanArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == true &&
        arg->elements[1] == false;
}

KBoolean pass_byte_array(KByteArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}

KBoolean pass_ubyte_array(KUByteArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 255u;
}

KBoolean pass_short_array(KShortArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}

KBoolean pass_ushort_array(KUShortArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 65535u;
}

KBoolean pass_int_array(KIntArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}

KBoolean pass_uint_array(KUIntArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 4294967295u;
}

KBoolean pass_long_array(KLongArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 2;
}

KBoolean pass_ulong_array(KULongArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1 &&
        arg->elements[1] == 18446744073709551615u;
}

KBoolean pass_float_array(KFloatArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1.1f &&
        arg->elements[1] == 2.2f;
}

KBoolean pass_double_array(KDoubleArray* arg) {
    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        arg->elements[0] == 1.1 &&
        arg->elements[1] == 2.2;
}

KBoolean pass_string_array(KArray* arg) {
    const KString* el1 = (KString*) arg->elements[0];
    const KString* el2 = (KString*) arg->elements[1];

    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        strncmp(el1->data, "string1", el1->length) == 0 &&
        strncmp(el2->data, "string2", el2->length) == 0;
}

KBoolean pass_string_array_n(KArray* arg) {
    return arg->elements[0] == NULL && arg->elements[1] == NULL;
}

KBoolean pass_enum_array(KIntArray* arg) {
    const MyEnum* elements = (MyEnum*)arg->elements;

    return !(arg->__flags & 1) &&
        arg->length == 2 &&
        elements[0] == MyEnum_CASE1 &&
        elements[1] == MyEnum_CASE2;
}

KBoolean pass_dictionary_array(KArray* arg) {
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

KBoolean pass_dictionary_array_n(KArray* arg) {
    return arg->elements[0] == NULL && arg->elements[1] == NULL;
}