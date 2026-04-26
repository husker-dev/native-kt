cmake_minimum_required(VERSION 3.15)

project("jvmOnlyTest")

add_library(${PROJECT_NAME} STATIC src/jvmOnlyTest.c)

target_include_directories(${PROJECT_NAME} PRIVATE include)