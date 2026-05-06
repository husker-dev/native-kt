---
icon: simple/gradle
---

# Gradle configuration

Add the plugin to your `build.gradle` file:

```kotlin title="build.gradle.kts"
plugins {
   id("com.huskerdev.native-kt") version "2.0.2"
}
```

All plugin configuration goes inside the `natives` block:
```kotlin
natives {
    // ...
}
```

!!! Warning
    It must be configured after `kotlin {}` configuration and the registration of its targets.

## Plugin properties

### Common

### `useCoroutines`
: When enabled, the plugin generates a suspend function for library loading.
: Default: `true`

### `applyRuntime`
: When enabled, the plugin automatically applies the runtime to the common or main source set.
: Default: `true`

---

### JVM-related

### `useJvmRecord`
: Allows the use of the `@JvmRecord` annotation in generated Kotlin code.
: Default: `true`

### `useUniversalMacOSLib`
: When enabled, the plugin compiles a universal binary on macOS, combining both `arm64` and `x86_64` architectures into a single file.
: Default: `false`

### `useJNI`
: Specifies whether to generate integration with `JNI`.
: Default: `true`

### `useForeign`
: Specifies whether to generate integration with `Foreign Function & Memory API`.
: Default: `true`

### `useJVMCI`
: Specifies whether to generate integration with `JVMCI`.
: Default: `true`

---


### JS/Wasm-related

### `useJsBigInt`
: Enables `BigInt` support in the generated code. When disabled, `long` primitives cannot be passed to or from native code.
: Default: `false`

---

### Android-related

### `ndkVersion`
: Specifies the NDK version to use.
: Default: `latest`

### `androidTargets`
: Specifies the Android architectures to compile. Typically this setting does not need to be changed.
: Default: `arm64-v8a`, `armeabi-v7a`, `x86_64`

### `useAndroidCriticalNative`
: Specifies whether to use the `@CriticalNative` annotation.
: Default: `true`

### `applyAndroidCriticalStub`
: When enabled, the plugin automatically applies a stub for the `@CriticalNative` annotation to allow compilation with SDK versions below 34.
: Default: `true`

---
