package com.huskerdev.nativekt.intellij.actions

import com.huskerdev.nativekt.NativeKtInfo
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.findOrCreateFile

class NewCAction: ActionBase() {
    override fun actionPerformed(e: AnActionEvent) = createModule(e, {"""
        [cmake]
        language = "c"
        library_name = "$it"
        project_dir = "."
        profile = "Release"
    """.trimIndent() }) { root, name ->
        VfsUtil.saveText(root.findOrCreateFile("CMakeLists.txt"), $$"""
            cmake_minimum_required(VERSION 3.24)

            project("$$name")
            
            # Load dependencies
            include(FetchContent)
            FetchContent_Declare(
                    nativekt
                    GIT_REPOSITORY https://github.com/husker-dev/native-kt-cmake.git
                    GIT_TAG        $${NativeKtInfo.VERSION}
            )
            FetchContent_MakeAvailable(nativekt)
            
            # Create library
            add_library($$name OBJECT src/source.c)
            target_compile_options($$name PRIVATE -Wno-initializer-overrides)
            
            # Link nativekt
            nativekt_generate("nativekt.toml")
            target_link_libraries($$name PRIVATE NativeKt::bindings)
        """.trimIndent())

        VfsUtil.saveText(root.findOrCreateFile("src/source.c"), """
            #include <nativekt.h>
            #include <stdio.h>
            
            void hello_world() {
                printf("Hello, World!\n");
                fflush(stdout);
            }
        """.trimIndent())
        root.findOrCreateFile("include/api.h")
    }
}