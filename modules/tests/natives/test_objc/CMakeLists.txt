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

# Configure native-kt
nativekt_configure(testobjc_lib "nativekt.toml")

find_library(FOUNDATION_FRAMEWORK Foundation REQUIRED)
target_compile_options(testobjc_lib PRIVATE -fobjc-arc)
target_link_libraries(testobjc_lib PRIVATE "${FOUNDATION_FRAMEWORK}")