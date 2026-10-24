package com.huskerdev.nativekt.gradle.tasks

import com.huskerdev.nativekt.*
import com.huskerdev.nativekt.gradle.GradlePluginLogger
import com.huskerdev.nativekt.gradle.GradleTaskExecutor
import com.huskerdev.nativekt.printers.c.CApiHeaderPrinter
import com.huskerdev.nativekt.printers.cpp.CppApiHeaderPrinter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class ApiGenTask @Inject constructor(
    private val execOps: ExecOperations
): DefaultTask() {
    @get:Input abstract var context: String

    @TaskAction
    fun action() {
        val context = NativeModuleContext.deserialize(
            json = context,
            executor = GradleTaskExecutor(execOps),
            logger = GradlePluginLogger(logger)
        )
        val module = context.module

        when(val buildSystem = module.buildSystem) {
            is BuildSystem.CMake -> {
                val headerFile = buildSystem.headerFile
                when(buildSystem.language) {
                    Language.CPP -> CppApiHeaderPrinter(
                        context = context,
                        target = headerFile,
                        tppTarget = headerFile.parent!!.resolve("${headerFile.nameWithoutExtension}.tpp")
                    )
                    Language.C -> CApiHeaderPrinter(
                        context = context,
                        target = headerFile
                    )
                    else -> Unit
                }
            }
            is BuildSystem.Cargo -> Unit
        }
    }
}