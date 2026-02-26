package com.huskerdev.nativekt.plugin

import com.huskerdev.nativekt.printers.HeaderPrinter
import com.huskerdev.nativekt.utils.dir
import com.huskerdev.nativekt.utils.idl
import org.gradle.api.DefaultTask
import java.io.File
import javax.inject.Inject

open class InitTask @Inject constructor(
    module: NativeModule
): DefaultTask() {
    init {
        group = "native"
        doLast {
            val dir = module.dir(project)
            dir.mkdirs()
            if(dir.list()!!.isNotEmpty()) {
                project.logger.error("Can not init module: directory '${dir}' is not empty.")
                return@doLast
            }
            File(dir, "src").mkdirs()
            File(dir, "include").mkdirs()

            File(dir, "CMakeLists.txt").writeText($$"""
                cmake_minimum_required(VERSION 3.15)

                project("$${module.name}")
                
                add_library(${PROJECT_NAME} STATIC src/$${module.name}.c)
                
                target_include_directories(${PROJECT_NAME} PRIVATE include)
            """.trimIndent())
            File(dir, "api.ndl").writeText("""
                
                namespace global {
                    void helloWorld();
                };
            """.trimIndent())
            File(dir, "src/${module.name}.c").writeText("""
                #include <api.h>
                #include <stdio.h>
                
                void helloWorld() {
                    printf("Hello, World!\n");
                    fflush(stdout);
                }
            """.trimIndent())
            HeaderPrinter(
                idl = module.idl(project),
                target = File(module.dir(project), "include/api.h"),
                guardName = module.name.uppercase()
            )
        }
    }
}