#import <nativekt.h>

@implementation NativeKt (CallbackArgArray)

+ (BOOL)callbackArgCharArray:(CallbackPassCharArray*)arg {
    return [arg invoke:[[KTCharArray alloc] initWithNSArray:@[@('a'), @('b')]]];
}

+ (BOOL)callbackArgCharArrayEmpty:(CallbackPassCharArrayEmpty*)arg {
    return [arg invoke:[[KTCharArray alloc] initWithNSArray:@[]]];
}

+ (BOOL)callbackArgCharArrayN:(CallbackPassCharArrayN*)arg {
    return [arg invoke:nil];
}

+ (BOOL)callbackArgBooleanArray:(CallbackPassBooleanArray*)arg {
    return [arg invoke:[[KTBooleanArray alloc] initWithNSArray:@[@YES, @NO]]];
}

+ (BOOL)callbackArgByteArray:(CallbackPassByteArray*)arg {
    return [arg invoke:[[KTByteArray alloc] initWithNSArray:@[@1, @2]]];
}

+ (BOOL)callbackArgUbyteArray:(CallbackPassUByteArray*)arg {
    return [arg invoke:[[KTByteArray alloc] initWithNSArray:@[@1, @(-1)]]];
}

+ (BOOL)callbackArgShortArray:(CallbackPassShortArray*)arg {
    return [arg invoke:[[KTShortArray alloc] initWithNSArray:@[@1, @2]]];
}

+ (BOOL)callbackArgUshortArray:(CallbackPassUShortArray*)arg {
    return [arg invoke:[[KTShortArray alloc] initWithNSArray:@[@1, @(-1)]]];
}

+ (BOOL)callbackArgIntArray:(CallbackPassIntArray*)arg {
    return [arg invoke:[[KTIntArray alloc] initWithNSArray:@[@1, @2]]];
}

+ (BOOL)callbackArgUintArray:(CallbackPassUIntArray*)arg {
    return [arg invoke:[[KTIntArray alloc] initWithNSArray:@[@1, @(-1)]]];
}

+ (BOOL)callbackArgLongArray:(CallbackPassLongArray*)arg {
    return [arg invoke:[[KTLongArray alloc] initWithNSArray:@[@1, @2]]];
}

+ (BOOL)callbackArgUlongArray:(CallbackPassULongArray*)arg {
    return [arg invoke:[[KTLongArray alloc] initWithNSArray:@[@1, @(-1)]]];
}

+ (BOOL)callbackArgFloatArray:(CallbackPassFloatArray*)arg {
    return [arg invoke:[[KTFloatArray alloc] initWithNSArray:@[@1.1f, @2.2f]]];
}

+ (BOOL)callbackArgDoubleArray:(CallbackPassDoubleArray*)arg {
    return [arg invoke:[[KTDoubleArray alloc] initWithNSArray:@[@1.1, @2.2]]];
}

+ (BOOL)callbackArgStringArray:(CallbackPassStringArray*)arg {
    return [arg invoke:[[KTArray alloc] initWithNSArray:@[@"string1", @"string2"]]];
}

+ (BOOL)callbackArgStringArrayN:(CallbackPassStringArrayN*)arg {
    return [arg invoke:[[KTArray alloc] initWithNSArray:@[[NSNull null], [NSNull null]]]];
}

+ (BOOL)callbackArgEnumArray:(CallbackPassEnumArray*)arg {
    return [arg invoke:[[KTIntArray alloc] initWithNSArray:@[@(MyEnum_CASE1), @(MyEnum_CASE2)]]];
}

+ (BOOL)callbackArgDictionaryArray:(CallbackPassDictionaryArray*)arg {
    return [arg invoke:[[KTArray alloc] initWithNSArray:@[
        [[MyDictionary alloc] initWithA:1 b:2 c:3 d:4],
        [[MyDictionary alloc] initWithA:5 b:6 c:7 d:8]
    ]]];
}

+ (BOOL)callbackArgDictionaryArrayN:(CallbackPassDictionaryArrayN*)arg {
    return [arg invoke:[[KTArray alloc] initWithNSArray:@[[NSNull null], [NSNull null]]]];
}

+ (BOOL)callbackArgInterfaceArray:(CallbackPassInterfaceArray*)arg {
    return [arg invoke:[[KTArray alloc] initWithNSArray:@[
        [MyInterface create0],
        [MyInterface create0]
    ]]];
}

+ (BOOL)callbackArgInterfaceArrayN:(CallbackPassInterfaceArrayN*)arg {
    return [arg invoke:[[KTArray alloc] initWithNSArray:@[[NSNull null], [NSNull null]]]];
}

@end
