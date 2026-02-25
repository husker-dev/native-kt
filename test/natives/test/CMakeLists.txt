cmake_minimum_required(VERSION 3.15)

project("test")

add_library(${PROJECT_NAME} OBJECT
        src/callbacks.c
        src/jvmci.c
        src/pass.c
        src/passAndReturn.c
        src/return.c
)

target_include_directories(${PROJECT_NAME} PRIVATE include)