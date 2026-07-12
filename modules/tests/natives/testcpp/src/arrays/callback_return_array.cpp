#include <api.hpp>
#include <cstring>

KBoolean callback_return_char_array(CallbackReturnCharArray* arg) {
    const KCharArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 'a' &&
        array->elements[1] == 'b';
}

KBoolean callback_return_char_array_n(CallbackReturnCharArrayN* arg) {
    return arg->invoke(arg) == nullptr;
}

KBoolean callback_return_boolean_array(CallbackReturnBooleanArray* arg) {
    const KBooleanArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == true &&
        array->elements[1] == false;
}

KBoolean callback_return_byte_array(CallbackReturnByteArray* arg) {
    const KByteArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 2;
}

KBoolean callback_return_ubyte_array(CallbackReturnUByteArray* arg) {
    const KUByteArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 255u;
}

KBoolean callback_return_short_array(CallbackReturnShortArray* arg) {
    const KShortArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 2;
}

KBoolean callback_return_ushort_array(CallbackReturnUShortArray* arg) {
    const KUShortArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 65535u;
}

KBoolean callback_return_int_array(CallbackReturnIntArray* arg) {
    const KIntArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 2;
}

KBoolean callback_return_uint_array(CallbackReturnUIntArray* arg) {
    const KUIntArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 4294967295u;
}

KBoolean callback_return_long_array(CallbackReturnLongArray* arg) {
    const KLongArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 2;
}

KBoolean callback_return_ulong_array(CallbackReturnULongArray* arg) {
    const KULongArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 18446744073709551615u;
}

KBoolean callback_return_float_array(CallbackReturnFloatArray* arg) {
    const KFloatArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1.1f &&
        array->elements[1] == 2.2f;
}

KBoolean callback_return_double_array(CallbackReturnDoubleArray* arg) {
    const KDoubleArray* array = arg->invoke(arg);
    return (array->__flags & 1) &&
        array->length == 2 &&
        array->elements[0] == 1.1 &&
        array->elements[1] == 2.2;
}

KBoolean callback_return_string_array(CallbackReturnStringArray* arg) {
    const KArray* array = arg->invoke(arg);
    const KString* el1 = (KString*) array->elements[0];
    const KString* el2 = (KString*) array->elements[1];

    return (array->__flags & 1) &&
        array->length == 2 &&
        strncmp(el1->data, "string1", el1->length) == 0 &&
        strncmp(el2->data, "string2", el2->length) == 0;
}

KBoolean callback_return_string_array_n(CallbackReturnStringArrayN* arg) {
    const KArray* array = arg->invoke(arg);
    return array->elements[0] == NULL && array->elements[1] == NULL;
}

KBoolean callback_return_enum_array(CallbackReturnEnumArray* arg) {
    const KIntArray* array = arg->invoke(arg);
    const MyEnum* elements = (MyEnum*)array->elements;

    return (array->__flags & 1) &&
        array->length == 2 &&
        elements[0] == CASE1 &&
        elements[1] == CASE2;
}

KBoolean callback_return_dictionary_array(CallbackReturnDictionaryArray* arg) {
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

KBoolean callback_return_dictionary_array_n(CallbackReturnDictionaryArrayN* arg) {
    const KArray* array = arg->invoke(arg);
    return array->elements[0] == nullptr && array->elements[1] == nullptr;
}