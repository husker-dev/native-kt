#include <api.hpp>

// Consume

void callback_void(const std::shared_ptr<VoidCallback>& arg) {
    arg->invoke();
}

bool callback_void_n(const KOptional<std::shared_ptr<VoidCallback>>& arg) {
    return arg.is_none();
}

bool callback_arg_char(const std::shared_ptr<CallbackPassChar>& arg) {
    return arg->invoke('a');
}

bool callback_arg_boolean(const std::shared_ptr<CallbackPassBoolean>& arg) {
    return arg->invoke(true);
}

bool callback_arg_byte(const std::shared_ptr<CallbackPassByte>& arg) {
    return arg->invoke(1);
}

bool callback_arg_ubyte(const std::shared_ptr<CallbackPassUByte>& arg) {
    return arg->invoke(255u);
}

bool callback_arg_short(const std::shared_ptr<CallbackPassShort>& arg) {
    return arg->invoke(1);
}

bool callback_arg_ushort(const std::shared_ptr<CallbackPassUShort>& arg) {
    return arg->invoke(65535u);
}

bool callback_arg_int(const std::shared_ptr<CallbackPassInt>& arg) {
    return arg->invoke(1);
}

bool callback_arg_uint(const std::shared_ptr<CallbackPassUInt>& arg) {
    return arg->invoke(4294967295u);
}

bool callback_arg_long(const std::shared_ptr<CallbackPassLong>& arg) {
    return arg->invoke(1);
}

bool callback_arg_ulong(const std::shared_ptr<CallbackPassULong>& arg) {
    return arg->invoke(18446744073709551615u);
}

bool callback_arg_float(const std::shared_ptr<CallbackPassFloat>& arg) {
    return arg->invoke(1.1f);
}

bool callback_arg_double(const std::shared_ptr<CallbackPassDouble>& arg) {
    return arg->invoke(1.1);
}

bool callback_arg_string(const std::shared_ptr<CallbackPassString>& arg) {
    return arg->invoke(KString("test string"));
}

bool callback_arg_string_n(const std::shared_ptr<CallbackPassStringN>& arg) {
    return arg->invoke(KOptional<KString>());
}

bool callback_arg_callback(const std::shared_ptr<VoidCallback>& pass, const std::shared_ptr<CallbackPassCallback>& arg) {
    return arg->invoke(pass);
}

bool callback_arg_callback_n(const std::shared_ptr<CallbackPassCallbackN>& arg) {
    return arg->invoke(KOptional<std::shared_ptr<VoidCallback>>());
}

bool callback_arg_enum(const std::shared_ptr<CallbackPassEnum>& arg) {
    return arg->invoke(CASE2);
}

bool callback_arg_dictionary(const std::shared_ptr<CallbackPassDictionary>& arg) {
    return arg->invoke(MyDictionary(1, 2, 3, 4));
}

bool callback_arg_dictionary_n(const std::shared_ptr<CallbackPassDictionaryN>& arg) {
    return arg->invoke(KOptional<MyDictionary>());
}

bool callback_arg_interface(const std::shared_ptr<IMyInterface>& pass, const std::shared_ptr<CallbackPassInterface>& arg) {
    return arg->invoke(std::shared_ptr(pass));
}

bool callback_arg_interface_n(const std::shared_ptr<CallbackPassInterfaceN>& arg) {
    return arg->invoke(KOptional<std::shared_ptr<IMyInterface>>());
}
