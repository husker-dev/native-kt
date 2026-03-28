cmake_minimum_required(VERSION 3.15)
project(root)

add_subdirectory(./tests/natives/test)
add_subdirectory(./examples/freetype/natives/freetypeBindings)
add_subdirectory(./examples/glfw/natives/glfwBindings)