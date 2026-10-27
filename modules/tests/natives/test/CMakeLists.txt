cmake_minimum_required(VERSION 3.24)

project("test")

include("${CMAKE_CURRENT_LIST_DIR}/../LocalNativeKt.cmake")

# Create lib
add_library(${PROJECT_NAME} OBJECT
        src/interfaces.c
        src/jvmci.c
        src/dictionaries.c
        src/critical.c
        src/primitives/pass_primitive.c
        src/primitives/ping_primitive.c
        src/primitives/return_primitive.c
        src/primitives/callback_arg_primitive.c
        src/primitives/callback_return_primitive.c
        src/arrays/pass_array.c
        src/arrays/return_array.c
        src/arrays/ping_array.c
        src/arrays/callback_arg_array.c
        src/arrays/callback_return_array.c
)

# Generate and add native-kt files
nativekt_generate("nativekt.toml")
target_link_libraries(${PROJECT_NAME} PRIVATE NativeKt::bindings)