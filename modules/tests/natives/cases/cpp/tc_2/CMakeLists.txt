cmake_minimum_required(VERSION 3.24)

project("tc_2_cpp" LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include("${CMAKE_CURRENT_LIST_DIR}/../../../LocalNativeKt.cmake")

add_library(${PROJECT_NAME} STATIC src/tc_2_cpp.cpp)

# Generate and add native-kt files
nativekt_generate("nativekt.toml")
target_link_libraries(${PROJECT_NAME} PRIVATE NativeKt::bindings)