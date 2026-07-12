#include <api.h>
#include <stdio.h>

void* _Nonnull _interface_b_new_0() {
    return (void*) 1;
}
void _interface_b_free(void* _Nonnull _self) {}

void* _Nonnull _interface_a_new_0() {
    return (void*) 1;
}

void _interface_a_fn_test(void* _Nonnull _self, void* _Nonnull arg) {
    printf("Hello from C interface!\n");
    fflush(stdout);
}

void _interface_a_free(void* _Nonnull _self) {}