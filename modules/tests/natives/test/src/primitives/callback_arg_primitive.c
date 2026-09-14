#include <api.h>

void callback_void(RC_VoidCallback* arg) {
    voidcallback_invoke(arg);
}

bool callback_void_n(RC_VoidCallback* arg) {
    return arg == NULL;
}

bool callback_arg_char(RC_CallbackPassChar* arg) {
    return callbackpasschar_invoke(arg, 'a');
}

bool callback_arg_boolean(RC_CallbackPassBoolean* arg) {
    return callbackpassboolean_invoke(arg, true);
}

bool callback_arg_byte(RC_CallbackPassByte* arg) {
    return callbackpassbyte_invoke(arg, 1);
}

bool callback_arg_ubyte(RC_CallbackPassUByte* arg) {
    return callbackpassubyte_invoke(arg, 255u);
}

bool callback_arg_short(RC_CallbackPassShort* arg) {
    return callbackpassshort_invoke(arg, 1);
}

bool callback_arg_ushort(RC_CallbackPassUShort* arg) {
    return callbackpassushort_invoke(arg, 65535u);
}

bool callback_arg_int(RC_CallbackPassInt* arg) {
    return callbackpassint_invoke(arg, 1);
}

bool callback_arg_uint(RC_CallbackPassUInt* arg) {
    return callbackpassuint_invoke(arg, 4294967295u);
}

bool callback_arg_long(RC_CallbackPassLong* arg) {
    return callbackpasslong_invoke(arg, 1);
}

bool callback_arg_ulong(RC_CallbackPassULong* arg) {
    return callbackpassulong_invoke(arg, 18446744073709551615u);
}

bool callback_arg_float(RC_CallbackPassFloat* arg) {
    return callbackpassfloat_invoke(arg, 1.1f);
}

bool callback_arg_double(RC_CallbackPassDouble* arg) {
    return callbackpassdouble_invoke(arg, 1.1);
}

bool callback_arg_string(RC_CallbackPassString* arg) {
    return callbackpassstring_invoke(arg, kstring_new("test string"));
}

bool callback_arg_string_n(RC_CallbackPassStringN* arg) {
    return callbackpassstringn_invoke(arg, NULL);
}

bool callback_arg_callback(RC_VoidCallback* pass, RC_CallbackPassCallback* arg) {
    return callbackpasscallback_invoke(arg, pass->clone(pass));
}

bool callback_arg_callback_n(RC_CallbackPassCallbackN* arg) {
    return callbackpasscallbackn_invoke(arg, NULL);
}

bool callback_arg_enum(RC_CallbackPassEnum* arg) {
    return callbackpassenum_invoke(arg, MyEnum_CASE2);
}

bool callback_arg_dictionary(RC_CallbackPassDictionary* arg) {
    return callbackpassdictionary_invoke(arg, MyDictionary_new(1, 2, 3, 4));
}

bool callback_arg_dictionary_n(RC_CallbackPassDictionaryN* arg) {
    return callbackpassdictionaryn_invoke(arg, NULL);
}

bool callback_arg_interface(RC_MyInterface* pass, RC_CallbackPassInterface* arg) {
    return callbackpassinterface_invoke(arg, pass->clone(pass));
}

bool callback_arg_interface_n(RC_CallbackPassInterfaceN* arg) {
    return callbackpassinterfacen_invoke(arg, NULL);
}