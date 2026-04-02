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

KBoolean callbackReturnShort(CallbackReturnShort* arg) {
    return arg->invoke(arg) == 1;
}

KBoolean callbackReturnInt(CallbackReturnInt* arg) {
    return arg->invoke(arg) == 1;
}

KBoolean callbackReturnLong(CallbackReturnLong* arg) {
    return arg->invoke(arg) == 1;
}

KBoolean callbackReturnFloat(CallbackReturnFloat* arg) {
    return arg->invoke(arg) == 1.1f;
}

KBoolean callbackReturnDouble(CallbackReturnDouble* arg) {
    return arg->invoke(arg) == 1.1;
}

KBoolean callbackReturnString(CallbackReturnString* arg) {
    return strcmp(arg->invoke(arg).data, "test string") == 0;
}

VoidCallback* callbackReturnCallback(CallbackReturnCallback* arg) {
    return arg->invoke(arg);
}

KBoolean callbackReturnEnum(CallbackReturnEnum* arg) {
    return arg->invoke(arg) == MyEnum_CASE2;
}

KBoolean callbackReturnStruct(CallbackReturnStruct* arg) {
    MyStruct result = arg->invoke(arg);
    return result.a == 1 &&
        result.b == 2 &&
        result.c == 3 &&
        result.d == 4;
}