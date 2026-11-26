cmake_minimum_required(VERSION 3.24)

project("tc_0_cpp" LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include("${CMAKE_CURRENT_LIST_DIR}/../../../LocalNativeKt.cmake")

add_library(tc_0_cpp STATIC src/tc_0_cpp.cpp)

# Configure native-kt
nativekt_configure(tc_0_cpp "nativekt.toml")