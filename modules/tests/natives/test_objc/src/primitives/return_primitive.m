#import <nativekt.h>

@implementation NativeKt (ReturnPrimitive)

+ (void)returnVoid {
}

+ (unichar)returnChar {
    return 'a';
}

+ (NSNumber*)returnCharN {
    return nil;
}

+ (BOOL)returnBoolean {
    return YES;
}

+ (NSNumber*)returnBooleanN {
    return @YES;
}

+ (int8_t)returnByte {
    return 99;
}

+ (NSNumber*)returnByteN {
    return nil;
}

+ (uint8_t)returnUbyte {
    return UINT8_MAX;
}

+ (NSNumber*)returnUbyteN {
    return @(UINT8_MAX);
}

+ (int16_t)returnShort {
    return 99;
}

+ (NSNumber*)returnShortN {
    return nil;
}

+ (uint16_t)returnUshort {
    return UINT16_MAX;
}

+ (NSNumber*)returnUshortN {
    return @(UINT16_MAX);
}

+ (int32_t)returnInt {
    return 99;
}

+ (NSNumber*)returnIntN {
    return nil;
}

+ (uint32_t)returnUint {
    return UINT32_MAX;
}

+ (NSNumber*)returnUintN {
    return @(UINT32_MAX);
}

+ (int64_t)returnLong {
    return INT64_MAX - 2;
}

+ (NSNumber*)returnLongN {
    return nil;
}

+ (uint64_t)returnUlong {
    return UINT64_MAX;
}

+ (NSNumber*)returnUlongN {
    return @(UINT64_MAX);
}

+ (float)returnFloat {
    return 99;
}

+ (NSNumber*)returnFloatN {
    return nil;
}

+ (double)returnDouble {
    return 99.0;
}

+ (NSNumber*)returnDoubleN {
    return @99.0;
}

+ (KTString*)returnString {
    return [[KTString alloc] initWithNSString:@"test string"];
}

+ (KTString*)returnStringEmpty {
    return [[KTString alloc] initWithNSString:@""];
}

+ (KTString*)returnStringN {
    return nil;
}

+ (MyEnum)returnEnum {
    return MyEnum_CASE2;
}

+ (NSNumber*)returnEnumN {
    return @(MyEnum_CASE2);
}

+ (MyDictionary*)returnDictionary {
    return [[MyDictionary alloc] initWithA:1 b:2 c:3 d:4];
}

+ (MyDictionary*)returnDictionaryN {
    return nil;
}

@end
