#include <api.hpp>
#include <iostream>

namespace {
    struct MyInterface : IMyInterface {
        explicit MyInterface() {}
        ~MyInterface() override {
            std::cout << "Destroy MyInterface" << std::endl;
        }

        bool test() override {
            std::cout << "Hello from C++ interface!" << std::endl;
            return true;
        }

        bool test_critical() override {
            return true;
        }

        void pass_interface(std::shared_ptr<IMyInterface> a1) override {

        }

        std::shared_ptr<IMyInterface> return_interface() override {
            return std::make_shared<MyInterface>();
        }
    };
}

IMyInterface* IMyInterface::_create() {
    return new MyInterface();
}

IMyInterface* IMyInterface::new_critical(int32_t a1) {
    return new MyInterface();
}