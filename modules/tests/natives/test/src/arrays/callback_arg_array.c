#include <api.h>
#include <string.h>

KBoolean callbackArgCharArray(CallbackPassCharArray* arg) {
    return arg->invoke(arg, KCharArray_of('a', 'b'));
}

KBoolean callbackArgCharArrayN(CallbackPassCharArrayN* arg) {
    return arg->invoke(arg, NULL);
}

KBoolean callbackArgBooleanArray(CallbackPassBooleanArray* arg) {
    return arg->invoke(arg, KBooleanArray_of(true, false));
}

KBoolean callbackArgByteArray(CallbackPassByteArray* arg) {
    return arg->invoke(arg, KByteArray_of(1, 2));
}

KBoolean callbackArgShortArray(CallbackPassShortArray* arg) {
    return arg->invoke(arg, KShortArray_of(1, 2));
}

KBoolean callbackArgIntArray(CallbackPassIntArray* arg) {
    return arg->invoke(arg, KIntArray_of(1, 2));
}

KBoolean callbackArgLongArray(CallbackPassLongArray* arg) {
    KLong* elements = malloc(2 * sizeof(KLong));
    elements[0] = 1;
    elements[1] = 2;
    return arg->invoke(arg, KLongArray_new(elements, 2, true));
}

KBoolean callbackArgFloatArray(CallbackPassFloatArray* arg) {
    return arg->invoke(arg, KFloatArray_of(1.1f, 2.2f));
}

KBoolean callbackArgDoubleArray(CallbackPassDoubleArray* arg) {
    return arg->invoke(arg, KDoubleArray_of(1.1, 2.2));
}

KBoolean callbackArgStringArray(CallbackPassStringArray* arg) {
    return arg->invoke(arg, KArray_of(
        KString_new("string1", 7, 7, false),
        KString_new("string2", 7, 7, false)
    ));
}

KBoolean callbackArgStringArrayN(CallbackPassStringArrayN* arg) {
    return arg->invoke(arg, KArray_of(NULL, NULL));
}

KBoolean callbackArgEnumArray(CallbackPassEnumArray* arg) {
    return arg->invoke(arg, KIntArray_of(MyEnum_CASE1, MyEnum_CASE2));
}

KBoolean callbackArgDictionaryArray(CallbackPassDictionaryArray* arg) {
    return arg->invoke(arg, KArray_of(
        MyDictionary_new(1, 2, 3, 4),
        MyDictionary_new(5, 6, 7, 8)
    ));
}

KBoolean callbackArgDictionaryArrayN(CallbackPassDictionaryArrayN* arg) {
    return arg->invoke(arg, KArray_of(NULL, NULL));
}

