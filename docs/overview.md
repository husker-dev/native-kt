---
icon: lucide/file-sliders
---

# Overview

**native-kt** is a Gradle plugin that creates an 
interop between Kotlin Multiplatform and C/C++ 
based on [NDL](/ndl_file) description file.

This project also includes a syntax highlighting [plugin](https://plugins.jetbrains.com/plugin/31414-native-kt-utils).

## Runtime

This project **doesn't provide** tools to manipulate native memory from Kotlin. 

However, it does require a runtime, which it adds itself.
The runtime only includes the functions needed for interop inside generated code. 

As a bonus, there is a class for retrieving the current OS and architecture, that you can use.

## Target details

#### JVM
`JNI` and `Foreign Functions & Memory API` are used to interact with native code. 

`FFM API` is supporting critical functions.
Also, there is a `JVMCI` acceleration for critical functions in supported JVMs (e.g. GraalVM).

#### JS/Wasm
Kotlin JS interoperability is used to interop with Wasm code, compiled by [Emscripten](https://emscripten.org/).

Not every native program can be compiled to this target. See [Emscripten limitations](https://emscripten.org/docs/porting/guidelines/api_limitations.html) for details.

#### Native
`cinterop` is used to call native functions. For now, only static linking is supported.

#### Android
Uses `JNI` as JVM, but with [@CriticalNative](https://developer.android.com/reference/dalvik/annotation/optimization/CriticalNative) 
acceleration, as well as "!bang notation" for older Android versions


!!! note
    All generated code can be viewed in the `build/generated/natives` and `build/cmake` directories.<br>
    If you think the code isn't efficient enough and know how to improve it, please let me know in an issue on GitHub.