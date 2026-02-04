<div>
  <div>
        <h3>native-kt</h3>
        Gradle plugin for convenient C/C++ integration into Kotlin Multiplatform project. 
        <br>
        Supports JVM, Native and JS
  </div>
</div>
<br>

<a href="LICENSE"><img src="https://img.shields.io/github/license/husker-dev/native-kt?style=flat-square"></a>
<a href="https://github.com/husker-dev/native-kt/releases/latest"><img src="https://img.shields.io/github/v/release/husker-dev/native-kt?style=flat-square"></a>

## How to use
   
Apply plugin:
```kotlin
plugins {
   id("com.huskerdev.native-kt") version "1.0.0"
}
```
Then add native project (CMake project):

```kotlin
native {
   create("mylib", Multiplatform::class)
}
```

By default, all native projects are stored in `src/nativeInterop/[name]`. 
You can change it using property `projectDir`.

Each project requires these files: 
- `CMakeLists.txt` - CMake configuration file
- `api.idl` - API description

> Call `:cmakeInitMyLib` to generate basic project for you.

## Usage in native

When Gradle project is loaded, it generates header `api.h` based on `api.idl` in native project directory. 

This C-header needs to be included and implemented in your code.

Example: 
```c
// api.h

// ...
void helloWorld();
// ...
```
```c
// myLib.c

#include "api.h"
#include <stdio.h>

void helloWorld() {
    printf("Hello, World!\n");
    fflush(stdout);
}
```

### CMake tips 
- Do not specify the compiler type in your CMake configuration - it may break the whole compilation.
- It is not recommended to use compiler-specific arguments without checking for the compiler type.
  The default compiler is clang, but emscripten can also be used, where the arguments won't work.

## Usage in Kotlin

When Gradle project is loaded, it generates functions based on your `api.idl`.

Before you can call your native function, you must initialize the library.
You can do it synchronously or asynchronously. 
> (!) Note that synchronous initialization will not work in `Kotlin/JS`.

```kotlin

// Sync init (won't work in Kotlin/JS)
loadLibMyLibSync()

// Async init with callback
loadLibMyLib { /* ... */ }

// Async init (suspend function)
loadLibMyLib()
```

After initialization, you can freely use native functions

```kotlin
suspend fun main() {
    loadLibMyLib()  // Initialize
    helloWorld()    // Call native function
}
```

### Windows
mingw64 is needed for compilations using `clang`. It is important to set clang binaries to Path variable.

## Single source set

The previous guide assumed you wanted to use one shared module and several child modules on different platforms. 

There's also a way to use just one sourceSet (for example, jvmMain).

```kotlin
// build.gradle.kts

native {
    create("mylib", SingleModule::class)  {
        targetSourceSet = TARGET_JVM
    }
}
```

Now your module will be available only in `jvmMain` source set.
