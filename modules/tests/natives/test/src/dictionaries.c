#include <api.h>
#include <string.h>

bool pass_big_dictionary(TypeDictionary* arg) {
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
        arg->a16->pointed == (void*) 1 &&
        // callback (a16) is skipped
        arg->a18->length == 2 &&
            arg->a18->elements[0] == 'a' &&
            arg->a18->elements[1] == 'b' &&
        arg->a19->length == 2 &&
            arg->a19->elements[0] == true &&
            arg->a19->elements[1] == false &&
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
            arg->a27->elements[0] == 1 &&
            arg->a27->elements[1] == 2 &&
        arg->a28->length == 2 &&
            arg->a28->elements[0] == 1.2f &&
            arg->a28->elements[1] == 3.4f &&
        arg->a29->length == 2 &&
            arg->a29->elements[0] == 1.2 &&
            arg->a29->elements[1] == 3.4 &&
        arg->a30->length == 2 &&
            strncmp(((KString*)arg->a30->elements[0])->data, "string1", ((KString*)arg->a30->elements[0])->length) == 0 &&
            strncmp(((KString*)arg->a30->elements[1])->data, "string2", ((KString*)arg->a30->elements[1])->length) == 0 &&
        arg->a31->length == 2 &&
            arg->a31->elements[0] == MyEnum_CASE1 &&
            arg->a31->elements[1] == MyEnum_CASE2 &&
        arg->a32->length == 2 &&
            ((MyDictionary**)arg->a32->elements)[0]->a == 1 &&
            ((MyDictionary**)arg->a32->elements)[0]->b == 2 &&
            ((MyDictionary**)arg->a32->elements)[0]->c == 3 &&
            ((MyDictionary**)arg->a32->elements)[0]->d == 4 &&
            ((MyDictionary**)arg->a32->elements)[1]->a == 5 &&
            ((MyDictionary**)arg->a32->elements)[1]->b == 6 &&
            ((MyDictionary**)arg->a32->elements)[1]->c == 7 &&
            ((MyDictionary**)arg->a32->elements)[1]->d == 8 &&
        arg->a33->length == 2 &&
            ((RC_MyInterface*)arg->a33->elements[0])->pointed == (void*) 1 &&
            ((RC_MyInterface*)arg->a33->elements[1])->pointed == (void*) 1
    ;
}

TypeDictionary* return_big_dictionary(
    RC_VoidCallback* callback,
    RC_MyInterface* inter
) {
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
        kstring_new("test string"),
        MyEnum_CASE2,
        MyDictionary_new(1, 2, 3, 4),
        inter->clone(inter),
        callback->clone(callback),
        kchar_array_of('a', 'b'),
        kboolean_array_of(true, false),
        kbyte_array_of(1, 2),
        kubyte_array_of(1, 2),
        kshort_array_of(1, 2),
        kushort_array_of(1, 2),
        kint_array_of(1, 2),
        kuint_array_of(1, 2),
        klong_array_new((int64_t[]){ 1, 2 }, 2, true),
        kulong_array_new((uint64_t[]){ 1, 2 }, 2, true),
        kfloat_array_of(1.2f, 3.4f),
        kdouble_array_of(1.2, 3.4),
        karray_of(kstring_new("string1"), kstring_new("string2")),
        kint_array_of(MyEnum_CASE1, MyEnum_CASE2),
        karray_of(MyDictionary_new(1, 2, 3, 4), MyDictionary_new(5, 6, 7, 8)),
        karray_of(inter->clone(inter), inter->clone(inter))
    );
}

TypeDictionary* ping_big_dictionary(TypeDictionary* arg) {
    return arg->clone(arg);
}

bool pass_big_dictionary_n(TypeDictionary* arg) {
    return arg == NULL;
}

TypeDictionary* return_big_dictionary_n(void) {
    return NULL;
}

TypeDictionary* ping_big_dictionary_n(TypeDictionary* arg) {
    return arg;
}