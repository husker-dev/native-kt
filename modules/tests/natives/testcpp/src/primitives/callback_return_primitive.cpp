#include <api.hpp>

bool callback_return_char(std::shared_ptr<CallbackReturnChar> arg) {
    return arg->invoke() == 'a';
}

bool callback_return_boolean(std::shared_ptr<CallbackReturnBoolean> arg) {
    return arg->invoke() == true;
}

bool callback_return_byte(std::shared_ptr<CallbackReturnByte> arg) {
    return arg->invoke() == 1;
}

bool callback_return_ubyte(std::shared_ptr<CallbackReturnUByte> arg) {
    return arg->invoke() == 255u;
}

bool callback_return_short(std::shared_ptr<CallbackReturnShort> arg) {
    return arg->invoke() == 1;
}

bool callback_return_ushort(std::shared_ptr<CallbackReturnUShort> arg) {
    return arg->invoke() == 65535u;
}

bool callback_return_int(std::shared_ptr<CallbackReturnInt> arg) {
    return arg->invoke() == 1;
}

bool callback_return_uint(std::shared_ptr<CallbackReturnUInt> arg) {
    return arg->invoke() == 4294967295u;
}

bool callback_return_long(std::shared_ptr<CallbackReturnLong> arg) {
    return arg->invoke() == 1;
}

bool callback_return_ulong(std::shared_ptr<CallbackReturnULong> arg) {
    return arg->invoke() == 18446744073709551615u;
}

bool callback_return_float(std::shared_ptr<CallbackReturnFloat> arg) {
    return arg->invoke() == 1.1f;
}

bool callback_return_double(std::shared_ptr<CallbackReturnDouble> arg) {
    return arg->invoke() == 1.1;
}

bool callback_return_string(std::shared_ptr<CallbackReturnString> arg) {
    const KString result = arg->invoke();
    return std::string(result.get_data(), result.get_size()) == "test string";
}

bool callback_return_string_n(std::shared_ptr<CallbackReturnStringN> arg) {
    return arg->invoke().is_none();
}

std::shared_ptr<VoidCallback> callback_return_callback(std::shared_ptr<CallbackReturnCallback> arg) {
    return arg->invoke();
}

bool callback_return_callback_n(std::shared_ptr<CallbackReturnCallbackN> arg) {
    return arg->invoke().is_none();
}

bool callback_return_enum(std::shared_ptr<CallbackReturnEnum> arg) {
    return arg->invoke() == CASE2;
}

bool callback_return_dictionary(std::shared_ptr<CallbackReturnDictionary> arg) {
    const MyDictionary result = arg->invoke();
    return result.a == 1 &&
        result.b == 2 &&
        result.c == 3 &&
        result.d == 4;
}

bool callback_return_dictionary_n(std::shared_ptr<CallbackReturnDictionaryN> arg) {
    return arg->invoke().is_none();
}

bool callback_return_interface(std::shared_ptr<CallbackReturnInterface> arg) {
    return arg->invoke()->test();
}

bool callback_return_interface_n(std::shared_ptr<CallbackReturnInterfaceN> arg) {
    return arg->invoke().is_none();
}