#include <api.h>
#include <string.h>

KBoolean callbackReturnCharArray(CallbackReturnCharArray* arg) {
    const KCharArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 'a' &&
        array->elements[1] == 'b';
}

KBoolean callbackReturnBooleanArray(CallbackReturnBooleanArray* arg) {
    const KBooleanArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == true &&
        array->elements[1] == false;
}

KBoolean callbackReturnByteArray(CallbackReturnByteArray* arg) {
    const KByteArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 2;
}

KBoolean callbackReturnShortArray(CallbackReturnShortArray* arg) {
    const KShortArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 2;
}

KBoolean callbackReturnIntArray(CallbackReturnIntArray* arg) {
    const KIntArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 2;
}

KBoolean callbackReturnLongArray(CallbackReturnLongArray* arg) {
    const KLongArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 2;
}

KBoolean callbackReturnFloatArray(CallbackReturnFloatArray* arg) {
    const KFloatArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1.1f &&
        array->elements[1] == 2.2f;
}

KBoolean callbackReturnDoubleArray(CallbackReturnDoubleArray* arg) {
    const KDoubleArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1.1 &&
        array->elements[1] == 2.2;
}

KBoolean callbackReturnStringArray(CallbackReturnStringArray* arg) {
    const KArray* array = arg->invoke(arg);
    const KString* el1 = (KString*) array->elements[0];
    const KString* el2 = (KString*) array->elements[1];

    return (array->__flags & 1) &&
        array->length == 2 &&
        strncmp(el1->data, "string1", el1->length) == 0 &&
        strncmp(el2->data, "string2", el2->length) == 0;
}

KBoolean callbackReturnEnumArray(CallbackReturnEnumArray* arg) {
    const KIntArray* array = arg->invoke(arg);
    const MyEnum* elements = (MyEnum*)array->elements;

    return (array->__flags & 1) &&
        array->length == 2 &&
        elements[0] == MyEnum_CASE1 &&
        elements[1] == MyEnum_CASE2;
}

KBoolean callbackReturnDictionaryArray(CallbackReturnDictionaryArray* arg) {
    const KArray* array = arg->invoke(arg);
    MyDictionary** elements = (MyDictionary**)array->elements;

    return (array->__flags & 1) &&
        array->length == 2 &&
        elements[0]->a == 1 &&
        elements[0]->b == 2 &&
        elements[0]->c == 3 &&
        elements[0]->d == 4 &&
        elements[1]->a == 5 &&
        elements[1]->b == 6 &&
        elements[1]->c == 7 &&
        elements[1]->d == 8;
}