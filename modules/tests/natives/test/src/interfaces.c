#include <api.h>
#include <stdio.h>

void* _interface_myinterface_new_0(void) {
    return (void*) 1;
}

void* _interface_myinterface_new_1(int32_t a1) {
    return (void*) 1;
}

bool _interface_myinterface_fn_test(void* _self) {
    printf("Hello from C interface!\n");
    fflush(stdout);
    return true;
}

bool _interface_myinterface_fn_test_critical(void* _self) {
    return true;
}

void _interface_myinterface_fn_pass_interface(void* _Nonnull _self, RC_MyInterface* _Nonnull a1) {

}
RC_MyInterface* _Nonnull _interface_myinterface_fn_return_interface(void* _Nonnull _self) {
    return (void*) 1;
}

void _interface_myinterface_free(void* _self) {
    printf("Clean MyInterface!\n");
    fflush(stdout);
}