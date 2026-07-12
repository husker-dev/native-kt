#include <api.hpp>
#include <cstring>

// Consume

KBoolean pass_void() {
    return true;
}

KBoolean pass_char(const KChar arg) {
    return arg == 'a';
}

KBoolean pass_boolean(const KBoolean arg) {
    return arg == true;
}

KBoolean pass_byte(const KByte arg) {
    return arg == 1;
}

KBoolean pass_ubyte(const KUByte arg) {
    return arg == 255u;
}

KBoolean pass_short(const KShort arg) {
    return arg == 1;
}

KBoolean pass_ushort(const KUShort arg) {
    return arg == 65535u;
}

KBoolean pass_int(const KInt arg) {
    return arg == 99;
}

KBoolean pass_uint(const KUInt arg) {
    return arg == 4294967295u;
}

KBoolean pass_long(const KLong arg) {
    return arg == 9223372036854775805;
}

KBoolean pass_ulong(const KULong arg) {
    return arg == 18446744073709551615u;
}

KBoolean pass_float(const KFloat arg) {
    return arg == 99.9f;
}

KBoolean pass_double(const KDouble arg) {
    return arg == 1.1;
}

KBoolean pass_string(KString* arg) {
    return !(arg->__flags & 1) && arg->length == 11 && strncmp(arg->data, "test string", arg->length) == 0;
}

KBoolean pass_string_n(KString* arg) {
    return arg == nullptr;
}

KBoolean pass_enum(const MyEnum arg) {
    return arg == CASE2;
}

KBoolean pass_dictionary(MyDictionary* arg) {
    return arg->a == 1 &&
        arg->b == 2 &&
        arg->c == 3 &&
        arg->d == 4;
}

KBoolean pass_dictionary_n(MyDictionary* arg) {
    return arg == nullptr;
}