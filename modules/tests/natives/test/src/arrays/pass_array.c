#include <api.h>

KBoolean passArray(const KIntArray arg) {
    return !arg.releasable &&
        arg.size == 2 &&
        arg.elements[0] == 1 &&
        arg.elements[1] == 2;
}

KBoolean passCharArray(const KCharArray arg) {
    return !arg.releasable &&
        arg.size == 2 &&
        arg.elements[0] == 'a' &&
        arg.elements[1] == 'b';
}

KBoolean passBooleanArray(const KBooleanArray arg) {
    return !arg.releasable &&
        arg.size == 2 &&
        arg.elements[0] == true &&
        arg.elements[1] == false;
}

KBoolean passByteArray(const KByteArray arg) {
    return !arg.releasable &&
        arg.size == 2 &&
        arg.elements[0] == 1 &&
        arg.elements[1] == 2;
}

KBoolean passShortArray(const KShortArray arg) {
    return !arg.releasable &&
        arg.size == 2 &&
        arg.elements[0] == 1 &&
        arg.elements[1] == 2;
}

KBoolean passIntArray(const KIntArray arg) {
    return !arg.releasable &&
        arg.size == 2 &&
        arg.elements[0] == 1 &&
        arg.elements[1] == 2;
}
KBoolean passLongArray(const KLongArray arg) {
    return !arg.releasable &&
        arg.size == 2 &&
        arg.elements[0] == 1 &&
        arg.elements[1] == 2;
}

KBoolean passFloatArray(const KFloatArray arg) {
    return !arg.releasable &&
        arg.size == 2 &&
        arg.elements[0] == 1.1f &&
        arg.elements[1] == 2.2f;
}

KBoolean passDoubleArray(const KDoubleArray arg) {
    return !arg.releasable &&
        arg.size == 2 &&
        arg.elements[0] == 1.1 &&
        arg.elements[1] == 2.2;
}

KBoolean passEnumArray(const KIntArray arg) {
    const MyEnum* elements = (MyEnum*)arg.elements;

    return !arg.releasable &&
        arg.size == 2 &&
        elements[0] == MyEnum_CASE1 &&
        elements[1] == MyEnum_CASE2;
}

KBoolean passDictionaryArray(const KArray arg) {
    MyDictionary** elements = (MyDictionary**)arg.elements;

    return !arg.releasable &&
        arg.size == 2 &&
        elements[0]->a == 1 &&
        elements[0]->b == 2 &&
        elements[0]->c == 3 &&
        elements[0]->d == 4 &&
        elements[1]->a == 5 &&
        elements[1]->b == 6 &&
        elements[1]->c == 7 &&
        elements[1]->d == 8;
}