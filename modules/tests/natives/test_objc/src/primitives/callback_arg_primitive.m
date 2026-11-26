#import <nativekt.h>

@implementation NativeKt (CallbackArgPrimitive)

+ (void)callbackVoid:(VoidCallback*)arg {
    [arg invoke];
}

+ (BOOL)callbackVoidN:(VoidCallback*)arg {
    return arg == nil;
}

+ (BOOL)callbackArgChar:(CallbackPassChar*)arg {
    return [arg invoke:'a'];
}

+ (BOOL)callbackArgBoolean:(CallbackPassBoolean*)arg {
    return [arg invoke:YES];
}

+ (BOOL)callbackArgByte:(CallbackPassByte*)arg {
    return [arg invoke:1];
}

+ (BOOL)callbackArgUbyte:(CallbackPassUByte*)arg {
    return [arg invoke:UINT8_MAX];
}

+ (BOOL)callbackArgShort:(CallbackPassShort*)arg {
    return [arg invoke:1];
}

+ (BOOL)callbackArgUshort:(CallbackPassUShort*)arg {
    return [arg invoke:UINT16_MAX];
}

+ (BOOL)callbackArgInt:(CallbackPassInt*)arg {
    return [arg invoke:1];
}

+ (BOOL)callbackArgUint:(CallbackPassUInt*)arg {
    return [arg invoke:UINT32_MAX];
}

+ (BOOL)callbackArgLong:(CallbackPassLong*)arg {
    return [arg invoke:1];
}

+ (BOOL)callbackArgUlong:(CallbackPassULong*)arg {
    return [arg invoke:UINT64_MAX];
}

+ (BOOL)callbackArgFloat:(CallbackPassFloat*)arg {
    return [arg invoke:1.1f];
}

+ (BOOL)callbackArgDouble:(CallbackPassDouble*)arg {
    return [arg invoke:1.1];
}

+ (BOOL)callbackArgString:(CallbackPassString*)arg {
    return [arg invoke:[[KTString alloc] initWithNSString:@"test string"]];
}

+ (BOOL)callbackArgStringEmpty:(CallbackPassStringEmpty*)arg {
    return [arg invoke:[[KTString alloc] initWithNSString:@""]];
}

+ (BOOL)callbackArgStringN:(CallbackPassStringN*)arg {
    return [arg invoke:nil];
}

+ (BOOL)callbackArgCallbackN:(CallbackPassCallbackN*)arg {
    return [arg invoke:nil];
}

+ (BOOL)callbackArgEnum:(CallbackPassEnum*)arg {
    return [arg invoke:MyEnum_CASE2];
}

+ (BOOL)callbackArgDictionary:(CallbackPassDictionary*)arg {
    return [arg invoke:[[MyDictionary alloc] initWithA:1 b:2 c:3 d:4]];
}

+ (BOOL)callbackArgDictionaryN:(CallbackPassDictionaryN*)arg {
    return [arg invoke:nil];
}

+ (BOOL)callbackArgInterfaceN:(CallbackPassInterfaceN*)arg {
    return [arg invoke:nil];
}

+ (BOOL)callbackArgCallback:(VoidCallback*)pass arg:(CallbackPassCallback*)arg {
    return [arg invoke:pass];
}

+ (BOOL)callbackArgInterface:(MyInterface*)pass arg:(CallbackPassInterface*)arg {
    return [arg invoke:pass];
}

@end
