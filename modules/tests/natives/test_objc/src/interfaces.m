#import <nativekt.h>

@implementation MyInterface

+ (instancetype)create0 {
    return [self new];
}

+ (instancetype)newCritical:(int32_t)a {
    (void)a;
    return [self new];
}

- (BOOL)test {
    return YES;
}

- (BOOL)testCritical {
    return YES;
}

- (void)passInterface:(MyInterface*)a1 {
    NSCAssert(a1 != nil, @"Missing interface");
}

- (MyInterface*)returnInterface {
    return self;
}

- (void)snakeCase {
}

@end
