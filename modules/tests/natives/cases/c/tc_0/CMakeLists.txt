cmake_minimum_required(VERSION 3.24)

project("tc_0_c")

include("${CMAKE_CURRENT_LIST_DIR}/../../../LocalNativeKt.cmake")

add_library(tc_0_c STATIC src/tc_0_c.c)

# Configure native-kt
nativekt_configure(tc_0_c "nativekt.toml")