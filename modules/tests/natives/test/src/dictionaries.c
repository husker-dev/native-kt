#include <api.h>
#include <string.h>

KBoolean passBigDictionary(TypeDictionary* arg) {
    return arg->a1 == 'a' &&
        arg->a2 == true &&
        arg->a3 == 123 &&
        arg->a4 == 123 &&
        arg->a5 == 123 &&
        arg->a6 == 123 &&
        arg->a7 == 123 &&
        arg->a8 == 123 &&
        arg->a9 == 9223372036854775807L &&
        arg->a10 == 9223372036854775807L &&
        arg->a11 == 123.0f &&
        arg->a12 == 123.4 &&
        strncmp(arg->a13->data, "test string", arg->a13->length) == 0 &&
        arg->a14 == MyEnum_CASE2 &&
        arg->a15->a == 1 && arg->a15->b == 2 && arg->a15->c == 3 && arg->a15->d == 4 &&
        // callback (a16) is skipped
        arg->a17->length == 2 &&
            arg->a17->elements[0] == 'a' &&
            arg->a17->elements[1] == 'b' &&
        arg->a18->length == 2 &&
            arg->a18->elements[0] == true &&
            arg->a18->elements[1] == false &&
        arg->a19->length == 2 &&
            arg->a19->elements[0] == 1 &&
            arg->a19->elements[1] == 2 &&
        arg->a20->length == 2 &&
            arg->a20->elements[0] == 1 &&
            arg->a20->elements[1] == 2 &&
        arg->a21->length == 2 &&
            arg->a21->elements[0] == 1 &&
            arg->a21->elements[1] == 2 &&
        arg->a22->length == 2 &&
            arg->a22->elements[0] == 1 &&
            arg->a22->elements[1] == 2 &&
        arg->a23->length == 2 &&
            arg->a23->elements[0] == 1 &&
            arg->a23->elements[1] == 2 &&
        arg->a24->length == 2 &&
            arg->a24->elements[0] == 1 &&
            arg->a24->elements[1] == 2 &&
        arg->a25->length == 2 &&
            arg->a25->elements[0] == 1 &&
            arg->a25->elements[1] == 2 &&
        arg->a26->length == 2 &&
            arg->a26->elements[0] == 1 &&
            arg->a26->elements[1] == 2 &&
        arg->a27->length == 2 &&
            arg->a27->elements[0] == 1.2f &&
            arg->a27->elements[1] == 3.4f &&
        arg->a28->length == 2 &&
            arg->a28->elements[0] == 1.2 &&
            arg->a28->elements[1] == 3.4 &&
        arg->a29->length == 2 &&
            strncmp(((KString*)arg->a29->elements[0])->data, "string1", ((KString*)arg->a29->elements[0])->length) == 0 &&
            strncmp(((KString*)arg->a29->elements[1])->data, "string2", ((KString*)arg->a29->elements[1])->length) == 0 &&
        arg->a30->length == 2 &&
            arg->a30->elements[0] == MyEnum_CASE1 &&
            arg->a30->elements[1] == MyEnum_CASE2 &&
        arg->a31->length == 2 &&
            ((MyDictionary**)arg->a31->elements)[0]->a == 1 &&
            ((MyDictionary**)arg->a31->elements)[0]->b == 2 &&
            ((MyDictionary**)arg->a31->elements)[0]->c == 3 &&
            ((MyDictionary**)arg->a31->elements)[0]->d == 4 &&
            ((MyDictionary**)arg->a31->elements)[1]->a == 5 &&
            ((MyDictionary**)arg->a31->elements)[1]->b == 6 &&
            ((MyDictionary**)arg->a31->elements)[1]->c == 7 &&
            ((MyDictionary**)arg->a31->elements)[1]->d == 8;
}

TypeDictionary* returnBigDictionary(VoidCallback* callback) {
    KLong* longElements = malloc(2 * sizeof(KLong));
    longElements[0] = 1;
    longElements[1] = 2;

    KULong* ulongElements = malloc(2 * sizeof(KULong));
    ulongElements[0] = 1;
    ulongElements[1] = 2;

    return TypeDictionary_new(
        'a',
        true,
        123,
        123,
        123,
        123,
        123,
        123,
        9223372036854775807L,
        9223372036854775807L,
        123.0f,
        123.4,
        KString_new("test string", 11, 11, false),
        MyEnum_CASE2,
        MyDictionary_new(1, 2, 3, 4),
        callback,
        KCharArray_of('a', 'b'),
        KBooleanArray_of(true, false),
        KByteArray_of(1, 2),
        KUByteArray_of(1, 2),
        KShortArray_of(1, 2),
        KUShortArray_of(1, 2),
        KIntArray_of(1, 2),
        KUIntArray_of(1, 2),
        KLongArray_new(longElements, 2, true),
        KULongArray_new(ulongElements, 2, true),
        KFloatArray_of(1.2f, 3.4f),
        KDoubleArray_of(1.2, 3.4),
        KArray_of(KString_new("string1", 7, 7, false), KString_new("string2", 7, 7, false)),
        KIntArray_of(MyEnum_CASE1, MyEnum_CASE2),
        KArray_of(MyDictionary_new(1, 2, 3, 4), MyDictionary_new(5, 6, 7, 8))
    );
}

TypeDictionary* pingBigDictionary(TypeDictionary* arg) {
    return arg;
}

KBoolean passBigDictionaryN(TypeDictionary* arg) {
    return arg == NULL;
}

TypeDictionary* returnBigDictionaryN() {
    return NULL;
}

TypeDictionary* pingBigDictionaryN(TypeDictionary* arg) {
    return arg;
}