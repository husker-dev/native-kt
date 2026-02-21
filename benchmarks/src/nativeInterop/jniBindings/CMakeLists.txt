cmake_minimum_required(VERSION 3.15)

project("jniBindings")

add_library(${PROJECT_NAME} STATIC src/bindings.c)
target_include_directories(${PROJECT_NAME} PRIVATE include)