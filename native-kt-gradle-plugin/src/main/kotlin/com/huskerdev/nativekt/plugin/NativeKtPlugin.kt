package com.huskerdev.nativekt.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

@Suppress("unused")
class NativeKtPlugin: Plugin<Project> {
    lateinit var project: Project
    lateinit var extension: NativeKtExtension

    override fun apply(project: Project) {
        this.project = project
        extension = project.extensions.create("native", NativeKtExtension::class.java)

        val buildDir = project.layout.buildDirectory.get().asFile
        val cmakeDir = File(buildDir, "cmake")
        val srcGenDir = File(buildDir, "generated/natives")

        project.plugins.withId("com.android.kotlin.multiplatform.library") {
            initAndroid(cmakeDir, srcGenDir)
        }

        project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            configureKotlin(cmakeDir, srcGenDir)
        }
    }
}