#include <api.h>
#include <string.h>

bool callback_return_char(RC_CallbackReturnChar* arg) {
    return callbackreturnchar_invoke(arg) == 'a';
}

bool callback_return_boolean(RC_CallbackReturnBoolean* arg) {
    return callbackreturnboolean_invoke(arg) == true;
}

bool callback_return_byte(RC_CallbackReturnByte* arg) {
    return callbackreturnbyte_invoke(arg) == 1;
}

bool callback_return_ubyte(RC_CallbackReturnUByte* arg) {
    return callbackreturnubyte_invoke(arg) == 255u;
}

bool callback_return_short(RC_CallbackReturnShort* arg) {
    return callbackreturnshort_invoke(arg) == 1;
}

bool callback_return_ushort(RC_CallbackReturnUShort* arg) {
    return callbackreturnushort_invoke(arg) == 65535u;
}

bool callback_return_int(RC_CallbackReturnInt* arg) {
    return callbackreturnint_invoke(arg) == 1;
}

bool callback_return_uint(RC_CallbackReturnUInt* arg) {
    return callbackreturnuint_invoke(arg) == 4294967295u;
}

bool callback_return_long(RC_CallbackReturnLong* arg) {
    return callbackreturnlong_invoke(arg) == 1;
}

bool callback_return_ulong(RC_CallbackReturnULong* arg) {
    return callbackreturnulong_invoke(arg) == 18446744073709551615u;
}

bool callback_return_float(RC_CallbackReturnFloat* arg) {
    return callbackreturnfloat_invoke(arg) == 1.1f;
}

bool callback_return_double(RC_CallbackReturnDouble* arg) {
    return callbackreturndouble_invoke(arg) == 1.1;
}

bool callback_return_string(RC_CallbackReturnString* arg) {
    const KString* str = callbackreturnstring_invoke(arg);
    return strncmp(str->data, "test string", str->length) == 0;
}

bool callback_return_string_n(RC_CallbackReturnStringN* arg) {
    return callbackreturnstringn_invoke(arg) == NULL;
}

RC_VoidCallback* callback_return_callback(RC_CallbackReturnCallback* arg) {
    return callbackreturncallback_invoke(arg);
}

bool callback_return_callback_n(RC_CallbackReturnCallbackN* arg) {
    return callbackreturncallbackn_invoke(arg) == NULL;
}

bool callback_return_enum(RC_CallbackReturnEnum* arg) {
    return callbackreturnenum_invoke(arg) == MyEnum_CASE2;
}

bool callback_return_dictionary(RC_CallbackReturnDictionary* arg) {
    const MyDictionary* result = callbackreturndictionary_invoke(arg);
    return result->a == 1 &&
        result->b == 2 &&
        result->c == 3 &&
        result->d == 4;
}

bool callback_return_dictionary_n(RC_CallbackReturnDictionaryN* arg) {
    return callbackreturndictionaryn_invoke(arg) == NULL;
}

bool callback_return_interface(RC_CallbackReturnInterface* arg) {
    RC_MyInterface* inter = callbackreturninterface_invoke(arg->clone(arg));
    const bool result = inter->pointed == (void*) 1;
    inter->free(inter);
    return result;
}

bool callback_return_interface_n(RC_CallbackReturnInterfaceN* arg) {
    return callbackreturninterfacen_invoke(arg) == NULL;
}