#include <api.h>
#include <stdlib.h>
#include <string.h>

// Get

void get() {
}

int32_t getInt() {
    return 99;
}

int64_t getLong() {
    return 9223372036854775805;
}

float getFloat() {
    return 99;
}

double getDouble() {
    return 99.0;
}

int8_t getByte() {
    return 99;
}

bool getBoolean() {
    return true;
}

uint16_t getChar() {
    return 'a';
}

const char* getStringLiteral() {
    return "test string";
}

const char* getString() {
    char* str = (char*)malloc(100);
    strcpy(str, "test string\0");
    return str;
}