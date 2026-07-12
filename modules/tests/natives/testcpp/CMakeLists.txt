cmake_minimum_required(VERSION 3.15)

project("testcpp" LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(${PROJECT_NAME} OBJECT
        src/interfaces.cpp
        src/jvmci.cpp
        src/dictionaries.cpp
        src/critical.cpp
        src/primitives/pass_primitive.cpp
        src/primitives/ping_primitive.cpp
        src/primitives/return_primitive.cpp
        src/primitives/callback_arg_primitive.cpp
        src/primitives/callback_return_primitive.cpp
        src/arrays/pass_array.cpp
        src/arrays/return_array.cpp
        src/arrays/ping_array.cpp
        src/arrays/callback_arg_array.cpp
        src/arrays/callback_return_array.cpp
)

target_include_directories(${PROJECT_NAME} PRIVATE include)