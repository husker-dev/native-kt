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
        arg.a16.is_none() &&
        arg.a17->test() &&
        arg.a18.is_none() &&
        // callback (a17) is skipped
        arg.a20.get_length() == 2 &&
            arg.a20[0] == 'a' &&
            arg.a20[1] == 'b' &&
        arg.a21.get_length() == 2 &&
            arg.a21[0] == true &&
            arg.a21[1] == false &&
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
            arg.a28[0] == 1 &&
            arg.a28[1] == 2 &&
        arg.a29.get_length() == 2 &&
            arg.a29[0] == 1 &&
            arg.a29[1] == 2 &&
        arg.a30.get_length() == 2 &&
            arg.a30[0] == 1.2f &&
            arg.a30[1] == 3.4f &&
        arg.a31.get_length() == 2 &&
            arg.a31[0] == 1.2 &&
            arg.a31[1] == 3.4 &&
        arg.a32.get_length() == 2 &&
            std::string(arg.a32[0].get_data(), arg.a32[0].get_size()) == "string1" &&
            std::string(arg.a32[1].get_data(), arg.a32[1].get_size()) == "string2" &&
        arg.a33.get_length() == 2 &&
            arg.a33[0] == CASE1 &&
            arg.a33[1] == CASE2 &&
        arg.a34.get_length() == 2 &&
            arg.a34[0].a == 1 &&
            arg.a34[0].b == 2 &&
            arg.a34[0].c == 3 &&
            arg.a34[0].d == 4 &&
            arg.a34[1].a == 5 &&
            arg.a34[1].b == 6 &&
            arg.a34[1].c == 7 &&
            arg.a34[1].d == 8 &&
        arg.a35.get_length() == 2 &&
            arg.a35[0]->test() &&
            arg.a35[1]->test()
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
        KOptional<MyDictionary>(),
        inter,
        KOptional<std::shared_ptr<IMyInterface>>(),
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