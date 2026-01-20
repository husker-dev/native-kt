package com.huskerdev.nativekt.plugin

import com.huskerdev.webidl.WebIDL
import com.huskerdev.webidl.jvm.iterator
import com.huskerdev.webidl.resolver.IdlResolver
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import java.io.File
import kotlin.text.trim

fun NativeModule.dir(project: Project): File =
    project.file("src/nativeInterop/$name")

fun NativeModule.idl(project: Project): IdlResolver =
    WebIDL.resolve(File(dir(project), "api.idl").reader().iterator())

fun Project.exec(command: String, workingDir: File? = null){
    println(project.providers.exec {
        if(workingDir != null)
            this.workingDir = workingDir

        if(!Os.isFamily(Os.FAMILY_WINDOWS))
            commandLine("/bin/bash", "-c", command)
        else
            commandLine("cmd.exe", "/c", command)
    }.standardOutput.asText.get().trim())
}

fun currentCompilationTargetName() = when {
    Os.isFamily(Os.FAMILY_WINDOWS) -> "mingwX64"
    Os.isFamily(Os.FAMILY_MAC) -> "macosX64"
    Os.isFamily(Os.FAMILY_UNIX) -> "linuxX64"
    else -> "unknown"
}

fun cmakeGenerator() = when {
    Os.isFamily(Os.FAMILY_WINDOWS) -> "MinGW Makefiles"
    else -> "Unix Makefiles"
}