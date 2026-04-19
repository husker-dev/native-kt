cmake_minimum_required(VERSION 3.15)
project(root)

add_subdirectory(./modules/tests/natives/test)
add_subdirectory(./modules/examples/freetype/natives/freetypeBindings)
add_subdirectory(./modules/examples/glfw/natives/glfwBindings)