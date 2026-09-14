#include <api.hpp>

bool callback_return_char_array(const std::shared_ptr<CallbackReturnCharArray>& arg) {
    const KArray<uint16_t> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0] == 'a' &&
        result[1] == 'b';
}

bool callback_return_char_array_n(const std::shared_ptr<CallbackReturnCharArrayN>& arg) {
    return arg->invoke().is_none();
}

bool callback_return_boolean_array(const std::shared_ptr<CallbackReturnBooleanArray>& arg) {
    const KArray<bool> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0] == true &&
        result[1] == false;
}

bool callback_return_byte_array(const std::shared_ptr<CallbackReturnByteArray>& arg) {
    const KArray<int8_t> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0] == 1 &&
        result[1] == 2;
}

bool callback_return_ubyte_array(const std::shared_ptr<CallbackReturnUByteArray>& arg) {
    const KArray<uint8_t> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0] == 1 &&
        result[1] == 255u;
}

bool callback_return_short_array(const std::shared_ptr<CallbackReturnShortArray>& arg) {
    const KArray<int16_t> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0] == 1 &&
        result[1] == 2;
}

bool callback_return_ushort_array(const std::shared_ptr<CallbackReturnUShortArray>& arg) {
    const KArray<uint16_t> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0] == 1 &&
        result[1] == 65535u;
}

bool callback_return_int_array(const std::shared_ptr<CallbackReturnIntArray>& arg) {
    const KArray<int32_t> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0] == 1 &&
        result[1] == 2;
}

bool callback_return_uint_array(const std::shared_ptr<CallbackReturnUIntArray>& arg) {
    const KArray<uint32_t> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0] == 1 &&
        result[1] == 4294967295u;
}

bool callback_return_long_array(const std::shared_ptr<CallbackReturnLongArray>& arg) {
    const KArray<int64_t> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0] == 1 &&
        result[1] == 2;
}

bool callback_return_ulong_array(const std::shared_ptr<CallbackReturnULongArray>& arg) {
    const KArray<uint64_t> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0] == 1 &&
        result[1] == 18446744073709551615u;
}

bool callback_return_float_array(const std::shared_ptr<CallbackReturnFloatArray>& arg) {
    const KArray<float> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0] == 1.1f &&
        result[1] == 2.2f;
}

bool callback_return_double_array(const std::shared_ptr<CallbackReturnDoubleArray>& arg) {
    const KArray<double> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0] == 1.1 &&
        result[1] == 2.2;
}

bool callback_return_string_array(const std::shared_ptr<CallbackReturnStringArray>& arg) {
    const KArray<KString> result = arg->invoke();
    return result.get_length() == 2 &&
        std::string(result[0].get_data(), result[0].get_size()) == "string1" &&
        std::string(result[1].get_data(), result[1].get_size()) == "string2";
}

bool callback_return_string_array_n(const std::shared_ptr<CallbackReturnStringArrayN>& arg) {
    const KArray<KOptional<KString>> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0].is_none() &&
        result[1].is_none();
}

bool callback_return_enum_array(const std::shared_ptr<CallbackReturnEnumArray>& arg) {
    const KArray<MyEnum> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0] == CASE1 &&
        result[1] == CASE2;
}

bool callback_return_dictionary_array(const std::shared_ptr<CallbackReturnDictionaryArray>& arg) {
    const KArray<MyDictionary> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0].a == 1 &&
        result[0].b == 2 &&
        result[0].c == 3 &&
        result[0].d == 4 &&
        result[1].a == 5 &&
        result[1].b == 6 &&
        result[1].c == 7 &&
        result[1].d == 8;
}

bool callback_return_dictionary_array_n(const std::shared_ptr<CallbackReturnDictionaryArrayN>& arg) {
    const KArray<KOptional<MyDictionary>> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0].is_none() &&
        result[1].is_none();
}

bool callback_return_interface_array(const std::shared_ptr<CallbackReturnInterfaceArray>& arg) {
    const KArray<std::shared_ptr<IMyInterface>> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0]->test() &&
        result[1]->test();
}

bool callback_return_interface_array_n(const std::shared_ptr<CallbackReturnInterfaceArrayN>& arg) {
    const KArray<KOptional<std::shared_ptr<IMyInterface>>> result = arg->invoke();
    return result.get_length() == 2 &&
        result[0].is_none() &&
        result[1].is_none();
}
