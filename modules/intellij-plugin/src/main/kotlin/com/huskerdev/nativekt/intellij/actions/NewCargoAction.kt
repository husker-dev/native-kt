package com.huskerdev.nativekt.intellij.actions

import com.huskerdev.nativekt.NativeKtInfo
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.findOrCreateFile

class NewCargoAction: ActionBase() {
    override fun actionPerformed(e: AnActionEvent) = createModule(e, { """
        [cargo]
        project_dir = "."
        lib_name = "$it"
        profile = "release"
    """.trimIndent() }) { root, name ->
        VfsUtil.saveText(root.findOrCreateFile("Cargo.toml"), $$"""
            [package]
            name = "$$name"
            version = "0.1.0"
            edition = "2021"

            [lib]
            crate-type = ["staticlib", "cdylib"]

            [dependencies]
            nativekt = "$${NativeKtInfo.VERSION}"
            
            [build-dependencies]
            nativekt = "$${NativeKtInfo.VERSION}"
        """.trimIndent())

        VfsUtil.saveText(root.findOrCreateFile("build.rs"), """
            fn main() {
                nativekt::generate("nativekt.toml")
                    .expect("Native-Kt generation failed");
            }
        """.trimIndent())

        VfsUtil.saveText(root.findOrCreateFile("src/lib.rs"), """
            #[allow(unused)]
            pub mod $name {
                nativekt::include_source!("$name");
            }
            
            fn hello_world() {
                println!("Hello, World!");
            }
        """.trimIndent())
    }
}