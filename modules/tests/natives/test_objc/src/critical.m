#import <nativekt.h>

@implementation NativeKt (Critical)

+ (BOOL)criticalPrimitives:(unichar)a1
                        a2:(BOOL)a2
                        a3:(int8_t)a3
                        a4:(uint8_t)a4
                        a5:(int16_t)a5
                        a6:(uint16_t)a6
                        a7:(int32_t)a7
                        a8:(uint32_t)a8
                        a9:(int64_t)a9
                       a10:(uint64_t)a10
                       a11:(float)a11
                       a12:(double)a12 {
    return a1 == 'a' &&
           a2 &&
           a3 == 1 &&
           a4 == UINT8_MAX &&
           a5 == 3 &&
           a6 == UINT16_MAX &&
           a7 == 5 &&
           a8 == UINT32_MAX &&
           a9 == 7 &&
           a10 == UINT64_MAX &&
           a11 == 1.0f &&
           a12 == 2.0;
}

+ (BOOL)criticalEnum:(MyEnum)a1 {
    return a1 == MyEnum_CASE1;
}

+ (BOOL)criticalString:(KTString*)a1 {
    return [[a1 toNSString] isEqualToString:@"test string"];
}

+ (BOOL)criticalStringEmpty:(KTString*)a1 {
    return a1.size == 0;
}

+ (BOOL)criticalStringN:(KTString*)a1 {
    return a1 == nil;
}

+ (BOOL)criticalInterface:(MyInterface*)a1 {
    return [a1 test];
}

+ (BOOL)criticalInterfaceN:(MyInterface*)a1 {
    return a1 == nil;
}

+ (BOOL)criticalPrimitivesArray:(KTCharArray*)a1
                             a2:(KTBooleanArray*)a2
                             a3:(KTByteArray*)a3
                             a4:(KTByteArray*)a4
                             a5:(KTShortArray*)a5
                             a6:(KTShortArray*)a6
                             a7:(KTIntArray*)a7
                             a8:(KTIntArray*)a8
                             a9:(KTLongArray*)a9
                            a10:(KTLongArray*)a10
                            a11:(KTFloatArray*)a11
                            a12:(KTDoubleArray*)a12 {
    return [[a1 toNSArray] isEqualToArray:@[@('a'), @('b')]] &&
           [[a2 toNSArray] isEqualToArray:@[@YES, @NO]] &&
           [[a3 toNSArray] isEqualToArray:@[@1, @2]] &&
           [[a4 toNSArray] isEqualToArray:@[@1, @(-1)]] &&
           [[a5 toNSArray] isEqualToArray:@[@1, @2]] &&
           [[a6 toNSArray] isEqualToArray:@[@1, @(-1)]] &&
           [[a7 toNSArray] isEqualToArray:@[@1, @2]] &&
           [[a8 toNSArray] isEqualToArray:@[@1, @(-1)]] &&
           [[a9 toNSArray] isEqualToArray:@[@1, @2]] &&
           [[a10 toNSArray] isEqualToArray:@[@1, @(-1)]] &&
           [[a11 toNSArray] isEqualToArray:@[@1.1f, @2.2f]] &&
           [[a12 toNSArray] isEqualToArray:@[@1.1, @2.2]];
}

+ (BOOL)criticalPrimitivesArrayEmpty:(KTCharArray*)a1
                                  a2:(KTBooleanArray*)a2
                                  a3:(KTByteArray*)a3
                                  a4:(KTByteArray*)a4
                                  a5:(KTShortArray*)a5
                                  a6:(KTShortArray*)a6
                                  a7:(KTIntArray*)a7
                                  a8:(KTIntArray*)a8
                                  a9:(KTLongArray*)a9
                                 a10:(KTLongArray*)a10
                                 a11:(KTFloatArray*)a11
                                 a12:(KTDoubleArray*)a12 {
    return a1.length == 0 &&
           a2.length == 0 &&
           a3.length == 0 &&
           a4.length == 0 &&
           a5.length == 0 &&
           a6.length == 0 &&
           a7.length == 0 &&
           a8.length == 0 &&
           a9.length == 0 &&
           a10.length == 0 &&
           a11.length == 0 &&
           a12.length == 0;
}

+ (BOOL)criticalPrimitivesArrayN:(KTCharArray*)a1
                              a2:(KTBooleanArray*)a2
                              a3:(KTByteArray*)a3
                              a4:(KTByteArray*)a4
                              a5:(KTShortArray*)a5
                              a6:(KTShortArray*)a6
                              a7:(KTIntArray*)a7
                              a8:(KTIntArray*)a8
                              a9:(KTLongArray*)a9
                             a10:(KTLongArray*)a10
                             a11:(KTFloatArray*)a11
                             a12:(KTDoubleArray*)a12 {
    return a1 == nil &&
           a2 == nil &&
           a3 == nil &&
           a4 == nil &&
           a5 == nil &&
           a6 == nil &&
           a7 == nil &&
           a8 == nil &&
           a9 == nil &&
           a10 == nil &&
           a11 == nil &&
           a12 == nil;
}

+ (BOOL)criticalEnumArray:(KTIntArray*)a1 {
    return [[a1 toNSArray] isEqualToArray:@[@(MyEnum_CASE1), @(MyEnum_CASE2)]];
}

+ (BOOL)criticalEnumArrayEmpty:(KTIntArray*)a1 {
    return a1.length == 0;
}

+ (BOOL)criticalEnumArrayN:(KTIntArray*)a1 {
    return a1 == nil;
}

+ (unichar)criticalReturnChar {
    return 'a';
}

+ (BOOL)criticalReturnBoolean {
    return YES;
}

+ (int8_t)criticalReturnByte {
    return 1;
}

+ (uint8_t)criticalReturnUbyte {
    return UINT8_MAX;
}

+ (int16_t)criticalReturnShort {
    return 1;
}

+ (uint16_t)criticalReturnUshort {
    return UINT16_MAX;
}

+ (int32_t)criticalReturnInt {
    return 1;
}

+ (uint32_t)criticalReturnUint {
    return UINT32_MAX;
}

+ (int64_t)criticalReturnLong {
    return 1;
}

+ (uint64_t)criticalReturnUlong {
    return UINT64_MAX;
}

+ (float)criticalReturnFloat {
    return 1.0f;
}

+ (double)criticalReturnDouble {
    return 1.0;
}

+ (MyEnum)criticalReturnEnum {
    return MyEnum_CASE1;
}

+ (MyInterface*)criticalReturnInterface {
    return [MyInterface create0];
}

@end
