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
- Only primitive types are supported
- No structs, arrays, or callbacks yet
- Kotlin 2.0+

> ⚠️ This project is under active development. APIs may change between minor versions.

## How to use
   
Apply plugin in your `build.gradle.kts`:
```kotlin
plugins {
   id("com.huskerdev.native-kt") version "1.0.0"
}
```

Then declare a native project in the `native {}` block:
```kotlin
native {
   create("mylib", Multiplatform::class)
}
```

- `Multiplatform` - generates bindings for all enabled KMP targets
- `SingleModule` - generates bindings for a single source set only (see [Single source set](#single-source-set))

> Run `./gradlew :cmakeInitMyLib` to generate a minimal CMake native project
> with `CMakeLists.txt`, `api.idl`, and example source files.<br>
> This task is optional but recommended for getting started.

By default, all native projects are stored in `src/nativeInterop/[name]`.
You can change it using property `projectDir`.



## Requirements

- **macOS**: `Xcode Command Line Tools`
- **Windows**: `mingw64` ([MSYS2](https://www.msys2.org/)) <br>It is important to set `msys64/clang64/bin` directory to PATH.
- **Linux**: It is required to have `clang`. Please follow your own distribution instructions.

[CMake](https://cmake.org/) is required to configure your projects.

### Compilation for Android
It is important to have **Android SDK** installed, as well as **NDK** with specified version. 
<br>`ANDROID_HOME` is required to locate the SDK.
<br>You can simply install [Android Studio](https://developer.android.com/studio) and set up [NDK](https://developer.android.com/studio/projects/install-ndk) in UI.

### Compilation for Kotlin/JS
[Emscripten](https://emscripten.org/docs/getting_started/downloads.html) is required to be installed.
<br>`EMSDK` is required to locate the SDK.


## IDL file

The [`WebIDL`](https://webidl.spec.whatwg.org/) format is used to describe the interface of C functions. 

All functions **must** be declared inside the `global` namespace.
Other namespaces will be ignored.

Example:
```webidl
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

When Gradle project is loaded, it generates header `api.h` based on `api.idl` in native project directory. 

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

### CMake tips 
- Do not specify the compiler type in your CMake configuration - it may break the whole compilation.
- It is not recommended to use compiler-specific arguments without checking for the compiler type.
  The default compiler is clang, but emscripten can also be used, where the arguments won't work.

## Usage in Kotlin

When Gradle project is loaded, it generates functions based on your `api.idl`.

Before you can call your native function, you must initialize the library.
You can do it synchronously or asynchronously. 

Initialization is required once per process before calling any native functions.
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

JVM and Android have a `critical` method for calling native functions. 

These functions should be fast, use only primitive types, and must not perform blocking operations or callbacks.

To declare a critical function, add the `[Critical]` annotation in `api.idl` before declaration: 
```webidl
namespace global {
    [Critical]
    long fastAdd(long a, long b);
}
```

> Currently, this is only implemented for the `Foreign Function API` in JVM.<br>
> Support for Android will be coming in the future.<br>
> Support for JNI on the JVM will likely not be implemented due to its deprecation.

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
