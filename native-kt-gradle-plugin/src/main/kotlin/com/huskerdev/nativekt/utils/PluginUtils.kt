package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.NDLEnv
import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.plugin.NativeModule
import com.huskerdev.webidl.WebIDL
import com.huskerdev.webidl.jvm.iterator
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.process.ExecOperations
import java.io.File
import java.io.OutputStream

fun NativeModule.dir(project: Project): File =
    projectDir?.get()?.asFile
        ?: project.file("natives/$name")

fun NativeModule.idlFile(project: Project): File =
    File(dir(project), "api.ndl")

fun NativeModule.idl(project: Project) = WebIDL.resolve(
    iterable = idlFile(project).reader().iterator(),
    env = NDLEnv()
)

fun TaskProvider<*>.dependsOnReload() {
    // Invoke task when reloading using IDEA
    get().project.tasks.matching { it.name == "prepareKotlinIdeaImport" }.configureEach {
        dependsOn(this@dependsOnReload)
    }
}

fun ExecOperations.exec(command: String, workingDir: File? = null, silent: Boolean = false): String {
    class StringOutputStream(
        private val delegate: OutputStream,
        private val string: StringBuilder = StringBuilder()
    ): OutputStream() {
        override fun write(b: Int) {
            string.append(b.toChar())
            if(!silent) delegate.write(b)
        }
        override fun flush() = delegate.flush()
        override fun toString() = string.toString()
    }
    val stdOut = StringOutputStream(System.out)
    val errOut = StringOutputStream(System.err)

    return exec {
        isIgnoreExitValue = true
        if(workingDir != null)
            this.workingDir = workingDir

        if(!Os.isFamily(Os.FAMILY_WINDOWS))
            commandLine("/bin/bash", "-c", command)
        else
            commandLine("cmd.exe", "/c", command)

        standardOutput = stdOut
        errorOutput = errOut
    }.run {
        if(exitValue != 0)
            throw Exception("Failed to execute command (code=${exitValue}): \n$command\nError:\n${stdOut}")
        stdOut.toString().trim()
    }
}

fun currentTargetType(): TargetType = when {
    Os.isFamily(Os.FAMILY_WINDOWS) -> TargetType.MINGW_X64
    Os.isFamily(Os.FAMILY_MAC) -> when {
        Os.isArch("aarch64") -> TargetType.MACOS_ARM64
        else -> TargetType.MACOS_X64
    }
    Os.isFamily(Os.FAMILY_UNIX) -> when {
        Os.isArch("aarch64") -> TargetType.LINUX_ARM64
        else -> TargetType.LINUX_X64
    }
    else -> throw UnsupportedOperationException()
}

fun File.fresh(){
    deleteRecursively()
    mkdirs()
}