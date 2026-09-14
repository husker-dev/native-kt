#include <api.hpp>

bool pass_big_dictionary(const TypeDictionary& arg) {
    return arg.a1 == 'a' &&
        arg.a2 == true &&
        arg.a3 == 123 &&
        arg.a4 == 123 &&
        arg.a5 == 123 &&
        arg.a6 == 123 &&
        arg.a7 == 123 &&
        arg.a8 == 123 &&
        arg.a9 == 9223372036854775807L &&
        arg.a10 == 9223372036854775807L &&
        arg.a11 == 123.0f &&
        arg.a12 == 123.4 &&
        std::string(arg.a13.get_data(), arg.a13.get_size()) == "test string" &&
        arg.a14 == CASE2 &&
        arg.a15.a == 1 && arg.a15.b == 2 && arg.a15.c == 3 && arg.a15.d == 4 &&
        arg.a16->test() &&
        // callback (a17) is skipped
        arg.a18.get_length() == 2 &&
            arg.a18[0] == 'a' &&
            arg.a18[1] == 'b' &&
        arg.a19.get_length() == 2 &&
            arg.a19[0] == true &&
            arg.a19[1] == false &&
        arg.a20.get_length() == 2 &&
            arg.a20[0] == 1 &&
            arg.a20[1] == 2 &&
        arg.a21.get_length() == 2 &&
            arg.a21[0] == 1 &&
            arg.a21[1] == 2 &&
        arg.a22.get_length() == 2 &&
            arg.a22[0] == 1 &&
            arg.a22[1] == 2 &&
        arg.a23.get_length() == 2 &&
            arg.a23[0] == 1 &&
            arg.a23[1] == 2 &&
        arg.a24.get_length() == 2 &&
            arg.a24[0] == 1 &&
            arg.a24[1] == 2 &&
        arg.a25.get_length() == 2 &&
            arg.a25[0] == 1 &&
            arg.a25[1] == 2 &&
        arg.a26.get_length() == 2 &&
            arg.a26[0] == 1 &&
            arg.a26[1] == 2 &&
        arg.a27.get_length() == 2 &&
            arg.a27[0] == 1 &&
            arg.a27[1] == 2 &&
        arg.a28.get_length() == 2 &&
            arg.a28[0] == 1.2f &&
            arg.a28[1] == 3.4f &&
        arg.a29.get_length() == 2 &&
            arg.a29[0] == 1.2 &&
            arg.a29[1] == 3.4 &&
        arg.a30.get_length() == 2 &&
            std::string(arg.a30[0].get_data(), arg.a30[0].get_size()) == "string1" &&
            std::string(arg.a30[1].get_data(), arg.a30[1].get_size()) == "string2" &&
        arg.a31.get_length() == 2 &&
            arg.a31[0] == CASE1 &&
            arg.a31[1] == CASE2 &&
        arg.a32.get_length() == 2 &&
            arg.a32[0].a == 1 &&
            arg.a32[0].b == 2 &&
            arg.a32[0].c == 3 &&
            arg.a32[0].d == 4 &&
            arg.a32[1].a == 5 &&
            arg.a32[1].b == 6 &&
            arg.a32[1].c == 7 &&
            arg.a32[1].d == 8 &&
        arg.a33.get_length() == 2 &&
            arg.a33[0]->test() &&
            arg.a33[1]->test()
    ;
}

TypeDictionary return_big_dictionary(
    const std::shared_ptr<VoidCallback>& callback,
    const std::shared_ptr<IMyInterface>& inter
) {
    return TypeDictionary(
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
        KString("test string"),
        CASE2,
        MyDictionary(1, 2, 3, 4),
        inter,
        callback,
        KArray<uint16_t>{'a', 'b'},
        KArray{true, false},
        KArray<int8_t>{1, 2},
        KArray<uint8_t>{1, 2},
        KArray<int16_t>{1, 2},
        KArray<uint16_t>{1, 2},
        KArray{1, 2},
        KArray{1u, 2u},
        KArray{(int64_t) 1ll, (int64_t) 2ll},
        KArray{(uint64_t) 1ull, (uint64_t) 2ull},
        KArray{1.2f, 3.4f},
        KArray{1.2, 3.4},
        KArray {
            KString("string1"),
            KString("string2")
        },
        KArray{CASE1, CASE2},
        KArray{MyDictionary(1, 2, 3, 4), MyDictionary(5, 6, 7, 8)},
        KArray{inter, inter}
    );
}

TypeDictionary ping_big_dictionary(const TypeDictionary& arg) {
    return arg;
}

bool pass_big_dictionary_n(const KOptional<TypeDictionary>& arg) {
    return arg.is_none();
}

KOptional<TypeDictionary> return_big_dictionary_n() {
    return KOptional<TypeDictionary>();
}

KOptional<TypeDictionary> ping_big_dictionary_n(const KOptional<TypeDictionary>& arg) {
    return arg;
}