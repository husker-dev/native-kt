import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.intellij.platform)
    alias(libs.plugins.grammarkit)
}


group = "com.huskerdev"
version = "1.0.0"

repositories {
    mavenCentral()
    gradlePluginPortal()

    intellijPlatform {
        defaultRepositories()
    }
}

sourceSets["main"].java.srcDirs("src/main/gen")

dependencies {
    intellijPlatform {
        intellijIdea("2025.3.3")

        bundledPlugin("org.jetbrains.kotlin")
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }

    patchPluginXml {
        sinceBuild.set("243")
        untilBuild.set("*.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

tasks.generateLexer {
    sourceFile = File("src/main/kotlin/com/huskerdev/nativekt/intellij/lexer/ndl.flex")
    targetOutputDir = File("src/main/gen/com/huskerdev/nativekt/intellij/lexer")
}