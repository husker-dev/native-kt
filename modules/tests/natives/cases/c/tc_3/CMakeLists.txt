cmake_minimum_required(VERSION 3.15)

project("tc_3_c")

add_library(${PROJECT_NAME} STATIC src/tc_3_c.c)

target_include_directories(${PROJECT_NAME} PRIVATE include)