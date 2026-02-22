# Basics

This project allows you to integrate almost any C/C++ project into your Kotlin Multiplatform code.

The idea behind this project is very similar to native integration in Android.<br>
You only specify the CMake configuration, and the plugin will do everything for you.

We support all Kotlin targets:

- Kotlin/JVM
- Kotlin/JS
- Kotlin/Native

!!! note
    All generated code can be viewed in the `build/generated/natives` and `build/cmake` directories.<br>
    If you think the code isn't efficient enough and know how to improve it, please let us know in an issue on GitHub.

# Requirements



Regardless of platform, you need to have [`CMake`](https://cmake.org/) installed.

<div class="grid cards" markdown>

-   __Windows__

    ---

    `mingw64` (MSYS2) with `clang` and `make`. 
    
    !!! tip "Also check records in Path:"

        - `C:\msys64\mingw64\bin`
        - `C:\msys64\clang64\bin`

-   __macOS__

    ---

    Installed `Xcode Command Line Tools` or `Xcode` application.

-   __Linux__

    ---

    It is required to have `clang` and `make` installed. Please follow your own distribution instructions.
</div>



### Target requirements

<div class="grid cards" markdown>

-   __Android__

    ---

    - **Android SDK** with `ANDROID_HOME` in env
    - **NDK** with specified version.
    
    !!! tip
        You can simply install [Android Studio](https://developer.android.com/studio) and set up [NDK](https://developer.android.com/studio/projects/install-ndk) in UI.

-   __JS__

    ---

    [Emscripten](https://emscripten.org/docs/getting_started/downloads.html) is required to be installed.
    `EMSDK` is required to locate the SDK.

</div>

!!! info
    Plugin uses Clang for compilation. <br>
    If your code depends on the another compiler, it may cause problems.