package com.huskerdev.nativekt.intellij.actions

import com.huskerdev.nativekt.NativeKtInfo
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.findOrCreateFile

class NewCppAction: ActionBase() {
    override fun actionPerformed(e: AnActionEvent) = createModule(e, { """
        [cmake]
        language = "cpp"
        library_name = "$it"
        project_dir = "."
        profile = "Release"
    """.trimIndent() }) { root, name ->
        VfsUtil.saveText(root.findOrCreateFile("CMakeLists.txt"), $$"""
            cmake_minimum_required(VERSION 3.24)

            project("$$name" LANGUAGES CXX)
            
            set(CMAKE_CXX_STANDARD 17)
            set(CMAKE_CXX_STANDARD_REQUIRED ON)
            
            # Load dependencies
            include(FetchContent)
            FetchContent_Declare(
                    nativekt
                    GIT_REPOSITORY https://github.com/husker-dev/native-kt-cmake.git
                    GIT_TAG        $${NativeKtInfo.VERSION}
            )
            FetchContent_MakeAvailable(nativekt)
            
            # Create library
            add_library($$name OBJECT src/source.cpp)
            
            # Link nativekt
            nativekt_generate("nativekt.toml")
            target_link_libraries($$name PRIVATE NativeKt::bindings)
        """.trimIndent())

        VfsUtil.saveText(root.findOrCreateFile("src/source.cpp"), """
            #include <nativekt.hpp>
            #include <iostream>
            
            void hello_world() {
                std::cout << "Hello, World!" << std::endl;
            }
        """.trimIndent())
    }
}