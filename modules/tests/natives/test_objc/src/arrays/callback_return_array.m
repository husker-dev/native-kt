#import <nativekt.h>

@implementation NativeKt (CallbackReturnArray)

+ (BOOL)callbackReturnCharArray:(CallbackReturnCharArray*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[@('a'), @('b')]];
}

+ (BOOL)callbackReturnCharArrayEmpty:(CallbackReturnCharArrayEmpty*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[]];
}

+ (BOOL)callbackReturnCharArrayN:(CallbackReturnCharArrayN*)arg {
    return [arg invoke] == nil;
}

+ (BOOL)callbackReturnBooleanArray:(CallbackReturnBooleanArray*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[@YES, @NO]];
}

+ (BOOL)callbackReturnByteArray:(CallbackReturnByteArray*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[@1, @2]];
}

+ (BOOL)callbackReturnUbyteArray:(CallbackReturnUByteArray*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[@1, @(-1)]];
}

+ (BOOL)callbackReturnShortArray:(CallbackReturnShortArray*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[@1, @2]];
}

+ (BOOL)callbackReturnUshortArray:(CallbackReturnUShortArray*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[@1, @(-1)]];
}

+ (BOOL)callbackReturnIntArray:(CallbackReturnIntArray*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[@1, @2]];
}

+ (BOOL)callbackReturnUintArray:(CallbackReturnUIntArray*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[@1, @(-1)]];
}

+ (BOOL)callbackReturnLongArray:(CallbackReturnLongArray*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[@1, @2]];
}

+ (BOOL)callbackReturnUlongArray:(CallbackReturnULongArray*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[@1, @(-1)]];
}

+ (BOOL)callbackReturnFloatArray:(CallbackReturnFloatArray*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[@1.1f, @2.2f]];
}

+ (BOOL)callbackReturnDoubleArray:(CallbackReturnDoubleArray*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[@1.1, @2.2]];
}

+ (BOOL)callbackReturnStringArray:(CallbackReturnStringArray*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[@"string1", @"string2"]];
}

+ (BOOL)callbackReturnStringArrayN:(CallbackReturnStringArrayN*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[[NSNull null], [NSNull null]]];
}

+ (BOOL)callbackReturnEnumArray:(CallbackReturnEnumArray*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[@(MyEnum_CASE1), @(MyEnum_CASE2)]];
}

+ (BOOL)callbackReturnDictionaryArrayN:(CallbackReturnDictionaryArrayN*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[[NSNull null], [NSNull null]]];
}

+ (BOOL)callbackReturnInterfaceArrayN:(CallbackReturnInterfaceArrayN*)arg {
    return [[[arg invoke] toNSArray] isEqualToArray:@[[NSNull null], [NSNull null]]];
}

+ (BOOL)callbackReturnDictionaryArray:(CallbackReturnDictionaryArray*)arg {
    return [self passDictionaryArray:[arg invoke]];
}

+ (BOOL)callbackReturnInterfaceArray:(CallbackReturnInterfaceArray*)arg {
    return [self passInterfaceArray:[arg invoke]];
}

@end
