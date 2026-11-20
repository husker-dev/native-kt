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
        val directory = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: let {
            e.presentation.isEnabledAndVisible = false
            return
        }

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

internal fun createModule(e: AnActionEvent, buildSystemPart: (String) -> String, createFiles: (VirtualFile, String) -> Unit) {
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
                inputString.isNotBlank()
                        && projectRoot.findFileByRelativePath("natives/$inputString") == null
                        && Regex("[A-Za-z_][A-Za-z0-9_]*").matches(inputString)

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
            append(buildSystemPart(name))
            append("""
                
                
                [kotlin]
                package = "com.example"
                use_coroutines = false
                internal_loader = false
        
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
                
                [apple]
                min_ios = "15.0"
                min_macos = "12.0"
                min_tvos = "15.0"
                min_watchos = "9.0"
        
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
                    val expression = file.add(
                        factory.createExpression("natives {}")
                    ) as KtCallExpression
                    expression
                        .lambdaArguments
                        .single()
                        .getLambdaExpression()!!
                        .bodyExpression
                } as KtBlockExpression

            with(nativeKtBlock) {
                addBefore(factory.createNewLine(), rBrace)
                addBefore(factory.createExpression("add(\"$name\")"), rBrace)
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

        // Select directory
        ProjectView.getInstance(project).select(null, moduleRoot, true)
    }
}

abstract class ActionBase: AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }
}