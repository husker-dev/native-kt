cmake_minimum_required(VERSION 3.24)

project("tc_4_c")

include("${CMAKE_CURRENT_LIST_DIR}/../../../LocalNativeKt.cmake")

add_library(tc_4_c STATIC src/tc_4_c.c)

# Configure native-kt
nativekt_configure(tc_4_c "nativekt.toml")