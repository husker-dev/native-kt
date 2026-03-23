cmake_minimum_required(VERSION 3.15)

project("freetypeBindings")

find_package(PkgConfig REQUIRED)
pkg_check_modules(FREETYPE REQUIRED freetype2)

add_library(${PROJECT_NAME} STATIC src/freetypeBindings.c)

target_include_directories(${PROJECT_NAME} PRIVATE ${FREETYPE_INCLUDE_DIRS})
target_link_libraries(${PROJECT_NAME} PRIVATE ${FREETYPE_LIBRARIES})

target_include_directories(${PROJECT_NAME} PRIVATE include)