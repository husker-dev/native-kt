cmake_minimum_required(VERSION 3.15)

project("test")

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

target_include_directories(${PROJECT_NAME} PRIVATE include)