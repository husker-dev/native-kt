cmake_minimum_required(VERSION 3.24)

project("tc_1_c")

include("${CMAKE_CURRENT_LIST_DIR}/../../../LocalNativeKt.cmake")

add_library(tc_1_c STATIC src/tc_1_c.c)

# Configure native-kt
nativekt_configure(tc_1_c "nativekt.toml")