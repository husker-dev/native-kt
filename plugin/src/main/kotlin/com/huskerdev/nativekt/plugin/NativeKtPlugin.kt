package com.huskerdev.nativekt.plugin

import com.huskerdev.nativekt.configurators.configureCommon
import com.huskerdev.nativekt.configurators.configureJvm
import com.huskerdev.nativekt.configurators.configureNative
import com.huskerdev.nativekt.printers.DefPrinter
import com.huskerdev.nativekt.printers.HeaderPrinter
import com.huskerdev.nativekt.printers.KotlinCommonPrinter
import com.huskerdev.nativekt.printers.KotlinNativePrinter
import com.huskerdev.webidl.WebIDL
import com.huskerdev.webidl.jvm.iterator
import com.huskerdev.webidl.resolver.IdlResolver
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import java.io.File

class NativeKtPlugin: Plugin<Project> {
    lateinit var project: Project
    lateinit var config: Configuration

    override fun apply(project: Project) {
        this.project = project
        config = project.extensions.create("native", Configuration::class.java)

        project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            configure()
        }
    }

    private fun configure(){
        val kotlin = project.the<KotlinMultiplatformExtension>()

        val cmakeDir = project.layout.buildDirectory.file("cmake").get().asFile
        val genDir = project.layout.buildDirectory.file("generated/natives").get().asFile

        project.afterEvaluate {
            configureCommon(project, config, kotlin, genDir)

            if (kotlin.targets.any { it is KotlinNativeTarget })
                configureNative(project, config, kotlin, genDir, cmakeDir)

            if (kotlin.targets.any { it is KotlinJvmTarget })
                configureJvm(project, config, kotlin, genDir, cmakeDir)
        }
    }
}