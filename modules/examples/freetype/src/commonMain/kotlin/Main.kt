import natives.freetypeBindings.helloWorld
import natives.freetypeBindings.loadLibFreetypeBindings


fun main() = loadLibFreetypeBindings {
    helloWorld()
}