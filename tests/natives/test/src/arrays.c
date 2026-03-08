#include <api.h>
#include <stdlib.h>

// Arrays

KBoolean passArray(KIntArray arg) {
    return arg.size == 2 &&
        arg.elements[0] == 1 &&
        arg.elements[1] == 2;
}

KIntArray returnArray() {
    KInt* elements = malloc(2 * sizeof(KInt));
    elements[0] = 1;
    elements[1] = 2;
    return makeKIntArray(elements, 2);
}

KIntArray pingArray(KIntArray arg) {
    return arg;
}