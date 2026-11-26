#import <nativekt.h>

@implementation NativeKt (Jvmci)

+ (BOOL)jvmci1 {
    return YES;
}

+ (BOOL)jvmci2:(int32_t)a1 {
    return a1 == 1;
}

+ (BOOL)jvmci3:(int32_t)a1 a2:(int32_t)a2 {
    return a1 == 1 && a2 == 2;
}

+ (BOOL)jvmci4:(int32_t)a1
            a2:(int32_t)a2
            a3:(int32_t)a3
            a4:(int32_t)a4
            a5:(int32_t)a5
            a6:(int32_t)a6
            a7:(int32_t)a7
            a8:(int32_t)a8
            a9:(int32_t)a9 {
    return a1 == 1 &&
           a2 == 2 &&
           a3 == 3 &&
           a4 == 4 &&
           a5 == 5 &&
           a6 == 6 &&
           a7 == 7 &&
           a8 == 8 &&
           a9 == 9;
}

+ (BOOL)jvmci5:(int32_t)a1
            a2:(int64_t)a2
            a3:(int32_t)a3
            a4:(int64_t)a4
            a5:(int32_t)a5
            a6:(int64_t)a6
            a7:(int32_t)a7
            a8:(int32_t)a8
            a9:(int64_t)a9 {
    return a1 == 1 &&
           a2 == 2 &&
           a3 == 3 &&
           a4 == 4 &&
           a5 == 5 &&
           a6 == 6 &&
           a7 == 7 &&
           a8 == 8 &&
           a9 == 9;
}

+ (BOOL)jvmci6:(float)a1
            a2:(float)a2
            a3:(float)a3
            a4:(float)a4
            a5:(float)a5
            a6:(float)a6
            a7:(float)a7
            a8:(float)a8
            a9:(float)a9
           a10:(int32_t)a10
           a11:(int32_t)a11
           a12:(int32_t)a12
           a13:(int32_t)a13 {
    return a1 == 1 &&
           a2 == 2 &&
           a3 == 3 &&
           a4 == 4 &&
           a5 == 5 &&
           a6 == 6 &&
           a7 == 7 &&
           a8 == 8 &&
           a9 == 9 &&
           a10 == 10 &&
           a11 == 11 &&
           a12 == 12 &&
           a13 == 13;
}

+ (BOOL)jvmci7:(float)a1
            a2:(double)a2
            a3:(float)a3
            a4:(double)a4
            a5:(float)a5
            a6:(double)a6
            a7:(float)a7
            a8:(float)a8
            a9:(double)a9 {
    return a1 == 1 &&
           a2 == 2 &&
           a3 == 3 &&
           a4 == 4 &&
           a5 == 5 &&
           a6 == 6 &&
           a7 == 7 &&
           a8 == 8 &&
           a9 == 9;
}

+ (BOOL)jvmci8:(int32_t)a1 a2:(double)a2 a3:(float)a3 a4:(int64_t)a4 {
    return a1 == 1 && a2 == 2 && a3 == 3 && a4 == 4;
}

+ (BOOL)jvmci9:(int32_t)a1
            a2:(double)a2
            a3:(float)a3
            a4:(int64_t)a4
            a5:(int64_t)a5
            a6:(double)a6
            a7:(float)a7
            a8:(float)a8
            a9:(int32_t)a9 {
    return a1 == 1 &&
           a2 == 2 &&
           a3 == 3 &&
           a4 == 4 &&
           a5 == 5 &&
           a6 == 6 &&
           a7 == 7 &&
           a8 == 8 &&
           a9 == 9;
}

+ (BOOL)jvmci10:(KTString*)a1
             a2:(double)a2
             a3:(float)a3
             a4:(int64_t)a4
             a5:(int64_t)a5
             a6:(double)a6
             a7:(KTString*)a7
             a8:(float)a8
             a9:(int32_t)a9 {
    return [[a1 toNSString] isEqualToString:@"string1"] &&
           a2 == 2 &&
           a3 == 3 &&
           a4 == 4 &&
           a5 == 5 &&
           a6 == 6 &&
           [[a7 toNSString] isEqualToString:@"string7"] &&
           a8 == 8 &&
           a9 == 9;
}

+ (BOOL)jvmci11:(float)a1
             a2:(int32_t)a2
             a3:(float)a3
             a4:(int32_t)a4
             a5:(float)a5
             a6:(int32_t)a6
             a7:(float)a7
             a8:(int32_t)a8
             a9:(float)a9
            a10:(int32_t)a10
            a11:(float)a11
            a12:(int32_t)a12
            a13:(float)a13
            a14:(int32_t)a14
            a15:(float)a15
            a16:(int32_t)a16
            a17:(float)a17 {
    return a1 == 1 &&
           a2 == 2 &&
           a3 == 3 &&
           a4 == 4 &&
           a5 == 5 &&
           a6 == 6 &&
           a7 == 7 &&
           a8 == 8 &&
           a9 == 9 &&
           a10 == 10 &&
           a11 == 11 &&
           a12 == 12 &&
           a13 == 13 &&
           a14 == 14 &&
           a15 == 15 &&
           a16 == 16 &&
           a17 == 17;
}

+ (int32_t)jvmci12 {
    return 1;
}

+ (int64_t)jvmci13 {
    return 1;
}

+ (float)jvmci14 {
    return 1.5f;
}

+ (double)jvmci15 {
    return 1.5;
}

+ (BOOL)jvmciArray:(KTIntArray*)array {
    return [[array toNSArray] isEqualToArray:@[@1, @2, @3]];
}

+ (BOOL)jvmciSomeArrays:(KTIntArray*)array1
                 array2:(KTFloatArray*)array2
                 array3:(KTDoubleArray*)array3 {
    return [[array1 toNSArray] isEqualToArray:@[@1, @2, @3]] &&
           [[array2 toNSArray] isEqualToArray:@[@4.0f, @5.0f, @6.0f]] &&
           [[array3 toNSArray] isEqualToArray:@[@7.0, @8.0, @9.0]];
}

+ (BOOL)jvmciEnum:(MyEnum)enum1 enum2:(MyEnum)enum2 enumArray:(KTIntArray*)enumArray {
    return enum1 == MyEnum_CASE1 &&
           enum2 == MyEnum_CASE2 &&
           [[enumArray toNSArray] isEqualToArray:@[
               @(MyEnum_CASE1), @(MyEnum_CASE2), @(MyEnum_CASE1)
           ]];
}

@end
