#include <api.h>
#include <string.h>

KBoolean callback_return_char(CallbackReturnChar* arg) {
    return arg->invoke(arg) == 'a';
}

KBoolean callback_return_boolean(CallbackReturnBoolean* arg) {
    return arg->invoke(arg) == true;
}

KBoolean callback_return_byte(CallbackReturnByte* arg) {
    return arg->invoke(arg) == 1;
}

KBoolean callback_return_ubyte(CallbackReturnUByte* arg) {
    return arg->invoke(arg) == 255u;
}

KBoolean callback_return_short(CallbackReturnShort* arg) {
    return arg->invoke(arg) == 1;
}

KBoolean callback_return_ushort(CallbackReturnUShort* arg) {
    return arg->invoke(arg) == 65535u;
}

KBoolean callback_return_int(CallbackReturnInt* arg) {
    return arg->invoke(arg) == 1;
}

KBoolean callback_return_uint(CallbackReturnUInt* arg) {
    return arg->invoke(arg) == 4294967295u;
}

KBoolean callback_return_long(CallbackReturnLong* arg) {
    return arg->invoke(arg) == 1;
}

KBoolean callback_return_ulong(CallbackReturnULong* arg) {
    return arg->invoke(arg) == 18446744073709551615u;
}

KBoolean callback_return_float(CallbackReturnFloat* arg) {
    return arg->invoke(arg) == 1.1f;
}

KBoolean callback_return_double(CallbackReturnDouble* arg) {
    return arg->invoke(arg) == 1.1;
}

KBoolean callback_return_string(CallbackReturnString* arg) {
    const KString* str = arg->invoke(arg);
    return (str->__flags & 1) && strncmp(str->data, "test string", str->length) == 0;
}

KBoolean callback_return_string_n(CallbackReturnStringN* arg) {
    return  arg->invoke(arg) == NULL;
}

VoidCallback* callback_return_callback(CallbackReturnCallback* arg) {
    return arg->invoke(arg);
}

KBoolean callback_return_callback_n(CallbackReturnCallbackN* arg) {
    return arg->invoke(arg) == NULL;
}

KBoolean callback_return_enum(CallbackReturnEnum* arg) {
    return arg->invoke(arg) == MyEnum_CASE2;
}

KBoolean callback_return_dictionary(CallbackReturnDictionary* arg) {
    const MyDictionary* result = arg->invoke(arg);
    return result->a == 1 &&
        result->b == 2 &&
        result->c == 3 &&
        result->d == 4;
}

KBoolean callback_return_dictionary_n(CallbackReturnDictionaryN* arg) {
    return arg->invoke(arg) == NULL;
}