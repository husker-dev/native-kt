#include <api.hpp>
#include <iostream>

struct B : IB {
    explicit B() {}
};

struct A : IA {
    explicit A() {}
    void test(void* arg) override {
        std::cout << "Hello from C++ interface!" << std::endl;
    }
};

IB* IB::_create() {
    return new B();
}

IA* IA::_create() {
    return new A();
}