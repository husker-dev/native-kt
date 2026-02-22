# Gradle setup

Gradle is the only supported build tool.

To integrate it into your project, simply add the plugin:

```kotlin title="build.gradle.kts"
plugins {
   id("com.huskerdev.native-kt") version "2.0.0"
}
```

Then declare a native project:

```kotlin title="build.gradle.kts"
native {
    create("mylib") {
        // ...
    }
}
```

This method will create a project for all source sets (targets) in the project. 

If you want to create a project for just one source set, then do this:

```kotlin title="build.gradle.kts"
native {
    create("myJvmLib", SinglePlatform::class) {
        // ...
    }
}
```

## Plugin properties

### `useJVMCI`
: Specifies whether to generate JVM integration with `JVMCI`.
: Default: `false`

### `useForeign`
: Specifies whether to generate JVM integration with `Foreign Function & Memory API`.
: Default: `true`

### `useCoroutines`
: When enabled, plugin will generate suspend function for library loading.
: Default: `true`

### `useUniversalMacOSLib`
: When enabled, on macOS plugin will compile binaries as a universal library. <br> (for both `arm64` and `x86` in one file).
: Default: `false`

### `ndkVersion`
: Specifies NDK version. Required to be filled when adding an Android plugin.
: Default: `Not specified`

### `androidTargets`
: Specifies android architectures to be compiled using NDK. <br>Typically this setting does not need to be changed
: Default: `arm64-v8a`, `armeabi-v7a`, `x86_64`

---


## Common project properties

### `classPath`
: Kotlin classpath where API will be generated.
: Default: `natives.[name]`

### `projectDir`
: Directory with CMake project.
: Default: `src/nativeInterop/[name]`

### `buildType`
: CMake build type. <br> Possible values:
: - `DEBUG`
- `REL_WITH_DEB_INFO`
- `RELEASE`
- `MIN_SIZE_REL`

: Default: `RELEASE`

---


## Multi-target project properties

!!! note "Specified in:"
    ```kotlin
    create("mylib") { 
        // ...
    }
    ```

### `commonSourceSet`
: SourceSet that will have 'expect' api
: Default: `commonMain`

### `stubSourceSets`
: SourceSets with stub (empty implementation)
: Default: `null`

### `targetSourceSets`
: SourceSets with implementation
: Default: all default source sets

---


## Single-target project properties

!!! note "Specified in:"
    ```kotlin
    create("mylib", SinglePlatform::class) { 
        // ...
    }
    ```

### `targetSourceSet`
: SourceSet with implementation
: Default: `TARGET_JVM` (jvmMain)
