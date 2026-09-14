cmake_minimum_required(VERSION 3.15)

project("tc_0_cpp" LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(${PROJECT_NAME} STATIC src/tc_0_cpp.cpp)

target_include_directories(${PROJECT_NAME} PRIVATE include)