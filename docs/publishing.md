---
icon: lucide/cloud-upload
---

# Publishing

Publishing to Maven doesn't require anything special.

The only exception is with the JVM - you need to publish separate packages with platform natives.

To do this, you need to add a `Jar` task to your publications.

```kotlin title="build.gradle.kts"
publishing {
    publications.getByName<MavenPublication>("jvm") {
        artifact(natives.jvmNativesJarTask)
    }
}
```

After this, you can use these natives as follows:

```kotlin title="build.gradle.kts"
kotlin {
    // ...

    sourceSets {
        commonMain.dependencies {
            implementation("${groupId}:${name}:${version}")
        }
        jvmMain.dependencies {
            implementation("${groupId}:${name}-jvm:${version}:windows-x64")
        }
    }
}
```

Available platforms:

OS:

- `windows`
- `macos`
- `linux`

Arch:

- `x64`
- `x86`
- `arm64`
- `riscv`
- `universal` (if universal .dylib is enabled for macOS)
