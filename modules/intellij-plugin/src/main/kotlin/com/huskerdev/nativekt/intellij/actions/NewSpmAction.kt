package com.huskerdev.nativekt.intellij.actions

import com.huskerdev.nativekt.NativeKtInfo
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.findOrCreateFile

class NewSpmAction: ActionBase() {
    override fun actionPerformed(e: AnActionEvent) = createModule(e, { """
        [spm]
        project_dir = "."
        static_lib_name = "${it}Static"
        dynamic_lib_name = "${it}Dynamic"
        profile = "release"
    """.trimIndent() }) { root, name ->
        VfsUtil.saveText(root.findOrCreateFile("Package.swift"), $$"""
            // swift-tools-version: 5.9
            import PackageDescription

            let package = Package(
                name: "$$name",
                products: [
                    .library(name: "$${name}Static", type: .static, targets: ["$$name"]),
                    .library(name: "$${name}Dynamic", type: .dynamic, targets: ["$$name"]),
                ],
                dependencies: [
                    .package(url: "https://github.com/husker-dev/native-kt-swift.git",
                             from: "$${NativeKtInfo.VERSION}")
                ],
                targets: [
                    .target(
                        name: "$$name",
                        plugins: [.plugin(name: "NativeKtPlugin", package: "nativekt-swift")]
                    ),
                ]
            )
        """.trimIndent())

        VfsUtil.saveText(root.findOrCreateFile("Sources/$name/Main.swift"), """
            func helloWorld() { 
                print("Hello world")
            }
        """.trimIndent())
    }
}