#include "glad/glad.h"
#include "GLFW/glfw3.h"


void run() {
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);

    GLFWwindow* glfw_window = glfwCreateWindow(800, 600, "OpenGL experiment", NULL, NULL);
    glfwMakeContextCurrent(glfw_window);
    gladLoadGL();
    glfwSwapInterval(1);

    while (!glfwWindowShouldClose(glfw_window)) {
        glfwSwapBuffers(glfw_window);
        glfwPollEvents();

        // Rendering
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
    }
}