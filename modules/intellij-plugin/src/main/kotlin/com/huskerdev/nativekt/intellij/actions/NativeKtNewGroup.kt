package com.huskerdev.nativekt.intellij.actions

import com.huskerdev.nativekt.NativeKtInfo
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.createContext
import com.huskerdev.nativekt.parseModuleFromToml
import com.huskerdev.nativekt.printers.c.CApiHeaderPrinter
import com.huskerdev.nativekt.printers.cpp.CppApiHeaderPrinter
import com.huskerdev.stdlibx.fs.SyncPath
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findOrCreateDirectory
import com.intellij.openapi.vfs.findOrCreateFile
import com.intellij.openapi.vfs.isFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtScriptInitializer
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

class NativeKtNewGroup: DefaultActionGroup() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val directory = e.getData(CommonDataKeys.VIRTUAL_FILE)!!

        e.presentation.isEnabledAndVisible =
            e.project != null &&
            (directory.isGradleModule() || (directory.name == "natives" && directory.parent.isGradleModule()) )
    }
}

private fun VirtualFile.isGradleModule(): Boolean {
    val buildFile = findChild("build.gradle.kts")
        ?: findChild("build.gradle")
        ?: return false
    return buildFile.isFile && buildFile.exists()
}

private fun createModule(e: AnActionEvent, buildSystemPart: String, createFiles: (VirtualFile, String) -> Unit) {
    val project = e.project ?: return
    var projectRoot = e.getData(CommonDataKeys.VIRTUAL_FILE)!!

    if(!projectRoot.isGradleModule()) // for 'natives' directory
        projectRoot = projectRoot.parent
    if(!projectRoot.isGradleModule())
        return

    val name = Messages.showInputDialog(
        project,
        "Enter name:",
        "New Native Module",
        Messages.getQuestionIcon(),
        "",
        object : InputValidator {
            override fun checkInput(inputString: String) =
                inputString.isNotBlank() && projectRoot.findChild("natives/$inputString") == null

            override fun canClose(inputString: String) =
                checkInput(inputString)
        }
    ) ?: return

    WriteCommandAction.runWriteCommandAction(project) {
        // Create module
        val moduleRoot = projectRoot.findOrCreateDirectory("natives/$name")

        // api.ndl
        VfsUtil.saveText(moduleRoot.findOrCreateFile("api.ndl"), """
            namespace global {
                void helloWorld();
            };
        """.trimIndent())

        // nativekt.toml
        val toml = moduleRoot.findOrCreateFile("nativekt.toml")
        VfsUtil.saveText(toml, buildString {
            append($$"""
                # $scheme:nativekt
                
                [description]
                name = "$$name"
                api_file = "api.ndl"
            
            
            """.trimIndent())
            append(buildSystemPart)
            append("""
                
                
                [kotlin]
                package = "com.example"
                use_coroutines = false
        
                [jvm]
                use_records = true
                backends = ["jni", "foreign", "jvmci"]
        
                [js]
                use_bigint = true
                target = "web"
        
                [android]
                ndk_version = "latest"
                abis = ["arm64-v8a", "armeabi-v7a", "x86_64"]
                critical_natives = true
        
                [compilation]
                common_target = "common"
                targets = [
                    "jvm",
                    "android",
        
                    "js",
                    "wasmJs",
        
                    "mingwX64",
        
                    "macosArm64",
                    "macosX64",
        
                    "linuxArm64",
                    "linuxX64",
        
                    "iosArm64",
                    "iosX64",
                    "iosSimulatorArm64",
        
                    "watchosX64",
                    "watchosArm32",
                    "watchosArm64",
                    "watchosDeviceArm64",
                    "watchosSimulatorArm64",
        
                    "tvosX64",
                    "tvosArm64",
                    "tvosSimulatorArm64",
        
                    "androidNativeX64",
                    "androidNativeX86",
                    "androidNativeArm32",
                    "androidNativeArm64"
                ]
                stubs = []
            """.trimIndent())
        })

        // Create module's project files
        createFiles(moduleRoot, name)

        // Add to build.gradle.kts
        val buildGradleFile = projectRoot.findChild("build.gradle.kts")
        if(buildGradleFile?.exists() == true) {
            val factory = KtPsiFactory(project)
            val file = PsiManager.getInstance(project).findFile(buildGradleFile) as KtFile

            val calls = file.script?.blockExpression?.statements
                ?.map { if(it is KtScriptInitializer) it.body else null }
                ?.filterIsInstance<KtCallExpression>()

            // Add in 'natives {}' block
            val nativeKtBlock = calls
                ?.firstOrNull { it.referenceExpression()?.text == "natives" }
                ?.lambdaArguments
                ?.singleOrNull()
                ?.getLambdaExpression()
                ?.bodyExpression
                ?: run {
                    val expression = factory.createExpression("natives {}") as KtCallExpression
                    file.add(expression)
                    expression
                        .lambdaArguments
                        .single()
                        .getLambdaExpression()!!
                        .bodyExpression
                } as KtBlockExpression

            with(nativeKtBlock) {
                addBefore(factory.createNewLine(), rBrace)
                addBefore(factory.createExpression("add(\"$name\")"), rBrace)
                addBefore(factory.createNewLine(), rBrace)
            }

            // Add in 'plugins {}' block
            calls?.firstOrNull { it.referenceExpression()?.text == "plugins" }
                ?.lambdaArguments
                ?.singleOrNull()
                ?.getLambdaExpression()
                ?.bodyExpression
                ?.apply {
                    if(statements.any {
                        it.text.contains("nativekt") || it.text.contains("native-kt")
                    }) return@apply

                    addBefore(factory.createNewLine(), rBrace)
                    addBefore(factory.createExpression(
                        "id(\"com.huskerdev.native-kt\") version \"${NativeKtInfo.VERSION}\""
                    ), rBrace)
                }
        }

        // Mark as module
        /*
        val projectRootFile = projectRoot.toNioPath().toFile()
        val modulesService = project.service<NativeKtModulesService>()

        if(modulesService.holders.none { it.root == projectRootFile }) {
            modulesService.holders += NativeKtModulesService.NativeKtHolder(
                root = projectRootFile,
                nativeProjects = arrayListOf()
            )
        }
        modulesService.holders
            .first { it.root == projectRootFile }
            .nativeProjects += NativeKtModulesService.NativeKtProject(
                SyncPath(toml.canonicalPath!!),
                parseModuleFromToml(SyncPath(toml.canonicalPath!!))
            )

         */

        // Select directory
        ProjectView.getInstance(project).select(null, moduleRoot, true)
    }
}

fun createContext(root: VirtualFile) = createContext(
    buildDir = SyncPath(""),
    module = parseModuleFromToml(SyncPath(root.path).resolve("nativekt.toml")),
    executor = object: NativeModuleContext.CommandExecutor {
        override fun exec(
            context: NativeModuleContext,
            command: String,
            workingDir: SyncPath?,
            silent: Boolean,
            errAsStd: Boolean,
            environment: Map<String, String>?
        ): String = ""
    },
    logger = object: NativeModuleContext.Logger {
        override fun error(text: String) = Unit
        override fun info(text: String) = Unit
    }
)!!

abstract class ActionBase: AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }
}

class NewCAction: ActionBase() {
    override fun actionPerformed(e: AnActionEvent) = createModule(e, """
        [cmake]
        language = "c"
        project_dir = "."
        header_file = "include/api.h"
        profile = "Release"
    """.trimIndent()) { root, name ->
        VfsUtil.saveText(root.findOrCreateFile("CMakeLists.txt"), $$"""
            cmake_minimum_required(VERSION 3.24)

            project("$$name")
            
            add_library(${PROJECT_NAME} STATIC src/source.c)
            
            target_include_directories(${PROJECT_NAME} PRIVATE include)
        """.trimIndent())

        VfsUtil.saveText(root.findOrCreateFile("src/sources.c"), """
            #include <api.h>
            #include <stdio.h>
            
            void hello_world() {
                printf("Hello, World!\n");
                fflush(stdout);
            }
        """.trimIndent())

        CApiHeaderPrinter(
            context = createContext(root),
            target = SyncPath(root.path).resolve("include/api.h")
        )
        root.findOrCreateFile("include/api.h")
    }
}

class NewCppAction: ActionBase() {
    override fun actionPerformed(e: AnActionEvent) = createModule(e, """
        [cmake]
        language = "cpp"
        project_dir = "."
        header_file = "include/api.hpp"
        profile = "Release"
    """.trimIndent()) { root, name ->
        VfsUtil.saveText(root.findOrCreateFile("CMakeLists.txt"), $$"""
            cmake_minimum_required(VERSION 3.24)

            project("$$name" LANGUAGES CXX)
            
            set(CMAKE_CXX_STANDARD 17)
            set(CMAKE_CXX_STANDARD_REQUIRED ON)
            
            add_library(${PROJECT_NAME} STATIC src/source.cpp)
            
            target_include_directories(${PROJECT_NAME} PRIVATE include)
        """.trimIndent())

        VfsUtil.saveText(root.findOrCreateFile("src/sources.c"), $$"""
            #include <api.hpp>
            #include <iostream>
            
            void hello_world() {
                std::cout << "Hello, World!" << std::endl;
            }
        """.trimIndent())

        CppApiHeaderPrinter(
            context = createContext(root),
            target = SyncPath(root.path).resolve("include/api.hpp"),
            tppTarget = SyncPath(root.path).resolve("include/api.tpp")
        )
        root.findOrCreateFile("include/api.hpp")
        root.findOrCreateFile("include/api.tpp")
    }
}

class NewCargoAction: ActionBase() {
    override fun actionPerformed(e: AnActionEvent) = createModule(e, """
        [cargo]
        project_dir = "."
        profile = "release"
    """.trimIndent()) { root, name ->
        VfsUtil.saveText(root.findOrCreateFile("Cargo.toml"), $$"""
            [package]
            name = "$$name"
            version = "0.1.0"
            edition = "2021"

            [lib]
            crate-type = ["staticlib", "cdylib"]

            [dependencies]
            wasm-bindgen = "0.2.129"
        """.trimIndent())

        VfsUtil.saveText(root.findOrCreateFile("src/lib.rs"), $$"""
            // TODO
        """.trimIndent())
    }
}