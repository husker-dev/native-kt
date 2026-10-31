package com.huskerdev.nativekt.intellij.listeners

import com.huskerdev.nativekt.BuildSystem
import com.huskerdev.nativekt.NativeKtModule
import com.huskerdev.nativekt.NativeModuleContext
import com.huskerdev.nativekt.parseModuleFromToml
import com.huskerdev.nativekt.printers.kotlin.*
import com.huskerdev.nativekt.printers.rust.RustPrinter
import com.huskerdev.stdlibx.fs.SyncPath
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.util.ui.update.MergingUpdateQueue
import com.intellij.util.ui.update.Update
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.plugins.gradle.settings.GradleSettingsListener
import kotlin.coroutines.cancellation.CancellationException

@Service(Service.Level.PROJECT)
class NativeKtUpdateService(
    private val project: Project,
    private val scope: CoroutineScope
): Disposable {

    data class Module(
        val configFile: SyncPath,
        val ndlFile: SyncPath,
        val nativeProjectDir: SyncPath,

        val kotlinBuildDir: SyncPath? = null,
        val nativeBuildDir: SyncPath? = null,
    )

    data class KnownDir(
        val configuration: SyncPath,
        val buildDir: SyncPath
    )

    private data class GenerationInput(
        val module: NativeKtModule,
        val ndlText: String,
        val kotlinBuildDir: SyncPath?,
        val nativeBuildDir: SyncPath?,
    )

    private val log = Logger.getInstance(NativeKtUpdateService::class.java)
    private val generationMutex = Mutex()

    private var knownRustDirs: List<KnownDir> = listOf()
    private val kotlinDirsByRoot = mutableMapOf<String, List<KnownDir>>()

    @Volatile
    var modules = listOf<Module>()
        private set

    private val queue = MergingUpdateQueue(
        name = "NativeKt updates",
        mergingTimeSpan = 1000,
        isActive = true,
        modalityStateComponent = null,
        parent = this
    ).setRestartTimerOnAdd(true)

    init {
        // Listen Gradle project unloading
        project.messageBus.connect(this).subscribe(
            GradleSettingsListener.TOPIC, object: GradleSettingsListener {
                override fun onProjectsUnlinked(linkedProjectPaths: Set<String>) {
                    removeGradleRoots(linkedProjectPaths)
                }
            }
        )
    }

    fun replaceGradleDirs(root: String, dirs: List<KnownDir>) {
        val snapshot = dirs.toList()
        scope.launch(Dispatchers.EDT) {
            if (project.isDisposed) return@launch
            kotlinDirsByRoot[root] = snapshot
            update()
        }
    }

    fun replaceRustDirs(dirs: List<KnownDir>) {
        val snapshot = dirs.toList()
        scope.launch(Dispatchers.EDT) {
            if (project.isDisposed) return@launch
            knownRustDirs = snapshot
            update()
        }
    }

    private fun removeGradleRoots(roots: Set<String>) {
        val snapshot = roots.toSet()
        scope.launch(Dispatchers.EDT) {
            if (project.isDisposed) return@launch
            if (kotlinDirsByRoot.keys.removeAll(snapshot)) {
                update()
            }
        }
    }

    private fun update() {
        val knownKotlinDirs = kotlinDirsByRoot.values.flatten()
        val kotlinBuildDirs = knownKotlinDirs.associate { it.configuration to it.buildDir }
        val nativeBuildDirs = knownRustDirs.associate { it.configuration to it.buildDir }

        // Collect known modules
        val modules = (knownKotlinDirs + knownRustDirs)
            .map { it.configuration }.toSet()
            .mapNotNull {
                try {
                    val module = parseModuleFromToml(it)
                    it to Module(
                        configFile = it,
                        ndlFile = module.apiFile,
                        nativeProjectDir = module.buildSystem.projectDir,
                        kotlinBuildDir = kotlinBuildDirs[it],
                        nativeBuildDir = nativeBuildDirs[it],
                    )
                } catch (_: Throwable) { null }
            }.toMap()

        this.modules = modules.values.toList()
    }

    fun schedule(
        configFile: SyncPath
    ) = queue.queue(Update.create(configFile.absoluteString()) {
        scope.launch(Dispatchers.IO) {
            try {
                generationMutex.withLock {
                    ensureActive()
                    val input = withContext(Dispatchers.EDT) {
                        captureInput(configFile)
                    } ?: return@withLock

                    ensureActive()
                    val changedFiles = generate(input)

                    ensureActive()
                    refreshGeneratedFiles(changedFiles)
                }
            } catch (e: ProcessCanceledException) {
                throw e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.warn("Native-Kt generation failed: ${configFile.absoluteString()}", e)
            }
        }
    })

    private fun captureInput(configFile: SyncPath): GenerationInput? {
        if (project.isDisposed) return null

        val moduleDesc = modules.firstOrNull {
            it.configFile.absoluteString() == configFile.absoluteString()
        } ?: return null

        val module = parseModuleFromToml(
            readCurrentText(moduleDesc.configFile),
            moduleDesc.configFile.parent!!,
        )

        val ndlText = readCurrentText(module.apiFile)

        val updatedModule = moduleDesc.copy(
            ndlFile = module.apiFile,
            nativeProjectDir = module.buildSystem.projectDir,
        )
        modules = modules.map { if (it === moduleDesc) updatedModule else it }

        return GenerationInput(
            module = module,
            ndlText = ndlText,
            kotlinBuildDir = moduleDesc.kotlinBuildDir,
            nativeBuildDir = moduleDesc.nativeBuildDir,
        )
    }

    private fun generate(input: GenerationInput): List<SyncPath> {
        val module = input.module
        val changedFiles = arrayListOf<SyncPath>()

        val context = NativeModuleContext(
            module = module,
            buildDir = SyncPath(""),
            executor = ExecutorStub(),
            logger = LoggerStub(),
            ndlText = { input.ndlText },
        )

        // Print Native
        if(input.nativeBuildDir != null) {
            when (context.buildSystem) {
                is BuildSystem.CMake -> Unit
                is BuildSystem.Cargo -> {
                    val target = input.nativeBuildDir.resolve("${module.name}.nativekt.rs")
                    changedFiles += target

                    RustPrinter(
                        context = context,
                        target = target
                    )
                }
            }
        }

        // Print Kotlin
        if(input.kotlinBuildDir != null) {
            val targetDir = input.kotlinBuildDir.resolve(module.name)
            val expectActual = module.expectActual

            buildSet {
                addAll(module.targets)
                add(module.commonTarget)
            }.forEach { target ->
                val kotlinDir = targetDir.resolve("$target/kotlin")
                if(kotlinDir.exists())
                    kotlinDir.deleteRecursively()

                val classPath = context.classPath.replace(".", "/")
                val targetKtFile = kotlinDir.resolve("$classPath/${module.name}.$target.kt")
                changedFiles += targetKtFile

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
        }
        return changedFiles
    }

    private fun refreshGeneratedFiles(paths: List<SyncPath>) {
        val fs = LocalFileSystem.getInstance()

        val roots = paths.mapNotNull { path ->
            var current: java.io.File? = java.io.File(path.absoluteString())
            var found: com.intellij.openapi.vfs.VirtualFile? = null

            while (current != null && found == null) {
                found = fs.findFileByIoFile(current)
                current = current.parentFile
            }
            found
        }.distinct()

        if (roots.isNotEmpty()) {
            VfsUtil.markDirtyAndRefresh(
                true,
                true,
                true,
                *roots.toTypedArray(),
            )
        }
    }

    private fun readCurrentText(path: SyncPath): String {
        return LocalFileSystem.getInstance()
            .findFileByPath(path.absoluteString())
            ?.let { FileDocumentManager.getInstance().getCachedDocument(it) }
            ?.text
            ?: path.readString()
    }

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
