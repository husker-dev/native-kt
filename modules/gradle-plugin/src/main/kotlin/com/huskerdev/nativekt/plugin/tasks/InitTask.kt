package com.huskerdev.nativekt.plugin.tasks

import com.huskerdev.nativekt.plugin.*
import com.huskerdev.nativekt.printers.c.CApiHeaderPrinter
import com.huskerdev.nativekt.printers.cpp.CppApiHeaderPrinter
import com.huskerdev.nativekt.printers.rust.RustPrinter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class InitTask: DefaultTask() {
    @get:Input abstract var extension: NativeKtCommonInterface
    @get:Input abstract var module: NativeProject
    @get:Input abstract var buildDir: String

    private lateinit var moduleName: String
    private lateinit var dir: File
    private lateinit var ndlFile: File

    init {
        group = NATIVE_TASK_GROUP
    }

    @TaskAction
    fun action() {
        moduleName = module.name
        dir = module.projectDir
        ndlFile = module.ndlFile()

        dir.mkdirs()
        if(dir.list()!!.isNotEmpty()) {
            logger.error("Could not init module: directory '$dir' is not empty.")
            return
        }

        if(!ndlFile.exists()) {
            ndlFile.writeText("""
                namespace global {
                    void helloWorld();
                };
            """.trimIndent())
        }

        when (val buildSystem = module.buildSystem) {
            is BuildSystem.CMake -> when (buildSystem.language) {
                Language.C -> printCmakeCProject()
                Language.CPP -> printCmakeCppProject()
                else -> throw UnsupportedOperationException()
            }
            is BuildSystem.Cargo -> printCargoRustProject()
        }
    }

    private fun createContext() =
        com.huskerdev.nativekt.createContext(File(buildDir), extension, module)
            ?: throw UnsupportedOperationException("Could not create context")

    private fun printCmakeCProject() {
        File(dir, "src").mkdirs()
        File(dir, "include").mkdirs()
        File(dir, "CMakeLists.txt").writeText($$"""
            cmake_minimum_required(VERSION 3.15)

            project("$$moduleName")
            
            add_library(${PROJECT_NAME} STATIC src/$$moduleName.c)
            
            target_include_directories(${PROJECT_NAME} PRIVATE include)
        """.trimIndent())
        File(dir, "src/$moduleName.c").writeText("""
            #include <api.h>
            #include <stdio.h>
            
            void hello_world() {
                printf("Hello, World!\n");
                fflush(stdout);
            }
        """.trimIndent())

        CApiHeaderPrinter(
            context = createContext(),
            target = (module.buildSystem as BuildSystem.CMake).headerFile()
        )
    }

    private fun printCmakeCppProject() {
        File(dir, "src").mkdirs()
        File(dir, "include").mkdirs()
        File(dir, "CMakeLists.txt").writeText($$"""
            cmake_minimum_required(VERSION 3.15)

            project("$$moduleName" LANGUAGES CXX)
            
            set(CMAKE_CXX_STANDARD 17)
            set(CMAKE_CXX_STANDARD_REQUIRED ON)
            
            add_library(${PROJECT_NAME} STATIC src/$$moduleName.cpp)
            
            target_include_directories(${PROJECT_NAME} PRIVATE include)
        """.trimIndent())
        File(dir, "src/$moduleName.cpp").writeText("""
            #include <api.hpp>
            #include <iostream>
            
            void hello_world() {
                std::cout << "Hello, World!" << std::endl;
            }
        """.trimIndent())

        val headerFile = (module.buildSystem as BuildSystem.CMake).headerFile()
        CppApiHeaderPrinter(
            context = createContext(),
            target = headerFile,
            tppTarget = File(headerFile.parentFile, headerFile.nameWithoutExtension + ".tpp")
        )
    }

    private fun printCargoRustProject() {
        File(dir, "src").mkdirs()
        File(dir, "Cargo.toml").writeText($$"""
            [package]
            name = "$$moduleName"
            version = "0.1.0"
            edition = "2021"
            
            [lib]
            crate-type = ["staticlib", "cdylib"]
            
            [dependencies]
            wasm-bindgen = "~0.2.128"
        """.trimIndent())
        File(dir, "src/lib.rs").writeText("""
            use crate::nativekt::*;

            mod nativekt;
            
            fn hello_world() {
                println!("Hello, World!");
            }
        """.trimIndent())

        RustPrinter(
            context = createContext(),
            target =  (module.buildSystem as BuildSystem.Cargo).apiRsFile()
        )
    }
}