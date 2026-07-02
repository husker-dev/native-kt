#include <api.h>
#include <string.h>

KBoolean callbackReturnChar(CallbackReturnChar* arg) {
    return arg->invoke(arg) == 'a';
}

KBoolean callbackReturnBoolean(CallbackReturnBoolean* arg) {
    return arg->invoke(arg) == true;
}

KBoolean callbackReturnByte(CallbackReturnByte* arg) {
    return arg->invoke(arg) == 1;
}

KBoolean callbackReturnUByte(CallbackReturnUByte* arg) {
    return arg->invoke(arg) == 255u;
}

KBoolean callbackReturnShort(CallbackReturnShort* arg) {
    return arg->invoke(arg) == 1;
}

KBoolean callbackReturnUShort(CallbackReturnUShort* arg) {
    return arg->invoke(arg) == 65535u;
}

KBoolean callbackReturnInt(CallbackReturnInt* arg) {
    return arg->invoke(arg) == 1;
}

KBoolean callbackReturnUInt(CallbackReturnUInt* arg) {
    return arg->invoke(arg) == 4294967295u;
}

KBoolean callbackReturnLong(CallbackReturnLong* arg) {
    return arg->invoke(arg) == 1;
}

KBoolean callbackReturnULong(CallbackReturnULong* arg) {
    return arg->invoke(arg) == 18446744073709551615u;
}

KBoolean callbackReturnFloat(CallbackReturnFloat* arg) {
    return arg->invoke(arg) == 1.1f;
}

KBoolean callbackReturnDouble(CallbackReturnDouble* arg) {
    return arg->invoke(arg) == 1.1;
}

KBoolean callbackReturnString(CallbackReturnString* arg) {
    const KString* str = arg->invoke(arg);
    return (str->__flags & 1) && strncmp(str->data, "test string", str->length) == 0;
}

KBoolean callbackReturnStringN(CallbackReturnStringN* arg) {
    return  arg->invoke(arg) == NULL;
}

VoidCallback* callbackReturnCallback(CallbackReturnCallback* arg) {
    return arg->invoke(arg);
}

KBoolean callbackReturnCallbackN(CallbackReturnCallbackN* arg) {
    return arg->invoke(arg) == NULL;
}

KBoolean callbackReturnEnum(CallbackReturnEnum* arg) {
    return arg->invoke(arg) == MyEnum_CASE2;
}

KBoolean callbackReturnDictionary(CallbackReturnDictionary* arg) {
    const MyDictionary* result = arg->invoke(arg);
    return result->a == 1 &&
        result->b == 2 &&
        result->c == 3 &&
        result->d == 4;
}

KBoolean callbackReturnDictionaryN(CallbackReturnDictionaryN* arg) {
    return arg->invoke(arg) == NULL;
}