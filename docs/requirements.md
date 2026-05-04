
# Requirements

### IDEA plugins

To work comfortably with native code, the following plugins are required:

- [Native-Kt Utils](https://plugins.jetbrains.com/plugin/31414-native-kt-utils) (project's plugin)
- [C and C++](https://plugins.jetbrains.com/plugin/28804-clion-c-and-c-)
- [CMake](https://plugins.jetbrains.com/plugin/28794-cmake)
- [Native Build Tools](https://plugins.jetbrains.com/plugin/28796-native-build-tools)


### Platform configuration

Regardless of platform, you need to have [`CMake`](https://cmake.org/) installed.

The project only supports compilation via `clang`, so please follow the instructions below.

<div class="grid cards" markdown>

-   :material-microsoft-windows: &nbsp; __Windows__

    ---

    1. Install [MSYS2](https://www.msys2.org/), open `clang64.exe`
    2. Update packages:
       ```sh
       pacman -Syu
       ```
    3. Install `clang` and `make` packages:
       ```sh
       pacman -S mingw-w64-clang-x86_64-clang mingw-w64-clang-x86_64-make
       ```
       
    4. Check `Path` in environment variables:
       ```
       [YOUR_PATH]\msys2\clang64\bin
       ```
</div>

<div class="grid cards" markdown>

-   :simple-apple: &nbsp; __macOS__

    ---

    1. Install [Xcode Command Line Tools](https://developer.apple.com/download/all/?q=command%20line%20tools) or [Xcode](https://developer.apple.com/xcode/) application.
    2. Run it once
</div>

<div class="grid cards" markdown>

-   :simple-linux: &nbsp; __Linux__

    ---

    It is required to have `clang` and `make` installed. 
    Please follow your own distribution instructions.
</div>


### Additional platform configuration

Some targets require special compilation tools.

<div class="grid cards" markdown>

-   :simple-android: &nbsp; __Android__

    ---
    1. Install [Android SDK](https://developer.android.com/tools/releases/platform-tools)
    2. Install `NDK` via `sdkmanager`
    3. Check `ANDROID_HOME` in environment variables
    
    !!! tip
        You can simply install [Android Studio](https://developer.android.com/studio) and set up [NDK](https://developer.android.com/studio/projects/install-ndk) in UI.
</div>

<div class="grid cards" markdown>

-   :simple-javascript: &nbsp; __Web__

    ---

    1. Install [Emscripten](https://emscripten.org/docs/getting_started/downloads.html)
    2. Check `EMSDK` in environment variables

</div>