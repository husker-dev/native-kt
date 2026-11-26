cmake_minimum_required(VERSION 3.24)

project("tc_6_c")

include("${CMAKE_CURRENT_LIST_DIR}/../../../LocalNativeKt.cmake")

add_library(tc_6_c STATIC src/tc_6_c.c)

# Configure native-kt
nativekt_configure(tc_6_c "nativekt.toml")