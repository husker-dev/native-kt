package com.huskerdev.nativekt.plugin

import com.huskerdev.nativekt.NDLEnv
import com.huskerdev.nativekt.printers.c.CApiHeaderPrinter
import com.huskerdev.webidl.WebIDL
import com.huskerdev.webidl.jvm.iterator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import java.io.File

abstract class InitTask: DefaultTask() {
    @get:Input abstract var dir: String
    @get:Input abstract var moduleName: String

    init {
        group = NATIVE_TASK_GROUP
        doLast {
            val dir = File(dir)

            dir.mkdirs()
            if(dir.list()!!.isNotEmpty()) {
                project.logger.error("Can not init module: directory '${dir}' is not empty.")
                return@doLast
            }

            File(dir, "src").mkdirs()
            File(dir, "include").mkdirs()

            File(dir, "CMakeLists.txt").writeText($$"""
                cmake_minimum_required(VERSION 3.15)

                project("$$moduleName")
                
                add_library(${PROJECT_NAME} STATIC src/$$moduleName.c)
                
                target_include_directories(${PROJECT_NAME} PRIVATE include)
            """.trimIndent())
            File(dir, "api.ndl").writeText("""
                
                namespace global {
                    void helloWorld();
                };
            """.trimIndent())
            File(dir, "src/$moduleName.c").writeText("""
                #include <api.h>
                #include <stdio.h>
                
                void helloWorld() {
                    printf("Hello, World!\n");
                    fflush(stdout);
                }
            """.trimIndent())

            val idl = WebIDL.resolve(
                iterable = File(dir, "api.ndl").reader().iterator(),
                env = NDLEnv()
            )

            CApiHeaderPrinter(
                idl = idl,
                target = File(dir, "include/api.h"),
                guardName = moduleName.uppercase()
            )
        }
    }
}