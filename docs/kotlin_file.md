---
icon: simple/kotlin
---

# Kotlin code

For Kotlin, a special file is generated with functions, structures, etc.

By changing `classPath` property in the project configuration, you can configure file location. 
By default, this is `natives.[name]`

## Initialization

Before using native code, the library must be initialized.

This can be done synchronously or asynchronously.
!!! warning
    Asynchronous is highly recommended, as it is the only method supported in JS.

The initialization function name looks like this:
```c
load[name]()
load[name]Sync()
```

The rest of the API elements look the same as in NDL.

Example:
```ndl
namespace global {
    void printHelloWorld();
};
```
```kotlin

suspend fun main() {
    loadExampleLib()
    
    printHelloWorld()
}
```