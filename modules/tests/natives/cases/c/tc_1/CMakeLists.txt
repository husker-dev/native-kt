cmake_minimum_required(VERSION 3.24)

project("tc_1_c")

include("${CMAKE_CURRENT_LIST_DIR}/../../../LocalNativeKt.cmake")

add_library(${PROJECT_NAME} STATIC src/tc_1_c.c)

# Generate and add native-kt files
nativekt_generate("nativekt.toml")
target_link_libraries(${PROJECT_NAME} PRIVATE NativeKt::bindings)