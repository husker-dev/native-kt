#include <api.h>

void return_void(void) {
}

uint16_t return_char(void) {
    return 'a';
}

BoxedChar* return_char_n(void) {
    return NULL;
}

bool return_boolean(void) {
    return true;
}

BoxedBoolean* return_boolean_n(void) {
    return boxed_boolean_new(true);
}

int8_t return_byte(void) {
    return 99;
}

BoxedByte* return_byte_n(void) {
    return NULL;
}

uint8_t return_ubyte(void) {
    return 255u;
}

BoxedByte* return_ubyte_n(void) {
    return boxed_byte_new(255u);
}

int16_t return_short(void) {
    return 99;
}

BoxedShort* return_short_n(void) {
    return NULL;
}

uint16_t return_ushort(void) {
    return 65535u;
}

BoxedShort* return_ushort_n(void) {
    return boxed_short_new(65535u);
}

int32_t return_int(void) {
    return 99;
}

BoxedInt* return_int_n(void) {
    return NULL;
}

uint32_t return_uint(void) {
    return 4294967295u;
}

BoxedInt* return_uint_n(void) {
    return boxed_int_new(4294967295u);
}

int64_t return_long(void) {
    return 9223372036854775805;
}

BoxedLong* return_long_n(void) {
    return NULL;
}

uint64_t return_ulong(void) {
    return 18446744073709551615u;
}

BoxedLong* return_ulong_n(void) {
    return boxed_long_new(18446744073709551615u);
}

float return_float(void) {
    return 99;
}

BoxedFloat* return_float_n(void) {
    return NULL;
}

double return_double(void) {
    return 99.0;
}

BoxedDouble* return_double_n(void) {
    return boxed_double_new(99.0);
}

KString* return_string(void) {
    return kstring_new("test string");
}

KString* return_string_empty(void) {
    return kstring_new("");
}

KString* return_string_n(void) {
    return NULL;
}

MyEnum return_enum(void) {
    return MyEnum_CASE2;
}

BoxedInt* return_enum_n(void) {
    return boxed_int_new(MyEnum_CASE2);
}

MyDictionary* return_dictionary(void) {
    return MyDictionary_new(1, 2, 3, 4);
}

MyDictionary* return_dictionary_n(void) {
    return NULL;
}
