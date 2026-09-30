#include <api.hpp>

KArray<uint16_t> return_char_array() {
    return KArray<uint16_t> {
        'a', 'b'
    };
}

KArray<uint16_t> return_char_array_empty() {
    return KArray<uint16_t> {};
}

KOptional<KArray<uint16_t>> return_char_array_n() {
    return KOptional<KArray<uint16_t>>();
}

KArray<KOptional<uint16_t>> return_char_narray() {
    return KArray {
        KOptional<uint16_t>(),
        KOptional<uint16_t>('a')
    };
}

KArray<bool> return_boolean_array() {
    return KArray {
        true, false
    };
}

KArray<KOptional<bool>> return_boolean_narray() {
    return KArray {
        KOptional<bool>(),
        KOptional<bool>(true)
    };
}

KArray<int8_t> return_byte_array() {
    return KArray<int8_t> {
        1, 2
    };
}

KArray<KOptional<int8_t>> return_byte_narray() {
    return KArray {
        KOptional<int8_t>(),
        KOptional<int8_t>(1)
    };
}

KArray<uint8_t> return_ubyte_array() {
    return KArray<uint8_t> {
        1u, 255u
    };
}

KArray<KOptional<uint8_t>> return_ubyte_narray() {
    return KArray {
        KOptional<uint8_t>(),
        KOptional<uint8_t>(255u)
    };
}

KArray<int16_t> return_short_array() {
    return KArray<int16_t> {
        1, 2
    };
}

KArray<KOptional<int16_t>> return_short_narray() {
    return KArray {
        KOptional<int16_t>(),
        KOptional<int16_t>(1)
    };
}

KArray<uint16_t> return_ushort_array() {
    return KArray<uint16_t> {
        1u, 65535u
    };
}

KArray<KOptional<uint16_t>> return_ushort_narray() {
    return KArray {
        KOptional<uint16_t>(),
        KOptional<uint16_t>(65535u)
    };
}

KArray<int32_t> return_int_array() {
    return KArray {
        1, 2
    };
}

KArray<KOptional<int32_t>> return_int_narray() {
    return KArray {
        KOptional<int32_t>(),
        KOptional<int32_t>(1)
    };
}

KArray<uint32_t> return_uint_array() {
    return KArray {
        1u, 4294967295u
    };
}

KArray<KOptional<uint32_t>> return_uint_narray() {
    return KArray {
        KOptional<uint32_t>(),
        KOptional<uint32_t>(4294967295u)
    };
}

KArray<int64_t> return_long_array() {
    return KArray<int64_t> {
        1, 2
    };
}

KArray<KOptional<int64_t>> return_long_narray() {
    return KArray {
        KOptional<int64_t>(),
        KOptional<int64_t>(1)
    };
}

KArray<uint64_t> return_ulong_array() {
    return KArray<uint64_t> {
        1u, 18446744073709551615u
    };
}

KArray<KOptional<uint64_t>> return_ulong_narray() {
    return KArray {
        KOptional<uint64_t>(),
        KOptional<uint64_t>(18446744073709551615u)
    };
}

KArray<float> return_float_array() {
    return KArray {
        1.1f, 2.2f
    };
}

KArray<KOptional<float>> return_float_narray() {
    return KArray {
        KOptional<float>(),
        KOptional<float>(1.1f)
    };
}

KArray<double> return_double_array() {
    return KArray {
        1.1, 2.2
    };
}

KArray<KOptional<double>> return_double_narray() {
    return KArray {
        KOptional<double>(),
        KOptional<double>(1.1)
    };
}

KArray<KString> return_string_array() {
    return KArray {
        KString("string1"), KString("string2")
    };
}

KArray<KOptional<KString>> return_string_array_n() {
    return KArray {
        KOptional<KString>(),
        KOptional<KString>()
    };
}

KArray<MyEnum> return_enum_array() {
    return KArray {
        CASE1, CASE2
    };
}

KArray<KOptional<MyEnum>> return_enum_narray() {
    return KArray {
        KOptional<MyEnum>(),
        KOptional<MyEnum>(CASE2)
    };
}

KArray<MyDictionary> return_dictionary_array() {
    return KArray {
        MyDictionary(1, 2, 3, 4),
        MyDictionary(5, 6, 7, 8)
    };
}

KArray<KOptional<MyDictionary>> return_dictionary_array_n() {
    return KArray {
        KOptional<MyDictionary>(),
        KOptional<MyDictionary>()
    };
}