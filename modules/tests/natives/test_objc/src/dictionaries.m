#import <nativekt.h>

@implementation NativeKt (Dictionaries)

+ (BOOL)passBigDictionary:(TypeDictionary*)arg {
    return arg.a1 == 'a' &&
           arg.a2 == YES &&
           arg.a3 == 123 &&
           arg.a4 == 123 &&
           arg.a5 == 123 &&
           arg.a6 == 123 &&
           arg.a7 == 123 &&
           arg.a8 == 123 &&
           arg.a9 == INT64_MAX &&
           arg.a10 == INT64_MAX &&
           arg.a11 == 123.0f &&
           arg.a12 == 123.4 &&
           [[arg.a13 toNSString] isEqualToString:@"test string"] &&
           arg.a14 == MyEnum_CASE2 &&
           [self passDictionary:arg.a15] &&
           arg.a16 == nil &&
           [arg.a17 test] &&
           arg.a18 == nil &&
           [[arg.a20 toNSArray] isEqualToArray:@[@('a'), @('b')]] &&
           [[arg.a21 toNSArray] isEqualToArray:@[@YES, @NO]] &&
           [[arg.a22 toNSArray] isEqualToArray:@[@1, @2]] &&
           [[arg.a23 toNSArray] isEqualToArray:@[@1, @2]] &&
           [[arg.a24 toNSArray] isEqualToArray:@[@1, @2]] &&
           [[arg.a25 toNSArray] isEqualToArray:@[@1, @2]] &&
           [[arg.a26 toNSArray] isEqualToArray:@[@1, @2]] &&
           [[arg.a27 toNSArray] isEqualToArray:@[@1, @2]] &&
           [[arg.a28 toNSArray] isEqualToArray:@[@1, @2]] &&
           [[arg.a29 toNSArray] isEqualToArray:@[@1, @2]] &&
           [[arg.a30 toNSArray] isEqualToArray:@[@1.2f, @3.4f]] &&
           [[arg.a31 toNSArray] isEqualToArray:@[@1.2, @3.4]] &&
           [self passStringArray:arg.a32] &&
           [self passEnumArray:arg.a33] &&
           [self passDictionaryArray:arg.a34] &&
           [self passInterfaceArray:arg.a35];
}

+ (TypeDictionary*)returnBigDictionary:(VoidCallback*)callback inter:(MyInterface*)inter {
    TypeDictionary* value = [TypeDictionary new];
    value.a1 = 'a';
    value.a2 = YES;
    value.a3 = 123;
    value.a4 = 123;
    value.a5 = 123;
    value.a6 = 123;
    value.a7 = 123;
    value.a8 = 123;
    value.a9 = INT64_MAX;
    value.a10 = INT64_MAX;
    value.a11 = 123.0f;
    value.a12 = 123.4;
    value.a13 = [[KTString alloc] initWithNSString:@"test string"];
    value.a14 = MyEnum_CASE2;
    value.a15 = [self returnDictionary];
    value.a16 = nil;
    value.a17 = inter;
    value.a18 = nil;
    value.a19 = callback;
    value.a20 = [[KTCharArray alloc] initWithNSArray:@[@('a'), @('b')]];
    value.a21 = [[KTBooleanArray alloc] initWithNSArray:@[@YES, @NO]];
    value.a22 = [[KTByteArray alloc] initWithNSArray:@[@1, @2]];
    value.a23 = [[KTByteArray alloc] initWithNSArray:@[@1, @2]];
    value.a24 = [[KTShortArray alloc] initWithNSArray:@[@1, @2]];
    value.a25 = [[KTShortArray alloc] initWithNSArray:@[@1, @2]];
    value.a26 = [[KTIntArray alloc] initWithNSArray:@[@1, @2]];
    value.a27 = [[KTIntArray alloc] initWithNSArray:@[@1, @2]];
    value.a28 = [[KTLongArray alloc] initWithNSArray:@[@1, @2]];
    value.a29 = [[KTLongArray alloc] initWithNSArray:@[@1, @2]];
    value.a30 = [[KTFloatArray alloc] initWithNSArray:@[@1.2f, @3.4f]];
    value.a31 = [[KTDoubleArray alloc] initWithNSArray:@[@1.2, @3.4]];
    value.a32 = [self returnStringArray];
    value.a33 = [self returnEnumArray];
    value.a34 = [self returnDictionaryArray];
    value.a35 = [[KTArray alloc] initWithNSArray:@[inter, inter]];
    return value;
}

+ (TypeDictionary*)pingBigDictionary:(TypeDictionary*)arg {
    return arg;
}

+ (BOOL)passBigDictionaryN:(TypeDictionary*)arg {
    return arg == nil;
}

+ (TypeDictionary*)returnBigDictionaryN {
    return nil;
}

+ (TypeDictionary*)pingBigDictionaryN:(TypeDictionary*)arg {
    return arg;
}

@end
