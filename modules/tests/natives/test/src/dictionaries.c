#include <api.h>
#include <string.h>

KBoolean passBigDictionary(TypeDictionary* arg) {
    return arg->a1 == 'a' &&
        arg->a2 == true &&
        arg->a3 == 123 &&
        arg->a4 == 123 &&
        arg->a5 == 123 &&
        arg->a6 == 9223372036854775807L &&
        arg->a7 == 123.0f &&
        arg->a8 == 123.4 &&
        strncmp(arg->a9->data, "test string", arg->a9->length) == 0 &&
        arg->a10 == MyEnum_CASE2 &&
        arg->a11->a == 1 && arg->a11->b == 2 && arg->a11->c == 3 && arg->a11->d == 4 &&
        // callback (a12) is skipped
        arg->a13->length == 2 &&
            arg->a13->elements[0] == 'a' &&
            arg->a13->elements[1] == 'b' &&
        arg->a14->length == 2 &&
            arg->a14->elements[0] == true &&
            arg->a14->elements[1] == false &&
        arg->a15->length == 2 &&
            arg->a15->elements[0] == 1 &&
            arg->a15->elements[1] == 2 &&
        arg->a16->length == 2 &&
            arg->a16->elements[0] == 1 &&
            arg->a16->elements[1] == 2 &&
        arg->a17->length == 2 &&
            arg->a17->elements[0] == 1 &&
            arg->a17->elements[1] == 2 &&
        arg->a18->length == 2 &&
            arg->a18->elements[0] == 1 &&
            arg->a18->elements[1] == 2 &&
        arg->a19->length == 2 &&
            arg->a19->elements[0] == 1.2f &&
            arg->a19->elements[1] == 3.4f &&
        arg->a20->length == 2 &&
            arg->a20->elements[0] == 1.2 &&
            arg->a20->elements[1] == 3.4 &&
        arg->a21->length == 2 &&
            strncmp(((KString*)arg->a21->elements[0])->data, "string1", ((KString*)arg->a21->elements[0])->length) == 0 &&
            strncmp(((KString*)arg->a21->elements[1])->data, "string2", ((KString*)arg->a21->elements[1])->length) == 0 &&
        arg->a22->length == 2 &&
            arg->a22->elements[0] == MyEnum_CASE1 &&
            arg->a22->elements[1] == MyEnum_CASE2 &&
        arg->a23->length == 2 &&
            ((MyDictionary**)arg->a23->elements)[0]->a == 1 &&
            ((MyDictionary**)arg->a23->elements)[0]->b == 2 &&
            ((MyDictionary**)arg->a23->elements)[0]->c == 3 &&
            ((MyDictionary**)arg->a23->elements)[0]->d == 4 &&
            ((MyDictionary**)arg->a23->elements)[1]->a == 5 &&
            ((MyDictionary**)arg->a23->elements)[1]->b == 6 &&
            ((MyDictionary**)arg->a23->elements)[1]->c == 7 &&
            ((MyDictionary**)arg->a23->elements)[1]->d == 8;
}

TypeDictionary* returnBigDictionary(VoidCallback* callback) {
    KLong* longElements = malloc(2 * sizeof(KLong));
    longElements[0] = 1;
    longElements[1] = 2;

    return TypeDictionary_new(
        'a',
        true,
        123,
        123,
        123,
        9223372036854775807L,
        123.0f,
        123.4,
        KString_new(strdup("test string"), 11, 11),
        MyEnum_CASE2,
        MyDictionary_new(1, 2, 3, 4),
        callback,
        KCharArray_of('a', 'b'),
        KBooleanArray_of(true, false),
        KByteArray_of(1, 2),
        KShortArray_of(1, 2),
        KIntArray_of(1, 2),
        KLongArray_new(longElements, 2),
        KFloatArray_of(1.2f, 3.4f),
        KDoubleArray_of(1.2, 3.4),
        KArray_of(KString_new(strdup("string1"), 7, 7), KString_new(strdup("string2"), 7, 7)),
        KIntArray_of(MyEnum_CASE1, MyEnum_CASE2),
        KArray_of(MyDictionary_new(1, 2, 3, 4), MyDictionary_new(5, 6, 7, 8))
    );
}

TypeDictionary* pingBigDictionary(TypeDictionary* arg) {
    return arg;
}