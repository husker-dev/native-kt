import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.konan.target.Family

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.shadow)
    alias(libs.plugins.proguard)
}

group = "com.huskerdev"
version = projectDir.parentFile.parentFile.resolve("VERSION").readText().trim()

kotlin {
    sourceSets.commonMain.dependencies {
        implementation("$group:native-kt-core:$version")
        implementation(libs.clikt)
    }

    jvm {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                jvmTarget = JvmTarget.JVM_11
            }
        }
    }
    listOf(
        macosArm64(),
        mingwX64(),
        linuxX64(),
        linuxArm64(),
    ).forEach {
        it.binaries {
            executable {
                binaryOption("smallBinary", "true")
                entryPoint = "com.huskerdev.nativekt.cli.main"
                baseName = "nativekt-cli-${version}"
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.shadowJar {
    archiveBaseName = "nativekt-cli"
    archiveClassifier = "fat"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "Main-Class" to "com.huskerdev.nativekt.cli.MainKt",
            "Implementation-Title" to "native-kt-cli",
            "Implementation-Version" to project.version
        )
    }
}

tasks.register("proguardJar", proguard.taskClass) {
    group = "build"
    description = "ProGuard task"
    addInput {
        classpath.from(tasks.shadowJar)
    }
    addOutput {
        archiveFile.set(base.libsDirectory.file("nativekt-cli-${project.version}.jar"))
    }
    jdkModules.add("java.base")
    rules.addAll(
        "-dontoptimize",
        "-dontwarn !com.huskerdev.**",
        "-flattenpackagehierarchy",
        "-keep class com.huskerdev.nativekt.cli.MainKt { public static void main(java.lang.String[]); }",
        "-keepattributes Signature,InnerClasses,*Annotation*"
    )
}

val prepareCMakeBundle = tasks.register<Copy>("prepareCMakeBundle") {
    description = "Prepares CMake CLI bundle"

    val bundleVersion = project.version.toString()
    into(layout.buildDirectory.dir("nativekt-cmake"))
    from(file("CMakeLists.txt"))

    kotlin.targets.withType<KotlinNativeTarget>().forEach { target ->
        val executable = target.binaries.getExecutable(NativeBuildType.RELEASE)
        val platform = when (target.name) {
            "linuxArm64" -> "linux-arm64"
            "linuxX64" -> "linux-x64"
            "macosArm64" -> "macos-arm64"
            "mingwX64" -> "windows-x64"
            else -> target.name
        }
        val extension = if (target.konanTarget.family == Family.MINGW) ".exe" else ""

        dependsOn(executable.linkTaskProvider)
        from(executable.outputFile) {
            rename { "nativekt-cli-$bundleVersion-$platform$extension" }
        }
    }
}

tasks.register<Tar>("packCMake") {
    group = "cli"
    description = "Packs CMake CLI bundle as a tar.gz archive"

    val bundleVersion = project.version.toString()
    archiveFileName.set("nativekt-cmake-$bundleVersion.tar.gz")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    compression = Compression.GZIP

    into("nativekt-cmake-$bundleVersion")
    from(prepareCMakeBundle) {
        include("nativekt-cli-*")
        filePermissions {
            unix("755")
        }
    }
    from(prepareCMakeBundle) {
        exclude("nativekt-cli-*")
    }
}

tasks.register<Sync>("packCargoPackage") {
    group = "cli"
    description = "Stages nativekt-build with bundled CLI executables for Cargo packaging"

    val packageVersion = project.version.toString()
    inputs.property("packageVersion", packageVersion)

    into(layout.buildDirectory.dir("nativekt-cargo"))

    // Copy cargo files
    from(file("cargo")) {
        include("Cargo.toml", "README.md", "src/**", "LICENSE*")
        filesMatching("Cargo.toml") {
            filter { line ->
                if (line.startsWith("version = ")) "version = \"$packageVersion\"" else line
            }
        }
    }
    from(rootProject.file("LICENSE"))

    // Copy binaries
    kotlin.targets.withType<KotlinNativeTarget>().forEach { target ->
        val executable = target.binaries.getExecutable(NativeBuildType.RELEASE)
        val platform = when (target.name) {
            "linuxArm64" -> "linux-arm64"
            "linuxX64" -> "linux-x64"
            "macosArm64" -> "macos-arm64"
            "mingwX64" -> "windows-x64"
            else -> target.name
        }
        val extension = if (target.konanTarget.family == Family.MINGW) ".exe" else ""

        dependsOn(executable.linkTaskProvider)
        from(executable.outputFile) {
            into("bin")
            rename { "nativekt-cli-$platform$extension" }
            filePermissions {
                unix("755")
            }
        }
    }
}

tasks.register("packAll") {
    group = "cli"
    description = "Packs all destributions"
    dependsOn("packCargoPackage", "packCMake")
}