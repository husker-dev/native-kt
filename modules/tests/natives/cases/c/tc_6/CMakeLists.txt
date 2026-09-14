cmake_minimum_required(VERSION 3.15)

project("tc_6_c")

add_library(${PROJECT_NAME} STATIC src/tc_6_c.c)

target_include_directories(${PROJECT_NAME} PRIVATE include)