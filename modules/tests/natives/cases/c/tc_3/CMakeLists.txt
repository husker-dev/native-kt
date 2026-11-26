cmake_minimum_required(VERSION 3.24)

project("tc_3_c")

include("${CMAKE_CURRENT_LIST_DIR}/../../../LocalNativeKt.cmake")

add_library(tc_3_c STATIC src/tc_3_c.c)

# Configure native-kt
nativekt_configure(tc_3_c "nativekt.toml")