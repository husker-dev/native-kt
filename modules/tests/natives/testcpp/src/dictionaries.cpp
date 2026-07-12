#include <api.hpp>
#include <cstring>

KBoolean pass_big_dictionary(TypeDictionary* arg) {
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
        arg->a14 == CASE2 &&
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
            arg->a30->elements[0] == CASE1 &&
            arg->a30->elements[1] == CASE2 &&
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

TypeDictionary* return_big_dictionary(VoidCallback* callback) {
    return new TypeDictionary(
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
        new KString("test string", 11, 11, false),
        CASE2,
        new MyDictionary(1, 2, 3, 4),
        callback,
        KCharArray::of({'a', 'b'}),
        KBooleanArray::of({true, false}),
        KByteArray::of({1, 2}),
        KUByteArray::of({1, 2}),
        KShortArray::of({1, 2}),
        KUShortArray::of({1, 2}),
        KIntArray::of({1, 2}),
        KUIntArray::of({1, 2}),
        KLongArray::of({1, 2}),
        KULongArray::of({1, 2}),
        KFloatArray::of({1.2f, 3.4f}),
        KDoubleArray::of({1.2, 3.4}),
        KArray::of({
            new KString("string1", 7, 7, false),
            new KString("string2", 7, 7, false)
        }),
        KIntArray::of({CASE1, CASE2}),
        KArray::of({
            new MyDictionary(1, 2, 3, 4),
            new MyDictionary(5, 6, 7, 8)
        })
    );
}

TypeDictionary* ping_big_dictionary(TypeDictionary* arg) {
    return arg;
}

KBoolean pass_big_dictionary_n(TypeDictionary* arg) {
    return arg == nullptr;
}

TypeDictionary* return_big_dictionary_n() {
    return nullptr;
}

TypeDictionary* ping_big_dictionary_n(TypeDictionary* arg) {
    return arg;
}