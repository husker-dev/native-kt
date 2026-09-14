package com.huskerdev.nativekt.plugin.tasks

import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.plugin.BuildSystem
import com.huskerdev.nativekt.plugin.Language
import com.huskerdev.nativekt.printers.c.CApiHeaderPrinter
import com.huskerdev.nativekt.printers.cpp.CppApiHeaderPrinter
import com.huskerdev.nativekt.printers.rust.RustPrinter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ApiGenTask: DefaultTask() {
    @get:Input abstract var context: NativeModuleContext

    @TaskAction
    fun action() {
        val module = context.module

        when(val buildSystem = module.buildSystem) {
            is BuildSystem.CMake -> {
                val headerFile = buildSystem.headerFile()
                when(buildSystem.language) {
                    Language.CPP -> CppApiHeaderPrinter(
                        context = context,
                        target = headerFile,
                        tppTarget = File(headerFile.parentFile, "${headerFile.nameWithoutExtension}.tpp")
                    )
                    Language.C -> CApiHeaderPrinter(
                        context = context,
                        target = headerFile
                    )
                    else -> Unit
                }
            }
            is BuildSystem.Cargo -> {
                RustPrinter(
                    context = context,
                    target = buildSystem.apiRsFile()
                )
            }
        }
    }
}