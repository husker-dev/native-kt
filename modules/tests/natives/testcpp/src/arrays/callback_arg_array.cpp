#include <api.hpp>

bool callback_arg_char_array(std::shared_ptr<CallbackPassCharArray> arg) {
    return arg->invoke(KArray<uint16_t> {
        'a', 'b'
    });
}

bool callback_arg_char_array_n(std::shared_ptr<CallbackPassCharArrayN> arg) {
    return arg->invoke(KOptional<KArray<uint16_t>>());
}

bool callback_arg_boolean_array(std::shared_ptr<CallbackPassBooleanArray> arg) {
    return arg->invoke(KArray {
        true, false
    });
}

bool callback_arg_byte_array(std::shared_ptr<CallbackPassByteArray> arg) {
    return arg->invoke(KArray<int8_t> {
        1, 2
    });
}

bool callback_arg_ubyte_array(std::shared_ptr<CallbackPassUByteArray> arg) {
    return arg->invoke(KArray<uint8_t> {
        1u, 255u
    });
}

bool callback_arg_short_array(std::shared_ptr<CallbackPassShortArray> arg) {
    return arg->invoke(KArray<int16_t> {
        1, 2
    });
}

bool callback_arg_ushort_array(std::shared_ptr<CallbackPassUShortArray> arg) {
    return arg->invoke(KArray<uint16_t> {
        1u, 65535u
    });
}

bool callback_arg_int_array(std::shared_ptr<CallbackPassIntArray> arg) {
    return arg->invoke(KArray {
        1, 2
    });
}

bool callback_arg_uint_array(std::shared_ptr<CallbackPassUIntArray> arg) {
    return arg->invoke(KArray {
        1u, 4294967295u
    });
}

bool callback_arg_long_array(std::shared_ptr<CallbackPassLongArray> arg) {
    return arg->invoke(KArray<int64_t> {
        1, 2
    });
}

bool callback_arg_ulong_array(std::shared_ptr<CallbackPassULongArray> arg) {
    return arg->invoke(KArray<uint64_t> {
        1, 18446744073709551615u
    });
}

bool callback_arg_float_array(std::shared_ptr<CallbackPassFloatArray> arg) {
    return arg->invoke(KArray {
        1.1f, 2.2f
    });
}

bool callback_arg_double_array(std::shared_ptr<CallbackPassDoubleArray> arg) {
    return arg->invoke(KArray {
        1.1, 2.2
    });
}

bool callback_arg_string_array(std::shared_ptr<CallbackPassStringArray> arg) {
    return arg->invoke(KArray {
        KString("string1"),
        KString("string2")
    });
}

bool callback_arg_string_array_n(std::shared_ptr<CallbackPassStringArrayN> arg) {
    return arg->invoke(KArray {
        KOptional<KString>(),
        KOptional<KString>()
    });
}

bool callback_arg_enum_array(std::shared_ptr<CallbackPassEnumArray> arg) {
    return arg->invoke(KArray {
        CASE1, CASE2
    });
}

bool callback_arg_dictionary_array(std::shared_ptr<CallbackPassDictionaryArray> arg) {
    return arg->invoke(KArray {
        MyDictionary(1, 2, 3, 4),
        MyDictionary(5, 6, 7, 8)
    });
}

bool callback_arg_dictionary_array_n(std::shared_ptr<CallbackPassDictionaryArrayN> arg) {
    return arg->invoke(KArray {
        KOptional<MyDictionary>(),
        KOptional<MyDictionary>()
    });
}

bool callback_arg_interface_array(std::shared_ptr<CallbackPassInterfaceArray> arg) {
    return arg->invoke(KArray {
        std::shared_ptr<IMyInterface>(IMyInterface::_create()),
        std::shared_ptr<IMyInterface>(IMyInterface::_create())
    });
}

bool callback_arg_interface_array_n(std::shared_ptr<CallbackPassInterfaceArrayN> arg) {
    return arg->invoke(KArray {
        KOptional<std::shared_ptr<IMyInterface>>(),
        KOptional<std::shared_ptr<IMyInterface>>()
    });
}
