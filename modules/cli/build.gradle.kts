import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
        implementation(libs.stdlibx.process)
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