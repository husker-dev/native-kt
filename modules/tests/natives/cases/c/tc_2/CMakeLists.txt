cmake_minimum_required(VERSION 3.24)

project("tc_2_c")

include("${CMAKE_CURRENT_LIST_DIR}/../../../LocalNativeKt.cmake")

add_library(tc_2_c STATIC src/tc_2_c.c)

# Configure native-kt
nativekt_configure(tc_2_c "nativekt.toml")