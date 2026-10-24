package com.huskerdev.nativekt.intellij

import com.huskerdev.nativekt.NativeKtModule
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.parseModuleFromToml
import com.huskerdev.nativekt.printers.kotlin.*
import com.huskerdev.stdlibx.fs.SyncPath
import com.intellij.openapi.Disposable
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.update.MergingUpdateQueue
import com.intellij.util.ui.update.Update
import kotlinx.serialization.json.Json

@Service(Service.Level.APP)
class NativeKtUpdateService: Disposable {

    private val queue = MergingUpdateQueue(
        "NativeKt updates",
        500,
        true,
        null,
        this
    ).setRestartTimerOnAdd(true)

    fun scheduleFromNdl(project: Project, ndlFile: VirtualFile, currentText: String) {
        try {
            val ndlFilePath = SyncPath(ndlFile.canonicalPath!!)
            if (!ndlFilePath.exists())
                return

            getAllModels(project).forEach { model ->
                model.serializedModules.forEach {
                    val module = Json.decodeFromString<NativeKtModule>(it)

                    if (module.apiFile.isSameFileAs(ndlFilePath)) {
                        schedule(
                            project = project,
                            module = module,
                            ndlText = currentText,
                            buildDir = SyncPath(model.buildDir)
                        )
                    }
                }
            }
        } catch (_: Throwable) {}
    }

    fun scheduleFromToml(project: Project, tomlFile: VirtualFile, currentText: String) {
        try {
            val tomlFilePath = SyncPath(tomlFile.canonicalPath!!)
            if (!tomlFilePath.exists())
                return

            getAllModels(project).forEach { model ->
                model.configureFiles.forEach {
                    if (SyncPath(it).isSameFileAs(tomlFilePath)) {
                        val module = parseModuleFromToml(currentText, SyncPath(tomlFile.canonicalPath!!).parent!!)
                        schedule(
                            project = project,
                            module = module,
                            ndlText = module.apiFile.readString(),
                            buildDir = SyncPath(model.buildDir)
                        )
                    }
                }
            }
        } catch (_: Throwable) {}
    }

    private fun schedule(
        project: Project,
        module: NativeKtModule,
        ndlText: String,
        buildDir: SyncPath,
    ) = queue.queue(Update.create(module) {
        WriteCommandAction.runWriteCommandAction(project) {
            try {
                val context = NativeModuleContext(
                    module = module,
                    buildDir = buildDir,
                    executor = ExecutorStub(),
                    logger = LoggerStub(),
                    ndlText = { ndlText }
                )
                val expectActual = module.expectActual
                val targetDir = buildDir.resolve(module.name)

                buildSet {
                    addAll(module.targets)
                    add(module.commonTarget)
                }.forEach { target ->
                    val kotlinDir = targetDir.resolve("$target/kotlin")
                    kotlinDir.deleteRecursively()

                    val targetKtFile = kotlinDir.resolve(
                        "${context.classPath.replace(".", "/")}/${module.name}.$target.kt"
                    )

                    when (target) {
                        "common" -> KotlinCommonPrinter(
                            context = context,
                            target = targetKtFile
                        )

                        "jvm" -> KotlinJvmPrinter(
                            context = context,
                            target = targetKtFile,
                            expectActual = expectActual
                        )

                        "android" -> KotlinAndroidPrinter(
                            context = context,
                            target = targetKtFile,
                            expectActual = expectActual
                        )

                        "js", "wasmJs" -> KotlinJsPrinter(
                            context = context,
                            target = targetKtFile,
                            expectActual = expectActual,
                            isWasm = target == "wasmJs"
                        )

                        "mingwX64",
                        "macosArm64", "macosX64",
                        "linuxArm64", "linuxX64",
                        "iosArm64", "iosX64", "iosSimulatorArm64",
                        "watchosX64", "watchosArm32", "watchosArm64",
                        "watchosDeviceArm64", "watchosSimulatorArm64",
                        "tvosX64", "tvosArm64", "tvosSimulatorArm64",
                        "androidNativeX64", "androidNativeX86",
                        "androidNativeArm32", "androidNativeArm64" ->
                            KotlinNativePrinter(
                                context = context,
                                target = targetKtFile,
                                expectActual = expectActual
                            )

                        else -> Unit
                    }
                }

                VfsUtil.markDirtyAndRefresh(
                    true,
                    true,
                    true,
                    LocalFileSystem.getInstance().findFileByPath(targetDir.absoluteString())
                )
            } catch (_: Throwable) {}
        }
    })

    override fun dispose() = Unit

    private class ExecutorStub: NativeModuleContext.CommandExecutor {
        override fun exec(
            context: NativeModuleContext,
            command: String,
            workingDir: SyncPath?,
            silent: Boolean,
            errAsStd: Boolean,
            environment: Map<String, String>?
        ): String = ""
    }

    private class LoggerStub: NativeModuleContext.Logger {
        override fun error(text: String) = Unit
        override fun info(text: String) = Unit
    }
}