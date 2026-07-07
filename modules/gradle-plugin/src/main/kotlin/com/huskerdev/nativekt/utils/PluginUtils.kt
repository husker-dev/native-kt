package com.huskerdev.nativekt.utils

import com.huskerdev.nativekt.NDLEnv
import com.huskerdev.nativekt.TargetType
import com.huskerdev.nativekt.plugin.BuildSystem
import com.huskerdev.nativekt.plugin.NativeProject
import com.huskerdev.webidl.WebIDL
import com.huskerdev.webidl.jvm.iterator
import com.huskerdev.webidl.resolver.*
import com.huskerdev.webidl.resolver.WebIDLBuiltinKind.*
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.process.ExecOperations
import java.io.File
import java.io.OutputStream

enum class Arch {
    X86,
    X64,
    ARM32,
    ARM64,
    RISCV32,
    RISCV64,
    UNKNOWN
    ;
    companion object {
        @JvmStatic fun current() = System.getProperty("os.arch").lowercase().run {
            when {
                matches("^(x8632|x86|i[3-6]86|ia32|x32)$".toRegex()) -> X86
                matches("^(x8664|amd64|ia32e|em64t|x64)$".toRegex()) -> X64
                matches("^(arm|arm32)$".toRegex()) -> ARM32
                equals("aarch64") -> ARM64
                matches("^(riscv|riscv32)$".toRegex()) -> RISCV32
                equals("riscv64") -> RISCV64
                else -> UNKNOWN
            }
        }
    }
}

val File.posixPath: String
    get() = absolutePath.replace("\\", "/")

fun NativeProject.dir(project: Project): File =
    projectDir ?: project.file("natives/$name")

fun NativeProject.getNDLFile(project: Project): File =
    ndlFile ?: File(dir(project), "api.ndl")

fun NativeProject.getHeaderFile(project: Project): File =
    (buildSystem as BuildSystem.CMake).headerFile
        ?: File(dir(project), "include/api.h")

fun NativeProject.getApiRsFile(project: Project): File =
    (buildSystem as BuildSystem.Cargo).apiRsFile
        ?: File(dir(project), "src/nativekt.rs")

fun NativeProject.idl(project: Project) = WebIDL.resolve(
    iterable = getNDLFile(project).reader().iterator(),
    env = NDLEnv()
)

fun TaskProvider<*>.dependsOnReload() {
    // Invoke task when reloading using IDEA
    get().project.rootProject.tasks
        .matching { it.name == "prepareKotlinBuildScriptModel" }
        .configureEach {
            dependsOn(this@dependsOnReload)
        }
}

internal fun locate(execOps: ExecOperations, binary: String): File? {
    return try {
        File(
            execOps.exec(
                when {
                    Os.isFamily(Os.FAMILY_WINDOWS) -> "where $binary"
                    else -> "which $binary"
                },
                silent = true
            )
        ).run { if (exists()) this else null }
    } catch (_: Throwable) {
        null
    }
}


fun ExecOperations.execWithArgsFile(
    command: String,
    args: File,
    workingDir: File? = null,
    silent: Boolean = false,
    errAsStd: Boolean = false
): String {
    val filePath = args.posixPath
    val toExec = when {
        Os.isFamily(Os.FAMILY_WINDOWS) -> "$command @$filePath"
        else -> "$command $(cat $filePath)"
    }
    return exec(toExec, workingDir, silent, errAsStd)
}

fun ExecOperations.exec(
    command: String,
    workingDir: File? = null,
    silent: Boolean = false,
    errAsStd: Boolean = false
): String {
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
        errorOutput = if(errAsStd) stdOut else errOut
    }.run {
        if(exitValue != 0)
            throw Exception("Failed to execute command (code=${exitValue}): \n$command\nError:\n${if(errAsStd) stdOut else errOut}")
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

fun File.fresh(): File {
    deleteRecursively()
    mkdirs()
    return this
}

fun validateIDL(idl: IdlResolver) {
    fun checkType(type: ResolvedIdlType, isInsideArray: Boolean = false) {
        when(type) {
            is ResolvedIdlType.Union ->
                throw UnsupportedOperationException("Union types are not supported: $type")
            is ResolvedIdlType.Default -> when(val declaration = type.declaration) {
                is BuiltinIdlDeclaration -> when(declaration.kind) {
                    ANY,
                    MUTABLE_LIST,
                    MAP,
                    PROMISE,
                    USV_STRING,
                    BIG_INT,
                    UNRESTRICTED_FLOAT,
                    UNRESTRICTED_DOUBLE,
                    BYTE_SEQUENCE,
                    OBJECT -> throw UnsupportedOperationException("Unsupported type: ${declaration.kind}")
                    STRING,
                    VOID,
                    BOOLEAN,
                    CHAR,
                    INT,
                    UNSIGNED_INT,
                    FLOAT,
                    DOUBLE,
                    BYTE,
                    UNSIGNED_BYTE,
                    SHORT,
                    UNSIGNED_SHORT,
                    LONG,
                    UNSIGNED_LONG -> Unit // ok
                    LIST -> {
                        if(isInsideArray)
                            throw UnsupportedOperationException("Nested arrays are not supported: $type")
                        checkType(type.parameters[0], true)
                    }
                }
                is ResolvedIdlCallbackFunction -> {
                    if(isInsideArray)
                        throw UnsupportedOperationException("Callback arrays are not supported yet")
                }
                is ResolvedIdlInterface,
                is ResolvedIdlDictionary,
                is ResolvedIdlEnum,
                is ResolvedIdlNamespace,
                is ResolvedIdlTypeDef -> Unit // ok
            }
            is ResolvedIdlType.Void -> Unit // ok
        }
    }
    fun checkName(name: String) {
        if(name.startsWith("_"))
            throw UnsupportedOperationException("Identifiers cannot begin with an underscore: $name")
    }
    fun checkField(field: ResolvedIdlField) {
        checkType(field.type)
        checkName(field.name)
    }
    fun checkOperation(operation: ResolvedIdlOperation) {
        checkType(operation.type)
        checkName(operation.name)
        operation.args.forEach { checkField(it) }
    }

    idl.namespaces.values.forEach { namespace ->
        if(namespace.name != "global")
            throw UnsupportedOperationException("Only 'global' namespace is supported yet")
        namespace.operations.forEach { checkOperation(it) }
    }

    idl.callbacks.values.forEach { callback ->
        checkType(callback.type)
        checkName(callback.name)
        callback.args.forEach { checkField(it) }
    }

    idl.dictionaries.values.forEach { dictionary ->
        checkName(dictionary.name)
        dictionary.fields.forEach { checkField(it) }
    }

    idl.enums.values.forEach { enum ->
        checkName(enum.name)
        enum.elements.forEach { checkName(it) }
    }

    idl.globalOperators().forEach { operation ->
        checkType(operation.type)
        checkName(operation.name)
        operation.args.forEach { checkField(it) }
    }
}