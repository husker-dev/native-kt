<div>
  <img width="140" src="./.github/icon.webp" alt="logo" align="left">
  <div>
        <h3>native-kt</h3>
        Gradle plugin for convenient C/C++ integration into a Kotlin Multiplatform project. 
        <br>
        Supports JVM, Android, Native, and JS targets
  </div>
</div>
<br>

<a href="LICENSE"><img src="https://img.shields.io/github/license/husker-dev/native-kt?style=flat-square"></a>
<a href="https://github.com/husker-dev/native-kt/releases/latest"><img src="https://img.shields.io/github/v/release/husker-dev/native-kt?style=flat-square"></a>

## Key features

- **Automatic binding generation from IDL file** (no JNI / JNA / cinterop boilerplate)
- JVM interop via **Foreign Function & Memory API (Project Panama)**
- Low-overhead **critical native calls**
- **Static native linking** for Kotlin/Native
- **JavaScript target support** via Emscripten
- **CMake-based native builds** with existing toolchains

## Current limitations
- No structs, arrays, or callbacks yet
- Kotlin 2.0+
> ⚠️ This project is under active development.

## How to use
   
Apply plugin in your `build.gradle.kts`:
```kotlin
plugins {
   id("com.huskerdev.native-kt") version "1.0.1"
}
```

Then declare a native project in the `native {}` block:
```kotlin
native {
    create("mylib") {
        // ...
    }
}
```

You can run `./gradlew :cmakeInitMyLib` to generate a minimal CMake project.<br>
This task is optional but recommended for getting started.

For an example, you can look at the [test-glfw](https://github.com/husker-dev/native-kt/tree/master/test-glfw) module.

## Requirements

- **macOS**: `Xcode Command Line Tools`
- **Windows**: `mingw64` ([MSYS2](https://www.msys2.org/)) <br>It is important to set `msys64/clang64/bin` directory to PATH.
- **Linux**: It is required to have `clang`. Please follow your own distribution instructions.

[CMake](https://cmake.org/) is required to configure your projects.

#### Compilation for Android
It is important to have **Android SDK** installed, as well as **NDK** with specified version. 
<br>`ANDROID_HOME` is required to locate the SDK.
<br>You can simply install [Android Studio](https://developer.android.com/studio) and set up [NDK](https://developer.android.com/studio/projects/install-ndk) in UI.

#### Compilation for Kotlin/JS
[Emscripten](https://emscripten.org/docs/getting_started/downloads.html) is required to be installed.
<br>`EMSDK` is required to locate the SDK.


## IDL file

The [`WebIDL`](https://webidl.spec.whatwg.org/) format is used to describe the interface of C functions in `api.idl`.

It should be located in the root of the native project.<br>
By default: `src/nativeInterop/[name]/api.idl`

All functions **must** be declared inside the `global` namespace.
Other namespaces will be ignored.

```webidl
// Example
namespace global {
    void helloWorld();
}
```

> ⚠️ **Currently, you can only specify functions with primitive types.** <br>Structures and arrays support will be coming later.

### Supported types

|     IDL     | Kotlin  |      C      |
| ----------- | ------- | ------------|
| `long`      | Int     | int32_t     |
| `long long` | Long    | int64_t     |
| `float`     | Float   | float       |
| `double`    | Double  | double      |
| `byte`      | Byte    | int8_t      |
| `symbol`    | Char    | int16_t     |
| `bool`      | Boolean | bool        |
| `DOMString` | String  | const char* |
| `void`      | Unit    | void        |

## Usage in native

By default, all native projects are stored in `src/nativeInterop/[name]`.<br>
You can change it in `build.gradle` using property `projectDir`.

When Gradle project is loaded, it generates header `api.h` based on `api.idl`. 

This C-header should be included and **must** be implemented in your code.
> For C++, make sure the functions are exported with `extern "C"`.

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

### Memory lifecycle
There is no API for manual memory management, but it is important to understand the memory lifecycle.

#### ➡️ Incoming pointers

All pointer variables from function arguments (strings, arrays, ...) , **must not** be freed or stored outside this function.<br>
Before function execution, actual Kotlin String/Array data is copied to the pointers, and freed after completion.

In other words, if you want to store the string in native code, you **must** copy it to your own allocated memory.

> This behavior may be modified in the future.

#### ⬅️ Outcoming pointer

When you return a pointer, it will not be used "as is" in Kotlin variables. <br>
Data will be **copied** to actual Kotlin objects, but pointer will not be freed.

However, you can add `[Dealloc]` annotation to the function in the `api.idl` file - it will free the pointer after execution.

It's also important to consider string literals.<br>
If you have a statement like `return "my literal"`, you **must not** use `[Dealloc]` annotation.

## Usage in Kotlin

When Gradle project is loaded, it generates functions based on your `api.idl`.

By default, API is generated in `natives.[name]` classpath.<br>
It can be changed when declaring a module in `build.gradle` using the `classPath` option.

> If you have restarted a Gradle, but IntelliJ IDEA does not see the functions,<br>
> then right-click at the `build` directory and select `Reload from Disk`

Before you can call your native function, you must load the library.<br>
You can do it synchronously or asynchronously.

> ⚠️ Note that synchronous initialization will not work in `Kotlin/JS`.

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

## Critical native calls

JVM and Android have a `critical` way for calling native functions. <br>
These functions should be fast, use only primitive types, and must not perform blocking operations or callbacks.

To declare a critical function, add the `[Critical]` annotation in `api.idl` before declaration: 
```webidl
namespace global {
    [Critical]
    long fastAdd(long a, long b);
}
```

> Currently, this is only implemented for the `Foreign Function & Memory API` in JVM.<br>
> Support for JNI and Android will be coming in the future.<br>

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

This mode is useful when native code is required only for a specific platform.
