#include "api.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Consume

bool consume() {
    return true;
}

bool consumeInt(int32_t arg) {
    return true;
}

bool consumeLong(int64_t arg) {
    return true;
}

bool consumeFloat(float arg) {
    return true;
}

bool consumeDouble(double arg) {
    return true;
}

bool consumeByte(int8_t arg) {
    return true;
}

bool consumeBoolean(bool arg) {
    return true;
}

bool consumeChar(uint16_t arg) {
    return true;
}

bool consumeString(const char* arg) {
    return true;
}

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

// Ping

int32_t pingInt(int32_t arg) {
    return arg;
}

int64_t pingLong(int64_t arg) {
    return arg;
}

float pingFloat(float arg) {
    return arg;
}

double pingDouble(double arg) {
    return arg;
}

int8_t pingByte(int8_t arg) {
    return arg;
}

bool pingBoolean(bool arg) {
    return arg;
}

uint16_t pingChar(uint16_t arg) {
    return arg;
}

const char* pingString(const char* arg) {
    return arg;
}
