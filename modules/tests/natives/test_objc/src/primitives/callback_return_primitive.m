#import <nativekt.h>

@implementation NativeKt (CallbackReturnPrimitive)

+ (BOOL)callbackReturnChar:(CallbackReturnChar*)arg {
    return [arg invoke] == 'a';
}

+ (BOOL)callbackReturnBoolean:(CallbackReturnBoolean*)arg {
    return [arg invoke] == YES;
}

+ (BOOL)callbackReturnByte:(CallbackReturnByte*)arg {
    return [arg invoke] == 1;
}

+ (BOOL)callbackReturnUbyte:(CallbackReturnUByte*)arg {
    return [arg invoke] == UINT8_MAX;
}

+ (BOOL)callbackReturnShort:(CallbackReturnShort*)arg {
    return [arg invoke] == 1;
}

+ (BOOL)callbackReturnUshort:(CallbackReturnUShort*)arg {
    return [arg invoke] == UINT16_MAX;
}

+ (BOOL)callbackReturnInt:(CallbackReturnInt*)arg {
    return [arg invoke] == 1;
}

+ (BOOL)callbackReturnUint:(CallbackReturnUInt*)arg {
    return [arg invoke] == UINT32_MAX;
}

+ (BOOL)callbackReturnLong:(CallbackReturnLong*)arg {
    return [arg invoke] == 1;
}

+ (BOOL)callbackReturnUlong:(CallbackReturnULong*)arg {
    return [arg invoke] == UINT64_MAX;
}

+ (BOOL)callbackReturnFloat:(CallbackReturnFloat*)arg {
    return [arg invoke] == 1.1f;
}

+ (BOOL)callbackReturnDouble:(CallbackReturnDouble*)arg {
    return [arg invoke] == 1.1;
}

+ (BOOL)callbackReturnStringN:(CallbackReturnStringN*)arg {
    return [arg invoke] == nil;
}

+ (BOOL)callbackReturnCallbackN:(CallbackReturnCallbackN*)arg {
    return [arg invoke] == nil;
}

+ (BOOL)callbackReturnEnum:(CallbackReturnEnum*)arg {
    return [arg invoke] == MyEnum_CASE2;
}

+ (BOOL)callbackReturnDictionaryN:(CallbackReturnDictionaryN*)arg {
    return [arg invoke] == nil;
}

+ (BOOL)callbackReturnInterfaceN:(CallbackReturnInterfaceN*)arg {
    return [arg invoke] == nil;
}

+ (BOOL)callbackReturnString:(CallbackReturnString*)arg {
    return [[[arg invoke] toNSString] isEqualToString:@"test string"];
}

+ (BOOL)callbackReturnStringEmpty:(CallbackReturnStringEmpty*)arg {
    return [arg invoke].size == 0;
}

+ (VoidCallback*)callbackReturnCallback:(CallbackReturnCallback*)arg {
    return [arg invoke];
}

+ (BOOL)callbackReturnDictionary:(CallbackReturnDictionary*)arg {
    MyDictionary* value = [arg invoke];
    return value.a == 1 && value.b == 2 && value.c == 3 && value.d == 4;
}

+ (BOOL)callbackReturnInterface:(CallbackReturnInterface*)arg {
    return [[arg invoke] test];
}

@end
