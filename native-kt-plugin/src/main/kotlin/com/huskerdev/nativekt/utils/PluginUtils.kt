package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.plugin.NativeModule
import com.huskerdev.nativekt.plugin.TargetType
import com.huskerdev.webidl.WebIDL
import com.huskerdev.webidl.jvm.iterator
import com.huskerdev.webidl.resolver.IdlResolver
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import java.io.File
import kotlin.text.trim

fun NativeModule.dir(project: Project): File =
    projectDir?.get()?.asFile
        ?: project.file("src/nativeInterop/$name")

fun NativeModule.idlFile(project: Project): File =
    File(dir(project), "api.idl")

fun NativeModule.idl(project: Project): IdlResolver =
    WebIDL.resolve(idlFile(project).reader().iterator())

fun Project.exec(command: String, workingDir: File? = null, silent: Boolean = false): String {
    return project.providers.exec {
        isIgnoreExitValue = true
        if(workingDir != null)
            this.workingDir = workingDir

        if(!Os.isFamily(Os.FAMILY_WINDOWS))
            commandLine("/bin/bash", "-c", command)
        else
            commandLine("cmd.exe", "/c", command)
    }.run {
        val output = standardOutput.asText.get()
        if(!silent)
            println(output)
        if(result.get().exitValue != 0)
            throw Exception("Failed to execute command (code=${result.get().exitValue}): \n$command\nError:\n${standardError.asText.get()}")
        output.trim()
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