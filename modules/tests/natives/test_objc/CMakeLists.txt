cmake_minimum_required(VERSION 3.24)

project("test" LANGUAGES OBJC)

include("${CMAKE_CURRENT_LIST_DIR}/../LocalNativeKt.cmake")

# Create lib
add_library(testobjc_lib OBJECT
        src/interfaces.m
        src/jvmci.m
        src/dictionaries.m
        src/critical.m
        src/primitives/pass_primitive.m
        src/primitives/ping_primitive.m
        src/primitives/return_primitive.m
        src/primitives/callback_arg_primitive.m
        src/primitives/callback_return_primitive.m
        src/arrays/pass_array.m
        src/arrays/return_array.m
        src/arrays/ping_array.m
        src/arrays/callback_arg_array.m
        src/arrays/callback_return_array.m
)

# Generate and add native-kt files
nativekt_generate("nativekt.toml")
find_library(FOUNDATION_FRAMEWORK Foundation REQUIRED)
target_compile_options(testobjc_lib PRIVATE -fobjc-arc)
target_link_libraries(testobjc_lib PRIVATE NativeKt::bindings "${FOUNDATION_FRAMEWORK}")

option(NATIVEKT_OBJC_DATA_TESTS "Build Objective-C data container tests" OFF)
if(NATIVEKT_OBJC_DATA_TESTS)
    enable_testing()
    add_executable(objc_data_tests
            tests/main.m
            tests/string.m
            tests/arrays.m
            tests/dictionaries.m
            src/interfaces.m
    )
    target_compile_options(objc_data_tests PRIVATE -fobjc-arc)
    target_link_libraries(objc_data_tests PRIVATE NativeKt::bindings "${FOUNDATION_FRAMEWORK}")
    add_test(NAME objc_data_tests COMMAND objc_data_tests)
endif()
