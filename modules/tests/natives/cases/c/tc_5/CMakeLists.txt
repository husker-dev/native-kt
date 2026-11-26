cmake_minimum_required(VERSION 3.24)

project("tc_5_c")

include("${CMAKE_CURRENT_LIST_DIR}/../../../LocalNativeKt.cmake")

add_library(tc_5_c STATIC src/tc_5_c.c)

# Configure native-kt
nativekt_configure(tc_5_c "nativekt.toml")