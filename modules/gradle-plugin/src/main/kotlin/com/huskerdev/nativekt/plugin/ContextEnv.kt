package com.huskerdev.nativekt

import com.huskerdev.osutils.OS
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import org.gradle.api.Project
import org.gradle.process.ExecOperations
import org.gradle.api.logging.Logger
import org.gradle.api.provider.ProviderFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream

class GradlePluginLogger(
    val logger: Logger
): NativeModuleContext.Logger {
    override fun error(text: String) = logger.error("[nativekt] $text")
    override fun info(text: String) = logger.info("[nativekt] $text")
}

class GradleTaskExecutor private constructor(
    val execObj: Any
): NativeModuleContext.CommandExecutor {

    constructor(execOps: ExecOperations): this(execOps as Any)
    constructor(project: Project): this(project.providers as Any)

    override fun exec(
        context: NativeModuleContext,
        command: String,
        workingDir: PlatformFile?,
        silent: Boolean,
        errAsStd: Boolean,
        environment: Map<String, String>?
    ): String {
        val printExec = DebugKind.PRINT_EXEC in context.debug
        if(printExec) {
            val dir = workingDir?.absolutePath() ?: File("./").absolutePath
            context.logger!!.error("Executed command for '${context.moduleName}': \n\t$command\n\tat: $dir")
        }

        class StringOutputStream(
            private val delegate: OutputStream,
            private val bytes: ByteArrayOutputStream = ByteArrayOutputStream()
        ): OutputStream() {
            override fun write(b: Int) {
                bytes.write(b)
                if(!silent) delegate.write(b)
            }
            override fun write(b: ByteArray, off: Int, len: Int) {
                bytes.write(b, off, len)
                if(!silent) delegate.write(b, off, len)
            }
            override fun flush() = delegate.flush()
            fun toDecodedString() = String(bytes.toByteArray(), Charsets.UTF_8)
        }

        fun String.stripAnsi() = replace(
            "\u001B\\[[0-?]*[ -/]*[@-~]|\u001B][^\u0007]*\u0007".toRegex(),
            ""
        )

        val stdOut = StringOutputStream(System.out)
        val errOut = StringOutputStream(System.err)



        return when(execObj) {
            is ExecOperations -> execObj.exec {
                isIgnoreExitValue = true
                if(workingDir != null)
                    this.workingDir = workingDir.file

                if(OS.current == OS.WINDOWS)
                    commandLine("cmd.exe", "/c", command)
                else
                    commandLine("/bin/bash", "-c", command)

                standardOutput = stdOut
                errorOutput = if(errAsStd) stdOut else errOut
                environment?.forEach(this.environment::put)
            }
            is ProviderFactory -> execObj.exec {
                isIgnoreExitValue = true
                if(workingDir != null)
                    this.workingDir = workingDir.file

                if(OS.current == OS.WINDOWS)
                    commandLine("cmd.exe", "/c", command)
                else
                    commandLine("/bin/bash", "-c", command)

                standardOutput = stdOut
                errorOutput = if(errAsStd) stdOut else errOut
                environment?.forEach(this.environment::put)
            }.result.get()
            else -> throw UnsupportedOperationException()
        }.run {
            if(exitValue != 0) throw Exception(buildString {
                appendLine("Failed to execute command (code=${exitValue}): ")
                appendLine(command)
                if(environment != null) {
                    append("Environment:")
                    environment.forEach { (key, value) -> append("\n   $key = $value") }
                    append("\n")
                }
                appendLine("Error:")
                append(if(errAsStd) stdOut.toDecodedString() else errOut.toDecodedString())
            })
            stdOut.toDecodedString().stripAnsi().trim()
        }
    }
}