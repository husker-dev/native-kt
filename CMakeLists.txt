cmake_minimum_required(VERSION 3.24)
project(root)

add_subdirectory(./modules/examples/freetype/natives/freetypeBindings)
add_subdirectory(./modules/examples/glfw/natives/glfwBindings)

add_subdirectory(./modules/tests/natives/test_c)
add_subdirectory(./modules/tests/natives/test_cpp)
add_subdirectory(./modules/test-jvm-only/natives/jvmOnlyTest)