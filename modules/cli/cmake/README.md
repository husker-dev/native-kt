# native-kt-cmake

CMake implementation of native-kt generator.

### Usage

#### Cmake (>= 3.24) 

```cmake
# Enable FetchContent
include(FetchContent)

# Add dependency and generate
FetchContent_Declare(
        nativekt
        GIT_REPOSITORY https://github.com/husker-dev/native-kt-cmake.git
        GIT_TAG        ${{VERSION}}
)
FetchContent_MakeAvailable(nativekt)

# Generate bindings
nativekt_generate("nativekt.toml")

# Link with the target (must be OBJECT)
# add_library(my_lib OBJECT main.c)
target_link_libraries(my_lib PRIVATE NativeKt::bindings)
```

#### C/C++/ObjC
```c
#include <nativekt.h>

void hello_world() {
    // ...
}
```