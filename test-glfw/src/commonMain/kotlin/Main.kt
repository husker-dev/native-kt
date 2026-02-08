import natives.glfwBindings.glfwInit
import natives.glfwBindings.loadLibGlfwBindings
import natives.glfwBindings.run

fun main() = loadLibGlfwBindings {
    glfwInit()
    run()
}