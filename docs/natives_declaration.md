---
icon: lucide/package
---

# Natives declaration

To declare a new native project, add `create()` in `natives` block:

```kotlin title="build.gradle.kts"
natives {
    create("mylib")
}
```

Without additional configuration this means:

- Search for CMake project `mylib` in `<projectDir>/natives/mylib`
- Generate Kotlin bindings in package `natives.mylib` with name `mylib`

You can run `./gradlew :cmakeInit[Name]` to generate a minimal CMake project.
This task is optional but recommended for getting started.

!!! abstract "In Depth"
    There are two types of project configurations: 

    - For a multiplatform project if `org.jetbrains.kotlin.multiplatform` is applied
    - For a specific platform if `org.jetbrains.kotlin.jvm` or `org.jetbrains.kotlin.js` are applied.

    In a multiplatform project, you can also add a platform-specific native project (e.g. for JS only) as follows:
    ```kotlin
    natives {
        create("myJsLib", SinglePlatform::class) {
            targetSourceSet = "jsMain"
            // ...
        }
    }
    ```

## Project structure

Each native project has a minimum set of files required to work. These are:

- ### CMakeLists.txt
  Configuration file for native compilation. 
  Must contain project with the same name, as specified in their Gradle configuration.
- ### [include/api.h](c_header.md)
  Header file that contains generated API functions and structures.
  It is regenerated each time the Gradle project is synced.
  Native project must implement this header.
- ### [api.ndl](ndl_file.md)
  File that declares how the API looks. Uses NDL syntax.

## Properties

### `classPath`
: Kotlin classpath where API will be generated.
: Default: `natives.[name]`

### `projectDir`
: Directory with CMake project.
: Default: `<projectDir>/natives/[name]`

### `headerFile`
: Generated header file.
: Default: `<projectDir>/natives/[name]/include/api.h`

### `ndlFile`
: NDL file.
: Default: `<projectDir>/natives/[name]/api.ndl`


### `buildType`
: CMake build type. <br> Possible values:
: - `DEBUG`
- `REL_WITH_DEB_INFO`
- `RELEASE`
- `MIN_SIZE_REL`

: Default: `RELEASE`

### `cmakeArgs`
: CMake command-line arguments.

: Default: `[]`

---


## Multiplatform project properties

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
: Default: `jvmMain`
