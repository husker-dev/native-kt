#import <nativekt.h>

@implementation NativeKt (PassPrimitive)

+ (BOOL)passVoid {
    return YES;
}

+ (BOOL)passChar:(unichar)arg {
    return arg == 'a';
}

+ (BOOL)passCharN:(NSNumber*)arg {
    return arg == nil || arg.unsignedShortValue == 'a';
}

+ (BOOL)passBoolean:(BOOL)arg {
    return arg == YES;
}

+ (BOOL)passBooleanN:(NSNumber*)arg {
    return arg == nil || arg.boolValue == YES;
}

+ (BOOL)passByte:(int8_t)arg {
    return arg == 1;
}

+ (BOOL)passByteN:(NSNumber*)arg {
    return arg == nil || arg.charValue == 1;
}

+ (BOOL)passUbyte:(uint8_t)arg {
    return arg == UINT8_MAX;
}

+ (BOOL)passUbyteN:(NSNumber*)arg {
    return arg == nil || arg.unsignedCharValue == UINT8_MAX;
}

+ (BOOL)passShort:(int16_t)arg {
    return arg == 1;
}

+ (BOOL)passShortN:(NSNumber*)arg {
    return arg == nil || arg.shortValue == 1;
}

+ (BOOL)passUshort:(uint16_t)arg {
    return arg == UINT16_MAX;
}

+ (BOOL)passUshortN:(NSNumber*)arg {
    return arg == nil || arg.unsignedShortValue == UINT16_MAX;
}

+ (BOOL)passInt:(int32_t)arg {
    return arg == 99;
}

+ (BOOL)passIntN:(NSNumber*)arg {
    return arg == nil || arg.intValue == 99;
}

+ (BOOL)passUint:(uint32_t)arg {
    return arg == UINT32_MAX;
}

+ (BOOL)passUintN:(NSNumber*)arg {
    return arg == nil || arg.unsignedIntValue == UINT32_MAX;
}

+ (BOOL)passLong:(int64_t)arg {
    return arg == INT64_MAX - 2;
}

+ (BOOL)passLongN:(NSNumber*)arg {
    return arg == nil || arg.longLongValue == INT64_MAX - 2;
}

+ (BOOL)passUlong:(uint64_t)arg {
    return arg == UINT64_MAX;
}

+ (BOOL)passUlongN:(NSNumber*)arg {
    return arg == nil || arg.unsignedLongLongValue == UINT64_MAX;
}

+ (BOOL)passFloat:(float)arg {
    return arg == 99.9f;
}

+ (BOOL)passFloatN:(NSNumber*)arg {
    return arg == nil || arg.floatValue == 99.9f;
}

+ (BOOL)passDouble:(double)arg {
    return arg == 1.1;
}

+ (BOOL)passDoubleN:(NSNumber*)arg {
    return arg == nil || arg.doubleValue == 1.1;
}

+ (BOOL)passString:(KTString*)arg {
    return [[arg toNSString] isEqualToString:@"test string"];
}

+ (BOOL)passStringEmpty:(KTString*)arg {
    return arg.size == 0;
}

+ (BOOL)passStringN:(KTString*)arg {
    return arg == nil;
}

+ (BOOL)passEnum:(MyEnum)arg {
    return arg == MyEnum_CASE2;
}

+ (BOOL)passEnumN:(NSNumber*)arg {
    return arg == nil || arg.intValue == MyEnum_CASE2;
}

+ (BOOL)passDictionary:(MyDictionary*)arg {
    return arg.a == 1 && arg.b == 2 && arg.c == 3 && arg.d == 4;
}

+ (BOOL)passDictionaryN:(MyDictionary*)arg {
    return arg == nil;
}

+ (BOOL)passInterface:(MyInterface*)arg {
    return [arg test];
}

+ (BOOL)passInterfaceN:(MyInterface*)arg {
    return arg == nil;
}

@end
