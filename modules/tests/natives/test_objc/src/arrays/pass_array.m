#import <nativekt.h>

@implementation NativeKt (PassArray)

+ (BOOL)passCharArray:(KTCharArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[@('a'), @('b')]];
}

+ (BOOL)passBooleanArray:(KTBooleanArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[@YES, @NO]];
}

+ (BOOL)passByteArray:(KTByteArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[@1, @2]];
}

+ (BOOL)passUbyteArray:(KTByteArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[@1, @(-1)]];
}

+ (BOOL)passShortArray:(KTShortArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[@1, @2]];
}

+ (BOOL)passUshortArray:(KTShortArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[@1, @(-1)]];
}

+ (BOOL)passIntArray:(KTIntArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[@1, @2]];
}

+ (BOOL)passUintArray:(KTIntArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[@1, @(-1)]];
}

+ (BOOL)passLongArray:(KTLongArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[@1, @2]];
}

+ (BOOL)passUlongArray:(KTLongArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[@1, @(-1)]];
}

+ (BOOL)passFloatArray:(KTFloatArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[@1.1f, @2.2f]];
}

+ (BOOL)passDoubleArray:(KTDoubleArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[@1.1, @2.2]];
}

+ (BOOL)passEnumArray:(KTIntArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[@(MyEnum_CASE1), @(MyEnum_CASE2)]];
}

+ (BOOL)passCharNArray:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], @('a')]];
}

+ (BOOL)passBooleanNArray:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], @YES]];
}

+ (BOOL)passByteNArray:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], @1]];
}

+ (BOOL)passUbyteNArray:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], @(UINT8_MAX)]];
}

+ (BOOL)passShortNArray:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], @1]];
}

+ (BOOL)passUshortNArray:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], @(UINT16_MAX)]];
}

+ (BOOL)passIntNArray:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], @1]];
}

+ (BOOL)passUintNArray:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], @(UINT32_MAX)]];
}

+ (BOOL)passLongNArray:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], @1]];
}

+ (BOOL)passUlongNArray:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], @(UINT64_MAX)]];
}

+ (BOOL)passFloatNArray:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], @1.1f]];
}

+ (BOOL)passDoubleNArray:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], @1.1]];
}

+ (BOOL)passEnumNArray:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], @(MyEnum_CASE2)]];
}

+ (BOOL)passCharArrayEmpty:(KTCharArray*)arg {
    return arg.length == 0;
}

+ (BOOL)passCharArrayN:(KTCharArray*)arg {
    return arg == nil;
}

+ (BOOL)passStringArray:(KTArray<KTString*>*)arg {
    return [[arg toNSArray] isEqualToArray:@[@"string1", @"string2"]];
}

+ (BOOL)passStringArrayN:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], [NSNull null]]];
}

+ (BOOL)passDictionaryArray:(KTArray<MyDictionary*>*)arg {
    return arg.length == 2 &&
           arg[0].a == 1 &&
           arg[0].b == 2 &&
           arg[0].c == 3 &&
           arg[0].d == 4 &&
           arg[1].a == 5 &&
           arg[1].b == 6 &&
           arg[1].c == 7 &&
           arg[1].d == 8;
}

+ (BOOL)passDictionaryArrayN:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], [NSNull null]]];
}

+ (BOOL)passInterfaceArray:(KTArray<MyInterface*>*)arg {
    return arg.length == 2 && [arg[0] test] && [arg[1] test];
}

+ (BOOL)passInterfaceArrayN:(KTArray*)arg {
    return [[arg toNSArray] isEqualToArray:@[[NSNull null], [NSNull null]]];
}

@end
