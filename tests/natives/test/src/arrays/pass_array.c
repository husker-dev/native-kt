#include <api.h>
#include <stdio.h>

KBoolean passArray(KIntArray arg) {
    return arg.size == 2 &&
        arg.elements[0] == 1 &&
        arg.elements[1] == 2;
}

KBoolean passCharArray(KCharArray arg) {
    return arg.size == 2 &&
        arg.elements[0] == 'a' &&
        arg.elements[1] == 'b';
}

KBoolean passBooleanArray(KBooleanArray arg) {
    return arg.size == 2 &&
        arg.elements[0] == true &&
        arg.elements[1] == false;
}

KBoolean passByteArray(KByteArray arg) {
    return arg.size == 2 &&
        arg.elements[0] == 1 &&
        arg.elements[1] == 2;
}

KBoolean passShortArray(KShortArray arg) {
    return arg.size == 2 &&
        arg.elements[0] == 1 &&
        arg.elements[1] == 2;
}

KBoolean passIntArray(KIntArray arg) {
    return arg.size == 2 &&
        arg.elements[0] == 1 &&
        arg.elements[1] == 2;
}
KBoolean passLongArray(KLongArray arg) {
    return arg.size == 2 &&
        arg.elements[0] == 1 &&
        arg.elements[1] == 2;
}

KBoolean passFloatArray(KFloatArray arg) {
    return arg.size == 2 &&
        arg.elements[0] == 1.1f &&
        arg.elements[1] == 2.2f;
}

KBoolean passDoubleArray(KDoubleArray arg) {
    return arg.size == 2 &&
        arg.elements[0] == 1.1 &&
        arg.elements[1] == 2.2;
}

KBoolean passEnumArray(KArray arg) {
    const MyEnum* elements = (MyEnum*)arg.elements;

    return arg.size == 2 &&
        elements[0] == CASE1 &&
        elements[1] == CASE2;
}