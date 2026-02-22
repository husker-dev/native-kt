# Internals

Plugin generates a special API for communicating with native code:

```kotlin title="Example"
expect fun printHelloWorld()
```
The implementation will be unique for each target, but you don't need to worry about that.

!!! note
All generated code can be viewed in the `build/generated/natives` and `build/cmake` directories.<br>
If you think the code isn't efficient enough and know how to improve it, please let us know in an issue on GitHub.

### Kotlin/JVM

The JVM has several ways to communicate with native:

- JNI
- Foreign Function & Memory API
- JVMCI

You can use all of them with this plugin.
Almost any integration can be disabled via the Gradle configuration.

By default, the plugin automatically selects which integration type to use.<br>
JVMCI is the most efficient, but it only applies to critical functions.