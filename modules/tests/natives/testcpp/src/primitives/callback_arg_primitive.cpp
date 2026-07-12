#include <api.hpp>

void callback_void(VoidCallback* arg) {
    arg->invoke(arg);
}

KBoolean callback_void_n(VoidCallback* arg) {
    return arg == nullptr;
}

KBoolean callback_arg_char(CallbackPassChar* arg) {
    return arg->invoke(arg, 'a');
}

KBoolean callback_arg_boolean(CallbackPassBoolean* arg) {
    return arg->invoke(arg, true);
}

KBoolean callback_arg_byte(CallbackPassByte* arg) {
    return arg->invoke(arg, 1);
}

KBoolean callback_arg_ubyte(CallbackPassUByte* arg) {
    return arg->invoke(arg, 255u);
}

KBoolean callback_arg_short(CallbackPassShort* arg) {
    return arg->invoke(arg, 1);
}

KBoolean callback_arg_ushort(CallbackPassUShort* arg) {
    return arg->invoke(arg, 65535u);
}

KBoolean callback_arg_int(CallbackPassInt* arg) {
    return arg->invoke(arg, 1);
}

KBoolean callback_arg_uint(CallbackPassUInt* arg) {
    return arg->invoke(arg, 4294967295u);
}

KBoolean callback_arg_long(CallbackPassLong* arg) {
    return arg->invoke(arg, 1);
}

KBoolean callback_arg_ulong(CallbackPassULong* arg) {
    return arg->invoke(arg, 18446744073709551615u);
}

KBoolean callback_arg_float(CallbackPassFloat* arg) {
    return arg->invoke(arg, 1.1f);
}

KBoolean callback_arg_double(CallbackPassDouble* arg) {
    return arg->invoke(arg, 1.1);
}

KBoolean callback_arg_string(CallbackPassString* arg) {
    return arg->invoke(arg, new KString("test string", 11, 11, false));
}

KBoolean callback_arg_string_n(CallbackPassStringN* arg) {
    return arg->invoke(arg, nullptr);
}

KBoolean callback_arg_callback(VoidCallback* pass, CallbackPassCallback* arg) {
    return arg->invoke(arg, pass);
}

KBoolean callback_arg_callback_n(CallbackPassCallbackN* arg) {
    return arg->invoke(arg, nullptr);
}

KBoolean callback_arg_enum(CallbackPassEnum* arg) {
    return arg->invoke(arg, CASE2);
}

KBoolean callback_arg_dictionary(CallbackPassDictionary* arg) {
    return arg->invoke(arg, new MyDictionary(1, 2, 3, 4));
}

KBoolean callback_arg_dictionary_n(CallbackPassDictionaryN* arg) {
    return arg->invoke(arg, nullptr);
}