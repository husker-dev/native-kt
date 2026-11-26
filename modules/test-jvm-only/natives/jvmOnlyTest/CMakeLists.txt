cmake_minimum_required(VERSION 3.24)

project("jvmOnlyTest")

include("${CMAKE_CURRENT_LIST_DIR}/../../../tests/natives/LocalNativeKt.cmake")

add_library(${PROJECT_NAME} STATIC src/jvmOnlyTest.c)

# Configure native-kt
nativekt_configure(${PROJECT_NAME} "nativekt.toml")