#import <nativekt.h>

@implementation NativeKt (ReturnArray)

+ (KTCharArray*)returnCharArray {
    return [[KTCharArray alloc] initWithNSArray:@[@('a'), @('b')]];
}

+ (KTArray*)returnCharNArray {
    return [[KTArray alloc] initWithNSArray:@[[NSNull null], @('a')]];
}

+ (KTBooleanArray*)returnBooleanArray {
    return [[KTBooleanArray alloc] initWithNSArray:@[@YES, @NO]];
}

+ (KTArray*)returnBooleanNArray {
    return [[KTArray alloc] initWithNSArray:@[[NSNull null], @YES]];
}

+ (KTByteArray*)returnByteArray {
    return [[KTByteArray alloc] initWithNSArray:@[@1, @2]];
}

+ (KTArray*)returnByteNArray {
    return [[KTArray alloc] initWithNSArray:@[[NSNull null], @1]];
}

+ (KTByteArray*)returnUbyteArray {
    return [[KTByteArray alloc] initWithNSArray:@[@1, @(-1)]];
}

+ (KTArray*)returnUbyteNArray {
    return [[KTArray alloc] initWithNSArray:@[[NSNull null], @(UINT8_MAX)]];
}

+ (KTShortArray*)returnShortArray {
    return [[KTShortArray alloc] initWithNSArray:@[@1, @2]];
}

+ (KTArray*)returnShortNArray {
    return [[KTArray alloc] initWithNSArray:@[[NSNull null], @1]];
}

+ (KTShortArray*)returnUshortArray {
    return [[KTShortArray alloc] initWithNSArray:@[@1, @(-1)]];
}

+ (KTArray*)returnUshortNArray {
    return [[KTArray alloc] initWithNSArray:@[[NSNull null], @(UINT16_MAX)]];
}

+ (KTIntArray*)returnIntArray {
    return [[KTIntArray alloc] initWithNSArray:@[@1, @2]];
}

+ (KTArray*)returnIntNArray {
    return [[KTArray alloc] initWithNSArray:@[[NSNull null], @1]];
}

+ (KTIntArray*)returnUintArray {
    return [[KTIntArray alloc] initWithNSArray:@[@1, @(-1)]];
}

+ (KTArray*)returnUintNArray {
    return [[KTArray alloc] initWithNSArray:@[[NSNull null], @(UINT32_MAX)]];
}

+ (KTLongArray*)returnLongArray {
    return [[KTLongArray alloc] initWithNSArray:@[@1, @2]];
}

+ (KTArray*)returnLongNArray {
    return [[KTArray alloc] initWithNSArray:@[[NSNull null], @1]];
}

+ (KTLongArray*)returnUlongArray {
    return [[KTLongArray alloc] initWithNSArray:@[@1, @(-1)]];
}

+ (KTArray*)returnUlongNArray {
    return [[KTArray alloc] initWithNSArray:@[[NSNull null], @(UINT64_MAX)]];
}

+ (KTFloatArray*)returnFloatArray {
    return [[KTFloatArray alloc] initWithNSArray:@[@1.1f, @2.2f]];
}

+ (KTArray*)returnFloatNArray {
    return [[KTArray alloc] initWithNSArray:@[[NSNull null], @1.1f]];
}

+ (KTDoubleArray*)returnDoubleArray {
    return [[KTDoubleArray alloc] initWithNSArray:@[@1.1, @2.2]];
}

+ (KTArray*)returnDoubleNArray {
    return [[KTArray alloc] initWithNSArray:@[[NSNull null], @1.1]];
}

+ (KTIntArray*)returnEnumArray {
    return [[KTIntArray alloc] initWithNSArray:@[@(MyEnum_CASE1), @(MyEnum_CASE2)]];
}

+ (KTArray*)returnEnumNArray {
    return [[KTArray alloc] initWithNSArray:@[[NSNull null], @(MyEnum_CASE2)]];
}

+ (KTCharArray*)returnCharArrayEmpty {
    return [[KTCharArray alloc] initWithNSArray:@[]];
}

+ (KTCharArray*)returnCharArrayN {
    return nil;
}

+ (KTArray<KTString*>*)returnStringArray {
    return [[KTArray alloc] initWithNSArray:@[@"string1", @"string2"]];
}

+ (KTArray*)returnStringArrayN {
    return [[KTArray alloc] initWithNSArray:@[[NSNull null], [NSNull null]]];
}

+ (KTArray<MyDictionary*>*)returnDictionaryArray {
    return [[KTArray alloc] initWithNSArray:@[
        [[MyDictionary alloc] initWithA:1 b:2 c:3 d:4],
        [[MyDictionary alloc] initWithA:5 b:6 c:7 d:8]
    ]];
}

+ (KTArray*)returnDictionaryArrayN {
    return [[KTArray alloc] initWithNSArray:@[[NSNull null], [NSNull null]]];
}

@end
