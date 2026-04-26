cmake_minimum_required(VERSION 3.15)

project("jsOnlyTest")

add_library(${PROJECT_NAME} STATIC src/jsOnlyTest.c)

target_include_directories(${PROJECT_NAME} PRIVATE include)