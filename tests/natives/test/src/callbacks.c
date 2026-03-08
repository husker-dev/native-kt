#include <api.h>
#include <string.h>
#include <stdlib.h>

// Callback

void simpleCallback(SimpleCallback* callback) {
    callback->invoke(callback, 2);
}

SimpleCallback* callbackReturn(SimpleCallback* callback) {
    return callback;
}

KBoolean callbackReturnString(StringCallback* callback) {
    KString text = callback->invoke(callback);
    bool result = strcmp(text.data, "test") == 0;
    free((void*)text.data);
    return result;
}

KBoolean callbackPingString(StringPingCallback* callback) {
    KString txt = callback->invoke(callback, makeKString("test", 4));
    return strcmp(txt.data, "test") == 0;
}

SimpleCallback* callbackPingCallback(CallbackPingCallback* callback, SimpleCallback* item) {
    return callback->invoke(callback, item);
}