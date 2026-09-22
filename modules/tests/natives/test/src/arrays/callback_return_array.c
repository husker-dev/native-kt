#include <api.h>
#include <string.h>

bool callback_return_char_array(RC_CallbackReturnCharArray* arg) {
    const KCharArray* array = callbackreturnchararray_invoke(arg);
    return array->length == 2 &&
        array->elements[0] == 'a' &&
        array->elements[1] == 'b';
}

bool callback_return_char_array_n(RC_CallbackReturnCharArrayN* arg) {
    return callbackreturnchararrayn_invoke(arg) == NULL;
}

bool callback_return_boolean_array(RC_CallbackReturnBooleanArray* arg) {
    const KBooleanArray* array = callbackreturnbooleanarray_invoke(arg);
    return array->length == 2 &&
        array->elements[0] == true &&
        array->elements[1] == false;
}

bool callback_return_byte_array(RC_CallbackReturnByteArray* arg) {
    const KByteArray* array = callbackreturnbytearray_invoke(arg);
    return array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 2;
}

bool callback_return_ubyte_array(RC_CallbackReturnUByteArray* arg) {
    const KUByteArray* array = callbackreturnubytearray_invoke(arg);
    return array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 255u;
}

bool callback_return_short_array(RC_CallbackReturnShortArray* arg) {
    const KShortArray* array = callbackreturnshortarray_invoke(arg);
    return array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 2;
}

bool callback_return_ushort_array(RC_CallbackReturnUShortArray* arg) {
    const KUShortArray* array = callbackreturnushortarray_invoke(arg);
    return array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 65535u;
}

bool callback_return_int_array(RC_CallbackReturnIntArray* arg) {
    const KIntArray* array = callbackreturnintarray_invoke(arg);
    return array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 2;
}

bool callback_return_uint_array(RC_CallbackReturnUIntArray* arg) {
    const KUIntArray* array = callbackreturnuintarray_invoke(arg);
    return array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 4294967295u;
}

bool callback_return_long_array(RC_CallbackReturnLongArray* arg) {
    const KLongArray* array = callbackreturnlongarray_invoke(arg);
    return array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 2;
}

bool callback_return_ulong_array(RC_CallbackReturnULongArray* arg) {
    const KULongArray* array = callbackreturnulongarray_invoke(arg);
    return array->length == 2 &&
        array->elements[0] == 1 &&
        array->elements[1] == 18446744073709551615u;
}

bool callback_return_float_array(RC_CallbackReturnFloatArray* arg) {
    const KFloatArray* array = callbackreturnfloatarray_invoke(arg);
    return array->length == 2 &&
        array->elements[0] == 1.1f &&
        array->elements[1] == 2.2f;
}

bool callback_return_double_array(RC_CallbackReturnDoubleArray* arg) {
    const KDoubleArray* array = callbackreturndoublearray_invoke(arg);
    return array->length == 2 &&
        array->elements[0] == 1.1 &&
        array->elements[1] == 2.2;
}

bool callback_return_string_array(RC_CallbackReturnStringArray* arg) {
    const KArray* array = callbackreturnstringarray_invoke(arg);
    const KString* el1 = (KString*) array->elements[0];
    const KString* el2 = (KString*) array->elements[1];

    return array->length == 2 &&
        strncmp(el1->data, "string1", el1->size) == 0 &&
        strncmp(el2->data, "string2", el2->size) == 0;
}

bool callback_return_string_array_n(RC_CallbackReturnStringArrayN* arg) {
    const KArray* array = callbackreturnstringarrayn_invoke(arg);
    return array->elements[0] == NULL && array->elements[1] == NULL;
}

bool callback_return_enum_array(RC_CallbackReturnEnumArray* arg) {
    const KIntArray* array = callbackreturnenumarray_invoke(arg);
    const MyEnum* elements = (MyEnum*)array->elements;

    return array->length == 2 &&
        elements[0] == MyEnum_CASE1 &&
        elements[1] == MyEnum_CASE2;
}

bool callback_return_dictionary_array(RC_CallbackReturnDictionaryArray* arg) {
    const KArray* array = callbackreturndictionaryarray_invoke(arg);
    MyDictionary** elements = (MyDictionary**)array->elements;

    return array->length == 2 &&
        elements[0]->a == 1 &&
        elements[0]->b == 2 &&
        elements[0]->c == 3 &&
        elements[0]->d == 4 &&
        elements[1]->a == 5 &&
        elements[1]->b == 6 &&
        elements[1]->c == 7 &&
        elements[1]->d == 8;
}

bool callback_return_dictionary_array_n(RC_CallbackReturnDictionaryArrayN* arg) {
    const KArray* array = callbackreturndictionaryarrayn_invoke(arg);
    return array->elements[0] == NULL && array->elements[1] == NULL;
}

bool callback_return_interface_array(RC_CallbackReturnInterfaceArray* arg) {
    const KArray* array = callbackreturninterfacearray_invoke(arg);
    RC_MyInterface** elements = (RC_MyInterface**) array->elements;
    const bool result = elements[0]->pointed == (void*) 1 &&
                        elements[1]->pointed == (void*) 1;
    elements[0]->free(elements[0]);
    elements[1]->free(elements[1]);
    return result;
}

bool callback_return_interface_array_n(RC_CallbackReturnInterfaceArrayN* arg) {
    const KArray* array = callbackreturninterfacearrayn_invoke(arg);
    return array->elements[0] == NULL && array->elements[1] == NULL;
}