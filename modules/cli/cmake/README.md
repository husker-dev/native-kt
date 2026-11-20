# native-kt-cmake

CMake implementation of native-kt generator.

### Usage

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
nativekt_generate("nativekt.toml")

# Link with the target
target_link_libraries(my_target PRIVATE NativeKt::bindings)

```