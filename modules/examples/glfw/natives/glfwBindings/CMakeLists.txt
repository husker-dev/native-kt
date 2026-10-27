cmake_minimum_required(VERSION 3.24)

project("glfwBindings")

include(FetchContent)

# Install: nativekt (local)
include("${CMAKE_CURRENT_LIST_DIR}/../../../../tests/natives/LocalNativeKt.cmake")

# Install: glfw
FetchContent_Declare(glfw URL "https://github.com/glfw/glfw/archive/refs/tags/3.5.1.zip")
set(GLFW_BUILD_DOCS OFF CACHE BOOL "" FORCE)
set(GLFW_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(GLFW_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(glfw)

# Install: glad
FetchContent_Declare(glad URL "https://github.com/Dav1dde/glad/archive/refs/tags/v2.0.8.zip" SOURCE_SUBDIR  cmake)
FetchContent_MakeAvailable(glad)
glad_add_library(glad REPRODUCIBLE EXCLUDE_FROM_ALL LOADER API gl:core=4.6 EXTENSIONS GL_ARB_bindless_texture GL_EXT_texture_compression_s3tc)

# Setup
add_library(${PROJECT_NAME} STATIC src/glfw.c)

nativekt_generate("nativekt.toml")
target_link_libraries(${PROJECT_NAME} PRIVATE glad glfw NativeKt::bindings)
