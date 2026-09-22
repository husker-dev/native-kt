#include <api.hpp>


void stub1(std::shared_ptr<IEmptyInterface> e) {}
void stub2(std::shared_ptr<IMyInterface> e) {}

struct EmptyInterface : IEmptyInterface {
};

struct MyInterface : IMyInterface {
};
