#include <api.h>
#include <string.h>

void callbackVoid(VoidCallback* arg) {
    arg->invoke(arg);
}

KBoolean callbackVoidN(VoidCallback* arg) {
    return arg == NULL;
}

KBoolean callbackArgChar(CallbackPassChar* arg) {
    return arg->invoke(arg, 'a');
}

KBoolean callbackArgBoolean(CallbackPassBoolean* arg) {
    return arg->invoke(arg, true);
}

KBoolean callbackArgByte(CallbackPassByte* arg) {
    return arg->invoke(arg, 1);
}

KBoolean callbackArgUByte(CallbackPassUByte* arg) {
    return arg->invoke(arg, 255u);
}

KBoolean callbackArgShort(CallbackPassShort* arg) {
    return arg->invoke(arg, 1);
}

KBoolean callbackArgUShort(CallbackPassUShort* arg) {
    return arg->invoke(arg, 65535u);
}

KBoolean callbackArgInt(CallbackPassInt* arg) {
    return arg->invoke(arg, 1);
}

KBoolean callbackArgUInt(CallbackPassUInt* arg) {
    return arg->invoke(arg, 4294967295u);
}

KBoolean callbackArgLong(CallbackPassLong* arg) {
    return arg->invoke(arg, 1);
}

KBoolean callbackArgULong(CallbackPassULong* arg) {
    return arg->invoke(arg, 18446744073709551615u);
}

KBoolean callbackArgFloat(CallbackPassFloat* arg) {
    return arg->invoke(arg, 1.1f);
}

KBoolean callbackArgDouble(CallbackPassDouble* arg) {
    return arg->invoke(arg, 1.1);
}

KBoolean callbackArgString(CallbackPassString* arg) {
    return arg->invoke(arg, KString_new("test string", 11, 11, false));
}

KBoolean callbackArgStringN(CallbackPassStringN* arg) {
    return arg->invoke(arg, NULL);
}

KBoolean callbackArgCallback(VoidCallback* pass, CallbackPassCallback* arg) {
    return arg->invoke(arg, pass);
}

KBoolean callbackArgCallbackN(CallbackPassCallbackN* arg) {
    return arg->invoke(arg, NULL);
}

KBoolean callbackArgEnum(CallbackPassEnum* arg) {
    return arg->invoke(arg, MyEnum_CASE2);
}

KBoolean callbackArgDictionary(CallbackPassDictionary* arg) {
    return arg->invoke(arg, MyDictionary_new(1, 2, 3, 4));
}

KBoolean callbackArgDictionaryN(CallbackPassDictionaryN* arg) {
    return arg->invoke(arg, NULL);
}