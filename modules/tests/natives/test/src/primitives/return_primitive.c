#include <api.h>

void return_void(void) {
}

uint16_t return_char(void) {
    return 'a';
}

uint16_t* return_char_n(void) {
    return NULL;
}

bool return_boolean(void) {
    return true;
}

bool* return_boolean_n(void) {
    bool* result = (bool*) malloc(sizeof(bool));
    *result = true;
    return result;
}

int8_t return_byte(void) {
    return 99;
}

int8_t* return_byte_n(void) {
    return NULL;
}

uint8_t return_ubyte(void) {
    return 255u;
}

uint8_t* return_ubyte_n(void) {
    uint8_t* result = (uint8_t*) malloc(sizeof(uint8_t));
    *result = 255u;
    return result;
}

int16_t return_short(void) {
    return 99;
}

int16_t* return_short_n(void) {
    return NULL;
}

uint16_t return_ushort(void) {
    return 65535u;
}

uint16_t* return_ushort_n(void) {
    uint16_t* result = (uint16_t*) malloc(sizeof(uint16_t));
    *result = 65535u;
    return result;
}

int32_t return_int(void) {
    return 99;
}

int32_t* return_int_n(void) {
    return NULL;
}

uint32_t return_uint(void) {
    return 4294967295u;
}

uint32_t* return_uint_n(void) {
    uint32_t* result = (uint32_t*) malloc(sizeof(uint32_t));
    *result = 4294967295u;
    return result;
}

int64_t return_long(void) {
    return 9223372036854775805;
}

int64_t* return_long_n(void) {
    return NULL;
}

uint64_t return_ulong(void) {
    return 18446744073709551615u;
}

uint64_t* return_ulong_n(void) {
    uint64_t* result = (uint64_t*) malloc(sizeof(uint64_t));
    *result = 18446744073709551615u;
    return result;
}

float return_float(void) {
    return 99;
}

float* return_float_n(void) {
    return NULL;
}

double return_double(void) {
    return 99.0;
}

double* return_double_n(void) {
    double* result = (double*) malloc(sizeof(double));
    *result = 99.0;
    return result;
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

MyEnum* return_enum_n(void) {
    MyEnum* result = (MyEnum*) malloc(sizeof(MyEnum));
    *result = MyEnum_CASE2;
    return result;
}

MyDictionary* return_dictionary(void) {
    return MyDictionary_new(1, 2, 3, 4);
}

MyDictionary* return_dictionary_n(void) {
    return NULL;
}
