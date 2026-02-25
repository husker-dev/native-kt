#include <api.h>
#include <string.h>
#include <stdlib.h>

// Callback

void simpleCallback(SimpleCallback* callback) {
    INVOKE(callback, 2);
}

SimpleCallback* callbackReturn(SimpleCallback* callback) {
    return callback;
}

bool callbackReturnString(StringCallback* callback) {
    const char* text = INVOKE(callback);
    bool result = strcmp(text, "test") == 0;
    free((void*)text);
    return result;
}

bool callbackPingString(StringPingCallback* callback) {
    return strcmp(INVOKE(callback, "test"), "test") == 0;
}

SimpleCallback* callbackPingCallback(CallbackPingCallback* callback, SimpleCallback* item) {
    return INVOKE(callback, item);
}