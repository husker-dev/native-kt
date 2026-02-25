cmake_minimum_required(VERSION 3.15)

project("glfwBindings")


# Setup libraries
include(FetchContent)
FetchContent_Declare(glfw GIT_REPOSITORY https://github.com/glfw/glfw.git GIT_TAG master)
FetchContent_Declare(glad GIT_REPOSITORY https://github.com/SasLuca/MyGlad.git GIT_TAG master)

set(GLFW_BUILD_DOCS OFF CACHE BOOL "" FORCE)
set(GLFW_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(GLFW_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)

FetchContent_MakeAvailable(glfw)
FetchContent_MakeAvailable(glad)

# Setup
add_library(${PROJECT_NAME} STATIC src/glfw.c)
target_link_libraries(${PROJECT_NAME} PUBLIC glad glfw)
target_include_directories(${PROJECT_NAME} PRIVATE include)